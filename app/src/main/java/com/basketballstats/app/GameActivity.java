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
import com.basketballstats.app.sync.SyncManager;
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
    
    // ✅ ARCHITECTURE NOTE: Two-model system for performance and data consistency
    // - TeamPlayer: SQLite-persisted team roster members (database IDs)
    // - Player: In-memory game session players (converted from TeamPlayer via toGamePlayer())
    // - Events use Player.getId() which returns the original TeamPlayer.id for database linkage
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
    
    // Derived game state (from currentGame)
    
    // Derived game state (from currentGame)
    private int gameId;
    private String teamAName;
    private String teamBName;
    private int currentQuarter;
    private int gameTimeSeconds;
    private boolean isClockRunning;
    private int teamAFouls;
    private int teamBFouls;
    
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
        
        // ✅ SUCCESS: UI updates work! Now let's debug the real scoring logic
        android.util.Log.d("GameActivity", "✅ UI SYSTEM VERIFIED: setText() calls work correctly!");
        android.util.Log.d("GameActivity", String.format("📱 Initial XML values: TeamA='%s', TeamB='%s'", 
            tvTeamAScore != null ? tvTeamAScore.getText().toString() : "NULL",
            tvTeamBScore != null ? tvTeamBScore.getText().toString() : "NULL"));
            
        // Check current database values
        if (currentGame != null) {
            android.util.Log.d("GameActivity", String.format("🏀 Database scores: HOME=%d, AWAY=%d", 
                currentGame.getHomeScore(), currentGame.getAwayScore()));
        }
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
                        currentGame.setStatus("game_in_progress"); // Updated for 3-state system
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
            
            // ✅ NEW: Load existing game players from database for state restoration
            loadGamePlayers();
            
            // Load existing game events from database
            loadGameEvents();
            
            // Update event sequence counter
            updateEventSequenceCounter();
            
            // ✅ FIX: Initialize derived game state from currentGame object
            if (currentGame != null) {
                this.gameId = currentGame.getId();
                this.currentQuarter = currentGame.getCurrentQuarter();
                this.gameTimeSeconds = currentGame.getGameClockSeconds();
                this.isClockRunning = currentGame.isClockRunning();
            }
            
            // Initialize team names from loaded team objects
            if (teamA != null) {
                this.teamAName = teamA.getName();
            }
            if (teamB != null) {
                this.teamBName = teamB.getName();
            }
            
            // ✅ NEW: Get navigation mode from MainActivity (status-aware navigation)
            String navigationMode = getIntent().getStringExtra("navigationMode");
            if (navigationMode == null) {
                navigationMode = "setup"; // Default fallback
            }
            
            // ✅ NEW: Determine setup mode based on game status and loaded players
            isInSetupMode = determineSetupMode(navigationMode, currentGame);
            
            // ✅ NEW: Enhanced status message with mode information
            String modeInfo = isInSetupMode ? "Setup Mode" : "Game Mode";
            String playerInfo = String.format("(TeamA: %d players, TeamB: %d players)", teamAPlayers.size(), teamBPlayers.size());
            Toast.makeText(this, "Loaded game: " + teamA.getName() + " vs " + teamB.getName() + " - " + modeInfo + " " + playerInfo, Toast.LENGTH_LONG).show();
            
            android.util.Log.d("GameActivity", String.format("Game initialized - ID: %d, Quarter: %d, Clock: %d, Mode: %s, Navigation: %s", 
                this.gameId, this.currentQuarter, this.gameTimeSeconds, modeInfo, navigationMode));
            
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
        
        // ✅ CRITICAL DEBUG: Check if TextViews were actually found
        android.util.Log.d("GameActivity", "🔍 FINDVIEWBYID RESULTS:");
        android.util.Log.d("GameActivity", String.format("tvTeamAScore = %s", tvTeamAScore != null ? "FOUND" : "NULL"));
        android.util.Log.d("GameActivity", String.format("tvTeamBScore = %s", tvTeamBScore != null ? "FOUND" : "NULL"));
        
        if (tvTeamAScore != null) {
            android.util.Log.d("GameActivity", String.format("tvTeamAScore current text: '%s'", tvTeamAScore.getText().toString()));
        }
        if (tvTeamBScore != null) {
            android.util.Log.d("GameActivity", String.format("tvTeamBScore current text: '%s'", tvTeamBScore.getText().toString()));
        }
        
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
        
        // ✅ FIX: Don't clear game events! They were just loaded from database in getGameDataFromIntent()
        // Events are already loaded via loadGameEvents() - keep them!
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
     * ✅ NEW: Load existing game players from database for state restoration
     * Converts GamePlayer database records back to in-memory Player objects
     */
    private void loadGamePlayers() {
        try {
            if (currentGame != null && currentGame.getId() > 0) {
                // Load GamePlayer records for this game
                List<GamePlayer> gamePlayers = GamePlayer.findByGameId(dbController.getDatabaseHelper(), currentGame.getId());
                
                // Convert to Player objects and populate team lists
                for (GamePlayer gamePlayer : gamePlayers) {
                    if (gamePlayer.getTeamPlayer() != null) {
                        // Convert GamePlayer back to Player for in-memory use
                        TeamPlayer teamPlayer = gamePlayer.getTeamPlayer();
                        Player player = teamPlayer.toGamePlayer(currentGame.getId(), gamePlayer.getTeamSide());
                        
                        // Restore game-specific state (fouls, etc.)
                        player.setPersonalFouls(gamePlayer.getPersonalFouls());
                        player.setOnCourt(gamePlayer.isOnCourt());
                        
                        // Add to appropriate team list
                        if ("home".equals(gamePlayer.getTeamSide())) {
                            teamAPlayers.add(player);
                        } else if ("away".equals(gamePlayer.getTeamSide())) {
                            teamBPlayers.add(player);
                        }
                    }
                }
                
                android.util.Log.d("GameActivity", String.format("Loaded %d game players from database (TeamA: %d, TeamB: %d)", 
                    gamePlayers.size(), teamAPlayers.size(), teamBPlayers.size()));
                
            } else {
                android.util.Log.d("GameActivity", "No existing game or gameId - starting with empty player lists");
            }
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error loading game players from database", e);
            // Keep empty lists as fallback
        }
    }
    
    /**
     * ✅ NEW: Determine setup mode based on navigation mode and game state
     * Implements the 3-state game management logic
     */
    private boolean determineSetupMode(String navigationMode, Game game) {
        try {
            switch (navigationMode) {
                case "setup":
                    // "not_started" games always go to Setup Mode
                    android.util.Log.d("GameActivity", "Navigation mode: SETUP - Starting new game");
                    return true;
                    
                case "resume":
                    // "game_in_progress" games go to Game Mode if players are loaded
                    boolean hasPlayers = (teamAPlayers.size() >= 5 && teamBPlayers.size() >= 5);
                    if (hasPlayers) {
                        android.util.Log.d("GameActivity", "Navigation mode: RESUME - Continuing game with existing players");
                        return false; // Game Mode
                    } else {
                        android.util.Log.w("GameActivity", "Navigation mode: RESUME - But no players found, forcing Setup Mode");
                        return true; // Setup Mode as fallback
                    }
                    
                case "review":
                    // "done" games go to Game Mode for review/editing (even if no players)
                    android.util.Log.d("GameActivity", "Navigation mode: REVIEW - Reviewing completed game");
                    return false; // Game Mode for editing
                    
                default:
                    // Unknown mode - default to setup
                    android.util.Log.w("GameActivity", "Unknown navigation mode: " + navigationMode + " - defaulting to Setup");
                    return true;
            }
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error determining setup mode", e);
            return true; // Safe fallback to Setup Mode
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
    
    // ========== STATUS TRANSITION METHODS ==========
    
    /**
     * ✅ NEW: Transition game from "not_started" to "game_in_progress"
     * Called when both teams select 5 players
     */
    private void transitionToGameInProgress() {
        try {
            if (currentGame != null && currentGame.isNotStarted()) {
                currentGame.setToGameInProgress();
                currentGame.save(dbController.getDatabaseHelper());
                
                android.util.Log.d("GameActivity", "✅ Status Transition: not_started → game_in_progress");
                
                // ✅ NEW: Save player selections to database for persistence
                savePlayerSelections();
                
            } else if (currentGame != null) {
                android.util.Log.d("GameActivity", "Status transition skipped - game already in progress or done");
            }
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error transitioning to game_in_progress", e);
        }
    }
    
    /**
     * ✅ NEW: Transition game from "game_in_progress" to "done"
     * Called when Q4 timer completes or manual end game
     */
    private void transitionToDone() {
        try {
            if (currentGame != null && currentGame.isGameInProgress()) {
                currentGame.setToDone();
                currentGame.save(dbController.getDatabaseHelper());
                
                android.util.Log.d("GameActivity", "✅ Status Transition: game_in_progress → done");
                Toast.makeText(this, "🏁 Game Complete!", Toast.LENGTH_LONG).show();
                
            } else if (currentGame != null) {
                android.util.Log.d("GameActivity", "Status transition skipped - game already done");
            }
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error transitioning to done", e);
        }
    }
    
    /**
     * ✅ NEW: Transition game from any status to "not_started"
     * Called when clearing all events (complete reset)
     */
    private void transitionToNotStarted() {
        try {
            if (currentGame != null) {
                currentGame.setToNotStarted();
                currentGame.save(dbController.getDatabaseHelper());
                
                android.util.Log.d("GameActivity", "✅ Status Transition: [any] → not_started (complete reset)");
                
                // Note: This method is called from enhanced clear log functionality
                // The actual reset logic (clear players, events, etc.) is handled there
                
            }
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error transitioning to not_started", e);
        }
    }
    
    /**
     * ✅ NEW: Save current player selections to game_players table
     * Called when transitioning to game_in_progress to persist lineup
     */
    private void savePlayerSelections() {
        try {
            if (currentGame == null || currentGame.getId() <= 0) {
                android.util.Log.w("GameActivity", "Cannot save player selections - no valid game");
                return;
            }
            
            // Clear existing game players for this game
            GamePlayer.deleteByGameId(dbController.getDatabaseHelper(), currentGame.getId());
            
            // Save Team A players (home side)
            for (Player player : teamAPlayers) {
                if (player.getOriginalTeamPlayerId() > 0) {
                    GamePlayer gamePlayer = new GamePlayer(
                        currentGame.getId(), 
                        player.getOriginalTeamPlayerId(), 
                        "home", 
                        true // isStarter - all initially selected players are starters
                    );
                    gamePlayer.setPersonalFouls(player.getPersonalFouls());
                    gamePlayer.save(dbController.getDatabaseHelper());
                }
            }
            
            // Save Team B players (away side)  
            for (Player player : teamBPlayers) {
                if (player.getOriginalTeamPlayerId() > 0) {
                    GamePlayer gamePlayer = new GamePlayer(
                        currentGame.getId(), 
                        player.getOriginalTeamPlayerId(), 
                        "away", 
                        true // isStarter - all initially selected players are starters
                    );
                    gamePlayer.setPersonalFouls(player.getPersonalFouls());
                    gamePlayer.save(dbController.getDatabaseHelper());
                }
            }
            
            android.util.Log.d("GameActivity", String.format("✅ Saved player selections: %d TeamA + %d TeamB players", 
                teamAPlayers.size(), teamBPlayers.size()));
            
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error saving player selections to database", e);
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
        // Create Event object for lineup change
        Event lineupEvent = new Event();
        lineupEvent.setGameId(gameId);
        lineupEvent.setEventType("LINEUP");
        lineupEvent.setTeamSide(currentModalTeamSide);
        lineupEvent.setQuarter(currentQuarter);
        lineupEvent.setGameTimeSeconds(gameTimeSeconds);
        lineupEvent.setEventSequence(gameEvents.size() + 1);
        gameEvents.add(lineupEvent);
        
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
                // Create Event object for substitution out
                Event outEventObj = new Event();
                outEventObj.setGameId(gameId);
                outEventObj.setEventType("SUB_OUT");
                outEventObj.setTeamSide(currentModalTeamSide);
                outEventObj.setQuarter(currentQuarter);
                outEventObj.setGameTimeSeconds(gameTimeSeconds);
                outEventObj.setEventSequence(gameEvents.size() + 1);
                gameEvents.add(outEventObj);
            }
        }
        
        // Find players coming in  
        for (Player newPlayer : newLineup) {
            if (!oldNumbers.contains(newPlayer.getNumber())) {
                String inEvent = String.format("Q%d %s - %s - IN: #%d %s", 
                                              currentQuarter, formatGameTime(gameTimeSeconds), 
                                              teamName, newPlayer.getNumber(), newPlayer.getName());
                // Create Event object for substitution in
                Event inEventObj = new Event();
                inEventObj.setGameId(gameId);
                inEventObj.setEventType("SUB_IN");
                inEventObj.setTeamSide(currentModalTeamSide);
                inEventObj.setQuarter(currentQuarter);
                inEventObj.setGameTimeSeconds(gameTimeSeconds);
                inEventObj.setEventSequence(gameEvents.size() + 2);
                gameEvents.add(inEventObj);
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
            // Create Event object for substitution
            Event eventObj = new Event();
            eventObj.setGameId(gameId);
            eventObj.setEventType("SUBSTITUTION");
            eventObj.setTeamSide(currentModalTeamSide);
            eventObj.setQuarter(currentQuarter);
            eventObj.setGameTimeSeconds(gameTimeSeconds);
            eventObj.setEventSequence(gameEvents.size() + 1);
            gameEvents.add(eventObj);
            
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
            
            // ✅ NEW: Transition to "game_in_progress" when both teams have 5 players
            transitionToGameInProgress();
            
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
        
        // ✅ FIX: Save game state to database when quarter is manually changed
        // This ensures currentGame.getCurrentQuarter() stays in sync with currentQuarter
        saveGameStateToDatabase();
        
        // Update displays (including context-aware button text)
        updateAllDisplays();
        
        // Show helpful message about quarter lineup changes
        if (!isInSetupMode && (teamAPlayers.size() == 5 && teamBPlayers.size() == 5)) {
            Toast.makeText(this, "Quarter " + quarter + " ready - Use 'Quarter Lineup' to modify teams", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Quarter " + quarter + " selected - Clock reset to 10:00", Toast.LENGTH_SHORT).show();
        }
        
        android.util.Log.d("GameActivity", String.format("📅 Quarter manually changed to Q%d, game state saved", quarter));
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
            
            // ✅ DEBUG: Log scoring details to identify the issue
            android.util.Log.d("GameActivity", String.format("🏀 SCORING EVENT: Player=%s, Team=%s, Points=%d", 
                selectedPlayer.getName(), playerTeam, points));
            android.util.Log.d("GameActivity", String.format("📊 BEFORE: HomeScore=%d, AwayScore=%d", 
                currentGame.getHomeScore(), currentGame.getAwayScore()));
            
            if ("home".equals(playerTeam)) {
                int oldScore = currentGame.getHomeScore();
                int newHomeScore = oldScore + points;
                currentGame.setHomeScore(newHomeScore);
                android.util.Log.d("GameActivity", String.format("✅ Updated HOME score: %d + %d = %d", 
                    oldScore, points, newHomeScore));
            } else {
                int oldScore = currentGame.getAwayScore(); 
                int newAwayScore = oldScore + points;
                currentGame.setAwayScore(newAwayScore);
                android.util.Log.d("GameActivity", String.format("✅ Updated AWAY score: %d + %d = %d", 
                    oldScore, points, newAwayScore));
            }
            
            android.util.Log.d("GameActivity", String.format("📊 AFTER SCORE UPDATE: HomeScore=%d, AwayScore=%d", 
                currentGame.getHomeScore(), currentGame.getAwayScore()));
            android.util.Log.d("GameActivity", "📊 About to call saveGameStateToDatabase()...");
            
            // ✅ FIX: Use actual team name instead of "home"/"away"
            String actualTeamName = "home".equals(playerTeam) ? teamAName : teamBName;
            Event event = new Event(currentGame.getId(), selectedPlayer.getId(), actualTeamName, 
                                   currentQuarter, gameTimeSeconds, eventType);
            event.setEventSequence(eventSequenceCounter++);
            event.save(dbController.getDatabaseHelper());
            
            // Add to local event list
            gameEvents.add(event);
            
            // ✅ FIX: Use comprehensive save method to sync all state
            saveGameStateToDatabase();
            
            // ✅ VERIFICATION: Check if scores were preserved after save
            android.util.Log.d("GameActivity", String.format("📊 AFTER SAVE: HomeScore=%d, AwayScore=%d", 
                currentGame.getHomeScore(), currentGame.getAwayScore()));
            
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
        
        try {
            // ✅ FIX: Save miss event to SQLite database
            String playerTeam = selectedPlayer.getTeam();
            
            // ✅ FIX: Use actual team name instead of "home"/"away"
            String actualTeamName = "home".equals(playerTeam) ? teamAName : teamBName;
            Event event = new Event(currentGame.getId(), selectedPlayer.getId(), actualTeamName, 
                                   currentQuarter, gameTimeSeconds, eventType);
            event.setEventSequence(eventSequenceCounter++);
            event.save(dbController.getDatabaseHelper());
            
            // Add to local event list
            gameEvents.add(event);
            
            // Visual feedback - flash button blue
        flashEventButton(getEventButton(eventType));
        
        // Show feedback
        Toast.makeText(this, String.format("%s recorded for %s", 
            eventType, selectedPlayer.getName()), Toast.LENGTH_SHORT).show();
        
            // Update displays and deselect
            addToLiveEventFeedSQLite(event);
        deselectAllPlayers();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error saving miss event: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("GameActivity", "Error recording miss event", e);
        }
        
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
        
        try {
            // ✅ FIX: Save foul event to SQLite database
            String playerTeam = selectedPlayer.getTeam();
            
            // ✅ FIX: Use actual team name instead of "home"/"away"
            String actualTeamName = "home".equals(playerTeam) ? teamAName : teamBName;
            Event event = new Event(currentGame.getId(), selectedPlayer.getId(), actualTeamName, 
                                   currentQuarter, gameTimeSeconds, eventType);
            event.setEventSequence(eventSequenceCounter++);
            event.save(dbController.getDatabaseHelper());
            
            // Add to local event list
            gameEvents.add(event);
        
        // Increment personal fouls
        selectedPlayer.setPersonalFouls(selectedPlayer.getPersonalFouls() + 1);
        
        // Increment team fouls
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
            addToLiveEventFeedSQLite(event);
        deselectAllPlayers();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error saving foul event: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("GameActivity", "Error recording foul event", e);
        }
        
        // Single-event safety: Reset override after event recorded
        checkAndResetSingleEventOverride();
    }
    
    // Turnover Events (with steal workflow)
    private void recordTurnoverEvent(String eventType) {
        if (selectedPlayer == null) {
            Toast.makeText(this, "Please select a player first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // ✅ FIX: Save turnover event to SQLite database
            String playerTeam = selectedPlayer.getTeam();
            
            // ✅ FIX: Use actual team name instead of "home"/"away"
            String actualTeamName = "home".equals(playerTeam) ? teamAName : teamBName;
            Event event = new Event(currentGame.getId(), selectedPlayer.getId(), actualTeamName, 
                                   currentQuarter, gameTimeSeconds, eventType);
            event.setEventSequence(eventSequenceCounter++);
            event.save(dbController.getDatabaseHelper());
            
            // Add to local event list
            gameEvents.add(event);
        
        // Visual feedback
        flashEventButton(btnTO);
        
            // Show feedback
        Toast.makeText(this, String.format("Turnover recorded for %s", selectedPlayer.getName()), Toast.LENGTH_SHORT).show();
        
            // Update displays and deselect
            addToLiveEventFeedSQLite(event);
        deselectAllPlayers();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error saving turnover event: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("GameActivity", "Error recording turnover event", e);
        }
        
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
        
        try {
            // ✅ FIX: Save event to SQLite database
            String playerTeam = selectedPlayer.getTeam();
            
            // ✅ FIX: Use actual team name instead of "home"/"away"
            String actualTeamName = "home".equals(playerTeam) ? teamAName : teamBName;
            Event event = new Event(currentGame.getId(), selectedPlayer.getId(), actualTeamName, 
                                   currentQuarter, gameTimeSeconds, eventType);
            event.setEventSequence(eventSequenceCounter++);
            event.setPointsValue(points);
            event.save(dbController.getDatabaseHelper());
            
            // Add to local event list
            gameEvents.add(event);
        
        // Visual feedback
        flashEventButton(getEventButton(eventType));
        
        // Show feedback
        Toast.makeText(this, String.format("%s recorded for %s", 
            eventType, selectedPlayer.getName()), Toast.LENGTH_SHORT).show();
        
            // Update displays and deselect
            addToLiveEventFeedSQLite(event);
        deselectAllPlayers();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error saving event: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("GameActivity", "Error recording " + eventType + " event", e);
        }
        
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
        
        try {
            // ✅ FIX: Get the most recent event (last in chronological list)
            Event lastEvent = gameEvents.get(gameEvents.size() - 1);
            
            // ✅ FIX: Delete from SQLite database (same logic as LogActivity delete)
            boolean deleted = lastEvent.delete(dbController.getDatabaseHelper());
            
            if (!deleted) {
                Toast.makeText(this, "Error: Could not undo event", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // ✅ ENHANCED: Reverse the game state effects and recalculate scores
            reverseEventEffects(lastEvent);
            
            // Remove from local events list
            gameEvents.remove(gameEvents.size() - 1);
            
            // ✅ NEW: Recalculate scores from all remaining events to ensure accuracy
            recalculateScoresFromEvents();
            
            // Save updated game state to database
            saveGameStateToDatabase();
            
            // Update displays
            updateRecentEventsFeed();
        updateLiveEventFeedDisplay();
            updateAllDisplays(); // Update scores, fouls, etc.
            
            // Show confirmation with event details
            Toast.makeText(this, String.format("✅ Undone: %s", lastEvent.getEventType()), Toast.LENGTH_SHORT).show();
            
            android.util.Log.d("GameActivity", String.format("🔄 Undone event: %s (ID: %d)", 
                lastEvent.getEventType(), lastEvent.getId()));
            
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "❌ Error undoing last event", e);
            Toast.makeText(this, "Error: Could not undo event - " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * ✅ ENHANCED: Reverse the effects of an event on game state
     * Updated to use actual team names instead of "home"/"away"
     */
    private void reverseEventEffects(Event event) {
        String eventType = event.getEventType();
        int playerId = event.getPlayerId();
        
        // Find the player in current lineups to reverse personal stats
        Player affectedPlayer = findPlayerById(playerId);
        
        switch (eventType) {
            case "1P":
                // Reverse 1-point score
                reverseTeamScore(event.getTeamSide(), 1);
                break;
                
            case "2P":
                // Reverse 2-point score
                reverseTeamScore(event.getTeamSide(), 2);
                break;
                
            case "3P":
                // Reverse 3-point score
                reverseTeamScore(event.getTeamSide(), 3);
                break;
                
            case "FOUL":
                // Reverse personal and team fouls
                if (affectedPlayer != null) {
                    affectedPlayer.setPersonalFouls(Math.max(0, affectedPlayer.getPersonalFouls() - 1));
                    updatePlayerButtonText(); // Refresh foul displays
                }
                // Reverse team fouls using actual team names
                if (teamAName.equals(event.getTeamSide())) {
                    teamAFouls = Math.max(0, teamAFouls - 1);
                } else if (teamBName.equals(event.getTeamSide())) {
                    teamBFouls = Math.max(0, teamBFouls - 1);
                }
                updateTeamFoulsDisplay();
                break;
                
            // Other event types (1M, 2M, 3M, OR, DR, AST, STL, BLK, TO, TIMEOUT) 
            // don't affect scores or fouls, so no reversal needed
            default:
                // Non-scoring, non-foul events don't need game state reversal
                break;
        }
        
        android.util.Log.d("GameActivity", String.format("Reversed effects for %s event by team %s", 
            eventType, event.getTeamSide()));
    }
    
    /**
     * ✅ NEW: Find player by ID in current lineups
     */
    private Player findPlayerById(int playerId) {
        // Search Team A players
        for (Player player : teamAPlayers) {
            if (player.getId() == playerId) {
                return player;
            }
        }
        
        // Search Team B players
        for (Player player : teamBPlayers) {
            if (player.getId() == playerId) {
                return player;
            }
        }
        
        return null; // Player not in current lineups (substituted out)
    }
    
    /**
     * ✅ ENHANCED: Update team score by delta (positive or negative)
     * Updated to use actual team names instead of "home"/"away"
     */
    private void updateTeamScore(String teamSide, int scoreDelta) {
        if (teamAName.equals(teamSide)) {
            // Update Team A score
            String currentScoreText = tvTeamAScore.getText().toString();
            int currentScore = extractScoreFromDisplay(currentScoreText);
            int newScore = Math.max(0, currentScore + scoreDelta);
            tvTeamAScore.setText(String.valueOf(newScore));
            // Update database
            if (currentGame != null) {
                currentGame.setHomeScore(newScore);
            }
        } else if (teamBName.equals(teamSide)) {
            // Update Team B score
            String currentScoreText = tvTeamBScore.getText().toString();
            int currentScore = extractScoreFromDisplay(currentScoreText);
            int newScore = Math.max(0, currentScore + scoreDelta);
            tvTeamBScore.setText(String.valueOf(newScore));
            // Update database
            if (currentGame != null) {
                currentGame.setAwayScore(newScore);
            }
        }
    }
    
    /**
     * ✅ NEW: Reverse team score (subtract points)
     */
    private void reverseTeamScore(String teamName, int points) {
        if (teamAName.equals(teamName)) {
            // Reverse Team A score
            String currentScoreText = tvTeamAScore.getText().toString();
            int currentScore = extractScoreFromDisplay(currentScoreText);
            int newScore = Math.max(0, currentScore - points);
            tvTeamAScore.setText(String.valueOf(newScore));
            // Update database
            if (currentGame != null) {
                currentGame.setHomeScore(newScore);
            }
        } else if (teamBName.equals(teamName)) {
            // Reverse Team B score
            String currentScoreText = tvTeamBScore.getText().toString();
            int currentScore = extractScoreFromDisplay(currentScoreText);
            int newScore = Math.max(0, currentScore - points);
            tvTeamBScore.setText(String.valueOf(newScore));
            // Update database
            if (currentGame != null) {
                currentGame.setAwayScore(newScore);
            }
        }
    }
    
    /**
     * ✅ FIXED: Recalculate scores from all scoring events in database
     * Used when clearing events or deleting individual events
     * 
     * CRITICAL FIX: Properly map team names to home/away based on database assignment
     */
    private void recalculateScoresFromEvents() {
        if (currentGame == null) return;
        
        try {
            // Get all scoring events for this game
            List<Event> allEvents = Event.findByGameId(dbController.getDatabaseHelper(), currentGame.getId());
            
            int homeScore = 0;
            int awayScore = 0;
            
            // ✅ CRITICAL FIX: Get actual home/away team names from database
            String homeTeamName = currentGame.getHomeTeam() != null ? currentGame.getHomeTeam().getName() : teamAName;
            String awayTeamName = currentGame.getAwayTeam() != null ? currentGame.getAwayTeam().getName() : teamBName;
            
            // Calculate scores from all scoring events using actual home/away mapping
            for (Event event : allEvents) {
                int points = 0;
                switch (event.getEventType()) {
                    case "1P": points = 1; break;
                    case "2P": points = 2; break;
                    case "3P": points = 3; break;
                    default: continue; // Skip non-scoring events
                }
                
                // ✅ FIXED: Use actual home/away team names instead of assuming teamA=home
                if (homeTeamName.equals(event.getTeamSide())) {
                    homeScore += points;
                } else if (awayTeamName.equals(event.getTeamSide())) {
                    awayScore += points;
                }
            }
            
            // ✅ FIXED: Update UI based on actual home/away assignment
            // teamA is always displayed on left, teamB on right, but they might not be home/away
            if (teamAName.equals(homeTeamName)) {
                // Team A is home team
                tvTeamAScore.setText(String.valueOf(homeScore));
                tvTeamBScore.setText(String.valueOf(awayScore));
            } else {
                // Team A is away team (Team B is home)
                tvTeamAScore.setText(String.valueOf(awayScore));
                tvTeamBScore.setText(String.valueOf(homeScore));
            }
            
            // ✅ FIXED: Always update database with correct home/away scores
            currentGame.setHomeScore(homeScore);
            currentGame.setAwayScore(awayScore);
            
            android.util.Log.d("GameActivity", String.format("🔄 Recalculated scores: HOME[%s]=%d AWAY[%s]=%d", 
                homeTeamName, homeScore, awayTeamName, awayScore));
                
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error recalculating scores from events", e);
        }
    }
    
    /**
     * ✅ NEW: Extract numeric score from display text like "Lakers 45"
     */
    private int extractScoreFromDisplay(String displayText) {
        try {
            // Look for last number in the string
            String[] parts = displayText.trim().split("\\s+");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (Exception e) {
            android.util.Log.w("GameActivity", "Could not extract score from: " + displayText);
            return 0;
        }
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
                    
                    // ✅ FIX: Save game state every 10 seconds to avoid too frequent DB writes
                    if (gameTimeSeconds % 10 == 0) {
                        saveGameStateToDatabase();
                    }
                    
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
                        // ✅ NEW: Transition to "done" status when Q4 timer reaches 0:00
                        transitionToDone();
                        
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
        
        // ✅ FIX: Save clock state to database immediately
        saveGameStateToDatabase();
        
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
        
        // ✅ FIX: Save clock state to database immediately
        saveGameStateToDatabase();
        
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
        // ✅ FIXED: Update team scores for blue strip layout using correct home/away mapping
        android.util.Log.d("GameActivity", "🖥️ updateScoreDisplay() CALLED!");
        
        if (currentGame != null) {
            // Get actual home/away team names from database
            String homeTeamName = currentGame.getHomeTeam() != null ? currentGame.getHomeTeam().getName() : teamAName;
            String awayTeamName = currentGame.getAwayTeam() != null ? currentGame.getAwayTeam().getName() : teamBName;
            
            // ✅ DEBUG: Log display update details
            android.util.Log.d("GameActivity", String.format("🖥️ UPDATE DISPLAY: DB HomeScore=%d, AwayScore=%d", 
                currentGame.getHomeScore(), currentGame.getAwayScore()));
            android.util.Log.d("GameActivity", String.format("🏠 HOME team: %s, AWAY team: %s", homeTeamName, awayTeamName));
            android.util.Log.d("GameActivity", String.format("📱 UI teamA: %s, teamB: %s", teamAName, teamBName));
            
            // Map scores correctly based on actual home/away assignment
            if (teamAName.equals(homeTeamName)) {
                // Team A is home team
            tvTeamAScore.setText(String.valueOf(currentGame.getHomeScore()));
            tvTeamBScore.setText(String.valueOf(currentGame.getAwayScore()));
                android.util.Log.d("GameActivity", String.format("✅ UI UPDATE: TeamA(HOME)=%d, TeamB(AWAY)=%d", 
                    currentGame.getHomeScore(), currentGame.getAwayScore()));
            } else {
                // Team A is away team (Team B is home)
                tvTeamAScore.setText(String.valueOf(currentGame.getAwayScore()));
                tvTeamBScore.setText(String.valueOf(currentGame.getHomeScore()));
                android.util.Log.d("GameActivity", String.format("✅ UI UPDATE: TeamA(AWAY)=%d, TeamB(HOME)=%d", 
                    currentGame.getAwayScore(), currentGame.getHomeScore()));
            }
            
            // ✅ DEBUG: Verify UI actually updated after setText calls
            android.util.Log.d("GameActivity", String.format("🔍 POST-UPDATE: UI shows TeamA='%s', TeamB='%s'", 
                tvTeamAScore.getText().toString(), tvTeamBScore.getText().toString()));
                
        } else {
            android.util.Log.w("GameActivity", "❌ Cannot update score display: currentGame is null");
        }
        
        // ✅ DEBUG: The UI updates work! Now check if database scores are correct
        android.util.Log.d("GameActivity", String.format("🔧 REAL SCORES: currentGame.getHomeScore()=%d, currentGame.getAwayScore()=%d", 
            currentGame.getHomeScore(), currentGame.getAwayScore()));
        android.util.Log.d("GameActivity", String.format("🔧 TEAM MAPPING: teamAName='%s', homeTeamName='%s' (match=%b)", 
            teamAName, 
            currentGame.getHomeTeam() != null ? currentGame.getHomeTeam().getName() : "null",
            teamAName.equals(currentGame.getHomeTeam() != null ? currentGame.getHomeTeam().getName() : teamAName)));
            
        // ✅ SHOW REAL SCORES: Now that race condition is fixed, display actual scores
        if (tvTeamAScore != null && tvTeamBScore != null) {
            // Get actual home/away team names from database for proper mapping
            String homeTeamName = currentGame.getHomeTeam() != null ? currentGame.getHomeTeam().getName() : teamAName;
            
            if (teamAName.equals(homeTeamName)) {
                // Team A is home team - show home score on left, away on right
                tvTeamAScore.setText(String.valueOf(currentGame.getHomeScore()));
                tvTeamBScore.setText(String.valueOf(currentGame.getAwayScore()));
            } else {
                // Team A is away team - show away score on left, home on right
                tvTeamAScore.setText(String.valueOf(currentGame.getAwayScore()));
                tvTeamBScore.setText(String.valueOf(currentGame.getHomeScore()));
            }
            
            android.util.Log.d("GameActivity", String.format("✅ FINAL UI: TeamA='%s', TeamB='%s'", 
                tvTeamAScore.getText().toString(), tvTeamBScore.getText().toString()));
        }
    }
    
    private void updateGameClockDisplay() {
        // ✅ FIX: Use local gameTimeSeconds (live timer value) instead of currentGame.getGameClockSeconds() (database value)
        // This fixes the 1-second lag issue where display was behind the actual timer
        int minutes = gameTimeSeconds / 60;
        int seconds = gameTimeSeconds % 60;
            tvGameClock.setText(String.format("%d:%02d", minutes, seconds));
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
     * ✅ ENHANCED: Add SQLite Event to live feed with immediate database refresh
     */
    private void addToLiveEventFeedSQLite(Event event) {
        android.util.Log.d("GameActivity", String.format("🔄 LIVE FEED UPDATE: Adding event %s for %s", 
            event.getEventType(), event.getPlayer() != null ? event.getPlayer().getName() : event.getTeamSide()));
        
        // ✅ CRITICAL: Update recent events directly from database for immediate consistency
        updateRecentEventsFeed();
        
        android.util.Log.d("GameActivity", "✅ Live feed update completed");
    }
    
    /**
     * ✅ FIXED: Update recent events directly from database for immediate, accurate updates
     * This fixes the delay issue where in-memory gameEvents list was inconsistent with database ordering
     */
    private void updateRecentEventsFeed() {
        recentEvents.clear();
        
        try {
            // ✅ CRITICAL FIX: Get fresh events directly from database instead of stale in-memory list
            List<Event> freshEvents = Event.findByGameId(dbController.getDatabaseHelper(), currentGame.getId());
            
            android.util.Log.d("GameActivity", String.format("🔄 Live feed: Loading %d fresh events from database", freshEvents.size()));
            
            int count = Math.min(4, freshEvents.size());
            
            // Database returns events ordered DESC (newest first), so take first 4 events
            for (int i = 0; i < count; i++) {
                Event event = freshEvents.get(i);
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
                
                recentEvents.add(eventDescription); // Add in order (newest first)
            }
            
            android.util.Log.d("GameActivity", String.format("✅ Live feed: Updated with %d recent events", recentEvents.size()));
            
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "❌ Error updating live event feed from database", e);
            // Fallback to in-memory list if database query fails
            int count = Math.min(4, gameEvents.size());
            for (int i = 0; i < count; i++) {
                recentEvents.add(gameEvents.get(i).toString());
            }
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
        
        // ✅ FIX: Use currentGame object data instead of uninitialized fields
        if (currentGame != null) {
            intent.putExtra("gameId", currentGame.getId());
            intent.putExtra("teamAName", (teamA != null) ? teamA.getName() : "Team A");
            intent.putExtra("teamBName", (teamB != null) ? teamB.getName() : "Team B");
            
            android.util.Log.d("GameActivity", String.format("Opening event log for game %d: %s vs %s", 
                currentGame.getId(), teamA.getName(), teamB.getName()));
        } else {
            Toast.makeText(this, "Error: No game data available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        startActivity(intent);
    }
    
    private void updateRecentEventsList() {
        // ✅ FIX: Update recent events from gameEvents (last 4 instead of 2)
        recentEvents.clear();
        int count = Math.min(4, gameEvents.size());
        for (int i = 0; i < count; i++) {
            recentEvents.add(gameEvents.get(i).toString());
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("GameActivity", "🔄 onResume() CALLED - Reloading game data from database");
        
        // ✅ CRITICAL FIX: Reload current game from database in case scores were modified in LogActivity
        reloadGameFromDatabase();
        
        // ✅ FIX: Reload events from database in case they were modified in LogActivity
        loadGameEvents();
        
        // ✅ NEW: Reload player selections in case game was reset to "not_started"
        reloadGamePlayers();
        
        // ✅ NEW: Update setup mode based on current game status and loaded players
        updateSetupModeAfterReload();
        
        android.util.Log.d("GameActivity", "🔄 onResume() calling updateAllDisplays()...");
        updateAllDisplays();
        
        android.util.Log.d("GameActivity", String.format("🔄 onResume() FINISHED - Current scores: TeamA='%s', TeamB='%s'", 
            tvTeamAScore != null ? tvTeamAScore.getText().toString() : "NULL",
            tvTeamBScore != null ? tvTeamBScore.getText().toString() : "NULL"));
    }
    
    /**
     * ✅ NEW: Reload current game from database to get updated scores/state
     * Called in onResume() to refresh game state after returning from LogActivity
     */
    private void reloadGameFromDatabase() {
        if (currentGame == null || dbController == null) return;
        
        try {
            // Reload game from database using current game ID
            Game refreshedGame = Game.findById(dbController.getDatabaseHelper(), currentGame.getId());
            
            if (refreshedGame != null) {
                // Load teams with rosters
                refreshedGame.loadTeams(dbController.getDatabaseHelper());
                
                // Check if scores changed
                boolean scoresChanged = (currentGame.getHomeScore() != refreshedGame.getHomeScore() || 
                                       currentGame.getAwayScore() != refreshedGame.getAwayScore());
                
                if (scoresChanged) {
                    android.util.Log.d("GameActivity", String.format("🔄 SCORES UPDATED: OLD[%d-%d] → NEW[%d-%d]", 
                        currentGame.getHomeScore(), currentGame.getAwayScore(),
                        refreshedGame.getHomeScore(), refreshedGame.getAwayScore()));
                }
                
                // Update the current game object with fresh data
                currentGame = refreshedGame;
                
                // Update derived state
                this.currentQuarter = currentGame.getCurrentQuarter();
                this.gameTimeSeconds = currentGame.getGameClockSeconds();
                this.isClockRunning = currentGame.isClockRunning();
                
                android.util.Log.d("GameActivity", String.format("✅ Reloaded game: Q%d, Clock:%d, Scores:[%d-%d]", 
                    currentQuarter, gameTimeSeconds, currentGame.getHomeScore(), currentGame.getAwayScore()));
                
            } else {
                android.util.Log.w("GameActivity", "❌ Could not reload game from database");
            }
            
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "❌ Error reloading game from database", e);
        }
    }
    
    /**
     * ✅ NEW: Reload game players from database for state restoration
     * Called in onResume() to refresh player lineups after returning from LogActivity
     */
    private void reloadGamePlayers() {
        try {
            // Clear current player lists
            teamAPlayers.clear();
            teamBPlayers.clear();
            
            // Reload players from database
            loadGamePlayers();
            
            // Recreate player buttons with updated lineups
            createPlayerButtons();
            
            android.util.Log.d("GameActivity", String.format("🔄 Reloaded players: TeamA=%d, TeamB=%d", 
                teamAPlayers.size(), teamBPlayers.size()));
            
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "❌ Error reloading game players", e);
        }
    }
    
    /**
     * ✅ NEW: Update setup mode based on current game status and loaded players
     * Called in onResume() to ensure correct mode after potential status changes
     */
    private void updateSetupModeAfterReload() {
        try {
            if (currentGame != null) {
                String currentStatus = currentGame.getStatus();
                boolean hasPlayers = (teamAPlayers.size() >= 5 && teamBPlayers.size() >= 5);
                
                // Determine setup mode based on status and players
                boolean shouldBeInSetupMode;
                
                if (currentGame.isNotStarted()) {
                    // "not_started" games should be in Setup Mode
                    shouldBeInSetupMode = true;
                } else if (currentGame.isGameInProgress() || currentGame.isDone()) {
                    // "game_in_progress" and "done" games should be in Game Mode if they have players
                    shouldBeInSetupMode = !hasPlayers;
                } else {
                    // Unknown status - default to Setup Mode
                    shouldBeInSetupMode = true;
                }
                
                if (isInSetupMode != shouldBeInSetupMode) {
                    isInSetupMode = shouldBeInSetupMode;
                    android.util.Log.d("GameActivity", String.format("🔄 Mode updated: %s (Status: %s, Players: %s)", 
                        isInSetupMode ? "SETUP" : "GAME", currentStatus, hasPlayers ? "LOADED" : "EMPTY"));
                }
                
                // Update UI accordingly
                checkIfGameReady();
                
            } else {
                android.util.Log.w("GameActivity", "Cannot update setup mode - no current game");
            }
            
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "❌ Error updating setup mode after reload", e);
        }
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
    
    /**
     * ✅ FIXED: Save current game state (clock, quarter, scores) to database
     * Called when clock starts/stops or changes quarter
     * CRITICAL FIX: Use proper home/away mapping for scores
     */
    private void saveGameStateToDatabase() {
        if (currentGame == null) return;
        
        try {
            // Update game object with current state
            currentGame.setCurrentQuarter(currentQuarter);
            currentGame.setGameClockSeconds(gameTimeSeconds);
            currentGame.setClockRunning(isClockRunning);
            
            // ✅ CRITICAL FIX: DON'T overwrite scores from UI - they're already set correctly by event recording!
            // The scores were already updated correctly in recordScoringEvent() via currentGame.setHomeScore()
            // Reading from UI and overwriting causes a race condition where stale UI values destroy correct scores
            android.util.Log.d("GameActivity", String.format("💾 PRESERVING SCORES: HOME=%d, AWAY=%d (already set by event recording)", 
                currentGame.getHomeScore(), currentGame.getAwayScore()));
            
            // Save to database
            long result = currentGame.save(dbController.getDatabaseHelper());
            
            if (result > 0) {
                android.util.Log.d("GameActivity", String.format("💾 Saved game state - Q%d %s Clock:%b", 
                    currentQuarter, formatTime(gameTimeSeconds), isClockRunning));
            } else {
                android.util.Log.w("GameActivity", "Failed to save game state to database");
            }
            
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error saving game state to database", e);
        }
    }
    
    /**
     * Helper method to format time in MM:SS format
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
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
    
    /**
     * Update derived game state from currentGame
     */
    private void updateDerivedGameState() {
        if (currentGame == null) return;
        
        gameId = currentGame.getId();
        teamAName = teamA != null ? teamA.getName() : "Team A";
        teamBName = teamB != null ? teamB.getName() : "Team B";
        currentQuarter = currentGame.getCurrentQuarter();
        gameTimeSeconds = currentGame.getGameClockSeconds();
        isClockRunning = currentGame.isClockRunning();
        
        // Calculate team fouls (simplified for now)
        teamAFouls = 0; // TODO: Query from database
        teamBFouls = 0; // TODO: Query from database
    }
    
    /**
     * ✅ ENHANCED: Initialize game clock state - properly handle new vs resumed games
     * NEW games: Clock starts at 10:00 and waits for manual start
     * RESUMED games: Restore saved clock state and resume if was running
     */
    private void setupGameClock() {
        // Determine if this is a new game (no events recorded yet)
        boolean isNewGame = gameEvents.isEmpty();
        
        // Initialize game clock defaults if not loaded from database
        if (currentQuarter <= 0) {
            currentQuarter = 1;
        }
        if (gameTimeSeconds <= 0) {
            gameTimeSeconds = 600; // 10:00 default
        }
        
        // ✅ NEW GAME LOGIC: Always start fresh for new games
        if (isNewGame) {
            currentQuarter = 1;
            gameTimeSeconds = 600; // Always start at 10:00
            isClockRunning = false; // Never auto-start for new games
            android.util.Log.d("GameActivity", "🆕 New game detected - Clock set to 10:00, waiting for manual start");
        }
        
        android.util.Log.d("GameActivity", String.format("⏰ setupGameClock - Quarter: %d, Time: %d, Running: %b, IsNew: %b", 
            currentQuarter, gameTimeSeconds, isClockRunning, isNewGame));
        
        // Update clock display immediately
        updateGameClockDisplay();
        
        // Only resume clock for existing games that were running
        if (isClockRunning && !isNewGame) {
            android.util.Log.d("GameActivity", "🔄 Resuming clock from saved state");
            startClock();
        }
    }
}
