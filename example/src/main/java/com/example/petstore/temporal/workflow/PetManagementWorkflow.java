package com.example.petstore.temporal.workflow;

import com.example.petstore.client.model.Pet;
import com.example.petstore.client.model.NewPet;
import com.example.petstore.temporal.activities.PetStoreActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.time.Duration;

/**
 * Example workflow that orchestrates pet store operations using Temporal Activities
 */
@WorkflowInterface
public interface PetManagementWorkflow {
    
    @WorkflowMethod
    Pet createAndVerifyPet(NewPet petData);
}

class PetManagementWorkflowImpl implements PetManagementWorkflow {
    
    // Configure activity options
    private final ActivityOptions activityOptions = ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(5))
        .setRetryOptions(
            io.temporal.common.RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(1))
                .setMaximumInterval(Duration.ofMinutes(1))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(3)
                .build()
        )
        .build();
    
    // Create activity stub
    private final PetStoreActivity activity = 
        Workflow.newActivityStub(PetStoreActivity.class, activityOptions);

    @Override
    public Pet createAndVerifyPet(NewPet petData) {
        // Step 1: Create the pet
        Workflow.getLogger(PetManagementWorkflow.class)
            .info("Creating new pet: {}", petData.getName());
        
        Pet createdPet = activity.createPet(petData);
        
        // Step 2: Retrieve it to verify creation
        Workflow.getLogger(PetManagementWorkflow.class)
            .info("Verifying pet creation with ID: {}", createdPet.getId());
        
        Pet verifiedPet = activity.getPet(createdPet.getId());
        
        // Step 3: Log success
        Workflow.getLogger(PetManagementWorkflow.class)
            .info("Successfully created and verified pet: {} (ID: {})", 
                verifiedPet.getName(), verifiedPet.getId());
        
        return verifiedPet;
    }
}
