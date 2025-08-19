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
            TextView tvEventInfo = convertView.findViewById(R.id.tvEventInfo);
            Button btnEdit = convertView.findViewById(R.id.btnEditEvent);
            Button btnDelete = convertView.findViewById(R.id.btnDeleteEvent);
            
            // Display event info
            tvEventInfo.setText(event);
            
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