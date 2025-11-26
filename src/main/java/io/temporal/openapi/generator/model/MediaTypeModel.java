package io.temporal.openapi.generator.model;

/**
 * Represents a media type with its schema
 */
public class MediaTypeModel {
    private final String contentType;
    private final String javaType;
    private final String schemaRef;
    private final boolean isArray;
    private final String itemType;

    public MediaTypeModel(String contentType, String javaType, String schemaRef, 
                         boolean isArray, String itemType) {
        this.contentType = contentType;
        this.javaType = javaType;
        this.schemaRef = schemaRef;
        this.isArray = isArray;
        this.itemType = itemType;
    }

    public String getContentType() {
        return contentType;
    }

    public String getJavaType() {
        return javaType;
    }

    public String getSchemaRef() {
        return schemaRef;
    }

    public boolean isArray() {
        return isArray;
    }

    public String getItemType() {
        return itemType;
    }
}
