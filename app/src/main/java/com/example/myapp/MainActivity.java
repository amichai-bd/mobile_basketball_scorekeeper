package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    
    // UI Components
    private TextView quarterDisplay;
    private TextView timeDisplay;
    private TextView homeScore;
    private TextView guestScore;
    private Button btnStartStop;
    private Button btnNextQuarter;
    private Button btnViewLog;
    
    // Game State
    private int homePoints = 0;
    private int guestPoints = 0;
    private int currentQuarter = 1;
    private String selectedPlayer = null;
    private boolean isHomeTeam = true;
    
    // Timer
    private Handler timerHandler = new Handler();
    private boolean isTimerRunning = false;
    private int quarterTimeSeconds = 720; // 12 minutes = 720 seconds
    private final int QUARTER_DURATION = 720; // 12 minutes per quarter
    
    // Game Log
    private ArrayList<GameEvent> gameLog = new ArrayList<>();
    
    // Inner class for game events
    public static class GameEvent implements Serializable {
        private static final long serialVersionUID = 1L;
        public int quarter;
        public String time;
        public String team;
        public String player;
        public String event;
        public Date timestamp;
        
        public GameEvent(int quarter, String time, String team, String player, String event) {
            this.quarter = quarter;
            this.time = time;
            this.team = team;
            this.player = player;
            this.event = event;
            this.timestamp = new Date();
        }
        
        @Override
        public String toString() {
            return String.format("Q%d %s | %s | %s | %s", 
                quarter, time, team, player, event);
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupPlayerButtons();
        setupActionButtons();
    }
    
    private void initializeViews() {
        quarterDisplay = findViewById(R.id.quarterDisplay);
        timeDisplay = findViewById(R.id.timeDisplay);
        homeScore = findViewById(R.id.homeScore);
        guestScore = findViewById(R.id.guestScore);
        btnStartStop = findViewById(R.id.btnStartStop);
        btnNextQuarter = findViewById(R.id.btnNextQuarter);
        btnViewLog = findViewById(R.id.btnViewLog);
        
        updateTimerDisplay();
    }
    
    private void setupPlayerButtons() {
        // Home Team Players
        setupPlayerButton(R.id.homePlayer1, "#23 J. Smith", true);
        setupPlayerButton(R.id.homePlayer2, "#11 M. Johnson", true);
        setupPlayerButton(R.id.homePlayer3, "#5 K. Williams", true);
        setupPlayerButton(R.id.homePlayer4, "#32 D. Brown", true);
        setupPlayerButton(R.id.homePlayer5, "#7 R. Davis", true);
        
        // Guest Team Players
        setupPlayerButton(R.id.guestPlayer1, "#15 A. Wilson", false);
        setupPlayerButton(R.id.guestPlayer2, "#24 B. Martinez", false);
        setupPlayerButton(R.id.guestPlayer3, "#9 C. Anderson", false);
        setupPlayerButton(R.id.guestPlayer4, "#17 D. Thomas", false);
        setupPlayerButton(R.id.guestPlayer5, "#3 E. Garcia", false);
    }
    
    private void setupPlayerButton(int buttonId, final String playerName, final boolean isHome) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPlayer = playerName;
                isHomeTeam = isHome;
                String team = isHome ? "Home" : "Guest";
                Toast.makeText(MainActivity.this, 
                    "Selected: " + playerName + " (" + team + ")", 
                    Toast.LENGTH_SHORT).show();
                
                // Visual feedback - highlight selected button
                highlightSelectedPlayer((Button) v, isHome);
            }
        });
    }
    
    private void highlightSelectedPlayer(Button selectedButton, boolean isHome) {
        // Reset all player buttons to default color
        int[] homePlayerIds = {R.id.homePlayer1, R.id.homePlayer2, R.id.homePlayer3, 
                                R.id.homePlayer4, R.id.homePlayer5};
        int[] guestPlayerIds = {R.id.guestPlayer1, R.id.guestPlayer2, R.id.guestPlayer3,
                                 R.id.guestPlayer4, R.id.guestPlayer5};
        
        // Reset home team buttons
        for (int id : homePlayerIds) {
            Button btn = findViewById(id);
            btn.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_dark));
        }
        
        // Reset guest team buttons
        for (int id : guestPlayerIds) {
            Button btn = findViewById(id);
            btn.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
        }
        
        // Highlight selected button
        selectedButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_light));
    }
    
    private void setupActionButtons() {
        // Scoring buttons
        findViewById(R.id.btn1Point).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAction("1 Point", 1);
            }
        });
        
        findViewById(R.id.btn2Point).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAction("2 Points", 2);
            }
        });
        
        findViewById(R.id.btn3Point).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAction("3 Points", 3);
            }
        });
        
        // Stats buttons
        findViewById(R.id.btnRebound).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAction("Rebound", 0);
            }
        });
        
        findViewById(R.id.btnAssist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAction("Assist", 0);
            }
        });
        
        findViewById(R.id.btnSteal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAction("Steal", 0);
            }
        });
        
        findViewById(R.id.btnBlock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAction("Block", 0);
            }
        });
        
        // Violations
        findViewById(R.id.btnFoul).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAction("Foul", 0);
            }
        });
        
        findViewById(R.id.btnTurnover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAction("Turnover", 0);
            }
        });
        
        // Game control
        findViewById(R.id.btnTimeout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTimeout();
            }
        });
        
        // Timer controls
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTimer();
            }
        });
        
        btnNextQuarter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextQuarter();
            }
        });
        
        // View Log button
        btnViewLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGameLog();
            }
        });
    }
    
    private void handleAction(String action, int points) {
        if (selectedPlayer == null) {
            Toast.makeText(this, "Please select a player first!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String team = isHomeTeam ? "Home" : "Guest";
        String message = selectedPlayer + " - " + action;
        
        // Update score if it's a scoring action
        if (points > 0) {
            if (isHomeTeam) {
                homePoints += points;
                homeScore.setText("HOME: " + homePoints);
            } else {
                guestPoints += points;
                guestScore.setText("GUEST: " + guestPoints);
            }
        }
        
        // Add to game log
        String currentTime = getFormattedTime();
        GameEvent event = new GameEvent(currentQuarter, currentTime, team, selectedPlayer, action);
        gameLog.add(event);
        
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        // Log the action (for future backend integration)
        logGameEvent(team, selectedPlayer, action, points);
    }
    
    private void handleTimeout() {
        String team;
        String eventDescription;
        
        if (selectedPlayer == null) {
            team = "Game";
            eventDescription = "Official Timeout";
            Toast.makeText(this, "General Timeout Called", Toast.LENGTH_SHORT).show();
        } else {
            team = isHomeTeam ? "Home" : "Guest";
            eventDescription = "Team Timeout";
            Toast.makeText(this, team + " Team Timeout", Toast.LENGTH_SHORT).show();
        }
        
        // Pause the timer
        if (isTimerRunning) {
            toggleTimer();
        }
        
        // Add to game log
        String currentTime = getFormattedTime();
        GameEvent event = new GameEvent(currentQuarter, currentTime, team, "-", eventDescription);
        gameLog.add(event);
    }
    
    private void logGameEvent(String team, String player, String action, int points) {
        // This is where you would log events for future backend integration
        // For now, just print to console
        System.out.println("Game Event: " + team + " - " + player + " - " + action);
    }
    
    // Timer methods
    private void toggleTimer() {
        if (isTimerRunning) {
            stopTimer();
        } else {
            startTimer();
        }
    }
    
    private void startTimer() {
        isTimerRunning = true;
        btnStartStop.setText("STOP");
        timerHandler.post(timerRunnable);
    }
    
    private void stopTimer() {
        isTimerRunning = false;
        btnStartStop.setText("START");
        timerHandler.removeCallbacks(timerRunnable);
    }
    
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (quarterTimeSeconds > 0) {
                quarterTimeSeconds--;
                updateTimerDisplay();
                timerHandler.postDelayed(this, 1000);
            } else {
                // Quarter ended
                stopTimer();
                Toast.makeText(MainActivity.this, "Quarter " + currentQuarter + " Ended!", Toast.LENGTH_LONG).show();
            }
        }
    };
    
    private void updateTimerDisplay() {
        int minutes = quarterTimeSeconds / 60;
        int seconds = quarterTimeSeconds % 60;
        timeDisplay.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
    }
    
    private String getFormattedTime() {
        int minutes = quarterTimeSeconds / 60;
        int seconds = quarterTimeSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }
    
    private void nextQuarter() {
        if (currentQuarter < 4) {
            // Stop timer if running
            if (isTimerRunning) {
                stopTimer();
            }
            
            currentQuarter++;
            quarterTimeSeconds = QUARTER_DURATION;
            quarterDisplay.setText("Q" + currentQuarter);
            updateTimerDisplay();
            
            // Log quarter change
            GameEvent event = new GameEvent(currentQuarter, "12:00", "Game", "-", "Quarter " + currentQuarter + " Started");
            gameLog.add(event);
            
            Toast.makeText(this, "Quarter " + currentQuarter + " Started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Game Over!", Toast.LENGTH_LONG).show();
            
            // Log game end
            GameEvent event = new GameEvent(currentQuarter, getFormattedTime(), "Game", "-", "Game Ended");
            gameLog.add(event);
        }
    }
    
    private void showGameLog() {
        // Launch the LogActivity
        Intent intent = new Intent(this, LogActivity.class);
        intent.putExtra("gameLog", gameLog);
        startActivity(intent);
    }
}