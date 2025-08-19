package com.example.myapp;

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
        
        // Get all events from GameActivity's shared storage
        allEvents = new ArrayList<>(GameActivity.getAllGameEvents());
        
        // Add some sample historical events for demonstration if empty
        if (allEvents.isEmpty()) {
            addSampleEvents();
        }
    }
    
    private void addSampleEvents() {
        allEvents.add("Q1 9:30 - #23 LeBron James - 2P");
        allEvents.add("Q1 9:15 - #30 Stephen Curry - 3P");
        allEvents.add("Q1 8:45 - #3 Anthony Davis - DR");
        allEvents.add("Q1 8:30 - Lakers - TIMEOUT");
        allEvents.add("Q1 8:00 - #11 Klay Thompson - FOUL");
    }
    
    private void initializeViews() {
        tvTitle = findViewById(R.id.tvLogTitle);
        lvEventLog = findViewById(R.id.lvEventLog);
        btnBackToGame = findViewById(R.id.btnBackToGame);
        
        // Set title
        String title = String.format("Event Log - %s vs %s", teamAName, teamBName);
        tvTitle.setText(title);
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
         * - Player events: "Q1 8:45 - #23 LeBron James - 2P"
         * - Team events: "Q1 8:45 - Lakers - TIMEOUT"
         * - Legacy format: "8:45 - #23 LeBron James - 2P" (without quarter)
         */
        private String[] parseEventString(String event) {
            String[] result = new String[4]; // Quarter, Time, Player, Event
            
            try {
                // Split by " - "
                String[] parts = event.split(" - ");
                
                if (parts.length >= 3) {
                    // Check if first part contains quarter info "Q1 8:45" or just time "8:45"
                    String[] timeInfo = parts[0].split(" ");
                    
                    if (timeInfo.length >= 2 && timeInfo[0].startsWith("Q")) {
                        // New format with quarter: "Q1 8:45"
                        result[0] = timeInfo[0]; // Quarter (Q1)
                        result[1] = timeInfo[1]; // Time (8:45)
                    } else {
                        // Legacy format without quarter: "8:45"
                        result[0] = "Q1"; // Default quarter
                        result[1] = parts[0]; // Time (8:45)
                    }
                    
                    // Second part: Player name or team name
                    result[2] = parts[1]; // "#23 LeBron James" or "Lakers"
                    
                    // Third part: Event type
                    result[3] = parts[2]; // "2P", "TIMEOUT", etc.
                    
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
        // Remove from shared game events (this updates the source)
        boolean removed = GameActivity.removeGameEvent(event);
        
        if (removed) {
            // Update local list and refresh adapter
            allEvents.remove(position);
            eventAdapter.notifyDataSetChanged();
            Toast.makeText(this, "✅ Event deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error: Could not delete event", Toast.LENGTH_SHORT).show();
        }
    }
}