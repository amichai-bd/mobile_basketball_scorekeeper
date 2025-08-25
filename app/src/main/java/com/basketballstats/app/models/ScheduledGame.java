package com.basketballstats.app.models;

/**
 * ScheduledGame model for league scheduled games
 * Links league teams to specific game dates with status tracking
 */
public class ScheduledGame {
    public static final String STATUS_SCHEDULED = "Scheduled";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_COMPLETED = "Completed";
    
    private int id;
    private String date; // DD/MM/YYYY format
    private Team homeTeam;
    private Team awayTeam;
    private String status;
    private int homeScore;
    private int awayScore;
    
    // Constructor for new scheduled game
    public ScheduledGame(int id, String date, Team homeTeam, Team awayTeam) {
        this.id = id;
        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.status = STATUS_SCHEDULED;
        this.homeScore = 0;
        this.awayScore = 0;
    }
    
    // Constructor with all fields (for completed games)
    public ScheduledGame(int id, String date, Team homeTeam, Team awayTeam, 
                        String status, int homeScore, int awayScore) {
        this.id = id;
        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.status = status;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }
    
    // Getters
    public int getId() { return id; }
    public String getDate() { return date; }
    public Team getHomeTeam() { return homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public String getStatus() { return status; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
    
    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setScore(int homeScore, int awayScore) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }
    
    // Status check methods
    public boolean isScheduled() { return STATUS_SCHEDULED.equals(status); }
    public boolean isInProgress() { return STATUS_IN_PROGRESS.equals(status); }
    public boolean isCompleted() { return STATUS_COMPLETED.equals(status); }
    
    // Display format for list (shows date, teams, and status/score)
    @Override
    public String toString() {
        if (isCompleted()) {
            return String.format("%s: %s vs %s (%d-%d)", 
                date, homeTeam.getName(), awayTeam.getName(), homeScore, awayScore);
        } else {
            return String.format("%s: %s vs %s [%s]", 
                date, homeTeam.getName(), awayTeam.getName(), status);
        }
    }
    
    // Convert to old Game model for Frame 2 compatibility (temporary)
    public Game toGame() {
        return new Game(this.id, this.date, this.homeTeam.getName(), this.awayTeam.getName());
    }
}
