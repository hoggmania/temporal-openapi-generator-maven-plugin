package io.temporal.openapi.generator.model;

/**
 * Represents a parameter in an OpenAPI operation
 */
public class ParameterModel {
    private final String name;
    private final String in; // path, query, header, cookie
    private final String type;
    private final String javaType;
    private final boolean required;
    private final String description;
    private final String schemaRef;

    public ParameterModel(String name, String in, String type, String javaType, 
                         boolean required, String description, String schemaRef) {
        this.name = name;
        this.in = in;
        this.type = type;
        this.javaType = javaType;
        this.required = required;
        this.description = description;
        this.schemaRef = schemaRef;
    }

    public String getName() {
        return name;
    }

    public String getIn() {
        return in;
    }

    public String getType() {
        return type;
    }

    public String getJavaType() {
        return javaType;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }

    public String getSchemaRef() {
        return schemaRef;
    }

    public String getJavaFieldName() {
        return toCamelCase(name);
    }

    private String toCamelCase(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : str.toCharArray()) {
            if (c == '-' || c == '_' || c == ' ') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
