package com.basketballstats.app.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.basketballstats.app.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Game model with SQLite persistence for comprehensive game management
 * 
 * Enhanced with CRUD operations for SQLite-primary architecture
 * Supports live game state, scores, and sync metadata for Firebase synchronization
 */
public class Game {
    private static final String TAG = "Game";
    
    // Core fields
    private int id;
    private String date; // DD/MM/YYYY format
    private String time; // HH:MM format (24-hour)
    private int homeTeamId;
    private int awayTeamId;
    private String status; // "not_started", "game_in_progress", "done"
    private int homeScore;
    private int awayScore;
    private int currentQuarter; // 1-4
    private int gameClockSeconds; // 600 seconds = 10 minutes
    private boolean isClockRunning;
    
    // Team objects (loaded separately)
    private Team homeTeam;
    private Team awayTeam;
    
    // Sync fields
    private String createdAt;
    private String updatedAt;
    private String firebaseId;
    private String syncStatus;
    private String lastSyncTimestamp;
    
    // Constructors
    public Game() {
        this.status = "not_started"; // Updated for 3-state system
        this.homeScore = 0;
        this.awayScore = 0;
        this.currentQuarter = 1;
        this.gameClockSeconds = 600; // 10 minutes
        this.isClockRunning = false;
        this.syncStatus = "local";
    }
    
    public Game(int id, String date, int homeTeamId, int awayTeamId) {
        this();
        this.id = id;
        this.date = date;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
    }
    
    public Game(String date, String time, int homeTeamId, int awayTeamId) {
        this();
        this.date = date;
        this.time = time;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    
    public int getHomeTeamId() { return homeTeamId; }
    public void setHomeTeamId(int homeTeamId) { this.homeTeamId = homeTeamId; }
    
    public int getAwayTeamId() { return awayTeamId; }
    public void setAwayTeamId(int awayTeamId) { this.awayTeamId = awayTeamId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        this.updatedAt = getCurrentTimestamp();
    }
    
    public int getHomeScore() { return homeScore; }
    public void setHomeScore(int homeScore) { this.homeScore = homeScore; }
    
    public int getAwayScore() { return awayScore; }
    public void setAwayScore(int awayScore) { this.awayScore = awayScore; }
    
    public int getCurrentQuarter() { return currentQuarter; }
    public void setCurrentQuarter(int currentQuarter) { this.currentQuarter = currentQuarter; }
    
    public int getGameClockSeconds() { return gameClockSeconds; }
    public void setGameClockSeconds(int gameClockSeconds) { this.gameClockSeconds = gameClockSeconds; }
    
    public boolean isClockRunning() { return isClockRunning; }
    public void setClockRunning(boolean clockRunning) { isClockRunning = clockRunning; }
    
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }
    
    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }
    
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
    
    // Convenience methods for long timestamps
    public long getUpdatedAtLong() { 
        try {
            return updatedAt != null ? Long.parseLong(updatedAt) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = String.valueOf(updatedAt); }
    
    public long getCreatedAtLong() { 
        try {
            return createdAt != null ? Long.parseLong(createdAt) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public void setCreatedAt(long createdAt) { this.createdAt = String.valueOf(createdAt); }
    
    public long getLastSyncTimestampLong() { 
        try {
            return lastSyncTimestamp != null ? Long.parseLong(lastSyncTimestamp) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public void setLastSyncTimestamp(long lastSyncTimestamp) { this.lastSyncTimestamp = String.valueOf(lastSyncTimestamp); }
    
    // ========== BUSINESS LOGIC METHODS ==========
    
    /**
     * Get formatted clock time (MM:SS)
     */
    public String getFormattedClock() {
        int minutes = gameClockSeconds / 60;
        int seconds = gameClockSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    /**
     * Get quarter display (Q1, Q2, Q3, Q4)
     */
    public String getQuarterDisplay() {
        return "Q" + currentQuarter;
    }
    
    /**
     * Check if game has not started yet
     */
    public boolean isNotStarted() {
        return "not_started".equals(status);
    }
    
    /**
     * Check if game is currently in progress
     */
    public boolean isGameInProgress() {
        return "game_in_progress".equals(status);
    }
    
    /**
     * Check if game is completed/done
     */
    public boolean isDone() {
        return "done".equals(status);
    }
    
    // ========== LEGACY COMPATIBILITY METHODS ==========
    
    /**
     * @deprecated Use isGameInProgress() instead
     */
    @Deprecated
    public boolean isInProgress() {
        return isGameInProgress();
    }
    
    /**
     * @deprecated Use isDone() instead
     */
    @Deprecated
    public boolean isCompleted() {
        return isDone();
    }
    
    /**
     * @deprecated Use isNotStarted() instead
     */
    @Deprecated
    public boolean isScheduled() {
        return isNotStarted();
    }
    
    // ========== STATUS TRANSITION METHODS ==========
    
    /**
     * Transition game to 'not_started' status
     * Used when clearing all events and resetting game
     */
    public void setToNotStarted() {
        this.status = "not_started";
        this.updatedAt = getCurrentTimestamp();
    }
    
    /**
     * Transition game to 'game_in_progress' status
     * Used when both teams select 5 players and game begins
     */
    public void setToGameInProgress() {
        this.status = "game_in_progress";
        this.updatedAt = getCurrentTimestamp();
    }
    
    /**
     * Transition game to 'done' status
     * Used when Q4 timer ends or manual end game
     */
    public void setToDone() {
        this.status = "done";
        this.updatedAt = getCurrentTimestamp();
    }
    
    /**
     * Check if status value is valid for 3-state system
     */
    public static boolean isValidStatus(String status) {
        return "not_started".equals(status) || 
               "game_in_progress".equals(status) || 
               "done".equals(status);
    }
    
    /**
     * Display format for list (backward compatibility)
     */
    @Override
    public String toString() {
        String homeTeamName = homeTeam != null ? homeTeam.getName() : "Team " + homeTeamId;
        String awayTeamName = awayTeam != null ? awayTeam.getName() : "Team " + awayTeamId;
        return homeTeamName + " vs " + awayTeamName + " - " + date;
    }
    
    /**
     * Get game display with score
     */
    public String getDisplayWithScore() {
        String homeTeamName = homeTeam != null ? homeTeam.getName() : "Team " + homeTeamId;
        String awayTeamName = awayTeam != null ? awayTeam.getName() : "Team " + awayTeamId;
        
        if (isGameInProgress() || isDone()) {
            return homeTeamName + " " + homeScore + " - " + awayScore + " " + awayTeamName;
        } else {
            return homeTeamName + " vs " + awayTeamName;
        }
    }
    
    // ========== SQLITE CRUD OPERATIONS ==========
    
    /**
     * Save game to database (INSERT or UPDATE)
     */
    public long save(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.GAMES_COLUMN_DATE, date);
        values.put(DatabaseHelper.GAMES_COLUMN_TIME, time);
        values.put(DatabaseHelper.GAMES_COLUMN_HOME_TEAM_ID, homeTeamId);
        values.put(DatabaseHelper.GAMES_COLUMN_AWAY_TEAM_ID, awayTeamId);
        values.put(DatabaseHelper.GAMES_COLUMN_STATUS, status);
        values.put(DatabaseHelper.GAMES_COLUMN_HOME_SCORE, homeScore);
        values.put(DatabaseHelper.GAMES_COLUMN_AWAY_SCORE, awayScore);
        values.put(DatabaseHelper.GAMES_COLUMN_CURRENT_QUARTER, currentQuarter);
        values.put(DatabaseHelper.GAMES_COLUMN_GAME_CLOCK_SECONDS, gameClockSeconds);
        values.put(DatabaseHelper.GAMES_COLUMN_IS_CLOCK_RUNNING, isClockRunning ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentTimestamp());
        values.put(DatabaseHelper.COLUMN_FIREBASE_ID, firebaseId);
        values.put(DatabaseHelper.COLUMN_SYNC_STATUS, syncStatus);
        values.put(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP, lastSyncTimestamp);
        
        long result;
        if (id > 0) {
            // UPDATE existing game
            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(id)};
            result = db.update(DatabaseHelper.TABLE_GAMES, values, whereClause, whereArgs);
            Log.d(TAG, "Updated game: " + toString() + " (ID: " + id + ")");
        } else {
            // INSERT new game
            values.put(DatabaseHelper.COLUMN_CREATED_AT, getCurrentTimestamp());
            result = db.insert(DatabaseHelper.TABLE_GAMES, null, values);
            if (result != -1) {
                this.id = (int) result;
                Log.d(TAG, "Created game: " + toString() + " (ID: " + id + ")");
            }
        }
        
        return result;
    }
    
    /**
     * Delete game from database
     */
    public boolean delete(DatabaseHelper dbHelper) {
        if (id <= 0) {
            Log.w(TAG, "Cannot delete game with invalid ID: " + id);
            return false;
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        
        int rowsAffected = db.delete(DatabaseHelper.TABLE_GAMES, whereClause, whereArgs);
        boolean success = rowsAffected > 0;
        
        if (success) {
            Log.d(TAG, "Deleted game: " + toString() + " (ID: " + id + ")");
        } else {
            Log.w(TAG, "Failed to delete game: " + toString() + " (ID: " + id + ")");
        }
        
        return success;
    }
    
    /**
     * Load game from database by ID
     */
    public static Game findById(DatabaseHelper dbHelper, int gameId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(gameId)};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_GAMES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        Game game = null;
        if (cursor.moveToFirst()) {
            game = fromCursor(cursor);
            game.loadTeams(dbHelper);
        }
        cursor.close();
        
        return game;
    }
    
    /**
     * Get all games from database
     */
    public static List<Game> findAll(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Game> games = new ArrayList<>();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_GAMES,
            null,
            null,
            null,
            null,
            null,
            DatabaseHelper.GAMES_COLUMN_DATE + " ASC, " + DatabaseHelper.GAMES_COLUMN_TIME + " ASC"
        );
        
        while (cursor.moveToNext()) {
            Game game = fromCursor(cursor);
            game.loadTeams(dbHelper);
            games.add(game);
        }
        cursor.close();
        
        Log.d(TAG, "Loaded " + games.size() + " games from database");
        return games;
    }
    
    /**
     * Get games by status
     */
    public static List<Game> findByStatus(DatabaseHelper dbHelper, String status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Game> games = new ArrayList<>();
        
        String selection = DatabaseHelper.GAMES_COLUMN_STATUS + " = ?";
        String[] selectionArgs = {status};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_GAMES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseHelper.GAMES_COLUMN_DATE + " ASC, " + DatabaseHelper.GAMES_COLUMN_TIME + " ASC"
        );
        
        while (cursor.moveToNext()) {
            Game game = fromCursor(cursor);
            game.loadTeams(dbHelper);
            games.add(game);
        }
        cursor.close();
        
        return games;
    }
    
    /**
     * Get games that need syncing
     */
    public static List<Game> findPendingSync(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Game> games = new ArrayList<>();
        
        String selection = DatabaseHelper.COLUMN_SYNC_STATUS + " IN (?, ?)";
        String[] selectionArgs = {"local", "pending"};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_GAMES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseHelper.COLUMN_UPDATED_AT + " ASC"
        );
        
        while (cursor.moveToNext()) {
            Game game = fromCursor(cursor);
            game.loadTeams(dbHelper);
            games.add(game);
        }
        cursor.close();
        
        return games;
    }
    
    /**
     * Load team objects for this game
     */
    public void loadTeams(DatabaseHelper dbHelper) {
        if (homeTeamId > 0) {
            this.homeTeam = Team.findById(dbHelper, homeTeamId);
        }
        if (awayTeamId > 0) {
            this.awayTeam = Team.findById(dbHelper, awayTeamId);
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
        
        db.update(DatabaseHelper.TABLE_GAMES, values, whereClause, whereArgs);
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Create Game object from database cursor
     */
    private static Game fromCursor(Cursor cursor) {
        Game game = new Game();
        
        game.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        game.date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.GAMES_COLUMN_DATE));
        game.time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.GAMES_COLUMN_TIME));
        game.homeTeamId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAMES_COLUMN_HOME_TEAM_ID));
        game.awayTeamId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAMES_COLUMN_AWAY_TEAM_ID));
        game.status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.GAMES_COLUMN_STATUS));
        game.homeScore = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAMES_COLUMN_HOME_SCORE));
        game.awayScore = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAMES_COLUMN_AWAY_SCORE));
        game.currentQuarter = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAMES_COLUMN_CURRENT_QUARTER));
        game.gameClockSeconds = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAMES_COLUMN_GAME_CLOCK_SECONDS));
        game.isClockRunning = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.GAMES_COLUMN_IS_CLOCK_RUNNING)) == 1;
        game.createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT));
        game.updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATED_AT));
        game.firebaseId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIREBASE_ID));
        game.syncStatus = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SYNC_STATUS));
        game.lastSyncTimestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP));
        
        return game;
    }
    
    /**
     * Get current timestamp as string
     */
    private static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Get game count
     */
    public static int getCount(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_GAMES, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    /**
     * Find game by Firebase ID (for sync operations)
     */
    public static Game findByFirebaseId(DatabaseHelper dbHelper, String firebaseId) {
        if (firebaseId == null) {
            return null;
        }
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_FIREBASE_ID + " = ?";
        String[] selectionArgs = {firebaseId};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_GAMES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        Game game = null;
        if (cursor.moveToFirst()) {
            game = fromCursor(cursor);
            game.loadTeams(dbHelper);
        }
        cursor.close();
        
        return game;
    }
    
    // ========== OBJECT METHODS ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Game game = (Game) obj;
        return id == game.id && 
               homeTeamId == game.homeTeamId && 
               awayTeamId == game.awayTeamId &&
               (date != null ? date.equals(game.date) : game.date == null);
    }
    
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + homeTeamId;
        result = 31 * result + awayTeamId;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }
}
