package com.example.myapp;

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
import com.example.myapp.models.SimpleGame;
import com.example.myapp.data.LeagueDataProvider;
import java.util.List;

/**
 * Main activity - Clean Game Selection (Frame 1) - SIMPLE & SLEEK
 * One-tap game selection with clean card-based interface
 */
public class MainActivity extends Activity {
    
    // UI Components
    private Button btnEditLeague;
    private ListView lvGames;
    
    // Data
    private List<SimpleGame> gamesList;
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
        lvGames = findViewById(R.id.lvGames);
    }
    
    private void initializeData() {
        // Load simple games from league database
        refreshGamesList();
    }
    
    private void refreshGamesList() {
        // Reload games from data provider (in case they were modified in League Management)
        gamesList = LeagueDataProvider.getAvailableGames();
        
        // Debug output to verify games are loaded
        Toast.makeText(this, "Loaded " + gamesList.size() + " games", Toast.LENGTH_SHORT).show();
        
        // Create or update adapter
        if (gameAdapter == null) {
            gameAdapter = new GameCardAdapter(this, gamesList);
            lvGames.setAdapter(gameAdapter);
        } else {
            // Update existing adapter with new data
            gameAdapter.updateGames(gamesList);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh games list when returning from League Management or other activities
        refreshGamesList();
    }
    
    private void setupEventListeners() {
        // Edit League button - navigate to League Management (placeholder)
        btnEditLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLeagueManagement();
            }
        });
        
        // Game selection - one tap to proceed to player selection
        lvGames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SimpleGame selectedGame = gamesList.get(position);
                proceedToPlayerSelection(selectedGame);
            }
        });
    }
    
    private void proceedToPlayerSelection(SimpleGame game) {
        // Verify game still exists (in case it was deleted in League Management)
        if (game == null || game.getHomeTeam() == null || game.getAwayTeam() == null) {
            Toast.makeText(this, "Error: Game no longer available", Toast.LENGTH_SHORT).show();
            refreshGamesList(); // Refresh to show current games
            return;
        }
        
        // Debug output
        Toast.makeText(this, "Game selected: " + game.getMatchupText(), Toast.LENGTH_SHORT).show();
        
        // Navigate directly to Game Roster Activity (Frame 2)
        Intent intent = new Intent(this, GameRosterActivity.class);
        intent.putExtra("gameId", game.getId());
        intent.putExtra("homeTeam", game.getHomeTeam().getName());
        intent.putExtra("awayTeam", game.getAwayTeam().getName());
        startActivity(intent);
    }
    
    private void openLeagueManagement() {
        // Navigate to League Management Activity
        Intent intent = new Intent(this, LeagueManagementActivity.class);
        startActivity(intent);
    }
    
    /**
     * Custom adapter for clean game card display
     */
    private class GameCardAdapter extends BaseAdapter {
        private Context context;
        private List<SimpleGame> games;
        private LayoutInflater inflater;
        
        public GameCardAdapter(Context context, List<SimpleGame> games) {
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
            
            SimpleGame game = games.get(position);
            
            TextView tvMatchup = convertView.findViewById(R.id.tvMatchup);
            TextView tvDate = convertView.findViewById(R.id.tvDate);
            
            tvMatchup.setText(game.getMatchupText());
            tvDate.setText(game.getDateText());
            
            return convertView;
        }
        
        /**
         * Update games list and refresh adapter
         */
        public void updateGames(List<SimpleGame> newGames) {
            this.games = newGames;
            notifyDataSetChanged();
        }
    }
}