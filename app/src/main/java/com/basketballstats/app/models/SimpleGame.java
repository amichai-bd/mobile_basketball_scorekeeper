package com.basketballstats.app.models;

/**
 * SimpleGame model for clean game selection
 * Represents available games without status complexity
 */
public class SimpleGame {
    private int id;
    private String date; // DD/MM/YYYY format
    private Team homeTeam;
    private Team awayTeam;
    
    // Constructor
    public SimpleGame(int id, String date, Team homeTeam, Team awayTeam) {
        this.id = id;
        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }
    
    // Getters
    public int getId() { return id; }
    public String getDate() { return date; }
    public Team getHomeTeam() { return homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    
    // Display format for clean card layout
    @Override
    public String toString() {
        return homeTeam.getName() + " vs " + awayTeam.getName() + "\n" + date;
    }
    
    // Card display format
    public String getMatchupText() {
        return homeTeam.getName() + " vs " + awayTeam.getName();
    }
    
    public String getDateText() {
        return date;
    }
}
