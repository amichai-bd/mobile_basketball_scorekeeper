package com.basketballstats.app.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.basketballstats.app.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * GamePlayer model for persisting selected players for specific games
 * 
 * Bridges TeamPlayer (roster) and Game (specific game instance) for lineup management
 * Stores selected players with their game-specific state (on court, fouls, etc.)
 */
public class GamePlayer {
    private static final String TAG = "GamePlayer";
    
    // Core fields
    private int id;
    private int gameId;
    private int teamPlayerId;
    private String teamSide; // 'home' or 'away'
    private boolean isOnCourt;
    private boolean isStarter;
    private int personalFouls;
    private int minutesPlayed;
    
    // Sync fields
    private String createdAt;
    private String updatedAt;
    private String firebaseId;
    private String syncStatus;
    private String lastSyncTimestamp;
    
    // Related objects (loaded separately)
    private TeamPlayer teamPlayer;
    
    // Constructors
    public GamePlayer() {
        this.isOnCourt = true;
        this.isStarter = false;
        this.personalFouls = 0;
        this.minutesPlayed = 0;
        this.syncStatus = "local";
    }
    
    public GamePlayer(int gameId, int teamPlayerId, String teamSide) {
        this();
        this.gameId = gameId;
        this.teamPlayerId = teamPlayerId;
        this.teamSide = teamSide;
    }
    
    public GamePlayer(int gameId, int teamPlayerId, String teamSide, boolean isStarter) {
        this(gameId, teamPlayerId, teamSide);
        this.isStarter = isStarter;
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    
    public int getTeamPlayerId() { return teamPlayerId; }
    public void setTeamPlayerId(int teamPlayerId) { this.teamPlayerId = teamPlayerId; }
    
    public String getTeamSide() { return teamSide; }
    public void setTeamSide(String teamSide) { this.teamSide = teamSide; }
    
    public boolean isOnCourt() { return isOnCourt; }
    public void setOnCourt(boolean onCourt) { isOnCourt = onCourt; }
    
    public boolean isStarter() { return isStarter; }
    public void setStarter(boolean starter) { isStarter = starter; }
    
    public int getPersonalFouls() { return personalFouls; }
    public void setPersonalFouls(int personalFouls) { this.personalFouls = personalFouls; }
    
    public int getMinutesPlayed() { return minutesPlayed; }
    public void setMinutesPlayed(int minutesPlayed) { this.minutesPlayed = minutesPlayed; }
    
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
    
    public TeamPlayer getTeamPlayer() { return teamPlayer; }
    public void setTeamPlayer(TeamPlayer teamPlayer) { this.teamPlayer = teamPlayer; }
    
    // ========== BUSINESS LOGIC METHODS ==========
    
    /**
     * Add a personal foul to this player
     */
    public void addPersonalFoul() {
        this.personalFouls++;
        this.updatedAt = getCurrentTimestamp();
    }
    
    /**
     * Check if player has fouled out (5+ fouls)
     */
    public boolean isFouledOut() {
        return personalFouls >= 5;
    }
    
    /**
     * Get player display name with number
     */
    public String getDisplayName() {
        if (teamPlayer != null) {
            return "#" + teamPlayer.getNumber() + " " + teamPlayer.getName();
        }
        return "Player " + teamPlayerId;
    }
    
    /**
     * Get player jersey number
     */
    public int getJerseyNumber() {
        return teamPlayer != null ? teamPlayer.getNumber() : 0;
    }
    
    /**
     * Get player name
     */
    public String getPlayerName() {
        return teamPlayer != null ? teamPlayer.getName() : "Unknown";
    }
    
    // ========== SQLITE CRUD OPERATIONS ==========
    
    /**
     * Save game player to database (INSERT or UPDATE)
     */
    public long save(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.GAME_PLAYERS_COLUMN_GAME_ID, gameId);
        values.put(DatabaseHelper.GAME_PLAYERS_COLUMN_TEAM_PLAYER_ID, teamPlayerId);
        values.put(DatabaseHelper.GAME_PLAYERS_COLUMN_TEAM_SIDE, teamSide);
        values.put(DatabaseHelper.GAME_PLAYERS_COLUMN_IS_ON_COURT, isOnCourt ? 1 : 0);
        values.put(DatabaseHelper.GAME_PLAYERS_COLUMN_IS_STARTER, isStarter ? 1 : 0);
        values.put(DatabaseHelper.GAME_PLAYERS_COLUMN_PERSONAL_FOULS, personalFouls);
        values.put(DatabaseHelper.GAME_PLAYERS_COLUMN_MINUTES_PLAYED, minutesPlayed);
        values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentTimestamp());
        values.put(DatabaseHelper.COLUMN_FIREBASE_ID, firebaseId);
        values.put(DatabaseHelper.COLUMN_SYNC_STATUS, syncStatus);
        values.put(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP, lastSyncTimestamp);
        
        long result;
        if (id > 0) {
            // UPDATE existing game player
            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(id)};
            result = db.update(DatabaseHelper.TABLE_GAME_PLAYERS, values, whereClause, whereArgs);
            Log.d(TAG, "Updated game player: " + getDisplayName() + " (ID: " + id + ")");
        } else {
            // INSERT new game player
            values.put(DatabaseHelper.COLUMN_CREATED_AT, getCurrentTimestamp());
            result = db.insert(DatabaseHelper.TABLE_GAME_PLAYERS, null, values);
            if (result != -1) {
                this.id = (int) result;
                Log.d(TAG, "Created game player: " + getDisplayName() + " (ID: " + id + ")");
            }
        }
        
        return result;
    }
    
    /**
     * Delete game player from database
     */
    public boolean delete(DatabaseHelper dbHelper) {
        if (id <= 0) return false;
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        
        int deletedRows = db.delete(DatabaseHelper.TABLE_GAME_PLAYERS, whereClause, whereArgs);
        Log.d(TAG, "Deleted game player: " + getDisplayName() + " (ID: " + id + ")");
        
        return deletedRows > 0;
    }
    
    // ========== STATIC QUERY METHODS ==========
    
    /**
     * Find game player by ID
     */
    public static GamePlayer findById(DatabaseHelper dbHelper, int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_GAME_PLAYERS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        GamePlayer gamePlayer = null;
        if (cursor.moveToFirst()) {
            gamePlayer = fromCursor(cursor);
            gamePlayer.loadTeamPlayer(dbHelper);
        }
        cursor.close();
        
        return gamePlayer;
    }
    
    /**
     * Find all game players for a specific game
     */
    public static List<GamePlayer> findByGameId(DatabaseHelper dbHelper, int gameId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<GamePlayer> gamePlayers = new ArrayList<>();
        
        String selection = DatabaseHelper.GAME_PLAYERS_COLUMN_GAME_ID + " = ?";
        String[] selectionArgs = {String.valueOf(gameId)};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_GAME_PLAYERS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseHelper.GAME_PLAYERS_COLUMN_TEAM_SIDE + " ASC, " + 
            DatabaseHelper.GAME_PLAYERS_COLUMN_IS_STARTER + " DESC"
        );
        
        while (cursor.moveToNext()) {
            GamePlayer gamePlayer = fromCursor(cursor);
            gamePlayer.loadTeamPlayer(dbHelper);
            gamePlayers.add(gamePlayer);
        }
        cursor.close();
        
        Log.d(TAG, "Loaded " + gamePlayers.size() + " game players for game " + gameId);
        return gamePlayers;
    }
    
    /**
     * Find game players for a specific game and team side
     */
    public static List<GamePlayer> findByGameIdAndTeamSide(DatabaseHelper dbHelper, int gameId, String teamSide) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<GamePlayer> gamePlayers = new ArrayList<>();
        
        String selection = DatabaseHelper.GAME_PLAYERS_COLUMN_GAME_ID + " = ? AND " +
                          DatabaseHelper.GAME_PLAYERS_COLUMN_TEAM_SIDE + " = ?";
        String[] selectionArgs = {String.valueOf(gameId), teamSide};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_GAME_PLAYERS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseHelper.GAME_PLAYERS_COLUMN_IS_STARTER + " DESC"
        );
        
        while (cursor.moveToNext()) {
            GamePlayer gamePlayer = fromCursor(cursor);
            gamePlayer.loadTeamPlayer(dbHelper);
            gamePlayers.add(gamePlayer);
        }
        cursor.close();
        
        return gamePlayers;
    }
    
    /**
     * Find currently on-court players for a game and team side
     */
    public static List<GamePlayer> findOnCourtPlayers(DatabaseHelper dbHelper, int gameId, String teamSide) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<GamePlayer> gamePlayers = new ArrayList<>();
        
        String selection = DatabaseHelper.GAME_PLAYERS_COLUMN_GAME_ID + " = ? AND " +
                          DatabaseHelper.GAME_PLAYERS_COLUMN_TEAM_SIDE + " = ? AND " +
                          DatabaseHelper.GAME_PLAYERS_COLUMN_IS_ON_COURT + " = 1";
        String[] selectionArgs = {String.valueOf(gameId), teamSide};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_GAME_PLAYERS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseHelper.GAME_PLAYERS_COLUMN_IS_STARTER + " DESC"
        );
        
        while (cursor.moveToNext()) {
            GamePlayer gamePlayer = fromCursor(cursor);
            gamePlayer.loadTeamPlayer(dbHelper);
            gamePlayers.add(gamePlayer);
        }
        cursor.close();
        
        return gamePlayers;
    }
    
    /**
     * Delete all game players for a specific game
     * Used when clearing/resetting game
     */
    public static int deleteByGameId(DatabaseHelper dbHelper, int gameId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.GAME_PLAYERS_COLUMN_GAME_ID + " = ?";
        String[] whereArgs = {String.valueOf(gameId)};
        
        int deletedRows = db.delete(DatabaseHelper.TABLE_GAME_PLAYERS, whereClause, whereArgs);
        Log.d(TAG, "Deleted " + deletedRows + " game players for game " + gameId);
        
        return deletedRows;
    }
    
    /**
     * Load the associated TeamPlayer object
     */
    public void loadTeamPlayer(DatabaseHelper dbHelper) {
        if (teamPlayerId > 0) {
            this.teamPlayer = TeamPlayer.findById(dbHelper, teamPlayerId);
        }
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Create GamePlayer object from database cursor
     */
    private static GamePlayer fromCursor(Cursor cursor) {
        GamePlayer gamePlayer = new GamePlayer();
        
        gamePlayer.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        gamePlayer.gameId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAME_PLAYERS_COLUMN_GAME_ID));
        gamePlayer.teamPlayerId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAME_PLAYERS_COLUMN_TEAM_PLAYER_ID));
        gamePlayer.teamSide = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.GAME_PLAYERS_COLUMN_TEAM_SIDE));
        gamePlayer.isOnCourt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAME_PLAYERS_COLUMN_IS_ON_COURT)) == 1;
        gamePlayer.isStarter = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAME_PLAYERS_COLUMN_IS_STARTER)) == 1;
        gamePlayer.personalFouls = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAME_PLAYERS_COLUMN_PERSONAL_FOULS));
        gamePlayer.minutesPlayed = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAME_PLAYERS_COLUMN_MINUTES_PLAYED));
        gamePlayer.createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT));
        gamePlayer.updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATED_AT));
        gamePlayer.firebaseId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIREBASE_ID));
        gamePlayer.syncStatus = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SYNC_STATUS));
        gamePlayer.lastSyncTimestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP));
        
        return gamePlayer;
    }
    
    /**
     * Get current timestamp as string
     */
    private static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Convert to string for debugging
     */
    @Override
    public String toString() {
        return String.format("GamePlayer{id=%d, game=%d, player=%s, side=%s, onCourt=%b, fouls=%d}", 
                           id, gameId, getDisplayName(), teamSide, isOnCourt, personalFouls);
    }
}
