# Aquasec API Example

This example demonstrates using the Temporal OpenAPI Generator Maven Plugin with the Aquasec public API.

## Overview

This project:
1. Uses the pre-downloaded Aquasec OpenAPI specification
2. Generates a Java client using OpenAPI Generator
3. Generates Temporal Activity interfaces and implementations using the Temporal OpenAPI Generator Plugin

The OpenAPI specification was downloaded from:
```
https://3920520305-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FyZbW8vbNs5hz8x57emJJ%2Fuploads%2Flc6qdAlkwSzkwHOe3tR8%2Faqua-public-swagger.json?alt=media&token=7cf87950-9ca0-4be2-9d12-95498d22c3fa
```

## Prerequisites

- Java 17+
- Maven 3.6+
- The Temporal OpenAPI Generator Plugin installed locally

## Building

### Step 1: Install the Plugin

From the root of the repository:

```bash
cd ..
mvn clean install
```

### Step 2: Build This Example

```bash
cd example-aquasec
mvn clean compile
```

This will:
1. Generate the OpenAPI client code in `target/generated-sources/openapi-client/`
2. Generate Temporal Activities in `target/generated-sources/temporal-activities/`

## Generated Code

After building, you'll find:

- **OpenAPI Client**: `target/generated-sources/openapi-client/`
  - API classes in `com.example.aquasec.client.api`
  - Model classes in `com.example.aquasec.client.model`

- **Temporal Activities**: `target/generated-sources/temporal-activities/`
  - `AquasecActivity.java` - Activity interface
  - `AquasecActivityImpl.java` - Activity implementation

## Usage Example

```java
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import com.example.aquasec.client.ApiClient;
import com.example.aquasec.temporal.activities.AquasecActivity;
import com.example.aquasec.temporal.activities.AquasecActivityImpl;

public class AquasecWorker {
    public static void main(String[] args) {
        WorkflowClient client = WorkflowClient.newInstance(...);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker("aquasec-queue");
        
        // Configure API client
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("https://api.aquasec.com");
        // Add authentication if needed
        // apiClient.setApiKey("your-api-key");
        
        // Register activity implementation
        worker.registerActivitiesImplementations(
            new AquasecActivityImpl(apiClient)
        );
        
        factory.start();
    }
}
```

## OpenAPI Specification

The OpenAPI specification is included in `src/main/resources/aquasec-openapi.json`. It was downloaded from the Aquasec public API documentation.

## Customization

You can customize the generation by modifying the plugin configuration in `pom.xml`:

- `<activityName>`: Change the name of the generated Activity interface
- `<packageName>`: Change the package for generated activities
- `<generateImplementation>`: Set to `false` to only generate the interface
- `<generateModels>`: Set to `true` to generate model classes separately

## Troubleshooting

Make sure you've installed the plugin first:
```bash
cd ..
mvn clean install
cd example-aquasec
```

## More Information

- [Aquasec API Documentation](https://docs.aquasec.com/)
- [Temporal OpenAPI Generator Plugin](../README.md)
