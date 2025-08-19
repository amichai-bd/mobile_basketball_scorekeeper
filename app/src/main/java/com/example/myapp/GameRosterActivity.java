package com.example.myapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapp.models.Player;
import com.example.myapp.models.Team;
import com.example.myapp.models.TeamPlayer;
import com.example.myapp.data.LeagueDataProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Game Roster Activity - Player selection interface (Frame 2) - SPECIFICATION ALIGNED
 * Teams are pre-selected from scheduled game, user chooses 5 players from each team's roster
 */
public class GameRosterActivity extends Activity {
    
    // UI Components
    private TextView tvTeamATitle, tvTeamBTitle;
    private TextView tvTeamAPlayersTitle, tvTeamBPlayersTitle;
    private LinearLayout llTeamAPlayers, llTeamBPlayers;
    private Button btnApproveTeamA, btnEditTeamA, btnApproveTeamB, btnEditTeamB, btnStartGame;
    
    // Data
    private int gameId;
    private String homeTeamName, awayTeamName;
    private Team preselectedTeamA, preselectedTeamB; // Pre-selected from scheduled game
    private List<TeamPlayer> selectedTeamAPlayers, selectedTeamBPlayers;
    private List<CheckBox> teamACheckboxes, teamBCheckboxes;
    private boolean teamAApproved = false, teamBApproved = false;
    
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
        // Team title displays (pre-selected teams)
        tvTeamATitle = findViewById(R.id.tvTeamATitle);
        tvTeamBTitle = findViewById(R.id.tvTeamBTitle);
        
        // Player selection containers
        tvTeamAPlayersTitle = findViewById(R.id.tvTeamAPlayersTitle);
        tvTeamBPlayersTitle = findViewById(R.id.tvTeamBPlayersTitle);
        llTeamAPlayers = findViewById(R.id.llTeamAPlayers);
        llTeamBPlayers = findViewById(R.id.llTeamBPlayers);
        
        // Buttons
        btnApproveTeamA = findViewById(R.id.btnApproveTeamA);
        btnEditTeamA = findViewById(R.id.btnEditTeamA);
        btnApproveTeamB = findViewById(R.id.btnApproveTeamB);
        btnEditTeamB = findViewById(R.id.btnEditTeamB);
        btnStartGame = findViewById(R.id.btnStartGame);
    }
    
    private void initializeData() {
        // Get pre-selected teams from league data based on team names
        preselectedTeamA = LeagueDataProvider.getTeamByName(homeTeamName);
        preselectedTeamB = LeagueDataProvider.getTeamByName(awayTeamName);
        
        // Initialize selected player lists
        selectedTeamAPlayers = new ArrayList<>();
        selectedTeamBPlayers = new ArrayList<>();
        
        // Initialize checkbox lists
        teamACheckboxes = new ArrayList<>();
        teamBCheckboxes = new ArrayList<>();
    }
    
    private void setupPreselectedTeams() {
        // Update team title displays with actual team names
        tvTeamATitle.setText(homeTeamName + " (Home)");
        tvTeamBTitle.setText(awayTeamName + " (Away)");
        
        // Automatically populate player lists for both teams
        populateTeamAPlayers();
        populateTeamBPlayers();
    }
    

    
    private void setupEventListeners() {
        btnApproveTeamA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                approveTeamA();
            }
        });
        
        btnEditTeamA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTeamA();
            }
        });
        
        btnApproveTeamB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                approveTeamB();
            }
        });
        
        btnEditTeamB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTeamB();
            }
        });
        
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartGameConfirmation();
            }
        });
    }
    
    private void populateTeamAPlayers() {
        if (preselectedTeamA == null) {
            Toast.makeText(this, "Error: Team A not found in league data", Toast.LENGTH_LONG).show();
            return;
        }
        
        llTeamAPlayers.removeAllViews();
        teamACheckboxes.clear();
        
        // Create checkboxes for all 12 players from pre-selected Team A
        for (TeamPlayer player : preselectedTeamA.getPlayers()) {
            CheckBox checkbox = new CheckBox(this);
            checkbox.setText(player.toString()); // Shows "#5 Player Name"
            checkbox.setTextSize(16);
            checkbox.setPadding(8, 8, 8, 8);
            
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onTeamAPlayerSelectionChanged(player, isChecked);
                }
            });
            
            teamACheckboxes.add(checkbox);
            llTeamAPlayers.addView(checkbox);
        }
    }
    
    private void populateTeamBPlayers() {
        if (preselectedTeamB == null) {
            Toast.makeText(this, "Error: Team B not found in league data", Toast.LENGTH_LONG).show();
            return;
        }
        
        llTeamBPlayers.removeAllViews();
        teamBCheckboxes.clear();
        
        // Create checkboxes for all 12 players from pre-selected Team B
        for (TeamPlayer player : preselectedTeamB.getPlayers()) {
            CheckBox checkbox = new CheckBox(this);
            checkbox.setText(player.toString()); // Shows "#5 Player Name"
            checkbox.setTextSize(16);
            checkbox.setPadding(8, 8, 8, 8);
            
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onTeamBPlayerSelectionChanged(player, isChecked);
                }
            });
            
            teamBCheckboxes.add(checkbox);
            llTeamBPlayers.addView(checkbox);
        }
    }
    
    private void onTeamAPlayerSelectionChanged(TeamPlayer player, boolean isChecked) {
        if (isChecked) {
            if (selectedTeamAPlayers.size() >= 5) {
                Toast.makeText(this, "Maximum 5 players allowed", Toast.LENGTH_SHORT).show();
                // Find and uncheck this checkbox
                for (CheckBox cb : teamACheckboxes) {
                    if (cb.getText().toString().equals(player.toString())) {
                        cb.setChecked(false);
                        return;
                    }
                }
                return;
            }
            selectedTeamAPlayers.add(player);
        } else {
            selectedTeamAPlayers.remove(player);
        }
        updateTeamAApproveButton();
    }
    
    private void onTeamBPlayerSelectionChanged(TeamPlayer player, boolean isChecked) {
        if (isChecked) {
            if (selectedTeamBPlayers.size() >= 5) {
                Toast.makeText(this, "Maximum 5 players allowed", Toast.LENGTH_SHORT).show();
                // Find and uncheck this checkbox
                for (CheckBox cb : teamBCheckboxes) {
                    if (cb.getText().toString().equals(player.toString())) {
                        cb.setChecked(false);
                        return;
                    }
                }
                return;
            }
            selectedTeamBPlayers.add(player);
        } else {
            selectedTeamBPlayers.remove(player);
        }
        updateTeamBApproveButton();
    }
    
    private void approveTeamA() {
        if (selectedTeamAPlayers.size() == 5) {
            teamAApproved = true;
            setTeamAEnabled(false);
            btnApproveTeamA.setEnabled(false);
            btnEditTeamA.setEnabled(true);
            Toast.makeText(this, "Team A roster approved!", Toast.LENGTH_SHORT).show();
            updateStartGameButton();
        }
    }
    
    private void editTeamA() {
        teamAApproved = false;
        setTeamAEnabled(true);
        btnApproveTeamA.setEnabled(selectedTeamAPlayers.size() == 5);
        btnEditTeamA.setEnabled(false);
        updateStartGameButton();
    }
    
    private void approveTeamB() {
        if (selectedTeamBPlayers.size() == 5) {
            teamBApproved = true;
            setTeamBEnabled(false);
            btnApproveTeamB.setEnabled(false);
            btnEditTeamB.setEnabled(true);
            Toast.makeText(this, "Team B roster approved!", Toast.LENGTH_SHORT).show();
            updateStartGameButton();
        }
    }
    
    private void editTeamB() {
        teamBApproved = false;
        setTeamBEnabled(true);
        btnApproveTeamB.setEnabled(selectedTeamBPlayers.size() == 5);
        btnEditTeamB.setEnabled(false);
        updateStartGameButton();
    }
    
    // Helper methods for the specification-aligned implementation
    
    private void clearTeamASelections() {
        selectedTeamAPlayers.clear();
        teamAApproved = false;
        btnApproveTeamA.setEnabled(false);
        btnEditTeamA.setEnabled(false);
        
        // Uncheck all Team A checkboxes
        for (CheckBox checkbox : teamACheckboxes) {
            checkbox.setChecked(false);
        }
        updateStartGameButton();
    }
    
    private void clearTeamBSelections() {
        selectedTeamBPlayers.clear();
        teamBApproved = false;
        btnApproveTeamB.setEnabled(false);
        btnEditTeamB.setEnabled(false);
        
        // Uncheck all Team B checkboxes
        for (CheckBox checkbox : teamBCheckboxes) {
            checkbox.setChecked(false);
        }
        updateStartGameButton();
    }
    
    private void updateTeamAApproveButton() {
        btnApproveTeamA.setEnabled(selectedTeamAPlayers.size() == 5);
    }
    
    private void updateTeamBApproveButton() {
        btnApproveTeamB.setEnabled(selectedTeamBPlayers.size() == 5);
    }
    
    private void setTeamAEnabled(boolean enabled) {
        for (CheckBox checkbox : teamACheckboxes) {
            checkbox.setEnabled(enabled);
        }
    }
    
    private void setTeamBEnabled(boolean enabled) {
        for (CheckBox checkbox : teamBCheckboxes) {
            checkbox.setEnabled(enabled);
        }
    }
    
    private void updateStartGameButton() {
        btnStartGame.setEnabled(teamAApproved && teamBApproved);
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
