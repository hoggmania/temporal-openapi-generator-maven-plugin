package io.temporal.openapi.generator.model;

import java.util.Map;

/**
 * Represents a response in an OpenAPI operation
 */
public class ResponseModel {
    private final String statusCode;
    private final String description;
    private final Map<String, MediaTypeModel> contentTypes;

    public ResponseModel(String statusCode, String description, 
                        Map<String, MediaTypeModel> contentTypes) {
        this.statusCode = statusCode;
        this.description = description;
        this.contentTypes = contentTypes;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, MediaTypeModel> getContentTypes() {
        return contentTypes;
    }

    public MediaTypeModel getPrimaryContentType() {
        if (contentTypes == null || contentTypes.isEmpty()) {
            return null;
        }
        // Prefer application/json, then first available
        if (contentTypes.containsKey("application/json")) {
            return contentTypes.get("application/json");
        }
        return contentTypes.values().stream().findFirst().orElse(null);
    }

    public String getJavaReturnType() {
        MediaTypeModel media = getPrimaryContentType();
        return media != null ? media.getJavaType() : "void";
    }
}
