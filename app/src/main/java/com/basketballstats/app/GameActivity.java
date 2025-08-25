package com.basketballstats.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.basketballstats.app.models.Player;
import com.basketballstats.app.models.Team;
import com.basketballstats.app.models.TeamPlayer;
import com.basketballstats.app.models.Game;
import com.basketballstats.app.models.Event;
import com.basketballstats.app.data.DatabaseController;
import java.util.ArrayList;
import java.util.List;

/**
 * Game Activity - Live Basketball Statistics Recording with SQLite Persistence (Frame 3)
 * THE CORE FUNCTIONALITY - Real-time game statistics recording interface
 * 
 * Features:
 * - SQLite database integration for persistent game state and events
 * - Dual modes: Setup Mode (player selection) and Game Mode (live recording)
 * - Real-time event tracking with automatic database saving
 * - Live score updates and game clock management
 * - Complete event history with sequential ordering
 */
public class GameActivity extends Activity implements PlayerSelectionModal.PlayerSelectionListener {
    
    // Database and Game State
    private DatabaseController dbController;
    private Game currentGame; // SQLite game object with persistent state
    private List<Player> teamAPlayers, teamBPlayers;
    private Player selectedPlayer = null;
    private int eventSequenceCounter = 1; // For tracking event order
    
    // Dual Mode Support (Setup Mode vs Game Mode)
    private boolean isInSetupMode = true; // Start in setup mode
    private Team teamA, teamB; // Full team rosters for player selection
    private Button btnSelectTeamAPlayers, btnSelectTeamBPlayers; // Setup mode buttons
    private String currentModalTeamSide; // Track which team's modal is currently open
    
    // UI Components - Team Panels (Team names now in team panels, not blue strip)
    private TextView tvTeamAName, tvTeamBName; // Team names in team panels
    private TextView tvTeamAScore, tvTeamBScore, tvGameClock;
    private TextView tvTeamAFouls, tvTeamBFouls;
    private Button btnGameToggle;
    
    // UI Components - Team Panels
    private LinearLayout llTeamAPlayers, llTeamBPlayers;
    private Button btnTeamATimeout, btnTeamASub, btnTeamBTimeout, btnTeamBSub;
    private Button btnTeamAFoul, btnTeamBFoul; // New team foul buttons
    
    // UI Components - Event Panel (12 buttons - FOUL moved to team panels)
    private Button btn1P, btn2P, btn3P, btn1M, btn2M, btn3M;
    private Button btnOR, btnDR, btnAST, btnSTL, btnBLK, btnTO;
    
    // UI Components - Quarter Management
    private Spinner spinnerQuarter;
    
    // UI Components - Live Event Feed (Updated for 2 events and undo)
    private LinearLayout llLiveEventFeed;
    private Button btnViewLog;
    private Button btnAllowEvents; // Override toggle for events when timer stopped
    private Button btnUndo; // New undo button
    
    // Game Management
    private Handler clockHandler = new Handler();
    private Runnable clockRunnable;
    private List<Button> teamAPlayerButtons, teamBPlayerButtons;
    private boolean allowEventsOverride = false; // Override to allow events when timer stopped
    
    // Event Tracking (SQLite-backed)
    private List<Event> gameEvents = new ArrayList<>(); // SQLite Event objects for current game
    private List<String> recentEvents;  // Last 5 events for live feed (derived from gameEvents)
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        
        // Get game data from intent
        getGameDataFromIntent();
        
        // Initialize UI components
        initializeViews();
        
        // Initialize game state
        initializeGameState();
        
        // Create player buttons
        createPlayerButtons();
        
        // Set up event listeners
        setupEventListeners();
        
        // Initialize clock
        setupGameClock();
        
        // Check initial game readiness and set up UI accordingly
        checkIfGameReady();
        
        // Update initial display
        updateAllDisplays();
    }
    
    private void getGameDataFromIntent() {
        // Initialize database controller
        dbController = DatabaseController.getInstance(this);
        
        try {
            // Get game ID from intent
            int gameId = getIntent().getIntExtra("gameId", -1);
            
            if (gameId > 0) {
                // Load game from SQLite database
                currentGame = Game.findById(dbController.getDatabaseHelper(), gameId);
                
                if (currentGame == null) {
                    Toast.makeText(this, "Error: Game not found in database", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                
                // Load teams with rosters
                currentGame.loadTeams(dbController.getDatabaseHelper());
                teamA = currentGame.getHomeTeam();
                teamB = currentGame.getAwayTeam();
                
                if (teamA != null) {
                    teamA.loadPlayers(dbController.getDatabaseHelper());
                }
                if (teamB != null) {
                    teamB.loadPlayers(dbController.getDatabaseHelper());
                }
                
            } else {
                // Fallback: Try to get team names from intent (backward compatibility)
                String homeTeamName = getIntent().getStringExtra("homeTeam");
                String awayTeamName = getIntent().getStringExtra("awayTeam");
                
                if (homeTeamName != null && awayTeamName != null) {
                    teamA = Team.findByName(dbController.getDatabaseHelper(), homeTeamName);
                    teamB = Team.findByName(dbController.getDatabaseHelper(), awayTeamName);
                    
                    if (teamA != null && teamB != null) {
                        teamA.loadPlayers(dbController.getDatabaseHelper());
                        teamB.loadPlayers(dbController.getDatabaseHelper());
                        
                        // Create new game in database for this session
                        currentGame = new Game();
                        currentGame.setHomeTeamId(teamA.getId());
                        currentGame.setAwayTeamId(teamB.getId());
                        currentGame.setDate(java.text.DateFormat.getDateInstance().format(new java.util.Date()));
                        currentGame.setTime(java.text.DateFormat.getTimeInstance().format(new java.util.Date()));
                        currentGame.setStatus("in_progress");
                        currentGame.setHomeTeam(teamA);
                        currentGame.setAwayTeam(teamB);
                        
                        long result = currentGame.save(dbController.getDatabaseHelper());
                        if (result <= 0) {
                            Toast.makeText(this, "Error: Could not create game in database", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                }
            }
            
            // Verify we have valid teams
            if (teamA == null || teamB == null) {
                Toast.makeText(this, "Error: Could not load team data", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // Initialize empty player lists (will be populated when players are selected)
            teamAPlayers = new ArrayList<>();
            teamBPlayers = new ArrayList<>();
            
            // Load existing game events from database
            loadGameEvents();
            
            // Update event sequence counter
            updateEventSequenceCounter();
            
            // Check if we're resuming a game or starting fresh
            isInSetupMode = teamAPlayers.isEmpty() || teamBPlayers.isEmpty();
            
            Toast.makeText(this, "Loaded game: " + teamA.getName() + " vs " + teamB.getName(), Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "Database error loading game: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("GameActivity", "Error loading game data", e);
            finish();
        }
    }
    

    
    private void initializeViews() {
        // Blue strip components (Enhanced Layout)
        tvTeamAName = findViewById(R.id.tvTeamAName);
        tvTeamBName = findViewById(R.id.tvTeamBName);
        tvTeamAScore = findViewById(R.id.tvTeamAScore);
        tvTeamBScore = findViewById(R.id.tvTeamBScore);
        tvGameClock = findViewById(R.id.tvGameClock);
        tvTeamAFouls = findViewById(R.id.tvTeamAFouls);
        tvTeamBFouls = findViewById(R.id.tvTeamBFouls);
        btnGameToggle = findViewById(R.id.btnGameToggle);
        
        // Team panel components
        llTeamAPlayers = findViewById(R.id.llTeamAPlayers);
        llTeamBPlayers = findViewById(R.id.llTeamBPlayers);
        btnTeamATimeout = findViewById(R.id.btnTeamATimeout);
        btnTeamASub = findViewById(R.id.btnTeamASub);
        btnTeamBTimeout = findViewById(R.id.btnTeamBTimeout);
        btnTeamBSub = findViewById(R.id.btnTeamBSub);
        btnTeamAFoul = findViewById(R.id.btnTeamAFoul);
        btnTeamBFoul = findViewById(R.id.btnTeamBFoul);
        
        // Event panel components
        btn1P = findViewById(R.id.btn1P);
        btn2P = findViewById(R.id.btn2P);
        btn3P = findViewById(R.id.btn3P);
        btn1M = findViewById(R.id.btn1M);
        btn2M = findViewById(R.id.btn2M);
        btn3M = findViewById(R.id.btn3M);
        btnOR = findViewById(R.id.btnOR);
        btnDR = findViewById(R.id.btnDR);
        btnAST = findViewById(R.id.btnAST);
        btnSTL = findViewById(R.id.btnSTL);
        btnBLK = findViewById(R.id.btnBLK);
        btnTO = findViewById(R.id.btnTO);
        
        // Quarter management component
        spinnerQuarter = findViewById(R.id.spinnerQuarter);
        
        // Live event feed components
        llLiveEventFeed = findViewById(R.id.llLiveEventFeed);
        btnViewLog = findViewById(R.id.btnViewLog);
        btnAllowEvents = findViewById(R.id.btnAllowEvents);
        btnUndo = findViewById(R.id.btnUndo);
    }
    
    private void initializeGameState() {
        // Initialize team names in team panels from loaded teams
        if (teamA != null && teamB != null) {
            tvTeamAName.setText(teamA.getName());
            tvTeamBName.setText(teamB.getName());
        }
        
        // Initialize player button lists
        teamAPlayerButtons = new ArrayList<>();
        teamBPlayerButtons = new ArrayList<>();
        
        // Initialize event tracking
        recentEvents = new ArrayList<>();
        
                // Clear game events when starting new game (for MVP)
        // TODO: In full implementation, load from database
        gameEvents.clear();
    }
    
    /**
     * Load existing game events from SQLite database
     */
    private void loadGameEvents() {
        try {
            if (currentGame != null && currentGame.getId() > 0) {
                gameEvents = Event.findByGameId(dbController.getDatabaseHelper(), currentGame.getId());
                
                // Update recent events for live feed
                updateRecentEventsFeed();
                
                android.util.Log.d("GameActivity", "Loaded " + gameEvents.size() + " events from database");
            }
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error loading game events", e);
            gameEvents = new ArrayList<>();
        }
    }
    
    /**
     * Update the event sequence counter based on existing events
     */
    private void updateEventSequenceCounter() {
        eventSequenceCounter = 1;
        if (!gameEvents.isEmpty()) {
            int maxSequence = 0;
            for (Event event : gameEvents) {
                if (event.getEventSequence() > maxSequence) {
                    maxSequence = event.getEventSequence();
                }
            }
            eventSequenceCounter = maxSequence + 1;
        }
    }
    
    /**
     * Save current game state to SQLite database
     */
    private void saveGameState() {
        try {
            if (currentGame != null) {
                currentGame.save(dbController.getDatabaseHelper());
            }
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error saving game state", e);
            Toast.makeText(this, "Error saving game state", Toast.LENGTH_SHORT).show();
        }
    }

    
    private void createPlayerButtons() {
        // Clear existing buttons
        llTeamAPlayers.removeAllViews();
        llTeamBPlayers.removeAllViews();
        teamAPlayerButtons.clear();
        teamBPlayerButtons.clear();
        
        // Create UI for each team independently
        createTeamAButtons();
        createTeamBButtons();
    }
    
    private void createTeamAButtons() {
        if (teamAPlayers.size() == 5) {
            // Team A has players selected - show player buttons
            for (Player player : teamAPlayers) {
                Button playerBtn = createPlayerButton(player);
                teamAPlayerButtons.add(playerBtn);
                llTeamAPlayers.addView(playerBtn);
            }
        } else {
            // Team A needs player selection - show "Select 5 Players" button
            btnSelectTeamAPlayers = new Button(this);
            btnSelectTeamAPlayers.setText("Select 5 Players");
            btnSelectTeamAPlayers.setTextSize(16);
            btnSelectTeamAPlayers.setBackgroundColor(Color.parseColor("#BDC3C7")); // Light grey
            btnSelectTeamAPlayers.setTextColor(Color.parseColor("#2C3E50"));
            btnSelectTeamAPlayers.setPadding(16, 32, 16, 32);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(8, 8, 8, 8);
            btnSelectTeamAPlayers.setLayoutParams(params);
            
            btnSelectTeamAPlayers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPlayerSelectionModal(teamA, "home");
                }
            });
            
            llTeamAPlayers.addView(btnSelectTeamAPlayers);
        }
    }
    
    private void createTeamBButtons() {
        if (teamBPlayers.size() == 5) {
            // Team B has players selected - show player buttons
            for (Player player : teamBPlayers) {
                Button playerBtn = createPlayerButton(player);
                teamBPlayerButtons.add(playerBtn);
                llTeamBPlayers.addView(playerBtn);
            }
        } else {
            // Team B needs player selection - show "Select 5 Players" button
            btnSelectTeamBPlayers = new Button(this);
            btnSelectTeamBPlayers.setText("Select 5 Players");
            btnSelectTeamBPlayers.setTextSize(16);
            btnSelectTeamBPlayers.setBackgroundColor(Color.parseColor("#BDC3C7")); // Light grey
            btnSelectTeamBPlayers.setTextColor(Color.parseColor("#2C3E50"));
            btnSelectTeamBPlayers.setPadding(16, 32, 16, 32);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(8, 8, 8, 8);
            btnSelectTeamBPlayers.setLayoutParams(params);
            
            btnSelectTeamBPlayers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openPlayerSelectionModal(teamB, "away");
                }
            });
            
            llTeamBPlayers.addView(btnSelectTeamBPlayers);
        }
    }
    
    private Button createPlayerButton(Player player) {
        Button btn = new Button(this);
        btn.setText(String.format("#%d %s [%d]", player.getNumber(), player.getName(), player.getPersonalFouls()));
        btn.setTextSize(12);
        btn.setBackgroundColor(Color.parseColor("#EEEEEE"));
        btn.setTextColor(Color.parseColor("#2C3E50"));
        btn.setPadding(8, 8, 8, 8);
        
        // Set layout parameters with increased spacing
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            0, 1.0f);
        params.setMargins(8, 6, 8, 6); // Increased spacing between players
        btn.setLayoutParams(params);
        
        // Set click listener for player selection
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPlayer(player, btn);
            }
        });
        
        return btn;
    }
    
    private void setupEventListeners() {
        // Game control toggle
        btnGameToggle.setOnClickListener(v -> toggleGameTimer());
        
        // Quarter management - setup spinner
        setupQuarterSpinner();
        
        // Scoring events
        btn1P.setOnClickListener(v -> recordScoringEvent("1P", 1));
        btn2P.setOnClickListener(v -> recordScoringEvent("2P", 2));
        btn3P.setOnClickListener(v -> recordScoringEvent("3P", 3));
        
        // Miss events (with rebound workflow)
        btn1M.setOnClickListener(v -> recordMissEvent("1M"));
        btn2M.setOnClickListener(v -> recordMissEvent("2M"));
        btn3M.setOnClickListener(v -> recordMissEvent("3M"));
        
        // Rebound events
        btnOR.setOnClickListener(v -> recordEvent("OR", 0));
        btnDR.setOnClickListener(v -> recordEvent("DR", 0));
        
        // Assist event
        btnAST.setOnClickListener(v -> recordEvent("AST", 0));
        
        // Defense events
        btnSTL.setOnClickListener(v -> recordEvent("STL", 0));
        btnBLK.setOnClickListener(v -> recordEvent("BLK", 0));
        
        // Violation events
        btnTO.setOnClickListener(v -> recordTurnoverEvent("TO"));
        
        // Team events (timeouts handled by team buttons only)
        btnTeamATimeout.setOnClickListener(v -> recordTeamTimeout("home"));
        btnTeamBTimeout.setOnClickListener(v -> recordTeamTimeout("away"));
        
        // Context-aware lineup management (Quarter Lineup vs Substitution)
        btnTeamASub.setOnClickListener(v -> openContextAwareLineupModal(teamA, "home", teamAPlayers));
        btnTeamBSub.setOnClickListener(v -> openContextAwareLineupModal(teamB, "away", teamBPlayers));
        
        // New team foul buttons
        btnTeamAFoul.setOnClickListener(v -> recordTeamFoul("home"));
        btnTeamBFoul.setOnClickListener(v -> recordTeamFoul("away"));
        
        // View Log button
        btnViewLog.setOnClickListener(v -> openEventLog());
        
        // Allow Events override toggle
        btnAllowEvents.setOnClickListener(v -> toggleAllowEventsOverride());
        
        // Undo button
        btnUndo.setOnClickListener(v -> undoLastEvent());
    }
    
    // Player Selection Modal Methods
    private void openPlayerSelectionModal(Team team, String teamSide) {
        if (team == null || team.getPlayers() == null || team.getPlayers().isEmpty()) {
            Toast.makeText(this, "No players available for " + team.getName(), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Track which team's modal is open
        currentModalTeamSide = teamSide;
        
        // Create modal with Setup mode
        PlayerSelectionModal modal = PlayerSelectionModal.newInstance(
            PlayerSelectionModal.SelectionMode.SETUP,
            team.getName(),
            team.getPlayers(),
            null // No current lineup for setup mode
        );
        
        modal.setPlayerSelectionListener(this);
        modal.show(getFragmentManager(), "PlayerSelection");
    }
    
    // Context-Aware Lineup Modal Method (Timer-based decision)
    private void openContextAwareLineupModal(Team team, String teamSide, List<Player> currentPlayers) {
        // Check if we're in game mode (can't modify lineups in setup mode)
        if (isInSetupMode) {
            Toast.makeText(this, "Complete team setup first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (team == null || team.getPlayers() == null || team.getPlayers().isEmpty()) {
            Toast.makeText(this, "No players available for " + team.getName(), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentPlayers == null || currentPlayers.size() != 5) {
            Toast.makeText(this, "Must have 5 players to modify lineup", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Decide mode based on timer state
        boolean isQuarterFresh = (gameTimeSeconds == 600); // 10:00 = quarter hasn't started
        
        if (isQuarterFresh) {
            // Quarter Lineup Mode - strategic planning before quarter starts
            openQuarterChangeModal(team, teamSide, currentPlayers);
        } else {
            // Substitution Mode - tactical changes during live game
            openSubstitutionModal(team, teamSide, currentPlayers);
        }
    }
    
    // Substitution Modal Method
    private void openSubstitutionModal(Team team, String teamSide, List<Player> currentPlayers) {
        // Track which team's modal is open
        currentModalTeamSide = teamSide;
        
        // Convert current Players to TeamPlayers for the modal
        List<TeamPlayer> currentTeamPlayers = new ArrayList<>();
        for (Player player : currentPlayers) {
            // Find the corresponding TeamPlayer in the team roster
            for (TeamPlayer teamPlayer : team.getPlayers()) {
                if (teamPlayer.getNumber() == player.getNumber()) {
                    currentTeamPlayers.add(teamPlayer);
                    break;
                }
            }
        }
        
        // Create modal with Substitution mode
        PlayerSelectionModal modal = PlayerSelectionModal.newInstance(
            PlayerSelectionModal.SelectionMode.SUBSTITUTION,
            team.getName(),
            team.getPlayers(),
            currentTeamPlayers // Current lineup for substitution mode
        );
        
        modal.setPlayerSelectionListener(this);
        modal.show(getFragmentManager(), "PlayerSubstitution");
    }
    
    // PlayerSelectionModal.PlayerSelectionListener interface implementation
    @Override
    public void onPlayersSelected(List<TeamPlayer> selectedPlayers, List<TeamPlayer> playersOut) {
        
        // Check if this is a substitution operation
        boolean isSubstitution = playersOut.size() > 0;
        
        if (isSubstitution) {
            // Handle substitution - validate equal numbers in/out
            if (selectedPlayers.size() != playersOut.size()) {
                Toast.makeText(this, "Must have equal players in and out", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Process substitution
            handleSubstitution(selectedPlayers, playersOut);
            
        } else {
            // Handle setup mode or quarter change mode - must have exactly 5 players
            if (selectedPlayers.size() != 5) {
                Toast.makeText(this, "Must select exactly 5 players", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check if this is initial setup or quarter change based on current game state
            if (isInSetupMode) {
                handleInitialLineupSelection(selectedPlayers);
            } else {
                handleQuarterLineupChange(selectedPlayers);
            }
        }
    }
    
    private void handleInitialLineupSelection(List<TeamPlayer> selectedPlayers) {
        // Convert TeamPlayer to Player (game players)
        List<Player> gamePlayers = new ArrayList<>();
        for (TeamPlayer teamPlayer : selectedPlayers) {
            gamePlayers.add(teamPlayer.toGamePlayer(gameId, currentModalTeamSide));
        }
        
        // Assign to appropriate team
        if ("home".equals(currentModalTeamSide)) {
            teamAPlayers = gamePlayers;
            Toast.makeText(this, "Team A lineup set! ✅", Toast.LENGTH_SHORT).show();
        } else {
            teamBPlayers = gamePlayers;
            Toast.makeText(this, "Team B lineup set! ✅", Toast.LENGTH_SHORT).show();
        }
        
        // Immediately update UI for this team (independent of other team)
        createPlayerButtons();
        
        // Enable event buttons only when BOTH teams have 5 players
        checkIfGameReady();
    }
    
    private void handleQuarterLineupChange(List<TeamPlayer> selectedPlayers) {
        // Convert TeamPlayer to Player (game players) - reset fouls for quarter change
        List<Player> gamePlayers = new ArrayList<>();
        for (TeamPlayer teamPlayer : selectedPlayers) {
            Player gamePlayer = teamPlayer.toGamePlayer(gameId, currentModalTeamSide);
            // Keep existing personal fouls (they carry over between quarters)
            // Find existing player to preserve foul count
            List<Player> existingPlayers = "home".equals(currentModalTeamSide) ? teamAPlayers : teamBPlayers;
            for (Player existingPlayer : existingPlayers) {
                if (existingPlayer.getNumber() == gamePlayer.getNumber()) {
                    gamePlayer.setPersonalFouls(existingPlayer.getPersonalFouls());
                    break;
                }
            }
            gamePlayers.add(gamePlayer);
        }
        
        // Store old lineup for logging
        List<Player> oldLineup = "home".equals(currentModalTeamSide) ? 
                                new ArrayList<>(teamAPlayers) : new ArrayList<>(teamBPlayers);
        
        // Assign new lineup to appropriate team
        String teamName;
        if ("home".equals(currentModalTeamSide)) {
            teamAPlayers = gamePlayers;
            teamName = teamAName;
        } else {
            teamBPlayers = gamePlayers;
            teamName = teamBName;
        }
        
        // Log lineup change
        logQuarterLineupChange(oldLineup, gamePlayers, teamName);
        
        // Update UI
        createPlayerButtons();
        updatePlayerButtonText(); // Refresh foul counts etc.
        
        // Show confirmation
        Toast.makeText(this, String.format("🏀 Q%d %s lineup updated!", currentQuarter, teamName), 
                      Toast.LENGTH_SHORT).show();
    }
    
    private void logQuarterLineupChange(List<Player> oldLineup, List<Player> newLineup, String teamName) {
        // Log lineup change to game events
        String eventLogEntry = String.format("Q%d %s - %s - LINEUP CHANGE", 
                                            currentQuarter, formatGameTime(gameTimeSeconds), teamName);
        gameEvents.add(eventLogEntry);
        
        // Log individual changes (players coming in/out)
        List<Integer> oldNumbers = new ArrayList<>();
        List<Integer> newNumbers = new ArrayList<>();
        
        for (Player player : oldLineup) oldNumbers.add(player.getNumber());
        for (Player player : newLineup) newNumbers.add(player.getNumber());
        
        // Find players going out
        for (Player oldPlayer : oldLineup) {
            if (!newNumbers.contains(oldPlayer.getNumber())) {
                String outEvent = String.format("Q%d %s - %s - OUT: #%d %s", 
                                               currentQuarter, formatGameTime(gameTimeSeconds), 
                                               teamName, oldPlayer.getNumber(), oldPlayer.getName());
                gameEvents.add(outEvent);
            }
        }
        
        // Find players coming in  
        for (Player newPlayer : newLineup) {
            if (!oldNumbers.contains(newPlayer.getNumber())) {
                String inEvent = String.format("Q%d %s - %s - IN: #%d %s", 
                                              currentQuarter, formatGameTime(gameTimeSeconds), 
                                              teamName, newPlayer.getNumber(), newPlayer.getName());
                gameEvents.add(inEvent);
            }
        }
        
        // Add to live event feed
        addToLiveEventFeed("LINEUP", null, teamName + ": Q" + currentQuarter + " lineup change");
    }
    
    private void handleSubstitution(List<TeamPlayer> playersIn, List<TeamPlayer> playersOut) {
        // Get the current team players list to modify
        List<Player> currentPlayers = "home".equals(currentModalTeamSide) ? teamAPlayers : teamBPlayers;
        
        // Create new lineup by replacing outgoing players with incoming players
        List<Player> newLineup = new ArrayList<>(currentPlayers);
        
        // Remove outgoing players
        for (TeamPlayer playerOut : playersOut) {
            newLineup.removeIf(player -> player.getNumber() == playerOut.getNumber());
        }
        
        // Add incoming players
        for (TeamPlayer playerIn : playersIn) {
            newLineup.add(playerIn.toGamePlayer(gameId, currentModalTeamSide));
        }
        
        // Update team lineup
        if ("home".equals(currentModalTeamSide)) {
            teamAPlayers = newLineup;
        } else {
            teamBPlayers = newLineup;
        }
        
        // Log substitution events
        logSubstitutionEvents(playersIn, playersOut);
        
        // Update UI
        createPlayerButtons();
        updatePlayerButtonText(); // Refresh foul counts etc.
        
        // Show confirmation
        String teamName = "home".equals(currentModalTeamSide) ? teamAName : teamBName;
        Toast.makeText(this, String.format("🔄 %s: %d player%s substituted", 
                     teamName, playersIn.size(), playersIn.size() == 1 ? "" : "s"), 
                     Toast.LENGTH_SHORT).show();
    }
    
    private void logSubstitutionEvents(List<TeamPlayer> playersIn, List<TeamPlayer> playersOut) {
        String teamName = "home".equals(currentModalTeamSide) ? teamAName : teamBName;
        
        // Log each substitution
        for (int i = 0; i < playersIn.size(); i++) {
            TeamPlayer playerIn = playersIn.get(i);
            TeamPlayer playerOut = playersOut.get(i);
            
            String substitutionEvent = String.format("SUB: #%d %s → #%d %s", 
                                                   playerOut.getNumber(), playerOut.getName(),
                                                   playerIn.getNumber(), playerIn.getName());
            
            // Add to game events log
            String eventLogEntry = String.format("Q%d %s - %s - %s", 
                                                currentQuarter, formatGameTime(gameTimeSeconds), 
                                                teamName, substitutionEvent);
            gameEvents.add(eventLogEntry);
            
            // Add to live event feed
            addToLiveEventFeed("SUB", null, teamName + ": " + substitutionEvent);
        }
    }
    
    @Override
    public void onSelectionCancelled() {
        Toast.makeText(this, "Player selection cancelled", Toast.LENGTH_SHORT).show();
    }
    
    // Utility method to format game time for logging
    private String formatGameTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    private void checkIfGameReady() {
        // Enable event buttons only when BOTH teams have 5 players
        boolean bothTeamsReady = (teamAPlayers.size() == 5 && teamBPlayers.size() == 5);
        
        enableEventButtons(bothTeamsReady);
        updateGameToggleButton(); // Update START button availability
        updateAllowEventsButton(); // Update override toggle state
        
        if (bothTeamsReady) {
            isInSetupMode = false; // We're now in full game mode
            Toast.makeText(this, "🏀 Game Ready! Both teams set - events enabled.", Toast.LENGTH_LONG).show();
        } else {
            isInSetupMode = true; // Still in setup mode
            // Only show messages after user has started selecting (not on initial load)
            if (teamAPlayers.size() > 0 || teamBPlayers.size() > 0) {
                if (teamAPlayers.size() != 5 && teamBPlayers.size() != 5) {
                    // This shouldn't happen since we only get here when one team is done
                } else if (teamAPlayers.size() != 5) {
                    Toast.makeText(this, "Now select Team A players to enable events", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Now select Team B players to enable events", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Initial state - show welcoming message
                Toast.makeText(this, "Select 5 players for each team to begin", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void enableEventButtons(boolean gameReady) {
        // Event buttons enabled only if:
        // 1. Game is ready (both teams have 5 players) AND
        // 2. Timer is running OR override is active
        boolean eventsEnabled = gameReady && (isClockRunning || allowEventsOverride);
        
        // Enable/disable all event buttons based on combined conditions
        btn1P.setEnabled(eventsEnabled);
        btn2P.setEnabled(eventsEnabled);
        btn3P.setEnabled(eventsEnabled);
        btn1M.setEnabled(eventsEnabled);
        btn2M.setEnabled(eventsEnabled);
        btn3M.setEnabled(eventsEnabled);
        btnOR.setEnabled(eventsEnabled);
        btnDR.setEnabled(eventsEnabled);
        btnAST.setEnabled(eventsEnabled);
        btnSTL.setEnabled(eventsEnabled);
        btnBLK.setEnabled(eventsEnabled);
        btnTO.setEnabled(eventsEnabled);
        
        // Set colors based on enabled state - preserve original colors when enabled
        int disabledColor = Color.parseColor("#BDC3C7"); // Light grey for disabled
        
        // Scoring makes (green)
        int scoringColor = eventsEnabled ? Color.parseColor("#2ECC71") : disabledColor;
        btn1P.setBackgroundColor(scoringColor);
        btn2P.setBackgroundColor(scoringColor);
        btn3P.setBackgroundColor(scoringColor);
        
        // Scoring misses (red)
        int missColor = eventsEnabled ? Color.parseColor("#E74C3C") : disabledColor;
        btn1M.setBackgroundColor(missColor);
        btn2M.setBackgroundColor(missColor);
        btn3M.setBackgroundColor(missColor);
        
        // Rebounds & assists (teal)
        int reboundColor = eventsEnabled ? Color.parseColor("#16A085") : disabledColor;
        btnOR.setBackgroundColor(reboundColor);
        btnDR.setBackgroundColor(reboundColor);
        btnAST.setBackgroundColor(reboundColor);
        
        // Defense (purple)
        int defenseColor = eventsEnabled ? Color.parseColor("#9B59B6") : disabledColor;
        btnSTL.setBackgroundColor(defenseColor);
        btnBLK.setBackgroundColor(defenseColor);
        
        // Turnover (orange)
        int turnoverColor = eventsEnabled ? Color.parseColor("#E67E22") : disabledColor;
        btnTO.setBackgroundColor(turnoverColor);
        
        // Foul button removed - now handled by team panels
    }
    
    private void setupGameClock() {
        // Initialize with timer paused state
        isClockRunning = false;
        updateGameToggleButton();
    }
    
    private void selectPlayer(Player player, Button button) {
        // Deselect all players first
        deselectAllPlayers();
        
        // Select this player
        selectedPlayer = player;
        button.setBackgroundColor(Color.parseColor("#3498DB")); // Blue for selected
        button.setTextColor(Color.parseColor("#FFFFFF"));
        
        Toast.makeText(this, "Player selected: " + player.getName(), Toast.LENGTH_SHORT).show();
    }
    
    private void deselectAllPlayers() {
        selectedPlayer = null;
        
        // Reset Team A player button colors
        for (Button btn : teamAPlayerButtons) {
            btn.setBackgroundColor(Color.parseColor("#EEEEEE"));
            btn.setTextColor(Color.parseColor("#2C3E50"));
        }
        
        // Reset Team B player button colors
        for (Button btn : teamBPlayerButtons) {
            btn.setBackgroundColor(Color.parseColor("#EEEEEE"));
            btn.setTextColor(Color.parseColor("#2C3E50"));
        }
    }
    
    // Quarter Management
    private void setupQuarterSpinner() {
        // Create quarter options
        String[] quarters = {"Q1", "Q2", "Q3", "Q4"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, quarters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuarter.setAdapter(adapter);
        
        // Set current quarter
        spinnerQuarter.setSelection(currentQuarter - 1);
        
        // Handle quarter selection
        spinnerQuarter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int newQuarter = position + 1;
                if (newQuarter != currentQuarter) {
                    selectQuarter(newQuarter);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void selectQuarter(int quarter) {
        // Direct quarter change - reset clock and stop timer
        currentQuarter = quarter;
        gameTimeSeconds = 600; // Reset to 10 minutes
        
        // Stop timer if running
        if (isClockRunning) {
            pauseClock();
        }
        
        // Update displays (including context-aware button text)
        updateAllDisplays();
        
        // Show helpful message about quarter lineup changes
        if (!isInSetupMode && (teamAPlayers.size() == 5 && teamBPlayers.size() == 5)) {
            Toast.makeText(this, "Quarter " + quarter + " ready - Use 'Quarter Lineup' to modify teams", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Quarter " + quarter + " selected - Clock reset to 10:00", Toast.LENGTH_SHORT).show();
        }
    }
    
    // Context-Aware Button Text Updates
    private void updateContextAwareButtons() {
        // Update button text based on timer state
        boolean isQuarterFresh = (gameTimeSeconds == 600); // 10:00 = quarter hasn't started
        
        if (isQuarterFresh) {
            // Quarter hasn't started - show "Quarter Lineup" for strategic planning
            btnTeamASub.setText("Quarter Lineup");
            btnTeamBSub.setText("Quarter Lineup");
        } else {
            // Quarter in progress - show "Sub" for tactical substitutions
            btnTeamASub.setText("Sub");
            btnTeamBSub.setText("Sub");
        }
    }
    
    // Quarter Change Modal Method
    private void openQuarterChangeModal(Team team, String teamSide, List<Player> currentPlayers) {
        if (team == null || team.getPlayers() == null || team.getPlayers().isEmpty()) {
            Toast.makeText(this, "No players available for " + team.getName(), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentPlayers == null || currentPlayers.size() != 5) {
            Toast.makeText(this, "Current lineup must have 5 players", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Track which team's modal is open
        currentModalTeamSide = teamSide;
        
        // Convert current Players to TeamPlayers for the modal
        List<TeamPlayer> currentTeamPlayers = new ArrayList<>();
        for (Player player : currentPlayers) {
            // Find the corresponding TeamPlayer in the team roster
            for (TeamPlayer teamPlayer : team.getPlayers()) {
                if (teamPlayer.getNumber() == player.getNumber()) {
                    currentTeamPlayers.add(teamPlayer);
                    break;
                }
            }
        }
        
        // Create modal with Quarter Change mode
        PlayerSelectionModal modal = PlayerSelectionModal.newInstance(
            PlayerSelectionModal.SelectionMode.QUARTER_CHANGE,
            team.getName(),
            team.getPlayers(),
            currentTeamPlayers // Current lineup pre-selected
        );
        
        modal.setPlayerSelectionListener(this);
        modal.show(getFragmentManager(), "QuarterChange");
    }
    
    // Event Recording Methods
    
    // Scoring Events (with assist workflow for 2P/3P)
    private void recordScoringEvent(String eventType, int points) {
        if (selectedPlayer == null) {
            Toast.makeText(this, "Please select a player first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Record scoring event to SQLite database
            String playerTeam = selectedPlayer.getTeam();
            if ("home".equals(playerTeam)) {
                currentGame.setHomeScore(currentGame.getHomeScore() + points);
            } else {
                currentGame.setAwayScore(currentGame.getAwayScore() + points);
            }
            
            // Create and save Event to database
            Event event = new Event(currentGame.getId(), selectedPlayer.getId(), playerTeam, 
                                   currentGame.getCurrentQuarter(), currentGame.getGameClockSeconds(), eventType);
            event.setEventSequence(eventSequenceCounter++);
            event.save(dbController.getDatabaseHelper());
            
            // Add to local event list
            gameEvents.add(event);
            
            // Save updated game state
            saveGameState();
            
            // Visual feedback - flash button blue for 3 seconds
            flashEventButton(getEventButton(eventType));
            
            // Show feedback
            Toast.makeText(this, String.format("%s recorded for %s (+%d points)", 
                eventType, selectedPlayer.getName(), points), Toast.LENGTH_SHORT).show();
            
            // Update displays and deselect
            updateScoreDisplay();
            addToLiveEventFeedSQLite(event);
            deselectAllPlayers();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error saving event: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("GameActivity", "Error recording scoring event", e);
        }
        
        // Single-event safety: Reset override after event recorded
        checkAndResetSingleEventOverride();
        
        // Assist workflow for 2P and 3P
        if ("2P".equals(eventType) || "3P".equals(eventType)) {
            // For MVP, we'll skip the assist pop-up and just enable assist button
            // TODO: Implement assist pop-up or streamlined assist workflow
            Toast.makeText(this, "Assist available (tap AST button if applicable)", Toast.LENGTH_SHORT).show();
        }
    }
    
    // Miss Events (with rebound workflow)
    private void recordMissEvent(String eventType) {
        if (selectedPlayer == null) {
            Toast.makeText(this, "Please select a player first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Visual feedback - flash button blue for 3 seconds
        flashEventButton(getEventButton(eventType));
        
        // Show feedback
        Toast.makeText(this, String.format("%s recorded for %s", 
            eventType, selectedPlayer.getName()), Toast.LENGTH_SHORT).show();
        
        addToLiveEventFeed(eventType, selectedPlayer);
        deselectAllPlayers();
        
        // Single-event safety: Reset override after event recorded
        checkAndResetSingleEventOverride();
        
        // Rebound workflow
        // For MVP, we'll skip the rebound pop-up and just show hint
        Toast.makeText(this, "Rebound available (tap OR/DR button for rebounder)", Toast.LENGTH_SHORT).show();
    }
    
    // Foul Events (increment personal and team fouls)
    private void recordFoulEvent(String eventType) {
        if (selectedPlayer == null) {
            Toast.makeText(this, "Please select a player first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Increment personal fouls
        selectedPlayer.setPersonalFouls(selectedPlayer.getPersonalFouls() + 1);
        
        // Increment team fouls
        String playerTeam = selectedPlayer.getTeam();
        if ("home".equals(playerTeam)) {
            teamAFouls++;
        } else {
            teamBFouls++;
        }
        
        // Visual feedback
        if ("home".equals(playerTeam)) {
            flashEventButton(btnTeamAFoul);
        } else {
            flashEventButton(btnTeamBFoul);
        }
        
        // Check for foul out (5 fouls)
        if (selectedPlayer.getPersonalFouls() >= 5) {
            Toast.makeText(this, selectedPlayer.getName() + " fouled out! (5 fouls)", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, String.format("Foul recorded for %s (%d fouls)", 
                selectedPlayer.getName(), selectedPlayer.getPersonalFouls()), Toast.LENGTH_SHORT).show();
        }
        
        // Update displays
        updatePlayerButtonText();
        updateTeamFoulsDisplay();
        addToLiveEventFeed(eventType, selectedPlayer);
        deselectAllPlayers();
        
        // Single-event safety: Reset override after event recorded
        checkAndResetSingleEventOverride();
    }
    
    // Turnover Events (with steal workflow)
    private void recordTurnoverEvent(String eventType) {
        if (selectedPlayer == null) {
            Toast.makeText(this, "Please select a player first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Visual feedback
        flashEventButton(btnTO);
        
        Toast.makeText(this, String.format("Turnover recorded for %s", selectedPlayer.getName()), Toast.LENGTH_SHORT).show();
        
        addToLiveEventFeed(eventType, selectedPlayer);
        deselectAllPlayers();
        
        // Single-event safety: Reset override after event recorded
        checkAndResetSingleEventOverride();
        
        // Steal workflow
        // For MVP, we'll skip the steal pop-up and just show hint
        Toast.makeText(this, "Steal available (tap STL button for defensive player)", Toast.LENGTH_SHORT).show();
    }
    
    // Generic Event Recording
    private void recordEvent(String eventType, int points) {
        if (selectedPlayer == null) {
            Toast.makeText(this, "Please select a player first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Visual feedback
        flashEventButton(getEventButton(eventType));
        
        // Show feedback
        Toast.makeText(this, String.format("%s recorded for %s", 
            eventType, selectedPlayer.getName()), Toast.LENGTH_SHORT).show();
        
        addToLiveEventFeed(eventType, selectedPlayer);
        deselectAllPlayers();
        
        // Single-event safety: Reset override after event recorded
        checkAndResetSingleEventOverride();
    }
    
    private void recordTeamTimeout(String team) {
        // Timeout pauses the clock and updates UI state
        pauseClock();
        String teamName = "home".equals(team) ? teamAName : teamBName;
        Toast.makeText(this, teamName + " timeout called", Toast.LENGTH_SHORT).show();
        
        // Add team event to live feed (no specific player)
        addToLiveEventFeed("TIMEOUT", null, teamName);
        
        // Single-event safety: Reset override after event recorded
        checkAndResetSingleEventOverride();
    }
    
    private void recordTeamFoul(String team) {
        // Team foul buttons now record personal fouls (require player selection)
        if (selectedPlayer == null) {
            String teamName = "home".equals(team) ? teamAName : teamBName;
            Toast.makeText(this, "Select a " + teamName + " player first to record personal foul", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Record as personal foul (same as old FOUL button functionality)
        recordFoulEvent("FOUL");
    }
    
    private void undoLastEvent() {
        if (gameEvents.isEmpty()) {
            Toast.makeText(this, "No events to undo", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get the last event
        String lastEvent = gameEvents.get(0); // Most recent is at index 0
        
        // Remove it from the events list
        gameEvents.remove(0);
        
        // Update recent events list and display
        updateRecentEventsList();
        updateLiveEventFeedDisplay();
        
        // TODO: Parse the event and undo its effects on scores/fouls
        // For MVP, just show confirmation
        Toast.makeText(this, "Event undone: " + lastEvent.split(" - ")[2], Toast.LENGTH_SHORT).show();
        
        // NOTE: Full implementation would:
        // 1. Parse the event type and details
        // 2. Reverse score changes (subtract points)
        // 3. Reverse foul changes (decrement fouls)
        // 4. Update all displays accordingly
    }
    
        private void toggleGameTimer() {
        if (isClockRunning) {
            // Currently running, so pause it
            pauseClock();
        } else {
            // Currently stopped, so start it
            startClock();
        }
    }
    
    private void startClock() {
        // CRITICAL: Stop any existing timer first to prevent multiple timers
        stopClock();
        
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameTimeSeconds > 0) {
                    gameTimeSeconds--;
                    updateGameClockDisplay();
                    
                    // Update context-aware buttons when timer changes from 10:00 to 9:59
                    if (gameTimeSeconds == 599) { // Just crossed from 10:00 to 9:59
                        updateContextAwareButtons();
                    }
                    
                    clockHandler.postDelayed(this, 1000); // Update every second
                } else {
                    // Quarter complete handling
                    updateGameClockDisplay();

                    int completedQuarter = currentQuarter;

                    // Pause to update UI state
                    pauseClock();

                    if (completedQuarter < 4) {
                        // Advance to next quarter and reset clock
                        currentQuarter = completedQuarter + 1;
                        gameTimeSeconds = 600; // 10:00
                        updateAllDisplays();
                        if (spinnerQuarter != null) {
                            spinnerQuarter.setSelection(currentQuarter - 1);
                        }
                        Toast.makeText(
                            GameActivity.this,
                            "Quarter " + completedQuarter + " complete! Starting Quarter " + currentQuarter,
                            Toast.LENGTH_LONG
                        ).show();
                    } else {
                        // Q4 complete – game over
                        Toast.makeText(
                            GameActivity.this,
                            "Quarter 4 complete! Game over.",
                            Toast.LENGTH_LONG
                        ).show();
                    }
                }
            }
        };
        
        clockHandler.post(clockRunnable);
        
        // Update timer state and button
        isClockRunning = true;
        
        // Auto-reset override toggle when timer starts
        allowEventsOverride = false;
        
        // Update both buttons to reflect new timer state
        updateGameToggleButton();
        updateAllowEventsButton();
        
        // Update event buttons to reflect new timer state
        boolean bothTeamsReady = (teamAPlayers.size() == 5 && teamBPlayers.size() == 5);
        enableEventButtons(bothTeamsReady);
        
        Toast.makeText(this, "⏰ Game clock started", Toast.LENGTH_SHORT).show();
    }

    private void stopClock() {
        if (clockRunnable != null) {
            clockHandler.removeCallbacks(clockRunnable);
            clockRunnable = null; // Clear reference
        }
    }
    
    private void pauseClock() {
        stopClock();
        
        // Update timer state and button
        isClockRunning = false;
        updateGameToggleButton();
        updateAllowEventsButton(); // CRITICAL: Update Allow Events button when timer pauses
        
        // Update event buttons to reflect new timer state (they should be disabled unless override active)
        boolean bothTeamsReady = (teamAPlayers.size() == 5 && teamBPlayers.size() == 5);
        enableEventButtons(bothTeamsReady);
        
        Toast.makeText(this, "⏸️ Game clock paused", Toast.LENGTH_SHORT).show();
    }
    
    private void updateGameToggleButton() {
        // Check if both teams are ready (have 5 players each)
        boolean bothTeamsReady = (teamAPlayers.size() == 5 && teamBPlayers.size() == 5);
        
        if (!bothTeamsReady) {
            // Setup incomplete - disable START button
            btnGameToggle.setText("START");
            btnGameToggle.setBackgroundColor(Color.parseColor("#BDC3C7")); // Grey (disabled)
            btnGameToggle.setEnabled(false);
            
            // Clock background: Default (neutral)
            tvGameClock.setBackgroundColor(getResources().getColor(android.R.color.background_light));
        } else if (isClockRunning) {
            // Timer is running - show PAUSE option
            btnGameToggle.setText("PAUSE");
            btnGameToggle.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light)); // Blue (pleasant during gameplay)
            btnGameToggle.setEnabled(true);
            
            // Clock background: Green (running)
            tvGameClock.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            // Timer is paused but game is ready - show START option
            btnGameToggle.setText("START");
            btnGameToggle.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light)); // Green (ready to start)
            btnGameToggle.setEnabled(true);
            
            // Clock background: Yellow (paused)
            tvGameClock.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        }
    }
    
    private void updateAllDisplays() {
        updateScoreDisplay();
        updateGameClockDisplay();
        updateTeamFoulsDisplay();
        updateContextAwareButtons(); // Update button text based on timer state
        // Quarter display is now handled by spinner
    }
    
    private void updateScoreDisplay() {
        // Update team scores for blue strip layout using SQLite currentGame
        if (currentGame != null) {
            tvTeamAScore.setText(String.valueOf(currentGame.getHomeScore()));
            tvTeamBScore.setText(String.valueOf(currentGame.getAwayScore()));
        }
    }
    
    private void updateGameClockDisplay() {
        if (currentGame != null) {
            int minutes = currentGame.getGameClockSeconds() / 60;
            int seconds = currentGame.getGameClockSeconds() % 60;
            tvGameClock.setText(String.format("%d:%02d", minutes, seconds));
        }
    }
    
    // Quarter display now handled by spinner - no separate method needed
    
    private void updateTeamFoulsDisplay() {
        // Update team foul displays for new blue strip layout (fouls count + "F")
        int redColor = Color.parseColor("#F44336");
        int whiteColor = Color.parseColor("#FFFFFF");
        
        // Update Team A fouls
        tvTeamAFouls.setText(teamAFouls + "F");
        tvTeamAFouls.setTextColor(teamAFouls >= 5 ? redColor : whiteColor);
        
        // Update Team B fouls  
        tvTeamBFouls.setText(teamBFouls + "F");
        tvTeamBFouls.setTextColor(teamBFouls >= 5 ? redColor : whiteColor);
    }
    
    private void updatePlayerButtonText() {
        // Update Team A player buttons with current foul counts
        for (int i = 0; i < teamAPlayerButtons.size() && i < teamAPlayers.size(); i++) {
            Player player = teamAPlayers.get(i);
            Button btn = teamAPlayerButtons.get(i);
            btn.setText(String.format("#%d %s [%d]", player.getNumber(), player.getName(), player.getPersonalFouls()));
        }
        
        // Update Team B player buttons with current foul counts
        for (int i = 0; i < teamBPlayerButtons.size() && i < teamBPlayers.size(); i++) {
            Player player = teamBPlayers.get(i);
            Button btn = teamBPlayerButtons.get(i);
            btn.setText(String.format("#%d %s [%d]", player.getNumber(), player.getName(), player.getPersonalFouls()));
        }
    }
    
    // Quick Visual Feedback - Flash event button blue for 0.3 seconds (updated specification)
    private void flashEventButton(Button button) {
        if (button == null) return;
        
        // Store original color based on button type
        int originalColor = getOriginalButtonColor(button);
        
        // Flash blue
        button.setBackgroundColor(Color.parseColor("#3498DB")); // Blue
        
        // Reset after 0.3 seconds (much faster, less disruptive)
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            button.setBackgroundColor(originalColor);
        }, 300); // 0.3 seconds
    }
    
    private int getOriginalButtonColor(Button button) {
        // Return appropriate color based on button type
        if (button == btn1P || button == btn2P || button == btn3P) {
            return Color.parseColor("#2ECC71"); // Green for scoring
        } else if (button == btn1M || button == btn2M || button == btn3M) {
            return Color.parseColor("#E74C3C"); // Red for misses
        } else if (button == btnOR || button == btnDR || button == btnAST) {
            return Color.parseColor("#16A085"); // Teal for rebounds/assists
        } else if (button == btnSTL || button == btnBLK) {
            return Color.parseColor("#9B59B6"); // Purple for defense
        } else if (button == btnTO) {
            return Color.parseColor("#E67E22"); // Orange for turnover
        }
        // FOUL button removed - now handled by team panels
        return Color.parseColor("#BDC3C7"); // Default grey
    }
    
    // Live Event Feed Management
    private void addToLiveEventFeed(String eventType, Player player) {
        addToLiveEventFeed(eventType, player, null);
    }
    
    private void addToLiveEventFeed(String eventType, Player player, String teamName) {
        // Legacy method for backward compatibility
        // Create event description
        String timeStr = String.format("%d:%02d", currentGame.getGameClockSeconds() / 60, currentGame.getGameClockSeconds() % 60);
        String eventDescription;
        
        if (player != null) {
            // Player event - include quarter info
            eventDescription = String.format("Q%d %s - #%d %s - %s", 
                currentGame.getCurrentQuarter(), timeStr, player.getNumber(), player.getName(), eventType);
        } else {
            // Team event - include quarter info
            eventDescription = String.format("Q%d %s - %s - %s", 
                currentGame.getCurrentQuarter(), timeStr, teamName, eventType);
        }
        
        // Update recent events display
        updateRecentEventsFeed();
    }
    
    /**
     * Add SQLite Event to live feed
     */
    private void addToLiveEventFeedSQLite(Event event) {
        // Update recent events from database
        updateRecentEventsFeed();
        
        // Update live feed display
        updateLiveEventFeedDisplay();
    }
    
    /**
     * Update recent events from SQLite gameEvents list
     */
    private void updateRecentEventsFeed() {
        recentEvents.clear();
        int count = Math.min(2, gameEvents.size());
        for (int i = gameEvents.size() - count; i < gameEvents.size(); i++) {
            Event event = gameEvents.get(i);
            String timeStr = String.format("%d:%02d", event.getGameTimeSeconds() / 60, event.getGameTimeSeconds() % 60);
            String eventDescription;
            
            if (event.getPlayer() != null) {
                eventDescription = String.format("Q%d %s - #%d %s - %s", 
                    event.getQuarter(), timeStr, event.getPlayer().getJerseyNumber(), 
                    event.getPlayer().getName(), event.getEventType());
            } else {
                eventDescription = String.format("Q%d %s - %s - %s", 
                    event.getQuarter(), timeStr, event.getTeamSide().toUpperCase(), event.getEventType());
            }
            
            recentEvents.add(0, eventDescription); // Add at beginning (most recent first)
        }
        
        // Update display
        updateLiveEventFeedDisplay();
    }
    

    

    
    private void updateLiveEventFeedDisplay() {
        // Clear current feed display
        llLiveEventFeed.removeAllViews();
        
        // Add each recent event as a TextView
        for (String event : recentEvents) {
            TextView eventView = new TextView(this);
            eventView.setText(event);
            eventView.setTextSize(10);
            eventView.setTextColor(Color.parseColor("#2C3E50"));
            eventView.setPadding(4, 2, 4, 2);
            eventView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 1, 0, 1);
            eventView.setLayoutParams(params);
            
            llLiveEventFeed.addView(eventView);
        }
    }
    
    private void openEventLog() {
        // Navigate to LogActivity (Frame 5) for complete event history
        android.content.Intent intent = new android.content.Intent(this, LogActivity.class);
        intent.putExtra("gameId", gameId);
        intent.putExtra("teamAName", teamAName);
        intent.putExtra("teamBName", teamBName);
        
        startActivity(intent);
    }
    
    private void updateRecentEventsList() {
        // Update recent events from gameEvents (last 2)
        recentEvents.clear();
        int count = Math.min(2, gameEvents.size());
        for (int i = 0; i < count; i++) {
            recentEvents.add(gameEvents.get(i));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh recent events in case they were modified in LogActivity
        updateRecentEventsList();
        updateLiveEventFeedDisplay();
    }
    
    // Note: Static methods removed - LogActivity should access events via SQLite database
    // Use Event.findByGameId(dbHelper, gameId) to get events for a specific game
    
    // Helper to get event button by type
    private Button getEventButton(String eventType) {
        switch (eventType) {
            case "1P": return btn1P;
            case "2P": return btn2P;
            case "3P": return btn3P;
            case "1M": return btn1M;
            case "2M": return btn2M;
            case "3M": return btn3M;
            case "OR": return btnOR;
            case "DR": return btnDR;
            case "AST": return btnAST;
            case "STL": return btnSTL;
            case "BLK": return btnBLK;
            case "TO": return btnTO;
            case "FOUL": return null; // FOUL button moved to team panels
            default: return null;
        }
    }
    
    // Single-Event Safety: Auto-reset override after each event
    private void checkAndResetSingleEventOverride() {
        if (allowEventsOverride && !isClockRunning) {
            // Single-event safety: Reset override to OFF after event recorded during pause
            allowEventsOverride = false;
            updateAllowEventsButton();
            
            // Update event buttons to reflect new state (should be disabled now)
            boolean bothTeamsReady = (teamAPlayers.size() == 5 && teamBPlayers.size() == 5);
            enableEventButtons(bothTeamsReady);
            
            // Optional: Show feedback that override was reset
            // Toast.makeText(this, "Events disabled - click 'Events: OFF' again for next event", Toast.LENGTH_SHORT).show();
        }
    }

    // Allow Events Override Toggle Management
    private void toggleAllowEventsOverride() {
        // Only allow toggle when timer is stopped and game is ready
        boolean bothTeamsReady = (teamAPlayers.size() == 5 && teamBPlayers.size() == 5);
        
        if (!bothTeamsReady) {
            Toast.makeText(this, "Complete team setup first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isClockRunning) {
            // Events are always allowed when timer is running, no need for override
            Toast.makeText(this, "Events are already enabled while timer is running", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Toggle the override state
        allowEventsOverride = !allowEventsOverride;
        
        // Update UI to reflect new state
        updateAllowEventsButton();
        enableEventButtons(bothTeamsReady); // Refresh event buttons
        
        // Show feedback
        String message = allowEventsOverride ? 
                        "🟢 Events ON - Can record during pause" : 
                        "🔴 Events OFF - No recording during pause";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    private void updateAllowEventsButton() {
        boolean bothTeamsReady = (teamAPlayers.size() == 5 && teamBPlayers.size() == 5);
        
        if (!bothTeamsReady) {
            // Game not ready - show disabled state
            btnAllowEvents.setText("Events: DISABLED");
            btnAllowEvents.setBackgroundColor(Color.parseColor("#BDC3C7")); // Grey
            btnAllowEvents.setTextColor(Color.parseColor("#7F8C8D"));
            btnAllowEvents.setEnabled(false);
        } else if (isClockRunning) {
            // Timer is running - events are always enabled, show current state but disable button
            btnAllowEvents.setText("Events: ACTIVE");
            btnAllowEvents.setBackgroundColor(Color.parseColor("#3498DB")); // Blue (active during play)
            btnAllowEvents.setTextColor(Color.parseColor("#FFFFFF"));
            btnAllowEvents.setEnabled(false); // Can't toggle during live play
        } else if (allowEventsOverride) {
            // Timer stopped, override ON - events enabled during pause
            btnAllowEvents.setText("Events: ON");
            btnAllowEvents.setBackgroundColor(Color.parseColor("#27AE60")); // Green (override active)
            btnAllowEvents.setTextColor(Color.parseColor("#FFFFFF"));
            btnAllowEvents.setEnabled(true); // Can toggle off
        } else {
            // Timer stopped, override OFF - events blocked during pause
            btnAllowEvents.setText("Events: OFF");
            btnAllowEvents.setBackgroundColor(Color.parseColor("#E74C3C")); // Red (blocked)
            btnAllowEvents.setTextColor(Color.parseColor("#FFFFFF"));
            btnAllowEvents.setEnabled(true); // Can toggle on
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the clock when activity is destroyed
        if (clockHandler != null) {
            clockHandler.removeCallbacks(clockRunnable);
        }
    }
}
