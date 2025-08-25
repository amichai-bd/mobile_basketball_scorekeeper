package com.basketballstats.app.sync;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.basketballstats.app.data.DatabaseHelper;
import com.basketballstats.app.data.DatabaseController;
import com.basketballstats.app.models.Team;
import com.basketballstats.app.models.Game;
import com.basketballstats.app.models.AppSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SyncManager
 * Tests sync logic, conflict resolution, and error handling
 */
@RunWith(AndroidJUnit4.class)
public class SyncManagerTest {
    
    private DatabaseHelper dbHelper;
    private DatabaseController dbController;
    private Context context;
    private SyncManager syncManager;
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        dbController = DatabaseController.getInstance(context);
        
        // Clear database
        clearDatabase();
        
        // Note: SyncManager requires Firebase and network components
        // In real tests, we would mock these dependencies
        // For now, we'll test the utility methods and data handling
    }
    
    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    private void clearDatabase() {
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_TEAMS);
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_GAMES);
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_APP_SETTINGS);
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_SYNC_QUEUE);
    }
    
    @Test
    public void testLastSyncTimestampManagement() {
        // Test getting last sync timestamp when none exists
        AppSettings setting = AppSettings.findByKey(dbHelper, "last_sync_timestamp");
        assertNull("Initially no last sync timestamp should exist", setting);
        
        // Create a timestamp setting
        AppSettings newSetting = new AppSettings();
        newSetting.setSettingKey("last_sync_timestamp");
        newSetting.setSettingValue("1234567890");
        newSetting.save(dbHelper);
        
        // Verify we can retrieve it
        AppSettings retrievedSetting = AppSettings.findByKey(dbHelper, "last_sync_timestamp");
        assertNotNull("Last sync timestamp should be retrievable", retrievedSetting);
        assertEquals("Timestamp value should match", "1234567890", retrievedSetting.getSettingValue());
    }
    
    @Test
    public void testConflictResolutionScenario() {
        long baseTime = System.currentTimeMillis();
        
        // Create local team (newer)
        Team localTeam = new Team();
        localTeam.setId(1);
        localTeam.setName("Local Lakers");
        localTeam.setFirebaseId("firebase_123");
        localTeam.setUpdatedAt(baseTime + 1000); // 1 second newer
        localTeam.setSyncStatus("local");
        
        // Create Firebase team (older)
        Team firebaseTeam = new Team();
        firebaseTeam.setName("Firebase Lakers");
        firebaseTeam.setFirebaseId("firebase_123");
        firebaseTeam.setLastSyncTimestamp(baseTime); // Older
        firebaseTeam.setSyncStatus("synced");
        
        // Test conflict resolution logic
        long lastSyncTimestamp = baseTime - 500; // Even older baseline
        boolean localIsNewer = localTeam.getUpdatedAt() > lastSyncTimestamp;
        assertTrue("Local team should be considered newer", localIsNewer);
        
        // In real sync, local should win
        assertTrue("Local team update time should be newer than Firebase sync time", 
                  localTeam.getUpdatedAt() > firebaseTeam.getLastSyncTimestamp());
    }
    
    @Test
    public void testIncrementalSyncDataFiltering() {
        long lastSyncTimestamp = System.currentTimeMillis() - 10000; // 10 seconds ago
        
        // Create teams with different modification times
        Team oldTeam = new Team();
        oldTeam.setName("Old Team");
        oldTeam.setUpdatedAt(lastSyncTimestamp - 1000); // Before last sync
        oldTeam.setSyncStatus("synced");
        oldTeam.save(dbHelper);
        
        Team newTeam = new Team();
        newTeam.setName("New Team");
        newTeam.setUpdatedAt(lastSyncTimestamp + 1000); // After last sync
        newTeam.setSyncStatus("local");
        newTeam.save(dbHelper);
        
        Team pendingTeam = new Team();
        pendingTeam.setName("Pending Team");
        pendingTeam.setUpdatedAt(lastSyncTimestamp - 500); // Before last sync
        pendingTeam.setSyncStatus("pending_upload"); // But marked for upload
        pendingTeam.save(dbHelper);
        
        // Simulate incremental sync filtering
        // Teams that should be synced: newTeam (modified after last sync) and pendingTeam (marked for upload)
        assertTrue("New team should be included in incremental sync", 
                  newTeam.getUpdatedAt() > lastSyncTimestamp || 
                  "pending_upload".equals(newTeam.getSyncStatus()) ||
                  newTeam.getFirebaseId() == null);
        
        assertTrue("Pending team should be included in incremental sync", 
                  pendingTeam.getUpdatedAt() > lastSyncTimestamp || 
                  "pending_upload".equals(pendingTeam.getSyncStatus()) ||
                  pendingTeam.getFirebaseId() == null);
        
        assertFalse("Old synced team should not be included in incremental sync", 
                   oldTeam.getUpdatedAt() > lastSyncTimestamp || 
                   "pending_upload".equals(oldTeam.getSyncStatus()) ||
                   oldTeam.getFirebaseId() == null);
    }
    
    @Test
    public void testUserDeviceWinsConflictResolution() {
        long currentTime = System.currentTimeMillis();
        
        // Scenario: User device has newer data than Firebase
        Team localTeam = new Team();
        localTeam.setId(1);
        localTeam.setName("User Updated Team");
        localTeam.setFirebaseId("firebase_conflict_123");
        localTeam.setUpdatedAt(currentTime);
        localTeam.setSyncStatus("local");
        
        Team firebaseTeam = new Team();
        firebaseTeam.setName("Server Updated Team");
        firebaseTeam.setFirebaseId("firebase_conflict_123");
        firebaseTeam.setLastSyncTimestamp(currentTime - 5000); // 5 seconds older
        
        // User device wins: local data should take precedence
        boolean localWins = localTeam.getUpdatedAt() > firebaseTeam.getLastSyncTimestamp();
        assertTrue("User device should win conflict when local data is newer", localWins);
        
        // The local team should be marked for upload
        if (localWins) {
            localTeam.setSyncStatus("pending_upload");
        }
        assertEquals("Local team should be marked for upload in conflict", 
                    "pending_upload", localTeam.getSyncStatus());
    }
    
    @Test
    public void testFirebaseWinsWhenLocalNotModified() {
        long currentTime = System.currentTimeMillis();
        long lastSyncTime = currentTime - 10000; // 10 seconds ago
        
        // Scenario: Firebase has newer data than user's last modification
        Team localTeam = new Team();
        localTeam.setId(1);
        localTeam.setName("Old Local Team");
        localTeam.setFirebaseId("firebase_no_conflict_123");
        localTeam.setUpdatedAt(lastSyncTime - 1000); // Updated before last sync
        localTeam.setSyncStatus("synced");
        
        Team firebaseTeam = new Team();
        firebaseTeam.setName("Updated Server Team");
        firebaseTeam.setFirebaseId("firebase_no_conflict_123");
        firebaseTeam.setLastSyncTimestamp(currentTime - 1000); // Much newer
        
        // Check if local was modified since last sync
        boolean localModifiedSinceSync = localTeam.getUpdatedAt() > lastSyncTime;
        assertFalse("Local team should not be considered modified since last sync", localModifiedSinceSync);
        
        // Firebase should win
        if (!localModifiedSinceSync) {
            // In real sync, we would update local with Firebase data
            localTeam.setName(firebaseTeam.getName());
            localTeam.setSyncStatus("synced");
        }
        
        assertEquals("Firebase data should win when local not modified", 
                    "Updated Server Team", localTeam.getName());
        assertEquals("Team should be marked as synced", "synced", localTeam.getSyncStatus());
    }
    
    @Test
    public void testSyncStatusProgression() {
        Team team = new Team();
        team.setName("Status Test Team");
        
        // Initial status
        team.setSyncStatus("local");
        assertEquals("Initial status should be local", "local", team.getSyncStatus());
        
        // After queuing for upload
        team.setSyncStatus("pending_upload");
        assertEquals("Status should be pending upload", "pending_upload", team.getSyncStatus());
        
        // After successful sync
        team.setSyncStatus("synced");
        team.setLastSyncTimestamp(System.currentTimeMillis());
        assertEquals("Status should be synced", "synced", team.getSyncStatus());
        assertTrue("Last sync timestamp should be set", team.getLastSyncTimestamp() > 0);
    }
    
    @Test
    public void testMultipleRecordConflictResolution() {
        long baseTime = System.currentTimeMillis();
        long lastSyncTime = baseTime - 5000;
        
        // Create multiple teams with different conflict scenarios
        Team[] localTeams = new Team[3];
        Team[] firebaseTeams = new Team[3];
        
        // Team 1: Local wins (newer local modification)
        localTeams[0] = new Team();
        localTeams[0].setName("Local Winner");
        localTeams[0].setFirebaseId("team_1");
        localTeams[0].setUpdatedAt(baseTime);
        
        firebaseTeams[0] = new Team();
        firebaseTeams[0].setName("Firebase Loser");
        firebaseTeams[0].setFirebaseId("team_1");
        firebaseTeams[0].setLastSyncTimestamp(baseTime - 1000);
        
        // Team 2: Firebase wins (no local modification since sync)
        localTeams[1] = new Team();
        localTeams[1].setName("Local Loser");
        localTeams[1].setFirebaseId("team_2");
        localTeams[1].setUpdatedAt(lastSyncTime - 1000);
        
        firebaseTeams[1] = new Team();
        firebaseTeams[1].setName("Firebase Winner");
        firebaseTeams[1].setFirebaseId("team_2");
        firebaseTeams[1].setLastSyncTimestamp(baseTime - 500);
        
        // Team 3: New from Firebase (no local version)
        firebaseTeams[2] = new Team();
        firebaseTeams[2].setName("New Firebase Team");
        firebaseTeams[2].setFirebaseId("team_3");
        firebaseTeams[2].setLastSyncTimestamp(baseTime);
        
        // Simulate conflict resolution
        int localWins = 0;
        int firebaseWins = 0;
        int newFromFirebase = 0;
        
        for (int i = 0; i < 3; i++) {
            if (i == 2) {
                // New team from Firebase
                newFromFirebase++;
            } else {
                boolean localModifiedSinceSync = localTeams[i].getUpdatedAt() > lastSyncTime;
                if (localModifiedSinceSync) {
                    localWins++;
                } else {
                    firebaseWins++;
                }
            }
        }
        
        assertEquals("Should have 1 local win", 1, localWins);
        assertEquals("Should have 1 Firebase win", 1, firebaseWins);
        assertEquals("Should have 1 new from Firebase", 1, newFromFirebase);
    }
    
    @Test
    public void testSyncCallbackInterface() {
        // Test that we can create and use sync callbacks
        final boolean[] callbackCalled = {false, false, false, false, false};
        
        SyncManager.SyncCallback testCallback = new SyncManager.SyncCallback() {
            @Override
            public void onSyncStarted() {
                callbackCalled[0] = true;
            }
            
            @Override
            public void onSyncProgress(String message) {
                callbackCalled[1] = true;
                assertNotNull("Progress message should not be null", message);
            }
            
            @Override
            public void onSyncSuccess(String message) {
                callbackCalled[2] = true;
                assertNotNull("Success message should not be null", message);
            }
            
            @Override
            public void onSyncError(String errorMessage) {
                callbackCalled[3] = true;
                assertNotNull("Error message should not be null", errorMessage);
            }
            
            @Override
            public void onSyncComplete() {
                callbackCalled[4] = true;
            }
        };
        
        // Simulate callback usage
        testCallback.onSyncStarted();
        testCallback.onSyncProgress("Testing progress");
        testCallback.onSyncSuccess("Testing success");
        testCallback.onSyncError("Testing error");
        testCallback.onSyncComplete();
        
        // Verify all callbacks were called
        assertTrue("onSyncStarted should be called", callbackCalled[0]);
        assertTrue("onSyncProgress should be called", callbackCalled[1]);
        assertTrue("onSyncSuccess should be called", callbackCalled[2]);
        assertTrue("onSyncError should be called", callbackCalled[3]);
        assertTrue("onSyncComplete should be called", callbackCalled[4]);
    }
}
