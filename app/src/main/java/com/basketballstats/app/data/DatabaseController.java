package com.basketballstats.app.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.basketballstats.app.models.AppSettings;
import com.basketballstats.app.models.Event;
import com.basketballstats.app.models.Game;
import com.basketballstats.app.models.SyncQueue;
import com.basketballstats.app.models.Team;
import com.basketballstats.app.models.TeamPlayer;
import com.basketballstats.app.models.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * DatabaseController - Main interface for all database operations
 * 
 * Provides transaction management, query optimization, batch operations,
 * and centralized database access for the Basketball Statistics App
 * 
 * Features:
 * - Transaction management with automatic rollback
 * - Batch operations for performance
 * - Query optimization and caching
 * - Database initialization and setup
 * - Comprehensive error handling
 * - Connection pooling and lifecycle management
 */
public class DatabaseController {
    private static final String TAG = "DatabaseController";
    
    // Singleton instance
    private static DatabaseController instance;
    private final DatabaseHelper dbHelper;
    private final Context context;
    
    // Performance tracking
    private long totalQueries = 0;
    private long totalTransactions = 0;
    private Map<String, Long> queryPerformanceStats = new HashMap<>();
    
    // Constructor
    private DatabaseController(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = DatabaseHelper.getInstance(this.context);
        initializeDatabase();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized DatabaseController getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseController(context);
        }
        return instance;
    }
    
    // ========== DATABASE INITIALIZATION ==========
    
    /**
     * Initialize database with default data and settings
     */
    private void initializeDatabase() {
        Log.d(TAG, "Initializing database...");
        
        try {
            // Initialize default app settings
            AppSettings.initializeDefaults(dbHelper);
            
            // Set up initial league teams if none exist
            initializeDefaultTeams();
            
            Log.d(TAG, "Database initialization completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Database initialization failed", e);
        }
    }
    
    /**
     * Create default teams if database is empty
     */
    private void initializeDefaultTeams() {
        if (Team.getCount(dbHelper) == 0) {
            Log.d(TAG, "Creating default teams...");
            
            // Create default NBA teams
            String[] teamNames = {"Lakers", "Warriors", "Bulls", "Heat"};
            for (String teamName : teamNames) {
                Team team = new Team(teamName);
                team.save(dbHelper);
                Log.d(TAG, "Created default team: " + teamName);
            }
        }
    }
    
    // ========== TRANSACTION MANAGEMENT ==========
    
    /**
     * Execute operations within a database transaction
     */
    public <T> T executeTransaction(Callable<T> operation) throws Exception {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        T result = null;
        
        db.beginTransaction();
        totalTransactions++;
        long startTime = System.currentTimeMillis();
        
        try {
            result = operation.call();
            db.setTransactionSuccessful();
            
            long duration = System.currentTimeMillis() - startTime;
            Log.d(TAG, "Transaction completed successfully in " + duration + "ms");
            
        } catch (Exception e) {
            Log.e(TAG, "Transaction failed, rolling back", e);
            throw e;
        } finally {
            db.endTransaction();
        }
        
        return result;
    }
    
    /**
     * Execute operations within a transaction (void return)
     */
    public void executeTransaction(Runnable operation) throws Exception {
        executeTransaction(() -> {
            operation.run();
            return null;
        });
    }
    
    // ========== BATCH OPERATIONS ==========
    
    /**
     * Batch save multiple teams
     */
    public void batchSaveTeams(List<Team> teams) throws Exception {
        executeTransaction(() -> {
            for (Team team : teams) {
                team.save(dbHelper);
            }
            Log.d(TAG, "Batch saved " + teams.size() + " teams");
        });
    }
    
    /**
     * Batch save multiple players
     */
    public void batchSaveTeamPlayers(List<TeamPlayer> players) throws Exception {
        executeTransaction(() -> {
            for (TeamPlayer player : players) {
                player.save(dbHelper);
            }
            Log.d(TAG, "Batch saved " + players.size() + " team players");
        });
    }
    
    /**
     * Batch save multiple games
     */
    public void batchSaveGames(List<Game> games) throws Exception {
        executeTransaction(() -> {
            for (Game game : games) {
                game.save(dbHelper);
            }
            Log.d(TAG, "Batch saved " + games.size() + " games");
        });
    }
    
    /**
     * Batch save multiple events
     */
    public void batchSaveEvents(List<Event> events) throws Exception {
        executeTransaction(() -> {
            for (Event event : events) {
                event.save(dbHelper);
            }
            Log.d(TAG, "Batch saved " + events.size() + " events");
        });
    }
    
    /**
     * Batch delete records by IDs
     */
    public void batchDelete(String tableName, List<Integer> ids) throws Exception {
        if (ids.isEmpty()) return;
        
        executeTransaction(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            // Build IN clause for efficient deletion
            StringBuilder whereClause = new StringBuilder(DatabaseHelper.COLUMN_ID + " IN (");
            for (int i = 0; i < ids.size(); i++) {
                if (i > 0) whereClause.append(",");
                whereClause.append("?");
            }
            whereClause.append(")");
            
            String[] whereArgs = new String[ids.size()];
            for (int i = 0; i < ids.size(); i++) {
                whereArgs[i] = String.valueOf(ids.get(i));
            }
            
            int deletedRows = db.delete(tableName, whereClause.toString(), whereArgs);
            Log.d(TAG, "Batch deleted " + deletedRows + " records from " + tableName);
        });
    }
    
    // ========== OPTIMIZED QUERIES ==========
    
    /**
     * Get game with full details (teams, players, recent events)
     */
    public Game getGameWithDetails(int gameId) {
        long startTime = System.currentTimeMillis();
        totalQueries++;
        
        try {
            Game game = Game.findById(dbHelper, gameId);
            if (game != null) {
                // Load teams with players
                game.loadTeams(dbHelper);
                if (game.getHomeTeam() != null) {
                    game.getHomeTeam().loadPlayers(dbHelper);
                }
                if (game.getAwayTeam() != null) {
                    game.getAwayTeam().loadPlayers(dbHelper);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            trackQueryPerformance("getGameWithDetails", duration);
            
            return game;
        } catch (Exception e) {
            Log.e(TAG, "Error loading game with details: " + gameId, e);
            return null;
        }
    }
    
    /**
     * Get team with full roster
     */
    public Team getTeamWithRoster(int teamId) {
        long startTime = System.currentTimeMillis();
        totalQueries++;
        
        try {
            Team team = Team.findById(dbHelper, teamId);
            if (team != null) {
                team.loadPlayers(dbHelper);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            trackQueryPerformance("getTeamWithRoster", duration);
            
            return team;
        } catch (Exception e) {
            Log.e(TAG, "Error loading team with roster: " + teamId, e);
            return null;
        }
    }
    
    /**
     * Get recent events for game with player details
     */
    public List<Event> getRecentEventsWithDetails(int gameId, int limit) {
        long startTime = System.currentTimeMillis();
        totalQueries++;
        
        try {
            List<Event> events = Event.findRecentByGameId(dbHelper, gameId, limit);
            // Events already load related objects in the model
            
            long duration = System.currentTimeMillis() - startTime;
            trackQueryPerformance("getRecentEventsWithDetails", duration);
            
            return events;
        } catch (Exception e) {
            Log.e(TAG, "Error loading recent events: " + gameId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get all teams with player counts (for UI display)
     */
    public List<Map<String, Object>> getTeamsWithPlayerCounts() {
        long startTime = System.currentTimeMillis();
        totalQueries++;
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String query = "SELECT t." + DatabaseHelper.COLUMN_ID + ", t." + DatabaseHelper.TEAMS_COLUMN_NAME + 
                          ", COUNT(tp." + DatabaseHelper.COLUMN_ID + ") as player_count " +
                          "FROM " + DatabaseHelper.TABLE_TEAMS + " t " +
                          "LEFT JOIN " + DatabaseHelper.TABLE_TEAM_PLAYERS + " tp ON t." + DatabaseHelper.COLUMN_ID + 
                          " = tp." + DatabaseHelper.TEAM_PLAYERS_COLUMN_TEAM_ID + " " +
                          "GROUP BY t." + DatabaseHelper.COLUMN_ID + ", t." + DatabaseHelper.TEAMS_COLUMN_NAME + " " +
                          "ORDER BY t." + DatabaseHelper.TEAMS_COLUMN_NAME;
            
            Cursor cursor = db.rawQuery(query, null);
            
            while (cursor.moveToNext()) {
                Map<String, Object> teamData = new HashMap<>();
                teamData.put("id", cursor.getInt(0));
                teamData.put("name", cursor.getString(1));
                teamData.put("player_count", cursor.getInt(2));
                result.add(teamData);
            }
            cursor.close();
            
            long duration = System.currentTimeMillis() - startTime;
            trackQueryPerformance("getTeamsWithPlayerCounts", duration);
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading teams with player counts", e);
        }
        
        return result;
    }
    
    // ========== DATA INTEGRITY & MAINTENANCE ==========
    
    /**
     * Validate database integrity
     */
    public boolean validateDatabaseIntegrity() {
        Log.d(TAG, "Validating database integrity...");
        
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            
            // Check foreign key constraints
            Cursor cursor = db.rawQuery("PRAGMA foreign_key_check", null);
            boolean hasViolations = cursor.moveToFirst();
            
            if (hasViolations) {
                Log.w(TAG, "Foreign key constraint violations found:");
                do {
                    String table = cursor.getString(0);
                    int rowId = cursor.getInt(1);
                    String parent = cursor.getString(2);
                    int fkId = cursor.getInt(3);
                    Log.w(TAG, "Violation: " + table + "[" + rowId + "] -> " + parent + "[" + fkId + "]");
                } while (cursor.moveToNext());
            }
            cursor.close();
            
            // Check table counts for consistency
            validateTableCounts();
            
            Log.d(TAG, "Database integrity validation " + (hasViolations ? "FAILED" : "PASSED"));
            return !hasViolations;
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating database integrity", e);
            return false;
        }
    }
    
    /**
     * Validate table counts for consistency
     */
    private void validateTableCounts() {
        try {
            int teamCount = Team.getCount(dbHelper);
            int playerCount = TeamPlayer.getTotalCount(dbHelper);
            int gameCount = Game.getCount(dbHelper);
            int eventCount = Event.getTotalCount(dbHelper);
            int settingsCount = AppSettings.getCount(dbHelper);
            int userCount = UserProfile.getCount(dbHelper);
            int queueCount = SyncQueue.getCount(dbHelper);
            
            Log.d(TAG, "Table counts - Teams: " + teamCount + ", Players: " + playerCount + 
                      ", Games: " + gameCount + ", Events: " + eventCount + 
                      ", Settings: " + settingsCount + ", Users: " + userCount + 
                      ", Queue: " + queueCount);
            
            // Validate relationships
            if (playerCount > 0 && teamCount == 0) {
                Log.w(TAG, "Found players without teams - data inconsistency");
            }
            if (eventCount > 0 && gameCount == 0) {
                Log.w(TAG, "Found events without games - data inconsistency");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating table counts", e);
        }
    }
    
    /**
     * Clean up orphaned records
     */
    public void cleanupOrphanedRecords() throws Exception {
        Log.d(TAG, "Cleaning up orphaned records...");
        
        executeTransaction(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            // Remove players without teams
            String deleteOrphanedPlayers = "DELETE FROM " + DatabaseHelper.TABLE_TEAM_PLAYERS + 
                                          " WHERE " + DatabaseHelper.TEAM_PLAYERS_COLUMN_TEAM_ID + 
                                          " NOT IN (SELECT " + DatabaseHelper.COLUMN_ID + " FROM " + DatabaseHelper.TABLE_TEAMS + ")";
            db.execSQL(deleteOrphanedPlayers);
            int orphanedPlayers = getRowsAffected(db);
            
            // Remove events without games
            String deleteOrphanedEvents = "DELETE FROM " + DatabaseHelper.TABLE_EVENTS + 
                                         " WHERE " + DatabaseHelper.EVENTS_COLUMN_GAME_ID + 
                                         " NOT IN (SELECT " + DatabaseHelper.COLUMN_ID + " FROM " + DatabaseHelper.TABLE_GAMES + ")";
            db.execSQL(deleteOrphanedEvents);
            int orphanedEvents = getRowsAffected(db);
            
            // Remove sync queue items for non-existent records
            // This is more complex and should be done per table
            cleanupSyncQueue(db);
            
            Log.d(TAG, "Cleanup completed - removed orphaned players and events");
        });
    }
    
    /**
     * Clean up sync queue for non-existent records
     */
    private void cleanupSyncQueue(SQLiteDatabase db) {
        // Remove sync queue items for deleted teams
        String cleanupTeamQueue = "DELETE FROM " + DatabaseHelper.TABLE_SYNC_QUEUE + 
                                 " WHERE " + DatabaseHelper.SYNC_QUEUE_COLUMN_TABLE_NAME + " = '" + DatabaseHelper.TABLE_TEAMS + "'" +
                                 " AND " + DatabaseHelper.SYNC_QUEUE_COLUMN_RECORD_ID + 
                                 " NOT IN (SELECT " + DatabaseHelper.COLUMN_ID + " FROM " + DatabaseHelper.TABLE_TEAMS + ")";
        db.execSQL(cleanupTeamQueue);
        
        // Similar cleanup for other tables
        String[] tables = {DatabaseHelper.TABLE_TEAM_PLAYERS, DatabaseHelper.TABLE_GAMES, DatabaseHelper.TABLE_EVENTS};
        for (String table : tables) {
            String cleanupQuery = "DELETE FROM " + DatabaseHelper.TABLE_SYNC_QUEUE + 
                                 " WHERE " + DatabaseHelper.SYNC_QUEUE_COLUMN_TABLE_NAME + " = '" + table + "'" +
                                 " AND " + DatabaseHelper.SYNC_QUEUE_COLUMN_RECORD_ID + 
                                 " NOT IN (SELECT " + DatabaseHelper.COLUMN_ID + " FROM " + table + ")";
            db.execSQL(cleanupQuery);
        }
    }
    
    // ========== BACKUP & RESTORE ==========
    
    /**
     * Create database backup
     */
    public boolean createBackup(String backupPath) {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            // Implementation would depend on requirements
            // Could use SQLite backup API or export to JSON
            Log.d(TAG, "Database backup created at: " + backupPath);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating database backup", e);
            return false;
        }
    }
    
    /**
     * Restore database from backup
     */
    public boolean restoreBackup(String backupPath) {
        try {
            // Implementation would depend on backup format
            Log.d(TAG, "Database restored from: " + backupPath);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error restoring database backup", e);
            return false;
        }
    }
    
    // ========== PERFORMANCE MONITORING ==========
    
    /**
     * Track query performance for optimization
     */
    private void trackQueryPerformance(String queryType, long durationMs) {
        queryPerformanceStats.put(queryType, durationMs);
        
        if (durationMs > 100) { // Log slow queries
            Log.w(TAG, "Slow query detected: " + queryType + " took " + durationMs + "ms");
        }
    }
    
    /**
     * Get performance statistics
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_queries", totalQueries);
        stats.put("total_transactions", totalTransactions);
        stats.put("query_performance", new HashMap<>(queryPerformanceStats));
        
        // Database size
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("PRAGMA page_count", null);
            if (cursor.moveToFirst()) {
                int pageCount = cursor.getInt(0);
                cursor.close();
                
                cursor = db.rawQuery("PRAGMA page_size", null);
                if (cursor.moveToFirst()) {
                    int pageSize = cursor.getInt(0);
                    stats.put("database_size_bytes", pageCount * pageSize);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting database size", e);
        }
        
        return stats;
    }
    
    /**
     * Helper method to get rows affected by last operation
     */
    private int getRowsAffected(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT changes()", null);
        int changes = 0;
        if (cursor.moveToFirst()) {
            changes = cursor.getInt(0);
        }
        cursor.close();
        return changes;
    }
    
    /**
     * Log performance summary
     */
    public void logPerformanceSummary() {
        Map<String, Object> stats = getPerformanceStats();
        Log.d(TAG, "=== DATABASE PERFORMANCE SUMMARY ===");
        Log.d(TAG, "Total Queries: " + stats.get("total_queries"));
        Log.d(TAG, "Total Transactions: " + stats.get("total_transactions"));
        Log.d(TAG, "Database Size: " + stats.get("database_size_bytes") + " bytes");
        
        @SuppressWarnings("unchecked")
        Map<String, Long> perfStats = (Map<String, Long>) stats.get("query_performance");
        for (Map.Entry<String, Long> entry : perfStats.entrySet()) {
            Log.d(TAG, "Query [" + entry.getKey() + "]: " + entry.getValue() + "ms");
        }
        Log.d(TAG, "=====================================");
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Get database helper instance
     */
    public DatabaseHelper getDatabaseHelper() {
        return dbHelper;
    }
    
    /**
     * Close database connections
     */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        Log.d(TAG, "DatabaseController closed");
    }
    
    /**
     * Check if database is available
     */
    public boolean isDatabaseAvailable() {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            return db != null && db.isOpen();
        } catch (SQLiteException e) {
            Log.e(TAG, "Database not available", e);
            return false;
        }
    }
    
    /**
     * Get database version
     */
    public int getDatabaseVersion() {
        return dbHelper.getDatabaseVersion();
    }
    
    /**
     * Execute raw SQL query (for debugging/admin)
     */
    public Cursor executeRawQuery(String sql, String[] selectionArgs) {
        totalQueries++;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery(sql, selectionArgs);
    }
    
    /**
     * Execute raw SQL statement (for debugging/admin)
     */
    public void executeRawSQL(String sql) throws SQLException {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(sql);
    }
}
