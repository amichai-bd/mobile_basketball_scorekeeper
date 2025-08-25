package com.basketballstats.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Enhanced DatabaseHelper for Basketball Statistics App
 * 
 * SQLite-Primary Architecture: SQLite as primary data store with Firebase sync
 * 
 * Database Schema:
 * - 9 tables: teams, team_players, games, game_players, events, team_fouls, 
 *   app_settings, user_profile, sync_queue, sync_log
 * - Performance indexes for all key queries
 * - Foreign key constraints with CASCADE deletes
 * - Sync metadata tracking for Firebase synchronization
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String TAG = "DatabaseHelper";
    
    // Database Info
    private static final String DATABASE_NAME = "basketball_stats.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table Names
    public static final String TABLE_TEAMS = "teams";
    public static final String TABLE_TEAM_PLAYERS = "team_players";
    public static final String TABLE_GAMES = "games";
    public static final String TABLE_GAME_PLAYERS = "game_players";
    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_TEAM_FOULS = "team_fouls";
    public static final String TABLE_APP_SETTINGS = "app_settings";
    public static final String TABLE_USER_PROFILE = "user_profile";
    public static final String TABLE_SYNC_QUEUE = "sync_queue";
    public static final String TABLE_SYNC_LOG = "sync_log";
    
    // Common Columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";
    public static final String COLUMN_FIREBASE_ID = "firebase_id";
    public static final String COLUMN_SYNC_STATUS = "sync_status";
    public static final String COLUMN_LAST_SYNC_TIMESTAMP = "last_sync_timestamp";
    
    // Teams Table Columns
    public static final String TEAMS_COLUMN_NAME = "name";
    
    // Team Players Table Columns
    public static final String TEAM_PLAYERS_COLUMN_TEAM_ID = "team_id";
    public static final String TEAM_PLAYERS_COLUMN_JERSEY_NUMBER = "jersey_number";
    public static final String TEAM_PLAYERS_COLUMN_NAME = "name";
    
    // Games Table Columns
    public static final String GAMES_COLUMN_DATE = "date";
    public static final String GAMES_COLUMN_TIME = "time";
    public static final String GAMES_COLUMN_HOME_TEAM_ID = "home_team_id";
    public static final String GAMES_COLUMN_AWAY_TEAM_ID = "away_team_id";
    public static final String GAMES_COLUMN_STATUS = "status";
    public static final String GAMES_COLUMN_HOME_SCORE = "home_score";
    public static final String GAMES_COLUMN_AWAY_SCORE = "away_score";
    public static final String GAMES_COLUMN_CURRENT_QUARTER = "current_quarter";
    public static final String GAMES_COLUMN_GAME_CLOCK_SECONDS = "game_clock_seconds";
    public static final String GAMES_COLUMN_IS_CLOCK_RUNNING = "is_clock_running";
    
    // Game Players Table Columns
    public static final String GAME_PLAYERS_COLUMN_GAME_ID = "game_id";
    public static final String GAME_PLAYERS_COLUMN_TEAM_PLAYER_ID = "team_player_id";
    public static final String GAME_PLAYERS_COLUMN_TEAM_SIDE = "team_side";
    public static final String GAME_PLAYERS_COLUMN_IS_ON_COURT = "is_on_court";
    public static final String GAME_PLAYERS_COLUMN_IS_STARTER = "is_starter";
    public static final String GAME_PLAYERS_COLUMN_PERSONAL_FOULS = "personal_fouls";
    public static final String GAME_PLAYERS_COLUMN_MINUTES_PLAYED = "minutes_played";
    
    // Events Table Columns
    public static final String EVENTS_COLUMN_GAME_ID = "game_id";
    public static final String EVENTS_COLUMN_PLAYER_ID = "player_id";
    public static final String EVENTS_COLUMN_TEAM_SIDE = "team_side";
    public static final String EVENTS_COLUMN_QUARTER = "quarter";
    public static final String EVENTS_COLUMN_GAME_TIME_SECONDS = "game_time_seconds";
    public static final String EVENTS_COLUMN_EVENT_TYPE = "event_type";
    public static final String EVENTS_COLUMN_SUB_PLAYER_OUT_ID = "sub_player_out_id";
    public static final String EVENTS_COLUMN_SUB_PLAYER_IN_ID = "sub_player_in_id";
    public static final String EVENTS_COLUMN_POINTS_VALUE = "points_value";
    public static final String EVENTS_COLUMN_EVENT_SEQUENCE = "event_sequence";
    
    // Team Fouls Table Columns
    public static final String TEAM_FOULS_COLUMN_GAME_ID = "game_id";
    public static final String TEAM_FOULS_COLUMN_TEAM_SIDE = "team_side";
    public static final String TEAM_FOULS_COLUMN_QUARTER = "quarter";
    public static final String TEAM_FOULS_COLUMN_FOUL_COUNT = "foul_count";
    
    // App Settings Table Columns
    public static final String APP_SETTINGS_COLUMN_SETTING_KEY = "setting_key";
    public static final String APP_SETTINGS_COLUMN_SETTING_VALUE = "setting_value";
    
    // User Profile Table Columns
    public static final String USER_PROFILE_COLUMN_FIREBASE_UID = "firebase_uid";
    public static final String USER_PROFILE_COLUMN_EMAIL = "email";
    public static final String USER_PROFILE_COLUMN_DISPLAY_NAME = "display_name";
    public static final String USER_PROFILE_COLUMN_LEAGUE_NAME = "league_name";
    public static final String USER_PROFILE_COLUMN_LAST_LOGIN = "last_login";
    
    // Sync Queue Table Columns
    public static final String SYNC_QUEUE_COLUMN_TABLE_NAME = "table_name";
    public static final String SYNC_QUEUE_COLUMN_RECORD_ID = "record_id";
    public static final String SYNC_QUEUE_COLUMN_OPERATION = "operation";
    public static final String SYNC_QUEUE_COLUMN_DATA_JSON = "data_json";
    public static final String SYNC_QUEUE_COLUMN_RETRY_COUNT = "retry_count";
    public static final String SYNC_QUEUE_COLUMN_MAX_RETRIES = "max_retries";
    public static final String SYNC_QUEUE_COLUMN_LAST_ATTEMPT = "last_attempt";
    public static final String SYNC_QUEUE_COLUMN_ERROR_MESSAGE = "error_message";
    
    // Sync Log Table Columns
    public static final String SYNC_LOG_COLUMN_OPERATION_TYPE = "operation_type";
    public static final String SYNC_LOG_COLUMN_STATUS = "status";
    public static final String SYNC_LOG_COLUMN_RECORDS_PROCESSED = "records_processed";
    public static final String SYNC_LOG_COLUMN_RECORDS_SUCCESSFUL = "records_successful";
    public static final String SYNC_LOG_COLUMN_RECORDS_FAILED = "records_failed";
    public static final String SYNC_LOG_COLUMN_ERROR_DETAILS = "error_details";
    public static final String SYNC_LOG_COLUMN_STARTED_AT = "started_at";
    public static final String SYNC_LOG_COLUMN_COMPLETED_AT = "completed_at";
    public static final String SYNC_LOG_COLUMN_DURATION_SECONDS = "duration_seconds";
    
    // Singleton instance
    private static DatabaseHelper instance;
    
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");
        
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON;");
        
        // Create all tables
        createTeamsTable(db);
        createTeamPlayersTable(db);
        createGamesTable(db);
        createGamePlayersTable(db);
        createEventsTable(db);
        createTeamFoulsTable(db);
        createAppSettingsTable(db);
        createUserProfileTable(db);
        createSyncQueueTable(db);
        createSyncLogTable(db);
        
        // Create performance indexes
        createIndexes(db);
        
        // Insert default settings
        insertDefaultSettings(db);
        
        Log.d(TAG, "Database created successfully");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        // For now, drop and recreate all tables
        // In production, implement proper migration scripts
        dropAllTables(db);
        onCreate(db);
    }
    
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Ensure foreign key constraints are enabled
        db.execSQL("PRAGMA foreign_keys=ON;");
    }
    
    // ========== TABLE CREATION METHODS ==========
    
    /**
     * Teams Table - League teams (Lakers, Warriors, Bulls, Heat)
     */
    private void createTeamsTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TEAMS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TEAMS_COLUMN_NAME + " TEXT NOT NULL UNIQUE, " +
                COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_FIREBASE_ID + " TEXT, " +
                COLUMN_SYNC_STATUS + " TEXT DEFAULT 'local', " +
                COLUMN_LAST_SYNC_TIMESTAMP + " TIMESTAMP" +
                ");";
        
        db.execSQL(createTable);
        Log.d(TAG, "Teams table created");
    }
    
    /**
     * Team Players Table - Team rosters with jersey numbers (0-99)
     */
    private void createTeamPlayersTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TEAM_PLAYERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TEAM_PLAYERS_COLUMN_TEAM_ID + " INTEGER NOT NULL, " +
                TEAM_PLAYERS_COLUMN_JERSEY_NUMBER + " INTEGER NOT NULL, " +
                TEAM_PLAYERS_COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_FIREBASE_ID + " TEXT, " +
                COLUMN_SYNC_STATUS + " TEXT DEFAULT 'local', " +
                COLUMN_LAST_SYNC_TIMESTAMP + " TIMESTAMP, " +
                "UNIQUE(" + TEAM_PLAYERS_COLUMN_TEAM_ID + ", " + TEAM_PLAYERS_COLUMN_JERSEY_NUMBER + "), " +
                "FOREIGN KEY (" + TEAM_PLAYERS_COLUMN_TEAM_ID + ") REFERENCES " + TABLE_TEAMS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
                ");";
        
        db.execSQL(createTable);
        Log.d(TAG, "Team Players table created");
    }
    
    /**
     * Games Table - Scheduled and completed games with live state
     */
    private void createGamesTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_GAMES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GAMES_COLUMN_DATE + " TEXT NOT NULL, " +
                GAMES_COLUMN_TIME + " TEXT NOT NULL, " +
                GAMES_COLUMN_HOME_TEAM_ID + " INTEGER NOT NULL, " +
                GAMES_COLUMN_AWAY_TEAM_ID + " INTEGER NOT NULL, " +
                GAMES_COLUMN_STATUS + " TEXT DEFAULT 'scheduled', " +
                GAMES_COLUMN_HOME_SCORE + " INTEGER DEFAULT 0, " +
                GAMES_COLUMN_AWAY_SCORE + " INTEGER DEFAULT 0, " +
                GAMES_COLUMN_CURRENT_QUARTER + " INTEGER DEFAULT 1, " +
                GAMES_COLUMN_GAME_CLOCK_SECONDS + " INTEGER DEFAULT 600, " +
                GAMES_COLUMN_IS_CLOCK_RUNNING + " BOOLEAN DEFAULT FALSE, " +
                COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_FIREBASE_ID + " TEXT, " +
                COLUMN_SYNC_STATUS + " TEXT DEFAULT 'local', " +
                COLUMN_LAST_SYNC_TIMESTAMP + " TIMESTAMP, " +
                "FOREIGN KEY (" + GAMES_COLUMN_HOME_TEAM_ID + ") REFERENCES " + TABLE_TEAMS + "(" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + GAMES_COLUMN_AWAY_TEAM_ID + ") REFERENCES " + TABLE_TEAMS + "(" + COLUMN_ID + ")" +
                ");";
        
        db.execSQL(createTable);
        Log.d(TAG, "Games table created");
    }
    
    /**
     * Game Players Table - Selected lineups for specific games
     */
    private void createGamePlayersTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_GAME_PLAYERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GAME_PLAYERS_COLUMN_GAME_ID + " INTEGER NOT NULL, " +
                GAME_PLAYERS_COLUMN_TEAM_PLAYER_ID + " INTEGER NOT NULL, " +
                GAME_PLAYERS_COLUMN_TEAM_SIDE + " TEXT NOT NULL, " +
                GAME_PLAYERS_COLUMN_IS_ON_COURT + " BOOLEAN DEFAULT TRUE, " +
                GAME_PLAYERS_COLUMN_IS_STARTER + " BOOLEAN DEFAULT FALSE, " +
                GAME_PLAYERS_COLUMN_PERSONAL_FOULS + " INTEGER DEFAULT 0, " +
                GAME_PLAYERS_COLUMN_MINUTES_PLAYED + " INTEGER DEFAULT 0, " +
                COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_FIREBASE_ID + " TEXT, " +
                COLUMN_SYNC_STATUS + " TEXT DEFAULT 'local', " +
                COLUMN_LAST_SYNC_TIMESTAMP + " TIMESTAMP, " +
                "FOREIGN KEY (" + GAME_PLAYERS_COLUMN_GAME_ID + ") REFERENCES " + TABLE_GAMES + "(" + COLUMN_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + GAME_PLAYERS_COLUMN_TEAM_PLAYER_ID + ") REFERENCES " + TABLE_TEAM_PLAYERS + "(" + COLUMN_ID + ")" +
                ");";
        
        db.execSQL(createTable);
        Log.d(TAG, "Game Players table created");
    }
    
    /**
     * Events Table - Game statistics and actions
     */
    private void createEventsTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_EVENTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EVENTS_COLUMN_GAME_ID + " INTEGER NOT NULL, " +
                EVENTS_COLUMN_PLAYER_ID + " INTEGER, " +
                EVENTS_COLUMN_TEAM_SIDE + " TEXT NOT NULL, " +
                EVENTS_COLUMN_QUARTER + " INTEGER NOT NULL, " +
                EVENTS_COLUMN_GAME_TIME_SECONDS + " INTEGER NOT NULL, " +
                EVENTS_COLUMN_EVENT_TYPE + " TEXT NOT NULL, " +
                EVENTS_COLUMN_SUB_PLAYER_OUT_ID + " INTEGER, " +
                EVENTS_COLUMN_SUB_PLAYER_IN_ID + " INTEGER, " +
                EVENTS_COLUMN_POINTS_VALUE + " INTEGER DEFAULT 0, " +
                EVENTS_COLUMN_EVENT_SEQUENCE + " INTEGER NOT NULL, " +
                COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_FIREBASE_ID + " TEXT, " +
                COLUMN_SYNC_STATUS + " TEXT DEFAULT 'local', " +
                COLUMN_LAST_SYNC_TIMESTAMP + " TIMESTAMP, " +
                "FOREIGN KEY (" + EVENTS_COLUMN_GAME_ID + ") REFERENCES " + TABLE_GAMES + "(" + COLUMN_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + EVENTS_COLUMN_PLAYER_ID + ") REFERENCES " + TABLE_TEAM_PLAYERS + "(" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + EVENTS_COLUMN_SUB_PLAYER_OUT_ID + ") REFERENCES " + TABLE_TEAM_PLAYERS + "(" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + EVENTS_COLUMN_SUB_PLAYER_IN_ID + ") REFERENCES " + TABLE_TEAM_PLAYERS + "(" + COLUMN_ID + ")" +
                ");";
        
        db.execSQL(createTable);
        Log.d(TAG, "Events table created");
    }
    
    /**
     * Team Fouls Table - Quarter-by-quarter team foul tracking
     */
    private void createTeamFoulsTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TEAM_FOULS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TEAM_FOULS_COLUMN_GAME_ID + " INTEGER NOT NULL, " +
                TEAM_FOULS_COLUMN_TEAM_SIDE + " TEXT NOT NULL, " +
                TEAM_FOULS_COLUMN_QUARTER + " INTEGER NOT NULL, " +
                TEAM_FOULS_COLUMN_FOUL_COUNT + " INTEGER DEFAULT 0, " +
                COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_FIREBASE_ID + " TEXT, " +
                COLUMN_SYNC_STATUS + " TEXT DEFAULT 'local', " +
                COLUMN_LAST_SYNC_TIMESTAMP + " TIMESTAMP, " +
                "UNIQUE(" + TEAM_FOULS_COLUMN_GAME_ID + ", " + TEAM_FOULS_COLUMN_TEAM_SIDE + ", " + TEAM_FOULS_COLUMN_QUARTER + "), " +
                "FOREIGN KEY (" + TEAM_FOULS_COLUMN_GAME_ID + ") REFERENCES " + TABLE_GAMES + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
                ");";
        
        db.execSQL(createTable);
        Log.d(TAG, "Team Fouls table created");
    }
    
    /**
     * App Settings Table - User preferences and app configuration
     */
    private void createAppSettingsTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_APP_SETTINGS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                APP_SETTINGS_COLUMN_SETTING_KEY + " TEXT NOT NULL UNIQUE, " +
                APP_SETTINGS_COLUMN_SETTING_VALUE + " TEXT NOT NULL, " +
                COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_FIREBASE_ID + " TEXT, " +
                COLUMN_SYNC_STATUS + " TEXT DEFAULT 'local', " +
                COLUMN_LAST_SYNC_TIMESTAMP + " TIMESTAMP" +
                ");";
        
        db.execSQL(createTable);
        Log.d(TAG, "App Settings table created");
    }
    
    /**
     * User Profile Table - Firebase user information
     */
    private void createUserProfileTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_USER_PROFILE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_PROFILE_COLUMN_FIREBASE_UID + " TEXT NOT NULL UNIQUE, " +
                USER_PROFILE_COLUMN_EMAIL + " TEXT, " +
                USER_PROFILE_COLUMN_DISPLAY_NAME + " TEXT, " +
                USER_PROFILE_COLUMN_LEAGUE_NAME + " TEXT, " +
                COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_UPDATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                USER_PROFILE_COLUMN_LAST_LOGIN + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
        
        db.execSQL(createTable);
        Log.d(TAG, "User Profile table created");
    }
    
    /**
     * Sync Queue Table - Tracks pending Firebase operations
     */
    private void createSyncQueueTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_SYNC_QUEUE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SYNC_QUEUE_COLUMN_TABLE_NAME + " TEXT NOT NULL, " +
                SYNC_QUEUE_COLUMN_RECORD_ID + " INTEGER NOT NULL, " +
                SYNC_QUEUE_COLUMN_OPERATION + " TEXT NOT NULL, " +
                COLUMN_FIREBASE_ID + " TEXT, " +
                SYNC_QUEUE_COLUMN_DATA_JSON + " TEXT, " +
                SYNC_QUEUE_COLUMN_RETRY_COUNT + " INTEGER DEFAULT 0, " +
                SYNC_QUEUE_COLUMN_MAX_RETRIES + " INTEGER DEFAULT 3, " +
                COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                SYNC_QUEUE_COLUMN_LAST_ATTEMPT + " TIMESTAMP, " +
                SYNC_QUEUE_COLUMN_ERROR_MESSAGE + " TEXT" +
                ");";
        
        db.execSQL(createTable);
        Log.d(TAG, "Sync Queue table created");
    }
    
    /**
     * Sync Log Table - History of sync operations
     */
    private void createSyncLogTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_SYNC_LOG + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SYNC_LOG_COLUMN_OPERATION_TYPE + " TEXT NOT NULL, " +
                SYNC_LOG_COLUMN_STATUS + " TEXT NOT NULL, " +
                SYNC_LOG_COLUMN_RECORDS_PROCESSED + " INTEGER DEFAULT 0, " +
                SYNC_LOG_COLUMN_RECORDS_SUCCESSFUL + " INTEGER DEFAULT 0, " +
                SYNC_LOG_COLUMN_RECORDS_FAILED + " INTEGER DEFAULT 0, " +
                SYNC_LOG_COLUMN_ERROR_DETAILS + " TEXT, " +
                SYNC_LOG_COLUMN_STARTED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                SYNC_LOG_COLUMN_COMPLETED_AT + " TIMESTAMP, " +
                SYNC_LOG_COLUMN_DURATION_SECONDS + " INTEGER" +
                ");";
        
        db.execSQL(createTable);
        Log.d(TAG, "Sync Log table created");
    }
    
    // ========== INDEX CREATION METHODS ==========
    
    /**
     * Create performance indexes for optimized queries
     */
    private void createIndexes(SQLiteDatabase db) {
        Log.d(TAG, "Creating performance indexes...");
        
        // Game performance indexes
        db.execSQL("CREATE INDEX idx_games_date ON " + TABLE_GAMES + "(" + GAMES_COLUMN_DATE + ");");
        db.execSQL("CREATE INDEX idx_games_status ON " + TABLE_GAMES + "(" + GAMES_COLUMN_STATUS + ");");
        db.execSQL("CREATE INDEX idx_games_teams ON " + TABLE_GAMES + "(" + GAMES_COLUMN_HOME_TEAM_ID + ", " + GAMES_COLUMN_AWAY_TEAM_ID + ");");
        
        // Event performance indexes
        db.execSQL("CREATE INDEX idx_events_game ON " + TABLE_EVENTS + "(" + EVENTS_COLUMN_GAME_ID + ");");
        db.execSQL("CREATE INDEX idx_events_player ON " + TABLE_EVENTS + "(" + EVENTS_COLUMN_PLAYER_ID + ");");
        db.execSQL("CREATE INDEX idx_events_type ON " + TABLE_EVENTS + "(" + EVENTS_COLUMN_EVENT_TYPE + ");");
        db.execSQL("CREATE INDEX idx_events_sequence ON " + TABLE_EVENTS + "(" + EVENTS_COLUMN_GAME_ID + ", " + EVENTS_COLUMN_EVENT_SEQUENCE + ");");
        
        // Player performance indexes
        db.execSQL("CREATE INDEX idx_team_players_team ON " + TABLE_TEAM_PLAYERS + "(" + TEAM_PLAYERS_COLUMN_TEAM_ID + ");");
        db.execSQL("CREATE INDEX idx_team_players_number ON " + TABLE_TEAM_PLAYERS + "(" + TEAM_PLAYERS_COLUMN_TEAM_ID + ", " + TEAM_PLAYERS_COLUMN_JERSEY_NUMBER + ");");
        
        // Game players performance indexes
        db.execSQL("CREATE INDEX idx_game_players_game ON " + TABLE_GAME_PLAYERS + "(" + GAME_PLAYERS_COLUMN_GAME_ID + ");");
        db.execSQL("CREATE INDEX idx_game_players_team ON " + TABLE_GAME_PLAYERS + "(" + GAME_PLAYERS_COLUMN_GAME_ID + ", " + GAME_PLAYERS_COLUMN_TEAM_SIDE + ");");
        
        // Sync performance indexes
        db.execSQL("CREATE INDEX idx_sync_status_teams ON " + TABLE_TEAMS + "(" + COLUMN_SYNC_STATUS + ");");
        db.execSQL("CREATE INDEX idx_sync_status_players ON " + TABLE_TEAM_PLAYERS + "(" + COLUMN_SYNC_STATUS + ");");
        db.execSQL("CREATE INDEX idx_sync_status_games ON " + TABLE_GAMES + "(" + COLUMN_SYNC_STATUS + ");");
        db.execSQL("CREATE INDEX idx_sync_queue_table ON " + TABLE_SYNC_QUEUE + "(" + SYNC_QUEUE_COLUMN_TABLE_NAME + ", " + SYNC_QUEUE_COLUMN_OPERATION + ");");
        db.execSQL("CREATE INDEX idx_sync_timestamp_teams ON " + TABLE_TEAMS + "(" + COLUMN_LAST_SYNC_TIMESTAMP + ");");
        
        Log.d(TAG, "Performance indexes created");
    }
    
    // ========== DEFAULT DATA METHODS ==========
    
    /**
     * Insert default app settings
     */
    private void insertDefaultSettings(SQLiteDatabase db) {
        Log.d(TAG, "Inserting default settings...");
        
        // Default quarter length in minutes
        db.execSQL("INSERT INTO " + TABLE_APP_SETTINGS + " (" + APP_SETTINGS_COLUMN_SETTING_KEY + ", " + APP_SETTINGS_COLUMN_SETTING_VALUE + ") " +
                "VALUES ('quarter_length_minutes', '10');");
        
        // Auto sync enabled
        db.execSQL("INSERT INTO " + TABLE_APP_SETTINGS + " (" + APP_SETTINGS_COLUMN_SETTING_KEY + ", " + APP_SETTINGS_COLUMN_SETTING_VALUE + ") " +
                "VALUES ('auto_sync_enabled', 'true');");
        
        // Sync on wifi only
        db.execSQL("INSERT INTO " + TABLE_APP_SETTINGS + " (" + APP_SETTINGS_COLUMN_SETTING_KEY + ", " + APP_SETTINGS_COLUMN_SETTING_VALUE + ") " +
                "VALUES ('sync_wifi_only', 'false');");
        
        // App version for migration tracking
        db.execSQL("INSERT INTO " + TABLE_APP_SETTINGS + " (" + APP_SETTINGS_COLUMN_SETTING_KEY + ", " + APP_SETTINGS_COLUMN_SETTING_VALUE + ") " +
                "VALUES ('app_version', '1.0.0');");
        
        Log.d(TAG, "Default settings inserted");
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Drop all tables (for database upgrades)
     */
    private void dropAllTables(SQLiteDatabase db) {
        Log.w(TAG, "Dropping all database tables");
        
        // Drop tables in reverse order to respect foreign key constraints
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SYNC_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SYNC_QUEUE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEAM_FOULS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME_PLAYERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEAM_PLAYERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEAMS);
    }
    
    /**
     * Get database version for debugging
     */
    public int getDatabaseVersion() {
        return DATABASE_VERSION;
    }
    
    /**
     * Get database name for debugging
     */
    public String getDatabaseName() {
        return DATABASE_NAME;
    }
    
    /**
     * Enable/disable foreign key constraints
     */
    public void setForeignKeysEnabled(boolean enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (enabled) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        } else {
            db.execSQL("PRAGMA foreign_keys=OFF;");
        }
    }
    
    /**
     * Get foreign key constraints status
     */
    public boolean isForeignKeysEnabled() {
        SQLiteDatabase db = this.getReadableDatabase();
        return DatabaseHelper.pragmaQuery(db, "PRAGMA foreign_keys;") == 1;
    }
    
    /**
     * Helper method for PRAGMA queries
     */
    private static int pragmaQuery(SQLiteDatabase db, String pragma) {
        android.database.Cursor cursor = db.rawQuery(pragma, null);
        if (cursor.moveToFirst()) {
            int result = cursor.getInt(0);
            cursor.close();
            return result;
        }
        cursor.close();
        return 0;
    }
}
