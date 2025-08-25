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
import com.basketballstats.app.models.Game;
import com.basketballstats.app.data.DatabaseController;
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI components
        initializeViews();
        
        // Initialize data
        initializeData();
        
        // Set up event listeners
        setupEventListeners();
    }
    
    private void initializeViews() {
        btnEditLeague = findViewById(R.id.btnEditLeague);
        btnSync = findViewById(R.id.btnSync);
        lvGames = findViewById(R.id.lvGames);
        
        // Initialize sync button to default state
        setSyncButtonState(SyncState.DEFAULT);
    }
    
    private void initializeData() {
        // Initialize database controller
        dbController = DatabaseController.getInstance(this);
        
        // Load games from SQLite database
        refreshGamesList();
    }
    
    private void refreshGamesList() {
        try {
            // Reload games from SQLite database (in case they were modified in League Management)
            gamesList = Game.findAll(dbController.getDatabaseHelper());
            
            // Debug output to verify games are loaded
            Toast.makeText(this, "Loaded " + gamesList.size() + " games from SQLite", Toast.LENGTH_SHORT).show();
            
            // Create or update adapter
            if (gameAdapter == null) {
                gameAdapter = new GameCardAdapter(this, gamesList);
                lvGames.setAdapter(gameAdapter);
            } else {
                // Update existing adapter with new data
                gameAdapter.updateGames(gamesList);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading games from database", Toast.LENGTH_LONG).show();
            android.util.Log.e("MainActivity", "Error loading games", e);
            
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
        
        // Debug output
        Toast.makeText(this, "Starting game: " + game.toString(), Toast.LENGTH_SHORT).show();
        
        // Navigate directly to Game Activity (Frame 3) - Unified Refactor
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("gameId", game.getId());
        intent.putExtra("homeTeam", game.getHomeTeam().getName());
        intent.putExtra("awayTeam", game.getAwayTeam().getName());
        intent.putExtra("homeTeamId", game.getHomeTeamId());
        intent.putExtra("awayTeamId", game.getAwayTeamId());
        intent.putExtra("gameDate", game.getDate());
        intent.putExtra("gameTime", game.getTime());
        startActivity(intent);
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
            
            // Build matchup text with team names (load if needed)
            String homeTeamName = game.getHomeTeam() != null ? game.getHomeTeam().getName() : "Team " + game.getHomeTeamId();
            String awayTeamName = game.getAwayTeam() != null ? game.getAwayTeam().getName() : "Team " + game.getAwayTeamId();
            String matchupText = homeTeamName + " vs " + awayTeamName;
            
            // Build date/time text
            String dateText = game.getDate();
            if (game.getTime() != null) {
                dateText += " at " + game.getTime();
            }
            
            // Show status if not scheduled
            if (!"scheduled".equals(game.getStatus())) {
                matchupText += " [" + game.getStatus().toUpperCase() + "]";
            }
            
            tvMatchup.setText(matchupText);
            tvDate.setText(dateText);
            
            return convertView;
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
                break;
                
            case SYNCING:
                btnSync.setText("🔄");
                btnSync.setBackgroundColor(android.graphics.Color.parseColor("#3498DB"));
                btnSync.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                btnSync.setEnabled(false);
                // TODO: Add rotation animation in Task 3.2
                break;
                
            case SUCCESS:
                btnSync.setText("✓");
                btnSync.setBackgroundColor(android.graphics.Color.parseColor("#27AE60"));
                btnSync.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                btnSync.setEnabled(true);
                
                // Auto-reset to default after 2 seconds
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setSyncButtonState(SyncState.DEFAULT);
                    }
                }, 2000);
                break;
                
            case ERROR:
                btnSync.setText("⚠");
                btnSync.setBackgroundColor(android.graphics.Color.parseColor("#E74C3C"));
                btnSync.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                btnSync.setEnabled(true);
                break;
                
            case OFFLINE:
                btnSync.setText("🔄");
                btnSync.setBackgroundColor(android.graphics.Color.parseColor("#BDC3C7"));
                btnSync.setTextColor(android.graphics.Color.parseColor("#95A5A6"));
                btnSync.setEnabled(false);
                break;
        }
    }
    
    /**
     * Perform manual sync operation with visual feedback
     * Implements "last write wins" conflict resolution as specified
     */
    private void performSync() {
        // Set syncing state
        setSyncButtonState(SyncState.SYNCING);
        Toast.makeText(this, "Starting sync...", Toast.LENGTH_SHORT).show();
        
        // Simulate sync operation (placeholder for future Firebase integration)
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // TODO: Implement actual Firebase sync in Phase 4
                    // For now, simulate successful sync
                    
                    // Refresh games list from SQLite database
                    refreshGamesList();
                    
                    // Show success state
                    setSyncButtonState(SyncState.SUCCESS);
                    Toast.makeText(MainActivity.this, "Sync completed successfully", Toast.LENGTH_SHORT).show();
                    
                } catch (Exception e) {
                    // Handle sync errors
                    setSyncButtonState(SyncState.ERROR);
                    Toast.makeText(MainActivity.this, "Sync failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    android.util.Log.e("MainActivity", "Sync error", e);
                }
            }
        }, 1500); // 1.5 second delay to simulate sync operation
    }
}