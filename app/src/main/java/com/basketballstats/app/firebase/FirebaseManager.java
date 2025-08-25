package com.basketballstats.app.firebase;

import android.content.Context;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.basketballstats.app.auth.AuthController;
import com.basketballstats.app.models.*;
import com.basketballstats.app.data.DatabaseController;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * FirebaseManager - Firestore CRUD Operations for SQLite Sync
 * 
 * Manages all Firebase Firestore operations for syncing SQLite data to cloud storage
 * Implements user-isolated collections and batch operations for efficiency
 * Mirrors the complete SQLite schema in Firestore collections
 */
public class FirebaseManager {
    
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    
    // Firestore collection names (user-isolated)
    public static final String COLLECTION_TEAMS = "teams";
    public static final String COLLECTION_TEAM_PLAYERS = "team_players";
    public static final String COLLECTION_GAMES = "games";
    public static final String COLLECTION_EVENTS = "events";
    public static final String COLLECTION_TEAM_FOULS = "team_fouls";
    public static final String COLLECTION_APP_SETTINGS = "app_settings";
    public static final String COLLECTION_USER_PROFILE = "user_profile";
    
    private FirebaseFirestore firestore;
    private AuthController authController;
    private DatabaseController dbController;
    private Context context;
    
    // Firestore operation callbacks
    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }
    
    public interface BatchCallback {
        void onBatchSuccess(int operationsCount);
        void onBatchError(String errorMessage);
    }
    
    /**
     * Singleton pattern for FirebaseManager
     */
    public static synchronized FirebaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private FirebaseManager(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.authController = AuthController.getInstance(context);
        this.dbController = DatabaseController.getInstance(context);
        
        Log.d(TAG, "FirebaseManager initialized");
    }
    
    /**
     * Get user-isolated collection reference
     * All data is stored under user's UID for complete data isolation
     */
    private CollectionReference getUserCollection(String collectionName) {
        String userUid = authController.getCurrentUserUid();
        if (userUid == null) {
            throw new IllegalStateException("User must be authenticated");
        }
        return firestore.collection("users").document(userUid).collection(collectionName);
    }
    
    // ===== TEAM OPERATIONS =====
    
    /**
     * Upload team to Firestore
     */
    public void uploadTeam(Team team, FirestoreCallback<String> callback) {
        try {
            CollectionReference teamsRef = getUserCollection(COLLECTION_TEAMS);
            Map<String, Object> teamData = teamToMap(team);
            
            if (team.getFirebaseId() != null) {
                // Update existing document
                teamsRef.document(team.getFirebaseId())
                    .set(teamData)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess("Team updated successfully");
                        Log.d(TAG, "Team updated: " + team.getName());
                    })
                    .addOnFailureListener(e -> {
                        callback.onError("Failed to update team: " + e.getMessage());
                        Log.e(TAG, "Team update failed", e);
                    });
            } else {
                // Create new document
                teamsRef.add(teamData)
                    .addOnSuccessListener(documentReference -> {
                        String firebaseId = documentReference.getId();
                        // Update SQLite with Firebase ID
                        team.setFirebaseId(firebaseId);
                        team.setSyncStatus("synced");
                        team.setLastSyncTimestamp(System.currentTimeMillis());
                        team.save(dbController.getDatabaseHelper());
                        
                        callback.onSuccess("Team created successfully");
                        Log.d(TAG, "Team created: " + team.getName() + " (ID: " + firebaseId + ")");
                    })
                    .addOnFailureListener(e -> {
                        callback.onError("Failed to create team: " + e.getMessage());
                        Log.e(TAG, "Team creation failed", e);
                    });
            }
        } catch (Exception e) {
            callback.onError("Upload team error: " + e.getMessage());
            Log.e(TAG, "Upload team error", e);
        }
    }
    
    /**
     * Download all teams from Firestore
     */
    public void downloadTeams(FirestoreCallback<List<Team>> callback) {
        try {
            getUserCollection(COLLECTION_TEAMS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Team> teams = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Team team = mapToTeam(doc.getData(), doc.getId());
                        if (team != null) {
                            teams.add(team);
                        }
                    }
                    callback.onSuccess(teams);
                    Log.d(TAG, "Downloaded " + teams.size() + " teams");
                })
                .addOnFailureListener(e -> {
                    callback.onError("Failed to download teams: " + e.getMessage());
                    Log.e(TAG, "Teams download failed", e);
                });
        } catch (Exception e) {
            callback.onError("Download teams error: " + e.getMessage());
            Log.e(TAG, "Download teams error", e);
        }
    }
    
    /**
     * Download teams modified since timestamp (incremental sync)
     */
    public void downloadTeamsModifiedSince(long lastSyncTimestamp, FirestoreCallback<List<Team>> callback) {
        try {
            getUserCollection(COLLECTION_TEAMS)
                .whereGreaterThan("lastSyncTimestamp", lastSyncTimestamp)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Team> teams = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Team team = mapToTeam(doc.getData(), doc.getId());
                        if (team != null) {
                            teams.add(team);
                        }
                    }
                    callback.onSuccess(teams);
                    Log.d(TAG, "Downloaded " + teams.size() + " teams modified since " + lastSyncTimestamp);
                })
                .addOnFailureListener(e -> {
                    callback.onError("Failed to download modified teams: " + e.getMessage());
                    Log.e(TAG, "Modified teams download failed", e);
                });
        } catch (Exception e) {
            callback.onError("Download modified teams error: " + e.getMessage());
            Log.e(TAG, "Download modified teams error", e);
        }
    }
    
    // ===== GAME OPERATIONS =====
    
    /**
     * Upload game to Firestore
     */
    public void uploadGame(Game game, FirestoreCallback<String> callback) {
        try {
            CollectionReference gamesRef = getUserCollection(COLLECTION_GAMES);
            Map<String, Object> gameData = gameToMap(game);
            
            if (game.getFirebaseId() != null) {
                // Update existing document
                gamesRef.document(game.getFirebaseId())
                    .set(gameData)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess("Game updated successfully");
                        Log.d(TAG, "Game updated: " + game.getId());
                    })
                    .addOnFailureListener(e -> {
                        callback.onError("Failed to update game: " + e.getMessage());
                        Log.e(TAG, "Game update failed", e);
                    });
            } else {
                // Create new document
                gamesRef.add(gameData)
                    .addOnSuccessListener(documentReference -> {
                        String firebaseId = documentReference.getId();
                        // Update SQLite with Firebase ID
                        game.setFirebaseId(firebaseId);
                        game.setSyncStatus("synced");
                        game.setLastSyncTimestamp(System.currentTimeMillis());
                        game.save(dbController.getDatabaseHelper());
                        
                        callback.onSuccess("Game created successfully");
                        Log.d(TAG, "Game created: " + game.getId() + " (Firebase ID: " + firebaseId + ")");
                    })
                    .addOnFailureListener(e -> {
                        callback.onError("Failed to create game: " + e.getMessage());
                        Log.e(TAG, "Game creation failed", e);
                    });
            }
        } catch (Exception e) {
            callback.onError("Upload game error: " + e.getMessage());
            Log.e(TAG, "Upload game error", e);
        }
    }
    
    /**
     * Download all games from Firestore
     */
    public void downloadGames(FirestoreCallback<List<Game>> callback) {
        try {
            getUserCollection(COLLECTION_GAMES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Game> games = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Game game = mapToGame(doc.getData(), doc.getId());
                        if (game != null) {
                            games.add(game);
                        }
                    }
                    callback.onSuccess(games);
                    Log.d(TAG, "Downloaded " + games.size() + " games");
                })
                .addOnFailureListener(e -> {
                    callback.onError("Failed to download games: " + e.getMessage());
                    Log.e(TAG, "Games download failed", e);
                });
        } catch (Exception e) {
            callback.onError("Download games error: " + e.getMessage());
            Log.e(TAG, "Download games error", e);
        }
    }
    
    /**
     * Download games modified since timestamp (incremental sync)
     */
    public void downloadGamesModifiedSince(long lastSyncTimestamp, FirestoreCallback<List<Game>> callback) {
        try {
            getUserCollection(COLLECTION_GAMES)
                .whereGreaterThan("lastSyncTimestamp", lastSyncTimestamp)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Game> games = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Game game = mapToGame(doc.getData(), doc.getId());
                        if (game != null) {
                            games.add(game);
                        }
                    }
                    callback.onSuccess(games);
                    Log.d(TAG, "Downloaded " + games.size() + " games modified since " + lastSyncTimestamp);
                })
                .addOnFailureListener(e -> {
                    callback.onError("Failed to download modified games: " + e.getMessage());
                    Log.e(TAG, "Modified games download failed", e);
                });
        } catch (Exception e) {
            callback.onError("Download modified games error: " + e.getMessage());
            Log.e(TAG, "Download modified games error", e);
        }
    }
    
    // ===== BATCH OPERATIONS =====
    
    /**
     * Upload multiple records in a single batch operation for efficiency
     * Enhanced with intelligent batching and error recovery
     */
    public void batchUpload(List<Team> teams, List<Game> games, List<Event> events, BatchCallback callback) {
        try {
            // Firestore has a 500 operation limit per batch
            int totalOperations = teams.size() + games.size() + events.size();
            
            if (totalOperations == 0) {
                callback.onBatchSuccess(0);
                return;
            }
            
            // If over 500 operations, split into multiple batches
            if (totalOperations > 450) { // Leave some margin
                performMultipleBatches(teams, games, events, callback);
                return;
            }
            
            // Single batch operation
            performSingleBatch(teams, games, events, callback);
            
        } catch (Exception e) {
            callback.onBatchError("Batch upload error: " + e.getMessage());
            Log.e(TAG, "Batch upload error", e);
        }
    }
    
    /**
     * Perform single batch upload (under 450 operations)
     */
    private void performSingleBatch(List<Team> teams, List<Game> games, List<Event> events, BatchCallback callback) {
        WriteBatch batch = firestore.batch();
        int operationCount = 0;
        
        // Add teams to batch
        CollectionReference teamsRef = getUserCollection(COLLECTION_TEAMS);
        for (Team team : teams) {
            Map<String, Object> teamData = teamToMap(team);
            if (team.getFirebaseId() != null) {
                batch.set(teamsRef.document(team.getFirebaseId()), teamData);
            } else {
                DocumentReference newDoc = teamsRef.document();
                batch.set(newDoc, teamData);
                team.setFirebaseId(newDoc.getId());
            }
            operationCount++;
        }
        
        // Add games to batch
        CollectionReference gamesRef = getUserCollection(COLLECTION_GAMES);
        for (Game game : games) {
            Map<String, Object> gameData = gameToMap(game);
            if (game.getFirebaseId() != null) {
                batch.set(gamesRef.document(game.getFirebaseId()), gameData);
            } else {
                DocumentReference newDoc = gamesRef.document();
                batch.set(newDoc, gameData);
                game.setFirebaseId(newDoc.getId());
            }
            operationCount++;
        }
        
        // Add events to batch
        CollectionReference eventsRef = getUserCollection(COLLECTION_EVENTS);
        for (Event event : events) {
            Map<String, Object> eventData = eventToMap(event);
            if (event.getFirebaseId() != null) {
                batch.set(eventsRef.document(event.getFirebaseId()), eventData);
            } else {
                DocumentReference newDoc = eventsRef.document();
                batch.set(newDoc, eventData);
                event.setFirebaseId(newDoc.getId());
            }
            operationCount++;
        }
        
        // Commit batch
        final int finalOperationCount = operationCount;
        batch.commit()
            .addOnSuccessListener(aVoid -> {
                // Update SQLite sync status for all records
                updateSyncStatusAfterBatch(teams, games, events);
                callback.onBatchSuccess(finalOperationCount);
                Log.d(TAG, "Single batch upload successful: " + finalOperationCount + " operations");
            })
            .addOnFailureListener(e -> {
                callback.onBatchError("Batch upload failed: " + e.getMessage());
                Log.e(TAG, "Single batch upload failed", e);
            });
    }
    
    /**
     * Perform multiple batch uploads for large datasets
     */
    private void performMultipleBatches(List<Team> teams, List<Game> games, List<Event> events, BatchCallback callback) {
        // Split into chunks of 400 operations each
        int chunkSize = 400;
        int totalOperations = teams.size() + games.size() + events.size();
        int batchCount = (totalOperations + chunkSize - 1) / chunkSize; // Ceiling division
        
        Log.d(TAG, "Large dataset detected: " + totalOperations + " operations, splitting into " + batchCount + " batches");
        
        performBatchChunk(teams, games, events, 0, chunkSize, batchCount, 0, callback);
    }
    
    /**
     * Recursively perform batch chunks
     */
    private void performBatchChunk(List<Team> teams, List<Game> games, List<Event> events, 
                                 int startIndex, int chunkSize, int totalBatches, 
                                 int currentBatch, BatchCallback callback) {
        
        // Create lists for this chunk
        List<Team> chunkTeams = getChunk(teams, startIndex, chunkSize);
        List<Game> chunkGames = getChunk(games, startIndex - teams.size(), chunkSize - chunkTeams.size());
        List<Event> chunkEvents = getChunk(events, startIndex - teams.size() - games.size(), 
                                         chunkSize - chunkTeams.size() - chunkGames.size());
        
        // Perform single batch for this chunk
        performSingleBatch(chunkTeams, chunkGames, chunkEvents, new BatchCallback() {
            @Override
            public void onBatchSuccess(int operationsCount) {
                int nextBatch = currentBatch + 1;
                if (nextBatch < totalBatches) {
                    // Continue with next batch
                    performBatchChunk(teams, games, events, startIndex + chunkSize, 
                                    chunkSize, totalBatches, nextBatch, callback);
                } else {
                    // All batches complete
                    int totalOperations = teams.size() + games.size() + events.size();
                    callback.onBatchSuccess(totalOperations);
                    Log.d(TAG, "Multiple batch upload successful: " + totalOperations + " operations in " + totalBatches + " batches");
                }
            }

            @Override
            public void onBatchError(String errorMessage) {
                callback.onBatchError("Batch " + (currentBatch + 1) + " failed: " + errorMessage);
            }
        });
    }
    
    /**
     * Get chunk of list for batch processing
     */
    private <T> List<T> getChunk(List<T> list, int startIndex, int maxSize) {
        if (startIndex < 0 || startIndex >= list.size() || maxSize <= 0) {
            return new ArrayList<>();
        }
        
        int endIndex = Math.min(startIndex + maxSize, list.size());
        return list.subList(startIndex, endIndex);
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Convert Team object to Firestore map
     */
    private Map<String, Object> teamToMap(Team team) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", team.getName());
        map.put("createdAt", team.getCreatedAt());
        map.put("updatedAt", team.getUpdatedAt());
        map.put("lastSyncTimestamp", System.currentTimeMillis());
        return map;
    }
    
    /**
     * Convert Firestore map to Team object
     */
    private Team mapToTeam(Map<String, Object> data, String firebaseId) {
        if (data == null) return null;
        
        Team team = new Team();
        team.setFirebaseId(firebaseId);
        team.setName((String) data.get("name"));
        team.setCreatedAt((Long) data.get("createdAt"));
        team.setUpdatedAt((Long) data.get("updatedAt"));
        team.setSyncStatus("synced");
        team.setLastSyncTimestamp((Long) data.get("lastSyncTimestamp"));
        return team;
    }
    
    /**
     * Convert Game object to Firestore map
     */
    private Map<String, Object> gameToMap(Game game) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", game.getDate());
        map.put("time", game.getTime());
        map.put("homeTeamId", game.getHomeTeamId());
        map.put("awayTeamId", game.getAwayTeamId());
        map.put("status", game.getStatus());
        map.put("homeScore", game.getHomeScore());
        map.put("awayScore", game.getAwayScore());
        map.put("currentQuarter", game.getCurrentQuarter());
        map.put("gameClockSeconds", game.getGameClockSeconds());
        map.put("isClockRunning", game.isClockRunning());
        map.put("createdAt", game.getCreatedAt());
        map.put("updatedAt", game.getUpdatedAt());
        map.put("lastSyncTimestamp", System.currentTimeMillis());
        return map;
    }
    
    /**
     * Convert Firestore map to Game object
     */
    private Game mapToGame(Map<String, Object> data, String firebaseId) {
        if (data == null) return null;
        
        Game game = new Game();
        game.setFirebaseId(firebaseId);
        game.setDate((String) data.get("date"));
        game.setTime((String) data.get("time"));
        game.setHomeTeamId(((Long) data.get("homeTeamId")).intValue());
        game.setAwayTeamId(((Long) data.get("awayTeamId")).intValue());
        game.setStatus((String) data.get("status"));
        game.setHomeScore(((Long) data.get("homeScore")).intValue());
        game.setAwayScore(((Long) data.get("awayScore")).intValue());
        game.setCurrentQuarter(((Long) data.get("currentQuarter")).intValue());
        game.setGameClockSeconds(((Long) data.get("gameClockSeconds")).intValue());
        game.setClockRunning((Boolean) data.get("isClockRunning"));
        game.setCreatedAt((Long) data.get("createdAt"));
        game.setUpdatedAt((Long) data.get("updatedAt"));
        game.setSyncStatus("synced");
        game.setLastSyncTimestamp((Long) data.get("lastSyncTimestamp"));
        return game;
    }
    
    /**
     * Convert Event object to Firestore map
     */
    private Map<String, Object> eventToMap(Event event) {
        Map<String, Object> map = new HashMap<>();
        map.put("gameId", event.getGameId());
        map.put("playerId", event.getPlayerId());
        map.put("teamSide", event.getTeamSide());
        map.put("quarter", event.getQuarter());
        map.put("gameTimeSeconds", event.getGameTimeSeconds());
        map.put("eventType", event.getEventType());
        map.put("eventSequence", event.getEventSequence());
        map.put("createdAt", event.getCreatedAt());
        map.put("updatedAt", event.getUpdatedAt());
        map.put("lastSyncTimestamp", System.currentTimeMillis());
        return map;
    }
    
    /**
     * Update sync status for all records after successful batch operation
     */
    private void updateSyncStatusAfterBatch(List<Team> teams, List<Game> games, List<Event> events) {
        try {
            // Update teams
            for (Team team : teams) {
                team.setSyncStatus("synced");
                team.setLastSyncTimestamp(System.currentTimeMillis());
                team.save(dbController.getDatabaseHelper());
            }
            
            // Update games
            for (Game game : games) {
                game.setSyncStatus("synced");
                game.setLastSyncTimestamp(System.currentTimeMillis());
                game.save(dbController.getDatabaseHelper());
            }
            
            // Update events
            for (Event event : events) {
                event.setSyncStatus("synced");
                event.setLastSyncTimestamp(System.currentTimeMillis());
                event.save(dbController.getDatabaseHelper());
            }
            
            Log.d(TAG, "Updated sync status for all batch records");
        } catch (Exception e) {
            Log.e(TAG, "Error updating sync status after batch", e);
        }
    }
    
    /**
     * Check if user is authenticated for Firestore operations
     */
    public boolean isUserAuthenticated() {
        return authController.isUserAuthenticated();
    }
    
    /**
     * Get current user UID for Firestore operations
     */
    public String getCurrentUserUid() {
        return authController.getCurrentUserUid();
    }
}
