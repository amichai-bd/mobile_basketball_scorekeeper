package com.example.myapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class LogActivity extends Activity {
    
    private ListView logListView;
    private TextView emptyView;
    private TextView totalEventsView;
    private ArrayList<MainActivity.GameEvent> gameLog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create the layout programmatically
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFF1a1a1a);
        
        // Header
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setBackgroundColor(0xFF8e44ad);
        headerLayout.setPadding(20, 20, 20, 20);
        
        TextView titleView = new TextView(this);
        titleView.setText("GAME LOG");
        titleView.setTextColor(0xFFFFFFFF);
        titleView.setTextSize(20f);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        
        totalEventsView = new TextView(this);
        totalEventsView.setTextColor(0xFFFFFFFF);
        totalEventsView.setTextSize(16f);
        
        Button backButton = new Button(this);
        backButton.setText("BACK");
        backButton.setTextColor(0xFFFFFFFF);
        backButton.setBackgroundColor(0xFF6c3483);
        backButton.setPadding(30, 10, 30, 10);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        headerLayout.addView(titleView);
        headerLayout.addView(totalEventsView);
        headerLayout.addView(backButton);
        
        // List View
        logListView = new ListView(this);
        logListView.setBackgroundColor(0xFF2c2c2c);
        logListView.setDivider(getResources().getDrawable(android.R.color.darker_gray));
        logListView.setDividerHeight(1);
        logListView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        
        // Empty view
        emptyView = new TextView(this);
        emptyView.setText("No events logged yet");
        emptyView.setTextColor(0xFF888888);
        emptyView.setTextSize(18f);
        emptyView.setGravity(android.view.Gravity.CENTER);
        emptyView.setPadding(20, 100, 20, 100);
        
        mainLayout.addView(headerLayout);
        mainLayout.addView(logListView);
        
        setContentView(mainLayout);
        
        // Get the game log from the intent
        gameLog = (ArrayList<MainActivity.GameEvent>) getIntent().getSerializableExtra("gameLog");
        if (gameLog == null) {
            gameLog = new ArrayList<>();
        }
        
        updateDisplay();
    }
    
    private void updateDisplay() {
        totalEventsView.setText(gameLog.size() + " events");
        
        if (gameLog.isEmpty()) {
            logListView.setVisibility(View.GONE);
            if (emptyView.getParent() == null) {
                ((ViewGroup) logListView.getParent()).addView(emptyView);
            }
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            logListView.setVisibility(View.VISIBLE);
            logListView.setAdapter(new GameLogAdapter());
        }
    }
    
    private class GameLogAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return gameLog.size();
        }
        
        @Override
        public Object getItem(int position) {
            return gameLog.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemLayout;
            
            if (convertView == null) {
                itemLayout = new LinearLayout(LogActivity.this);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(20, 15, 20, 15);
                itemLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            } else {
                itemLayout = (LinearLayout) convertView;
                itemLayout.removeAllViews();
            }
            
            MainActivity.GameEvent event = gameLog.get(position);
            
            // Quarter badge
            TextView quarterBadge = new TextView(LogActivity.this);
            quarterBadge.setText("Q" + event.quarter);
            quarterBadge.setTextColor(0xFFffd700);
            quarterBadge.setTextSize(14f);
            quarterBadge.setTypeface(null, android.graphics.Typeface.BOLD);
            quarterBadge.setBackgroundColor(0xFF4a4a4a);
            quarterBadge.setPadding(15, 8, 15, 8);
            LinearLayout.LayoutParams qParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT);
            qParams.setMargins(0, 0, 15, 0);
            quarterBadge.setLayoutParams(qParams);
            
            // Time
            TextView timeView = new TextView(LogActivity.this);
            timeView.setText(event.time);
            timeView.setTextColor(0xFFFFFFFF);
            timeView.setTextSize(16f);
            timeView.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams tParams = new LinearLayout.LayoutParams(
                100, LinearLayout.LayoutParams.WRAP_CONTENT);
            tParams.setMargins(0, 0, 15, 0);
            timeView.setLayoutParams(tParams);
            
            // Team badge
            TextView teamBadge = new TextView(LogActivity.this);
            teamBadge.setText(event.team);
            teamBadge.setTextSize(12f);
            teamBadge.setPadding(12, 5, 12, 5);
            LinearLayout.LayoutParams teamParams = new LinearLayout.LayoutParams(
                120, LinearLayout.LayoutParams.WRAP_CONTENT);
            teamParams.setMargins(0, 0, 15, 0);
            teamBadge.setLayoutParams(teamParams);
            
            if (event.team.equals("Home")) {
                teamBadge.setTextColor(0xFFFFFFFF);
                teamBadge.setBackgroundColor(0xFFff6b6b);
            } else if (event.team.equals("Guest")) {
                teamBadge.setTextColor(0xFFFFFFFF);
                teamBadge.setBackgroundColor(0xFF50c878);
            } else {
                teamBadge.setTextColor(0xFF888888);
                teamBadge.setBackgroundColor(0xFF3d3d3d);
            }
            
            // Player and Event in a vertical layout
            LinearLayout detailsLayout = new LinearLayout(LogActivity.this);
            detailsLayout.setOrientation(LinearLayout.VERTICAL);
            detailsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            
            TextView playerView = new TextView(LogActivity.this);
            playerView.setText(event.player);
            playerView.setTextColor(0xFFcccccc);
            playerView.setTextSize(14f);
            
            TextView eventView = new TextView(LogActivity.this);
            eventView.setText(event.event);
            eventView.setTextColor(0xFFFFFFFF);
            eventView.setTextSize(16f);
            eventView.setTypeface(null, android.graphics.Typeface.BOLD);
            
            // Color code certain events
            if (event.event.contains("Point")) {
                eventView.setTextColor(0xFFffd700);
            } else if (event.event.equals("Foul")) {
                eventView.setTextColor(0xFFff6b6b);
            } else if (event.event.equals("Timeout")) {
                eventView.setTextColor(0xFFffa500);
            }
            
            detailsLayout.addView(playerView);
            detailsLayout.addView(eventView);
            
            // Add all views to item layout
            itemLayout.addView(quarterBadge);
            itemLayout.addView(timeView);
            itemLayout.addView(teamBadge);
            itemLayout.addView(detailsLayout);
            
            // Alternate row colors
            if (position % 2 == 0) {
                itemLayout.setBackgroundColor(0xFF2c2c2c);
            } else {
                itemLayout.setBackgroundColor(0xFF333333);
            }
            
            return itemLayout;
        }
    }
}
