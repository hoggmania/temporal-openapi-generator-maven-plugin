package io.temporal.openapi.generator.model;

import java.util.Map;

/**
 * Represents a request body in an OpenAPI operation
 */
public class RequestBodyModel {
    private final String description;
    private final boolean required;
    private final Map<String, MediaTypeModel> contentTypes;

    public RequestBodyModel(String description, boolean required, 
                           Map<String, MediaTypeModel> contentTypes) {
        this.description = description;
        this.required = required;
        this.contentTypes = contentTypes;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return required;
    }

    public Map<String, MediaTypeModel> getContentTypes() {
        return contentTypes;
    }

    public MediaTypeModel getPrimaryContentType() {
        // Prefer application/json, then first available
        if (contentTypes.containsKey("application/json")) {
            return contentTypes.get("application/json");
        }
        return contentTypes.values().stream().findFirst().orElse(null);
    }
}
