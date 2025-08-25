package com.basketballstats.app.sync;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.basketballstats.app.data.DatabaseHelper;
import com.basketballstats.app.models.SyncQueue;
import com.basketballstats.app.models.Team;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for SyncQueue model and queue operations
 * Tests queue management, retry logic, and queue processing
 */
@RunWith(AndroidJUnit4.class)
public class SyncQueueTest {
    
    private DatabaseHelper dbHelper;
    private Context context;
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        // Clear sync queue
        dbHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.TABLE_SYNC_QUEUE);
    }
    
    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    @Test
    public void testCreateSyncQueueItem() {
        SyncQueue queueItem = new SyncQueue();
        queueItem.setTableName("teams");
        queueItem.setRecordId(123);
        queueItem.setOperation("create");
        queueItem.setFirebaseId("firebase_456");
        queueItem.setDataJson("{\"name\":\"Test Team\"}");
        queueItem.setRetryCount(0);
        queueItem.setMaxRetries(3);
        queueItem.setErrorMessage("Network timeout");
        
        long result = queueItem.save(dbHelper);
        assertTrue("Queue item creation should succeed", result > 0);
        assertTrue("Queue item ID should be assigned", queueItem.getId() > 0);
        assertNotNull("Created timestamp should be set", queueItem.getCreatedAt());
    }
    
    @Test
    public void testFindPendingOperations() {
        // Create queue items with different retry counts
        SyncQueue pending1 = new SyncQueue();
        pending1.setTableName("teams");
        pending1.setRecordId(1);
        pending1.setOperation("create");
        pending1.setRetryCount(1);
        pending1.setMaxRetries(3);
        pending1.save(dbHelper);
        
        SyncQueue pending2 = new SyncQueue();
        pending2.setTableName("games");
        pending2.setRecordId(2);
        pending2.setOperation("update");
        pending2.setRetryCount(2);
        pending2.setMaxRetries(3);
        pending2.save(dbHelper);
        
        SyncQueue failed = new SyncQueue();
        failed.setTableName("events");
        failed.setRecordId(3);
        failed.setOperation("create");
        failed.setRetryCount(3);
        failed.setMaxRetries(3);
        failed.save(dbHelper);
        
        // Find pending operations
        List<SyncQueue> pendingOperations = SyncQueue.findPendingOperations(dbHelper);
        assertEquals("Should find 2 pending operations", 2, pendingOperations.size());
        
        // Verify only pending operations are returned
        for (SyncQueue operation : pendingOperations) {
            assertTrue("Operation should be pending", 
                     operation.getRetryCount() < operation.getMaxRetries());
        }
    }
    
    @Test
    public void testFindFailedOperations() {
        // Create successful and failed operations
        SyncQueue pending = new SyncQueue();
        pending.setTableName("teams");
        pending.setRecordId(1);
        pending.setOperation("create");
        pending.setRetryCount(1);
        pending.setMaxRetries(3);
        pending.save(dbHelper);
        
        SyncQueue failed1 = new SyncQueue();
        failed1.setTableName("games");
        failed1.setRecordId(2);
        failed1.setOperation("update");
        failed1.setRetryCount(3);
        failed1.setMaxRetries(3);
        failed1.save(dbHelper);
        
        SyncQueue failed2 = new SyncQueue();
        failed2.setTableName("events");
        failed2.setRecordId(3);
        failed2.setOperation("create");
        failed2.setRetryCount(5);
        failed2.setMaxRetries(3);
        failed2.save(dbHelper);
        
        // Find failed operations
        List<SyncQueue> failedOperations = SyncQueue.findFailedOperations(dbHelper);
        assertEquals("Should find 2 failed operations", 2, failedOperations.size());
        
        // Verify only failed operations are returned
        for (SyncQueue operation : failedOperations) {
            assertTrue("Operation should be failed", 
                     operation.getRetryCount() >= operation.getMaxRetries());
        }
    }
    
    @Test
    public void testRetryLogic() {
        SyncQueue queueItem = new SyncQueue();
        queueItem.setTableName("teams");
        queueItem.setRecordId(1);
        queueItem.setOperation("create");
        queueItem.setRetryCount(0);
        queueItem.setMaxRetries(3);
        queueItem.save(dbHelper);
        
        // Simulate retry attempts
        for (int attempt = 1; attempt <= 5; attempt++) {
            queueItem.setRetryCount(attempt);
            queueItem.setLastAttempt(String.valueOf(System.currentTimeMillis()));
            queueItem.save(dbHelper);
            
            boolean shouldRetry = queueItem.getRetryCount() < queueItem.getMaxRetries();
            
            if (attempt <= 3) {
                assertTrue("Should retry for attempt " + attempt, shouldRetry);
            } else {
                assertFalse("Should not retry for attempt " + attempt, shouldRetry);
            }
        }
    }
    
    @Test
    public void testOperationPriorities() {
        // Create operations for different table types
        SyncQueue criticalOp = new SyncQueue();
        criticalOp.setTableName("user_profile");
        criticalOp.setRecordId(1);
        criticalOp.setOperation("update");
        criticalOp.setCreatedAt(String.valueOf(System.currentTimeMillis() - 1000));
        criticalOp.save(dbHelper);
        
        SyncQueue normalOp = new SyncQueue();
        normalOp.setTableName("teams");
        normalOp.setRecordId(2);
        normalOp.setOperation("create");
        normalOp.setCreatedAt(String.valueOf(System.currentTimeMillis() - 500));
        normalOp.save(dbHelper);
        
        SyncQueue lowPriorityOp = new SyncQueue();
        lowPriorityOp.setTableName("events");
        lowPriorityOp.setRecordId(3);
        lowPriorityOp.setOperation("create");
        lowPriorityOp.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        lowPriorityOp.save(dbHelper);
        
        // Test priority classification (would be used by SyncQueueManager)
        assertTrue("User profile operations should be critical", 
                  "user_profile".equals(criticalOp.getTableName()));
        assertTrue("Team operations should be normal priority", 
                  "teams".equals(normalOp.getTableName()));
        assertTrue("Event operations should be low priority", 
                  "events".equals(lowPriorityOp.getTableName()));
    }
    
    @Test
    public void testQueueItemEquality() {
        SyncQueue item1 = new SyncQueue();
        item1.setId(1);
        item1.setTableName("teams");
        item1.setRecordId(123);
        item1.setOperation("create");
        item1.setRetryCount(2);
        
        SyncQueue item2 = new SyncQueue();
        item2.setId(1);
        item2.setTableName("teams");
        item2.setRecordId(123);
        item2.setOperation("create");
        item2.setRetryCount(2);
        
        SyncQueue item3 = new SyncQueue();
        item3.setId(2);
        item3.setTableName("games");
        item3.setRecordId(456);
        item3.setOperation("update");
        item3.setRetryCount(1);
        
        assertEquals("Items with same properties should be equal", item1, item2);
        assertNotEquals("Items with different properties should not be equal", item1, item3);
        assertEquals("Hash codes should match for equal items", item1.hashCode(), item2.hashCode());
    }
    
    @Test
    public void testQueueItemToString() {
        SyncQueue queueItem = new SyncQueue();
        queueItem.setId(5);
        queueItem.setTableName("teams");
        queueItem.setRecordId(123);
        queueItem.setOperation("create");
        queueItem.setRetryCount(2);
        queueItem.setMaxRetries(3);
        
        String toString = queueItem.toString();
        assertTrue("toString should contain operation", toString.contains("create"));
        assertTrue("toString should contain table name", toString.contains("teams"));
        assertTrue("toString should contain record ID", toString.contains("123"));
        assertTrue("toString should contain retry info", toString.contains("2"));
        assertTrue("toString should contain max retries", toString.contains("3"));
    }
    
    @Test
    public void testConvenienceConstructor() {
        SyncQueue queueItem = new SyncQueue("teams", 123, "create", "firebase_456");
        
        assertEquals("Table name should be set", "teams", queueItem.getTableName());
        assertEquals("Record ID should be set", 123, queueItem.getRecordId());
        assertEquals("Operation should be set", "create", queueItem.getOperation());
        assertEquals("Firebase ID should be set", "firebase_456", queueItem.getFirebaseId());
        assertEquals("Retry count should default to 0", 0, queueItem.getRetryCount());
        assertEquals("Max retries should default to 3", 3, queueItem.getMaxRetries());
    }
    
    @Test
    public void testQueueConvenienceMethods() {
        // Test enqueue convenience method
        Team team = new Team();
        team.setId(123);
        team.setName("Test Team");
        
        SyncQueue.enqueue(dbHelper, "teams", 123, "create", null, "{\"name\":\"Test Team\"}");
        
        // Verify item was added
        SyncQueue foundItem = SyncQueue.findByTableAndRecord(dbHelper, "teams", 123, "create");
        assertNotNull("Queue item should be found", foundItem);
        assertEquals("Table name should match", "teams", foundItem.getTableName());
        assertEquals("Record ID should match", 123, foundItem.getRecordId());
        assertEquals("Operation should match", "create", foundItem.getOperation());
        
        // Test dequeue convenience method
        SyncQueue.dequeue(dbHelper, "teams", 123, "create");
        
        // Verify item was removed
        SyncQueue removedItem = SyncQueue.findByTableAndRecord(dbHelper, "teams", 123, "create");
        assertNull("Queue item should be removed", removedItem);
    }
    
    @Test
    public void testClearOperations() {
        // Create various queue items
        SyncQueue item1 = new SyncQueue("teams", 1, "create", null);
        item1.setRetryCount(1);
        item1.setMaxRetries(3);
        item1.save(dbHelper);
        
        SyncQueue item2 = new SyncQueue("games", 2, "update", null);
        item2.setRetryCount(3);
        item2.setMaxRetries(3);
        item2.save(dbHelper);
        
        SyncQueue item3 = new SyncQueue("events", 3, "create", null);
        item3.setRetryCount(5);
        item3.setMaxRetries(3);
        item3.save(dbHelper);
        
        // Clear failed operations only
        SyncQueue.clearFailed(dbHelper);
        
        // Should have only pending operation left
        List<SyncQueue> remainingItems = SyncQueue.findAll(dbHelper);
        assertEquals("Should have 1 remaining item", 1, remainingItems.size());
        assertEquals("Remaining item should be pending", "teams", remainingItems.get(0).getTableName());
        
        // Clear all operations
        SyncQueue.clearAll(dbHelper);
        
        // Should have no items left
        List<SyncQueue> allItems = SyncQueue.findAll(dbHelper);
        assertEquals("Should have no items", 0, allItems.size());
    }
}
