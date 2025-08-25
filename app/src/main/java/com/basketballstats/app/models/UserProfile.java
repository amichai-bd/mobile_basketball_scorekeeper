package com.basketballstats.app.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.basketballstats.app.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * UserProfile model for Firebase user information with SQLite persistence
 * 
 * Links local app data with Firebase Authentication user accounts
 * Handles user profile data, league memberships, and authentication state
 * Enhanced with CRUD operations for SQLite-primary architecture
 */
public class UserProfile {
    private static final String TAG = "UserProfile";
    
    // Core fields
    private int id;
    private String firebaseUid; // Firebase Authentication UID
    private String email;
    private String displayName;
    private String leagueName; // User's league name
    private String lastLogin;
    
    // Metadata fields
    private String createdAt;
    private String updatedAt;
    
    // Constructors
    public UserProfile() {
        this.lastLogin = getCurrentTimestamp();
    }
    
    public UserProfile(String firebaseUid, String email) {
        this();
        this.firebaseUid = firebaseUid;
        this.email = email;
    }
    
    public UserProfile(String firebaseUid, String email, String displayName) {
        this(firebaseUid, email);
        this.displayName = displayName;
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getLeagueName() { return leagueName; }
    public void setLeagueName(String leagueName) { this.leagueName = leagueName; }
    
    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    // ========== BUSINESS LOGIC METHODS ==========
    
    /**
     * Check if user has a valid Firebase UID
     */
    public boolean hasValidFirebaseUid() {
        return firebaseUid != null && !firebaseUid.trim().isEmpty();
    }
    
    /**
     * Check if user has email address
     */
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }
    
    /**
     * Check if user has display name
     */
    public boolean hasDisplayName() {
        return displayName != null && !displayName.trim().isEmpty();
    }
    
    /**
     * Check if user has league name
     */
    public boolean hasLeague() {
        return leagueName != null && !leagueName.trim().isEmpty();
    }
    
    /**
     * Get display name or fallback to email
     */
    public String getDisplayNameOrEmail() {
        if (hasDisplayName()) {
            return displayName;
        } else if (hasEmail()) {
            return email;
        } else {
            return "User " + id;
        }
    }
    
    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        this.lastLogin = getCurrentTimestamp();
    }
    
    /**
     * Check if user is anonymous (guest mode)
     */
    public boolean isAnonymous() {
        return !hasEmail() || email.contains("@anonymous");
    }
    
    /**
     * Get formatted last login time
     */
    public String getFormattedLastLogin() {
        if (lastLogin != null) {
            try {
                long timestamp = Long.parseLong(lastLogin);
                return new java.util.Date(timestamp).toString();
            } catch (NumberFormatException e) {
                return lastLogin;
            }
        }
        return "Never";
    }
    
    @Override
    public String toString() {
        return getDisplayNameOrEmail() + " (" + firebaseUid + ")";
    }
    
    // ========== SQLITE CRUD OPERATIONS ==========
    
    /**
     * Save user profile to database (INSERT or UPDATE)
     */
    public long save(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.USER_PROFILE_COLUMN_FIREBASE_UID, firebaseUid);
        values.put(DatabaseHelper.USER_PROFILE_COLUMN_EMAIL, email);
        values.put(DatabaseHelper.USER_PROFILE_COLUMN_DISPLAY_NAME, displayName);
        values.put(DatabaseHelper.USER_PROFILE_COLUMN_LEAGUE_NAME, leagueName);
        values.put(DatabaseHelper.USER_PROFILE_COLUMN_LAST_LOGIN, lastLogin);
        values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentTimestamp());
        
        long result;
        if (id > 0) {
            // UPDATE existing user profile
            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(id)};
            result = db.update(DatabaseHelper.TABLE_USER_PROFILE, values, whereClause, whereArgs);
            Log.d(TAG, "Updated user profile: " + toString() + " (ID: " + id + ")");
        } else {
            // INSERT new user profile
            values.put(DatabaseHelper.COLUMN_CREATED_AT, getCurrentTimestamp());
            result = db.insert(DatabaseHelper.TABLE_USER_PROFILE, null, values);
            if (result != -1) {
                this.id = (int) result;
                Log.d(TAG, "Created user profile: " + toString() + " (ID: " + id + ")");
            }
        }
        
        return result;
    }
    
    /**
     * Delete user profile from database
     */
    public boolean delete(DatabaseHelper dbHelper) {
        if (id <= 0) {
            Log.w(TAG, "Cannot delete user profile with invalid ID: " + id);
            return false;
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        
        int rowsAffected = db.delete(DatabaseHelper.TABLE_USER_PROFILE, whereClause, whereArgs);
        boolean success = rowsAffected > 0;
        
        if (success) {
            Log.d(TAG, "Deleted user profile: " + toString() + " (ID: " + id + ")");
        } else {
            Log.w(TAG, "Failed to delete user profile: " + toString() + " (ID: " + id + ")");
        }
        
        return success;
    }
    
    /**
     * Load user profile from database by Firebase UID
     */
    public static UserProfile findByFirebaseUid(DatabaseHelper dbHelper, String firebaseUid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.USER_PROFILE_COLUMN_FIREBASE_UID + " = ?";
        String[] selectionArgs = {firebaseUid};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USER_PROFILE,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        UserProfile userProfile = null;
        if (cursor.moveToFirst()) {
            userProfile = fromCursor(cursor);
        }
        cursor.close();
        
        return userProfile;
    }
    
    /**
     * Load user profile from database by ID
     */
    public static UserProfile findById(DatabaseHelper dbHelper, int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USER_PROFILE,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        UserProfile userProfile = null;
        if (cursor.moveToFirst()) {
            userProfile = fromCursor(cursor);
        }
        cursor.close();
        
        return userProfile;
    }
    
    /**
     * Load user profile from database by email
     */
    public static UserProfile findByEmail(DatabaseHelper dbHelper, String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.USER_PROFILE_COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USER_PROFILE,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        );
        
        UserProfile userProfile = null;
        if (cursor.moveToFirst()) {
            userProfile = fromCursor(cursor);
        }
        cursor.close();
        
        return userProfile;
    }
    
    /**
     * Get all user profiles from database
     */
    public static List<UserProfile> findAll(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<UserProfile> userProfiles = new ArrayList<>();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USER_PROFILE,
            null,
            null,
            null,
            null,
            null,
            DatabaseHelper.USER_PROFILE_COLUMN_LAST_LOGIN + " DESC"
        );
        
        while (cursor.moveToNext()) {
            userProfiles.add(fromCursor(cursor));
        }
        cursor.close();
        
        Log.d(TAG, "Loaded " + userProfiles.size() + " user profiles from database");
        return userProfiles;
    }
    
    /**
     * Get current logged-in user (most recent login)
     */
    public static UserProfile getCurrentUser(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USER_PROFILE,
            null,
            null,
            null,
            null,
            null,
            DatabaseHelper.USER_PROFILE_COLUMN_LAST_LOGIN + " DESC LIMIT 1"
        );
        
        UserProfile userProfile = null;
        if (cursor.moveToFirst()) {
            userProfile = fromCursor(cursor);
        }
        cursor.close();
        
        return userProfile;
    }
    
    /**
     * Create or update user profile from Firebase user
     */
    public static UserProfile createOrUpdate(DatabaseHelper dbHelper, String firebaseUid, String email, String displayName) {
        UserProfile userProfile = findByFirebaseUid(dbHelper, firebaseUid);
        
        if (userProfile == null) {
            // Create new user profile
            userProfile = new UserProfile(firebaseUid, email, displayName);
        } else {
            // Update existing user profile
            userProfile.setEmail(email);
            userProfile.setDisplayName(displayName);
            userProfile.updateLastLogin();
        }
        
        userProfile.save(dbHelper);
        return userProfile;
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Create UserProfile object from database cursor
     */
    private static UserProfile fromCursor(Cursor cursor) {
        UserProfile userProfile = new UserProfile();
        
        userProfile.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        userProfile.firebaseUid = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_PROFILE_COLUMN_FIREBASE_UID));
        userProfile.email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_PROFILE_COLUMN_EMAIL));
        userProfile.displayName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_PROFILE_COLUMN_DISPLAY_NAME));
        userProfile.leagueName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_PROFILE_COLUMN_LEAGUE_NAME));
        userProfile.lastLogin = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_PROFILE_COLUMN_LAST_LOGIN));
        userProfile.createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT));
        userProfile.updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATED_AT));
        
        return userProfile;
    }
    
    /**
     * Get current timestamp as string
     */
    private static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Check if user profile exists for Firebase UID
     */
    public static boolean exists(DatabaseHelper dbHelper, String firebaseUid) {
        return findByFirebaseUid(dbHelper, firebaseUid) != null;
    }
    
    /**
     * Get user profile count
     */
    public static int getCount(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USER_PROFILE, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    /**
     * Clear all user profiles (for logout/reset)
     */
    public static void clearAll(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_USER_PROFILE, null, null);
        Log.d(TAG, "Cleared " + rowsDeleted + " user profiles");
    }
    
    // ========== OBJECT METHODS ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserProfile that = (UserProfile) obj;
        return id == that.id && 
               (firebaseUid != null ? firebaseUid.equals(that.firebaseUid) : that.firebaseUid == null);
    }
    
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (firebaseUid != null ? firebaseUid.hashCode() : 0);
        return result;
    }
}
