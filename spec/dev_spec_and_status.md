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

#### Games Table
```sql
CREATE TABLE games (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    league_name TEXT NOT NULL,
    date TEXT NOT NULL,
    home_team TEXT NOT NULL,
    away_team TEXT NOT NULL,
    home_score INTEGER DEFAULT 0,
    away_score INTEGER DEFAULT 0,
    status TEXT DEFAULT 'scheduled', -- scheduled, in_progress, completed
    current_quarter INTEGER DEFAULT 1,
    game_time INTEGER DEFAULT 600, -- seconds (10 minutes)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Players Table
```sql
CREATE TABLE players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id INTEGER NOT NULL,
    team TEXT NOT NULL, -- 'home' or 'away'
    number INTEGER NOT NULL,
    name TEXT NOT NULL,
    is_on_court BOOLEAN DEFAULT TRUE, -- MVP: always true (5 players each)
    personal_fouls INTEGER DEFAULT 0,
    FOREIGN KEY (game_id) REFERENCES games (id)
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
2. **Player.java** - Player data model  
3. **Event.java** - Game event data model
4. **DatabaseHelper.java** - SQLite operations
5. **StatsCalculator.java** - Statistics calculation utilities (basic counts only for MVP)
6. **TeamFoulTracker.java** - Team foul management

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
- [ ] Database schema implementation (including team fouls table)
- [ ] Basic UI layouts for all 6 activities
- [ ] Navigation between activities
- [ ] Model classes (Game, Player, Event, TeamFoul)
- [ ] DatabaseHelper implementation
- [ ] TeamFoulTracker utility class

#### Game Management Tasks
- [ ] Game schedule creation/editing (MainActivity)
- [ ] Team roster input - 5 players each (GameRosterActivity)
- [ ] Game state management (quarters, clock)
- [ ] Score display functionality
- [ ] Game CRUD operations

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
- Project structure created
- Basic MainActivity template
- SQLite database helper scaffolding
- Specification documentation

### 🚧 In Progress  
- Database schema finalization
- UI layout design for game schedule

### ⏳ Next Up
- Complete database implementation
- Game schedule CRUD operations
- Basic navigation setup

### ❌ Blocked/Issues
- None currently

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
- **Advanced Roster**: Bench players beyond 5, fouling out management
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
1. **Simple Roster**: 5 active players per team with basic substitution support
2. **10-Minute Quarters**: Standard amateur league timing
3. **Solo Operation**: Single device/user per game
4. **Simple Fouls**: Personal fouls only, no technical/flagrant
5. **Basic Timeouts**: Record timeout event, no duration tracking
6. **Statistics Approach**: Count events in real-time, calculate percentages in reports
7. **Team Fouls**: Track per-quarter, visual warning at 5+ fouls

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

### Immediate (This Week)
1. Finalize database schema implementation
2. Create basic UI layouts for Game Schedule frame
3. Implement game CRUD operations
4. Set up navigation between activities

### Short Term (Next 2 Weeks)
1. Complete roster management functionality  
2. Implement game clock and scoring system
3. Design and implement event recording UI
4. Basic event logging functionality

### Partner Input Needed
- Validation of basketball rules implementation
- User acceptance testing with actual basketball games
- Feedback on event button layout and sizing
- Testing with actual game scenarios

**Last Updated**: December 2024  
**Status**: Active Development - Phase 1
