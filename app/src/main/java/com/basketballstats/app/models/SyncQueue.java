package com.basketballstats.app.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.basketballstats.app.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SyncQueue model for managing pending Firebase operations with SQLite persistence
 * 
 * Tracks failed sync operations for retry when connectivity resumes
 * Handles operation queuing, retry logic, and error tracking
 * Essential for offline-first architecture with reliable sync
 */
public class SyncQueue {
    private static final String TAG = "SyncQueue";
    
    // Core fields
    private int id;
    private String tableName; // 'teams', 'team_players', 'games', 'events', etc.
    private int recordId; // Local record ID
    private String operation; // 'create', 'update', 'delete'
    private String firebaseId; // Firebase document ID (for updates/deletes)
    private String dataJson; // JSON representation of record data
    private int retryCount;
    private int maxRetries;
    private String lastAttempt;
    private String errorMessage;
    
    // Metadata fields
    private String createdAt;
    
    // Operation types constants
    public static final String OPERATION_CREATE = "create";
    public static final String OPERATION_UPDATE = "update";
    public static final String OPERATION_DELETE = "delete";
    
    // Table names constants
    public static final String TABLE_TEAMS = DatabaseHelper.TABLE_TEAMS;
    public static final String TABLE_TEAM_PLAYERS = DatabaseHelper.TABLE_TEAM_PLAYERS;
    public static final String TABLE_GAMES = DatabaseHelper.TABLE_GAMES;
    public static final String TABLE_EVENTS = DatabaseHelper.TABLE_EVENTS;
    public static final String TABLE_APP_SETTINGS = DatabaseHelper.TABLE_APP_SETTINGS;
    
    // Constructors
    public SyncQueue() {
        this.retryCount = 0;
        this.maxRetries = 3;
    }
    
    public SyncQueue(String tableName, int recordId, String operation) {
        this();
        this.tableName = tableName;
        this.recordId = recordId;
        this.operation = operation;
    }
    
    public SyncQueue(String tableName, int recordId, String operation, String firebaseId) {
        this(tableName, recordId, operation);
        this.firebaseId = firebaseId;
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    
    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }
    
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }
    
    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }
    
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    
    public String getLastAttempt() { return lastAttempt; }
    public void setLastAttempt(String lastAttempt) { this.lastAttempt = lastAttempt; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    // ========== BUSINESS LOGIC METHODS ==========
    
    /**
     * Check if this operation has exceeded max retries
     */
    public boolean hasExceededMaxRetries() {
        return retryCount >= maxRetries;
    }
    
    /**
     * Check if this operation can be retried
     */
    public boolean canRetry() {
        return !hasExceededMaxRetries();
    }
    
    /**
     * Increment retry count and update last attempt
     */
    public void incrementRetryCount() {
        this.retryCount++;
        this.lastAttempt = getCurrentTimestamp();
    }
    
    /**
     * Mark operation as failed with error message
     */
    public void markFailed(String errorMessage) {
        this.errorMessage = errorMessage;
        incrementRetryCount();
    }
    
    /**
     * Check if this is a create operation
     */
    public boolean isCreateOperation() {
        return OPERATION_CREATE.equals(operation);
    }
    
    /**
     * Check if this is an update operation
     */
    public boolean isUpdateOperation() {
        return OPERATION_UPDATE.equals(operation);
    }
    
    /**
     * Check if this is a delete operation
     */
    public boolean isDeleteOperation() {
        return OPERATION_DELETE.equals(operation);
    }
    
    /**
     * Get retry delay in milliseconds (exponential backoff)
     */
    public long getRetryDelayMs() {
        // Exponential backoff: 1s, 2s, 4s, 8s, etc.
        return (long) (1000 * Math.pow(2, retryCount));
    }
    
    /**
     * Get formatted last attempt time
     */
    public String getFormattedLastAttempt() {
        if (lastAttempt != null) {
            try {
                long timestamp = Long.parseLong(lastAttempt);
                return new java.util.Date(timestamp).toString();
            } catch (NumberFormatException e) {
                return lastAttempt;
            }
        }
        return "Never";
    }
    
    @Override
    public String toString() {
        return operation.toUpperCase() + " " + tableName + "#" + recordId + " (retry " + retryCount + "/" + maxRetries + ")";
    }
    
    // ========== SQLITE CRUD OPERATIONS ==========
    
    /**
     * Save sync queue item to database (INSERT or UPDATE)
     */
    public long save(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.SYNC_QUEUE_COLUMN_TABLE_NAME, tableName);
        values.put(DatabaseHelper.SYNC_QUEUE_COLUMN_RECORD_ID, recordId);
        values.put(DatabaseHelper.SYNC_QUEUE_COLUMN_OPERATION, operation);
        values.put(DatabaseHelper.COLUMN_FIREBASE_ID, firebaseId);
        values.put(DatabaseHelper.SYNC_QUEUE_COLUMN_DATA_JSON, dataJson);
        values.put(DatabaseHelper.SYNC_QUEUE_COLUMN_RETRY_COUNT, retryCount);
        values.put(DatabaseHelper.SYNC_QUEUE_COLUMN_MAX_RETRIES, maxRetries);
        values.put(DatabaseHelper.SYNC_QUEUE_COLUMN_LAST_ATTEMPT, lastAttempt);
        values.put(DatabaseHelper.SYNC_QUEUE_COLUMN_ERROR_MESSAGE, errorMessage);
        
        long result;
        if (id > 0) {
            // UPDATE existing queue item
            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(id)};
            result = db.update(DatabaseHelper.TABLE_SYNC_QUEUE, values, whereClause, whereArgs);
            Log.d(TAG, "Updated sync queue item: " + toString() + " (ID: " + id + ")");
        } else {
            // INSERT new queue item
            values.put(DatabaseHelper.COLUMN_CREATED_AT, getCurrentTimestamp());
            result = db.insert(DatabaseHelper.TABLE_SYNC_QUEUE, null, values);
            if (result != -1) {
                this.id = (int) result;
                Log.d(TAG, "Created sync queue item: " + toString() + " (ID: " + id + ")");
            }
        }
        
        return result;
    }
    
    /**
     * Delete sync queue item from database
     */
    public boolean delete(DatabaseHelper dbHelper) {
        if (id <= 0) {
            Log.w(TAG, "Cannot delete sync queue item with invalid ID: " + id);
            return false;
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        
        int rowsAffected = db.delete(DatabaseHelper.TABLE_SYNC_QUEUE, whereClause, whereArgs);
        boolean success = rowsAffected > 0;
        
        if (success) {
            Log.d(TAG, "Deleted sync queue item: " + toString() + " (ID: " + id + ")");
        } else {
            Log.w(TAG, "Failed to delete sync queue item: " + toString() + " (ID: " + id + ")");
        }
        
        return success;
    }
    
    /**
     * Load sync queue item from database by ID
     */
    public static SyncQueue findById(DatabaseHelper dbHelper, int queueId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(queueId)};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_SYNC_QUEUE,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        SyncQueue queueItem = null;
        if (cursor.moveToFirst()) {
            queueItem = fromCursor(cursor);
        }
        cursor.close();
        
        return queueItem;
    }
    
    /**
     * Get all pending sync queue items (that can be retried)
     */
    public static List<SyncQueue> findPending(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<SyncQueue> queueItems = new ArrayList<>();
        
        String selection = DatabaseHelper.SYNC_QUEUE_COLUMN_RETRY_COUNT + " < " + DatabaseHelper.SYNC_QUEUE_COLUMN_MAX_RETRIES;
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_SYNC_QUEUE,
            null,
            selection,
            null,
            null,
            null,
            DatabaseHelper.COLUMN_CREATED_AT + " ASC"
        );
        
        while (cursor.moveToNext()) {
            queueItems.add(fromCursor(cursor));
        }
        cursor.close();
        
        Log.d(TAG, "Found " + queueItems.size() + " pending sync queue items");
        return queueItems;
    }
    
    /**
     * Get failed sync queue items (exceeded max retries)
     */
    public static List<SyncQueue> findFailed(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<SyncQueue> queueItems = new ArrayList<>();
        
        String selection = DatabaseHelper.SYNC_QUEUE_COLUMN_RETRY_COUNT + " >= " + DatabaseHelper.SYNC_QUEUE_COLUMN_MAX_RETRIES;
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_SYNC_QUEUE,
            null,
            selection,
            null,
            null,
            null,
            DatabaseHelper.SYNC_QUEUE_COLUMN_LAST_ATTEMPT + " DESC"
        );
        
        while (cursor.moveToNext()) {
            queueItems.add(fromCursor(cursor));
        }
        cursor.close();
        
        return queueItems;
    }
    
    /**
     * Get sync queue items by table name
     */
    public static List<SyncQueue> findByTable(DatabaseHelper dbHelper, String tableName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<SyncQueue> queueItems = new ArrayList<>();
        
        String selection = DatabaseHelper.SYNC_QUEUE_COLUMN_TABLE_NAME + " = ?";
        String[] selectionArgs = {tableName};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_SYNC_QUEUE,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseHelper.COLUMN_CREATED_AT + " ASC"
        );
        
        while (cursor.moveToNext()) {
            queueItems.add(fromCursor(cursor));
        }
        cursor.close();
        
        return queueItems;
    }
    
    /**
     * Get sync queue item by table and record ID
     */
    public static SyncQueue findByTableAndRecord(DatabaseHelper dbHelper, String tableName, int recordId, String operation) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.SYNC_QUEUE_COLUMN_TABLE_NAME + " = ? AND " +
                          DatabaseHelper.SYNC_QUEUE_COLUMN_RECORD_ID + " = ? AND " +
                          DatabaseHelper.SYNC_QUEUE_COLUMN_OPERATION + " = ?";
        String[] selectionArgs = {tableName, String.valueOf(recordId), operation};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_SYNC_QUEUE,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        SyncQueue queueItem = null;
        if (cursor.moveToFirst()) {
            queueItem = fromCursor(cursor);
        }
        cursor.close();
        
        return queueItem;
    }
    
    /**
     * Clear all sync queue items
     */
    public static void clearAll(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_SYNC_QUEUE, null, null);
        Log.d(TAG, "Cleared " + rowsDeleted + " sync queue items");
    }
    
    /**
     * Clear failed sync queue items
     */
    public static void clearFailed(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.SYNC_QUEUE_COLUMN_RETRY_COUNT + " >= " + DatabaseHelper.SYNC_QUEUE_COLUMN_MAX_RETRIES;
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_SYNC_QUEUE, whereClause, null);
        Log.d(TAG, "Cleared " + rowsDeleted + " failed sync queue items");
    }
    
    // ========== CONVENIENCE METHODS ==========
    
    /**
     * Add operation to sync queue
     */
    public static void enqueue(DatabaseHelper dbHelper, String tableName, int recordId, String operation, String firebaseId, String dataJson) {
        // Check if operation already exists
        SyncQueue existing = findByTableAndRecord(dbHelper, tableName, recordId, operation);
        if (existing != null) {
            // Update existing queue item
            existing.setDataJson(dataJson);
            existing.setFirebaseId(firebaseId);
            existing.save(dbHelper);
        } else {
            // Create new queue item
            SyncQueue queueItem = new SyncQueue(tableName, recordId, operation, firebaseId);
            queueItem.setDataJson(dataJson);
            queueItem.save(dbHelper);
        }
    }
    
    /**
     * Remove operation from sync queue (successful sync)
     */
    public static void dequeue(DatabaseHelper dbHelper, String tableName, int recordId, String operation) {
        SyncQueue queueItem = findByTableAndRecord(dbHelper, tableName, recordId, operation);
        if (queueItem != null) {
            queueItem.delete(dbHelper);
        }
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Create SyncQueue object from database cursor
     */
    private static SyncQueue fromCursor(Cursor cursor) {
        SyncQueue queueItem = new SyncQueue();
        
        queueItem.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        queueItem.tableName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SYNC_QUEUE_COLUMN_TABLE_NAME));
        queueItem.recordId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SYNC_QUEUE_COLUMN_RECORD_ID));
        queueItem.operation = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SYNC_QUEUE_COLUMN_OPERATION));
        queueItem.firebaseId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIREBASE_ID));
        queueItem.dataJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SYNC_QUEUE_COLUMN_DATA_JSON));
        queueItem.retryCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SYNC_QUEUE_COLUMN_RETRY_COUNT));
        queueItem.maxRetries = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SYNC_QUEUE_COLUMN_MAX_RETRIES));
        queueItem.lastAttempt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SYNC_QUEUE_COLUMN_LAST_ATTEMPT));
        queueItem.errorMessage = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SYNC_QUEUE_COLUMN_ERROR_MESSAGE));
        queueItem.createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT));
        
        return queueItem;
    }
    
    /**
     * Get current timestamp as string
     */
    private static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Get sync queue count
     */
    public static int getCount(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SYNC_QUEUE, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    /**
     * Get pending sync queue count
     */
    public static int getPendingCount(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SYNC_QUEUE + 
                      " WHERE " + DatabaseHelper.SYNC_QUEUE_COLUMN_RETRY_COUNT + " < " + DatabaseHelper.SYNC_QUEUE_COLUMN_MAX_RETRIES;
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    // ========== OBJECT METHODS ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SyncQueue that = (SyncQueue) obj;
        return id == that.id && 
               recordId == that.recordId &&
               (tableName != null ? tableName.equals(that.tableName) : that.tableName == null) &&
               (operation != null ? operation.equals(that.operation) : that.operation == null);
    }
    
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + recordId;
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
        result = 31 * result + (operation != null ? operation.hashCode() : 0);
        return result;
    }
}
