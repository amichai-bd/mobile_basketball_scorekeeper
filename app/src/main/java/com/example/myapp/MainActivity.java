package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.example.myapp.models.ScheduledGame;
import com.example.myapp.data.LeagueDataProvider;
import java.util.List;

/**
 * Main activity - Game Schedule Selection (Frame 1) - SPECIFICATION ALIGNED
 * Game selection from pre-configured league database with Edit League management
 */
public class MainActivity extends Activity {
    
    // UI Components
    private Button btnEditLeague, btnStartGame;
    private ListView lvScheduledGames;
    
    // Data
    private List<ScheduledGame> scheduledGamesList;
    private ArrayAdapter<ScheduledGame> scheduledGamesAdapter;
    private ScheduledGame selectedGame = null;
    
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
        btnStartGame = findViewById(R.id.btnStartGame);
        lvScheduledGames = findViewById(R.id.lvScheduledGames);
    }
    
    private void initializeData() {
        // Load scheduled games from league database
        scheduledGamesList = LeagueDataProvider.getScheduledGames();
        
        // Create adapter for scheduled games list
        scheduledGamesAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_single_choice, scheduledGamesList);
        lvScheduledGames.setAdapter(scheduledGamesAdapter);
    }
    
    private void setupEventListeners() {
        // Edit League button - navigate to League Management (placeholder)
        btnEditLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLeagueManagement();
            }
        });
        
        // Start Game button - start selected scheduled game
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSelectedGame();
            }
        });
        
        // Game selection listener - only allow selection of scheduled games
        lvScheduledGames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScheduledGame game = scheduledGamesList.get(position);
                // Only allow selection of scheduled games
                if (game.isScheduled()) {
                    onGameSelected(position);
                } else {
                    String status = game.isCompleted() ? "completed" : "in progress";
                    Toast.makeText(MainActivity.this, 
                        "Cannot select " + status + " games", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void onGameSelected(int position) {
        selectedGame = scheduledGamesList.get(position);
        
        // Enable Start Game button only for scheduled games
        if (selectedGame.isScheduled()) {
            btnStartGame.setEnabled(true);
            Toast.makeText(this, "Game selected: " + selectedGame.toString(), Toast.LENGTH_SHORT).show();
        } else {
            btnStartGame.setEnabled(false);
            String status = selectedGame.isCompleted() ? "already completed" : "in progress";
            Toast.makeText(this, "Cannot start game - " + status, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void startSelectedGame() {
        if (selectedGame == null || !selectedGame.isScheduled()) {
            Toast.makeText(this, "Please select a scheduled game", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Navigate to Game Roster Activity (Frame 2) - Don't change status yet
        // Game status will change to "In Progress" only when actual game recording starts
        Intent intent = new Intent(this, GameRosterActivity.class);
        intent.putExtra("gameId", selectedGame.getId());
        intent.putExtra("homeTeam", selectedGame.getHomeTeam().getName());
        intent.putExtra("awayTeam", selectedGame.getAwayTeam().getName());
        startActivity(intent);
        
        Toast.makeText(this, "Proceeding to roster selection: " + selectedGame.toString(), Toast.LENGTH_SHORT).show();
        
        // Note: Don't change game status here - user can still back out
        // Status will change in Frame 3 when actual game recording begins
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the game list when returning from other activities
        if (scheduledGamesAdapter != null) {
            scheduledGamesAdapter.notifyDataSetChanged();
        }
        // Reset selection state
        btnStartGame.setEnabled(false);
        selectedGame = null;
    }
    
    private void openLeagueManagement() {
        // Placeholder for League Management interface
        Toast.makeText(this, "League Management interface coming soon!", Toast.LENGTH_LONG).show();
        
        // TODO: Navigate to League Management Activity
        // Intent intent = new Intent(this, LeagueManagementActivity.class);
        // startActivity(intent);
    }
}