# Quick Start Guide

## 5-Minute Setup

### Step 1: Install the Plugin

```bash
cd temporal-openapi-generator-maven-plugin
mvn clean install
```

### Step 2: Create Your Project

Create a new Maven project with this `pom.xml`:

```xml
<project>
    <groupId>com.mycompany</groupId>
    <artifactId>my-temporal-api</artifactId>
    <version>1.0.0</version>
    
    <dependencies>
        <dependency>
            <groupId>io.temporal</groupId>
            <artifactId>temporal-sdk</artifactId>
            <version>1.20.1</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <!-- Generate OpenAPI Client -->
            <plugin>
                <groupId>org.openapitools</groupId>
                <artifactId>openapi-generator-maven-plugin</artifactId>
                <version>7.0.1</version>
                <executions>
                    <execution>
                        <goals><goal>generate</goal></goals>
                        <configuration>
                            <inputSpec>${basedir}/openapi.yaml</inputSpec>
                            <generatorName>java</generatorName>
                            <apiPackage>com.mycompany.api.client</apiPackage>
                            <modelPackage>com.mycompany.api.models</modelPackage>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            
            <!-- Generate Temporal Activities -->
            <plugin>
                <groupId>io.github.hoggmania</groupId>
                <artifactId>temporal-openapi-generator-maven-plugin</artifactId>
                <version>1.0.0</version>
                <executions>
                    <execution>
                        <goals><goal>generate</goal></goals>
                        <configuration>
                            <specFile>${basedir}/openapi.yaml</specFile>
                            <packageName>com.mycompany.temporal.activities</packageName>
                            <activityName>MyApiActivity</activityName>
                            <apiClientPackage>com.mycompany.api.client</apiClientPackage>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 3: Add Your OpenAPI Spec

Save your OpenAPI spec as `openapi.yaml` in the project root.

### Step 4: Generate Code

```bash
mvn clean compile
```

### Step 5: Use in Your Workflow

```java
@WorkflowInterface
public interface MyWorkflow {
    @WorkflowMethod
    String processData();
}

class MyWorkflowImpl implements MyWorkflow {
    private final MyApiActivity activity = 
        Workflow.newActivityStub(MyApiActivity.class);

    public String processData() {
        return activity.getData();
    }
}
```

### Step 6: Start Worker

```java
public class Main {
    public static void main(String[] args) {
        WorkflowClient client = WorkflowClient.newInstance(...);
        Worker worker = factory.newWorker("my-queue");
        
        worker.registerWorkflowImplementationTypes(MyWorkflowImpl.class);
        
        ApiClient apiClient = new ApiClient();
        worker.registerActivitiesImplementations(
            new MyApiActivityImpl(apiClient)
        );
        
        factory.start();
    }
}
```

Done! 🎉

## What You Get

The plugin generates production-ready code from your OpenAPI spec. Here's what a generated Activity looks like:

### Generated Activity Interface

```java
@ActivityInterface
public interface PetStoreActivity {
    /**
     * List all pets
     *
     * @apiOperation GET /pets
     * @idempotent This operation is idempotent and can be safely retried
     */
    @ActivityMethod
    List<Pet> listPets(Integer limit, String tag);

    /**
     * Create a pet
     *
     * @apiOperation POST /pets
     * @nonIdempotent This operation is NOT idempotent, use caution with retries
     */
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

### Generated Implementation

```java
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
                "API_ERROR", 
                e
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
                "API_ERROR", 
                e
            );
        }
    }
    
    // ... other methods
}
```

**Features:**

✅ Type-safe Activity interface from your OpenAPI spec  
✅ Implementation that calls your generated API client  
✅ Request/response model POJOs  
✅ Automatic retry configuration  
✅ Idempotency hints in Javadoc  
✅ Proper error handling with ApplicationFailure  

## Next Steps

- See [README.md](README.md) for full documentation
- Check [example/](example/) for a complete working example
- Customize retry policies in your workflow
- Add authentication to your API client
