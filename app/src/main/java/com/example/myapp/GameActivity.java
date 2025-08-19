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
    private Button btnStart, btnStop;
    
    // UI Components - Team Panels
    private TextView tvTeamAName, tvTeamBName;
    private LinearLayout llTeamAPlayers, llTeamBPlayers;
    private Button btnTeamATimeout, btnTeamASub, btnTeamBTimeout, btnTeamBSub;
    
    // UI Components - Event Panel (13+ buttons)
    private Button btn1P, btn2P, btn3P, btn1M, btn2M, btn3M;
    private Button btnOR, btnDR, btnAST, btnSTL, btnBLK, btnTO, btnFOUL, btnTIMEOUT;
    
    // UI Components - Quarter Management
    private Button btnQ1, btnQ2, btnQ3, btnQ4;
    
    // Game Management
    private Handler clockHandler = new Handler();
    private Runnable clockRunnable;
    private List<Button> teamAPlayerButtons, teamBPlayerButtons;
    private int teamAFouls = 0, teamBFouls = 0;
    
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
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        
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
    }
    
    private void initializeGameState() {
        // Initialize team names
        tvTeamAName.setText(teamAName);
        tvTeamBName.setText(teamBName);
        
        // Initialize player button lists
        teamAPlayerButtons = new ArrayList<>();
        teamBPlayerButtons = new ArrayList<>();
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
        // Clock control
        btnStart.setOnClickListener(v -> startClock());
        btnStop.setOnClickListener(v -> stopClock());
        
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
    }
    
    private void setupGameClock() {
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                if (isClockRunning && gameTimeSeconds > 0) {
                    gameTimeSeconds--;
                    updateGameClockDisplay();
                    clockHandler.postDelayed(this, 1000); // Update every second
                }
            }
        };
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
                quarterButtons[i].setBackgroundColor(Color.parseColor("#3498DB")); // Active (blue)
            } else {
                quarterButtons[i].setBackgroundColor(Color.parseColor("#BDC3C7")); // Inactive (grey)
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
        deselectAllPlayers();
        
        // Assist workflow for 2P and 3P
        if ("2P".equals(eventType) || "3P".equals(eventType)) {
            // For MVP, we'll skip the assist pop-up and just enable assist button
            // TODO: Implement assist pop-up or streamlined assist workflow
            Toast.makeText(this, "Assist available (tap AST button if applicable)", Toast.LENGTH_SHORT).show();
        }
        
        // TODO: Add to event log
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
        
        deselectAllPlayers();
        
        // Rebound workflow
        // For MVP, we'll skip the rebound pop-up and just show hint
        Toast.makeText(this, "Rebound available (tap OR/DR button for rebounder)", Toast.LENGTH_SHORT).show();
        
        // TODO: Add to event log, implement rebound pop-up
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
        deselectAllPlayers();
        
        // TODO: Add to event log
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
        
        deselectAllPlayers();
        
        // Steal workflow
        // For MVP, we'll skip the steal pop-up and just show hint
        Toast.makeText(this, "Steal available (tap STL button for defensive player)", Toast.LENGTH_SHORT).show();
        
        // TODO: Add to event log, implement steal pop-up
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
        
        deselectAllPlayers();
        
        // TODO: Add to event log
    }
    
    private void recordTeamTimeout(String team) {
        stopClock(); // Timeout stops the clock
        String teamName = "home".equals(team) ? teamAName : teamBName;
        Toast.makeText(this, teamName + " timeout called", Toast.LENGTH_SHORT).show();
        
        // TODO: Add to event log
    }
    
    private void startClock() {
        isClockRunning = true;
        btnStart.setBackgroundColor(Color.parseColor("#3498DB")); // Blue when active
        btnStop.setBackgroundColor(Color.parseColor("#BDC3C7")); // Grey when inactive
        clockHandler.post(clockRunnable);
        Toast.makeText(this, "Clock started", Toast.LENGTH_SHORT).show();
    }
    
    private void stopClock() {
        isClockRunning = false;
        btnStop.setBackgroundColor(Color.parseColor("#E74C3C")); // Red when active
        btnStart.setBackgroundColor(Color.parseColor("#BDC3C7")); // Grey when inactive
        clockHandler.removeCallbacks(clockRunnable);
        Toast.makeText(this, "Clock stopped", Toast.LENGTH_SHORT).show();
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
    
    // Visual Feedback - Flash event button blue for 3 seconds (per specification)
    private void flashEventButton(Button button) {
        if (button == null) return;
        
        // Store original colors
        int originalColor = Color.parseColor("#2ECC71"); // Default event button color
        
        // Flash blue
        button.setBackgroundColor(Color.parseColor("#3498DB")); // Blue
        
        // Reset after 3 seconds
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            button.setBackgroundColor(originalColor);
        }, 3000);
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
