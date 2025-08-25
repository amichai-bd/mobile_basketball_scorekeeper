package com.basketballstats.app.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.basketballstats.app.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * TeamPlayer model for players in team rosters with SQLite persistence
 * 
 * Enhanced with CRUD operations for SQLite-primary architecture
 * Supports sync metadata for Firebase synchronization
 */
public class TeamPlayer {
    private static final String TAG = "TeamPlayer";
    
    // Core fields
    private int id;
    private int teamId;
    private int jerseyNumber; // Renamed from 'number' to match database schema
    private String name;
    private boolean isSelected; // For UI selection state (not persisted)
    
    // Sync fields
    private String createdAt;
    private String updatedAt;
    private String firebaseId;
    private String syncStatus;
    private String lastSyncTimestamp;
    
    // Constructors
    public TeamPlayer() {
        this.isSelected = false;
        this.syncStatus = "local";
    }
    
    public TeamPlayer(int id, int teamId, int jerseyNumber, String name) {
        this.id = id;
        this.teamId = teamId;
        this.jerseyNumber = jerseyNumber;
        this.name = name;
        this.isSelected = false;
        this.syncStatus = "local";
    }
    
    public TeamPlayer(int teamId, int jerseyNumber, String name) {
        this.teamId = teamId;
        this.jerseyNumber = jerseyNumber;
        this.name = name;
        this.isSelected = false;
        this.syncStatus = "local";
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    
    public int getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(int jerseyNumber) { this.jerseyNumber = jerseyNumber; }
    
    // Backward compatibility
    public int getNumber() { return jerseyNumber; }
    public void setNumber(int number) { this.jerseyNumber = number; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { this.isSelected = selected; }
    
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
     * Display format for checkbox list
     */
    @Override
    public String toString() {
        return "#" + jerseyNumber + " " + name;
    }
    
    /**
     * For game player creation (backward compatibility)
     */
    public Player toGamePlayer(int gameId, String teamSide) {
        return new Player(this.id, gameId, teamSide, this.jerseyNumber, this.name);
    }
    
    /**
     * Check if jersey number is valid (0-99)
     */
    public boolean isValidJerseyNumber() {
        return jerseyNumber >= 0 && jerseyNumber <= 99;
    }
    
    // ========== SQLITE CRUD OPERATIONS ==========
    
    /**
     * Save player to database (INSERT or UPDATE)
     */
    public long save(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.TEAM_PLAYERS_COLUMN_TEAM_ID, teamId);
        values.put(DatabaseHelper.TEAM_PLAYERS_COLUMN_JERSEY_NUMBER, jerseyNumber);
        values.put(DatabaseHelper.TEAM_PLAYERS_COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentTimestamp());
        values.put(DatabaseHelper.COLUMN_FIREBASE_ID, firebaseId);
        values.put(DatabaseHelper.COLUMN_SYNC_STATUS, syncStatus);
        values.put(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP, lastSyncTimestamp);
        
        long result;
        if (id > 0) {
            // UPDATE existing player
            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(id)};
            result = db.update(DatabaseHelper.TABLE_TEAM_PLAYERS, values, whereClause, whereArgs);
            Log.d(TAG, "Updated player: " + name + " #" + jerseyNumber + " (ID: " + id + ")");
        } else {
            // INSERT new player
            values.put(DatabaseHelper.COLUMN_CREATED_AT, getCurrentTimestamp());
            result = db.insert(DatabaseHelper.TABLE_TEAM_PLAYERS, null, values);
            if (result != -1) {
                this.id = (int) result;
                Log.d(TAG, "Created player: " + name + " #" + jerseyNumber + " (ID: " + id + ")");
            }
        }
        
        return result;
    }
    
    /**
     * Delete player from database
     */
    public boolean delete(DatabaseHelper dbHelper) {
        if (id <= 0) {
            Log.w(TAG, "Cannot delete player with invalid ID: " + id);
            return false;
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        
        int rowsAffected = db.delete(DatabaseHelper.TABLE_TEAM_PLAYERS, whereClause, whereArgs);
        boolean success = rowsAffected > 0;
        
        if (success) {
            Log.d(TAG, "Deleted player: " + name + " #" + jerseyNumber + " (ID: " + id + ")");
        } else {
            Log.w(TAG, "Failed to delete player: " + name + " #" + jerseyNumber + " (ID: " + id + ")");
        }
        
        return success;
    }
    
    /**
     * Load player from database by ID
     */
    public static TeamPlayer findById(DatabaseHelper dbHelper, int playerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(playerId)};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_TEAM_PLAYERS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        TeamPlayer player = null;
        if (cursor.moveToFirst()) {
            player = fromCursor(cursor);
        }
        cursor.close();
        
        return player;
    }
    
    /**
     * Get all players for a specific team
     */
    public static List<TeamPlayer> findByTeamId(DatabaseHelper dbHelper, int teamId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<TeamPlayer> players = new ArrayList<>();
        
        String selection = DatabaseHelper.TEAM_PLAYERS_COLUMN_TEAM_ID + " = ?";
        String[] selectionArgs = {String.valueOf(teamId)};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_TEAM_PLAYERS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseHelper.TEAM_PLAYERS_COLUMN_JERSEY_NUMBER + " ASC"
        );
        
        while (cursor.moveToNext()) {
            players.add(fromCursor(cursor));
        }
        cursor.close();
        
        Log.d(TAG, "Loaded " + players.size() + " players for team ID: " + teamId);
        return players;
    }
    
    /**
     * Get players that need syncing
     */
    public static List<TeamPlayer> findPendingSync(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<TeamPlayer> players = new ArrayList<>();
        
        String selection = DatabaseHelper.COLUMN_SYNC_STATUS + " IN (?, ?)";
        String[] selectionArgs = {"local", "pending"};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_TEAM_PLAYERS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseHelper.COLUMN_UPDATED_AT + " ASC"
        );
        
        while (cursor.moveToNext()) {
            players.add(fromCursor(cursor));
        }
        cursor.close();
        
        return players;
    }
    
    /**
     * Check if jersey number is available for team
     */
    public static boolean isJerseyNumberAvailable(DatabaseHelper dbHelper, int teamId, int jerseyNumber, int excludePlayerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = DatabaseHelper.TEAM_PLAYERS_COLUMN_TEAM_ID + " = ? AND " + 
                          DatabaseHelper.TEAM_PLAYERS_COLUMN_JERSEY_NUMBER + " = ?";
        String[] selectionArgs = {String.valueOf(teamId), String.valueOf(jerseyNumber)};
        
        if (excludePlayerId > 0) {
            selection += " AND " + DatabaseHelper.COLUMN_ID + " != ?";
            selectionArgs = new String[]{String.valueOf(teamId), String.valueOf(jerseyNumber), String.valueOf(excludePlayerId)};
        }
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_TEAM_PLAYERS,
            new String[]{DatabaseHelper.COLUMN_ID},
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        boolean isAvailable = !cursor.moveToFirst();
        cursor.close();
        
        return isAvailable;
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
        
        db.update(DatabaseHelper.TABLE_TEAM_PLAYERS, values, whereClause, whereArgs);
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Create TeamPlayer object from database cursor
     */
    private static TeamPlayer fromCursor(Cursor cursor) {
        TeamPlayer player = new TeamPlayer();
        
        player.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        player.teamId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TEAM_PLAYERS_COLUMN_TEAM_ID));
        player.jerseyNumber = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TEAM_PLAYERS_COLUMN_JERSEY_NUMBER));
        player.name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEAM_PLAYERS_COLUMN_NAME));
        player.createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT));
        player.updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATED_AT));
        player.firebaseId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIREBASE_ID));
        player.syncStatus = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SYNC_STATUS));
        player.lastSyncTimestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP));
        
        return player;
    }
    
    /**
     * Get current timestamp as string
     */
    private static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Get player count for team
     */
    public static int getCountForTeam(DatabaseHelper dbHelper, int teamId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TEAM_PLAYERS + 
                      " WHERE " + DatabaseHelper.TEAM_PLAYERS_COLUMN_TEAM_ID + " = ?";
        String[] selectionArgs = {String.valueOf(teamId)};
        
        Cursor cursor = db.rawQuery(query, selectionArgs);
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
        
        TeamPlayer that = (TeamPlayer) obj;
        return id == that.id && 
               teamId == that.teamId && 
               jerseyNumber == that.jerseyNumber &&
               (name != null ? name.equals(that.name) : that.name == null);
    }
    
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + teamId;
        result = 31 * result + jerseyNumber;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
    
    /**
     * Find all team players
     */
    public static List<TeamPlayer> findAll(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<TeamPlayer> teamPlayers = new ArrayList<>();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_TEAM_PLAYERS,
            null,
            null,
            null,
            null,
            null,
            DatabaseHelper.TEAM_PLAYERS_COLUMN_JERSEY_NUMBER + " ASC"
        );
        
        while (cursor.moveToNext()) {
            teamPlayers.add(fromCursor(cursor));
        }
        cursor.close();
        
        Log.d(TAG, "Loaded " + teamPlayers.size() + " team players");
        return teamPlayers;
    }
    
    /**
     * Get total count of all team players
     */
    public static int getTotalCount(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TEAM_PLAYERS, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        
        return count;
    }
}
