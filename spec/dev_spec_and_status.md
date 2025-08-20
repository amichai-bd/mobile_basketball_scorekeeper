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

#### Games Table (With Time Support)
```sql
CREATE TABLE games (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT NOT NULL, -- DD/MM/YYYY format
    time TEXT NOT NULL, -- HH:MM format (24-hour)
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
2. **GameActivity** - Live game statistics recording (includes Setup Mode and Unified Player Selection Modal)
3. **LogActivity** - Event log viewing/editing
4. **StatsActivity** - Statistics and reports
5. **~~GameRosterActivity~~** - *DEPRECATED: Functionality integrated into GameActivity as Setup Mode*
6. **~~SubstitutionActivity~~** - *DEPRECATED: Functionality integrated into Unified Player Selection Modal*

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
│   │   │   ├── GameActivity.java         # Live game recording (includes Setup Mode & Unified Modal)
│   │   │   ├── ~~GameRosterActivity.java~~ # DEPRECATED - integrated into GameActivity
│   │   │   ├── ~~SubstitutionActivity.java~~ # DEPRECATED - integrated into Unified Modal
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
│   │   │   │   ├── activity_game.xml     # Live game UI (dual mode: Setup/Game)
│   │   │   │   ├── dialog_unified_player_selection.xml # Unified player selection modal
│   │   │   │   ├── ~~activity_substitution.xml~~ # DEPRECATED - replaced by unified modal
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

#### Typography & Spacing Standards (Mobile-Optimized)
```xml
<!-- Mobile-friendly text sizes -->
Main titles: 24sp (reduced from 32sp)
Section titles: 14sp (reduced from 18sp) 
Modal headers: 16sp (reduced from 20sp)
Button text: 10-11sp (reduced from 12-13sp)
Player numbers: 16sp (reduced from 18-20sp)
Player names: 10sp (reduced from 11sp)

<!-- Compact spacing -->
Panel padding: 8-16dp (reduced from 20-24dp)
Button margins: 2dp (reduced from 3dp)
Layout margins: 4dp (reduced from 8dp)
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

#### Button Sizing Guidelines (Mobile-Optimized)
- **Player Cards**: 48dp height (reduced from 58dp) for regular cards
- **Modal Player Cards**: 75dp x 50dp (reduced from 100dp x 64dp) - 25% smaller for narrow screens
- **Event Buttons**: Auto-sized with reduced margins (2dp instead of 3dp)  
- **Control Buttons**: 32dp height (reduced from 36dp)
- **Modal Buttons**: 44dp height (reduced from 48dp)
- **Minimum Touch Target**: 48dp (Android accessibility standard) - maintained for tap areas
- **Spacing**: Reduced margins - 4dp panel margins (was 8dp), 2dp button margins (was 3dp)

#### Player Selection Modal Optimizations (Narrow Screen Support)
- **Layout**: Scrollable rows of 4 players (was fixed 3x4 grid) for better narrow screen compatibility
- **Scrolling**: ScrollView with 300dp max height supports unlimited players with scroll-through capability  
- **Flexible Width**: Cards use weight-based layout to adapt to any screen width
- **Compact Size**: 75dp x 50dp cards (was 100dp x 64dp) fit 4 columns on narrow screens
- **Touch-Friendly**: Maintained proper touch targets while maximizing screen space usage

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
- **Frame 2 Integration** - 🏗️ **PLANNED FOR REFACTOR**
  - **Player Selection Modal** - Planned: Will be accessed from GameActivity Setup Mode
  - **Independent Team Selection** - Planned: Each team's 5 players selected separately
  - **Quarter Lineup Changes** - Planned: Support for changing lineups between quarters
  - **Context Preservation** - Planned: User stays on game screen throughout
- **Complete Data Models** - ✅ **SPECIFICATION ALIGNED**
  - **ScheduledGame Model** - League games with status tracking and team linking
  - **Team Models** - Team, TeamPlayer, LeagueDataProvider with 4 predefined teams
  - **Sample Data** - 9 scheduled games with various statuses for testing
- Specification documentation and cursor rule updates

### ✅ **MAJOR MILESTONES COMPLETED**
- **League Management Interface** - ✅ **ENHANCED FULL IMPLEMENTATION**
  - **Games Tab**: Add games with smart date/time input (auto-formatting) and enhanced validation
  - **Games Management**: Edit/delete games with professional list items and confirmation dialogs
  - **Teams Tab**: Add teams with validation and edit/delete functionality
  - **Teams Management**: Professional team list with Edit/Delete/Players buttons
  - **Smart Input UX**: Auto-formatting for date (DD/MM/YYYY) and time (HH:MM) inputs
  - **Professional Navigation**: Improved gear button design with elevation and proper styling
  - **Tab Interface**: Two-tab layout (Games | Teams) with complete CRUD functionality
  - **Enhanced Validation**: Real-time format validation, duplicate checking, dependency validation

- **🏀 FRAME 3 (LIVE GAME RECORDING) - ✅ CORE FUNCTIONALITY COMPLETE!**
  - **Complete Event System**: All 13+ basketball events (1P, 2P, 3P, 1M, 2M, 3M, OR, DR, AST, STL, BLK, TO, FOUL, TIMEOUT)
  - **Quarter Management**: Q1-Q4 buttons with confirmation pop-ups and clock reset
  - **Game Clock**: MM:SS countdown with start/stop controls and visual feedback
  - **Score Tracking**: Live score updates from scoring events
  - **Foul System**: Personal fouls per player (with foul-out detection) and team fouls per quarter
  - **Player Selection**: Click player first, then event (with "Select player" validation)
  - **Visual Feedback**: Event buttons flash blue for 3 seconds as specified
  - **Team Panels**: 5-player buttons per team with foul counts displayed
  - **Event Workflows**: Simplified assist/rebound/steal workflows (MVP version)
  - **Professional UI**: Three-panel layout matching specification exactly

### 🚧 In Progress  
- **Testing Player Management** - Verify complete player management functionality works correctly

### ⏳ Next Up  
- **Event Logging System** - Database storage for recorded events and statistics
- **Enhanced Pop-up Workflows** - Full assist/rebound/steal pop-ups (if desired)
- **Statistics Reporting** - Frame 5 & 6 implementation
- Database implementation to replace in-memory storage

### 📋 **Latest Changes - Unified Modal Implementation & Context-Aware Enhancements**
- ✅ **UNIFIED MODAL IMPLEMENTED**: Complete working implementation with three modes
- ✅ **Frame 1 Navigation**: Direct navigation to GameActivity implemented
- ✅ **Frame 2 Integration**: Player selection now works as Setup Mode within GameActivity
- ✅ **Frame 4 Integration**: Substitutions use unified modal interface
- ✅ **Unified Player Selection Modal Working**: Single modal handles all scenarios:
  - **Setup Mode**: Select starting 5 players (0/5 → 5/5) ✅
  - **Quarter Change Mode**: Modify current lineup between quarters ✅
  - **Substitution Mode**: Replace players during game with flexible patterns ✅
- ✅ **Game Screen Dual Modes Implemented**: 
  - **Setup Mode**: Shows "Select 5 Players" buttons when no players selected
  - **Game Mode**: Full game functionality when both teams have 5 players
- ✅ **Independent Team UI**: Each team shows players immediately after selection
- ✅ **Context-Aware Button Enhancement**: Complete timer-based button context switching
  - **Timer-Based Context**: "Quarter Lineup" (10:00) vs "Sub" (<10:00) buttons ✅
  - **Independent Control**: Both teams modify lineups simultaneously ✅
  - **Mistake Recovery**: Fix setup errors before quarter starts ✅
  - **Dialog Elimination**: Removed quarter change dialog for cleaner UX ✅
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
- ✅ **League Management Complete**: Enhanced full-featured games and teams management
  - **Games Tab**: Add games with smart auto-formatting date/time inputs
  - **Games CRUD**: Full edit/delete functionality with professional list items
  - **Teams Tab**: Add new teams with validation and management actions
  - **Teams CRUD**: Edit/delete teams with dependency checking and confirmation dialogs
  - **Smart Input UX**: Auto-formatting for date (DD/MM/YYYY) and time (HH:MM)
  - **Professional UI**: Improved gear button, tab interface, action buttons
  - **Enhanced Validation**: Real-time format checking, business rule validation
  - **Data Synchronization**: Fixed critical bug - games deleted in League Management properly sync with home page
  - **Crash Prevention**: Added error handling for deleted games to prevent app crashes
- ✅ **🏀 Frame 3 Complete**: Live basketball statistics recording (THE CORE FUNCTIONALITY!)
  - **13+ Basketball Events**: Complete event system (scoring, misses, rebounds, assists, defense, violations)
  - **Quarter Management**: Q1-Q4 selection with confirmation and clock reset
  - **Game Clock**: Real-time countdown with start/stop controls
  - **Live Scoring**: Automatic score updates from recorded events
  - **Foul Tracking**: Personal fouls per player, team fouls per quarter with visual warnings
  - **Player Selection Workflow**: Click player → Click event → Record with visual feedback
  - **Quick Visual Feedback**: Buttons flash blue for 0.3 seconds (improved from 3s for better UX)
  - **Professional Layout**: Three-panel design (Team A | Events | Team B) matching specification
  - **Live Event Feed**: Always-visible last 5 events at bottom of Event Panel for context
  - **View Log Access**: One-tap access to complete event history (Frame 5)
- ✅ **Enhanced League Management - Player Management**: Complete team roster management
  - **Player Management Modal**: Full-screen modal overlay for team roster management ✅
  - **Add Players**: Jersey number (0-99) + name input with real-time validation ✅
  - **Edit Players**: Inline editing of existing player details with save/cancel ✅
  - **Delete Players**: Remove players with confirmation dialogs ✅
  - **Unlimited Rosters**: Support for unlimited players per team (no 12-player limit) ✅
  - **Empty Roster Default**: New teams start with empty rosters and proper empty state ✅
  - **Jersey Number Validation**: 0-99 range with uniqueness enforcement per team ✅
  - **Player Name Validation**: Required field validation with real-time feedback ✅
  - **Teams Integration**: "Manage Players" button integrated in Teams tab ✅
  - **Player Count Display**: Team list shows "(X players)" with live updates ✅
- ✅ **🔧 MOBILE UI OPTIMIZATION COMPLETE**: Comprehensive mobile compatibility improvements
  - **Reduced Heights**: Player cards 48dp (was 58dp), modal cards 75x50dp (was 100x64dp) ✅
  - **Compact Titles**: Main titles 24sp (was 32sp), section titles 14sp (was 18sp) ✅ 
  - **Smaller Buttons**: Control buttons 32dp (was 36dp), modal buttons 44dp (was 48dp) ✅
  - **Reduced Padding**: Panel padding 8-16dp (was 20-24dp), button margins 2dp (was 3dp) ✅
  - **Event Panel**: Reduced text sizes 10-11sp (was 13sp), tighter spacing ✅
  - **Modal Optimization**: All dialogs use less vertical space with compact layouts ✅
  - **Narrow Screen Support**: Player selection modal now scrollable with 4-column layout ✅
  - **Unlimited Players**: Scrollable interface supports teams with any number of players ✅
  - **Event Log Optimization**: Header reduced to single line with top-right back button ✅
  - **Home Page Streamlined**: Compact title, removed redundant "Select Game", smaller footer ✅
  - **Maintained Accessibility**: 48dp minimum touch targets preserved ✅
  - **Updated Specifications**: Mobile-friendly guidelines added to dev spec ✅
- ✅ **Complete App Flow**: Game selection → Player selection → Live game recording (FULL BASKETBALL STATS APP!)
- ✅ **Timer UX Enhanced**: Single toggle button design with pleasant colors and proper timer management  
- ✅ **Event Log Table Enhanced**: Professional table format with Quarter column and fixed-width columns
- ✅ **Single-Line Top Panel**: Ultra-compact layout with quarter dropdown for maximum space efficiency
- ✅ **Complete UI Polish**: Eliminated 3-line clutter, professional table format, quarter management dropdown

### ❌ Blocked/Issues
- None currently

### 🐛 **Critical Bugs Fixed**
- **Data Sync Issue**: Fixed crash when deleting games in League Management then tapping them on home page
- **Save Functionality Bug**: Fixed games/teams not actually being saved to data provider
- **Duplicate Game Bug**: Fixed games appearing twice when added, and both disappearing when one deleted
- **Data Corruption Bug**: Fixed games being deleted when opening League Management settings
- **Event Log Persistence Bug**: Fixed deleted events reappearing when navigating between screens
- **Button Color Visibility Bug**: Fixed OR, DR, AST buttons using blue (making blue flash invisible)
- **Timer Multiple-Start Bug**: Fixed timer running faster when START pressed multiple times
- **Timer UX Confusion**: Replaced confusing two-button system with single pleasant toggle
- **Top Panel Layout Clutter**: Reduced from 3-line layout to clean single-line design
- **Event Log Plain Text**: Replaced with professional table format with Quarter column
- **Root Causes**: 
  - Home page wasn't refreshing data when returning from League Management
  - addNewGame/addNewTeam methods only showed success messages without saving
  - Games/teams were being added to both local lists AND data provider, causing duplicates
  - onResume() method was corrupting data when League Management first opened
  - Event log worked with local copies instead of shared data storage
  - Multiple timers could run simultaneously without stopping previous ones
  - Red "PAUSE" button during gameplay created aggressive UX
- **Solutions**: 
  - Added proper data synchronization with onResume() refresh and LeagueDataProvider updates
  - Implemented actual save functionality with proper ID generation and data persistence
  - Fixed duplicate issue by using single source of truth (LeagueDataProvider) with proper refresh logic
  - Fixed data corruption by removing problematic onResume() refresh and adding safe initialization
  - Created shared static event storage that both GameActivity and LogActivity use
  - Changed default button colors to teal, reserved blue only for selected/active states
  - Implemented single timer management with proper cleanup of previous timers
  - Replaced two-button system with single pleasant toggle (Green→Blue, no red during gameplay)
  - Redesigned top panel from 3-line to clean single-line layout with quarter dropdown
  - Enhanced event log from plain text to professional table format with color-coded events
  - Added error handling for edge cases to prevent future crashes

### 📋 **Specification Compliance Notes**

**Frame 1 (Game Selection) Enhanced Implementation:**
- ✅ **SPECIFICATION COMPLIANT**: Clean, simple game selection interface
- ✅ **Card-based Design**: Modern, touch-friendly game cards with team matchups and dates
- ✅ **One-tap Selection**: Tap any game card to immediately proceed to game screen
- ✅ **Direct Game Entry**: Skip intermediate screens, go straight to GameActivity
- ✅ **Clean UI**: Sleek design with proper colors, spacing, and typography
- ✅ **Edit League Button**: Small gear icon in top-right corner
- ✅ **No Status Complexity**: Removed complicated status tracking for cleaner experience
- ✅ **Instant Navigation**: No confirmation dialogs, immediate flow to game screen

**Frame 2 (Game Roster) Planned Integration:**
- 📋 **PLANNED REFACTOR INTO FRAME 3**: Player selection to be integrated as Setup Mode in GameActivity
- 📋 **Modal Overlay Planned**: Clean modal interface for player selection without leaving game screen
- 📋 **Independent Selection Planned**: Each team's 5 players selected separately via their own modal
- 📋 **Instant Player Cards Specified**: Touch-friendly grid of player cards with immediate highlighting
- 📋 **Visual Feedback Specified**: Player cards highlight blue when selected, grey when unselected
- 📋 **Ready States Planned**: Team panels show selected players once confirmed
- 📋 **Quarter Lineup Changes Specified**: Support for changing entire lineups between quarters
- 📋 **Context Preservation Planned**: User stays on game screen throughout selection process
- 📋 **Modern Design Specified**: Card-based modal with elevation, proper spacing, clean typography

**Frame 3 (Live Game Recording) Enhanced Specification:**
- ✅ **CURRENT IMPLEMENTATION**: Full basketball statistics recording (existing functionality)
- 📋 **PLANNED DUAL MODE OPERATION**: 
  - **Setup Mode Planned**: Player selection interface when no players chosen
  - **Game Mode Current**: Full game functionality when both teams have 5 players
- ✅ **Complete Event System**: All 13+ basketball events implemented with proper workflows
- ✅ **UI Layout**: Three-panel design (Team A | Event Panel | Team B) as specified
- ✅ **Clock Management**: Game clock, start/stop buttons, quarter selection with pop-ups
- ✅ **Score Tracking**: Live score updates from scoring events (1P, 2P, 3P)
- ✅ **Foul System**: Personal fouls (with foul-out detection) and team fouls (with penalty warnings)
- ✅ **Player Workflow**: Select player first, then event (with validation)
- ✅ **Visual Feedback**: Event buttons flash blue for 3 seconds as specified
- ✅ **Event Workflows**: Simplified assist/rebound/steal workflows (MVP version)
- ✅ **Professional Design**: Color-coded event buttons, proper spacing, touch-friendly layout

---

## MVP Feature Scope

### ✅ Included in MVP
- **Game Schedule**: Create, edit games for season
- **Solo Mode**: Single user operation
- **5-Player Teams**: Fixed roster size with flexible substitutions
- **Unified Player Management**: Single modal interface for all player selection scenarios
- **Context-Aware Buttons**: Timer-based "Quarter Lineup" vs "Sub" button context
- **Independent Team Control**: Both teams can modify lineups simultaneously
- **Mistake Recovery**: Fix initial setup errors before quarter starts
- **Flexible Substitutions**: Support for any valid substitution pattern (1-for-1, 2-for-2, 3-for-3, etc.)
- **Quarter Lineup Changes**: Modify entire lineups between quarters without dialogs
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
2. **Direct Game Entry**: Tap game card to go directly to game screen - no intermediate screens
3. **Setup Mode**: Game screen starts with player selection interface when no players chosen
4. **Unified Player Management**: Single modal interface for all player selection scenarios
5. **Game Roster**: Select exactly 5 players from each team's 12-player roster
6. **Independent Team Selection**: Each team's 5 players selected separately via modal
7. **Context-Aware Buttons**: Timer-based button context (10:00 = "Quarter Lineup", <10:00 = "Sub")
8. **Flexible Substitutions**: Support for any valid substitution pattern (1-for-1, 2-for-2, etc.)
9. **Quarter Lineup Changes**: Independent team control before quarter starts (timer = 10:00)
10. **Mistake Recovery**: Allow fixing initial setup errors before quarter begins
11. **10-Minute Quarters**: Standard amateur league timing
12. **Solo Operation**: Single device/user per game
13. **Simple Fouls**: Personal fouls only, no technical/flagrant
14. **Basic Timeouts**: Record timeout event, no duration tracking
15. **Statistics Approach**: Count events in real-time, calculate percentages in reports
16. **Team Fouls**: Track per-quarter, visual warning at 5+ fouls

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
2. ✅ **COMPLETED**: ScheduledGame model and league database structure
3. ✅ **COMPLETED**: League Management interface with Games and Teams tabs
4. ✅ **COMPLETED**: All user feedback fixes (game selection logic, state management, team pre-selection)
5. ✅ **COMPLETED**: Modern UI improvements (jersey-style cards, scrolling, visual feedback)
6. ✅ **COMPLETED**: 🏀 **FRAME 3 (LIVE GAME RECORDING)** - Complete core basketball statistics functionality
7. ✅ **COMPLETED**: Unified Modal Architecture Specification - Complete design document
8. ✅ **COMPLETED**: Implement Unified Player Selection Modal with three modes:
   - **Setup Mode**: Initial 5 player selection (0/5 → 5/5) ✅
   - **Quarter Change Mode**: Modify current lineup between quarters ✅
   - **Substitution Mode**: Replace players during game (flexible patterns) ✅
9. ✅ **COMPLETED**: Update GameActivity to support dual modes (Setup/Game) with unified modal integration
10. ✅ **COMPLETED**: Remove/deprecate GameRosterActivity and SubstitutionActivity
11. ✅ **COMPLETED**: Update MainActivity navigation to go directly to GameActivity
12. ✅ **COMPLETED**: Implement Frame 2 Integration into Frame 3 as Setup Mode
13. **IN PROGRESS**: Enhanced League Management - Player Management Modal
    - **Player Management Interface**: Modal overlay for team roster management
    - **CRUD Operations**: Add, edit, delete players with validation
    - **Jersey Numbers**: 0-99 range with uniqueness per team
    - **Unlimited Rosters**: No maximum player limit per team
    - **Game Dependencies**: Prevent deletion of players used in games
14. **READY**: Mobile device testing and deployment verification of complete app flow

### Upcoming Tasks
1. **Event Logging & Database** - Persistent storage for recorded game events and statistics
2. **Enhanced Pop-up Workflows** - Full assist/rebound/steal pop-ups (currently simplified for MVP)
3. **Frame 5 (Event Log)** - View and edit recorded game events
4. **Frame 6 (Statistics)** - Game and season statistics reporting
5. **Enhanced Player Management** - Complete roster management within Teams tab
6. **Performance Optimization** - Database queries, UI responsiveness, memory management
7. **Unified Modal Polish** - Advanced features like preset lineups, formation templates

### Partner Input Needed
- Validation of basketball rules implementation
- User acceptance testing with actual basketball games
- Feedback on event button layout and sizing
- Testing with actual game scenarios

**Last Updated**: December 2024 - Unified Modal Architecture: Specifications Complete, Implementation Pending
**Status**: Active Development - Phase 1 (📋 PLANNING COMPLETE: Ready to implement unified modal for all player management scenarios)
