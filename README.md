# Temporal OpenAPI Generator Maven Plugin

A production-quality Maven plugin that generates Temporal Activities from OpenAPI specifications. This plugin automatically creates:

- **Temporal Activity Interface**: A single unified interface with all API operations as Activity methods
- **Activity Implementation**: Implementation class that delegates to OpenAPI Generator client
- **Model POJOs**: Java classes for request/response schemas
- **Type Mapping**: Automatic mapping from OpenAPI types to Java types
- **Retry Configuration**: Smart retry policies based on HTTP method idempotency
- **Multiple Content-Type Support**: Handles various content types (JSON, XML, etc.)

## Features

✅ **Automatic Code Generation**: Generate complete Temporal Activities from OpenAPI specs  
✅ **Type-Safe**: Proper Java types inferred from OpenAPI schemas  
✅ **Idempotency Hints**: Automatically identifies idempotent operations (GET, PUT, DELETE)  
✅ **Retry Metadata**: Configures retry policies based on operation characteristics  
✅ **OpenAPI Generator Integration**: Seamlessly integrates with generated API clients  
✅ **Request/Response Models**: Generates POJOs for complex request/response types  
✅ **Activity Namespacing**: Organizes operations by OpenAPI tags  
✅ **Production-Ready**: Error handling, logging, and proper Maven lifecycle integration

## Installation

### 1. Install the Plugin

First, build and install the plugin locally:

```bash
cd temporal-openapi-generator-maven-plugin
mvn clean install
```

### 2. Add to Your Project

Add the plugin to your project's `pom.xml`:

```xml
<project>
    <dependencies>
        <!-- Temporal SDK -->
        <dependency>
            <groupId>io.temporal</groupId>
            <artifactId>temporal-sdk</artifactId>
            <version>1.20.1</version>
        </dependency>
        
        <!-- OpenAPI Generator Generated Client -->
        <dependency>
            <groupId>your.group.id</groupId>
            <artifactId>your-openapi-client</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- OpenAPI Generator (generates API client) -->
            <plugin>
                <groupId>org.openapitools</groupId>
                <artifactId>openapi-generator-maven-plugin</artifactId>
                <version>7.0.1</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                        <configuration>
                            <inputSpec>${project.basedir}/src/main/resources/openapi.yaml</inputSpec>
                            <generatorName>java</generatorName>
                            <library>native</library>
                            <apiPackage>com.example.api.client</apiPackage>
                            <modelPackage>com.example.api.models</modelPackage>
                            <configOptions>
                                <dateLibrary>java8</dateLibrary>
                            </configOptions>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- Temporal OpenAPI Generator (generates Temporal Activities) -->
            <plugin>
                <groupId>io.temporal.openapi</groupId>
                <artifactId>temporal-openapi-generator-maven-plugin</artifactId>
                <version>1.0.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                        <configuration>
                            <specFile>${project.basedir}/src/main/resources/openapi.yaml</specFile>
                            <outputDirectory>${project.build.directory}/generated-sources/temporal</outputDirectory>
                            <packageName>com.example.temporal.activities</packageName>
                            <activityName>ApiActivity</activityName>
                            <apiClientPackage>com.example.api.client</apiClientPackage>
                            <generateImplementation>true</generateImplementation>
                            <generateModels>true</generateModels>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

## Configuration Options

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `specFile` | Yes | - | Path to OpenAPI specification file (YAML or JSON) |
| `outputDirectory` | No | `${project.build.directory}/generated-sources/openapi` | Output directory for generated sources |
| `packageName` | No | `io.temporal.openapi.generated` | Base package for generated classes |
| `activityName` | No | `ApiActivity` | Name of the generated Activity interface |
| `apiClientPackage` | No | `io.temporal.openapi.generated.client` | Package of OpenAPI Generator client |
| `generateImplementation` | No | `true` | Whether to generate implementation class |
| `generateModels` | No | `true` | Whether to generate model POJOs |

## Usage

### 1. Create Your OpenAPI Specification

```yaml
# openapi.yaml
openapi: 3.0.0
info:
  title: Pet Store API
  version: 1.0.0

paths:
  /pets:
    get:
      operationId: listPets
      summary: List all pets
      tags:
        - pets
      responses:
        '200':
          description: A list of pets
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Pet'
    post:
      operationId: createPet
      summary: Create a pet
      tags:
        - pets
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Pet'
      responses:
        '201':
          description: Pet created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Pet'

  /pets/{petId}:
    get:
      operationId: getPet
      summary: Get a pet by ID
      tags:
        - pets
      parameters:
        - name: petId
          in: path
          required: true
          schema:
            type: integer
            format: int64
      responses:
        '200':
          description: Pet details
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Pet'

components:
  schemas:
    Pet:
      type: object
      required:
        - id
        - name
      properties:
        id:
          type: integer
          format: int64
        name:
          type: string
        tag:
          type: string
```

### 2. Run Maven Build

```bash
mvn clean compile
```

This will:
1. Parse your OpenAPI spec
2. Generate model POJOs
3. Generate Temporal Activity interface
4. Generate Activity implementation

### 3. Generated Activity Interface

```java
package com.example.temporal.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import java.util.List;
import com.example.temporal.activities.models.Pet;

/**
 * Temporal Activity interface generated from OpenAPI specification.
 * This interface contains all API operations as Activity methods.
 */
@ActivityInterface
public interface ApiActivity {

    /**
     * List all pets
     *
     * @apiOperation GET /pets
     * @idempotent This operation is idempotent and can be safely retried
     * @return A list of pets
     */
    @ActivityMethod(scheduleToCloseTimeout = "PT5M")
    List<Pet> listPets();

    /**
     * Create a pet
     *
     * @apiOperation POST /pets
     * @nonIdempotent This operation is NOT idempotent, use caution with retries
     * @param body Request body
     * @return Pet created
     */
    @ActivityMethod(scheduleToCloseTimeout = "PT5M")
    Pet createPet(Pet body);

    /**
     * Get a pet by ID
     *
     * @apiOperation GET /pets/{petId}
     * @idempotent This operation is idempotent and can be safely retried
     * @param petId 
     * @return Pet details
     */
    @ActivityMethod(scheduleToCloseTimeout = "PT5M")
    Pet getPet(Long petId);
}
```

### 4. Generated Implementation

```java
package com.example.temporal.activities;

import com.example.api.client.ApiClient;
import com.example.api.client.PetsApi;
import io.temporal.failure.ApplicationFailure;
import java.util.List;

/**
 * Implementation of ApiActivity that delegates to OpenAPI Generator client.
 * This class is auto-generated from the OpenAPI specification.
 */
public class ApiActivityImpl implements ApiActivity {
    
    private final ApiClient apiClient;

    /**
     * Creates a new activity implementation with the provided API client.
     * @param apiClient The OpenAPI generator client
     */
    public ApiActivityImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<Pet> listPets() {
        try {
            PetsApi api = new PetsApi(apiClient);
            List<Pet> result = api.listPets();
            return result;
        } catch (Exception e) {
            // Log the error and throw a Temporal ApplicationFailure
            throw ApplicationFailure.newFailure("API call failed: " + e.getMessage(), "API_ERROR", e);
        }
    }

    @Override
    public Pet createPet(Pet body) {
        try {
            PetsApi api = new PetsApi(apiClient);
            Pet result = api.createPet(body);
            return result;
        } catch (Exception e) {
            throw ApplicationFailure.newFailure("API call failed: " + e.getMessage(), "API_ERROR", e);
        }
    }

    @Override
    public Pet getPet(Long petId) {
        try {
            PetsApi api = new PetsApi(apiClient);
            Pet result = api.getPet(petId);
            return result;
        } catch (Exception e) {
            throw ApplicationFailure.newFailure("API call failed: " + e.getMessage(), "API_ERROR", e);
        }
    }
}
```

### 5. Use in Your Workflow

```java
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface PetWorkflow {
    @WorkflowMethod
    Pet processNewPet(Pet petData);
}

public class PetWorkflowImpl implements PetWorkflow {
    
    private final ApiActivity apiActivity = 
        Workflow.newActivityStub(ApiActivity.class);

    @Override
    public Pet processNewPet(Pet petData) {
        // Create the pet via API
        Pet created = apiActivity.createPet(petData);
        
        // Retrieve it to confirm
        Pet retrieved = apiActivity.getPet(created.getId());
        
        return retrieved;
    }
}
```

### 6. Register and Run

```java
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import com.example.api.client.ApiClient;

public class TemporalWorker {
    public static void main(String[] args) {
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker("pet-task-queue");
        
        // Register workflow
        worker.registerWorkflowImplementationTypes(PetWorkflowImpl.class);
        
        // Register activity with OpenAPI client
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("https://api.petstore.example.com");
        worker.registerActivitiesImplementations(new ApiActivityImpl(apiClient));

        factory.start();
    }
}
```

## Advanced Features

### Retry Configuration

The plugin automatically configures retry policies based on HTTP method idempotency:

- **Idempotent** (GET, PUT, DELETE): More aggressive retries
  - Initial interval: 1s
  - Max interval: 300s
  - Backoff: 2.0x
  - Max attempts: 5

- **Non-idempotent** (POST, PATCH): Conservative retries
  - Initial interval: 2s
  - Max interval: 60s
  - Backoff: 1.5x
  - Max attempts: 2

### Request Objects

For operations with many parameters (>3) or parameters + request body, the plugin generates request objects:

```java
public class CreatePetRequest {
    private String name;
    private String category;
    private List<String> tags;
    private Pet body;
    
    // Constructor, getters, setters...
}

@ActivityMethod
Pet createPet(CreatePetRequest request);
```

### Type Mapping

The plugin maps OpenAPI types to Java types:

| OpenAPI Type | Format | Java Type |
|--------------|--------|-----------|
| string | - | String |
| string | date | java.time.LocalDate |
| string | date-time | java.time.OffsetDateTime |
| string | uuid | java.util.UUID |
| integer | int32 | Integer |
| integer | int64 | Long |
| number | float | Float |
| number | double | Double |
| boolean | - | Boolean |
| array | - | java.util.List<T> |
| object | - | Generated POJO |

## Troubleshooting

### Plugin Not Found

Make sure you've installed the plugin:
```bash
cd temporal-openapi-generator-maven-plugin
mvn clean install
```

### OpenAPI Spec Not Parsing

Validate your OpenAPI spec:
```bash
npx @apidevtools/swagger-cli validate openapi.yaml
```

### Generated Code Compilation Errors

Ensure your `apiClientPackage` matches the OpenAPI Generator configuration:
```xml
<apiPackage>com.example.api.client</apiPackage>
```

## License

Apache License 2.0

## Contributing

Contributions welcome! Please open an issue or pull request.

