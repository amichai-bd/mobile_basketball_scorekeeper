package com.example.myapp.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Team model for league teams (Lakers, Warriors, Bulls, Heat)
 * Represents predefined teams with player rosters
 */
public class Team {
    private int id;
    private String name;
    private List<TeamPlayer> players;
    
    // Constructor
    public Team(int id, String name) {
        this.id = id;
        this.name = name;
        this.players = new ArrayList<>();
    }
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public List<TeamPlayer> getPlayers() { return players; }
    
    // Add player to roster
    public void addPlayer(TeamPlayer player) {
        this.players.add(player);
    }
    
    // Display format for dropdown
    @Override
    public String toString() {
        return name;
    }
    
    // Get player by ID
    public TeamPlayer getPlayerById(int playerId) {
        for (TeamPlayer player : players) {
            if (player.getId() == playerId) {
                return player;
            }
        }
        return null;
    }
}
