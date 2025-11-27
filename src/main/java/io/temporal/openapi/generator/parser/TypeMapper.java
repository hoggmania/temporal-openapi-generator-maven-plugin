package io.temporal.openapi.generator.parser;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Maps OpenAPI types to Java types
 */
public class TypeMapper {
    
    private final OpenAPI openAPI;
    private final String modelsPackage;
    private final Map<String, String> primitiveTypeMap;

    public TypeMapper(OpenAPI openAPI) {
        this(openAPI, "io.temporal.openapi.models");
    }

    public TypeMapper(OpenAPI openAPI, String modelsPackage) {
        this.openAPI = openAPI;
        this.modelsPackage = modelsPackage;
        this.primitiveTypeMap = initializePrimitiveTypeMap();
    }

    private Map<String, String> initializePrimitiveTypeMap() {
        Map<String, String> map = new HashMap<>();
        map.put("string", "String");
        map.put("integer", "Integer");
        map.put("long", "Long");
        map.put("number", "Double");
        map.put("float", "Float");
        map.put("double", "Double");
        map.put("boolean", "Boolean");
        map.put("byte", "byte[]");
        map.put("binary", "byte[]");
        map.put("date", "java.time.LocalDate");
        map.put("date-time", "java.time.OffsetDateTime");
        map.put("password", "String");
        map.put("email", "String");
        map.put("uuid", "java.util.UUID");
        map.put("uri", "java.net.URI");
        map.put("url", "java.net.URL");
        return map;
    }

    /**
     * Map OpenAPI schema to Java type
     */
    public String mapSchemaToJavaType(Schema<?> schema) {
        if (schema == null) {
            return "Object";
        }

        // Handle $ref (references to components/schemas)
        if (schema.get$ref() != null) {
            return resolveRef(schema.get$ref());
        }

        // Handle arrays
        if (schema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) schema;
            String itemType = mapSchemaToJavaType(arraySchema.getItems());
            return "java.util.List<" + itemType + ">";
        }

        // Handle maps/dictionaries
        if (schema instanceof MapSchema || 
            (schema.getAdditionalProperties() instanceof Schema)) {
            Schema<?> valueSchema = (Schema<?>) schema.getAdditionalProperties();
            String valueType = mapSchemaToJavaType(valueSchema);
            return "java.util.Map<String, " + valueType + ">";
        }

        // Handle primitive types
        String type = schema.getType();
        String format = schema.getFormat();

        if (type == null) {
            return "Object";
        }

        // Check format-specific mappings first
        if (format != null) {
            if ("integer".equals(type)) {
                if ("int32".equals(format)) return "Integer";
                if ("int64".equals(format)) return "Long";
            }
            if ("number".equals(type)) {
                if ("float".equals(format)) return "Float";
                if ("double".equals(format)) return "Double";
            }
            if ("string".equals(type)) {
                if ("date".equals(format)) return "java.time.LocalDate";
                if ("date-time".equals(format)) return "java.time.OffsetDateTime";
                if ("byte".equals(format)) return "byte[]";
                if ("binary".equals(format)) return "byte[]";
                if ("uuid".equals(format)) return "java.util.UUID";
            }
        }

        // Fall back to type-only mapping
        return primitiveTypeMap.getOrDefault(type, StringUtils.capitalize(type));
    }

    /**
     * Resolve a $ref to a Java type name
     */
    private String resolveRef(String ref) {
        if (ref == null) {
            return "Object";
        }

        // Extract the model name from the reference
        // Example: #/components/schemas/Pet -> Pet
        String[] parts = ref.split("/");
        String modelName = parts[parts.length - 1];

        return modelsPackage + "." + modelName;
    }

    /**
     * Extract schema name from reference
     */
    public String extractSchemaName(String ref) {
        if (ref == null) {
            return null;
        }
        String[] parts = ref.split("/");
        return parts[parts.length - 1];
    }

    /**
     * Get all schemas that need to be generated as model classes
     */
    @SuppressWarnings("rawtypes")
    public Map<String, Schema> getSchemasToGenerate() {
        if (openAPI.getComponents() == null || 
            openAPI.getComponents().getSchemas() == null) {
            return Collections.emptyMap();
        }
        return openAPI.getComponents().getSchemas();
    }

    /**
     * Convert OpenAPI type to Java field type (boxed for nullable fields)
     */
    public String mapToFieldType(Schema<?> schema, boolean required) {
        String javaType = mapSchemaToJavaType(schema);
        
        // If not required, ensure we use boxed types for primitives
        if (!required) {
            if ("int".equals(javaType)) return "Integer";
            if ("long".equals(javaType)) return "Long";
            if ("float".equals(javaType)) return "Float";
            if ("double".equals(javaType)) return "Double";
            if ("boolean".equals(javaType)) return "Boolean";
        }
        
        return javaType;
    }

    public String getModelsPackage() {
        return modelsPackage;
    }
}
