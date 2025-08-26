package com.basketballstats.app.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.basketballstats.app.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Event model for basketball game statistics and actions with SQLite persistence
 * 
 * Represents individual basketball events: shots, fouls, rebounds, assists, etc.
 * Enhanced with CRUD operations for SQLite-primary architecture
 * Supports sync metadata for Firebase synchronization
 */
public class Event {
    private static final String TAG = "Event";
    
    // Core fields
    private int id;
    private int gameId;
    private int playerId; // Can be null for team events
    private String teamSide; // "home" or "away"
    private int quarter; // 1-4
    private int gameTimeSeconds; // Seconds remaining when event occurred
    private String eventType; // '1P', '2P', '3P', '1M', '2M', '3M', 'OR', 'DR', 'AST', 'STL', 'BLK', 'TO', 'FOUL', 'TIMEOUT', 'SUB_IN', 'SUB_OUT'
    private int subPlayerOutId; // For substitution events
    private int subPlayerInId; // For substitution events
    private int pointsValue; // Points awarded (1, 2, 3, or 0)
    private int eventSequence; // Order of events in game (1, 2, 3...)
    
    // Related objects (loaded separately)
    private TeamPlayer player;
    private TeamPlayer subPlayerOut;
    private TeamPlayer subPlayerIn;
    
    // Sync fields
    private String createdAt;
    private String updatedAt;
    private String firebaseId;
    private String syncStatus;
    private String lastSyncTimestamp;
    
    // Event types constants
    public static final String TYPE_1P = "1P";
    public static final String TYPE_2P = "2P";
    public static final String TYPE_3P = "3P";
    public static final String TYPE_1M = "1M";
    public static final String TYPE_2M = "2M";
    public static final String TYPE_3M = "3M";
    public static final String TYPE_OR = "OR"; // Offensive Rebound
    public static final String TYPE_DR = "DR"; // Defensive Rebound
    public static final String TYPE_AST = "AST"; // Assist
    public static final String TYPE_STL = "STL"; // Steal
    public static final String TYPE_BLK = "BLK"; // Block
    public static final String TYPE_TO = "TO"; // Turnover
    public static final String TYPE_FOUL = "FOUL"; // Personal Foul
    public static final String TYPE_TIMEOUT = "TIMEOUT"; // Team Timeout
    public static final String TYPE_SUB_IN = "SUB_IN"; // Player Substitution In
    public static final String TYPE_SUB_OUT = "SUB_OUT"; // Player Substitution Out
    
    // Constructors
    public Event() {
        this.pointsValue = 0;
        this.syncStatus = "local";
    }
    
    public Event(int gameId, int playerId, String teamSide, int quarter, int gameTimeSeconds, String eventType) {
        this();
        this.gameId = gameId;
        this.playerId = playerId;
        this.teamSide = teamSide;
        this.quarter = quarter;
        this.gameTimeSeconds = gameTimeSeconds;
        this.eventType = eventType;
        this.pointsValue = calculatePointsValue(eventType);
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    
    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    
    public String getTeamSide() { return teamSide; }
    public void setTeamSide(String teamSide) { this.teamSide = teamSide; }
    
    public int getQuarter() { return quarter; }
    public void setQuarter(int quarter) { this.quarter = quarter; }
    
    public int getGameTimeSeconds() { return gameTimeSeconds; }
    public void setGameTimeSeconds(int gameTimeSeconds) { this.gameTimeSeconds = gameTimeSeconds; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { 
        this.eventType = eventType;
        this.pointsValue = calculatePointsValue(eventType);
    }
    
    public int getSubPlayerOutId() { return subPlayerOutId; }
    public void setSubPlayerOutId(int subPlayerOutId) { this.subPlayerOutId = subPlayerOutId; }
    
    public int getSubPlayerInId() { return subPlayerInId; }
    public void setSubPlayerInId(int subPlayerInId) { this.subPlayerInId = subPlayerInId; }
    
    public int getPointsValue() { return pointsValue; }
    public void setPointsValue(int pointsValue) { this.pointsValue = pointsValue; }
    
    public int getEventSequence() { return eventSequence; }
    public void setEventSequence(int eventSequence) { this.eventSequence = eventSequence; }
    
    public TeamPlayer getPlayer() { return player; }
    public void setPlayer(TeamPlayer player) { this.player = player; }
    
    public TeamPlayer getSubPlayerOut() { return subPlayerOut; }
    public void setSubPlayerOut(TeamPlayer subPlayerOut) { this.subPlayerOut = subPlayerOut; }
    
    public TeamPlayer getSubPlayerIn() { return subPlayerIn; }
    public void setSubPlayerIn(TeamPlayer subPlayerIn) { this.subPlayerIn = subPlayerIn; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }
    
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
    
    public String getLastSyncTimestamp() { return lastSyncTimestamp; }
    public void setLastSyncTimestamp(String lastSyncTimestamp) { this.lastSyncTimestamp = lastSyncTimestamp; }
    
    // ========== BUSINESS LOGIC METHODS ==========
    
    /**
     * Calculate points value based on event type
     */
    private int calculatePointsValue(String eventType) {
        switch (eventType) {
            case TYPE_1P: return 1;
            case TYPE_2P: return 2;
            case TYPE_3P: return 3;
            default: return 0;
        }
    }
    
    /**
     * Check if this is a scoring event
     */
    public boolean isScoringEvent() {
        return TYPE_1P.equals(eventType) || TYPE_2P.equals(eventType) || TYPE_3P.equals(eventType);
    }
    
    /**
     * Check if this is a miss event
     */
    public boolean isMissEvent() {
        return TYPE_1M.equals(eventType) || TYPE_2M.equals(eventType) || TYPE_3M.equals(eventType);
    }
    
    /**
     * Check if this is a rebound event
     */
    public boolean isReboundEvent() {
        return TYPE_OR.equals(eventType) || TYPE_DR.equals(eventType);
    }
    
    /**
     * Check if this is a team event (no individual player)
     */
    public boolean isTeamEvent() {
        return TYPE_TIMEOUT.equals(eventType);
    }
    
    /**
     * Check if this is a substitution event
     */
    public boolean isSubstitutionEvent() {
        return TYPE_SUB_IN.equals(eventType) || TYPE_SUB_OUT.equals(eventType);
    }
    
    /**
     * Get formatted game time (MM:SS)
     */
    public String getFormattedGameTime() {
        int minutes = gameTimeSeconds / 60;
        int seconds = gameTimeSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    /**
     * Get quarter display (Q1, Q2, Q3, Q4)
     */
    public String getQuarterDisplay() {
        return "Q" + quarter;
    }
    
    /**
     * Get display description for event log
     */
    public String getDisplayDescription() {
        if (isTeamEvent()) {
            return getTeamSide().toUpperCase() + " - " + eventType;
        } else if (player != null) {
            return "#" + player.getJerseyNumber() + " " + player.getName() + " - " + eventType;
        } else {
            return "Player " + playerId + " - " + eventType;
        }
    }
    
    /**
     * Display format for event log
     * ✅ FIX: Include quarter information for LogActivity parsing
     * Expected format: "Q1 8:45 - #23 LeBron James - 2P"
     */
    @Override
    public String toString() {
        return getQuarterDisplay() + " " + getFormattedGameTime() + " - " + getDisplayDescription();
    }
    
    // ========== SQLITE CRUD OPERATIONS ==========
    
    /**
     * Save event to database (INSERT or UPDATE)
     */
    public long save(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.EVENTS_COLUMN_GAME_ID, gameId);
        values.put(DatabaseHelper.EVENTS_COLUMN_PLAYER_ID, playerId > 0 ? playerId : null);
        values.put(DatabaseHelper.EVENTS_COLUMN_TEAM_SIDE, teamSide);
        values.put(DatabaseHelper.EVENTS_COLUMN_QUARTER, quarter);
        values.put(DatabaseHelper.EVENTS_COLUMN_GAME_TIME_SECONDS, gameTimeSeconds);
        values.put(DatabaseHelper.EVENTS_COLUMN_EVENT_TYPE, eventType);
        values.put(DatabaseHelper.EVENTS_COLUMN_SUB_PLAYER_OUT_ID, subPlayerOutId > 0 ? subPlayerOutId : null);
        values.put(DatabaseHelper.EVENTS_COLUMN_SUB_PLAYER_IN_ID, subPlayerInId > 0 ? subPlayerInId : null);
        values.put(DatabaseHelper.EVENTS_COLUMN_POINTS_VALUE, pointsValue);
        values.put(DatabaseHelper.EVENTS_COLUMN_EVENT_SEQUENCE, eventSequence);
        values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentTimestamp());
        values.put(DatabaseHelper.COLUMN_FIREBASE_ID, firebaseId);
        values.put(DatabaseHelper.COLUMN_SYNC_STATUS, syncStatus);
        values.put(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP, lastSyncTimestamp);
        
        long result;
        if (id > 0) {
            // UPDATE existing event
            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(id)};
            result = db.update(DatabaseHelper.TABLE_EVENTS, values, whereClause, whereArgs);
            Log.d(TAG, "Updated event: " + toString() + " (ID: " + id + ")");
        } else {
            // INSERT new event - assign sequence number
            if (eventSequence <= 0) {
                eventSequence = getNextSequenceNumber(dbHelper, gameId);
                values.put(DatabaseHelper.EVENTS_COLUMN_EVENT_SEQUENCE, eventSequence);
            }
            values.put(DatabaseHelper.COLUMN_CREATED_AT, getCurrentTimestamp());
            result = db.insert(DatabaseHelper.TABLE_EVENTS, null, values);
            if (result != -1) {
                this.id = (int) result;
                Log.d(TAG, "Created event: " + toString() + " (ID: " + id + ")");
            }
        }
        
        return result;
    }
    
    /**
     * Delete event from database
     */
    public boolean delete(DatabaseHelper dbHelper) {
        if (id <= 0) {
            Log.w(TAG, "Cannot delete event with invalid ID: " + id);
            return false;
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        
        int rowsAffected = db.delete(DatabaseHelper.TABLE_EVENTS, whereClause, whereArgs);
        boolean success = rowsAffected > 0;
        
        if (success) {
            Log.d(TAG, "Deleted event: " + toString() + " (ID: " + id + ")");
        } else {
            Log.w(TAG, "Failed to delete event: " + toString() + " (ID: " + id + ")");
        }
        
        return success;
    }
    
    /**
     * Delete all events for a specific game
     * Used for clearing game logs
     */
    public static int deleteByGameId(DatabaseHelper dbHelper, int gameId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.EVENTS_COLUMN_GAME_ID + " = ?";
        String[] whereArgs = {String.valueOf(gameId)};
        
        int rowsAffected = db.delete(DatabaseHelper.TABLE_EVENTS, whereClause, whereArgs);
        
        Log.d(TAG, String.format("Deleted %d events for game ID: %d", rowsAffected, gameId));
        return rowsAffected;
    }
    
    /**
     * Load event from database by ID
     */
    public static Event findById(DatabaseHelper dbHelper, int eventId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(eventId)};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_EVENTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        Event event = null;
        if (cursor.moveToFirst()) {
            event = fromCursor(cursor);
            event.loadRelatedObjects(dbHelper);
        }
        cursor.close();
        
        return event;
    }
    
    /**
     * Get all events for a specific game
     */
    public static List<Event> findByGameId(DatabaseHelper dbHelper, int gameId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Event> events = new ArrayList<>();
        
        String selection = DatabaseHelper.EVENTS_COLUMN_GAME_ID + " = ?";
        String[] selectionArgs = {String.valueOf(gameId)};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_EVENTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseHelper.EVENTS_COLUMN_EVENT_SEQUENCE + " ASC"
        );
        
        while (cursor.moveToNext()) {
            Event event = fromCursor(cursor);
            event.loadRelatedObjects(dbHelper);
            events.add(event);
        }
        cursor.close();
        
        Log.d(TAG, "Loaded " + events.size() + " events for game ID: " + gameId);
        return events;
    }
    
    /**
     * Get recent events for live display (last N events)
     */
    public static List<Event> findRecentByGameId(DatabaseHelper dbHelper, int gameId, int limit) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Event> events = new ArrayList<>();
        
        String selection = DatabaseHelper.EVENTS_COLUMN_GAME_ID + " = ?";
        String[] selectionArgs = {String.valueOf(gameId)};
        String orderBy = DatabaseHelper.EVENTS_COLUMN_EVENT_SEQUENCE + " DESC LIMIT " + limit;
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_EVENTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        );
        
        while (cursor.moveToNext()) {
            Event event = fromCursor(cursor);
            event.loadRelatedObjects(dbHelper);
            events.add(event);
        }
        cursor.close();
        
        return events;
    }
    
    /**
     * Get events that need syncing
     */
    public static List<Event> findPendingSync(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Event> events = new ArrayList<>();
        
        String selection = DatabaseHelper.COLUMN_SYNC_STATUS + " IN (?, ?)";
        String[] selectionArgs = {"local", "pending"};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_EVENTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseHelper.COLUMN_UPDATED_AT + " ASC"
        );
        
        while (cursor.moveToNext()) {
            Event event = fromCursor(cursor);
            event.loadRelatedObjects(dbHelper);
            events.add(event);
        }
        cursor.close();
        
        return events;
    }
    
    /**
     * Get next sequence number for game
     */
    private static int getNextSequenceNumber(DatabaseHelper dbHelper, int gameId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT MAX(" + DatabaseHelper.EVENTS_COLUMN_EVENT_SEQUENCE + ") FROM " + 
                      DatabaseHelper.TABLE_EVENTS + " WHERE " + DatabaseHelper.EVENTS_COLUMN_GAME_ID + " = ?";
        String[] selectionArgs = {String.valueOf(gameId)};
        
        Cursor cursor = db.rawQuery(query, selectionArgs);
        int maxSequence = 0;
        if (cursor.moveToFirst()) {
            maxSequence = cursor.getInt(0);
        }
        cursor.close();
        
        return maxSequence + 1;
    }
    
    /**
     * Load related objects (player, substitution players)
     */
    public void loadRelatedObjects(DatabaseHelper dbHelper) {
        if (playerId > 0) {
            this.player = TeamPlayer.findById(dbHelper, playerId);
        }
        if (subPlayerOutId > 0) {
            this.subPlayerOut = TeamPlayer.findById(dbHelper, subPlayerOutId);
        }
        if (subPlayerInId > 0) {
            this.subPlayerIn = TeamPlayer.findById(dbHelper, subPlayerInId);
        }
    }
    
    /**
     * Update sync status
     */
    public void updateSyncStatus(DatabaseHelper dbHelper, String status, String firebaseId) {
        this.syncStatus = status;
        this.firebaseId = firebaseId;
        this.lastSyncTimestamp = getCurrentTimestamp();
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SYNC_STATUS, status);
        values.put(DatabaseHelper.COLUMN_FIREBASE_ID, firebaseId);
        values.put(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP, lastSyncTimestamp);
        values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentTimestamp());
        
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        
        db.update(DatabaseHelper.TABLE_EVENTS, values, whereClause, whereArgs);
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Create Event object from database cursor
     */
    private static Event fromCursor(Cursor cursor) {
        Event event = new Event();
        
        event.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        event.gameId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.EVENTS_COLUMN_GAME_ID));
        event.playerId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.EVENTS_COLUMN_PLAYER_ID));
        event.teamSide = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.EVENTS_COLUMN_TEAM_SIDE));
        event.quarter = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.EVENTS_COLUMN_QUARTER));
        event.gameTimeSeconds = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.EVENTS_COLUMN_GAME_TIME_SECONDS));
        event.eventType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.EVENTS_COLUMN_EVENT_TYPE));
        event.subPlayerOutId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.EVENTS_COLUMN_SUB_PLAYER_OUT_ID));
        event.subPlayerInId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.EVENTS_COLUMN_SUB_PLAYER_IN_ID));
        event.pointsValue = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.EVENTS_COLUMN_POINTS_VALUE));
        event.eventSequence = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.EVENTS_COLUMN_EVENT_SEQUENCE));
        event.createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT));
        event.updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATED_AT));
        event.firebaseId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIREBASE_ID));
        event.syncStatus = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SYNC_STATUS));
        event.lastSyncTimestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP));
        
        return event;
    }
    
    /**
     * Get current timestamp as string
     */
    private static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Get event count for game
     */
    public static int getCountForGame(DatabaseHelper dbHelper, int gameId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_EVENTS + 
                      " WHERE " + DatabaseHelper.EVENTS_COLUMN_GAME_ID + " = ?";
        String[] selectionArgs = {String.valueOf(gameId)};
        
        Cursor cursor = db.rawQuery(query, selectionArgs);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    /**
     * Get event count for player (for foreign key safety checking)
     */
    public static int getCountForPlayer(DatabaseHelper dbHelper, int playerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_EVENTS + 
                      " WHERE " + DatabaseHelper.EVENTS_COLUMN_PLAYER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(playerId)};
        
        Cursor cursor = db.rawQuery(query, selectionArgs);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        
        Log.d(TAG, String.format("Player %d has %d events recorded", playerId, count));
        return count;
    }
    
    /**
     * Get total event count
     */
    public static int getTotalCount(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_EVENTS, null);
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
        
        Event event = (Event) obj;
        return id == event.id && 
               gameId == event.gameId && 
               eventSequence == event.eventSequence &&
               (eventType != null ? eventType.equals(event.eventType) : event.eventType == null);
    }
    
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + gameId;
        result = 31 * result + eventSequence;
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
        return result;
    }
    
    /**
     * Find all events
     */
    public static List<Event> findAll(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Event> events = new ArrayList<>();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_EVENTS,
            null,
            null,
            null,
            null,
            null,
            DatabaseHelper.EVENTS_COLUMN_EVENT_SEQUENCE + " ASC"
        );
        
        while (cursor.moveToNext()) {
            events.add(fromCursor(cursor));
        }
        cursor.close();
        
        Log.d(TAG, "Loaded " + events.size() + " events");
        return events;
    }
}
