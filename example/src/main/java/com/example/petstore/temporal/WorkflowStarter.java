package com.example.petstore.temporal;

import com.example.petstore.client.model.NewPet;
import com.example.petstore.client.model.Owner;
import com.example.petstore.client.model.Pet;
import com.example.petstore.temporal.workflow.PetManagementWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

/**
 * Example client to start a workflow execution
 */
public class WorkflowStarter {
    
    private static final String TASK_QUEUE = "pet-store-task-queue";

    public static void main(String[] args) {
        // Create connection to Temporal service
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);

        // Create workflow options
        WorkflowOptions options = WorkflowOptions.newBuilder()
            .setTaskQueue(TASK_QUEUE)
            .setWorkflowId("pet-creation-" + System.currentTimeMillis())
            .build();

        // Create workflow stub
        PetManagementWorkflow workflow = client.newWorkflowStub(
            PetManagementWorkflow.class, 
            options
        );

        // Create test pet data
        Owner owner = new Owner();
        owner.setName("John Doe");
        owner.setEmail("john.doe@example.com");
        owner.setPhone("555-0123");

        NewPet newPet = new NewPet();
        newPet.setName("Buddy");
        newPet.setSpecies("dog");
        newPet.setBreed("Golden Retriever");
        newPet.setAge(3);
        newPet.setColor("Golden");
        newPet.setOwner(owner);

        System.out.println("Starting workflow to create pet: " + newPet.getName());

        // Execute workflow
        try {
            Pet result = workflow.createAndVerifyPet(newPet);
            System.out.println("Workflow completed successfully!");
            System.out.println("Created pet: " + result.getName() + " (ID: " + result.getId() + ")");
        } catch (Exception e) {
            System.err.println("Workflow failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
