package com.example.petstore.temporal;

import com.example.petstore.client.ApiClient;
import com.example.petstore.temporal.activities.PetStoreActivityImpl;
import com.example.petstore.temporal.workflow.PetManagementWorkflow;
import com.example.petstore.temporal.workflow.PetManagementWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

/**
 * Main class to start the Temporal worker with PetStore activities
 */
public class TemporalWorker {
    
    private static final String TASK_QUEUE = "pet-store-task-queue";

    public static void main(String[] args) {
        // Create connection to Temporal service
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);

        // Create worker
        Worker worker = factory.newWorker(TASK_QUEUE);

        // Register workflow implementation
        worker.registerWorkflowImplementationTypes(PetManagementWorkflowImpl.class);

        // Create and configure OpenAPI client
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("https://api.petstore.example.com/v1");
        // Add authentication if needed
        // apiClient.setApiKey("your-api-key");

        // Register activity implementation with the API client
        worker.registerActivitiesImplementations(new PetStoreActivityImpl(apiClient));

        // Start the worker
        System.out.println("Starting Temporal worker for task queue: " + TASK_QUEUE);
        factory.start();
        
        System.out.println("Worker started successfully. Press Ctrl+C to exit.");
    }
}
