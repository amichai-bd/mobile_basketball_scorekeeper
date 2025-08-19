package com.example.myapp.models;

/**
 * TeamPlayer model for players in team rosters
 * Represents predefined players with numbers and names for each team
 */
public class TeamPlayer {
    private int id;
    private int teamId;
    private int number;
    private String name;
    private boolean isSelected; // For UI selection state
    
    // Constructor
    public TeamPlayer(int id, int teamId, int number, String name) {
        this.id = id;
        this.teamId = teamId;
        this.number = number;
        this.name = name;
        this.isSelected = false;
    }
    
    // Getters
    public int getId() { return id; }
    public int getTeamId() { return teamId; }
    public int getNumber() { return number; }
    public String getName() { return name; }
    public boolean isSelected() { return isSelected; }
    
    // Setters
    public void setSelected(boolean selected) { this.isSelected = selected; }
    
    // Display format for checkbox list
    @Override
    public String toString() {
        return "#" + number + " " + name;
    }
    
    // For game player creation
    public Player toGamePlayer(int gameId, String teamSide) {
        return new Player(this.id, gameId, teamSide, this.number, this.name);
    }
}
