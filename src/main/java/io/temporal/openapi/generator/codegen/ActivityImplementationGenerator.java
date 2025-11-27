package io.temporal.openapi.generator.codegen;

import com.squareup.javapoet.*;
import io.temporal.openapi.generator.model.OperationModel;
import io.temporal.openapi.generator.model.ParameterModel;
import io.temporal.openapi.generator.model.MediaTypeModel;

import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * Generates the implementation class for Temporal Activities that delegates to OpenAPI Generator client
 */
public class ActivityImplementationGenerator {
    
    private final String packageName;
    private final String interfaceName;
    private final String implClassName;
    private final String apiClientPackage;

    public ActivityImplementationGenerator(String packageName, 
                                          String interfaceName, String apiClientPackage) {
        this.packageName = packageName;
        this.interfaceName = interfaceName;
        this.implClassName = interfaceName + "Impl";
        this.apiClientPackage = apiClientPackage;
    }

    /**
     * Generate the implementation class
     */
    public JavaFile generateImplementation(List<OperationModel> operations) {
        ClassName interfaceClass = ClassName.get(packageName, interfaceName);
        
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(implClassName)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(interfaceClass)
            .addJavadoc("Implementation of $L that delegates to OpenAPI Generator client.\n", interfaceName)
            .addJavadoc("This class is auto-generated from the OpenAPI specification.\n");

        // Add API client field for each unique tag/API
        // For simplicity, we'll create a generic ApiClient field
        ClassName apiClientClass = ClassName.get(apiClientPackage, "ApiClient");
        FieldSpec apiClientField = FieldSpec.builder(apiClientClass, "apiClient", Modifier.PRIVATE, Modifier.FINAL)
            .build();
        classBuilder.addField(apiClientField);

        // Add constructor
        MethodSpec constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(apiClientClass, "apiClient")
            .addStatement("this.$N = $N", "apiClient", "apiClient")
            .addJavadoc("Creates a new activity implementation with the provided API client.\n")
            .addJavadoc("@param apiClient The OpenAPI generator client\n")
            .build();
        classBuilder.addMethod(constructor);

        // Generate implementation methods
        for (OperationModel operation : operations) {
            MethodSpec method = generateImplementationMethod(operation);
            classBuilder.addMethod(method);
        }

        TypeSpec implClass = classBuilder.build();

        return JavaFile.builder(packageName, implClass)
            .indent("    ")
            .build();
    }

    /**
     * Generate implementation method that calls the OpenAPI client
     */
    private MethodSpec generateImplementationMethod(OperationModel operation) {
        String methodName = operation.getMethodName();
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class);

        // Determine if using request object
        boolean useRequestObject = shouldUseRequestObject(operation);
        
        // Add parameters
        if (useRequestObject) {
            String requestClassName = capitalize(methodName) + "Request";
            TypeName requestType = ClassName.get(packageName + ".requests", requestClassName);
            methodBuilder.addParameter(requestType, "request");
        } else {
            if (operation.getParameters() != null) {
                for (ParameterModel param : operation.getParameters()) {
                    TypeName paramType = getTypeName(param.getJavaType());
                    methodBuilder.addParameter(paramType, param.getJavaFieldName());
                }
            }
            
            if (operation.getRequestBody() != null) {
                MediaTypeModel mediaType = operation.getRequestBody().getPrimaryContentType();
                if (mediaType != null) {
                    TypeName bodyType = getTypeName(mediaType.getJavaType());
                    methodBuilder.addParameter(bodyType, "body");
                }
            }
        }

        // Set return type
        String returnType = operation.getResponse().getJavaReturnType();
        methodBuilder.returns(getTypeName(returnType));

        // Generate method body
        generateMethodBody(methodBuilder, operation, useRequestObject);

        return methodBuilder.build();
    }

    /**
     * Generate the method body that calls the OpenAPI client
     */
    private void generateMethodBody(MethodSpec.Builder methodBuilder, OperationModel operation, 
                                    boolean useRequestObject) {
        // Determine which API class to use based on tags
        String apiClassName = getApiClassName(operation);
        ClassName apiClass = ClassName.get(apiClientPackage, apiClassName);

        // Create API instance
        methodBuilder.addStatement("$T api = new $T(apiClient)", apiClass, apiClass);

        // Build the API method call
        StringBuilder callBuilder = new StringBuilder();
        String returnType = operation.getResponse().getJavaReturnType();
        
        if (!"void".equals(returnType)) {
            callBuilder.append("$T result = ");
        }
        
        callBuilder.append("api.$L(");
        
        // Add parameters to the call
        List<String> callParams = new java.util.ArrayList<>();
        
        if (useRequestObject) {
            // Extract parameters from request object
            if (operation.getParameters() != null) {
                for (ParameterModel param : operation.getParameters()) {
                    callParams.add("request.get" + capitalize(param.getJavaFieldName()) + "()");
                }
            }
            if (operation.getRequestBody() != null) {
                callParams.add("request.getBody()");
            }
        } else {
            // Use direct parameters
            if (operation.getParameters() != null) {
                for (ParameterModel param : operation.getParameters()) {
                    callParams.add(param.getJavaFieldName());
                }
            }
            if (operation.getRequestBody() != null) {
                callParams.add("body");
            }
        }
        
        callBuilder.append(String.join(", ", callParams));
        callBuilder.append(")");

        // Add try-catch for error handling
        methodBuilder.beginControlFlow("try");
        
        if (!"void".equals(returnType)) {
            methodBuilder.addStatement(callBuilder.toString(), 
                getTypeName(returnType), 
                operation.getMethodName());
            methodBuilder.addStatement("return result");
        } else {
            methodBuilder.addStatement(callBuilder.toString().replace("$T result = ", ""), 
                operation.getMethodName());
        }
        
        methodBuilder.nextControlFlow("catch ($T e)", ClassName.get(Exception.class));
        methodBuilder.addComment("Log the error and throw a Temporal ApplicationFailure");
        methodBuilder.addStatement("throw $T.newFailure($S + e.getMessage(), $S, e)",
            ClassName.get("io.temporal.failure", "ApplicationFailure"),
            "API call failed: ",
            "API_ERROR");
        methodBuilder.endControlFlow();
    }

    /**
     * Get the API class name based on operation tags
     */
    private String getApiClassName(OperationModel operation) {
        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            String tag = operation.getTags().get(0);
            return capitalize(tag) + "Api";
        }
        return "DefaultApi";
    }

    private boolean shouldUseRequestObject(OperationModel operation) {
        int paramCount = operation.getParameters() != null ? operation.getParameters().size() : 0;
        boolean hasRequestBody = operation.getRequestBody() != null;
        return paramCount > 3 || (paramCount > 0 && hasRequestBody);
    }

    private TypeName getTypeName(String javaType) {
        if (javaType == null || "void".equals(javaType)) {
            return TypeName.VOID;
        }

        if (javaType.contains("<")) {
            return parseGenericType(javaType);
        }

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
                if (javaType.contains(".")) {
                    int lastDot = javaType.lastIndexOf('.');
                    String pkg = javaType.substring(0, lastDot);
                    String className = javaType.substring(lastDot + 1);
                    return ClassName.get(pkg, className);
                }
                return ClassName.get(packageName, javaType);
        }
    }

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
