package com.example.myapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapp.models.Player;
import com.example.myapp.models.Team;
import com.example.myapp.data.LeagueDataProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Game Activity - Live Basketball Statistics Recording (Frame 3)
 * THE CORE FUNCTIONALITY - Real-time game statistics recording interface
 */
public class GameActivity extends Activity {
    
    // Game State
    private int gameId;
    private String teamAName, teamBName;
    private List<Player> teamAPlayers, teamBPlayers;
    private int teamAScore = 0, teamBScore = 0;
    private int currentQuarter = 1;
    private int gameTimeSeconds = 600; // 10 minutes
    private boolean isClockRunning = false;
    private Player selectedPlayer = null;
    
    // UI Components - Top Panel
    private TextView tvScore, tvGameClock, tvCurrentQuarter, tvTeamFouls;
    private Button btnGameToggle;
    
    // UI Components - Team Panels
    private TextView tvTeamAName, tvTeamBName;
    private LinearLayout llTeamAPlayers, llTeamBPlayers;
    private Button btnTeamATimeout, btnTeamASub, btnTeamBTimeout, btnTeamBSub;
    
    // UI Components - Event Panel (13+ buttons)
    private Button btn1P, btn2P, btn3P, btn1M, btn2M, btn3M;
    private Button btnOR, btnDR, btnAST, btnSTL, btnBLK, btnTO, btnFOUL, btnTIMEOUT;
    
    // UI Components - Quarter Management
    private Button btnQ1, btnQ2, btnQ3, btnQ4;
    
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
        
        // Update initial display
        updateAllDisplays();
        
        Toast.makeText(this, "🏀 Live Game Recording Started!", Toast.LENGTH_LONG).show();
    }
    
    private void getGameDataFromIntent() {
        gameId = getIntent().getIntExtra("gameId", 1);
        teamAName = getIntent().getStringExtra("teamAName");
        teamBName = getIntent().getStringExtra("teamBName");
        
        // Get player data (for now use placeholder, will come from Frame 2 later)
        // TODO: Get actual selected players from Frame 2
        teamAPlayers = new ArrayList<>();
        teamBPlayers = new ArrayList<>();
        
        // Create sample players for testing
        createSamplePlayers();
    }
    
    private void createSamplePlayers() {
        // Create 5 sample players for each team for testing
        // TODO: Replace with actual players from Frame 2
        Team teamA = LeagueDataProvider.getTeamByName(teamAName);
        Team teamB = LeagueDataProvider.getTeamByName(teamBName);
        
        if (teamA != null && teamA.getPlayers().size() >= 5) {
            for (int i = 0; i < 5; i++) {
                teamAPlayers.add(teamA.getPlayers().get(i).toGamePlayer(gameId, "home"));
            }
        }
        
        if (teamB != null && teamB.getPlayers().size() >= 5) {
            for (int i = 0; i < 5; i++) {
                teamBPlayers.add(teamB.getPlayers().get(i).toGamePlayer(gameId, "away"));
            }
        }
    }
    
    private void initializeViews() {
        // Top panel components
        tvScore = findViewById(R.id.tvScore);
        tvGameClock = findViewById(R.id.tvGameClock);
        tvCurrentQuarter = findViewById(R.id.tvCurrentQuarter);
        tvTeamFouls = findViewById(R.id.tvTeamFouls);
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
        btnTIMEOUT = findViewById(R.id.btnTIMEOUT);
        
        // Quarter management components
        btnQ1 = findViewById(R.id.btnQ1);
        btnQ2 = findViewById(R.id.btnQ2);
        btnQ3 = findViewById(R.id.btnQ3);
        btnQ4 = findViewById(R.id.btnQ4);
        
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
        // Create Team A player buttons
        for (Player player : teamAPlayers) {
            Button playerBtn = createPlayerButton(player);
            teamAPlayerButtons.add(playerBtn);
            llTeamAPlayers.addView(playerBtn);
        }
        
        // Create Team B player buttons
        for (Player player : teamBPlayers) {
            Button playerBtn = createPlayerButton(player);
            teamBPlayerButtons.add(playerBtn);
            llTeamBPlayers.addView(playerBtn);
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
        
        // Quarter management
        btnQ1.setOnClickListener(v -> selectQuarter(1));
        btnQ2.setOnClickListener(v -> selectQuarter(2));
        btnQ3.setOnClickListener(v -> selectQuarter(3));
        btnQ4.setOnClickListener(v -> selectQuarter(4));
        
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
        
        // Team events
        btnTIMEOUT.setOnClickListener(v -> recordEvent("TIMEOUT", 0));
        btnTeamATimeout.setOnClickListener(v -> recordTeamTimeout("home"));
        btnTeamBTimeout.setOnClickListener(v -> recordTeamTimeout("away"));
        
        // Substitution (placeholder)
        btnTeamASub.setOnClickListener(v -> Toast.makeText(this, "Substitution for " + teamAName + " (coming soon)", Toast.LENGTH_SHORT).show());
        btnTeamBSub.setOnClickListener(v -> Toast.makeText(this, "Substitution for " + teamBName + " (coming soon)", Toast.LENGTH_SHORT).show());
        
        // View Log button
        btnViewLog.setOnClickListener(v -> openEventLog());
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
    private void selectQuarter(int quarter) {
        // Show quarter confirmation pop-up as per specification
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Start Quarter")
               .setMessage("Start Q" + quarter + "?")
               .setPositiveButton("Yes", (dialog, which) -> {
                   startQuarter(quarter);
               })
               .setNegativeButton("No", null)
               .show();
    }
    
    private void startQuarter(int quarter) {
        currentQuarter = quarter;
        gameTimeSeconds = 600; // Reset to 10 minutes
        teamAFouls = 0; // Reset team fouls for new quarter
        teamBFouls = 0;
        
        // Update quarter button colors (active = blue, inactive = grey)
        updateQuarterButtonColors();
        updateAllDisplays();
        
        Toast.makeText(this, "Q" + quarter + " started - 10:00 on clock", Toast.LENGTH_SHORT).show();
    }
    
    private void updateQuarterButtonColors() {
        Button[] quarterButtons = {btnQ1, btnQ2, btnQ3, btnQ4};
        for (int i = 0; i < 4; i++) {
            if (i + 1 == currentQuarter) {
                quarterButtons[i].setBackgroundColor(Color.parseColor("#34495E")); // Active (dark grey)
            } else {
                quarterButtons[i].setBackgroundColor(Color.parseColor("#BDC3C7")); // Inactive (light grey)
            }
        }
    }
    
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
        stopClock(); // Timeout stops the clock
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
                    clockHandler.postDelayed(this, 1000); // Update every second
                } else {
                    // Time's up - could add buzzer sound or notification
                    updateGameClockDisplay();
                    // Auto-pause when time reaches 0
                    pauseClock();
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
            btnGameToggle.setText("PAUSE GAME");
            btnGameToggle.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light)); // Blue (pleasant during gameplay)
            
            // Clock background: Green (running)
            tvGameClock.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            // Timer is paused - show START option
            btnGameToggle.setText("START GAME");
            btnGameToggle.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light)); // Green (ready to start)
            
            // Clock background: Yellow (paused)
            tvGameClock.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        }
    }
    
    private void updateAllDisplays() {
        updateScoreDisplay();
        updateGameClockDisplay();
        updateQuarterDisplay();
        updateTeamFoulsDisplay();
    }
    
    private void updateScoreDisplay() {
        tvScore.setText(String.format("%s %d - %s %d", teamAName, teamAScore, teamBName, teamBScore));
    }
    
    private void updateGameClockDisplay() {
        int minutes = gameTimeSeconds / 60;
        int seconds = gameTimeSeconds % 60;
        tvGameClock.setText(String.format("%d:%02d", minutes, seconds));
    }
    
    private void updateQuarterDisplay() {
        tvCurrentQuarter.setText("Q" + currentQuarter);
    }
    
    private void updateTeamFoulsDisplay() {
        // Color coding: red when ≥5 fouls (penalty situation)
        String teamAColor = teamAFouls >= 5 ? "#E74C3C" : "#FFFFFF"; // Red if ≥5, white otherwise
        String teamBColor = teamBFouls >= 5 ? "#E74C3C" : "#FFFFFF";
        
        // For now, just show in text (HTML color styling would need custom TextView)
        String foulsText = String.format("Team Fouls: A-%d  B-%d", teamAFouls, teamBFouls);
        if (teamAFouls >= 5 || teamBFouls >= 5) {
            foulsText += " ⚠️"; // Warning indicator
        }
        
        tvTeamFouls.setText(foulsText);
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
        } else if (button == btnTIMEOUT) {
            return Color.parseColor("#F39C12"); // Yellow for timeout
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
            // Player event
            eventDescription = String.format("%s - #%d %s - %s", 
                timeStr, player.getNumber(), player.getName(), eventType);
        } else {
            // Team event
            eventDescription = String.format("%s - %s - %s", 
                timeStr, teamName, eventType);
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
            case "TIMEOUT": return btnTIMEOUT;
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
