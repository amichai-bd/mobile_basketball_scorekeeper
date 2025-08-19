package com.example.myapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.example.myapp.models.Game;
import java.util.ArrayList;

/**
 * Main activity - Game Schedule Management (Frame 1)
 * Simple game schedule interface for adding and viewing games
 */
public class MainActivity extends Activity {
    
    // UI Components
    private EditText etHomeTeam, etAwayTeam, etDate;
    private Button btnAddGame, btnStartGame;
    private ListView lvGames;
    
    // Data
    private ArrayList<Game> gameList;
    private ArrayAdapter<Game> gameAdapter;
    private int nextGameId = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI components
        initializeViews();
        
        // Initialize data
        initializeData();
        
        // Set up event listeners
        setupEventListeners();
    }
    
    private void initializeViews() {
        etHomeTeam = findViewById(R.id.etHomeTeam);
        etAwayTeam = findViewById(R.id.etAwayTeam);
        etDate = findViewById(R.id.etDate);
        btnAddGame = findViewById(R.id.btnAddGame);
        btnStartGame = findViewById(R.id.btnStartGame);
        lvGames = findViewById(R.id.lvGames);
    }
    
    private void initializeData() {
        gameList = new ArrayList<>();
        gameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gameList);
        lvGames.setAdapter(gameAdapter);
        
        // Add sample games for testing
        addSampleGames();
    }
    
    private void addSampleGames() {
        gameList.add(new Game(nextGameId++, "12/15", "Lakers", "Warriors"));
        gameList.add(new Game(nextGameId++, "12/16", "Bulls", "Heat"));
        gameAdapter.notifyDataSetChanged();
    }
    
    private void setupEventListeners() {
        btnAddGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewGame();
            }
        });
        
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });
    }
    
    private void addNewGame() {
        String homeTeam = etHomeTeam.getText().toString().trim();
        String awayTeam = etAwayTeam.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        
        // Simple validation
        if (homeTeam.isEmpty() || awayTeam.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Add new game
        Game newGame = new Game(nextGameId++, date, homeTeam, awayTeam);
        gameList.add(newGame);
        gameAdapter.notifyDataSetChanged();
        
        // Clear input fields
        etHomeTeam.setText("");
        etAwayTeam.setText("");
        etDate.setText("");
        
        Toast.makeText(this, "Game added!", Toast.LENGTH_SHORT).show();
    }
    
    private void startGame() {
        if (gameList.isEmpty()) {
            Toast.makeText(this, "No games available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // For now, just show a message (placeholder for future game roster screen)
        Toast.makeText(this, "Start Game clicked! (Game Roster coming soon)", Toast.LENGTH_LONG).show();
    }
}