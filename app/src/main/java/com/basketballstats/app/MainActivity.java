package com.basketballstats.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import com.basketballstats.app.models.Game;
import com.basketballstats.app.data.DatabaseController;
import com.basketballstats.app.sync.SyncManager;
import java.util.List;

/**
 * Main activity - Clean Game Selection (Frame 1) with SQLite Database
 * One-tap game selection with clean card-based interface
 * Uses SQLite database for persistent game storage and management
 */
public class MainActivity extends Activity {
    
    // Sync States Enum
    public enum SyncState {
        DEFAULT,    // Grey sync icon (ready to sync)
        SYNCING,    // Blue rotating animation
        SUCCESS,    // Green checkmark (2 seconds)
        ERROR,      // Red warning icon
        OFFLINE     // Greyed out (offline indicator)
    }
    
    // UI Components
    private Button btnEditLeague;
    private Button btnSync;
    private ListView lvGames;
    
    // Database and Data
    private DatabaseController dbController;
    private List<Game> gamesList;
    private GameCardAdapter gameAdapter;
    
    // Sync infrastructure
    private SyncManager syncManager;
    private RotateAnimation syncRotationAnimation;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("DEBUG", "MainActivity.onCreate() - START");
        
        try {
            setContentView(R.layout.activity_main);
            android.util.Log.d("DEBUG", "MainActivity.onCreate() - setContentView OK");
            Toast.makeText(this, "Step 1: setContentView OK", Toast.LENGTH_SHORT).show();
            
            // Initialize UI components
            initializeViews();
            android.util.Log.d("DEBUG", "MainActivity.onCreate() - initializeViews OK");
            Toast.makeText(this, "Step 2: initializeViews OK", Toast.LENGTH_SHORT).show();
            
            // Initialize data
            initializeData();
            android.util.Log.d("DEBUG", "MainActivity.onCreate() - initializeData OK");
            Toast.makeText(this, "Step 3: initializeData OK", Toast.LENGTH_SHORT).show();
            
            // Set up event listeners
            setupEventListeners();
            android.util.Log.d("DEBUG", "MainActivity.onCreate() - setupEventListeners OK");
            Toast.makeText(this, "Step 4: setupEventListeners OK", Toast.LENGTH_SHORT).show();
            
            android.util.Log.d("DEBUG", "MainActivity.onCreate() - COMPLETE SUCCESS");
            Toast.makeText(this, "✅ App Started Successfully!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            android.util.Log.e("DEBUG", "MainActivity.onCreate() - CRASH", e);
            throw e;
        }
    }
    
    private void initializeViews() {
        btnEditLeague = findViewById(R.id.btnEditLeague);
        btnSync = findViewById(R.id.btnSync);
        lvGames = findViewById(R.id.lvGames);
        
        // Initialize sync rotation animation
        setupSyncAnimation();
        
        // Initialize sync button to default state
        setSyncButtonState(SyncState.DEFAULT);
    }
    
    /**
     * Setup rotation animation for sync button
     */
    private void setupSyncAnimation() {
        syncRotationAnimation = new RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        syncRotationAnimation.setDuration(1000); // 1 second per rotation
        syncRotationAnimation.setRepeatCount(Animation.INFINITE);
        syncRotationAnimation.setRepeatMode(Animation.RESTART);
    }
    
    private void initializeData() {
        try {
            android.util.Log.d("DEBUG", "initializeData() - START");
            
            // Initialize database controller
            dbController = DatabaseController.getInstance(this);
            android.util.Log.d("DEBUG", "initializeData() - Database controller OK");
            
            // Load games from SQLite database first (core functionality)
            refreshGamesList();
            android.util.Log.d("DEBUG", "initializeData() - refreshGamesList OK");
            
            // Initialize sync manager in background (optional for core functionality)
            initializeSyncManagerSafely();
            android.util.Log.d("DEBUG", "initializeData() - initializeSyncManagerSafely OK");
            
        } catch (Exception e) {
            android.util.Log.e("DEBUG", "initializeData() - ERROR", e);
            Toast.makeText(this, "Starting in offline mode", Toast.LENGTH_SHORT).show();
            
            // Ensure we have empty lists as fallback
            if (gamesList == null) {
                gamesList = new java.util.ArrayList<>();
            }
        }
    }
    
    private void initializeSyncManagerSafely() {
        try {
            // Initialize sync manager in background with delay to ensure Firebase is ready
            new Thread(() -> {
                try {
                    // Give Firebase time to initialize
                    Thread.sleep(1000);
                    
                    // Verify Firebase is available before initializing sync
                    try {
                        com.google.firebase.FirebaseApp.getInstance();
                    } catch (Exception e) {
                        android.util.Log.w("MainActivity", "Firebase not available, using offline mode");
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Firebase unavailable - working offline", Toast.LENGTH_SHORT).show();
                            setSyncButtonState(SyncState.OFFLINE);
                        });
                        return;
                    }
                    
                    syncManager = SyncManager.getInstance(MainActivity.this);
                    android.util.Log.d("MainActivity", "Sync manager initialized");
                    
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Sync ready", Toast.LENGTH_SHORT).show();
                    });
                    
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Sync manager initialization failed", e);
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Sync unavailable - working offline", Toast.LENGTH_SHORT).show();
                        setSyncButtonState(SyncState.OFFLINE);
                    });
                }
            }).start();
            
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to start sync initialization", e);
            setSyncButtonState(SyncState.OFFLINE);
        }
    }
    
    private void refreshGamesList() {
        try {
            android.util.Log.d("DEBUG", "refreshGamesList() - START");
            
            // Reload games from SQLite database (in case they were modified in League Management)
            gamesList = Game.findAll(dbController.getDatabaseHelper());
            android.util.Log.d("DEBUG", "refreshGamesList() - Game.findAll OK, count: " + gamesList.size());
            
            // Debug output to verify games are loaded
            Toast.makeText(this, "Loaded " + gamesList.size() + " games from SQLite", Toast.LENGTH_SHORT).show();
            
            // Create or update adapter
            if (gameAdapter == null) {
                android.util.Log.d("DEBUG", "refreshGamesList() - Creating new adapter");
                gameAdapter = new GameCardAdapter(this, gamesList);
                lvGames.setAdapter(gameAdapter);
                android.util.Log.d("DEBUG", "refreshGamesList() - Adapter set OK");
            } else {
                android.util.Log.d("DEBUG", "refreshGamesList() - Updating existing adapter");
                // Update existing adapter with new data
                gameAdapter.updateGames(gamesList);
                android.util.Log.d("DEBUG", "refreshGamesList() - Adapter updated OK");
            }
        } catch (Exception e) {
            android.util.Log.e("DEBUG", "refreshGamesList() - ERROR", e);
            Toast.makeText(this, "Error loading games from database", Toast.LENGTH_LONG).show();
            
            // Initialize empty list as fallback
            if (gamesList == null) {
                gamesList = new java.util.ArrayList<>();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh games list when returning from League Management or other activities
        refreshGamesList();
    }
    
    private void setupEventListeners() {
        // Sync button - manual Firebase synchronization
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSync();
            }
        });
        
        // Sync button long-press - demonstrate all visual states (for testing)
        btnSync.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                demonstrateSyncStates();
                return true; // Consume the long click event
            }
        });
        
        // Edit League button - navigate to League Management
        btnEditLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLeagueManagement();
            }
        });
        
        // Game selection - one tap to proceed directly to game screen
        lvGames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Game selectedGame = gamesList.get(position);
                proceedToGameScreen(selectedGame);
            }
        });
    }
    
    private void proceedToGameScreen(Game game) {
        // Verify game still exists (in case it was deleted in League Management)
        if (game == null) {
            Toast.makeText(this, "Error: Game no longer available", Toast.LENGTH_SHORT).show();
            refreshGamesList(); // Refresh to show current games
            return;
        }
        
        // Ensure teams are loaded
        if (game.getHomeTeam() == null || game.getAwayTeam() == null) {
            game.loadTeams(dbController.getDatabaseHelper());
        }
        
        // Verify teams loaded successfully
        if (game.getHomeTeam() == null || game.getAwayTeam() == null) {
            Toast.makeText(this, "Error: Game teams not found in database", Toast.LENGTH_LONG).show();
            refreshGamesList(); // Refresh to show current games
            return;
        }
        
        // Determine navigation mode based on game status
        String navigationMode = getNavigationMode(game.getStatus());
        String statusMessage = getStatusMessage(game.getStatus());
        
        // Debug output with status information
        Toast.makeText(this, statusMessage + ": " + game.toString(), Toast.LENGTH_SHORT).show();
        
        // Navigate to Game Activity with status-aware mode
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("gameId", game.getId());
        intent.putExtra("homeTeam", game.getHomeTeam().getName());
        intent.putExtra("awayTeam", game.getAwayTeam().getName());
        intent.putExtra("homeTeamId", game.getHomeTeamId());
        intent.putExtra("awayTeamId", game.getAwayTeamId());
        intent.putExtra("gameDate", game.getDate());
        intent.putExtra("gameTime", game.getTime());
        intent.putExtra("navigationMode", navigationMode); // NEW: Status-aware navigation
        startActivity(intent);
    }
    
    /**
     * Determine navigation mode based on game status
     * Used by GameActivity to initialize in correct mode
     */
    private String getNavigationMode(String gameStatus) {
        switch (gameStatus) {
            case "not_started":
                return "setup"; // Go to Setup Mode (select players)
                
            case "game_in_progress":
                return "resume"; // Go to Game Mode with state restoration
                
            case "done":
                return "review"; // Go to Game Mode with full edit access
                
            default:
                // Handle invalid/legacy status values - default to setup
                return "setup";
        }
    }
    
    /**
     * Get user-friendly status message for navigation feedback
     */
    private String getStatusMessage(String gameStatus) {
        switch (gameStatus) {
            case "not_started":
                return "Starting new game";
                
            case "game_in_progress":
                return "Resuming game";
                
            case "done":
                return "Reviewing completed game";
                
            default:
                return "Opening game";
        }
    }
    
    private void openLeagueManagement() {
        // Navigate to League Management Activity
        Intent intent = new Intent(this, LeagueManagementActivity.class);
        startActivity(intent);
    }
    
    /**
     * Custom adapter for clean game card display with SQLite Game objects
     */
    private class GameCardAdapter extends BaseAdapter {
        private Context context;
        private List<Game> games;
        private LayoutInflater inflater;
        
        public GameCardAdapter(Context context, List<Game> games) {
            this.context = context;
            this.games = games;
            this.inflater = LayoutInflater.from(context);
        }
        
        @Override
        public int getCount() {
            return games.size();
        }
        
        @Override
        public Object getItem(int position) {
            return games.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return games.get(position).getId();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_game_card, parent, false);
            }
            
            Game game = games.get(position);
            
            TextView tvMatchup = convertView.findViewById(R.id.tvMatchup);
            TextView tvDate = convertView.findViewById(R.id.tvDate);
            TextView tvStatus = convertView.findViewById(R.id.tvStatus);
            
            // Build matchup text with team names (load if needed)
            String homeTeamName = game.getHomeTeam() != null ? game.getHomeTeam().getName() : "Team " + game.getHomeTeamId();
            String awayTeamName = game.getAwayTeam() != null ? game.getAwayTeam().getName() : "Team " + game.getAwayTeamId();
            String matchupText = homeTeamName + " vs " + awayTeamName;
            
            // Build date/time text
            String dateText = game.getDate();
            if (game.getTime() != null) {
                dateText += " at " + game.getTime();
            }
            
            // Set status with color coding according to specification
            setGameStatus(tvStatus, game.getStatus());
            
            tvMatchup.setText(matchupText);
            tvDate.setText(dateText);
            
            return convertView;
        }
        
        /**
         * Set game status with proper color coding
         * - "Not Started": Grey background, white text
         * - "Game In Progress": Blue background, white text  
         * - "Done": Green background, white text
         */
        private void setGameStatus(TextView tvStatus, String status) {
            switch (status) {
                case "not_started":
                    tvStatus.setText("Not Started");
                    tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#9E9E9E")); // Grey
                    tvStatus.setTextColor(android.graphics.Color.WHITE);
                    break;
                    
                case "game_in_progress":
                    tvStatus.setText("Game In Progress");
                    tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#2196F3")); // Blue
                    tvStatus.setTextColor(android.graphics.Color.WHITE);
                    break;
                    
                case "done":
                    tvStatus.setText("Done");
                    tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")); // Green
                    tvStatus.setTextColor(android.graphics.Color.WHITE);
                    break;
                    
                default:
                    // Handle invalid/legacy status values
                    tvStatus.setText("Unknown");
                    tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#FF9800")); // Orange warning
                    tvStatus.setTextColor(android.graphics.Color.WHITE);
                    break;
            }
        }
        
        /**
         * Update games list and refresh adapter
         */
        public void updateGames(List<Game> newGames) {
            this.games = newGames;
            notifyDataSetChanged();
        }
    }
    
    /**
     * Set sync button visual state according to sync operation status
     */
    private void setSyncButtonState(SyncState state) {
        switch (state) {
            case DEFAULT:
                btnSync.setText("🔄");
                btnSync.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
                btnSync.setTextColor(android.graphics.Color.parseColor("#7F8C8D"));
                btnSync.setEnabled(true);
                // Stop any animation
                btnSync.clearAnimation();
                break;
                
            case SYNCING:
                btnSync.setText("🔄");
                btnSync.setBackgroundColor(android.graphics.Color.parseColor("#3498DB"));
                btnSync.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                btnSync.setEnabled(false);
                // Start rotation animation
                btnSync.startAnimation(syncRotationAnimation);
                break;
                
            case SUCCESS:
                btnSync.setText("✅");
                btnSync.setBackgroundColor(android.graphics.Color.parseColor("#27AE60"));
                btnSync.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                btnSync.setEnabled(true);
                // Stop any animation
                btnSync.clearAnimation();
                
                // Auto-reset to default after 2 seconds
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setSyncButtonState(SyncState.DEFAULT);
                    }
                }, 2000);
                break;
                
            case ERROR:
                btnSync.setText("⚠️");
                btnSync.setBackgroundColor(android.graphics.Color.parseColor("#E74C3C"));
                btnSync.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                btnSync.setEnabled(true);
                // Stop any animation
                btnSync.clearAnimation();
                break;
                
            case OFFLINE:
                btnSync.setText("📴");
                btnSync.setBackgroundColor(android.graphics.Color.parseColor("#BDC3C7"));
                btnSync.setTextColor(android.graphics.Color.parseColor("#95A5A6"));
                btnSync.setEnabled(false);
                // Stop any animation
                btnSync.clearAnimation();
                break;
        }
    }
    
    /**
     * Perform manual sync operation using SyncManager with visual feedback
     * Implements "last write wins" conflict resolution as specified
     */
    private void performSync() {
        // Check if sync manager is available
        if (syncManager == null) {
            Toast.makeText(this, "Sync not available - initialize first", Toast.LENGTH_SHORT).show();
            setSyncButtonState(SyncState.OFFLINE);
            return;
        }
        
        // Prevent multiple sync operations
        if (btnSync.getAnimation() != null) {
            Toast.makeText(this, "Sync already in progress...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Use SyncManager with callback interface for comprehensive feedback
        syncManager.performManualSync(new SyncManager.SyncCallback() {
            @Override
            public void onSyncStarted() {
                setSyncButtonState(SyncState.SYNCING);
                Toast.makeText(MainActivity.this, "🔄 Starting sync with Firebase...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSyncProgress(String message) {
                // Update UI with progress (can add progress indicator in Phase 8)
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSyncSuccess(String message) {
                setSyncButtonState(SyncState.SUCCESS);
                Toast.makeText(MainActivity.this, "✅ " + message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSyncError(String errorMessage) {
                setSyncButtonState(SyncState.ERROR);
                Toast.makeText(MainActivity.this, "❌ " + errorMessage, Toast.LENGTH_LONG).show();
                android.util.Log.e("MainActivity", "Sync error: " + errorMessage);
            }

            @Override
            public void onSyncComplete() {
                // Refresh games list from SQLite database after sync
                refreshGamesList();
            }
        });
    }
    
    /**
     * Demo method to showcase all sync button states (for testing)
     * Long-press the sync button to trigger this demo
     */
    private void demonstrateSyncStates() {
        Toast.makeText(this, "🎨 Demonstrating sync button states...", Toast.LENGTH_SHORT).show();
        
        // Show each state for 1.5 seconds
        final SyncState[] states = {SyncState.SYNCING, SyncState.SUCCESS, SyncState.ERROR, SyncState.OFFLINE, SyncState.DEFAULT};
        final String[] stateNames = {"SYNCING (with rotation)", "SUCCESS (auto-reset)", "ERROR", "OFFLINE", "DEFAULT"};
        
        for (int i = 0; i < states.length; i++) {
            final int index = i;
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setSyncButtonState(states[index]);
                    Toast.makeText(MainActivity.this, "State: " + stateNames[index], Toast.LENGTH_SHORT).show();
                }
            }, i * 1500);
        }
    }
}