package com.basketballstats.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.basketballstats.app.models.GamePlayer;
import java.util.ArrayList;
import java.util.List;

/**
 * Log Activity - Complete Event Log Viewer (Frame 5)
 * Professional event log with edit/delete functionality
 */
public class LogActivity extends Activity {
    
    private TextView tvTitle;
    private ListView lvEventLog;
    private Button btnBackToGame;
    private Button btnClearLog;
    private Button btnEndGame;
    
    private int gameId;
    private String teamAName, teamBName;
    private List<String> allEvents;
    private EventLogAdapter eventAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        
        // Get data from intent
        getDataFromIntent();
        
        // Initialize UI
        initializeViews();
        
        // Initialize data
        initializeData();
        
        // Setup event listeners
        setupEventListeners();
        
        Toast.makeText(this, "Event Log loaded - " + allEvents.size() + " events", Toast.LENGTH_SHORT).show();
    }
    
    private void getDataFromIntent() {
        gameId = getIntent().getIntExtra("gameId", 1);
        teamAName = getIntent().getStringExtra("teamAName");
        teamBName = getIntent().getStringExtra("teamBName");
        
        // ✅ DEBUG: Log received data to diagnose empty log issue
        android.util.Log.d("LogActivity", String.format("📊 RECEIVED DATA - GameID: %d, TeamA: %s, TeamB: %s", 
            gameId, teamAName, teamBName));
        
        // ✅ FIXED: Load all events from database for this game
        loadEventsFromDatabase();
    }
    
    /**
     * Load events from SQLite database for the current game
     */
    private void loadEventsFromDatabase() {
        allEvents = new ArrayList<>();
        
        try {
            // ✅ DEBUG: Enhanced logging to diagnose empty log issue
            android.util.Log.d("LogActivity", "🔍 STARTING loadEventsFromDatabase() for gameId: " + gameId);
            
            // Initialize database controller
            com.basketballstats.app.data.DatabaseController dbController = 
                com.basketballstats.app.data.DatabaseController.getInstance(this);
            android.util.Log.d("LogActivity", "✅ Database controller initialized");
            
            // Load all events for this game from SQLite database
            java.util.List<com.basketballstats.app.models.Event> gameEvents = 
                com.basketballstats.app.models.Event.findByGameId(dbController.getDatabaseHelper(), gameId);
            android.util.Log.d("LogActivity", String.format("📋 Found %d raw events in database for gameId %d", 
                gameEvents.size(), gameId));
            
            // Convert Event objects to display strings
            for (int i = 0; i < gameEvents.size(); i++) {
                com.basketballstats.app.models.Event event = gameEvents.get(i);
                android.util.Log.d("LogActivity", String.format("🔄 Processing event %d: %s (player=%d)", 
                    i+1, event.getEventType(), event.getPlayerId()));
                
                // Load related objects (players) for complete display
                event.loadRelatedObjects(dbController.getDatabaseHelper());
                
                // Add formatted event string to display list
                String eventString = event.toString();
                allEvents.add(eventString);
                android.util.Log.d("LogActivity", String.format("✅ Event %d formatted: %s", i+1, eventString));
            }
            
            android.util.Log.d("LogActivity", String.format("🎯 FINAL RESULT: %d events loaded for display", allEvents.size()));
            
        } catch (Exception e) {
            android.util.Log.e("LogActivity", "❌ Error loading events from database for gameId " + gameId, e);
            allEvents = new ArrayList<>(); // Fallback to empty list
        }
    }
    

    
    private void initializeViews() {
        tvTitle = findViewById(R.id.tvLogTitle);
        lvEventLog = findViewById(R.id.lvEventLog);
        btnBackToGame = findViewById(R.id.btnBackToGame);
        btnClearLog = findViewById(R.id.btnClearLog);
        btnEndGame = findViewById(R.id.btnEndGame);
        
        // Set title
        String title = String.format("Event Log - %s vs %s", teamAName, teamBName);
        tvTitle.setText(title);
        
        // ✅ NEW: Show/hide End Game button based on game status
        updateEndGameButtonVisibility();
    }
    
    private void initializeData() {
        eventAdapter = new EventLogAdapter(this, allEvents);
        lvEventLog.setAdapter(eventAdapter);
    }
    
    private void setupEventListeners() {
        btnBackToGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Return to GameActivity
            }
        });
        
        btnClearLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearLogConfirmation();
            }
        });
        
        btnEndGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndGameConfirmation();
            }
        });
    }
    
    /**
     * Show confirmation dialog for complete game reset
     * ✅ ENHANCED: Updated to reflect complete reset functionality
     */
    private void showClearLogConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Game Completely")
               .setMessage("⚠️ This will COMPLETELY RESET the game:\n\n" +
                          "• Delete ALL events\n" +
                          "• Clear player selections\n" +
                          "• Reset score to 0-0\n" +
                          "• Reset to Quarter 1\n" +
                          "• Reset timer to 10:00\n" +
                          "• Change status to 'Not Started'\n\n" +
                          "🔄 The game will return to setup mode.\n\n" +
                          "Are you sure you want to continue?")
               .setPositiveButton("Reset Game", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       clearAllEvents();
                   }
               })
               .setNegativeButton("Cancel", null)
               .setIcon(android.R.drawable.ic_dialog_alert)
               .show();
    }
    
    /**
     * Clear all events for the current game from SQLite database
     * ✅ ENHANCED: Complete game reset - events, players, scores, quarter, timer, status
     */
    private void clearAllEvents() {
        try {
            com.basketballstats.app.data.DatabaseController dbController = 
                com.basketballstats.app.data.DatabaseController.getInstance(this);
            
            // 1. Delete all events for this game from SQLite database
            int deletedCount = com.basketballstats.app.models.Event.deleteByGameId(
                dbController.getDatabaseHelper(), gameId);
            
            // 2. ✅ NEW: Clear all player selections from game_players table
            int deletedPlayers = com.basketballstats.app.models.GamePlayer.deleteByGameId(
                dbController.getDatabaseHelper(), gameId);
            
            // 3. ✅ NEW: Complete game reset - scores, quarter, timer, status
            resetGameToNotStarted(dbController);
            
            // 4. Clear local list and refresh adapter
            allEvents.clear();
            eventAdapter.notifyDataSetChanged();
            
            Toast.makeText(this, String.format("✅ Complete reset: %d events + %d players cleared", 
                deletedCount, deletedPlayers), Toast.LENGTH_LONG).show();
            
            android.util.Log.d("LogActivity", String.format("🔄 COMPLETE RESET: %d events + %d players cleared, game → not_started", 
                deletedCount, deletedPlayers));
            
        } catch (Exception e) {
            android.util.Log.e("LogActivity", "❌ Error performing complete game reset for gameId " + gameId, e);
            Toast.makeText(this, "Error: Could not reset game completely", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * ✅ NEW: Complete game reset to "not_started" status
     * Resets scores, quarter, timer, and transitions status to "not_started"
     */
    private void resetGameToNotStarted(com.basketballstats.app.data.DatabaseController dbController) {
        try {
            // Load the game
            com.basketballstats.app.models.Game game = 
                com.basketballstats.app.models.Game.findById(dbController.getDatabaseHelper(), gameId);
            
            if (game != null) {
                // Reset all game state to initial values
                game.setHomeScore(0);
                game.setAwayScore(0);
                game.setCurrentQuarter(1);        // Reset to Q1
                game.setGameClockSeconds(600);    // Reset to 10:00
                game.setClockRunning(false);      // Stop clock
                
                // ✅ NEW: Transition to "not_started" status
                game.setToNotStarted();
                
                // Save all changes to database
                game.save(dbController.getDatabaseHelper());
                
                android.util.Log.d("LogActivity", "✅ Complete reset: scores → 0-0, quarter → Q1, timer → 10:00, status → not_started");
            }
        } catch (Exception e) {
            android.util.Log.e("LogActivity", "❌ Error performing complete game reset", e);
        }
    }
    
    /**
     * ✅ NEW: Show/hide End Game button based on game status
     * Only visible for games in "game_in_progress" status
     */
    private void updateEndGameButtonVisibility() {
        try {
            com.basketballstats.app.data.DatabaseController dbController = 
                com.basketballstats.app.data.DatabaseController.getInstance(this);
            
            // Load the game to check its status
            com.basketballstats.app.models.Game game = 
                com.basketballstats.app.models.Game.findById(dbController.getDatabaseHelper(), gameId);
            
            if (game != null && game.isGameInProgress()) {
                // Show End Game button only for games in progress
                btnEndGame.setVisibility(View.VISIBLE);
                android.util.Log.d("LogActivity", "End Game button shown - game in progress");
            } else {
                // Hide for not_started and done games
                btnEndGame.setVisibility(View.GONE);
                android.util.Log.d("LogActivity", "End Game button hidden - game not in progress");
            }
        } catch (Exception e) {
            android.util.Log.e("LogActivity", "Error checking game status for End Game button", e);
            btnEndGame.setVisibility(View.GONE); // Hide on error
        }
    }
    
    /**
     * ✅ NEW: Show confirmation dialog for manual end game
     */
    private void showEndGameConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("End Game")
               .setMessage("🏁 End this game now?\n\n" +
                          "This will mark the game as completed.\n" +
                          "You can still view and edit events afterwards.\n\n" +
                          "Continue?")
               .setPositiveButton("End Game", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       endGameManually();
                   }
               })
               .setNegativeButton("Cancel", null)
               .setIcon(android.R.drawable.ic_dialog_info)
               .show();
    }
    
    /**
     * ✅ NEW: Manual end game - transition to "done" status
     */
    private void endGameManually() {
        try {
            com.basketballstats.app.data.DatabaseController dbController = 
                com.basketballstats.app.data.DatabaseController.getInstance(this);
            
            // Load the game
            com.basketballstats.app.models.Game game = 
                com.basketballstats.app.models.Game.findById(dbController.getDatabaseHelper(), gameId);
            
            if (game != null && game.isGameInProgress()) {
                // Transition to "done" status
                game.setToDone();
                game.save(dbController.getDatabaseHelper());
                
                // Update button visibility (hide End Game button)
                updateEndGameButtonVisibility();
                
                Toast.makeText(this, "🏁 Game ended manually - marked as complete!", Toast.LENGTH_LONG).show();
                android.util.Log.d("LogActivity", "✅ Manual end game: game_in_progress → done");
                
            } else {
                Toast.makeText(this, "Cannot end game - not in progress", Toast.LENGTH_SHORT).show();
                android.util.Log.w("LogActivity", "Cannot end game manually - not in game_in_progress status");
            }
            
        } catch (Exception e) {
            android.util.Log.e("LogActivity", "❌ Error ending game manually", e);
            Toast.makeText(this, "Error: Could not end game", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * ✅ FIXED: Recalculate game scores from all remaining scoring events
     * CRITICAL FIX: Properly map team names to home/away based on database assignment
     */
    private void recalculateGameScores(com.basketballstats.app.data.DatabaseController dbController) {
        try {
            // Load the game with team information
            com.basketballstats.app.models.Game game = 
                com.basketballstats.app.models.Game.findById(dbController.getDatabaseHelper(), gameId);
            
            if (game == null) return;
            
            // Load the teams to get actual home/away assignment
            game.loadTeams(dbController.getDatabaseHelper());
            
            // Get all remaining events for this game
            java.util.List<com.basketballstats.app.models.Event> remainingEvents = 
                com.basketballstats.app.models.Event.findByGameId(dbController.getDatabaseHelper(), gameId);
            
            int homeScore = 0;
            int awayScore = 0;
            
            // ✅ CRITICAL FIX: Get actual home/away team names from database
            String homeTeamName = game.getHomeTeam() != null ? game.getHomeTeam().getName() : teamAName;
            String awayTeamName = game.getAwayTeam() != null ? game.getAwayTeam().getName() : teamBName;
            
            // Calculate scores from all scoring events using actual home/away mapping
            for (com.basketballstats.app.models.Event event : remainingEvents) {
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
            
            // ✅ FIXED: Always update database with correct home/away scores
            game.setHomeScore(homeScore);
            game.setAwayScore(awayScore);
            game.save(dbController.getDatabaseHelper());
            
            android.util.Log.d("LogActivity", String.format("🔄 Recalculated scores: HOME[%s]=%d AWAY[%s]=%d", 
                homeTeamName, homeScore, awayTeamName, awayScore));
                
        } catch (Exception e) {
            android.util.Log.e("LogActivity", "❌ Error recalculating game scores", e);
        }
    }
    
    /**
     * Custom adapter for event log with edit/delete functionality
     */
    private class EventLogAdapter extends BaseAdapter {
        private Context context;
        private List<String> events;
        private LayoutInflater inflater;
        
        public EventLogAdapter(Context context, List<String> events) {
            this.context = context;
            this.events = events;
            this.inflater = LayoutInflater.from(context);
        }
        
        @Override
        public int getCount() { return events.size(); }
        
        @Override
        public Object getItem(int position) { return events.get(position); }
        
        @Override
        public long getItemId(int position) { return position; }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_event_log, parent, false);
            }
            
            String event = events.get(position);
            
            // Find table column views
            TextView tvQuarter = convertView.findViewById(R.id.tvQuarter);
            TextView tvTime = convertView.findViewById(R.id.tvTime);
            TextView tvPlayer = convertView.findViewById(R.id.tvPlayer);
            TextView tvEvent = convertView.findViewById(R.id.tvEvent);
            Button btnEdit = convertView.findViewById(R.id.btnEditEvent);
            Button btnDelete = convertView.findViewById(R.id.btnDeleteEvent);
            
            // Parse event string (format: "Q1 8:45 - #23 LeBron James - 2P")
            String[] eventParts = parseEventString(event);
            
            // Set table columns
            tvQuarter.setText(eventParts[0]); // Quarter
            tvTime.setText(eventParts[1]);    // Time
            tvPlayer.setText(eventParts[2]);  // Player
            tvEvent.setText(eventParts[3]);   // Event
            
            // Set event color based on type
            setEventColor(tvEvent, eventParts[3]);
            
            // Edit button (placeholder)
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editEvent(event, position);
                }
            });
            
            // Delete button
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmDeleteEvent(event, position);
                }
            });
            
            return convertView;
        }
        
        /**
         * Parse event string into table columns
         * Expected formats:
         * - Player events: "Q1 8:45 - LAKERS - #23 LeBron James - 2P" (4 parts)
         * - Team events: "Q1 8:45 - LAKERS - TIMEOUT" (3 parts)
         * - Legacy format: "8:45 - #23 LeBron James - 2P" (without quarter, 3 parts)
         */
        private String[] parseEventString(String event) {
            String[] result = new String[4]; // Quarter, Time, Player, Event
            
            try {
                // Split by " - "
                String[] parts = event.split(" - ");
                
                if (parts.length >= 4) {
                    // New 4-part format: "Q1 8:45 - LAKERS - #23 LeBron James - 2P"
                    String[] timeInfo = parts[0].split(" ");
                    
                    if (timeInfo.length >= 2 && timeInfo[0].startsWith("Q")) {
                        result[0] = timeInfo[0]; // Quarter (Q1)
                        result[1] = timeInfo[1]; // Time (8:45)
                    } else {
                        result[0] = "Q1"; // Default quarter
                        result[1] = parts[0]; // Time (8:45)
                    }
                    
                    // Skip team name (parts[1]) for display, use player name
                    result[2] = parts[2]; // "#23 LeBron James"
                    result[3] = parts[3]; // "2P"
                    
                } else if (parts.length == 3) {
                    // 3-part format: Could be team event or legacy player event
                    String[] timeInfo = parts[0].split(" ");
                    
                    if (timeInfo.length >= 2 && timeInfo[0].startsWith("Q")) {
                        // "Q1 8:45 - LAKERS - TIMEOUT" (team event)
                        result[0] = timeInfo[0]; // Quarter (Q1)
                        result[1] = timeInfo[1]; // Time (8:45)
                        result[2] = parts[1]; // "LAKERS"
                        result[3] = parts[2]; // "TIMEOUT"
                    } else {
                        // Legacy format: "8:45 - #23 LeBron James - 2P"
                        result[0] = "Q1"; // Default quarter
                        result[1] = parts[0]; // Time (8:45)
                        result[2] = parts[1]; // "#23 LeBron James"
                        result[3] = parts[2]; // "2P"
                    }
                    
                } else if (parts.length == 2) {
                    // Handle format: "Player - Event"
                    result[0] = "Q1";
                    result[1] = "0:00";
                    result[2] = parts[0];
                    result[3] = parts[1];
                } else {
                    // Single string - treat as event type
                    result[0] = "Q1";
                    result[1] = "0:00";
                    result[2] = "Unknown";
                    result[3] = event;
                }
            } catch (Exception e) {
                // Error parsing - use defaults with debug info
                result[0] = "Q1";
                result[1] = "0:00";
                result[2] = event.length() > 20 ? event.substring(0, 20) + "..." : event;
                result[3] = "ERR";
            }
            
            return result;
        }
        
        /**
         * Set color for event type
         */
        private void setEventColor(TextView tvEvent, String eventType) {
            int color;
            switch (eventType) {
                case "1P":
                case "2P":
                case "3P":
                    color = 0xFF27AE60; // Green for scoring
                    break;
                case "1M":
                case "2M":
                case "3M":
                    color = 0xFFE74C3C; // Red for misses
                    break;
                case "OR":
                case "DR":
                case "AST":
                    color = 0xFF3498DB; // Blue for positive stats
                    break;
                case "FOUL":
                case "TO":
                    color = 0xFFE67E22; // Orange for violations
                    break;
                case "TIMEOUT":
                    color = 0xFF9B59B6; // Purple for team events
                    break;
                default:
                    color = 0xFF2C3E50; // Dark gray for others
                    break;
            }
            tvEvent.setTextColor(color);
        }
    }
    
    private void editEvent(String event, int position) {
        // Placeholder for event editing
        Toast.makeText(this, "Edit event: " + event + "\n(Editing functionality coming soon!)", Toast.LENGTH_LONG).show();
        // TODO: Implement event editing dialog with quarter, time, player, event type fields
    }
    
    private void confirmDeleteEvent(String event, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Event")
               .setMessage("Are you sure you want to delete this event?\n" + event)
               .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       deleteEvent(event, position);
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    private void deleteEvent(String event, int position) {
        try {
            com.basketballstats.app.data.DatabaseController dbController = 
                com.basketballstats.app.data.DatabaseController.getInstance(this);
            
            // ✅ FIX: Get all events from database and find the one at this position
            java.util.List<com.basketballstats.app.models.Event> gameEvents = 
                com.basketballstats.app.models.Event.findByGameId(dbController.getDatabaseHelper(), gameId);
            
            // Verify position is valid
            if (position < 0 || position >= gameEvents.size()) {
                Toast.makeText(this, "Error: Invalid event position", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Get the specific event to delete (events are ordered by sequence)
            com.basketballstats.app.models.Event eventToDelete = gameEvents.get(position);
            
            // Delete from SQLite database
            boolean deleted = eventToDelete.delete(dbController.getDatabaseHelper());
            
            if (deleted) {
                // ✅ NEW: Recalculate scores from remaining events
                recalculateGameScores(dbController);
                
                // Remove from local display list and refresh
                allEvents.remove(position);
                eventAdapter.notifyDataSetChanged();
                
                Toast.makeText(this, "✅ Event deleted", Toast.LENGTH_SHORT).show();
                android.util.Log.d("LogActivity", String.format("🗑️ Deleted event: %s (ID: %d)", 
                    event, eventToDelete.getId()));
            } else {
                Toast.makeText(this, "Error: Database deletion failed", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            android.util.Log.e("LogActivity", "❌ Error deleting event at position " + position, e);
            Toast.makeText(this, "Error: Could not delete event - " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}