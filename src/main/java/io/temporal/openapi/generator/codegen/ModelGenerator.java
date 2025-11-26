package io.temporal.openapi.generator.codegen;

import com.squareup.javapoet.*;
import io.swagger.v3.oas.models.media.Schema;
import io.temporal.openapi.generator.parser.TypeMapper;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.util.*;

/**
 * Generates Java POJO model classes from OpenAPI schemas
 */
public class ModelGenerator {
    
    private final TypeMapper typeMapper;
    private final String packageName;

    public ModelGenerator(TypeMapper typeMapper, String packageName) {
        this.typeMapper = typeMapper;
        this.packageName = packageName;
    }

    /**
     * Generate model classes for all schemas
     */
    public List<JavaFile> generateModels() {
        List<JavaFile> javaFiles = new ArrayList<>();
        Map<String, Schema> schemas = typeMapper.getSchemasToGenerate();

        for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
            String modelName = entry.getKey();
            Schema schema = entry.getValue();
            
            JavaFile javaFile = generateModel(modelName, schema);
            if (javaFile != null) {
                javaFiles.add(javaFile);
            }
        }

        return javaFiles;
    }

    /**
     * Generate a single model class
     */
    public JavaFile generateModel(String modelName, Schema schema) {
        if (schema.getProperties() == null || schema.getProperties().isEmpty()) {
            // Skip empty schemas or enums (handle separately if needed)
            return null;
        }

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(modelName)
            .addModifiers(Modifier.PUBLIC);

        // Add class javadoc
        if (schema.getDescription() != null) {
            classBuilder.addJavadoc(schema.getDescription() + "\n");
        }

        // Add fields, constructor, getters, setters
        List<FieldSpec> fields = new ArrayList<>();
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC);

        List<String> requiredFields = schema.getRequired() != null ? 
            schema.getRequired() : Collections.emptyList();

        for (Map.Entry<String, Schema> propEntry : 
             ((Map<String, Schema>) schema.getProperties()).entrySet()) {
            
            String propName = propEntry.getKey();
            Schema propSchema = propEntry.getValue();
            boolean required = requiredFields.contains(propName);

            String javaFieldName = toCamelCase(propName);
            String javaType = typeMapper.mapToFieldType(propSchema, required);

            // Create field
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(
                getTypeName(javaType), 
                javaFieldName,
                Modifier.PRIVATE
            );

            if (propSchema.getDescription() != null) {
                fieldBuilder.addJavadoc(propSchema.getDescription() + "\n");
            }

            FieldSpec field = fieldBuilder.build();
            fields.add(field);
            classBuilder.addField(field);

            // Add to constructor
            constructorBuilder.addParameter(getTypeName(javaType), javaFieldName);
            constructorBuilder.addStatement("this.$L = $L", javaFieldName, javaFieldName);

            // Add getter
            MethodSpec getter = MethodSpec.methodBuilder("get" + StringUtils.capitalize(javaFieldName))
                .addModifiers(Modifier.PUBLIC)
                .returns(getTypeName(javaType))
                .addStatement("return $L", javaFieldName)
                .build();
            classBuilder.addMethod(getter);

            // Add setter
            MethodSpec setter = MethodSpec.methodBuilder("set" + StringUtils.capitalize(javaFieldName))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getTypeName(javaType), javaFieldName)
                .addStatement("this.$L = $L", javaFieldName, javaFieldName)
                .build();
            classBuilder.addMethod(setter);
        }

        // Add constructor
        classBuilder.addMethod(constructorBuilder.build());

        // Add no-arg constructor for Jackson
        MethodSpec noArgConstructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .build();
        classBuilder.addMethod(noArgConstructor);

        // Build the class
        TypeSpec modelClass = classBuilder.build();

        return JavaFile.builder(packageName, modelClass)
            .indent("    ")
            .build();
    }

    /**
     * Convert field name to camelCase
     */
    private String toCamelCase(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        boolean first = true;
        for (char c : str.toCharArray()) {
            if (c == '-' || c == '_' || c == ' ') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
                first = false;
            } else {
                result.append(first ? Character.toLowerCase(c) : c);
                first = false;
            }
        }
        return result.toString();
    }

    /**
     * Convert string type name to TypeName
     */
    private TypeName getTypeName(String javaType) {
        // Handle generic types
        if (javaType.contains("<")) {
            return parseGenericType(javaType);
        }

        // Handle primitive types
        switch (javaType) {
            case "String": return ClassName.get(String.class);
            case "Integer": return ClassName.get(Integer.class);
            case "Long": return ClassName.get(Long.class);
            case "Double": return ClassName.get(Double.class);
            case "Float": return ClassName.get(Float.class);
            case "Boolean": return ClassName.get(Boolean.class);
            case "byte[]": return ArrayTypeName.of(TypeName.BYTE);
            case "Object": return ClassName.get(Object.class);
            default:
                // Assume it's a fully qualified class name
                if (javaType.contains(".")) {
                    int lastDot = javaType.lastIndexOf('.');
                    String pkg = javaType.substring(0, lastDot);
                    String className = javaType.substring(lastDot + 1);
                    return ClassName.get(pkg, className);
                }
                return ClassName.get(packageName, javaType);
        }
    }

    /**
     * Parse generic types like List<String> or Map<String, Integer>
     */
    private TypeName parseGenericType(String javaType) {
        // Simple parser for List and Map types
        if (javaType.startsWith("java.util.List<")) {
            String innerType = javaType.substring(15, javaType.length() - 1);
            return ParameterizedTypeName.get(
                ClassName.get(List.class),
                getTypeName(innerType)
            );
        }
        if (javaType.startsWith("java.util.Map<")) {
            String inner = javaType.substring(14, javaType.length() - 1);
            String[] types = inner.split(",\\s*");
            return ParameterizedTypeName.get(
                ClassName.get(Map.class),
                getTypeName(types[0]),
                getTypeName(types[1])
            );
        }
        return ClassName.bestGuess(javaType);
    }
}
