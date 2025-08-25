package com.basketballstats.app.models;

/**
 * Player model for storing player roster information
 * Represents individual players in team rosters
 */
public class Player {
    private int id;
    private int gameId;
    private String team; // "home" or "away"
    private int number;
    private String name;
    private boolean isOnCourt; // MVP: always true (5 players each)
    private int personalFouls;
    
    // Constructor
    public Player(int id, int gameId, String team, int number, String name) {
        this.id = id;
        this.gameId = gameId;
        this.team = team;
        this.number = number;
        this.name = name;
        this.isOnCourt = true; // MVP: all roster players are on court
        this.personalFouls = 0;
    }
    
    // Getters
    public int getId() { return id; }
    public int getGameId() { return gameId; }
    public String getTeam() { return team; }
    public int getNumber() { return number; }
    public String getName() { return name; }
    public boolean isOnCourt() { return isOnCourt; }
    public int getPersonalFouls() { return personalFouls; }
    
    // Setters
    public void setNumber(int number) { this.number = number; }
    public void setName(String name) { this.name = name; }
    public void setOnCourt(boolean onCourt) { this.isOnCourt = onCourt; }
    public void setPersonalFouls(int personalFouls) { this.personalFouls = personalFouls; }
    
    // Display format for roster list
    @Override
    public String toString() {
        return "#" + number + " " + name;
    }
    
    // Validation helper
    public boolean isValidPlayer() {
        return number > 0 && number <= 99 && name != null && !name.trim().isEmpty();
    }
}
