package com.basketballstats.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import com.basketballstats.app.models.Team;
import com.basketballstats.app.models.TeamPlayer;
import com.basketballstats.app.models.Game;
import com.basketballstats.app.data.DatabaseController;
import com.basketballstats.app.utils.InputFormatHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * League Management Activity - SQLite Database Implementation
 * Complete interface for managing games and teams with persistent storage:
 * - Games Tab: Add/edit scheduled games with teams, dates, and times
 * - Teams Tab: Add/edit teams and manage each team's player roster
 * - Uses SQLite database for persistent data storage
 * - Supports team/player relationships and data integrity
 */
public class LeagueManagementActivity extends Activity {
    
    // UI Components
    private TabHost tabHost;
    private Button btnBackToSchedule;
    
    // Games Tab Components
    private Spinner spinnerHomeTeam, spinnerAwayTeam;
    private EditText etGameDate, etGameTime;
    private Button btnAddGame;
    private ListView lvScheduledGames;
    
    // Teams Tab Components
    private EditText etTeamName;
    private Button btnAddTeam;
    private ListView lvTeams;
    
    // Database and Data
    private DatabaseController dbController;
    private List<Team> teamsList;
    private List<Game> gamesList;
    private ArrayAdapter<Team> homeTeamAdapter, awayTeamAdapter;
    private GameManagementAdapter gamesAdapter;
    private TeamManagementAdapter teamsAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_management);
        
        // Initialize UI components
        initializeViews();
        
        // Initialize data
        initializeData();
        
        // Setup tab host
        setupTabHost();
        
        // Setup event listeners
        setupEventListeners();
        
        // Add smart input formatting
        setupInputFormatting();
        
        Toast.makeText(this, "League Management with SQLite ready!", Toast.LENGTH_SHORT).show();
    }
    
    private void initializeViews() {
        // Main navigation
        btnBackToSchedule = findViewById(R.id.btnBackToSchedule);
        
        // Games Tab components
        spinnerHomeTeam = findViewById(R.id.spinnerHomeTeam);
        spinnerAwayTeam = findViewById(R.id.spinnerAwayTeam);
        etGameDate = findViewById(R.id.etGameDate);
        etGameTime = findViewById(R.id.etGameTime);
        btnAddGame = findViewById(R.id.btnAddGame);
        lvScheduledGames = findViewById(R.id.lvScheduledGames);
        
        // Teams Tab components
        etTeamName = findViewById(R.id.etTeamName);
        btnAddTeam = findViewById(R.id.btnAddTeam);
        lvTeams = findViewById(R.id.lvTeams);
    }
    
    private void initializeData() {
        // Initialize database controller
        dbController = DatabaseController.getInstance(this);
        
        try {
            // Load teams and games from SQLite database
            teamsList = new ArrayList<>(Team.findAll(dbController.getDatabaseHelper()));
            gamesList = new ArrayList<>(Game.findAll(dbController.getDatabaseHelper()));
            
            // Setup adapters for Games Tab
            homeTeamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teamsList);
            homeTeamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerHomeTeam.setAdapter(homeTeamAdapter);
            
            awayTeamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teamsList);
            awayTeamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAwayTeam.setAdapter(awayTeamAdapter);
            
            gamesAdapter = new GameManagementAdapter(this, gamesList);
            lvScheduledGames.setAdapter(gamesAdapter);
            
            // Setup adapter for Teams Tab
            teamsAdapter = new TeamManagementAdapter(this, teamsList);
            lvTeams.setAdapter(teamsAdapter);
            
            Toast.makeText(this, "Loaded " + teamsList.size() + " teams, " + gamesList.size() + " games", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error loading data from database", Toast.LENGTH_LONG).show();
            android.util.Log.e("LeagueManagement", "Database initialization failed", e);
            
            // Initialize empty lists as fallback
            teamsList = new ArrayList<>();
            gamesList = new ArrayList<>();
        }
    }
    
    private void setupTabHost() {
        tabHost = findViewById(R.id.tabhost);
        tabHost.setup();
        
        // Games Tab
        TabHost.TabSpec gamesTab = tabHost.newTabSpec("games");
        gamesTab.setContent(R.id.tab_games);
        gamesTab.setIndicator("Games");
        tabHost.addTab(gamesTab);
        
        // Teams Tab
        TabHost.TabSpec teamsTab = tabHost.newTabSpec("teams");
        teamsTab.setContent(R.id.tab_teams);
        teamsTab.setIndicator("Teams");
        tabHost.addTab(teamsTab);
        
        // Set default tab
        tabHost.setCurrentTab(0);
    }
    
    private void setupEventListeners() {
        // Back to Schedule
        btnBackToSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Return to MainActivity
            }
        });
        
        // Add Game functionality
        btnAddGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewGame();
            }
        });
        
        // Add Team functionality
        btnAddTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTeam();
            }
        });
    }
    

    
    private void addNewTeam() {
        String teamName = etTeamName.getText().toString().trim();
        
        // Validation
        if (teamName.isEmpty()) {
            Toast.makeText(this, "Please enter team name", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check for duplicate team names using SQLite
        if (Team.exists(dbController.getDatabaseHelper(), teamName)) {
            Toast.makeText(this, "Team already exists", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Create and save the team to SQLite
            Team newTeam = new Team(teamName);
            long result = newTeam.save(dbController.getDatabaseHelper());
            
            if (result > 0) {
                // Refresh local lists from database
                refreshTeamsData();
                
                Toast.makeText(this, "✅ Team saved: " + teamName + 
                              "\nID: " + newTeam.getId() + " (Tap 'Players' to add roster)", 
                              Toast.LENGTH_LONG).show();
                
                // Clear field
                etTeamName.setText("");
            } else {
                Toast.makeText(this, "Error: Failed to save team", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("LeagueManagement", "Error saving team", e);
        }
    }
    
    private void setupInputFormatting() {
        // Add smart date formatting (DD/MM/YYYY with auto-slashes)
        InputFormatHelper.addDateFormatting(etGameDate);
        
        // Add smart time formatting (HH:MM with auto-colon)
        InputFormatHelper.addTimeFormatting(etGameTime);
    }
    
    private void addNewGame() {
        Team homeTeam = (Team) spinnerHomeTeam.getSelectedItem();
        Team awayTeam = (Team) spinnerAwayTeam.getSelectedItem();
        String date = etGameDate.getText().toString().trim();
        String time = etGameTime.getText().toString().trim();
        
        // Enhanced validation
        if (homeTeam == null || awayTeam == null) {
            Toast.makeText(this, "Please select both teams", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (homeTeam.getId() == awayTeam.getId()) {
            Toast.makeText(this, "Home and away teams must be different", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (date.isEmpty()) {
            Toast.makeText(this, "Please enter date (DD/MM/YYYY)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!InputFormatHelper.isValidDate(date)) {
            Toast.makeText(this, "Please enter valid date (DD/MM/YYYY)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (time.isEmpty()) {
            Toast.makeText(this, "Please enter time (HH:MM)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!InputFormatHelper.isValidTime(time)) {
            Toast.makeText(this, "Please enter valid time (HH:MM, 24-hour format)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Create and save the game to SQLite
            Game newGame = new Game(date, time, homeTeam.getId(), awayTeam.getId());
            long result = newGame.save(dbController.getDatabaseHelper());
            
            if (result > 0) {
                // Refresh local list from database
                refreshGamesData();
                
                String gameInfo = String.format("%s %s - %s vs %s", date, time, homeTeam.getName(), awayTeam.getName());
                Toast.makeText(this, "✅ Game saved: " + gameInfo + 
                              "\nID: " + newGame.getId(), Toast.LENGTH_LONG).show();
                
                // Clear fields
                etGameDate.setText("");
                etGameTime.setText("");
            } else {
                Toast.makeText(this, "Error: Failed to save game", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("LeagueManagement", "Error saving game", e);
        }
    }
    
    /**
     * Custom adapter for games list with edit/delete functionality
     */
    private class GameManagementAdapter extends BaseAdapter {
        private Context context;
        private List<Game> games;
        private LayoutInflater inflater;
        
        public GameManagementAdapter(Context context, List<Game> games) {
            this.context = context;
            this.games = games;
            this.inflater = LayoutInflater.from(context);
        }
        
        @Override
        public int getCount() { return games.size(); }
        
        @Override
        public Object getItem(int position) { return games.get(position); }
        
        @Override
        public long getItemId(int position) { return games.get(position).getId(); }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_game_management, parent, false);
            }
            
            Game game = games.get(position);
            TextView tvGameInfo = convertView.findViewById(R.id.tvGameInfo);
            Button btnEdit = convertView.findViewById(R.id.btnEditGame);
            Button btnDelete = convertView.findViewById(R.id.btnDeleteGame);
            
            // Display game info with time and status
            String homeTeamName = game.getHomeTeam() != null ? game.getHomeTeam().getName() : "Team " + game.getHomeTeamId();
            String awayTeamName = game.getAwayTeam() != null ? game.getAwayTeam().getName() : "Team " + game.getAwayTeamId();
            
            String gameInfo = String.format("%s %s - %s vs %s [%s]", 
                game.getDate(), 
                game.getTime() != null ? game.getTime() : "TBD",
                homeTeamName, 
                awayTeamName,
                game.getStatus().toUpperCase());
            tvGameInfo.setText(gameInfo);
            
            // Edit button
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editGame(game);
                }
            });
            
            // Delete button
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmDeleteGame(game, position);
                }
            });
            
            return convertView;
        }
    }
    
    /**
     * Custom adapter for teams list with edit/delete functionality
     */
    private class TeamManagementAdapter extends BaseAdapter {
        private Context context;
        private List<Team> teams;
        private LayoutInflater inflater;
        
        public TeamManagementAdapter(Context context, List<Team> teams) {
            this.context = context;
            this.teams = teams;
            this.inflater = LayoutInflater.from(context);
        }
        
        @Override
        public int getCount() { return teams.size(); }
        
        @Override
        public Object getItem(int position) { return teams.get(position); }
        
        @Override
        public long getItemId(int position) { return teams.get(position).getId(); }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_team_management, parent, false);
            }
            
            Team team = teams.get(position);
            TextView tvTeamInfo = convertView.findViewById(R.id.tvTeamInfo);
            Button btnEdit = convertView.findViewById(R.id.btnEditTeam);
            Button btnPlayers = convertView.findViewById(R.id.btnManagePlayers);
            Button btnDelete = convertView.findViewById(R.id.btnDeleteTeam);
            
            // Display team info with player count from database
            int playerCount = TeamPlayer.getCountForTeam(dbController.getDatabaseHelper(), team.getId());
            tvTeamInfo.setText(String.format("%s (%d players)", 
                team.getName(), playerCount));
            
            // Edit button
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTeam(team);
                }
            });
            
            // Players button
            btnPlayers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPlayerManagement(team);
                }
            });
            
            // Delete button
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmDeleteTeam(team, position);
                }
            });
            
            return convertView;
        }
    }
    
    private void editGame(Game game) {
        // For MVP, show placeholder (full implementation with form population later)
        Toast.makeText(this, "Edit game: " + game.toString() + "\n(Coming soon!)", Toast.LENGTH_SHORT).show();
        // TODO: Populate form fields with game data for editing
    }
    
    private void confirmDeleteGame(Game game, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Game")
               .setMessage("Are you sure you want to delete this game?\n" + game.toString())
               .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       deleteGame(game, position);
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    private void deleteGame(Game game, int position) {
        try {
            // Delete from SQLite database
            boolean removed = game.delete(dbController.getDatabaseHelper());
            
            if (removed) {
                // Update local list and refresh adapter
                gamesList.remove(position);
                gamesAdapter.notifyDataSetChanged();
                Toast.makeText(this, "✅ Game deleted: " + game.toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error: Could not delete game", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("LeagueManagement", "Error deleting game", e);
        }
    }
    
    private void editTeam(Team team) {
        // For MVP, show placeholder (full implementation with inline editing later)
        Toast.makeText(this, "Edit team: " + team.getName() + "\n(Coming soon!)", Toast.LENGTH_SHORT).show();
        // TODO: Implement inline team name editing
    }
    
    private void confirmDeleteTeam(Team team, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Team")
               .setMessage("Are you sure you want to delete this team?\n" + team.getName())
               .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       deleteTeam(team, position);
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    private void deleteTeam(Team team, int position) {
        try {
            // Check if team is used in any games first
            List<Game> gamesWithTeam = Game.findAll(dbController.getDatabaseHelper());
            boolean teamInUse = false;
            for (Game game : gamesWithTeam) {
                if (game.getHomeTeamId() == team.getId() || game.getAwayTeamId() == team.getId()) {
                    teamInUse = true;
                    break;
                }
            }
            
            if (teamInUse) {
                Toast.makeText(this, "Cannot delete team - used in scheduled games", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Delete from SQLite database (will CASCADE delete players due to foreign key)
            boolean removed = team.delete(dbController.getDatabaseHelper());
            
            if (removed) {
                // Update local list and refresh all adapters
                teamsList.remove(position);
                teamsAdapter.notifyDataSetChanged();
                
                // Also update the spinner adapters
                homeTeamAdapter.notifyDataSetChanged();
                awayTeamAdapter.notifyDataSetChanged();
                
                Toast.makeText(this, "✅ Team deleted: " + team.getName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error: Could not delete team", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("LeagueManagement", "Error deleting team", e);
        }
    }
    
    private void openPlayerManagement(Team team) {
        // ✅ FIX: Load team with complete roster from SQLite database
        Team teamWithRoster = dbController.getTeamWithRoster(team.getId());
        
        if (teamWithRoster == null) {
            Toast.makeText(this, "Error: Could not load team data", Toast.LENGTH_SHORT).show();
            return;
        }
        
        PlayerManagementModal modal = new PlayerManagementModal(this, teamWithRoster, new PlayerManagementModal.OnPlayersChangedListener() {
            @Override
            public void onPlayersChanged(Team updatedTeam) {
                // Refresh the teams adapter to show updated player count
                refreshTeamsData();
            }
        });
        modal.show();
    }
    
    private void refreshGamesData() {
        try {
            // Safely reload games from SQLite database
            List<Game> currentGames = Game.findAll(dbController.getDatabaseHelper());
            gamesList.clear();
            gamesList.addAll(currentGames);
            if (gamesAdapter != null) {
                gamesAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error refreshing games data", Toast.LENGTH_SHORT).show();
            android.util.Log.e("LeagueManagement", "Error refreshing games", e);
        }
    }
    
    private void refreshTeamsData() {
        try {
            // Safely reload teams from SQLite database
            List<Team> currentTeams = Team.findAll(dbController.getDatabaseHelper());
            teamsList.clear();
            teamsList.addAll(currentTeams);
            
            // Safely refresh all adapters that use teams
            if (teamsAdapter != null) {
                teamsAdapter.notifyDataSetChanged();
            }
            if (homeTeamAdapter != null) {
                homeTeamAdapter.notifyDataSetChanged();
            }
            if (awayTeamAdapter != null) {
                awayTeamAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error refreshing teams data", Toast.LENGTH_SHORT).show();
            android.util.Log.e("LeagueManagement", "Error refreshing teams", e);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Only refresh if we're returning from another activity, not on first creation
        // This prevents data corruption when League Management first opens
        
        // Note: onResume() is called both on first creation AND when returning from other activities
        // We only want to refresh when returning, not on first creation
        // For now, we'll skip the refresh here and let the initialization handle first load
        
        // TODO: Add proper lifecycle management to distinguish first creation vs returning
    }
}
