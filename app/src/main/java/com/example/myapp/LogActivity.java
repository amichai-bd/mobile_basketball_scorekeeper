package com.example.myapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Log Activity - Event Log Viewer (Frame 5)
 * Complete game event history with quarter, time, player, event details
 */
public class LogActivity extends Activity {
    
    private TextView tvTitle;
    private ListView lvEventLog;
    private int gameId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get game ID from intent
        gameId = getIntent().getIntExtra("gameId", 1);
        
        // For MVP, show placeholder implementation
        Toast.makeText(this, 
            "Event Log (Frame 5) will show:\n" +
            "• Quarter (Q1, Q2, Q3, Q4)\n" +
            "• Game Time (MM:SS)\n" +
            "• Player (#Number Name) or Team Name\n" +
            "• Event Type (1P, 2P, FOUL, etc.)\n" +
            "• Edit/Delete functionality\n\n" +
            "Coming in next iteration!", 
            Toast.LENGTH_LONG).show();
        
        // Close for now
        finish();
    }
    
    // TODO: Full implementation will include:
    // - Complete event log display table
    // - Quarter, time, player, event columns
    // - Edit/delete individual events
    // - Filter by quarter, player, event type
    // - Export functionality
}