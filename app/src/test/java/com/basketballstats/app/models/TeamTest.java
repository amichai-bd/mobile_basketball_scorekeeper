package com.basketballstats.app.models;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.basketballstats.app.data.DatabaseHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for Team model SQLite operations
 * Tests CRUD operations, sync metadata, and data integrity
 */
@RunWith(AndroidJUnit4.class)
public class TeamTest {
    
    private DatabaseHelper dbHelper;
    private Context context;
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        // Clear any existing data
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_TEAMS);
    }
    
    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    @Test
    public void testCreateTeam() {
        // Create new team
        Team team = new Team();
        team.setName("Lakers");
        
        long result = team.save(dbHelper);
        assertTrue("Team creation should succeed", result > 0);
        assertTrue("Team ID should be assigned", team.getId() > 0);
        assertNotNull("Created timestamp should be set", team.getCreatedAt());
        assertNotNull("Updated timestamp should be set", team.getUpdatedAt());
        assertEquals("Sync status should default to local", "local", team.getSyncStatus());
    }
    
    @Test
    public void testReadTeam() {
        // Create and save team
        Team originalTeam = new Team();
        originalTeam.setName("Warriors");
        originalTeam.save(dbHelper);
        
        // Read team by ID
        Team retrievedTeam = Team.findById(dbHelper, originalTeam.getId());
        assertNotNull("Team should be found", retrievedTeam);
        assertEquals("Team name should match", "Warriors", retrievedTeam.getName());
        assertEquals("Team ID should match", originalTeam.getId(), retrievedTeam.getId());
    }
    
    @Test
    public void testUpdateTeam() {
        // Create team
        Team team = new Team();
        team.setName("Bulls");
        team.save(dbHelper);
        
        long originalUpdatedAt = team.getUpdatedAt();
        
        // Wait a moment to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        // Update team
        team.setName("Chicago Bulls");
        team.setSyncStatus("pending_upload");
        long updateResult = team.save(dbHelper);
        
        assertTrue("Update should succeed", updateResult > 0);
        assertTrue("Updated timestamp should change", team.getUpdatedAt() > originalUpdatedAt);
        assertEquals("Name should be updated", "Chicago Bulls", team.getName());
        assertEquals("Sync status should be updated", "pending_upload", team.getSyncStatus());
    }
    
    @Test
    public void testDeleteTeam() {
        // Create team
        Team team = new Team();
        team.setName("Heat");
        team.save(dbHelper);
        
        int teamId = team.getId();
        
        // Delete team
        boolean deleteResult = team.delete(dbHelper);
        assertTrue("Delete should succeed", deleteResult);
        
        // Verify team is deleted
        Team deletedTeam = Team.findById(dbHelper, teamId);
        assertNull("Team should be deleted", deletedTeam);
    }
    
    @Test
    public void testFindAllTeams() {
        // Create multiple teams
        Team team1 = new Team();
        team1.setName("Lakers");
        team1.save(dbHelper);
        
        Team team2 = new Team();
        team2.setName("Warriors");
        team2.save(dbHelper);
        
        Team team3 = new Team();
        team3.setName("Bulls");
        team3.save(dbHelper);
        
        // Find all teams
        List<Team> allTeams = Team.findAll(dbHelper);
        assertEquals("Should find 3 teams", 3, allTeams.size());
        
        // Verify teams are sorted by name
        assertEquals("First team should be Bulls", "Bulls", allTeams.get(0).getName());
        assertEquals("Second team should be Lakers", "Lakers", allTeams.get(1).getName());
        assertEquals("Third team should be Warriors", "Warriors", allTeams.get(2).getName());
    }
    
    @Test
    public void testFindByFirebaseId() {
        // Create team with Firebase ID
        Team team = new Team();
        team.setName("Celtics");
        team.setFirebaseId("firebase_abc123");
        team.setSyncStatus("synced");
        team.save(dbHelper);
        
        // Find by Firebase ID
        Team foundTeam = Team.findByFirebaseId(dbHelper, "firebase_abc123");
        assertNotNull("Team should be found by Firebase ID", foundTeam);
        assertEquals("Team name should match", "Celtics", foundTeam.getName());
        assertEquals("Firebase ID should match", "firebase_abc123", foundTeam.getFirebaseId());
        assertEquals("Sync status should match", "synced", foundTeam.getSyncStatus());
        
        // Test null Firebase ID
        Team nullResult = Team.findByFirebaseId(dbHelper, null);
        assertNull("Should return null for null Firebase ID", nullResult);
        
        // Test non-existent Firebase ID
        Team notFound = Team.findByFirebaseId(dbHelper, "nonexistent");
        assertNull("Should return null for non-existent Firebase ID", notFound);
    }
    
    @Test
    public void testFindPendingSync() {
        // Create teams with different sync statuses
        Team team1 = new Team();
        team1.setName("Team1");
        team1.setSyncStatus("local");
        team1.save(dbHelper);
        
        Team team2 = new Team();
        team2.setName("Team2");
        team2.setSyncStatus("pending");
        team2.save(dbHelper);
        
        Team team3 = new Team();
        team3.setName("Team3");
        team3.setSyncStatus("synced");
        team3.save(dbHelper);
        
        Team team4 = new Team();
        team4.setName("Team4");
        team4.setSyncStatus("local");
        team4.save(dbHelper);
        
        // Find pending sync teams
        List<Team> pendingTeams = Team.findPendingSync(dbHelper);
        assertEquals("Should find 3 teams pending sync", 3, pendingTeams.size());
        
        // Verify only local and pending teams are returned
        for (Team team : pendingTeams) {
            assertTrue("Team should have local or pending status", 
                     "local".equals(team.getSyncStatus()) || "pending".equals(team.getSyncStatus()));
        }
    }
    
    @Test
    public void testSyncMetadata() {
        // Create team with sync metadata
        Team team = new Team();
        team.setName("Sync Test Team");
        team.setFirebaseId("firebase_sync_123");
        team.setSyncStatus("synced");
        team.setLastSyncTimestamp(System.currentTimeMillis());
        team.save(dbHelper);
        
        // Retrieve and verify sync metadata
        Team retrievedTeam = Team.findById(dbHelper, team.getId());
        assertEquals("Firebase ID should match", "firebase_sync_123", retrievedTeam.getFirebaseId());
        assertEquals("Sync status should match", "synced", retrievedTeam.getSyncStatus());
        assertNotNull("Last sync timestamp should be set", retrievedTeam.getLastSyncTimestamp());
        assertTrue("Last sync timestamp should be valid", retrievedTeam.getLastSyncTimestamp() > 0);
    }
    
    @Test
    public void testUniqueNameConstraint() {
        // Create first team
        Team team1 = new Team();
        team1.setName("Unique Team");
        long result1 = team1.save(dbHelper);
        assertTrue("First team creation should succeed", result1 > 0);
        
        // Try to create second team with same name
        Team team2 = new Team();
        team2.setName("Unique Team");
        
        try {
            long result2 = team2.save(dbHelper);
            fail("Should not allow duplicate team names");
        } catch (Exception e) {
            // Expected - unique constraint violation
            assertTrue("Should be a constraint violation", 
                     e.getMessage().contains("UNIQUE") || e.getMessage().contains("constraint"));
        }
    }
    
    @Test
    public void testGetCount() {
        // Initially no teams
        int initialCount = Team.getCount(dbHelper);
        assertEquals("Initial count should be 0", 0, initialCount);
        
        // Add teams
        Team team1 = new Team();
        team1.setName("Team1");
        team1.save(dbHelper);
        
        Team team2 = new Team();
        team2.setName("Team2");
        team2.save(dbHelper);
        
        // Check count
        int finalCount = Team.getCount(dbHelper);
        assertEquals("Final count should be 2", 2, finalCount);
    }
    
    @Test
    public void testTeamEquality() {
        Team team1 = new Team();
        team1.setId(1);
        team1.setName("Lakers");
        
        Team team2 = new Team();
        team2.setId(1);
        team2.setName("Lakers");
        
        Team team3 = new Team();
        team3.setId(2);
        team3.setName("Warriors");
        
        assertEquals("Teams with same ID and name should be equal", team1, team2);
        assertNotEquals("Teams with different ID should not be equal", team1, team3);
        assertEquals("Hash codes should match for equal teams", team1.hashCode(), team2.hashCode());
    }
    
    @Test
    public void testToString() {
        Team team = new Team();
        team.setId(5);
        team.setName("Test Team");
        
        String toString = team.toString();
        assertTrue("toString should contain team name", toString.contains("Test Team"));
        assertTrue("toString should contain team ID", toString.contains("5"));
    }
}
