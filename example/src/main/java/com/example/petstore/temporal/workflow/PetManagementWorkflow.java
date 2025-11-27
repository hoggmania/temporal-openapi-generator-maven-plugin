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
