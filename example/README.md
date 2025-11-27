# Example Pet Store Project

This example demonstrates how to use the Temporal OpenAPI Generator Maven Plugin to generate Temporal Activities from an OpenAPI specification.

## Project Structure

```
example/
├── pom.xml                                    # Maven configuration
├── src/main/resources/
│   └── petstore-openapi.yaml                 # OpenAPI specification
└── src/main/java/
    └── com/example/petstore/temporal/
        ├── TemporalWorker.java               # Worker startup
        ├── WorkflowStarter.java              # Workflow client
        └── workflow/
            └── PetManagementWorkflow.java    # Example workflow
```

## Generated Code

When you run `mvn clean compile`, the plugin generates:

### OpenAPI Client (by OpenAPI Generator)
- `target/generated-sources/openapi-client/`
  - API client classes (`PetsApi`, etc.)
  - Model POJOs (`Pet`, `NewPet`, `Owner`, etc.)
  - `ApiClient` configuration

### Temporal Activities (by Temporal OpenAPI Generator)
- `target/generated-sources/temporal-activities/`
  - `PetStoreActivity.java` (interface)
  - `PetStoreActivityImpl.java` (implementation)

## Building

```bash
cd example
mvn clean compile
```

## Running

1. Start Temporal server (using Docker):
```bash
docker run -p 7233:7233 temporalio/auto-setup:latest
```

2. Start the worker:
```bash
mvn exec:java -Dexec.mainClass="com.example.petstore.temporal.TemporalWorker"
```

3. In another terminal, start a workflow:
```bash
mvn exec:java -Dexec.mainClass="com.example.petstore.temporal.WorkflowStarter"
```

## What Gets Generated

The plugin analyzes `petstore-openapi.yaml` (6 operations) and generates complete, production-ready code:

### Activity Interface

**Full Generated Code:**

```java
package com.example.petstore.temporal.activities;

import com.example.petstore.client.model.NewPet;
import com.example.petstore.client.model.Pet;
import com.example.petstore.client.model.Vaccination;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import java.util.List;

@ActivityInterface
public interface PetStoreActivity {
    /**
     * List all pets
     * @apiOperation GET /pets
     * @idempotent This operation is idempotent and can be safely retried
     */
    @ActivityMethod
    List<Pet> listPets(Integer limit, String tag);
    
    /**
     * Create a pet
     * @apiOperation POST /pets
     * @nonIdempotent This operation is NOT idempotent, use caution with retries
     */
    @ActivityMethod
    Pet createPet(NewPet body);
    
    /**
     * Get a pet by ID
     * @apiOperation GET /pets/{petId}
     * @idempotent This operation is idempotent and can be safely retried
     */
    @ActivityMethod
    Pet getPet(Long petId);
    
    /**
     * Update a pet
     * @apiOperation PUT /pets/{petId}
     * @idempotent This operation is idempotent and can be safely retried
     */
    @ActivityMethod
    Pet updatePet(Long petId, NewPet body);
    
    /**
     * Delete a pet
     * @apiOperation DELETE /pets/{petId}
     * @idempotent This operation is idempotent and can be safely retried
     */
    @ActivityMethod
    void deletePet(Long petId);
    
    /**
     * Get pet vaccination records
     * @apiOperation GET /pets/{petId}/vaccinations
     * @idempotent This operation is idempotent and can be safely retried
     */
    @ActivityMethod
    List<Vaccination> getPetVaccinations(Long petId);
}
```

### Activity Implementation

**Full Generated Code:**

```java
package com.example.petstore.temporal.activities;

import com.example.petstore.client.ApiClient;
import com.example.petstore.client.api.PetsApi;
import com.example.petstore.client.model.NewPet;
import com.example.petstore.client.model.Pet;
import com.example.petstore.client.model.Vaccination;
import io.temporal.failure.ApplicationFailure;
import java.util.List;

public class PetStoreActivityImpl implements PetStoreActivity {
    private final ApiClient apiClient;
    
    public PetStoreActivityImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    @Override
    public List<Pet> listPets(Integer limit, String tag) {
        PetsApi api = new PetsApi(apiClient);
        try {
            return api.listPets(limit, tag);
        } catch (Exception e) {
            throw ApplicationFailure.newFailure(
                "API call failed: " + e.getMessage(), 
                "API_ERROR", e
            );
        }
    }
    
    @Override
    public Pet createPet(NewPet body) {
        PetsApi api = new PetsApi(apiClient);
        try {
            return api.createPet(body);
        } catch (Exception e) {
            throw ApplicationFailure.newFailure(
                "API call failed: " + e.getMessage(), 
                "API_ERROR", e
            );
        }
    }
    
    @Override
    public Pet getPet(Long petId) {
        PetsApi api = new PetsApi(apiClient);
        try {
            return api.getPet(petId);
        } catch (Exception e) {
            throw ApplicationFailure.newFailure(
                "API call failed: " + e.getMessage(), 
                "API_ERROR", e
            );
        }
    }
    
    @Override
    public Pet updatePet(Long petId, NewPet body) {
        PetsApi api = new PetsApi(apiClient);
        try {
            return api.updatePet(petId, body);
        } catch (Exception e) {
            throw ApplicationFailure.newFailure(
                "API call failed: " + e.getMessage(), 
                "API_ERROR", e
            );
        }
    }
    
    @Override
    public void deletePet(Long petId) {
        PetsApi api = new PetsApi(apiClient);
        try {
            api.deletePet(petId);
        } catch (Exception e) {
            throw ApplicationFailure.newFailure(
                "API call failed: " + e.getMessage(), 
                "API_ERROR", e
            );
        }
    }
    
    @Override
    public List<Vaccination> getPetVaccinations(Long petId) {
        PetsApi api = new PetsApi(apiClient);
        try {
            return api.getPetVaccinations(petId);
        } catch (Exception e) {
            throw ApplicationFailure.newFailure(
                "API call failed: " + e.getMessage(), 
                "API_ERROR", e
            );
        }
    }
}
```

**Key Features:**

- ✅ All 6 API operations converted to Activity methods
- ✅ Type-safe signatures with proper Java types
- ✅ Idempotency hints in Javadoc for retry configuration
- ✅ Delegates to OpenAPI Generator's `PetsApi` client
- ✅ Proper error handling with `ApplicationFailure`
