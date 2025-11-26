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

The plugin analyzes `petstore-openapi.yaml` and generates:

### Activity Interface
```java
@ActivityInterface
public interface PetStoreActivity {
    @ActivityMethod
    List<Pet> listPets(Integer limit, String tag);
    
    @ActivityMethod
    Pet createPet(NewPet body);
    
    @ActivityMethod
    Pet getPet(Long petId);
    
    @ActivityMethod
    Pet updatePet(Long petId, NewPet body);
    
    @ActivityMethod
    void deletePet(Long petId);
}
```

### Activity Implementation
```java
public class PetStoreActivityImpl implements PetStoreActivity {
    private final ApiClient apiClient;
    
    @Override
    public List<Pet> listPets(Integer limit, String tag) {
        try {
            PetsApi api = new PetsApi(apiClient);
            return api.listPets(limit, tag);
        } catch (Exception e) {
            throw ApplicationFailure.newFailure(
                "API call failed: " + e.getMessage(), 
                "API_ERROR", e
            );
        }
    }
    // ... other methods
}
```
