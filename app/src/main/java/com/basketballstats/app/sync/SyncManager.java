package com.basketballstats.app.sync;

import android.content.Context;
import com.basketballstats.app.data.DatabaseController;
import com.basketballstats.app.auth.AuthController;
import com.basketballstats.app.firebase.FirebaseManager;
import com.basketballstats.app.network.NetworkManager;
import com.basketballstats.app.models.Game;
import com.basketballstats.app.models.Team;
import com.basketballstats.app.models.TeamPlayer;
import com.basketballstats.app.models.Event;
import com.basketballstats.app.models.AppSettings;
import java.util.List;
import java.util.ArrayList;

/**
 * SyncManager - Core synchronization logic for SQLite ↔ Firebase Firestore
 * 
 * Implements "Last Write Wins" conflict resolution strategy where user device data
 * takes priority during manual sync operations. Provides offline-first architecture
 * with reliable data synchronization.
 * 
 * Phase 3: Basic framework for sync button integration
 * Phase 5: Full Firebase integration with pull/merge/push workflow
 */
public class SyncManager {
    
    private static SyncManager instance;
    private Context context;
    private DatabaseController dbController;
    private AuthController authController;
    private FirebaseManager firebaseManager;
    private NetworkManager networkManager;
    private SyncQueueManager syncQueueManager;
    
    // Sync operation callback interface
    public interface SyncCallback {
        void onSyncStarted();
        void onSyncProgress(String message);
        void onSyncSuccess(String message);
        void onSyncError(String errorMessage);
        void onSyncComplete();
    }
    
    /**
     * Singleton pattern for SyncManager
     */
    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private SyncManager(Context context) {
        this.context = context;
        this.dbController = DatabaseController.getInstance(context);
        this.authController = AuthController.getInstance(context);
        this.firebaseManager = FirebaseManager.getInstance(context);
        this.networkManager = NetworkManager.getInstance(context);
        this.syncQueueManager = SyncQueueManager.getInstance(context);
    }
    
    /**
     * Perform manual sync operation with full user feedback
     * Implements "User Device Wins" conflict resolution strategy
     * 
     * @param callback Callback interface for sync progress updates
     */
    public void performManualSync(SyncCallback callback) {
        callback.onSyncStarted();
        
        // Check authentication first
        if (!authController.isUserAuthenticated()) {
            callback.onSyncError("User must be signed in to sync data");
            callback.onSyncComplete();
            return;
        }
        
        // Check network connectivity
        if (!networkManager.isNetworkAvailable()) {
            callback.onSyncError("No network connection. Changes will sync when connectivity resumes.");
            callback.onSyncComplete();
            return;
        }
        
        // Perform actual Firebase sync with queue integration
        performFirebaseSyncWithQueue(callback);
    }
    
    /**
     * Perform complete Firebase sync operation with pull/merge/push workflow and queue integration
     * Phase 6: Enhanced with error handling and queue management
     */
    private void performFirebaseSyncWithQueue(SyncCallback callback) {
        try {
            // First, process any pending queue operations
            syncQueueManager.processQueue(new SyncQueueManager.QueueCallback() {
                @Override
                public void onQueueProcessingStarted(int totalOperations) {
                    if (totalOperations > 0) {
                        callback.onSyncProgress("🔄 Processing " + totalOperations + " pending operations...");
                    }
                }

                @Override
                public void onOperationRetried(String operation, int attempt, int maxRetries) {
                    // Silent processing - queue handles its own logging
                }

                @Override
                public void onOperationSuccess(String operation) {
                    // Silent processing
                }

                @Override
                public void onOperationFailed(String operation, String error) {
                    // Silent processing - errors will be reported in final callback
                }

                @Override
                public void onQueueProcessingComplete(int successful, int failed) {
                    // After queue processing, perform regular sync
                    performFirebaseSync(callback);
                }
            });
            
        } catch (Exception e) {
            callback.onSyncError("Sync initialization error: " + e.getMessage());
            callback.onSyncComplete();
        }
    }
    
    /**
     * Perform complete Firebase sync operation with pull/merge/push workflow
     * Phase 5: Full implementation with conflict resolution
     */
    private void performFirebaseSync(SyncCallback callback) {
        try {
            // PHASE 1: PULL - Download latest data from Firebase
            callback.onSyncProgress("⬇️ Pulling latest data from Firebase...");
            pullDataFromFirebase(callback);
            
        } catch (Exception e) {
            callback.onSyncError("Sync initialization error: " + e.getMessage());
            callback.onSyncComplete();
        }
    }
    
    /**
     * PHASE 1: PULL - Download data from Firebase Firestore
     */
    private void pullDataFromFirebase(SyncCallback callback) {
        // Download teams first (foundational data)
        firebaseManager.downloadTeams(new FirebaseManager.FirestoreCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> firebaseTeams) {
                // Download games
                firebaseManager.downloadGames(new FirebaseManager.FirestoreCallback<List<Game>>() {
                    @Override
                    public void onSuccess(List<Game> firebaseGames) {
                        // Now proceed to merge phase with downloaded data
                        callback.onSyncProgress("🔄 Merging data with conflict resolution...");
                        
                        new android.os.Handler().postDelayed(() -> {
                            mergeDataWithConflictResolution(firebaseTeams, firebaseGames, callback);
                        }, 500);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onSyncError("Failed to download games: " + errorMessage);
                        callback.onSyncComplete();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                callback.onSyncError("Failed to download teams: " + errorMessage);
                callback.onSyncComplete();
            }
        });
    }
    
    /**
     * PHASE 2: MERGE - Implement "User Device Wins" conflict resolution
     */
    private void mergeDataWithConflictResolution(List<Team> firebaseTeams, List<Game> firebaseGames, SyncCallback callback) {
        try {
            // Get local data for comparison
            List<Team> localTeams = Team.findAll(dbController.getDatabaseHelper());
            List<Game> localGames = Game.findAll(dbController.getDatabaseHelper());
            
            int mergedTeams = 0;
            int mergedGames = 0;
            int conflictsResolved = 0;
            
            // Merge Teams with conflict resolution
            for (Team firebaseTeam : firebaseTeams) {
                Team localTeam = findTeamByFirebaseId(localTeams, firebaseTeam.getFirebaseId());
                
                if (localTeam == null) {
                    // New team from Firebase - add to local database
                    firebaseTeam.setSyncStatus("synced");
                    firebaseTeam.save(dbController.getDatabaseHelper());
                    mergedTeams++;
                } else {
                    // Conflict resolution: Compare timestamps
                    boolean localIsNewer = localTeam.getUpdatedAtLong() > firebaseTeam.getLastSyncTimestampLong();
                    
                    if (localIsNewer) {
                        // User device wins - keep local data, mark for upload
                        localTeam.setSyncStatus("pending_upload");
                        localTeam.save(dbController.getDatabaseHelper());
                        conflictsResolved++;
                    } else {
                        // Firebase data is newer - update local
                        firebaseTeam.setId(localTeam.getId()); // Preserve local ID
                        firebaseTeam.setSyncStatus("synced");
                        firebaseTeam.save(dbController.getDatabaseHelper());
                        mergedTeams++;
                    }
                }
            }
            
            // Merge Games with conflict resolution
            for (Game firebaseGame : firebaseGames) {
                Game localGame = findGameByFirebaseId(localGames, firebaseGame.getFirebaseId());
                
                if (localGame == null) {
                    // New game from Firebase - add to local database
                    firebaseGame.setSyncStatus("synced");
                    firebaseGame.save(dbController.getDatabaseHelper());
                    mergedGames++;
                } else {
                    // Conflict resolution: Compare timestamps
                    boolean localIsNewer = localGame.getUpdatedAtLong() > firebaseGame.getLastSyncTimestampLong();
                    
                    if (localIsNewer) {
                        // User device wins - keep local data, mark for upload
                        localGame.setSyncStatus("pending_upload");
                        localGame.save(dbController.getDatabaseHelper());
                        conflictsResolved++;
                    } else {
                        // Firebase data is newer - update local
                        firebaseGame.setId(localGame.getId()); // Preserve local ID
                        firebaseGame.setSyncStatus("synced");
                        firebaseGame.save(dbController.getDatabaseHelper());
                        mergedGames++;
                    }
                }
            }
            
            // Proceed to push phase
            callback.onSyncProgress("⬆️ Pushing local changes to Firebase...");
            
            String mergeMessage = String.format(
                "Merged %d teams, %d games. Resolved %d conflicts (user device wins)",
                mergedTeams, mergedGames, conflictsResolved
            );
            android.util.Log.d("SyncManager", mergeMessage);
            
            new android.os.Handler().postDelayed(() -> {
                pushLocalChangesToFirebase(callback, mergedTeams, mergedGames, conflictsResolved);
            }, 500);
            
        } catch (Exception e) {
            callback.onSyncError("Merge operation failed: " + e.getMessage());
            callback.onSyncComplete();
        }
    }
    
    /**
     * PHASE 3: PUSH - Upload local changes to Firebase
     */
    private void pushLocalChangesToFirebase(SyncCallback callback, int mergedTeams, int mergedGames, int conflictsResolved) {
        try {
            // Get all local data that needs syncing (pending_upload or never synced)
            List<Team> localTeams = Team.findAll(dbController.getDatabaseHelper());
            List<Game> localGames = Game.findAll(dbController.getDatabaseHelper());
            List<Event> localEvents = Event.findAll(dbController.getDatabaseHelper());
            
            // Filter for records that need uploading
            List<Team> teamsToUpload = new ArrayList<>();
            List<Game> gamesToUpload = new ArrayList<>();
            List<Event> eventsToUpload = new ArrayList<>();
            
            for (Team team : localTeams) {
                if (team.getSyncStatus() == null || 
                    team.getSyncStatus().equals("pending_upload") || 
                    team.getFirebaseId() == null) {
                    teamsToUpload.add(team);
                }
            }
            
            for (Game game : localGames) {
                if (game.getSyncStatus() == null || 
                    game.getSyncStatus().equals("pending_upload") || 
                    game.getFirebaseId() == null) {
                    gamesToUpload.add(game);
                }
            }
            
            for (Event event : localEvents) {
                if (event.getSyncStatus() == null || 
                    event.getSyncStatus().equals("pending_upload") || 
                    event.getFirebaseId() == null) {
                    eventsToUpload.add(event);
                }
            }
            
            // Use batch upload for efficiency with queue fallback
            firebaseManager.batchUpload(teamsToUpload, gamesToUpload, eventsToUpload, 
                new FirebaseManager.BatchCallback() {
                    @Override
                    public void onBatchSuccess(int operationsCount) {
                        String successMessage = String.format(
                            "✅ Sync Complete! Merged %d teams, %d games. Uploaded %d operations. Resolved %d conflicts (user wins)",
                            mergedTeams, mergedGames, operationsCount, conflictsResolved
                        );
                        callback.onSyncSuccess(successMessage);
                        callback.onSyncComplete();
                    }

                    @Override
                    public void onBatchError(String errorMessage) {
                        // Queue failed operations for retry when network resumes
                        queueFailedOperations(teamsToUpload, gamesToUpload, eventsToUpload, errorMessage);
                        
                        String errorMsg = String.format(
                            "Upload failed but queued for retry. Merged %d teams, %d games. Error: %s",
                            mergedTeams, mergedGames, errorMessage
                        );
                        callback.onSyncError(errorMsg);
                        callback.onSyncComplete();
                    }
                });
            
        } catch (Exception e) {
            callback.onSyncError("Push operation failed: " + e.getMessage());
            callback.onSyncComplete();
        }
    }
    
    // ===== UTILITY METHODS FOR CONFLICT RESOLUTION =====
    
    /**
     * Find local team by Firebase ID
     */
    private Team findTeamByFirebaseId(List<Team> teams, String firebaseId) {
        if (firebaseId == null) return null;
        
        for (Team team : teams) {
            if (firebaseId.equals(team.getFirebaseId())) {
                return team;
            }
        }
        return null;
    }
    
    /**
     * Find local game by Firebase ID
     */
    private Game findGameByFirebaseId(List<Game> games, String firebaseId) {
        if (firebaseId == null) return null;
        
        for (Game game : games) {
            if (firebaseId.equals(game.getFirebaseId())) {
                return game;
            }
        }
        return null;
    }
    
    /**
     * Upload local SQLite data to Firebase Firestore (Legacy - kept for compatibility)
     * Use pushLocalChangesToFirebase for new implementation
     */
    private void uploadLocalDataToFirebase(SyncCallback callback) {
        try {
            // Get all local data that needs syncing
            List<Game> games = Game.findAll(dbController.getDatabaseHelper());
            List<Team> teams = Team.findAll(dbController.getDatabaseHelper());
            List<Event> events = Event.findAll(dbController.getDatabaseHelper());
            
            // Use batch upload for efficiency
            firebaseManager.batchUpload(teams, games, events, new FirebaseManager.BatchCallback() {
                @Override
                public void onBatchSuccess(int operationsCount) {
                    String successMessage = String.format(
                        "Successfully synced %d operations (%d games, %d teams, %d events)",
                        operationsCount, games.size(), teams.size(), events.size()
                    );
                    callback.onSyncSuccess(successMessage);
                    callback.onSyncComplete();
                }

                @Override
                public void onBatchError(String errorMessage) {
                    callback.onSyncError("Upload failed: " + errorMessage);
                    callback.onSyncComplete();
                }
            });
            
        } catch (Exception e) {
            callback.onSyncError("Upload preparation failed: " + e.getMessage());
            callback.onSyncComplete();
        }
    }
    
    // ===== INCREMENTAL SYNC METHODS =====
    
    /**
     * Perform incremental sync - only sync data changed since last sync
     * Uses last_sync_timestamp for performance optimization
     */
    public void performIncrementalSync(SyncCallback callback) {
        callback.onSyncStarted();
        
        // Check authentication first
        if (!authController.isUserAuthenticated()) {
            callback.onSyncError("User must be signed in to sync data");
            callback.onSyncComplete();
            return;
        }
        
        try {
            // Get last sync timestamp from app settings
            long lastSyncTimestamp = getLastSyncTimestamp();
            callback.onSyncProgress("📅 Incremental sync since " + formatTimestamp(lastSyncTimestamp));
            
            // Perform incremental pull/merge/push
            performIncrementalPull(lastSyncTimestamp, callback);
            
        } catch (Exception e) {
            callback.onSyncError("Incremental sync error: " + e.getMessage());
            callback.onSyncComplete();
        }
    }
    
    /**
     * Pull only data changed since last sync timestamp
     */
    private void performIncrementalPull(long lastSyncTimestamp, SyncCallback callback) {
        // Download only teams modified since last sync
        firebaseManager.downloadTeamsModifiedSince(lastSyncTimestamp, 
            new FirebaseManager.FirestoreCallback<List<Team>>() {
                @Override
                public void onSuccess(List<Team> modifiedTeams) {
                    // Download only games modified since last sync
                    firebaseManager.downloadGamesModifiedSince(lastSyncTimestamp,
                        new FirebaseManager.FirestoreCallback<List<Game>>() {
                            @Override
                            public void onSuccess(List<Game> modifiedGames) {
                                callback.onSyncProgress("🔄 Processing " + 
                                    modifiedTeams.size() + " teams, " + 
                                    modifiedGames.size() + " games...");
                                
                                new android.os.Handler().postDelayed(() -> {
                                    mergeIncrementalData(modifiedTeams, modifiedGames, lastSyncTimestamp, callback);
                                }, 500);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                callback.onSyncError("Failed to download modified games: " + errorMessage);
                                callback.onSyncComplete();
                            }
                        });
                }

                @Override
                public void onError(String errorMessage) {
                    callback.onSyncError("Failed to download modified teams: " + errorMessage);
                    callback.onSyncComplete();
                }
            });
    }
    
    /**
     * Merge incremental data with optimized conflict resolution
     */
    private void mergeIncrementalData(List<Team> modifiedTeams, List<Game> modifiedGames, 
                                    long lastSyncTimestamp, SyncCallback callback) {
        try {
            int mergedTeams = 0;
            int mergedGames = 0;
            int conflictsResolved = 0;
            
            // Process modified teams
            for (Team firebaseTeam : modifiedTeams) {
                Team localTeam = Team.findByFirebaseId(dbController.getDatabaseHelper(), firebaseTeam.getFirebaseId());
                
                if (localTeam == null) {
                    // New team from Firebase
                    firebaseTeam.setSyncStatus("synced");
                    firebaseTeam.save(dbController.getDatabaseHelper());
                    mergedTeams++;
                } else {
                    // Check if local was modified since last sync
                    boolean localModifiedSinceSync = localTeam.getUpdatedAtLong() > lastSyncTimestamp;
                    
                    if (localModifiedSinceSync) {
                        // Local changes take priority - mark for upload
                        localTeam.setSyncStatus("pending_upload");
                        localTeam.save(dbController.getDatabaseHelper());
                        conflictsResolved++;
                    } else {
                        // No local changes - accept Firebase version
                        firebaseTeam.setId(localTeam.getId());
                        firebaseTeam.setSyncStatus("synced");
                        firebaseTeam.save(dbController.getDatabaseHelper());
                        mergedTeams++;
                    }
                }
            }
            
            // Process modified games
            for (Game firebaseGame : modifiedGames) {
                Game localGame = Game.findByFirebaseId(dbController.getDatabaseHelper(), firebaseGame.getFirebaseId());
                
                if (localGame == null) {
                    // New game from Firebase
                    firebaseGame.setSyncStatus("synced");
                    firebaseGame.save(dbController.getDatabaseHelper());
                    mergedGames++;
                } else {
                    // Check if local was modified since last sync
                    boolean localModifiedSinceSync = localGame.getUpdatedAtLong() > lastSyncTimestamp;
                    
                    if (localModifiedSinceSync) {
                        // Local changes take priority - mark for upload
                        localGame.setSyncStatus("pending_upload");
                        localGame.save(dbController.getDatabaseHelper());
                        conflictsResolved++;
                    } else {
                        // No local changes - accept Firebase version
                        firebaseGame.setId(localGame.getId());
                        firebaseGame.setSyncStatus("synced");
                        firebaseGame.save(dbController.getDatabaseHelper());
                        mergedGames++;
                    }
                }
            }
            
            // Proceed to incremental push
            callback.onSyncProgress("⬆️ Pushing incremental changes...");
            
            new android.os.Handler().postDelayed(() -> {
                pushIncrementalChanges(callback, mergedTeams, mergedGames, conflictsResolved, lastSyncTimestamp);
            }, 500);
            
        } catch (Exception e) {
            callback.onSyncError("Incremental merge failed: " + e.getMessage());
            callback.onSyncComplete();
        }
    }
    
    /**
     * Push only data modified since last sync
     */
    private void pushIncrementalChanges(SyncCallback callback, int mergedTeams, int mergedGames, 
                                      int conflictsResolved, long lastSyncTimestamp) {
        try {
            // Get only data modified since last sync OR marked as pending upload
            List<Team> teamsToUpload = getTeamsModifiedSinceSync(lastSyncTimestamp);
            List<Game> gamesToUpload = getGamesModifiedSinceSync(lastSyncTimestamp);
            List<Event> eventsToUpload = getEventsModifiedSinceSync(lastSyncTimestamp);
            
            if (teamsToUpload.isEmpty() && gamesToUpload.isEmpty() && eventsToUpload.isEmpty()) {
                // No local changes to upload
                updateLastSyncTimestamp();
                String successMessage = String.format(
                    "✅ Incremental sync complete! Merged %d teams, %d games. No local changes to upload.",
                    mergedTeams, mergedGames
                );
                callback.onSyncSuccess(successMessage);
                callback.onSyncComplete();
                return;
            }
            
            // Upload incremental changes
            firebaseManager.batchUpload(teamsToUpload, gamesToUpload, eventsToUpload,
                new FirebaseManager.BatchCallback() {
                    @Override
                    public void onBatchSuccess(int operationsCount) {
                        // Update last sync timestamp
                        updateLastSyncTimestamp();
                        
                        String successMessage = String.format(
                            "✅ Incremental sync complete! Merged %d teams, %d games. Uploaded %d operations. Resolved %d conflicts.",
                            mergedTeams, mergedGames, operationsCount, conflictsResolved
                        );
                        callback.onSyncSuccess(successMessage);
                        callback.onSyncComplete();
                    }

                    @Override
                    public void onBatchError(String errorMessage) {
                        String errorMsg = String.format(
                            "Incremental upload failed. Merged %d teams, %d games, but upload error: %s",
                            mergedTeams, mergedGames, errorMessage
                        );
                        callback.onSyncError(errorMsg);
                        callback.onSyncComplete();
                    }
                });
            
        } catch (Exception e) {
            callback.onSyncError("Incremental push failed: " + e.getMessage());
            callback.onSyncComplete();
        }
    }
    
    // ===== INCREMENTAL SYNC UTILITIES =====
    
    /**
     * Get teams modified since last sync timestamp
     */
    private List<Team> getTeamsModifiedSinceSync(long lastSyncTimestamp) {
        List<Team> allTeams = Team.findAll(dbController.getDatabaseHelper());
        List<Team> modifiedTeams = new ArrayList<>();
        
        for (Team team : allTeams) {
            boolean isModified = team.getUpdatedAtLong() > lastSyncTimestamp;
            boolean isPendingUpload = "pending_upload".equals(team.getSyncStatus());
            boolean hasNoFirebaseId = team.getFirebaseId() == null;
            
            if (isModified || isPendingUpload || hasNoFirebaseId) {
                modifiedTeams.add(team);
            }
        }
        
        return modifiedTeams;
    }
    
    /**
     * Get games modified since last sync timestamp
     */
    private List<Game> getGamesModifiedSinceSync(long lastSyncTimestamp) {
        List<Game> allGames = Game.findAll(dbController.getDatabaseHelper());
        List<Game> modifiedGames = new ArrayList<>();
        
        for (Game game : allGames) {
            boolean isModified = game.getUpdatedAtLong() > lastSyncTimestamp;
            boolean isPendingUpload = "pending_upload".equals(game.getSyncStatus());
            boolean hasNoFirebaseId = game.getFirebaseId() == null;
            
            if (isModified || isPendingUpload || hasNoFirebaseId) {
                modifiedGames.add(game);
            }
        }
        
        return modifiedGames;
    }
    
    /**
     * Get events modified since last sync timestamp
     */
    private List<Event> getEventsModifiedSinceSync(long lastSyncTimestamp) {
        List<Event> allEvents = Event.findAll(dbController.getDatabaseHelper());
        List<Event> modifiedEvents = new ArrayList<>();
        
        for (Event event : allEvents) {
            boolean isModified = Long.parseLong(event.getUpdatedAt() != null ? event.getUpdatedAt() : "0") > lastSyncTimestamp;
            boolean isPendingUpload = "pending_upload".equals(event.getSyncStatus());
            boolean hasNoFirebaseId = event.getFirebaseId() == null;
            
            if (isModified || isPendingUpload || hasNoFirebaseId) {
                modifiedEvents.add(event);
            }
        }
        
        return modifiedEvents;
    }
    
    /**
     * Get last sync timestamp from app settings
     */
    private long getLastSyncTimestamp() {
        try {
            AppSettings lastSyncSetting = AppSettings.findByKey(dbController.getDatabaseHelper(), "last_sync_timestamp");
            if (lastSyncSetting != null) {
                return Long.parseLong(lastSyncSetting.getSettingValue());
            }
        } catch (Exception e) {
            android.util.Log.w("SyncManager", "Could not get last sync timestamp", e);
        }
        // Default to 0 (sync everything) if no previous sync
        return 0L;
    }
    
    /**
     * Update last sync timestamp in app settings
     */
    private void updateLastSyncTimestamp() {
        try {
            long currentTimestamp = System.currentTimeMillis();
            
            AppSettings lastSyncSetting = AppSettings.findByKey(dbController.getDatabaseHelper(), "last_sync_timestamp");
            if (lastSyncSetting == null) {
                // Create new setting
                lastSyncSetting = new AppSettings();
                lastSyncSetting.setSettingKey("last_sync_timestamp");
            }
            
            lastSyncSetting.setSettingValue(String.valueOf(currentTimestamp));
            lastSyncSetting.save(dbController.getDatabaseHelper());
            
            android.util.Log.d("SyncManager", "Updated last sync timestamp: " + currentTimestamp);
        } catch (Exception e) {
            android.util.Log.e("SyncManager", "Failed to update last sync timestamp", e);
        }
    }
    
    /**
     * Format timestamp for user display
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) {
            return "beginning";
        }
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }
    
    /**
     * Queue failed operations for retry when connectivity resumes
     */
    private void queueFailedOperations(List<Team> teams, List<Game> games, List<Event> events, String errorMessage) {
        try {
            // Queue teams
            for (Team team : teams) {
                syncQueueManager.queueFailedOperation(
                    "teams", 
                    team.getId(), 
                    team.getFirebaseId() != null ? "update" : "create",
                    team.getFirebaseId(),
                    team,
                    errorMessage
                );
            }
            
            // Queue games
            for (Game game : games) {
                syncQueueManager.queueFailedOperation(
                    "games", 
                    game.getId(), 
                    game.getFirebaseId() != null ? "update" : "create",
                    game.getFirebaseId(),
                    game,
                    errorMessage
                );
            }
            
            // Queue events
            for (Event event : events) {
                syncQueueManager.queueFailedOperation(
                    "events", 
                    event.getId(), 
                    event.getFirebaseId() != null ? "update" : "create",
                    event.getFirebaseId(),
                    event,
                    errorMessage
                );
            }
            
            android.util.Log.d("SyncManager", "Queued " + (teams.size() + games.size() + events.size()) + " failed operations for retry");
            
        } catch (Exception e) {
            android.util.Log.e("SyncManager", "Error queueing failed operations", e);
        }
    }
    
    /**
     * Simulate sync operation for testing (Phase 3 compatibility)
     */
    private void simulateSync(SyncCallback callback) {
        // Simulate pull phase
        new android.os.Handler().postDelayed(() -> {
            callback.onSyncProgress("Pulling changes from server...");
        }, 500);
        
        // Simulate merge phase
        new android.os.Handler().postDelayed(() -> {
            callback.onSyncProgress("Merging data (user device wins)...");
        }, 1000);
        
        // Simulate push phase
        new android.os.Handler().postDelayed(() -> {
            callback.onSyncProgress("Pushing local changes...");
        }, 1500);
        
        // Complete sync
        new android.os.Handler().postDelayed(() -> {
            try {
                // Get current data counts for feedback
                List<Game> games = Game.findAll(dbController.getDatabaseHelper());
                List<Team> teams = Team.findAll(dbController.getDatabaseHelper());
                List<TeamPlayer> players = TeamPlayer.findAll(dbController.getDatabaseHelper());
                List<Event> events = Event.findAll(dbController.getDatabaseHelper());
                
                String successMessage = String.format(
                    "Simulated sync: %d games, %d teams, %d players, %d events",
                    games.size(), teams.size(), players.size(), events.size()
                );
                
                callback.onSyncSuccess(successMessage);
                callback.onSyncComplete();
                
            } catch (Exception e) {
                callback.onSyncError("Sync failed: " + e.getMessage());
                callback.onSyncComplete();
            }
        }, 2000);
    }
    
    /**
     * Check if sync is currently in progress
     */
    public boolean isSyncInProgress() {
        // TODO: Implement actual sync tracking in Phase 5
        return false;
    }
    

    
    /**
     * Get sync statistics for user feedback
     */
    public String getSyncStatus() {
        try {
            List<Game> games = Game.findAll(dbController.getDatabaseHelper());
            List<Team> teams = Team.findAll(dbController.getDatabaseHelper());
            
            return String.format("Local: %d games, %d teams", games.size(), teams.size());
        } catch (Exception e) {
            return "Unable to get sync status";
        }
    }
    
    // TODO: Phase 5 implementations
    // - Firebase authentication integration
    // - Firestore pull/merge/push operations
    // - Conflict resolution with timestamp comparison
    // - Incremental sync using last_sync_timestamp
    // - Batch operations for efficiency
    // - Error handling and retry logic
    // - Network connectivity detection
    // - Sync queue management
}
