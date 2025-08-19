package com.example.myapp.models;

/**
 * Basic Game model for storing game information
 * Simple in-memory model for initial testing (no database yet)
 */
public class Game {
    private int id;
    private String date;
    private String homeTeam;
    private String awayTeam;
    private String status; // "scheduled", "in_progress", "completed"
    
    // Constructor
    public Game(int id, String date, String homeTeam, String awayTeam) {
        this.id = id;
        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.status = "scheduled";
    }
    
    // Getters
    public int getId() { return id; }
    public String getDate() { return date; }
    public String getHomeTeam() { return homeTeam; }
    public String getAwayTeam() { return awayTeam; }
    public String getStatus() { return status; }
    
    // Setters
    public void setDate(String date) { this.date = date; }
    public void setHomeTeam(String homeTeam) { this.homeTeam = homeTeam; }
    public void setAwayTeam(String awayTeam) { this.awayTeam = awayTeam; }
    public void setStatus(String status) { this.status = status; }
    
    // Display format for list
    @Override
    public String toString() {
        return homeTeam + " vs " + awayTeam + " - " + date;
    }
}
