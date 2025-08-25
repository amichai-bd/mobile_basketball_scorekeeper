package com.basketballstats.app.auth;

import android.content.Context;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.basketballstats.app.models.UserProfile;
import com.basketballstats.app.data.DatabaseController;

/**
 * AuthController - Firebase Authentication Management
 * 
 * Handles user authentication, registration, and session management
 * Integrates with SQLite UserProfile for local user data storage
 * Supports email/password authentication and anonymous access for demos
 */
public class AuthController {
    
    private static final String TAG = "AuthController";
    private static AuthController instance;
    
    private FirebaseAuth firebaseAuth;
    private Context context;
    private DatabaseController dbController;
    
    // Authentication callback interfaces
    public interface AuthCallback {
        void onAuthSuccess(FirebaseUser user, String message);
        void onAuthError(String errorMessage);
    }
    
    public interface AuthStateListener {
        void onUserSignedIn(FirebaseUser user);
        void onUserSignedOut();
    }
    
    /**
     * Singleton pattern for AuthController
     */
    public static synchronized AuthController getInstance(Context context) {
        if (instance == null) {
            instance = new AuthController(context.getApplicationContext());
        }
        return instance;
    }
    
    private AuthController(Context context) {
        this.context = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.dbController = DatabaseController.getInstance(context);
        
        Log.d(TAG, "AuthController initialized");
    }
    
    /**
     * Register new user with email and password
     */
    public void registerUser(String email, String password, String displayName, AuthCallback callback) {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            callback.onAuthError("Email and password are required");
            return;
        }
        
        if (password.length() < 6) {
            callback.onAuthError("Password must be at least 6 characters");
            return;
        }
        
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Create user profile in SQLite
                            createUserProfile(user.getUid(), email, displayName);
                            callback.onAuthSuccess(user, "Account created successfully!");
                            Log.d(TAG, "User registered: " + user.getUid());
                        }
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Registration failed";
                        callback.onAuthError(errorMessage);
                        Log.e(TAG, "Registration failed", task.getException());
                    }
                }
            });
    }
    
    /**
     * Sign in existing user with email and password
     */
    public void signInUser(String email, String password, AuthCallback callback) {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            callback.onAuthError("Email and password are required");
            return;
        }
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Update user profile last login
                            updateUserLastLogin(user.getUid());
                            callback.onAuthSuccess(user, "Welcome back!");
                            Log.d(TAG, "User signed in: " + user.getUid());
                        }
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Sign in failed";
                        callback.onAuthError(errorMessage);
                        Log.e(TAG, "Sign in failed", task.getException());
                    }
                }
            });
    }
    
    /**
     * Sign in anonymously for demo/guest access
     */
    public void signInAnonymously(AuthCallback callback) {
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Create anonymous user profile
                            createUserProfile(user.getUid(), "guest@demo.com", "Guest User");
                            callback.onAuthSuccess(user, "Demo access granted!");
                            Log.d(TAG, "Anonymous user signed in: " + user.getUid());
                        }
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Anonymous sign in failed";
                        callback.onAuthError(errorMessage);
                        Log.e(TAG, "Anonymous sign in failed", task.getException());
                    }
                }
            });
    }
    
    /**
     * Sign out current user
     */
    public void signOut() {
        firebaseAuth.signOut();
        Log.d(TAG, "User signed out");
    }
    
    /**
     * Get currently authenticated user
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    /**
     * Check if user is currently authenticated
     */
    public boolean isUserAuthenticated() {
        return getCurrentUser() != null;
    }
    
    /**
     * Get current user's UID
     */
    public String getCurrentUserUid() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    /**
     * Add authentication state listener
     */
    public void addAuthStateListener(AuthStateListener listener) {
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    listener.onUserSignedIn(user);
                } else {
                    listener.onUserSignedOut();
                }
            }
        });
    }
    
    /**
     * Create user profile in SQLite database
     */
    private void createUserProfile(String firebaseUid, String email, String displayName) {
        try {
            // Check if profile already exists
            UserProfile existingProfile = UserProfile.findByFirebaseUid(dbController.getDatabaseHelper(), firebaseUid);
            
            if (existingProfile == null) {
                // Create new profile
                UserProfile profile = new UserProfile();
                profile.setFirebaseUid(firebaseUid);
                profile.setEmail(email);
                profile.setDisplayName(displayName != null ? displayName : "User");
                
                long result = profile.save(dbController.getDatabaseHelper());
                if (result > 0) {
                    Log.d(TAG, "User profile created in SQLite: " + firebaseUid);
                } else {
                    Log.e(TAG, "Failed to create user profile in SQLite");
                }
            } else {
                // Update existing profile
                existingProfile.setLastLogin(System.currentTimeMillis());
                existingProfile.save(dbController.getDatabaseHelper());
                Log.d(TAG, "User profile updated in SQLite: " + firebaseUid);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error managing user profile", e);
        }
    }
    
    /**
     * Update user's last login timestamp
     */
    private void updateUserLastLogin(String firebaseUid) {
        try {
            UserProfile profile = UserProfile.findByFirebaseUid(dbController.getDatabaseHelper(), firebaseUid);
            if (profile != null) {
                profile.setLastLogin(System.currentTimeMillis());
                profile.save(dbController.getDatabaseHelper());
                Log.d(TAG, "Updated last login for user: " + firebaseUid);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating last login", e);
        }
    }
    
    /**
     * Get user profile from SQLite
     */
    public UserProfile getUserProfile() {
        String uid = getCurrentUserUid();
        if (uid != null) {
            try {
                return UserProfile.findByFirebaseUid(dbController.getDatabaseHelper(), uid);
            } catch (Exception e) {
                Log.e(TAG, "Error getting user profile", e);
            }
        }
        return null;
    }
}
