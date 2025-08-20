package com.example.myapp;

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
import com.example.myapp.models.Player;
import com.example.myapp.models.Team;
import com.example.myapp.models.TeamPlayer;
import com.example.myapp.data.LeagueDataProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Game Activity - Live Basketball Statistics Recording (Frame 3)
 * THE CORE FUNCTIONALITY - Real-time game statistics recording interface
 * Now supports dual modes: Setup Mode (player selection) and Game Mode (live recording)
 */
public class GameActivity extends Activity implements PlayerSelectionModal.PlayerSelectionListener {
    
    // Game State
    private int gameId;
    private String teamAName, teamBName;
    private List<Player> teamAPlayers, teamBPlayers;
    private int teamAScore = 0, teamBScore = 0;
    private int currentQuarter = 1;
    private int gameTimeSeconds = 600; // 10 minutes
    private boolean isClockRunning = false;
    private Player selectedPlayer = null;
    
    // Dual Mode Support (Setup Mode vs Game Mode)
    private boolean isInSetupMode = true; // Start in setup mode
    private Team teamA, teamB; // Full team rosters for player selection
    private Button btnSelectTeamAPlayers, btnSelectTeamBPlayers; // Setup mode buttons
    private String currentModalTeamSide; // Track which team's modal is currently open
    
    // UI Components - Top Panel (Enhanced 2-Row Layout)
    private TextView tvTeamAScore, tvTeamBScore, tvGameClock;
    private TextView tvTeamAFouls, tvTeamBFouls;
    private Button btnGameToggle;
    
    // UI Components - Team Panels
    private TextView tvTeamAName, tvTeamBName;
    private LinearLayout llTeamAPlayers, llTeamBPlayers;
    private Button btnTeamATimeout, btnTeamASub, btnTeamBTimeout, btnTeamBSub;
    
    // UI Components - Event Panel (12+ buttons)
    private Button btn1P, btn2P, btn3P, btn1M, btn2M, btn3M;
    private Button btnOR, btnDR, btnAST, btnSTL, btnBLK, btnTO, btnFOUL;
    
    // UI Components - Quarter Management
    private Spinner spinnerQuarter;
    
    // UI Components - Live Event Feed
    private LinearLayout llLiveEventFeed;
    private Button btnViewLog;
    
    // Game Management
    private Handler clockHandler = new Handler();
    private Runnable clockRunnable;
    private List<Button> teamAPlayerButtons, teamBPlayerButtons;
    private int teamAFouls = 0, teamBFouls = 0;
    
    // Event Tracking
    private static List<String> gameEvents = new ArrayList<>(); // Shared event storage for entire game
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
        gameId = getIntent().getIntExtra("gameId", 1);
        
        // Try to get team names from different intent sources
        teamAName = getIntent().getStringExtra("teamAName");
        teamBName = getIntent().getStringExtra("teamBName");
        
        // If not found, try alternative keys (for compatibility with MainActivity)
        if (teamAName == null) teamAName = getIntent().getStringExtra("homeTeam");
        if (teamBName == null) teamBName = getIntent().getStringExtra("awayTeam");
        
        // Load full team rosters for player selection
        teamA = LeagueDataProvider.getTeamByName(teamAName);
        teamB = LeagueDataProvider.getTeamByName(teamBName);
        
        // Initialize empty player lists (will be populated when players are selected)
        teamAPlayers = new ArrayList<>();
        teamBPlayers = new ArrayList<>();
        
        // Check if we have pre-selected players from previous activities
        // For MVP, we start in Setup Mode
        isInSetupMode = teamAPlayers.isEmpty() || teamBPlayers.isEmpty();
        
        if (!isInSetupMode) {
            // If we somehow got pre-selected players, use them (future enhancement)
            // For now, we always start in Setup Mode
            isInSetupMode = true;
        }
    }
    

    
    private void initializeViews() {
        // Top panel components (Enhanced 2-Row Layout)
        tvTeamAScore = findViewById(R.id.tvTeamAScore);
        tvTeamBScore = findViewById(R.id.tvTeamBScore);
        tvGameClock = findViewById(R.id.tvGameClock);
        tvTeamAFouls = findViewById(R.id.tvTeamAFouls);
        tvTeamBFouls = findViewById(R.id.tvTeamBFouls);
        btnGameToggle = findViewById(R.id.btnGameToggle);
        
        // Team panel components
        tvTeamAName = findViewById(R.id.tvTeamAName);
        tvTeamBName = findViewById(R.id.tvTeamBName);
        llTeamAPlayers = findViewById(R.id.llTeamAPlayers);
        llTeamBPlayers = findViewById(R.id.llTeamBPlayers);
        btnTeamATimeout = findViewById(R.id.btnTeamATimeout);
        btnTeamASub = findViewById(R.id.btnTeamASub);
        btnTeamBTimeout = findViewById(R.id.btnTeamBTimeout);
        btnTeamBSub = findViewById(R.id.btnTeamBSub);
        
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
        btnFOUL = findViewById(R.id.btnFOUL);
        
        // Quarter management component
        spinnerQuarter = findViewById(R.id.spinnerQuarter);
        
        // Live event feed components
        llLiveEventFeed = findViewById(R.id.llLiveEventFeed);
        btnViewLog = findViewById(R.id.btnViewLog);
    }
    
    private void initializeGameState() {
        // Initialize team names
        tvTeamAName.setText(teamAName);
        tvTeamBName.setText(teamBName);
        
        // Initialize player button lists
        teamAPlayerButtons = new ArrayList<>();
        teamBPlayerButtons = new ArrayList<>();
        
        // Initialize event tracking
        recentEvents = new ArrayList<>();
        
        // Clear game events when starting new game (for MVP)
        // TODO: In full implementation, load from database
        gameEvents.clear();
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
        
        // Set layout parameters
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            0, 1.0f);
        params.setMargins(4, 4, 4, 4);
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
        btnFOUL.setOnClickListener(v -> recordFoulEvent("FOUL"));
        
        // Team events (timeouts handled by team buttons only)
        btnTeamATimeout.setOnClickListener(v -> recordTeamTimeout("home"));
        btnTeamBTimeout.setOnClickListener(v -> recordTeamTimeout("away"));
        
        // Context-aware lineup management (Quarter Lineup vs Substitution)
        btnTeamASub.setOnClickListener(v -> openContextAwareLineupModal(teamA, "home", teamAPlayers));
        btnTeamBSub.setOnClickListener(v -> openContextAwareLineupModal(teamB, "away", teamBPlayers));
        
        // View Log button
        btnViewLog.setOnClickListener(v -> openEventLog());
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
    
    private void enableEventButtons(boolean enabled) {
        // Enable/disable all event buttons based on mode
        btn1P.setEnabled(enabled);
        btn2P.setEnabled(enabled);
        btn3P.setEnabled(enabled);
        btn1M.setEnabled(enabled);
        btn2M.setEnabled(enabled);
        btn3M.setEnabled(enabled);
        btnOR.setEnabled(enabled);
        btnDR.setEnabled(enabled);
        btnAST.setEnabled(enabled);
        btnSTL.setEnabled(enabled);
        btnBLK.setEnabled(enabled);
        btnTO.setEnabled(enabled);
        btnFOUL.setEnabled(enabled);
        
        // Set colors based on enabled state - preserve original colors when enabled
        int disabledColor = Color.parseColor("#BDC3C7"); // Light grey for disabled
        
        // Scoring makes (green)
        int scoringColor = enabled ? Color.parseColor("#2ECC71") : disabledColor;
        btn1P.setBackgroundColor(scoringColor);
        btn2P.setBackgroundColor(scoringColor);
        btn3P.setBackgroundColor(scoringColor);
        
        // Scoring misses (red)
        int missColor = enabled ? Color.parseColor("#E74C3C") : disabledColor;
        btn1M.setBackgroundColor(missColor);
        btn2M.setBackgroundColor(missColor);
        btn3M.setBackgroundColor(missColor);
        
        // Rebounds & assists (teal)
        int reboundColor = enabled ? Color.parseColor("#16A085") : disabledColor;
        btnOR.setBackgroundColor(reboundColor);
        btnDR.setBackgroundColor(reboundColor);
        btnAST.setBackgroundColor(reboundColor);
        
        // Defense (purple)
        int defenseColor = enabled ? Color.parseColor("#9B59B6") : disabledColor;
        btnSTL.setBackgroundColor(defenseColor);
        btnBLK.setBackgroundColor(defenseColor);
        
        // Turnover (orange)
        int turnoverColor = enabled ? Color.parseColor("#E67E22") : disabledColor;
        btnTO.setBackgroundColor(turnoverColor);
        
        // Foul (dark purple)
        int foulColor = enabled ? Color.parseColor("#8E44AD") : disabledColor;
        btnFOUL.setBackgroundColor(foulColor);
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
        
        // Record scoring event
        String playerTeam = selectedPlayer.getTeam();
        if ("home".equals(playerTeam)) {
            teamAScore += points;
        } else {
            teamBScore += points;
        }
        
        // Visual feedback - flash button blue for 3 seconds
        flashEventButton(getEventButton(eventType));
        
        // Show feedback
        Toast.makeText(this, String.format("%s recorded for %s (+%d points)", 
            eventType, selectedPlayer.getName(), points), Toast.LENGTH_SHORT).show();
        
        // Update displays and deselect
        updateScoreDisplay();
        addToLiveEventFeed(eventType, selectedPlayer);
        deselectAllPlayers();
        
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
        flashEventButton(btnFOUL);
        
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
    }
    
    private void recordTeamTimeout(String team) {
        // Timeout pauses the clock and updates UI state
        pauseClock();
        String teamName = "home".equals(team) ? teamAName : teamBName;
        Toast.makeText(this, teamName + " timeout called", Toast.LENGTH_SHORT).show();
        
        // Add team event to live feed (no specific player)
        addToLiveEventFeed("TIMEOUT", null, teamName);
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
        updateGameToggleButton();
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
        Toast.makeText(this, "⏸️ Game clock paused", Toast.LENGTH_SHORT).show();
    }
    
    private void updateGameToggleButton() {
        if (isClockRunning) {
            // Timer is running - show PAUSE option
            btnGameToggle.setText("PAUSE");
            btnGameToggle.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light)); // Blue (pleasant during gameplay)
            
            // Clock background: Green (running)
            tvGameClock.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            // Timer is paused - show START option
            btnGameToggle.setText("START");
            btnGameToggle.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light)); // Green (ready to start)
            
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
        // Update separate team score displays for enhanced visibility
        tvTeamAScore.setText(String.format("%s %d", teamAName, teamAScore));
        tvTeamBScore.setText(String.format("%s %d", teamBName, teamBScore));
    }
    
    private void updateGameClockDisplay() {
        int minutes = gameTimeSeconds / 60;
        int seconds = gameTimeSeconds % 60;
        tvGameClock.setText(String.format("%d:%02d", minutes, seconds));
    }
    
    // Quarter display now handled by spinner - no separate method needed
    
    private void updateTeamFoulsDisplay() {
        // Update separate team foul displays with enhanced visibility and color coding
        int redColor = Color.parseColor("#F44336");
        int whiteColor = Color.parseColor("#FFFFFF");
        
        // Update Team A fouls
        tvTeamAFouls.setText(String.format("%s %d", teamAName, teamAFouls));
        tvTeamAFouls.setTextColor(teamAFouls >= 5 ? redColor : whiteColor);
        
        // Update Team B fouls  
        tvTeamBFouls.setText(String.format("%s %d", teamBName, teamBFouls));
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
        } else if (button == btnFOUL) {
            return Color.parseColor("#8E44AD"); // Purple for foul
        }
        return Color.parseColor("#BDC3C7"); // Default grey
    }
    
    // Live Event Feed Management
    private void addToLiveEventFeed(String eventType, Player player) {
        addToLiveEventFeed(eventType, player, null);
    }
    
    private void addToLiveEventFeed(String eventType, Player player, String teamName) {
        // Create event description
        String timeStr = String.format("%d:%02d", gameTimeSeconds / 60, gameTimeSeconds % 60);
        String eventDescription;
        
        if (player != null) {
            // Player event - include quarter info
            eventDescription = String.format("Q%d %s - #%d %s - %s", 
                currentQuarter, timeStr, player.getNumber(), player.getName(), eventType);
        } else {
            // Team event - include quarter info
            eventDescription = String.format("Q%d %s - %s - %s", 
                currentQuarter, timeStr, teamName, eventType);
        }
        
        // Add to complete game events list
        gameEvents.add(0, eventDescription); // Add at beginning (most recent first)
        
        // Update recent events list (last 5 from gameEvents)
        updateRecentEventsList();
        
        // Update live feed display
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
        // Update recent events from gameEvents (last 5)
        recentEvents.clear();
        int count = Math.min(5, gameEvents.size());
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
    
    // Static method to get all game events (for LogActivity)
    public static List<String> getAllGameEvents() {
        return gameEvents;
    }
    
    // Static method to remove event (for LogActivity)
    public static boolean removeGameEvent(String event) {
        return gameEvents.remove(event);
    }
    
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
            case "FOUL": return btnFOUL;
            default: return null;
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
