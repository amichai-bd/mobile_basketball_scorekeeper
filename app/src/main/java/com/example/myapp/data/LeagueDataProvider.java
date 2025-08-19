package com.example.myapp.data;

import com.example.myapp.models.Team;
import com.example.myapp.models.TeamPlayer;
import java.util.ArrayList;
import java.util.List;

/**
 * LeagueDataProvider - Provides predefined team and player data
 * Contains the 4 placeholder teams with 12 players each as specified
 */
public class LeagueDataProvider {
    
    private static List<Team> teams = null;
    private static int nextPlayerId = 1;
    
    /**
     * Get all league teams (Lakers, Warriors, Bulls, Heat)
     * @return List of 4 predefined teams with their rosters
     */
    public static List<Team> getTeams() {
        if (teams == null) {
            initializeTeams();
        }
        return teams;
    }
    
    /**
     * Get team by ID
     */
    public static Team getTeamById(int teamId) {
        for (Team team : getTeams()) {
            if (team.getId() == teamId) {
                return team;
            }
        }
        return null;
    }
    
    /**
     * Get team by name
     */
    public static Team getTeamByName(String teamName) {
        for (Team team : getTeams()) {
            if (team.getName().equals(teamName)) {
                return team;
            }
        }
        return null;
    }
    
    /**
     * Initialize the 4 predefined teams with their 12-player rosters
     */
    private static void initializeTeams() {
        teams = new ArrayList<>();
        
        // Create Lakers team
        Team lakers = new Team(1, "Lakers");
        addLakersPlayers(lakers);
        teams.add(lakers);
        
        // Create Warriors team
        Team warriors = new Team(2, "Warriors");
        addWarriorsPlayers(warriors);
        teams.add(warriors);
        
        // Create Bulls team
        Team bulls = new Team(3, "Bulls");
        addBullsPlayers(bulls);
        teams.add(bulls);
        
        // Create Heat team
        Team heat = new Team(4, "Heat");
        addHeatPlayers(heat);
        teams.add(heat);
    }
    
    private static void addLakersPlayers(Team lakers) {
        lakers.addPlayer(new TeamPlayer(nextPlayerId++, lakers.getId(), 6, "LeBron James"));
        lakers.addPlayer(new TeamPlayer(nextPlayerId++, lakers.getId(), 3, "Anthony Davis"));
        lakers.addPlayer(new TeamPlayer(nextPlayerId++, lakers.getId(), 1, "D'Angelo Russell"));
        lakers.addPlayer(new TeamPlayer(nextPlayerId++, lakers.getId(), 15, "Austin Reaves"));
        lakers.addPlayer(new TeamPlayer(nextPlayerId++, lakers.getId(), 5, "Taurean Prince"));
        lakers.addPlayer(new TeamPlayer(nextPlayerId++, lakers.getId(), 7, "Rui Hachimura"));
        lakers.addPlayer(new TeamPlayer(nextPlayerId++, lakers.getId(), 28, "Christian Wood"));
        lakers.addPlayer(new TeamPlayer(nextPlayerId++, lakers.getId(), 2, "Cam Reddish"));
        lakers.addPlayer(new TeamPlayer(nextPlayerId++, lakers.getId(), 14, "Gabe Vincent"));
        lakers.addPlayer(new TeamPlayer(nextPlayerId++, lakers.getId(), 55, "Jarred Vanderbilt"));
        lakers.addPlayer(new TeamPlayer(nextPlayerId++, lakers.getId(), 17, "Christian Braun"));
        lakers.addPlayer(new TeamPlayer(nextPlayerId++, lakers.getId(), 4, "Dalton Knecht"));
    }
    
    private static void addWarriorsPlayers(Team warriors) {
        warriors.addPlayer(new TeamPlayer(nextPlayerId++, warriors.getId(), 30, "Stephen Curry"));
        warriors.addPlayer(new TeamPlayer(nextPlayerId++, warriors.getId(), 11, "Klay Thompson"));
        warriors.addPlayer(new TeamPlayer(nextPlayerId++, warriors.getId(), 23, "Draymond Green"));
        warriors.addPlayer(new TeamPlayer(nextPlayerId++, warriors.getId(), 22, "Andrew Wiggins"));
        warriors.addPlayer(new TeamPlayer(nextPlayerId++, warriors.getId(), 5, "Kevon Looney"));
        warriors.addPlayer(new TeamPlayer(nextPlayerId++, warriors.getId(), 20, "Gary Payton II"));
        warriors.addPlayer(new TeamPlayer(nextPlayerId++, warriors.getId(), 3, "Jordan Poole"));
        warriors.addPlayer(new TeamPlayer(nextPlayerId++, warriors.getId(), 4, "Moses Moody"));
        warriors.addPlayer(new TeamPlayer(nextPlayerId++, warriors.getId(), 00, "Jonathan Kuminga"));
        warriors.addPlayer(new TeamPlayer(nextPlayerId++, warriors.getId(), 32, "Trayce Jackson-Davis"));
        warriors.addPlayer(new TeamPlayer(nextPlayerId++, warriors.getId(), 12, "Lester Quinones"));
        warriors.addPlayer(new TeamPlayer(nextPlayerId++, warriors.getId(), 15, "Gui Santos"));
    }
    
    private static void addBullsPlayers(Team bulls) {
        bulls.addPlayer(new TeamPlayer(nextPlayerId++, bulls.getId(), 11, "DeMar DeRozan"));
        bulls.addPlayer(new TeamPlayer(nextPlayerId++, bulls.getId(), 9, "Nikola Vucevic"));
        bulls.addPlayer(new TeamPlayer(nextPlayerId++, bulls.getId(), 8, "Zach LaVine"));
        bulls.addPlayer(new TeamPlayer(nextPlayerId++, bulls.getId(), 6, "Alex Caruso"));
        bulls.addPlayer(new TeamPlayer(nextPlayerId++, bulls.getId(), 2, "Coby White"));
        bulls.addPlayer(new TeamPlayer(nextPlayerId++, bulls.getId(), 24, "Patrick Williams"));
        bulls.addPlayer(new TeamPlayer(nextPlayerId++, bulls.getId(), 22, "Andre Drummond"));
        bulls.addPlayer(new TeamPlayer(nextPlayerId++, bulls.getId(), 5, "Josh Giddey"));
        bulls.addPlayer(new TeamPlayer(nextPlayerId++, bulls.getId(), 3, "Dalen Terry"));
        bulls.addPlayer(new TeamPlayer(nextPlayerId++, bulls.getId(), 7, "Ayo Dosunmu"));
        bulls.addPlayer(new TeamPlayer(nextPlayerId++, bulls.getId(), 13, "Torrey Craig"));
        bulls.addPlayer(new TeamPlayer(nextPlayerId++, bulls.getId(), 12, "Jevon Carter"));
    }
    
    private static void addHeatPlayers(Team heat) {
        heat.addPlayer(new TeamPlayer(nextPlayerId++, heat.getId(), 22, "Jimmy Butler"));
        heat.addPlayer(new TeamPlayer(nextPlayerId++, heat.getId(), 13, "Bam Adebayo"));
        heat.addPlayer(new TeamPlayer(nextPlayerId++, heat.getId(), 1, "Tyler Herro"));
        heat.addPlayer(new TeamPlayer(nextPlayerId++, heat.getId(), 14, "Terry Rozier"));
        heat.addPlayer(new TeamPlayer(nextPlayerId++, heat.getId(), 55, "Duncan Robinson"));
        heat.addPlayer(new TeamPlayer(nextPlayerId++, heat.getId(), 25, "Kendrick Nunn"));
        heat.addPlayer(new TeamPlayer(nextPlayerId++, heat.getId(), 2, "Pelle Larsson"));
        heat.addPlayer(new TeamPlayer(nextPlayerId++, heat.getId(), 15, "Nikola Jovic"));
        heat.addPlayer(new TeamPlayer(nextPlayerId++, heat.getId(), 9, "Kevin Love"));
        heat.addPlayer(new TeamPlayer(nextPlayerId++, heat.getId(), 4, "Josh Richardson"));
        heat.addPlayer(new TeamPlayer(nextPlayerId++, heat.getId(), 3, "Dru Smith"));
        heat.addPlayer(new TeamPlayer(nextPlayerId++, heat.getId(), 12, "Kel'el Ware"));
    }
}
