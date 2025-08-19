package com.example.myapp.data;

import com.example.myapp.models.Team;
import com.example.myapp.models.TeamPlayer;
import com.example.myapp.models.ScheduledGame;
import java.util.ArrayList;
import java.util.List;

/**
 * LeagueDataProvider - Provides predefined team, player, and scheduled game data
 * Contains the 4 placeholder teams with 12 players each and sample scheduled games
 */
public class LeagueDataProvider {
    
    private static List<Team> teams = null;
    private static List<ScheduledGame> scheduledGames = null;
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
     * Get all scheduled games
     * @return List of scheduled games with various statuses
     */
    public static List<ScheduledGame> getScheduledGames() {
        if (scheduledGames == null) {
            initializeScheduledGames();
        }
        return scheduledGames;
    }
    
    /**
     * Get scheduled game by ID
     */
    public static ScheduledGame getScheduledGameById(int gameId) {
        for (ScheduledGame game : getScheduledGames()) {
            if (game.getId() == gameId) {
                return game;
            }
        }
        return null;
    }
    
    /**
     * Get only scheduled (not started) games
     */
    public static List<ScheduledGame> getAvailableGames() {
        List<ScheduledGame> availableGames = new ArrayList<>();
        for (ScheduledGame game : getScheduledGames()) {
            if (game.isScheduled()) {
                availableGames.add(game);
            }
        }
        return availableGames;
    }
    
    /**
     * Update game status (for when game starts)
     */
    public static void updateGameStatus(int gameId, String newStatus) {
        ScheduledGame game = getScheduledGameById(gameId);
        if (game != null) {
            game.setStatus(newStatus);
        }
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
     * Initialize scheduled games with sample data
     * Creates games with different statuses for testing
     */
    private static void initializeScheduledGames() {
        scheduledGames = new ArrayList<>();
        
        // Ensure teams are initialized
        if (teams == null) {
            initializeTeams();
        }
        
        // Get team references
        Team lakers = getTeamByName("Lakers");
        Team warriors = getTeamByName("Warriors");
        Team bulls = getTeamByName("Bulls");
        Team heat = getTeamByName("Heat");
        
        // Create sample scheduled games (upcoming)
        scheduledGames.add(new ScheduledGame(nextGameId++, "15/12/2024", lakers, warriors));
        scheduledGames.add(new ScheduledGame(nextGameId++, "16/12/2024", bulls, heat));
        scheduledGames.add(new ScheduledGame(nextGameId++, "18/12/2024", lakers, bulls));
        scheduledGames.add(new ScheduledGame(nextGameId++, "20/12/2024", warriors, heat));
        scheduledGames.add(new ScheduledGame(nextGameId++, "22/12/2024", lakers, heat));
        scheduledGames.add(new ScheduledGame(nextGameId++, "24/12/2024", warriors, bulls));
        
        // Create sample completed games for demonstration
        ScheduledGame completedGame1 = new ScheduledGame(nextGameId++, "10/12/2024", lakers, bulls, 
            ScheduledGame.STATUS_COMPLETED, 112, 98);
        scheduledGames.add(completedGame1);
        
        ScheduledGame completedGame2 = new ScheduledGame(nextGameId++, "12/12/2024", warriors, heat, 
            ScheduledGame.STATUS_COMPLETED, 105, 110);
        scheduledGames.add(completedGame2);
        
        // Create one in-progress game for demonstration
        ScheduledGame inProgressGame = new ScheduledGame(nextGameId++, "14/12/2024", bulls, warriors);
        inProgressGame.setStatus(ScheduledGame.STATUS_IN_PROGRESS);
        scheduledGames.add(inProgressGame);
    }
}
