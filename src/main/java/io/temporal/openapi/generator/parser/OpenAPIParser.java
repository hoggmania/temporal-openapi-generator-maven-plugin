package io.temporal.openapi.generator.parser;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.temporal.openapi.generator.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Parser for OpenAPI specifications to extract operation models
 */
public class OpenAPIParser {
    
    private final OpenAPI openAPI;
    private final TypeMapper typeMapper;

    public OpenAPIParser(String specPath) {
        this.openAPI = new OpenAPIV3Parser().read(specPath);
        if (this.openAPI == null) {
            throw new IllegalArgumentException("Failed to parse OpenAPI spec: " + specPath);
        }
        this.typeMapper = new TypeMapper(openAPI);
    }

    public OpenAPIParser(OpenAPI openAPI) {
        this.openAPI = openAPI;
        this.typeMapper = new TypeMapper(openAPI);
    }

    /**
     * Parse all operations from the OpenAPI spec
     */
    public List<OperationModel> parseOperations() {
        List<OperationModel> operations = new ArrayList<>();
        
        if (openAPI.getPaths() == null) {
            return operations;
        }

        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            String path = pathEntry.getKey();
            PathItem pathItem = pathEntry.getValue();

            operations.addAll(parsePathItem(path, pathItem));
        }

        return operations;
    }

    private List<OperationModel> parsePathItem(String path, PathItem pathItem) {
        List<OperationModel> operations = new ArrayList<>();

        if (pathItem.getGet() != null) {
            operations.add(parseOperation("GET", path, pathItem.getGet()));
        }
        if (pathItem.getPost() != null) {
            operations.add(parseOperation("POST", path, pathItem.getPost()));
        }
        if (pathItem.getPut() != null) {
            operations.add(parseOperation("PUT", path, pathItem.getPut()));
        }
        if (pathItem.getDelete() != null) {
            operations.add(parseOperation("DELETE", path, pathItem.getDelete()));
        }
        if (pathItem.getPatch() != null) {
            operations.add(parseOperation("PATCH", path, pathItem.getPatch()));
        }

        return operations;
    }

    private OperationModel parseOperation(String httpMethod, String path, Operation operation) {
        String operationId = operation.getOperationId();
        String summary = operation.getSummary();
        String description = operation.getDescription();
        
        List<ParameterModel> parameters = parseParameters(operation.getParameters());
        RequestBodyModel requestBody = parseRequestBody(operation.getRequestBody());
        ResponseModel response = parseResponse(operation.getResponses());
        List<String> tags = operation.getTags() != null ? operation.getTags() : Collections.emptyList();
        
        // Determine idempotency based on HTTP method
        boolean idempotent = isIdempotentMethod(httpMethod);
        
        // Determine retry metadata based on idempotency
        RetryMetadata retryMetadata = idempotent ? 
            RetryMetadata.safeIdempotentRetry() : 
            RetryMetadata.nonIdempotentRetry();

        return new OperationModel(
            operationId, httpMethod, path, summary, description,
            parameters, requestBody, response, tags, retryMetadata, idempotent
        );
    }

    private List<ParameterModel> parseParameters(List<Parameter> parameters) {
        if (parameters == null) {
            return Collections.emptyList();
        }

        return parameters.stream()
            .map(this::parseParameter)
            .collect(Collectors.toList());
    }

    private ParameterModel parseParameter(Parameter parameter) {
        String name = parameter.getName();
        String in = parameter.getIn();
        Schema<?> schema = parameter.getSchema();
        String type = schema != null ? schema.getType() : "string";
        String javaType = typeMapper.mapSchemaToJavaType(schema);
        boolean required = parameter.getRequired() != null ? parameter.getRequired() : false;
        String description = parameter.getDescription();
        String schemaRef = schema != null && schema.get$ref() != null ? schema.get$ref() : null;

        return new ParameterModel(name, in, type, javaType, required, description, schemaRef);
    }

    private RequestBodyModel parseRequestBody(RequestBody requestBody) {
        if (requestBody == null) {
            return null;
        }

        String description = requestBody.getDescription();
        boolean required = requestBody.getRequired() != null ? requestBody.getRequired() : false;
        Map<String, MediaTypeModel> contentTypes = parseContent(requestBody.getContent());

        return new RequestBodyModel(description, required, contentTypes);
    }

    private ResponseModel parseResponse(io.swagger.v3.oas.models.responses.ApiResponses responses) {
        if (responses == null || responses.isEmpty()) {
            return new ResponseModel("200", "Success", Collections.emptyMap());
        }

        // Find first successful response (2xx)
        ApiResponse successResponse = null;
        String statusCode = "200";
        
        for (String code : responses.keySet()) {
            if (code.startsWith("2")) {
                successResponse = responses.get(code);
                statusCode = code;
                break;
            }
        }

        if (successResponse == null) {
            successResponse = responses.values().iterator().next();
            statusCode = responses.keySet().iterator().next();
        }

        String description = successResponse.getDescription();
        Map<String, MediaTypeModel> contentTypes = parseContent(successResponse.getContent());

        return new ResponseModel(statusCode, description, contentTypes);
    }

    private Map<String, MediaTypeModel> parseContent(Content content) {
        if (content == null) {
            return Collections.emptyMap();
        }

        Map<String, MediaTypeModel> contentTypes = new HashMap<>();
        
        for (Map.Entry<String, io.swagger.v3.oas.models.media.MediaType> entry : content.entrySet()) {
            String contentType = entry.getKey();
            io.swagger.v3.oas.models.media.MediaType mediaType = entry.getValue();
            Schema<?> schema = mediaType.getSchema();
            
            String javaType = typeMapper.mapSchemaToJavaType(schema);
            String schemaRef = schema != null && schema.get$ref() != null ? schema.get$ref() : null;
            boolean isArray = schema instanceof ArraySchema;
            String itemType = null;
            
            if (isArray) {
                ArraySchema arraySchema = (ArraySchema) schema;
                itemType = typeMapper.mapSchemaToJavaType(arraySchema.getItems());
            }

            contentTypes.put(contentType, new MediaTypeModel(
                contentType, javaType, schemaRef, isArray, itemType
            ));
        }

        return contentTypes;
    }

    private boolean isIdempotentMethod(String httpMethod) {
        // GET, PUT, DELETE are idempotent; POST and PATCH are not
        return "GET".equalsIgnoreCase(httpMethod) || 
               "PUT".equalsIgnoreCase(httpMethod) || 
               "DELETE".equalsIgnoreCase(httpMethod);
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    public TypeMapper getTypeMapper() {
        return typeMapper;
    }
}
