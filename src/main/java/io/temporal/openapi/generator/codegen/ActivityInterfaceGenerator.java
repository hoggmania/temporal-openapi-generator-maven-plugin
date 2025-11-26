package io.temporal.openapi.generator.codegen;

import com.squareup.javapoet.*;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.openapi.generator.model.OperationModel;
import io.temporal.openapi.generator.model.ParameterModel;
import io.temporal.openapi.generator.model.RequestBodyModel;
import io.temporal.openapi.generator.model.MediaTypeModel;
import io.temporal.openapi.generator.parser.TypeMapper;

import javax.lang.model.element.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates Temporal Activity interface from OpenAPI operations
 */
public class ActivityInterfaceGenerator {
    
    private final TypeMapper typeMapper;
    private final String packageName;
    private final String interfaceName;

    public ActivityInterfaceGenerator(TypeMapper typeMapper, String packageName, String interfaceName) {
        this.typeMapper = typeMapper;
        this.packageName = packageName;
        this.interfaceName = interfaceName;
    }

    /**
     * Generate the unified Activity interface with all operations
     */
    public JavaFile generateActivityInterface(List<OperationModel> operations) {
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(interfaceName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(ActivityInterface.class)
            .addJavadoc("Temporal Activity interface generated from OpenAPI specification.\n")
            .addJavadoc("This interface contains all API operations as Activity methods.\n");

        // Generate a method for each operation
        for (OperationModel operation : operations) {
            MethodSpec method = generateActivityMethod(operation);
            interfaceBuilder.addMethod(method);
        }

        TypeSpec activityInterface = interfaceBuilder.build();

        return JavaFile.builder(packageName, activityInterface)
            .indent("    ")
            .build();
    }

    /**
     * Generate a single Activity method for an operation
     */
    private MethodSpec generateActivityMethod(OperationModel operation) {
        String methodName = operation.getMethodName();
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        // Add javadoc
        if (operation.getSummary() != null) {
            methodBuilder.addJavadoc(operation.getSummary() + "\n\n");
        }
        if (operation.getDescription() != null) {
            methodBuilder.addJavadoc(operation.getDescription() + "\n\n");
        }
        
        // Add HTTP method and path info in javadoc
        methodBuilder.addJavadoc("@apiOperation $L $L\n", operation.getHttpMethod(), operation.getPath());
        
        // Add idempotency hint in javadoc
        if (operation.isIdempotent()) {
            methodBuilder.addJavadoc("@idempotent This operation is idempotent and can be safely retried\n");
        } else {
            methodBuilder.addJavadoc("@nonIdempotent This operation is NOT idempotent, use caution with retries\n");
        }

        // Add ActivityMethod annotation with retry options
        AnnotationSpec.Builder activityMethodBuilder = AnnotationSpec.builder(ActivityMethod.class);
        
        // Add schedule-to-close timeout
        activityMethodBuilder.addMember("scheduleToCloseTimeout", "$S", "PT5M");
        
        // Build annotation
        methodBuilder.addAnnotation(activityMethodBuilder.build());

        // Build parameter object if we have multiple parameters or a request body
        boolean useRequestObject = shouldUseRequestObject(operation);
        
        if (useRequestObject) {
            // Create a single request parameter object
            String requestClassName = capitalize(methodName) + "Request";
            TypeName requestType = ClassName.get(packageName + ".requests", requestClassName);
            methodBuilder.addParameter(requestType, "request");
            methodBuilder.addJavadoc("@param request The request parameters\n");
        } else {
            // Add individual parameters
            if (operation.getParameters() != null) {
                for (ParameterModel param : operation.getParameters()) {
                    TypeName paramType = getTypeName(param.getJavaType());
                    methodBuilder.addParameter(paramType, param.getJavaFieldName());
                    methodBuilder.addJavadoc("@param $L $L\n", 
                        param.getJavaFieldName(), 
                        param.getDescription() != null ? param.getDescription() : "");
                }
            }
            
            // Add request body parameter if present
            if (operation.getRequestBody() != null) {
                MediaTypeModel mediaType = operation.getRequestBody().getPrimaryContentType();
                if (mediaType != null) {
                    TypeName bodyType = getTypeName(mediaType.getJavaType());
                    methodBuilder.addParameter(bodyType, "body");
                    methodBuilder.addJavadoc("@param body Request body\n");
                }
            }
        }

        // Set return type
        String returnType = operation.getResponse().getJavaReturnType();
        methodBuilder.returns(getTypeName(returnType));
        
        if (!"void".equals(returnType)) {
            methodBuilder.addJavadoc("@return $L\n", 
                operation.getResponse().getDescription() != null ? 
                operation.getResponse().getDescription() : "The response");
        }

        return methodBuilder.build();
    }

    /**
     * Determine if we should use a request object instead of individual parameters
     */
    private boolean shouldUseRequestObject(OperationModel operation) {
        int paramCount = operation.getParameters() != null ? operation.getParameters().size() : 0;
        boolean hasRequestBody = operation.getRequestBody() != null;
        
        // Use request object if we have more than 3 parameters, or parameters + request body
        return paramCount > 3 || (paramCount > 0 && hasRequestBody);
    }

    /**
     * Convert string type name to TypeName
     */
    private TypeName getTypeName(String javaType) {
        if (javaType == null || "void".equals(javaType)) {
            return TypeName.VOID;
        }

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
                ClassName.get(java.util.Map.class),
                getTypeName(types[0]),
                getTypeName(types[1])
            );
        }
        return ClassName.bestGuess(javaType);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
