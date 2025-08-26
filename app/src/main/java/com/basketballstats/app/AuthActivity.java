package com.basketballstats.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseUser;
import com.basketballstats.app.auth.AuthController;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * AuthActivity - Main Authentication Screen (New App Launcher)
 * 
 * This activity serves as the new app entry point and handles:
 * - Session checking (auto-login for existing users)
 * - Email/password authentication 
 * - Google Sign-In integration
 * - Guest/anonymous access
 * - Navigation to MainActivity after successful authentication
 */
public class AuthActivity extends Activity {
    
    private static final String TAG = "AuthActivity";
    private static final int RC_SIGN_IN = 9001;
    
    // UI Components
    private EditText etEmail, etPassword, etDisplayName;
    private Button btnLogin, btnRegister, btnGoogleSignIn, btnGuest, btnToggleMode;
    private TextView tvTitle, tvModeToggle, tvError;
    private ProgressBar progressBar;
    
    // Authentication
    private AuthController authController;
    private GoogleSignInClient googleSignInClient;
    
    // UI State
    private boolean isLoginMode = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AuthActivity.onCreate() - START");
        
        // Initialize authentication controller
        authController = AuthController.getInstance(this);
        
        // Check if user is already authenticated
        if (authController.isUserAuthenticated()) {
            Log.d(TAG, "User already authenticated, navigating to MainActivity");
            navigateToMainActivity();
            return;
        }
        
        // User not authenticated, show auth UI
        setContentView(R.layout.activity_auth);
        
        // Initialize UI and Google Sign-In
        initializeViews();
        setupGoogleSignIn();
        setupEventListeners();
        
        Log.d(TAG, "AuthActivity.onCreate() - Auth UI displayed");
    }
    
    /**
     * Initialize UI components
     */
    private void initializeViews() {
        // Input fields
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etDisplayName = findViewById(R.id.etDisplayName);
        
        // Buttons
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnGuest = findViewById(R.id.btnGuest);
        btnToggleMode = findViewById(R.id.btnToggleMode);
        
        // Text views
        tvTitle = findViewById(R.id.tvTitle);
        tvModeToggle = findViewById(R.id.tvModeToggle);
        tvError = findViewById(R.id.tvError);
        
        // Progress bar
        progressBar = findViewById(R.id.progressBar);
        
        // Set initial state (login mode)
        updateUIForMode();
    }
    
    /**
     * Setup Google Sign-In configuration
     */
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    
    /**
     * Setup event listeners for UI components
     */
    private void setupEventListeners() {
        // Login button
        btnLogin.setOnClickListener(v -> handleEmailPasswordAuth(true));
        
        // Register button  
        btnRegister.setOnClickListener(v -> handleEmailPasswordAuth(false));
        
        // Google Sign-In button
        btnGoogleSignIn.setOnClickListener(v -> startGoogleSignIn());
        
        // Guest button
        btnGuest.setOnClickListener(v -> handleGuestAccess());
        
        // Toggle between login/register mode
        btnToggleMode.setOnClickListener(v -> toggleAuthMode());
    }
    
    /**
     * Handle email/password authentication (login or register)
     */
    private void handleEmailPasswordAuth(boolean isLogin) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        
        // Input validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password are required");
            return;
        }
        
        if (!isLogin && displayName.isEmpty()) {
            showError("Display name is required for registration");
            return;
        }
        
        // Show loading state
        setLoadingState(true);
        clearError();
        
        AuthController.AuthCallback callback = new AuthController.AuthCallback() {
            @Override
            public void onAuthSuccess(FirebaseUser user, String message) {
                setLoadingState(false);
                Toast.makeText(AuthActivity.this, message, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Auth success: " + message);
                navigateToMainActivity();
            }
            
            @Override
            public void onAuthError(String errorMessage) {
                setLoadingState(false);
                showError(errorMessage);
                Log.e(TAG, "Auth error: " + errorMessage);
            }
        };
        
        // Perform authentication
        if (isLogin) {
            authController.signInUser(email, password, callback);
        } else {
            authController.registerUser(email, password, displayName, callback);
        }
    }
    
    /**
     * Start Google Sign-In flow
     */
    private void startGoogleSignIn() {
        setLoadingState(true);
        clearError();
        
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    
    /**
     * Handle Google Sign-In result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                setLoadingState(false);
                showError("Google Sign-In failed: " + e.getMessage());
                Log.e(TAG, "Google Sign-In failed", e);
            }
        }
    }
    
    /**
     * Authenticate with Firebase using Google credentials
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    setLoadingState(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            // Create user profile in SQLite
                            authController.getUserProfile(); // This will create profile if needed
                            Toast.makeText(this, "Welcome " + user.getDisplayName() + "!", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        }
                    } else {
                        showError("Google authentication failed");
                        Log.e(TAG, "Google authentication failed", task.getException());
                    }
                });
    }
    
    /**
     * Handle guest/anonymous access
     */
    private void handleGuestAccess() {
        setLoadingState(true);
        clearError();
        
        authController.signInAnonymously(new AuthController.AuthCallback() {
            @Override
            public void onAuthSuccess(FirebaseUser user, String message) {
                setLoadingState(false);
                Toast.makeText(AuthActivity.this, message, Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            }
            
            @Override
            public void onAuthError(String errorMessage) {
                setLoadingState(false);
                showError("Guest access failed: " + errorMessage);
            }
        });
    }
    
    /**
     * Toggle between login and register modes
     */
    private void toggleAuthMode() {
        isLoginMode = !isLoginMode;
        updateUIForMode();
        clearError();
    }
    
    /**
     * Update UI based on current mode (login vs register)
     */
    private void updateUIForMode() {
        if (isLoginMode) {
            // Login mode
            tvTitle.setText("Sign In");
            etDisplayName.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.GONE);
            tvModeToggle.setText("Don't have an account?");
            btnToggleMode.setText("Sign Up");
        } else {
            // Register mode
            tvTitle.setText("Create Account");
            etDisplayName.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);
            btnRegister.setVisibility(View.VISIBLE);
            tvModeToggle.setText("Already have an account?");
            btnToggleMode.setText("Sign In");
        }
    }
    
    /**
     * Navigate to MainActivity after successful authentication
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Show/hide loading state
     */
    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        
        // Disable/enable buttons during loading
        btnLogin.setEnabled(!isLoading);
        btnRegister.setEnabled(!isLoading);
        btnGoogleSignIn.setEnabled(!isLoading);
        btnGuest.setEnabled(!isLoading);
        btnToggleMode.setEnabled(!isLoading);
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
    
    /**
     * Clear error message
     */
    private void clearError() {
        tvError.setVisibility(View.GONE);
    }
}
