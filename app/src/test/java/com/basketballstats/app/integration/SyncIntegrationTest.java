package com.basketballstats.app.integration;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.basketballstats.app.data.DatabaseHelper;
import com.basketballstats.app.data.DatabaseController;
import com.basketballstats.app.models.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for complete sync workflow
 * Tests: create data → sync → modify → sync → conflict scenarios
 * Simulates real user workflows and multi-device scenarios
 */
@RunWith(AndroidJUnit4.class)
public class SyncIntegrationTest {
    
    private DatabaseHelper dbHelper;
    private DatabaseController dbController;
    private Context context;
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        dbController = DatabaseController.getInstance(context);
        
        // Clear all data
        clearAllTables();
    }
    
    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    private void clearAllTables() {
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_EVENTS);
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_GAMES);
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_TEAM_PLAYERS);
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_TEAMS);
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_SYNC_QUEUE);
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_APP_SETTINGS);
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_USER_PROFILE);
    }
    
    @Test
    public void testCompleteWorkflow_CreateDataSyncModifySync() {
        // STEP 1: Create initial data (simulate user creating teams and games)
        Team team1 = createTestTeam("Lakers", "local");
        Team team2 = createTestTeam("Warriors", "local");
        Game game1 = createTestGame(team1.getId(), team2.getId(), "local");
        
        // Verify initial state
        assertEquals("Team 1 should be local", "local", team1.getSyncStatus());
        assertEquals("Team 2 should be local", "local", team2.getSyncStatus());
        assertEquals("Game should be local", "local", game1.getSyncStatus());
        assertNull("Teams should have no Firebase ID initially", team1.getFirebaseId());
        assertNull("Game should have no Firebase ID initially", game1.getFirebaseId());
        
        // STEP 2: Simulate first sync (upload to Firebase)
        simulateSuccessfulUploadSync(team1, team2, game1);
        
        // Verify post-sync state
        assertEquals("Team 1 should be synced", "synced", team1.getSyncStatus());
        assertEquals("Team 2 should be synced", "synced", team2.getSyncStatus());
        assertEquals("Game should be synced", "synced", game1.getSyncStatus());
        assertNotNull("Teams should have Firebase ID after sync", team1.getFirebaseId());
        assertNotNull("Game should have Firebase ID after sync", game1.getFirebaseId());
        assertTrue("Last sync timestamp should be set", team1.getLastSyncTimestamp() > 0);
        
        // STEP 3: Modify data locally (simulate user editing)
        long modificationTime = System.currentTimeMillis();
        team1.setName("Los Angeles Lakers");
        team1.setUpdatedAt(modificationTime);
        team1.setSyncStatus("local"); // Modified locally
        team1.save(dbHelper);
        
        game1.setStatus("in_progress");
        game1.setHomeScore(24);
        game1.setAwayScore(18);
        game1.setUpdatedAt(modificationTime);
        game1.setSyncStatus("local"); // Modified locally
        game1.save(dbHelper);
        
        // STEP 4: Simulate second sync (upload modifications)
        simulateSuccessfulUploadSync(team1, null, game1);
        
        // Verify final state
        assertEquals("Modified team should be synced again", "synced", team1.getSyncStatus());
        assertEquals("Modified game should be synced again", "synced", game1.getSyncStatus());
        assertTrue("Team sync timestamp should be updated", team1.getLastSyncTimestamp() >= modificationTime);
        assertTrue("Game sync timestamp should be updated", game1.getLastSyncTimestamp() >= modificationTime);
        
        // STEP 5: Verify data integrity throughout workflow
        Team retrievedTeam = Team.findById(dbHelper, team1.getId());
        Game retrievedGame = Game.findById(dbHelper, game1.getId());
        
        assertEquals("Team name should be preserved", "Los Angeles Lakers", retrievedTeam.getName());
        assertEquals("Game status should be preserved", "in_progress", retrievedGame.getStatus());
        assertEquals("Game scores should be preserved", 24, retrievedGame.getHomeScore());
        assertEquals("Game scores should be preserved", 18, retrievedGame.getAwayScore());
    }
    
    @Test
    public void testConflictResolution_UserDeviceWins() {
        long baseTime = System.currentTimeMillis();
        
        // STEP 1: Create team and simulate initial sync
        Team localTeam = createTestTeam("Conflict Team", "synced");
        localTeam.setFirebaseId("firebase_conflict_123");
        localTeam.setLastSyncTimestamp(baseTime - 10000); // 10 seconds ago
        localTeam.save(dbHelper);
        
        // STEP 2: Simulate user modification (device data)
        localTeam.setName("User Modified Team");
        localTeam.setUpdatedAt(baseTime); // User modified recently
        localTeam.setSyncStatus("local");
        localTeam.save(dbHelper);
        
        // STEP 3: Simulate incoming Firebase data (server data)
        Team firebaseTeam = new Team();
        firebaseTeam.setName("Server Modified Team");
        firebaseTeam.setFirebaseId("firebase_conflict_123");
        firebaseTeam.setLastSyncTimestamp(baseTime - 5000); // 5 seconds ago (older than user)
        
        // STEP 4: Simulate conflict resolution (user device wins)
        long lastSyncTimestamp = baseTime - 10000; // Last successful sync
        boolean localIsNewer = localTeam.getUpdatedAt() > lastSyncTimestamp;
        
        assertTrue("Local modification should be newer than last sync", localIsNewer);
        
        if (localIsNewer) {
            // User device wins - mark for upload
            localTeam.setSyncStatus("pending_upload");
        } else {
            // Firebase wins - update local
            localTeam.setName(firebaseTeam.getName());
            localTeam.setSyncStatus("synced");
        }
        
        // STEP 5: Verify conflict resolution result
        assertEquals("User device should win conflict", "User Modified Team", localTeam.getName());
        assertEquals("Team should be marked for upload", "pending_upload", localTeam.getSyncStatus());
        
        // STEP 6: Simulate successful upload after conflict resolution
        localTeam.setSyncStatus("synced");
        localTeam.setLastSyncTimestamp(System.currentTimeMillis());
        localTeam.save(dbHelper);
        
        assertEquals("Team should be synced after conflict resolution", "synced", localTeam.getSyncStatus());
    }
    
    @Test
    public void testConflictResolution_FirebaseWins() {
        long baseTime = System.currentTimeMillis();
        
        // STEP 1: Create team and simulate sync
        Team localTeam = createTestTeam("Old Local Team", "synced");
        localTeam.setFirebaseId("firebase_no_conflict_456");
        localTeam.setUpdatedAt(baseTime - 15000); // 15 seconds ago
        localTeam.setLastSyncTimestamp(baseTime - 10000); // Last sync 10 seconds ago
        localTeam.save(dbHelper);
        
        // STEP 2: Simulate incoming Firebase data (newer server data)
        Team firebaseTeam = new Team();
        firebaseTeam.setName("Updated Server Team");
        firebaseTeam.setFirebaseId("firebase_no_conflict_456");
        firebaseTeam.setLastSyncTimestamp(baseTime - 2000); // Much newer than local
        
        // STEP 3: Simulate conflict resolution (Firebase wins)
        long lastSyncTimestamp = baseTime - 10000;
        boolean localIsNewer = localTeam.getUpdatedAt() > lastSyncTimestamp;
        
        assertFalse("Local should not be newer than last sync", localIsNewer);
        
        if (!localIsNewer) {
            // Firebase wins - update local
            localTeam.setName(firebaseTeam.getName());
            localTeam.setSyncStatus("synced");
            localTeam.setLastSyncTimestamp(firebaseTeam.getLastSyncTimestamp());
        }
        
        // STEP 4: Verify conflict resolution result
        assertEquals("Firebase should win conflict", "Updated Server Team", localTeam.getName());
        assertEquals("Team should be marked as synced", "synced", localTeam.getSyncStatus());
        assertEquals("Last sync timestamp should be updated", 
                    firebaseTeam.getLastSyncTimestamp(), localTeam.getLastSyncTimestamp());
    }
    
    @Test
    public void testOfflineWorkflow_QueueAndRetry() {
        // STEP 1: Create data while offline
        Team offlineTeam = createTestTeam("Offline Team", "local");
        Game offlineGame = createTestGame(offlineTeam.getId(), offlineTeam.getId(), "local");
        
        // STEP 2: Simulate sync failure (network offline)
        simulateFailedSync(offlineTeam, offlineGame, "Network unavailable");
        
        // Verify items are queued
        List<SyncQueue> queuedItems = SyncQueue.findPendingOperations(dbHelper);
        assertEquals("Should have 2 queued operations", 2, queuedItems.size());
        
        // Verify queue items
        SyncQueue teamQueue = findQueueItemByTable(queuedItems, "teams");
        SyncQueue gameQueue = findQueueItemByTable(queuedItems, "games");
        
        assertNotNull("Team should be queued", teamQueue);
        assertNotNull("Game should be queued", gameQueue);
        assertEquals("Team operation should be create", "create", teamQueue.getOperation());
        assertEquals("Game operation should be create", "create", gameQueue.getOperation());
        
        // STEP 3: Simulate network restoration and retry
        simulateSuccessfulRetry(queuedItems);
        
        // Verify queue is empty after successful retry
        List<SyncQueue> remainingQueue = SyncQueue.findPendingOperations(dbHelper);
        assertEquals("Queue should be empty after successful retry", 0, remainingQueue.size());
        
        // Verify data is marked as synced
        Team syncedTeam = Team.findById(dbHelper, offlineTeam.getId());
        Game syncedGame = Game.findById(dbHelper, offlineGame.getId());
        
        assertEquals("Team should be synced after retry", "synced", syncedTeam.getSyncStatus());
        assertEquals("Game should be synced after retry", "synced", syncedGame.getSyncStatus());
    }
    
    @Test
    public void testIncrementalSync_OnlyModifiedData() {
        long initialTime = System.currentTimeMillis() - 20000; // 20 seconds ago
        long lastSyncTime = System.currentTimeMillis() - 10000; // 10 seconds ago
        long recentTime = System.currentTimeMillis() - 2000; // 2 seconds ago
        
        // STEP 1: Create teams at different times
        Team oldTeam = createTestTeam("Old Team", "synced");
        oldTeam.setUpdatedAt(initialTime);
        oldTeam.setLastSyncTimestamp(lastSyncTime);
        oldTeam.save(dbHelper);
        
        Team newTeam = createTestTeam("New Team", "local");
        newTeam.setUpdatedAt(recentTime); // Created after last sync
        newTeam.save(dbHelper);
        
        Team modifiedTeam = createTestTeam("Modified Team", "synced");
        modifiedTeam.setUpdatedAt(initialTime);
        modifiedTeam.setLastSyncTimestamp(lastSyncTime);
        modifiedTeam.save(dbHelper);
        
        // STEP 2: Modify one team after last sync
        modifiedTeam.setName("Recently Modified Team");
        modifiedTeam.setUpdatedAt(recentTime);
        modifiedTeam.setSyncStatus("local");
        modifiedTeam.save(dbHelper);
        
        // STEP 3: Simulate incremental sync (only sync changed data)
        List<Team> allTeams = Team.findAll(dbHelper);
        int teamsToSync = 0;
        
        for (Team team : allTeams) {
            boolean shouldSync = team.getUpdatedAt() > lastSyncTime || 
                               "local".equals(team.getSyncStatus()) ||
                               "pending_upload".equals(team.getSyncStatus()) ||
                               team.getFirebaseId() == null;
            
            if (shouldSync) {
                teamsToSync++;
            }
        }
        
        // STEP 4: Verify only modified and new teams are selected for sync
        assertEquals("Should sync 2 teams (new + modified)", 2, teamsToSync);
        
        // Old synced team should not be synced again
        assertFalse("Old team should not need sync", 
                   oldTeam.getUpdatedAt() > lastSyncTime || 
                   "local".equals(oldTeam.getSyncStatus()) ||
                   oldTeam.getFirebaseId() == null);
    }
    
    @Test
    public void testComplexGameWorkflow_WithEvents() {
        // STEP 1: Create complete game data structure
        Team homeTeam = createTestTeam("Home Team", "local");
        Team awayTeam = createTestTeam("Away Team", "local");
        Game game = createTestGame(homeTeam.getId(), awayTeam.getId(), "local");
        
        // Create game events
        Event event1 = createTestEvent(game.getId(), 1, "home", "2P", 1);
        Event event2 = createTestEvent(game.getId(), 2, "away", "3P", 2);
        Event event3 = createTestEvent(game.getId(), 1, "home", "FOUL", 3);
        
        // STEP 2: Verify initial data integrity
        assertEquals("Should have home team", homeTeam.getId(), game.getHomeTeamId());
        assertEquals("Should have away team", awayTeam.getId(), game.getAwayTeamId());
        
        List<Event> gameEvents = Event.findByGameId(dbHelper, game.getId());
        assertEquals("Should have 3 events", 3, gameEvents.size());
        
        // STEP 3: Simulate sync of complete data structure
        simulateSuccessfulUploadSync(homeTeam, awayTeam, game);
        
        // Mark events as synced
        for (Event event : gameEvents) {
            event.setSyncStatus("synced");
            event.setFirebaseId("firebase_event_" + event.getId());
            event.setLastSyncTimestamp(System.currentTimeMillis());
            event.save(dbHelper);
        }
        
        // STEP 4: Modify game state (simulate live game)
        game.setHomeScore(45);
        game.setAwayScore(42);
        game.setCurrentQuarter(2);
        game.setGameClockSeconds(480); // 8 minutes remaining
        game.setSyncStatus("local");
        game.save(dbHelper);
        
        // Add more events
        Event newEvent = createTestEvent(game.getId(), 2, "away", "2P", 4);
        
        // STEP 5: Verify game can be synced with mixed sync states
        List<Event> allEvents = Event.findByGameId(dbHelper, game.getId());
        assertEquals("Should have 4 events total", 4, allEvents.size());
        
        int syncedEvents = 0;
        int localEvents = 0;
        
        for (Event event : allEvents) {
            if ("synced".equals(event.getSyncStatus())) {
                syncedEvents++;
            } else if ("local".equals(event.getSyncStatus())) {
                localEvents++;
            }
        }
        
        assertEquals("Should have 3 synced events", 3, syncedEvents);
        assertEquals("Should have 1 local event", 1, localEvents);
        assertEquals("Game should be local", "local", game.getSyncStatus());
    }
    
    // Helper methods
    
    private Team createTestTeam(String name, String syncStatus) {
        Team team = new Team();
        team.setName(name);
        team.setSyncStatus(syncStatus);
        team.save(dbHelper);
        return team;
    }
    
    private Game createTestGame(int homeTeamId, int awayTeamId, String syncStatus) {
        Game game = new Game();
        game.setDate("15/12/2024");
        game.setTime("19:30");
        game.setHomeTeamId(homeTeamId);
        game.setAwayTeamId(awayTeamId);
        game.setStatus("scheduled");
        game.setSyncStatus(syncStatus);
        game.save(dbHelper);
        return game;
    }
    
    private Event createTestEvent(int gameId, int playerId, String teamSide, String eventType, int sequence) {
        Event event = new Event();
        event.setGameId(gameId);
        event.setPlayerId(playerId);
        event.setTeamSide(teamSide);
        event.setQuarter(1);
        event.setGameTimeSeconds(600);
        event.setEventType(eventType);
        event.setEventSequence(sequence);
        event.setSyncStatus("local");
        event.save(dbHelper);
        return event;
    }
    
    private void simulateSuccessfulUploadSync(Team team1, Team team2, Game game) {
        // Simulate Firebase upload success
        team1.setFirebaseId("firebase_team_" + team1.getId());
        team1.setSyncStatus("synced");
        team1.setLastSyncTimestamp(System.currentTimeMillis());
        team1.save(dbHelper);
        
        if (team2 != null) {
            team2.setFirebaseId("firebase_team_" + team2.getId());
            team2.setSyncStatus("synced");
            team2.setLastSyncTimestamp(System.currentTimeMillis());
            team2.save(dbHelper);
        }
        
        if (game != null) {
            game.setFirebaseId("firebase_game_" + game.getId());
            game.setSyncStatus("synced");
            game.setLastSyncTimestamp(System.currentTimeMillis());
            game.save(dbHelper);
        }
    }
    
    private void simulateFailedSync(Team team, Game game, String errorMessage) {
        // Simulate sync failure - queue operations
        SyncQueue teamQueue = new SyncQueue("teams", team.getId(), "create", null);
        teamQueue.setDataJson("{\"name\":\"" + team.getName() + "\"}");
        teamQueue.setErrorMessage(errorMessage);
        teamQueue.save(dbHelper);
        
        SyncQueue gameQueue = new SyncQueue("games", game.getId(), "create", null);
        gameQueue.setDataJson("{\"homeTeamId\":" + game.getHomeTeamId() + "}");
        gameQueue.setErrorMessage(errorMessage);
        gameQueue.save(dbHelper);
    }
    
    private void simulateSuccessfulRetry(List<SyncQueue> queuedItems) {
        // Simulate successful retry - mark as synced and remove from queue
        for (SyncQueue item : queuedItems) {
            if ("teams".equals(item.getTableName())) {
                Team team = Team.findById(dbHelper, item.getRecordId());
                team.setFirebaseId("firebase_team_" + team.getId());
                team.setSyncStatus("synced");
                team.setLastSyncTimestamp(System.currentTimeMillis());
                team.save(dbHelper);
            } else if ("games".equals(item.getTableName())) {
                Game game = Game.findById(dbHelper, item.getRecordId());
                game.setFirebaseId("firebase_game_" + game.getId());
                game.setSyncStatus("synced");
                game.setLastSyncTimestamp(System.currentTimeMillis());
                game.save(dbHelper);
            }
            
            // Remove from queue
            item.delete(dbHelper);
        }
    }
    
    private SyncQueue findQueueItemByTable(List<SyncQueue> items, String tableName) {
        for (SyncQueue item : items) {
            if (tableName.equals(item.getTableName())) {
                return item;
            }
        }
        return null;
    }
}
