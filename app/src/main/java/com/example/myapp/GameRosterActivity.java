package com.example.myapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapp.models.Player;
import com.example.myapp.models.Team;
import com.example.myapp.models.TeamPlayer;
import com.example.myapp.data.LeagueDataProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Game Roster Activity - Modern Player Selection (Frame 2) - SLEEK & INSTANT
 * Teams are pre-selected, instant player card selection with visual feedback
 */
public class GameRosterActivity extends Activity {
    
    // UI Components
    private TextView tvTeamATitle, tvTeamBTitle;
    private TextView tvTeamACounter, tvTeamBCounter;
    private GridLayout gridTeamAPlayers, gridTeamBPlayers;
    private LinearLayout teamASection, teamBSection;
    private Button btnStartGame;
    
    // Data
    private int gameId;
    private String homeTeamName, awayTeamName;
    private Team preselectedTeamA, preselectedTeamB;
    private List<TeamPlayer> selectedTeamAPlayers, selectedTeamBPlayers;
    private List<View> teamAPlayerCards, teamBPlayerCards;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_roster);
        
        // Get game data from intent
        getGameDataFromIntent();
        
        // Initialize UI components
        initializeViews();
        
        // Initialize data
        initializeData();
        
        // Set up team displays and player lists
        setupPreselectedTeams();
        
        // Set up event listeners
        setupEventListeners();
    }
    
    private void getGameDataFromIntent() {
        gameId = getIntent().getIntExtra("gameId", 1);
        homeTeamName = getIntent().getStringExtra("homeTeam");
        awayTeamName = getIntent().getStringExtra("awayTeam");
        
        // Default values if not provided
        if (homeTeamName == null) homeTeamName = "Home Team";
        if (awayTeamName == null) awayTeamName = "Away Team";
    }
    
    private void initializeViews() {
        // Team displays
        tvTeamATitle = findViewById(R.id.tvTeamATitle);
        tvTeamBTitle = findViewById(R.id.tvTeamBTitle);
        
        // Selection counters
        tvTeamACounter = findViewById(R.id.tvTeamACounter);
        tvTeamBCounter = findViewById(R.id.tvTeamBCounter);
        
        // Player grids
        gridTeamAPlayers = findViewById(R.id.gridTeamAPlayers);
        gridTeamBPlayers = findViewById(R.id.gridTeamBPlayers);
        
        // Team sections (for background color changes)
        teamASection = findViewById(R.id.teamASection);
        teamBSection = findViewById(R.id.teamBSection);
        
        // Start game button
        btnStartGame = findViewById(R.id.btnStartGame);
    }
    
    private void initializeData() {
        // Get pre-selected teams from league data based on team names
        preselectedTeamA = LeagueDataProvider.getTeamByName(homeTeamName);
        preselectedTeamB = LeagueDataProvider.getTeamByName(awayTeamName);
        
        // Initialize selected player lists
        selectedTeamAPlayers = new ArrayList<>();
        selectedTeamBPlayers = new ArrayList<>();
        
        // Initialize player card lists
        teamAPlayerCards = new ArrayList<>();
        teamBPlayerCards = new ArrayList<>();
    }
    
    private void setupPreselectedTeams() {
        // Update team title displays with actual team names
        tvTeamATitle.setText(homeTeamName + " (Home)");
        tvTeamBTitle.setText(awayTeamName + " (Away)");
        
        // Automatically populate player grids for both teams
        populateTeamAPlayerCards();
        populateTeamBPlayerCards();
        
        // Update counters
        updateTeamAStatus();
        updateTeamBStatus();
    }
    
    private void setupEventListeners() {
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartGameConfirmation();
            }
        });
    }
    
    private void populateTeamAPlayerCards() {
        if (preselectedTeamA == null) {
            Toast.makeText(this, "Error: Team A not found in league data", Toast.LENGTH_LONG).show();
            return;
        }
        
        gridTeamAPlayers.removeAllViews();
        teamAPlayerCards.clear();
        
        // Create modern player cards for all 12 players from pre-selected Team A
        for (TeamPlayer player : preselectedTeamA.getPlayers()) {
            View playerCard = createPlayerCard(player);
            
            playerCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTeamAPlayerCardClicked(player, v);
                }
            });
            
            teamAPlayerCards.add(playerCard);
            gridTeamAPlayers.addView(playerCard);
        }
    }
    
    private void populateTeamBPlayerCards() {
        if (preselectedTeamB == null) {
            Toast.makeText(this, "Error: Team B not found in league data", Toast.LENGTH_LONG).show();
            return;
        }
        
        gridTeamBPlayers.removeAllViews();
        teamBPlayerCards.clear();
        
        // Create modern player cards for all 12 players from pre-selected Team B
        for (TeamPlayer player : preselectedTeamB.getPlayers()) {
            View playerCard = createPlayerCard(player);
            
            playerCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTeamBPlayerCardClicked(player, v);
                }
            });
            
            teamBPlayerCards.add(playerCard);
            gridTeamBPlayers.addView(playerCard);
        }
    }
    
    private View createPlayerCard(TeamPlayer player) {
        // Inflate the modern player card layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View card = inflater.inflate(R.layout.item_player_card, null);
        
        // Get the number and name TextViews
        TextView tvNumber = card.findViewById(R.id.tvPlayerNumber);
        TextView tvName = card.findViewById(R.id.tvPlayerName);
        
        // Set player data
        tvNumber.setText(String.valueOf(player.getNumber()));
        tvName.setText(player.getName());
        
        // Set layout parameters for single column grid
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = GridLayout.LayoutParams.MATCH_PARENT;
        params.height = dpToPx(58); // Fixed height for consistency (slightly taller for number+name)
        params.columnSpec = GridLayout.spec(0); // Single column
        params.setMargins(6, 3, 6, 3);
        card.setLayoutParams(params);
        
        return card;
    }
    
    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    private void onTeamAPlayerCardClicked(TeamPlayer player, View card) {
        boolean isCurrentlySelected = selectedTeamAPlayers.contains(player);
        
        // Get the TextViews within the card
        TextView tvNumber = card.findViewById(R.id.tvPlayerNumber);
        TextView tvName = card.findViewById(R.id.tvPlayerName);
        
        if (isCurrentlySelected) {
            // Deselect player
            selectedTeamAPlayers.remove(player);
            card.setBackgroundColor(Color.parseColor("#F5F6FA")); // Unselected
            tvNumber.setTextColor(Color.parseColor("#2C3E50"));
            tvName.setTextColor(Color.parseColor("#2C3E50"));
        } else {
            // Try to select player
            if (selectedTeamAPlayers.size() >= 5) {
                Toast.makeText(this, "Maximum 5 players allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedTeamAPlayers.add(player);
            card.setBackgroundColor(Color.parseColor("#3498DB")); // Selected (blue)
            tvNumber.setTextColor(Color.parseColor("#FFFFFF"));
            tvName.setTextColor(Color.parseColor("#FFFFFF"));
        }
        
        updateTeamAStatus();
        updateStartGameButton();
    }
    
    private void onTeamBPlayerCardClicked(TeamPlayer player, View card) {
        boolean isCurrentlySelected = selectedTeamBPlayers.contains(player);
        
        // Get the TextViews within the card
        TextView tvNumber = card.findViewById(R.id.tvPlayerNumber);
        TextView tvName = card.findViewById(R.id.tvPlayerName);
        
        if (isCurrentlySelected) {
            // Deselect player
            selectedTeamBPlayers.remove(player);
            card.setBackgroundColor(Color.parseColor("#F5F6FA")); // Unselected
            tvNumber.setTextColor(Color.parseColor("#2C3E50"));
            tvName.setTextColor(Color.parseColor("#2C3E50"));
        } else {
            // Try to select player
            if (selectedTeamBPlayers.size() >= 5) {
                Toast.makeText(this, "Maximum 5 players allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedTeamBPlayers.add(player);
            card.setBackgroundColor(Color.parseColor("#3498DB")); // Selected (blue)
            tvNumber.setTextColor(Color.parseColor("#FFFFFF"));
            tvName.setTextColor(Color.parseColor("#FFFFFF"));
        }
        
        updateTeamBStatus();
        updateStartGameButton();
    }
    
    private void updateTeamAStatus() {
        int selectedCount = selectedTeamAPlayers.size();
        
        // Update counter display
        tvTeamACounter.setText(selectedCount + "/5 selected");
        
        // Update team section background based on ready state
        if (selectedCount == 5) {
            // Ready state - green background
            teamASection.setBackgroundColor(Color.parseColor("#2ECC71")); // Green
            tvTeamATitle.setTextColor(Color.parseColor("#FFFFFF"));
            tvTeamACounter.setTextColor(Color.parseColor("#FFFFFF"));
            tvTeamACounter.setText("READY");
        } else {
            // Not ready - white background
            teamASection.setBackgroundColor(Color.parseColor("#FFFFFF"));
            tvTeamATitle.setTextColor(Color.parseColor("#2C3E50"));
            tvTeamACounter.setTextColor(Color.parseColor("#7F8C8D"));
            tvTeamACounter.setText(selectedCount + "/5 selected");
        }
    }
    
    private void updateTeamBStatus() {
        int selectedCount = selectedTeamBPlayers.size();
        
        // Update counter display
        tvTeamBCounter.setText(selectedCount + "/5 selected");
        
        // Update team section background based on ready state
        if (selectedCount == 5) {
            // Ready state - green background
            teamBSection.setBackgroundColor(Color.parseColor("#2ECC71")); // Green
            tvTeamBTitle.setTextColor(Color.parseColor("#FFFFFF"));
            tvTeamBCounter.setTextColor(Color.parseColor("#FFFFFF"));
            tvTeamBCounter.setText("READY");
        } else {
            // Not ready - white background
            teamBSection.setBackgroundColor(Color.parseColor("#FFFFFF"));
            tvTeamBTitle.setTextColor(Color.parseColor("#2C3E50"));
            tvTeamBCounter.setTextColor(Color.parseColor("#7F8C8D"));
            tvTeamBCounter.setText(selectedCount + "/5 selected");
        }
    }
    
    private void updateStartGameButton() {
        boolean bothTeamsReady = (selectedTeamAPlayers.size() == 5 && selectedTeamBPlayers.size() == 5);
        
        if (bothTeamsReady) {
            // Both teams ready - enable button with green background
            btnStartGame.setEnabled(true);
            btnStartGame.setBackgroundColor(Color.parseColor("#27AE60")); // Green
            btnStartGame.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            // Not ready - disable button with grey background
            btnStartGame.setEnabled(false);
            btnStartGame.setBackgroundColor(Color.parseColor("#BDC3C7")); // Grey
            btnStartGame.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }
    
    private void showStartGameConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Start Game")
               .setMessage("Are you sure you want to start game?")
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       startGame();
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                   }
               })
               .show();
    }
    
    private void startGame() {
        // Create game players from selected team players
        List<Player> teamAGamePlayers = new ArrayList<>();
        List<Player> teamBGamePlayers = new ArrayList<>();
        
        // Convert selected TeamPlayers to GamePlayers
        int playerId = 1;
        for (TeamPlayer tp : selectedTeamAPlayers) {
            teamAGamePlayers.add(tp.toGamePlayer(gameId, "home"));
        }
        for (TeamPlayer tp : selectedTeamBPlayers) {
            teamBGamePlayers.add(tp.toGamePlayer(gameId, "away"));
        }
        
        // For MVP, show confirmation message with pre-selected team names
        String message = String.format("Game Time!!!\n%s vs %s\n(Game interface coming soon)", 
            homeTeamName, awayTeamName);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // TODO: Update game status to "In Progress" here when actual game recording begins
        // TODO: Navigate to GameActivity with roster data
        // Intent intent = new Intent(this, GameActivity.class);
        // intent.putExtra("gameId", gameId);
        // intent.putExtra("teamAName", homeTeamName);
        // intent.putExtra("teamBName", awayTeamName);
        // intent.putExtra("teamAPlayers", (Serializable) teamAGamePlayers);  
        // intent.putExtra("teamBPlayers", (Serializable) teamBGamePlayers);
        // startActivity(intent);
    }
}
