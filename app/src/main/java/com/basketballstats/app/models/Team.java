package com.basketballstats.app.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.basketballstats.app.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Team model for league teams with SQLite persistence
 * 
 * Enhanced with CRUD operations for SQLite-primary architecture
 * Supports sync metadata for Firebase synchronization
 */
public class Team {
    private static final String TAG = "Team";
    
    // Core fields
    private int id;
    private String name;
    private List<TeamPlayer> players;
    
    // Sync fields
    private String createdAt;
    private String updatedAt;
    private String firebaseId;
    private String syncStatus;
    private String lastSyncTimestamp;
    
    // Constructors
    public Team() {
        this.players = new ArrayList<>();
        this.syncStatus = "local";
    }
    
    public Team(int id, String name) {
        this.id = id;
        this.name = name;
        this.players = new ArrayList<>();
        this.syncStatus = "local";
    }
    
    public Team(String name) {
        this.name = name;
        this.players = new ArrayList<>();
        this.syncStatus = "local";
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public List<TeamPlayer> getPlayers() { return players; }
    public void setPlayers(List<TeamPlayer> players) { this.players = players; }
    
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
     * Add player to roster (in-memory only, call save() to persist)
     */
    public void addPlayer(TeamPlayer player) {
        this.players.add(player);
    }
    
    /**
     * Get player by ID from current roster
     */
    public TeamPlayer getPlayerById(int playerId) {
        for (TeamPlayer player : players) {
            if (player.getId() == playerId) {
                return player;
            }
        }
        return null;
    }
    
    /**
     * Get player count
     */
    public int getPlayerCount() {
        return players.size();
    }
    
    /**
     * Display format for dropdown
     */
    @Override
    public String toString() {
        return name;
    }
    
    // ========== SQLITE CRUD OPERATIONS ==========
    
    /**
     * Save team to database (INSERT or UPDATE)
     */
    public long save(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.TEAMS_COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentTimestamp());
        values.put(DatabaseHelper.COLUMN_FIREBASE_ID, firebaseId);
        values.put(DatabaseHelper.COLUMN_SYNC_STATUS, syncStatus);
        values.put(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP, lastSyncTimestamp);
        
        long result;
        if (id > 0) {
            // UPDATE existing team
            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(id)};
            result = db.update(DatabaseHelper.TABLE_TEAMS, values, whereClause, whereArgs);
            Log.d(TAG, "Updated team: " + name + " (ID: " + id + ")");
        } else {
            // INSERT new team
            values.put(DatabaseHelper.COLUMN_CREATED_AT, getCurrentTimestamp());
            result = db.insert(DatabaseHelper.TABLE_TEAMS, null, values);
            if (result != -1) {
                this.id = (int) result;
                Log.d(TAG, "Created team: " + name + " (ID: " + id + ")");
            }
        }
        
        return result;
    }
    
    /**
     * Delete team from database
     */
    public boolean delete(DatabaseHelper dbHelper) {
        if (id <= 0) {
            Log.w(TAG, "Cannot delete team with invalid ID: " + id);
            return false;
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        
        int rowsAffected = db.delete(DatabaseHelper.TABLE_TEAMS, whereClause, whereArgs);
        boolean success = rowsAffected > 0;
        
        if (success) {
            Log.d(TAG, "Deleted team: " + name + " (ID: " + id + ")");
        } else {
            Log.w(TAG, "Failed to delete team: " + name + " (ID: " + id + ")");
        }
        
        return success;
    }
    
    /**
     * Load team from database by ID
     */
    public static Team findById(DatabaseHelper dbHelper, int teamId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(teamId)};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_TEAMS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        Team team = null;
        if (cursor.moveToFirst()) {
            team = fromCursor(cursor);
        }
        cursor.close();
        
        return team;
    }
    
    /**
     * Load team from database by name
     */
    public static Team findByName(DatabaseHelper dbHelper, String teamName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.TEAMS_COLUMN_NAME + " = ?";
        String[] selectionArgs = {teamName};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_TEAMS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        Team team = null;
        if (cursor.moveToFirst()) {
            team = fromCursor(cursor);
        }
        cursor.close();
        
        return team;
    }
    
    /**
     * Get all teams from database
     */
    public static List<Team> findAll(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Team> teams = new ArrayList<>();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_TEAMS,
            null,
            null,
            null,
            null,
            null,
            DatabaseHelper.TEAMS_COLUMN_NAME + " ASC"
        );
        
        while (cursor.moveToNext()) {
            teams.add(fromCursor(cursor));
        }
        cursor.close();
        
        Log.d(TAG, "Loaded " + teams.size() + " teams from database");
        return teams;
    }
    
    /**
     * Get teams that need syncing
     */
    public static List<Team> findPendingSync(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Team> teams = new ArrayList<>();
        
        String selection = DatabaseHelper.COLUMN_SYNC_STATUS + " IN (?, ?)";
        String[] selectionArgs = {"local", "pending"};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_TEAMS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseHelper.COLUMN_UPDATED_AT + " ASC"
        );
        
        while (cursor.moveToNext()) {
            teams.add(fromCursor(cursor));
        }
        cursor.close();
        
        return teams;
    }
    
    /**
     * Load players for this team
     */
    public void loadPlayers(DatabaseHelper dbHelper) {
        if (id <= 0) {
            Log.w(TAG, "Cannot load players for team with invalid ID: " + id);
            return;
        }
        
        this.players = TeamPlayer.findByTeamId(dbHelper, id);
        Log.d(TAG, "Loaded " + players.size() + " players for team: " + name);
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
        
        db.update(DatabaseHelper.TABLE_TEAMS, values, whereClause, whereArgs);
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Create Team object from database cursor
     */
    private static Team fromCursor(Cursor cursor) {
        Team team = new Team();
        
        team.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        team.name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TEAMS_COLUMN_NAME));
        team.createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT));
        team.updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATED_AT));
        team.firebaseId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIREBASE_ID));
        team.syncStatus = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SYNC_STATUS));
        team.lastSyncTimestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP));
        
        return team;
    }
    
    /**
     * Get current timestamp as string
     */
    private static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Check if team exists in database
     */
    public static boolean exists(DatabaseHelper dbHelper, String teamName) {
        return findByName(dbHelper, teamName) != null;
    }
    
    /**
     * Get team count
     */
    public static int getCount(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TEAMS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    /**
     * Find team by Firebase ID (for sync operations)
     */
    public static Team findByFirebaseId(DatabaseHelper dbHelper, String firebaseId) {
        if (firebaseId == null) {
            return null;
        }
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_FIREBASE_ID + " = ?";
        String[] selectionArgs = {firebaseId};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_TEAMS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        Team team = null;
        if (cursor.moveToFirst()) {
            team = fromCursor(cursor);
        }
        cursor.close();
        
        return team;
    }
    
    // ========== OBJECT METHODS ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Team team = (Team) obj;
        return id == team.id && 
               (name != null ? name.equals(team.name) : team.name == null);
    }
    
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
