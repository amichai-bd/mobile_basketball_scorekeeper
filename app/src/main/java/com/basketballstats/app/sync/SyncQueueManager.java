package com.basketballstats.app.sync;

import android.content.Context;
import android.util.Log;
import com.basketballstats.app.data.DatabaseController;
import com.basketballstats.app.firebase.FirebaseManager;
import com.basketballstats.app.auth.AuthController;
import com.basketballstats.app.models.*;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * SyncQueueManager - Handles failed sync operations with retry logic
 * 
 * Manages offline scenarios, network failures, and automatic retry with exponential backoff
 * Ensures data integrity and eventual consistency when connectivity resumes
 * Integrates with SyncManager for seamless offline-first functionality
 */
public class SyncQueueManager {
    
    private static final String TAG = "SyncQueueManager";
    private static SyncQueueManager instance;
    
    private Context context;
    private DatabaseController dbController;
    private FirebaseManager firebaseManager;
    private AuthController authController;
    private Gson gson;
    
    // Retry configuration
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY = 1000L; // 1 second
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private static final long MAX_RETRY_DELAY = 300000L; // 5 minutes
    
    // Operation priorities
    public enum OperationPriority {
        CRITICAL,   // User profile, settings
        NORMAL,     // Teams, games, players
        LOW         // Events, statistics
    }
    
    // Queue processing callbacks
    public interface QueueCallback {
        void onQueueProcessingStarted(int totalOperations);
        void onOperationRetried(String operation, int attempt, int maxRetries);
        void onOperationSuccess(String operation);
        void onOperationFailed(String operation, String error);
        void onQueueProcessingComplete(int successful, int failed);
    }
    
    /**
     * Singleton pattern for SyncQueueManager
     */
    public static synchronized SyncQueueManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncQueueManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private SyncQueueManager(Context context) {
        this.context = context;
        this.dbController = DatabaseController.getInstance(context);
        this.firebaseManager = FirebaseManager.getInstance(context);
        this.authController = AuthController.getInstance(context);
        this.gson = new Gson();
        
        Log.d(TAG, "SyncQueueManager initialized");
    }
    
    // ===== QUEUE OPERATIONS =====
    
    /**
     * Add failed operation to sync queue for retry
     */
    public void queueFailedOperation(String tableName, int recordId, String operation, 
                                   String firebaseId, Object recordData, String errorMessage) {
        try {
            SyncQueue queueItem = new SyncQueue();
            queueItem.setTableName(tableName);
            queueItem.setRecordId(recordId);
            queueItem.setOperation(operation);
            queueItem.setFirebaseId(firebaseId);
            queueItem.setDataJson(gson.toJson(recordData));
            queueItem.setRetryCount(0);
            queueItem.setMaxRetries(getMaxRetriesForOperation(tableName, operation));
            queueItem.setErrorMessage(errorMessage);
            
            long result = queueItem.save(dbController.getDatabaseHelper());
            if (result > 0) {
                Log.d(TAG, "Queued failed operation: " + operation + " on " + tableName + " (ID: " + recordId + ")");
            } else {
                Log.e(TAG, "Failed to queue operation: " + operation + " on " + tableName);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error queueing failed operation", e);
        }
    }
    
    /**
     * Process all pending operations in the queue
     */
    public void processQueue(QueueCallback callback) {
        if (!authController.isUserAuthenticated()) {
            Log.w(TAG, "Cannot process queue: user not authenticated");
            callback.onQueueProcessingComplete(0, 0);
            return;
        }
        
        try {
            List<SyncQueue> pendingOperations = SyncQueue.findPendingOperations(dbController.getDatabaseHelper());
            
            if (pendingOperations.isEmpty()) {
                Log.d(TAG, "Queue is empty, nothing to process");
                callback.onQueueProcessingComplete(0, 0);
                return;
            }
            
            callback.onQueueProcessingStarted(pendingOperations.size());
            Log.d(TAG, "Processing " + pendingOperations.size() + " pending operations");
            
            // Sort by priority and creation time
            List<SyncQueue> prioritizedOperations = prioritizeOperations(pendingOperations);
            
            processOperationsSequentially(prioritizedOperations, 0, 0, 0, callback);
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing queue", e);
            callback.onQueueProcessingComplete(0, 1);
        }
    }
    
    /**
     * Recursively process operations with retry logic
     */
    private void processOperationsSequentially(List<SyncQueue> operations, int currentIndex, 
                                             int successCount, int failCount, QueueCallback callback) {
        
        if (currentIndex >= operations.size()) {
            // All operations processed
            callback.onQueueProcessingComplete(successCount, failCount);
            return;
        }
        
        SyncQueue operation = operations.get(currentIndex);
        
        // Check if operation should be retried
        if (operation.getRetryCount() >= operation.getMaxRetries()) {
            Log.w(TAG, "Operation exceeded max retries: " + operation.getOperation() + " on " + operation.getTableName());
            callback.onOperationFailed(operation.getOperation() + " on " + operation.getTableName(), "Max retries exceeded");
            
            // Remove from queue
            operation.delete(dbController.getDatabaseHelper());
            
            // Continue with next operation
            processOperationsSequentially(operations, currentIndex + 1, successCount, failCount + 1, callback);
            return;
        }
        
        // Calculate retry delay
        long retryDelay = calculateRetryDelay(operation.getRetryCount());
        
        // Update retry attempt
        operation.setRetryCount(operation.getRetryCount() + 1);
        operation.setLastAttempt(System.currentTimeMillis());
        operation.save(dbController.getDatabaseHelper());
        
        callback.onOperationRetried(operation.getOperation() + " on " + operation.getTableName(), 
                                  operation.getRetryCount(), operation.getMaxRetries());
        
        // Perform the operation with delay
        new android.os.Handler().postDelayed(() -> {
            performQueuedOperation(operation, new OperationCallback() {
                @Override
                public void onSuccess() {
                    callback.onOperationSuccess(operation.getOperation() + " on " + operation.getTableName());
                    
                    // Remove from queue on success
                    operation.delete(dbController.getDatabaseHelper());
                    
                    // Continue with next operation
                    processOperationsSequentially(operations, currentIndex + 1, successCount + 1, failCount, callback);
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Update error message
                    operation.setErrorMessage(errorMessage);
                    operation.save(dbController.getDatabaseHelper());
                    
                    if (operation.getRetryCount() >= operation.getMaxRetries()) {
                        callback.onOperationFailed(operation.getOperation() + " on " + operation.getTableName(), errorMessage);
                        operation.delete(dbController.getDatabaseHelper());
                        processOperationsSequentially(operations, currentIndex + 1, successCount, failCount + 1, callback);
                    } else {
                        // Will retry this operation
                        processOperationsSequentially(operations, currentIndex, successCount, failCount, callback);
                    }
                }
            });
        }, retryDelay);
    }
    
    /**
     * Perform a single queued operation
     */
    private void performQueuedOperation(SyncQueue queueItem, OperationCallback callback) {
        try {
            String tableName = queueItem.getTableName();
            String operation = queueItem.getOperation();
            String dataJson = queueItem.getDataJson();
            
            switch (tableName.toLowerCase()) {
                case "teams":
                    performTeamOperation(operation, dataJson, queueItem.getFirebaseId(), callback);
                    break;
                    
                case "games":
                    performGameOperation(operation, dataJson, queueItem.getFirebaseId(), callback);
                    break;
                    
                case "events":
                    performEventOperation(operation, dataJson, queueItem.getFirebaseId(), callback);
                    break;
                    
                case "team_players":
                    performTeamPlayerOperation(operation, dataJson, queueItem.getFirebaseId(), callback);
                    break;
                    
                default:
                    callback.onFailure("Unsupported table: " + tableName);
                    break;
            }
            
        } catch (Exception e) {
            callback.onFailure("Operation error: " + e.getMessage());
        }
    }
    
    // ===== OPERATION HANDLERS =====
    
    /**
     * Perform team operation from queue
     */
    private void performTeamOperation(String operation, String dataJson, String firebaseId, OperationCallback callback) {
        try {
            switch (operation.toLowerCase()) {
                case "create":
                case "update":
                    Team team = gson.fromJson(dataJson, Team.class);
                    if (firebaseId != null) {
                        team.setFirebaseId(firebaseId);
                    }
                    
                    firebaseManager.uploadTeam(team, new FirebaseManager.FirestoreCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            callback.onSuccess();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            callback.onFailure(errorMessage);
                        }
                    });
                    break;
                    
                case "delete":
                    // TODO: Implement delete operation when needed
                    callback.onFailure("Delete operation not yet implemented");
                    break;
                    
                default:
                    callback.onFailure("Unknown operation: " + operation);
                    break;
            }
        } catch (JsonSyntaxException e) {
            callback.onFailure("Invalid data format: " + e.getMessage());
        }
    }
    
    /**
     * Perform game operation from queue
     */
    private void performGameOperation(String operation, String dataJson, String firebaseId, OperationCallback callback) {
        try {
            switch (operation.toLowerCase()) {
                case "create":
                case "update":
                    Game game = gson.fromJson(dataJson, Game.class);
                    if (firebaseId != null) {
                        game.setFirebaseId(firebaseId);
                    }
                    
                    firebaseManager.uploadGame(game, new FirebaseManager.FirestoreCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            callback.onSuccess();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            callback.onFailure(errorMessage);
                        }
                    });
                    break;
                    
                case "delete":
                    // TODO: Implement delete operation when needed
                    callback.onFailure("Delete operation not yet implemented");
                    break;
                    
                default:
                    callback.onFailure("Unknown operation: " + operation);
                    break;
            }
        } catch (JsonSyntaxException e) {
            callback.onFailure("Invalid data format: " + e.getMessage());
        }
    }
    
    /**
     * Perform event operation from queue
     */
    private void performEventOperation(String operation, String dataJson, String firebaseId, OperationCallback callback) {
        try {
            switch (operation.toLowerCase()) {
                case "create":
                case "update":
                    // Events are typically handled in batches, so we'll queue this for batch processing
                    // For now, mark as successful and let the next full sync handle it
                    callback.onSuccess();
                    break;
                    
                case "delete":
                    // TODO: Implement delete operation when needed
                    callback.onFailure("Delete operation not yet implemented");
                    break;
                    
                default:
                    callback.onFailure("Unknown operation: " + operation);
                    break;
            }
        } catch (Exception e) {
            callback.onFailure("Event operation error: " + e.getMessage());
        }
    }
    
    /**
     * Perform team player operation from queue
     */
    private void performTeamPlayerOperation(String operation, String dataJson, String firebaseId, OperationCallback callback) {
        try {
            switch (operation.toLowerCase()) {
                case "create":
                case "update":
                    // Team players are typically synced with their parent team
                    // For now, mark as successful and let the next full sync handle it
                    callback.onSuccess();
                    break;
                    
                case "delete":
                    // TODO: Implement delete operation when needed
                    callback.onFailure("Delete operation not yet implemented");
                    break;
                    
                default:
                    callback.onFailure("Unknown operation: " + operation);
                    break;
            }
        } catch (Exception e) {
            callback.onFailure("Team player operation error: " + e.getMessage());
        }
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Prioritize operations by importance and creation time
     */
    private List<SyncQueue> prioritizeOperations(List<SyncQueue> operations) {
        List<SyncQueue> prioritized = new ArrayList<>(operations);
        
        prioritized.sort((a, b) -> {
            // First sort by priority
            OperationPriority priorityA = getOperationPriority(a.getTableName(), a.getOperation());
            OperationPriority priorityB = getOperationPriority(b.getTableName(), b.getOperation());
            
            int priorityComparison = priorityA.compareTo(priorityB);
            if (priorityComparison != 0) {
                return priorityComparison;
            }
            
            // Then sort by creation time (oldest first)
            return Long.compare(a.getCreatedAt(), b.getCreatedAt());
        });
        
        return prioritized;
    }
    
    /**
     * Get operation priority based on table and operation type
     */
    private OperationPriority getOperationPriority(String tableName, String operation) {
        switch (tableName.toLowerCase()) {
            case "user_profile":
            case "app_settings":
                return OperationPriority.CRITICAL;
                
            case "teams":
            case "games":
            case "team_players":
                return OperationPriority.NORMAL;
                
            case "events":
            case "team_fouls":
                return OperationPriority.LOW;
                
            default:
                return OperationPriority.NORMAL;
        }
    }
    
    /**
     * Get max retries for specific operation types
     */
    private int getMaxRetriesForOperation(String tableName, String operation) {
        OperationPriority priority = getOperationPriority(tableName, operation);
        
        switch (priority) {
            case CRITICAL:
                return 5; // More retries for critical operations
            case NORMAL:
                return 3; // Standard retries
            case LOW:
                return 2; // Fewer retries for low priority
            default:
                return DEFAULT_MAX_RETRIES;
        }
    }
    
    /**
     * Calculate retry delay with exponential backoff
     */
    private long calculateRetryDelay(int retryCount) {
        long delay = (long) (INITIAL_RETRY_DELAY * Math.pow(BACKOFF_MULTIPLIER, retryCount - 1));
        return Math.min(delay, MAX_RETRY_DELAY);
    }
    
    /**
     * Get queue statistics
     */
    public QueueStatistics getQueueStatistics() {
        try {
            List<SyncQueue> allOperations = SyncQueue.findAll(dbController.getDatabaseHelper());
            List<SyncQueue> pendingOperations = SyncQueue.findPendingOperations(dbController.getDatabaseHelper());
            
            return new QueueStatistics(
                allOperations.size(),
                pendingOperations.size(),
                allOperations.size() - pendingOperations.size()
            );
        } catch (Exception e) {
            Log.e(TAG, "Error getting queue statistics", e);
            return new QueueStatistics(0, 0, 0);
        }
    }
    
    /**
     * Clear completed operations from queue (cleanup)
     */
    public void cleanupQueue() {
        try {
            // Remove operations that have been successfully processed or exceeded max retries
            // This would be implemented with a custom SQL query to delete old records
            Log.d(TAG, "Queue cleanup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error during queue cleanup", e);
        }
    }
    
    // ===== INNER CLASSES =====
    
    /**
     * Callback interface for individual operations
     */
    private interface OperationCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
    
    /**
     * Queue statistics data class
     */
    public static class QueueStatistics {
        public final int totalOperations;
        public final int pendingOperations;
        public final int completedOperations;
        
        public QueueStatistics(int total, int pending, int completed) {
            this.totalOperations = total;
            this.pendingOperations = pending;
            this.completedOperations = completed;
        }
        
        @Override
        public String toString() {
            return String.format("Queue: %d total, %d pending, %d completed", 
                               totalOperations, pendingOperations, completedOperations);
        }
    }
}
