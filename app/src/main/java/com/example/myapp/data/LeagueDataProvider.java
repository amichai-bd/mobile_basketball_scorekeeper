package com.example.myapp.data;

import com.example.myapp.models.Team;
import com.example.myapp.models.TeamPlayer;
import com.example.myapp.models.SimpleGame;
import java.util.ArrayList;
import java.util.List;

/**
 * LeagueDataProvider - Provides predefined team, player, and simple game data
 * Contains the 4 placeholder teams with 12 players each and available games
 */
public class LeagueDataProvider {
    
    private static List<Team> teams = null;
    private static List<SimpleGame> availableGames = null;
    private static int nextPlayerId = 1;
    private static int nextGameId = 1;
    
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
     * Get all available games for selection
     * @return List of simple games available to start
     */
    public static List<SimpleGame> getAvailableGames() {
        if (availableGames == null) {
            initializeGames();
        }
        return availableGames;
    }
    
    /**
     * Get game by ID
     */
    public static SimpleGame getGameById(int gameId) {
        for (SimpleGame game : getAvailableGames()) {
            if (game.getId() == gameId) {
                return game;
            }
        }
        return null;
    }
    
    /**
     * Remove game from available games list
     */
    public static boolean removeGame(int gameId) {
        if (availableGames != null) {
            for (int i = 0; i < availableGames.size(); i++) {
                if (availableGames.get(i).getId() == gameId) {
                    availableGames.remove(i);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Add new game to available games list
     */
    public static void addGame(SimpleGame game) {
        if (availableGames == null) {
            initializeGames();
        }
        availableGames.add(game);
    }
    
    /**
     * Add new team to teams list
     */
    public static void addTeam(Team team) {
        if (teams == null) {
            initializeTeams();
        }
        teams.add(team);
    }
    
    /**
     * Get next available game ID
     */
    public static int getNextGameId() {
        int maxId = 0;
        for (SimpleGame game : getAvailableGames()) {
            if (game.getId() > maxId) {
                maxId = game.getId();
            }
        }
        return maxId + 1;
    }
    
    /**
     * Get next available team ID
     */
    public static int getNextTeamId() {
        int maxId = 0;
        for (Team team : getTeams()) {
            if (team.getId() > maxId) {
                maxId = team.getId();
            }
        }
        return maxId + 1;
    }
    
    /**
     * Remove team from teams list (only if not used in games)
     */
    public static boolean removeTeam(int teamId) {
        // Check if team is used in any games
        if (availableGames != null) {
            for (SimpleGame game : availableGames) {
                if (game.getHomeTeam().getId() == teamId || game.getAwayTeam().getId() == teamId) {
                    return false; // Cannot remove - team is in use
                }
            }
        }
        
        // Remove team if not in use
        if (teams != null) {
            for (int i = 0; i < teams.size(); i++) {
                if (teams.get(i).getId() == teamId) {
                    teams.remove(i);
                    return true;
                }
            }
        }
        return false;
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
    
    /**
     * Initialize simple games for clean selection
     * Creates available games without status complexity
     */
    private static void initializeGames() {
        availableGames = new ArrayList<>();
        
        // Ensure teams are initialized
        if (teams == null) {
            initializeTeams();
        }
        
        // Get team references
        Team lakers = getTeamByName("Lakers");
        Team warriors = getTeamByName("Warriors");
        Team bulls = getTeamByName("Bulls");
        Team heat = getTeamByName("Heat");
        
        // Create simple available games for selection
        availableGames.add(new SimpleGame(nextGameId++, "15/12/2024", lakers, warriors));
        availableGames.add(new SimpleGame(nextGameId++, "16/12/2024", bulls, heat));
        availableGames.add(new SimpleGame(nextGameId++, "18/12/2024", lakers, bulls));
        availableGames.add(new SimpleGame(nextGameId++, "20/12/2024", warriors, heat));
        availableGames.add(new SimpleGame(nextGameId++, "22/12/2024", lakers, heat));
        availableGames.add(new SimpleGame(nextGameId++, "24/12/2024", warriors, bulls));
        availableGames.add(new SimpleGame(nextGameId++, "25/12/2024", bulls, lakers));
        availableGames.add(new SimpleGame(nextGameId++, "28/12/2024", heat, warriors));
    }
}
