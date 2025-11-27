# Temporal OpenAPI Generator Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hoggmania/temporal-openapi-generator-maven-plugin.svg)](https://search.maven.org/artifact/io.github.hoggmania/temporal-openapi-generator-maven-plugin)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

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
                <groupId>io.github.hoggmania</groupId>
                <artifactId>temporal-openapi-generator-maven-plugin</artifactId>
                <version>1.0.0</version>
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

The plugin generates a Temporal Activity interface with all API operations as methods. Here's an actual example from the Pet Store API:

```java
package com.example.petstore.temporal.activities;

import com.example.petstore.client.model.NewPet;
import com.example.petstore.client.model.Pet;
import com.example.petstore.client.model.Vaccination;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.util.List;

/**
 * Temporal Activity interface generated from OpenAPI specification.
 * This interface contains all API operations as Activity methods.
 */
@ActivityInterface
public interface PetStoreActivity {
    /**
     * List all pets
     *
     * Returns a list of all pets in the store
     *
     * @apiOperation GET /pets
     * @idempotent This operation is idempotent and can be safely retried
     * @param limit Maximum number of pets to return
     * @param tag Filter pets by tag
     * @return A list of pets
     */
    @ActivityMethod
    List<Pet> listPets(Integer limit, String tag);

    /**
     * Create a pet
     *
     * Add a new pet to the store
     *
     * @apiOperation POST /pets
     * @nonIdempotent This operation is NOT idempotent, use caution with retries
     * @param body Request body
     * @return Pet created successfully
     */
    @ActivityMethod
    Pet createPet(NewPet body);

    /**
     * Get a pet by ID
     *
     * Returns details of a specific pet
     *
     * @apiOperation GET /pets/{petId}
     * @idempotent This operation is idempotent and can be safely retried
     * @param petId ID of the pet to retrieve
     * @return Pet details
     */
    @ActivityMethod
    Pet getPet(Long petId);

    /**
     * Update a pet
     *
     * Update an existing pet's information
     *
     * @apiOperation PUT /pets/{petId}
     * @idempotent This operation is idempotent and can be safely retried
     * @param petId ID of the pet to update
     * @param body Request body
     * @return Pet updated successfully
     */
    @ActivityMethod
    Pet updatePet(Long petId, NewPet body);

    /**
     * Delete a pet
     *
     * Remove a pet from the store
     *
     * @apiOperation DELETE /pets/{petId}
     * @idempotent This operation is idempotent and can be safely retried
     * @param petId ID of the pet to delete
     */
    @ActivityMethod
    void deletePet(Long petId);

    /**
     * Get pet vaccination records
     *
     * Returns vaccination history for a specific pet
     *
     * @apiOperation GET /pets/{petId}/vaccinations
     * @idempotent This operation is idempotent and can be safely retried
     * @param petId 
     * @return Vaccination records
     */
    @ActivityMethod
    List<Vaccination> getPetVaccinations(Long petId);
}
```

**Key Features:**

- ✅ All 6 API operations converted to Activity methods
- ✅ Proper Java types (Integer, Long, String, List)
- ✅ Model classes from OpenAPI Generator (Pet, NewPet, Vaccination)
- ✅ Detailed Javadoc with operation info and idempotency hints
- ✅ Simple `@ActivityMethod` annotation (timeout configured at registration time)

### 4. Generated Implementation

The plugin also generates the implementation class that delegates to the OpenAPI Generator client:

```java
package com.example.petstore.temporal.activities;

import com.example.petstore.client.ApiClient;
import com.example.petstore.client.api.PetsApi;
import com.example.petstore.client.model.NewPet;
import com.example.petstore.client.model.Pet;
import com.example.petstore.client.model.Vaccination;
import io.temporal.failure.ApplicationFailure;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.util.List;

/**
 * Implementation of PetStoreActivity that delegates to OpenAPI Generator client.
 * This class is auto-generated from the OpenAPI specification.
 */
public class PetStoreActivityImpl implements PetStoreActivity {
    private final ApiClient apiClient;

    /**
     * Creates a new activity implementation with the provided API client.
     * @param apiClient The OpenAPI generator client
     */
    public PetStoreActivityImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<Pet> listPets(Integer limit, String tag) {
        PetsApi api = new PetsApi(apiClient);
        try {
            List<Pet> result = api.listPets(limit, tag);
            return result;
        } catch (Exception e) {
            // Log the error and throw a Temporal ApplicationFailure
            throw ApplicationFailure.newFailure("API call failed: " + e.getMessage(), "API_ERROR", e);
        }
    }

    @Override
    public Pet createPet(NewPet body) {
        PetsApi api = new PetsApi(apiClient);
        try {
            Pet result = api.createPet(body);
            return result;
        } catch (Exception e) {
            // Log the error and throw a Temporal ApplicationFailure
            throw ApplicationFailure.newFailure("API call failed: " + e.getMessage(), "API_ERROR", e);
        }
    }

    @Override
    public Pet getPet(Long petId) {
        PetsApi api = new PetsApi(apiClient);
        try {
            Pet result = api.getPet(petId);
            return result;
        } catch (Exception e) {
            // Log the error and throw a Temporal ApplicationFailure
            throw ApplicationFailure.newFailure("API call failed: " + e.getMessage(), "API_ERROR", e);
        }
    }

    @Override
    public Pet updatePet(Long petId, NewPet body) {
        PetsApi api = new PetsApi(apiClient);
        try {
            Pet result = api.updatePet(petId, body);
            return result;
        } catch (Exception e) {
            // Log the error and throw a Temporal ApplicationFailure
            throw ApplicationFailure.newFailure("API call failed: " + e.getMessage(), "API_ERROR", e);
        }
    }

    @Override
    public void deletePet(Long petId) {
        PetsApi api = new PetsApi(apiClient);
        try {
            api.deletePet(petId);
        } catch (Exception e) {
            // Log the error and throw a Temporal ApplicationFailure
            throw ApplicationFailure.newFailure("API call failed: " + e.getMessage(), "API_ERROR", e);
        }
    }

    @Override
    public List<Vaccination> getPetVaccinations(Long petId) {
        PetsApi api = new PetsApi(apiClient);
        try {
            List<Vaccination> result = api.getPetVaccinations(petId);
            return result;
        } catch (Exception e) {
            // Log the error and throw a Temporal ApplicationFailure
            throw ApplicationFailure.newFailure("API call failed: " + e.getMessage(), "API_ERROR", e);
        }
    }
}
```

**Key Features:**

- ✅ Delegates to OpenAPI Generator's `PetsApi` client
- ✅ Proper error handling with `ApplicationFailure`
- ✅ Type-safe method signatures matching the interface
- ✅ Constructor injection for `ApiClient` configuration

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
| array | - | java.util.List&lt;T&gt; |
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

## Publishing

For maintainers: See [PUBLISHING.md](PUBLISHING.md) for instructions on publishing to Maven Central.

## License

Apache License 2.0 - See [LICENSE](LICENSE) for details.

## Contributing

Contributions welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes with tests
4. Submit a pull request

For bugs and feature requests, please [open an issue](https://github.com/hoggmania/temporal-openapi-generator-maven-plugin/issues).

