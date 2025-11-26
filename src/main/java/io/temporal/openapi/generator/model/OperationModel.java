package io.temporal.openapi.generator.model;

import io.swagger.v3.oas.models.media.Schema;
import java.util.List;

/**
 * Represents a parsed OpenAPI operation to be generated as a Temporal Activity method
 */
public class OperationModel {
    private final String operationId;
    private final String httpMethod;
    private final String path;
    private final String summary;
    private final String description;
    private final List<ParameterModel> parameters;
    private final RequestBodyModel requestBody;
    private final ResponseModel response;
    private final List<String> tags;
    private final RetryMetadata retryMetadata;
    private final boolean idempotent;

    public OperationModel(String operationId, String httpMethod, String path, 
                         String summary, String description,
                         List<ParameterModel> parameters, RequestBodyModel requestBody,
                         ResponseModel response, List<String> tags,
                         RetryMetadata retryMetadata, boolean idempotent) {
        this.operationId = operationId;
        this.httpMethod = httpMethod;
        this.path = path;
        this.summary = summary;
        this.description = description;
        this.parameters = parameters;
        this.requestBody = requestBody;
        this.response = response;
        this.tags = tags;
        this.retryMetadata = retryMetadata;
        this.idempotent = idempotent;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public List<ParameterModel> getParameters() {
        return parameters;
    }

    public RequestBodyModel getRequestBody() {
        return requestBody;
    }

    public ResponseModel getResponse() {
        return response;
    }

    public List<String> getTags() {
        return tags;
    }

    public RetryMetadata getRetryMetadata() {
        return retryMetadata;
    }

    public boolean isIdempotent() {
        return idempotent;
    }

    public String getMethodName() {
        return operationId != null ? operationId : 
               (httpMethod.toLowerCase() + path.replaceAll("[^a-zA-Z0-9]", ""));
    }
}
