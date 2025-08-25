package com.basketballstats.app.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.basketballstats.app.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AppSettings model for user preferences and app configuration with SQLite persistence
 * 
 * Handles app-wide settings like quarter length, sync preferences, etc.
 * Enhanced with CRUD operations for SQLite-primary architecture
 * Supports sync metadata for Firebase synchronization
 */
public class AppSettings {
    private static final String TAG = "AppSettings";
    
    // Core fields
    private int id;
    private String settingKey;
    private String settingValue;
    
    // Sync fields
    private String createdAt;
    private String updatedAt;
    private String firebaseId;
    private String syncStatus;
    private String lastSyncTimestamp;
    
    // Setting keys constants
    public static final String KEY_QUARTER_LENGTH_MINUTES = "quarter_length_minutes";
    public static final String KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled";
    public static final String KEY_SYNC_WIFI_ONLY = "sync_wifi_only";
    public static final String KEY_APP_VERSION = "app_version";
    public static final String KEY_LAST_SYNC_TIME = "last_sync_time";
    public static final String KEY_USER_LEAGUE_NAME = "user_league_name";
    public static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final String KEY_EVENT_SOUND_ENABLED = "event_sound_enabled";
    public static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    
    // Default values
    private static final Map<String, String> DEFAULT_VALUES = new HashMap<>();
    static {
        DEFAULT_VALUES.put(KEY_QUARTER_LENGTH_MINUTES, "10");
        DEFAULT_VALUES.put(KEY_AUTO_SYNC_ENABLED, "true");
        DEFAULT_VALUES.put(KEY_SYNC_WIFI_ONLY, "false");
        DEFAULT_VALUES.put(KEY_APP_VERSION, "1.0.0");
        DEFAULT_VALUES.put(KEY_NOTIFICATIONS_ENABLED, "true");
        DEFAULT_VALUES.put(KEY_THEME_MODE, "system");
        DEFAULT_VALUES.put(KEY_EVENT_SOUND_ENABLED, "true");
        DEFAULT_VALUES.put(KEY_VIBRATION_ENABLED, "true");
    }
    
    // Constructors
    public AppSettings() {
        this.syncStatus = "local";
    }
    
    public AppSettings(String settingKey, String settingValue) {
        this();
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    
    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
    
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
     * Get setting value as integer
     */
    public int getIntValue() {
        try {
            return Integer.parseInt(settingValue);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Cannot parse setting value as int: " + settingValue + " for key: " + settingKey);
            return 0;
        }
    }
    
    /**
     * Get setting value as boolean
     */
    public boolean getBooleanValue() {
        return "true".equalsIgnoreCase(settingValue);
    }
    
    /**
     * Get setting value as float
     */
    public float getFloatValue() {
        try {
            return Float.parseFloat(settingValue);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Cannot parse setting value as float: " + settingValue + " for key: " + settingKey);
            return 0.0f;
        }
    }
    
    /**
     * Set setting value from integer
     */
    public void setIntValue(int value) {
        this.settingValue = String.valueOf(value);
    }
    
    /**
     * Set setting value from boolean
     */
    public void setBooleanValue(boolean value) {
        this.settingValue = String.valueOf(value);
    }
    
    /**
     * Set setting value from float
     */
    public void setFloatValue(float value) {
        this.settingValue = String.valueOf(value);
    }
    
    /**
     * Check if this setting has a default value
     */
    public boolean hasDefaultValue() {
        return DEFAULT_VALUES.containsKey(settingKey);
    }
    
    /**
     * Get default value for this setting key
     */
    public String getDefaultValue() {
        return DEFAULT_VALUES.get(settingKey);
    }
    
    /**
     * Reset to default value
     */
    public void resetToDefault() {
        String defaultValue = getDefaultValue();
        if (defaultValue != null) {
            this.settingValue = defaultValue;
        }
    }
    
    @Override
    public String toString() {
        return settingKey + " = " + settingValue;
    }
    
    // ========== SQLITE CRUD OPERATIONS ==========
    
    /**
     * Save setting to database (INSERT or UPDATE)
     */
    public long save(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.APP_SETTINGS_COLUMN_SETTING_KEY, settingKey);
        values.put(DatabaseHelper.APP_SETTINGS_COLUMN_SETTING_VALUE, settingValue);
        values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentTimestamp());
        values.put(DatabaseHelper.COLUMN_FIREBASE_ID, firebaseId);
        values.put(DatabaseHelper.COLUMN_SYNC_STATUS, syncStatus);
        values.put(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP, lastSyncTimestamp);
        
        long result;
        if (id > 0) {
            // UPDATE existing setting
            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(id)};
            result = db.update(DatabaseHelper.TABLE_APP_SETTINGS, values, whereClause, whereArgs);
            Log.d(TAG, "Updated setting: " + toString() + " (ID: " + id + ")");
        } else {
            // INSERT new setting
            values.put(DatabaseHelper.COLUMN_CREATED_AT, getCurrentTimestamp());
            result = db.insert(DatabaseHelper.TABLE_APP_SETTINGS, null, values);
            if (result != -1) {
                this.id = (int) result;
                Log.d(TAG, "Created setting: " + toString() + " (ID: " + id + ")");
            }
        }
        
        return result;
    }
    
    /**
     * Delete setting from database
     */
    public boolean delete(DatabaseHelper dbHelper) {
        if (id <= 0) {
            Log.w(TAG, "Cannot delete setting with invalid ID: " + id);
            return false;
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        
        int rowsAffected = db.delete(DatabaseHelper.TABLE_APP_SETTINGS, whereClause, whereArgs);
        boolean success = rowsAffected > 0;
        
        if (success) {
            Log.d(TAG, "Deleted setting: " + toString() + " (ID: " + id + ")");
        } else {
            Log.w(TAG, "Failed to delete setting: " + toString() + " (ID: " + id + ")");
        }
        
        return success;
    }
    
    /**
     * Load setting from database by key
     */
    public static AppSettings findByKey(DatabaseHelper dbHelper, String settingKey) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.APP_SETTINGS_COLUMN_SETTING_KEY + " = ?";
        String[] selectionArgs = {settingKey};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_APP_SETTINGS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        AppSettings setting = null;
        if (cursor.moveToFirst()) {
            setting = fromCursor(cursor);
        }
        cursor.close();
        
        return setting;
    }
    
    /**
     * Get all settings from database
     */
    public static List<AppSettings> findAll(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<AppSettings> settings = new ArrayList<>();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_APP_SETTINGS,
            null,
            null,
            null,
            null,
            null,
            DatabaseHelper.APP_SETTINGS_COLUMN_SETTING_KEY + " ASC"
        );
        
        while (cursor.moveToNext()) {
            settings.add(fromCursor(cursor));
        }
        cursor.close();
        
        Log.d(TAG, "Loaded " + settings.size() + " settings from database");
        return settings;
    }
    
    /**
     * Get settings that need syncing
     */
    public static List<AppSettings> findPendingSync(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<AppSettings> settings = new ArrayList<>();
        
        String selection = DatabaseHelper.COLUMN_SYNC_STATUS + " IN (?, ?)";
        String[] selectionArgs = {"local", "pending"};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_APP_SETTINGS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseHelper.COLUMN_UPDATED_AT + " ASC"
        );
        
        while (cursor.moveToNext()) {
            settings.add(fromCursor(cursor));
        }
        cursor.close();
        
        return settings;
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
        
        db.update(DatabaseHelper.TABLE_APP_SETTINGS, values, whereClause, whereArgs);
    }
    
    // ========== CONVENIENCE METHODS ==========
    
    /**
     * Get setting value by key (with default)
     */
    public static String getValue(DatabaseHelper dbHelper, String settingKey, String defaultValue) {
        AppSettings setting = findByKey(dbHelper, settingKey);
        if (setting != null) {
            return setting.getSettingValue();
        }
        return defaultValue;
    }
    
    /**
     * Get setting value by key (with system default)
     */
    public static String getValue(DatabaseHelper dbHelper, String settingKey) {
        String defaultValue = DEFAULT_VALUES.get(settingKey);
        return getValue(dbHelper, settingKey, defaultValue);
    }
    
    /**
     * Get integer setting value
     */
    public static int getIntValue(DatabaseHelper dbHelper, String settingKey, int defaultValue) {
        String value = getValue(dbHelper, settingKey);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Cannot parse setting as int: " + value + " for key: " + settingKey);
            }
        }
        return defaultValue;
    }
    
    /**
     * Get boolean setting value
     */
    public static boolean getBooleanValue(DatabaseHelper dbHelper, String settingKey, boolean defaultValue) {
        String value = getValue(dbHelper, settingKey);
        if (value != null) {
            return "true".equalsIgnoreCase(value);
        }
        return defaultValue;
    }
    
    /**
     * Set setting value by key
     */
    public static void setValue(DatabaseHelper dbHelper, String settingKey, String settingValue) {
        AppSettings setting = findByKey(dbHelper, settingKey);
        if (setting == null) {
            setting = new AppSettings(settingKey, settingValue);
        } else {
            setting.setSettingValue(settingValue);
        }
        setting.save(dbHelper);
    }
    
    /**
     * Set integer setting value
     */
    public static void setIntValue(DatabaseHelper dbHelper, String settingKey, int value) {
        setValue(dbHelper, settingKey, String.valueOf(value));
    }
    
    /**
     * Set boolean setting value
     */
    public static void setBooleanValue(DatabaseHelper dbHelper, String settingKey, boolean value) {
        setValue(dbHelper, settingKey, String.valueOf(value));
    }
    
    /**
     * Initialize default settings if they don't exist
     */
    public static void initializeDefaults(DatabaseHelper dbHelper) {
        Log.d(TAG, "Initializing default settings...");
        
        for (Map.Entry<String, String> entry : DEFAULT_VALUES.entrySet()) {
            AppSettings existing = findByKey(dbHelper, entry.getKey());
            if (existing == null) {
                AppSettings defaultSetting = new AppSettings(entry.getKey(), entry.getValue());
                defaultSetting.save(dbHelper);
                Log.d(TAG, "Created default setting: " + defaultSetting.toString());
            }
        }
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Create AppSettings object from database cursor
     */
    private static AppSettings fromCursor(Cursor cursor) {
        AppSettings setting = new AppSettings();
        
        setting.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        setting.settingKey = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.APP_SETTINGS_COLUMN_SETTING_KEY));
        setting.settingValue = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.APP_SETTINGS_COLUMN_SETTING_VALUE));
        setting.createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT));
        setting.updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATED_AT));
        setting.firebaseId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIREBASE_ID));
        setting.syncStatus = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SYNC_STATUS));
        setting.lastSyncTimestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_SYNC_TIMESTAMP));
        
        return setting;
    }
    
    /**
     * Get current timestamp as string
     */
    private static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Get setting count
     */
    public static int getCount(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_APP_SETTINGS, null);
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
        
        AppSettings that = (AppSettings) obj;
        return id == that.id && 
               (settingKey != null ? settingKey.equals(that.settingKey) : that.settingKey == null);
    }
    
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (settingKey != null ? settingKey.hashCode() : 0);
        return result;
    }
}
