# Basketball Statistics App - Development Specification & Status

## Project Overview
**Target**: MVP/POC Basketball Statistics Recording App
**Scope**: Solo mode only, 5-player teams, core statistics tracking
**Platform**: Android (Initial target)

---

## Technical Architecture

### Technology Stack
- **Frontend**: Android (Java/Kotlin)
- **Backend**: Local SQLite Database (Phase 1)
- **Future**: Firebase/Cloud backend (Phase 2)
- **UI Framework**: Native Android XML layouts
- **Development Environment**: Android Studio

### Database Schema (SQLite)

#### Games Table (Simplified)
```sql
CREATE TABLE games (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT NOT NULL, -- DD/MM/YYYY format
    home_team_id INTEGER NOT NULL, -- Reference to teams table
    away_team_id INTEGER NOT NULL, -- Reference to teams table
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (home_team_id) REFERENCES teams (id),
    FOREIGN KEY (away_team_id) REFERENCES teams (id)
);
```

#### Teams Table (League Teams)
```sql
CREATE TABLE teams (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE, -- 'Lakers', 'Warriors', etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Team Players Table (Team Rosters)
```sql
CREATE TABLE team_players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    team_id INTEGER NOT NULL,
    number INTEGER NOT NULL,
    name TEXT NOT NULL,
    UNIQUE(team_id, number), -- No duplicate numbers per team
    FOREIGN KEY (team_id) REFERENCES teams (id)
);
```

#### Game Players Table (Selected for specific game)
```sql
CREATE TABLE game_players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id INTEGER NOT NULL,
    team_player_id INTEGER NOT NULL,
    team_side TEXT NOT NULL, -- 'home' or 'away'
    is_on_court BOOLEAN DEFAULT TRUE, -- MVP: always true (5 players each)
    personal_fouls INTEGER DEFAULT 0,
    FOREIGN KEY (game_id) REFERENCES games (id),
    FOREIGN KEY (team_player_id) REFERENCES team_players (id)
);
```

#### Events Table
```sql
CREATE TABLE events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id INTEGER NOT NULL,
    player_id INTEGER, -- NULL for team events like timeouts
    team TEXT, -- 'home' or 'away' - for team events or player team reference
    quarter INTEGER NOT NULL,
    game_time INTEGER NOT NULL, -- seconds remaining
    event_type TEXT NOT NULL, -- '1P', '2P', '3P', '1M', '2M', '3M', 'OR', 'DR', 'AST', 'STL', 'BLK', 'TO', 'FOUL', 'TIMEOUT', 'SUB_IN', 'SUB_OUT'
    sub_player_out_id INTEGER, -- for substitution events
    sub_player_in_id INTEGER, -- for substitution events
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_id) REFERENCES games (id),
    FOREIGN KEY (player_id) REFERENCES players (id)
);
```

#### Team Fouls Table
```sql
CREATE TABLE team_fouls (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id INTEGER NOT NULL,
    team TEXT NOT NULL, -- 'home' or 'away'
    quarter INTEGER NOT NULL,
    foul_count INTEGER DEFAULT 0,
    UNIQUE(game_id, team, quarter),
    FOREIGN KEY (game_id) REFERENCES games (id)
);
```

### App Architecture (MVC Pattern)

#### Activities (Views)
1. **MainActivity** - Game schedule management
2. **GameRosterActivity** - Team roster setup  
3. **GameActivity** - Live game statistics recording
4. **SubstitutionActivity** - Player substitution (MVP: enabled)
5. **LogActivity** - Event log viewing/editing
6. **StatsActivity** - Statistics and reports

#### Models
1. **Game.java** - Game data model
2. **Team.java** - League team data model (Lakers, Warriors, etc.)
3. **TeamPlayer.java** - Team roster player data model (12 players per team)
4. **GamePlayer.java** - Selected players for specific game (5 per side)
5. **Event.java** - Game event data model
6. **DatabaseHelper.java** - SQLite operations
7. **StatsCalculator.java** - Statistics calculation utilities (basic counts only for MVP)
8. **TeamFoulTracker.java** - Team foul management

#### Controllers
1. **GameController.java** - Game state management
2. **EventController.java** - Event logging logic
3. **StatsController.java** - Statistics generation

---

## Technical Project Structure

### Android Project Layout
```
my_first_app/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/myapp/
│   │   │   ├── MainActivity.java          # Game schedule management
│   │   │   ├── GameRosterActivity.java   # Team roster setup
│   │   │   ├── GameActivity.java         # Live game recording
│   │   │   ├── SubstitutionActivity.java # Player subs (MVP: disabled)
│   │   │   ├── LogActivity.java          # Event log viewing
│   │   │   ├── StatsActivity.java        # Statistics reports
│   │   │   ├── models/
│   │   │   │   ├── Game.java             # Game data model
│   │   │   │   ├── Player.java           # Player data model
│   │   │   │   └── Event.java            # Event data model
│   │   │   ├── database/
│   │   │   │   └── DatabaseHelper.java   # SQLite operations
│   │   │   ├── controllers/
│   │   │   │   ├── GameController.java   # Game state management
│   │   │   │   ├── EventController.java  # Event logging
│   │   │   │   └── StatsController.java  # Statistics calculation
│   │   │   └── utils/
│   │   │       ├── StatsCalculator.java  # Statistics utilities
│   │   │       └── TeamFoulTracker.java  # Team foul management
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml     # Game schedule UI
│   │   │   │   ├── activity_game_roster.xml
│   │   │   │   ├── activity_game.xml     # Live game UI
│   │   │   │   ├── activity_substitution.xml
│   │   │   │   ├── activity_log.xml
│   │   │   │   └── activity_stats.xml
│   │   │   ├── values/
│   │   │   │   ├── strings.xml           # App strings
│   │   │   │   ├── colors.xml            # Color definitions
│   │   │   │   └── styles.xml            # UI styles
│   │   │   └── drawable/                 # Icons and graphics
│   │   └── AndroidManifest.xml           # App configuration
│   └── build.gradle                      # App dependencies
├── spec/
│   ├── specification.md                  # Functional specification
│   └── dev_spec_and_status.md           # This file
└── README.md                            # Project documentation
```

### Key Generated Files
- **R.java** - Auto-generated resource IDs
- **BuildConfig.java** - Build configuration constants  
- **SQLite Database** - Local storage at runtime
- **APK** - Compiled Android application

### External Dependencies
```gradle
// app/build.gradle
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.10.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.gridlayout:gridlayout:1.0.0' // For event button grid
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
}
```

### UI Layout Specifications

#### Game Activity Layout Structure
```xml
<!-- activity_game.xml layout hierarchy -->
ConstraintLayout (main container)
 ├── LinearLayout (top panel - horizontal)
 │   ├── TextView (score display)
 │   ├── TextView (game clock)
 │   └── TextView (current quarter)
 ├── LinearLayout (control panel - horizontal)
 │   ├── Button (start)
 │   ├── Button (stop)
 │   └── TextView (team fouls display)
 └── LinearLayout (main game area - horizontal)
     ├── LinearLayout (Team A panel - vertical)
     │   ├── TextView (team name)
     │   ├── RecyclerView (5 player buttons with foul counts)
     │   ├── Button (timeout)
     │   └── Button (substitution)
     ├── GridLayout (Event buttons - 4x4 grid)
     │   ├── Button (1P, 2P, 3P, AST in row 1)
     │   ├── Button (1M, 2M, 3M, OR in row 2)
     │   ├── Button (DR, STL, BLK, TO in row 3)
     │   └── Button (FOUL, TIMEOUT, [space], [space] in row 4)
     └── LinearLayout (Team B panel - vertical)
         ├── TextView (team name)
         ├── RecyclerView (5 player buttons with foul counts)
         ├── Button (timeout)
         └── Button (substitution)
```

#### Color Coding Standards
```xml
<!-- colors.xml -->
<color name="selected_player">#2196F3</color>      <!-- Blue -->
<color name="inactive_button">#BDBDBD</color>      <!-- Light Grey -->
<color name="active_button">#4CAF50</color>        <!-- Green -->
<color name="team_foul_normal">#000000</color>     <!-- Black -->
<color name="team_foul_penalty">#F44336</color>    <!-- Red (5+ fouls) -->
<color name="event_highlight">#2196F3</color>      <!-- Blue (3 sec flash) -->
```

#### Button Sizing Guidelines
- **Player Buttons**: 120dp width x 60dp height (touch-friendly)
- **Event Buttons**: 80dp x 80dp (square, large enough for quick tapping)
- **Control Buttons**: 100dp width x 50dp height
- **Minimum Touch Target**: 48dp (Android accessibility standard)
- **Spacing**: 8dp between buttons, 16dp panel margins

---

## Implementation Phases

### Phase 1: Core MVP Foundation
**Goal**: Working app that can record a complete game

#### Foundation Tasks
- [x] Project setup and architecture
- [x] Basic Game model class implemented
- [x] MainActivity UI layout (Frame 1) completed
- [x] Build system configured and dependency conflicts resolved
- [ ] Database schema implementation (using in-memory storage for MVP)
- [ ] Navigation between activities
- [ ] Remaining model classes (Player, Event, TeamFoul)
- [ ] DatabaseHelper implementation (planned for Phase 2)
- [ ] TeamFoulTracker utility class

#### Game Management Tasks
- [x] Game schedule creation/display (MainActivity) - **MVP version with simplified interface**
- [x] Basic game addition functionality with validation
- [x] Game list display with in-memory storage
- [ ] Team roster input - 5 players each (GameRosterActivity)
- [ ] Game state management (quarters, clock)  
- [ ] Score display functionality
- [ ] Full Game CRUD operations (edit, delete, database persistence)

#### Event Recording Tasks
- [ ] Event button implementation (13 event types plus timeout)
- [ ] Player selection workflow
- [ ] Real-time score calculation
- [ ] Team foul tracking and display
- [ ] Event logging to database (including team events)
- [ ] Basic substitution workflow (SubstitutionActivity)
- [ ] Pop-up workflows for assists, rebounds, steals
- [ ] Basic event log viewing (LogActivity)
- [ ] Live game UI with defined layout (GameActivity)

### Phase 2: Statistics & Polish
**Goal**: Complete statistics and user experience

- [ ] Statistics calculation engine - percentages and advanced metrics (StatsCalculator)
- [ ] Statistics reports and filtering (StatsActivity)
- [ ] Real-time shooting percentages display
- [ ] Data export capabilities
- [ ] UI polish and user experience improvements
- [ ] Error handling and edge cases
- [ ] Input validation
- [ ] Performance optimization
- [ ] Advanced analytics (efficiency ratings, trends)

### Phase 3: Future Features (Post-MVP)
**Goal**: Advanced features for production use

- [ ] Team mode implementation
- [ ] Advanced player management (bench, substitutions)
- [ ] Cloud synchronization
- [ ] Enhanced analytics and reporting
- [ ] Shot clock functionality
- [ ] Advanced foul types

---

## Current Development Status

### ✅ Completed
- Project structure and architecture setup
- **Game Model** - Basic data structure for game information
- **Build Configuration** - Resolved dependency conflicts, successful builds
- **Frame 1 Complete Refactor** - ✅ **SPECIFICATION ALIGNED** 
  - **ScheduledGame Model** - Links league teams to specific dates with status tracking
  - **League Database** - Extended LeagueDataProvider with 9 sample scheduled games
  - **UI Redesign** - Replaced manual game entry with game selection interface
  - **Edit League Button** - Added specification-compliant navigation (top right)
  - **Game Selection Logic** - User selects specific scheduled games from list
  - **Status Validation** - Only scheduled games can be started
  - **League Management Placeholder** - Future implementation framework ready
- **Frame 1 User Feedback Fixes** - ✅ **ALL FIXES COMPLETED**
  - **Game Selection Logic** - Fixed: Completed/in-progress games are no longer selectable
  - **State Management** - Fixed: Game status only changes when actual recording begins
  - **onResume Handling** - Fixed: Proper state reset when returning via back button
- **Frame 2 User Feedback Fixes** - ✅ **ALL FIXES COMPLETED**
  - **Team Pre-selection** - Fixed: Teams automatically selected from scheduled game
  - **UI Redesign** - Fixed: Removed team dropdowns, show pre-selected team names
  - **Player Selection** - Fixed: Immediate display of relevant team rosters
  - **Workflow Improvement** - Fixed: Streamlined from team selection to player selection only
- **Complete Data Models** - ✅ **SPECIFICATION ALIGNED**
  - **ScheduledGame Model** - League games with status tracking and team linking
  - **Team Models** - Team, TeamPlayer, LeagueDataProvider with 4 predefined teams
  - **Sample Data** - 9 scheduled games with various statuses for testing
- Specification documentation and cursor rule updates

### 🚧 In Progress  
- **READY FOR DEPLOYMENT**: Complete modern UI with jersey-style player cards and independent scrolling

### ⏳ Next Up
- **Frame 3 Implementation** - Live game recording interface (GameActivity)
  - Game clock and quarter management  
  - Event recording (13+ basketball events)
  - Player button interface for live statistics
  - Real-time score tracking
- **League Management Interface** - Complete implementation of Games/Teams/Players tabs
- Database implementation to replace in-memory storage

### 📋 **Latest Changes - Modern Design Completed**
- ✅ **Frame 1 Simplified**: Removed status complexity, clean card-based game selection
- ✅ **Frame 2 Modernized**: Complete UI overhaul with instant player selection
  - **Modern Player Cards**: Touch-friendly cards with instant highlighting
  - **Visual Ready States**: Team sections turn green when 5 players selected
  - **No Approve/Edit Buttons**: Instant selection workflow
  - **Auto-Enable Start Game**: Button turns green when both teams ready
  - **Selection Counters**: Live "X/5 selected" feedback
  - **Consistent Layout**: Single-column layout per team for better UX
  - **Fixed Height Cards**: 58dp consistent height with jersey-style number and name
  - **Jersey-Style Design**: Player number prominently displayed on top, name below
  - **Independent Scrolling**: Each team's player list scrolls independently
  - **Perfect Text Layout**: Numbers always in same position, names clearly readable
- ✅ **One-tap Flow**: Tap game → instant player selection → auto-ready detection
- ✅ **Perfect UX**: Consistent height, centered text, clean single-column layout

### ❌ Blocked/Issues
- None currently

### 📋 **Specification Compliance Notes**

**Frame 1 (Game Selection) Simplified Implementation:**
- ✅ **SPECIFICATION COMPLIANT**: Clean, simple game selection interface
- ✅ **Card-based Design**: Modern, touch-friendly game cards with team matchups and dates
- ✅ **One-tap Selection**: Tap any game card to immediately proceed to player selection
- ✅ **Clean UI**: Sleek design with proper colors, spacing, and typography
- ✅ **Edit League Button**: Small gear icon in top-right corner
- ✅ **No Status Complexity**: Removed complicated status tracking for cleaner experience
- ✅ **Instant Navigation**: No confirmation dialogs, immediate flow to next screen

**Frame 2 (Game Roster) Modern Implementation:**
- ✅ **SPECIFICATION COMPLIANT**: Sleek, modern player selection interface
- ✅ **Team Pre-selection**: Teams automatically displayed in modern card design
- ✅ **Instant Player Cards**: Touch-friendly grid of player cards with immediate highlighting
- ✅ **Visual Feedback**: Player cards highlight blue when selected, grey when unselected
- ✅ **Ready States**: Team sections turn green with "READY" text when 5 players selected
- ✅ **Auto-Enable Logic**: Start Game button automatically enables/changes color when both teams ready
- ✅ **Selection Counters**: Live "X/5 selected" feedback for each team
- ✅ **No Workflow Complexity**: Removed approve/edit buttons for instant selection
- ✅ **Modern Design**: Card-based layout with elevation, proper spacing, clean typography

---

## MVP Feature Scope

### ✅ Included in MVP
- **Game Schedule**: Create, edit games for season
- **Solo Mode**: Single user operation
- **5-Player Teams**: Fixed roster size with basic substitutions
- **Player Substitution**: Simple in/out player swapping during games
- **Core Events**: All 13 basketball events (1P, 2P, 3P, misses, rebounds, etc.)
- **Live Scoring**: Real-time score updates
- **Team Foul Tracking**: Per-quarter team foul counts with visual indicators
- **Basic Stats**: Event counts per player (percentages calculated in reports)
- **Event Log**: View and edit game events
- **Simple Reports**: Per-game and season statistics

### ❌ Excluded from MVP (Future Features)  
- **Team Mode**: Multi-device synchronization
- **Team/Player Management Screen**: Separate interface for editing league teams and player rosters
- **Advanced Roster**: Bench players beyond 12, fouling out management
- **Shot Clock**: 24-second timer
- **Advanced Fouls**: Technical, flagrant fouls
- **Overtime**: Extended game periods
- **Cloud Sync**: Data backup and sharing
- **Real-time Analytics**: Live efficiency ratings, shooting percentages
- **Advanced Statistics**: Heat maps, plus/minus, advanced metrics

---

## Key Assumptions & Decisions

### Technical Decisions
1. **Local-First**: SQLite database, no cloud dependency for MVP
2. **Single Platform**: Android only initially
3. **Native Development**: Android Java/XML (not cross-platform)

### Business Logic Decisions  
1. **League Teams**: 4 predefined teams (Lakers, Warriors, Bulls, Heat) with 12 players each
2. **Simple Game Selection**: Tap game card to proceed - no status complexity
3. **Game Roster**: Select exactly 5 players from each team's 12-player roster  
4. **Team Pre-selection**: Teams automatically selected from chosen game matchup
5. **10-Minute Quarters**: Standard amateur league timing
6. **Solo Operation**: Single device/user per game
7. **Simple Fouls**: Personal fouls only, no technical/flagrant
8. **Basic Timeouts**: Record timeout event, no duration tracking
9. **Statistics Approach**: Count events in real-time, calculate percentages in reports
10. **Team Fouls**: Track per-quarter, visual warning at 5+ fouls

### UI/UX Decisions
1. **Portrait Orientation**: Mobile-first design
2. **Large Buttons**: Easy tapping during live games
3. **Minimal Pop-ups**: Streamlined workflow
4. **Color Coding**: Blue for selected, grey for inactive

---

## Development Guidelines

### Code Standards
- Follow Android development best practices
- Use meaningful variable and method names
- Comment complex business logic
- Implement proper error handling

### Testing Strategy  
- Unit tests for statistics calculations
- Integration tests for database operations  
- Manual testing for UI workflows
- Game simulation testing with sample data

### Performance Requirements
- App startup < 3 seconds
- Event recording response < 500ms
- Statistics calculation < 2 seconds
- Database queries optimized with indexes

---

## Risk Assessment

### High Risk
- **Complex Event Workflow**: Player selection + event recording needs to be intuitive
- **Statistics Accuracy**: Basketball statistics have complex interdependencies

### Medium Risk  
- **UI Complexity**: 13+ buttons on mobile screen requires careful design
- **Data Integrity**: Preventing invalid game states/statistics

### Low Risk
- **Database Performance**: SQLite adequate for expected data volumes
- **Platform Compatibility**: Targeting recent Android versions only

---

## Success Metrics

### MVP Success Criteria
- [ ] Successfully record complete game (4 quarters) without crashes
- [ ] Generate accurate basic statistics for all players
- [ ] Support season-long game scheduling and tracking
- [ ] Intuitive UI requiring minimal training for basketball scorekeepers

### Performance Targets
- Support games up to 40 minutes duration
- Handle rosters up to 10 players per team (5 each)
- Store full season data (20+ games) without performance issues
- Export game data in standard formats

---

## Next Action Items

### High Priority Tasks
1. ✅ **COMPLETED**: Frame 1 (Game Schedule) complete refactor to specification alignment
2. ✅ **COMPLETED**: Frame 2 (Game Roster) specification-aligned implementation  
3. ✅ **COMPLETED**: ScheduledGame model and league database structure
4. ✅ **COMPLETED**: Edit League button and LeagueManagement placeholder
5. ✅ **COMPLETED**: All user feedback fixes (game selection logic, state management, team pre-selection)
6. **READY**: Mobile device testing and deployment verification
7. **NEXT**: Frame 3 (Live Game Recording) implementation

### Upcoming Tasks
1. Complete League Management interface implementation (Games/Teams/Players tabs)
2. Begin Frame 3 (Live Game Recording) implementation 
   - Game clock and quarter management
   - Event recording system (13+ basketball events)
   - Player button interface for live statistics
3. Database implementation to replace in-memory storage
4. Enhanced game state management and persistence
5. Statistics calculation and reporting functionality

### Partner Input Needed
- Validation of basketball rules implementation
- User acceptance testing with actual basketball games
- Feedback on event button layout and sizing
- Testing with actual game scenarios

**Last Updated**: December 2024 - After Jersey-Style Player Cards and Scrolling Implementation  
**Status**: Active Development - Phase 1 (Frame 1 & 2 Perfect Modern UI Complete, Ready for Testing & Frame 3)
