# Basketball Statistics App Specification

## Overview

This app is designed for recording basketball statistics in minor and amateur leagues. Due to budget, manpower, and infrastructure limitations, most games currently track only the final score. Our app makes it possible to capture full game statistics using just one or two people, their own smartphones, and a simple, intuitive interface. The focus is on a smooth workflow and a clear UI that allows even a single person to record as many stats as possible with high accuracy.

### General Features
- **Undo**: Any event saved to log can be undone by swiping screen to the left. When this happens, an approval pop-up will show to make sure that the swipe was intentional.

---

## Frame 1 – Game Selection

### Description
Simple, clean interface for selecting a game to start recording statistics. User chooses from pre-configured matchups and proceeds directly to the game screen.

### Components

#### Main Title
- **Description**: Main title of app
- **Type**: Text
- **Location**: Top center
- **Content**: "Basketball Stats"
- **Clickable**: No

#### Edit League Button
- **Description**: Professional settings button to access league management
- **Type**: Modern Icon Button  
- **Location**: Top right corner
- **Content**: Settings icon with subtle styling
- **Design**: Round background, proper elevation, professional appearance
- **Clickable**: Yes
- **When clicked**: Navigate to League Management interface

#### Game Selection Title
- **Description**: Section title
- **Type**: Text
- **Location**: Center under main title
- **Content**: "Select Game"
- **Clickable**: No

#### Game List
- **Description**: Clean list of available games to start
- **Type**: Simple List
- **Location**: Center of screen
- **Format**: Large, touch-friendly cards showing:
  - **Team A vs Team B**
  - **Date**
- **Clickable**: Yes – Tap any game to proceed
- **Selection**: Single tap immediately proceeds to game screen (Frame 3)
- **Design**: Card-based layout with clear typography, generous spacing

#### Quick Info
- **Description**: Brief instruction text
- **Type**: Text (small, subtle)
- **Location**: Bottom of screen
- **Content**: "Tap a game to start recording"
- **Clickable**: No

### Flow
1. User sees clean list of available games
2. User taps any game card
3. Automatically proceeds to game screen in Setup Mode (Frame 3)
4. **No status tracking, no mode selection, no confirmation dialogs**

### Simplified Design Principles
- **One-tap selection**: No separate "Start Game" button needed
- **Clean cards**: Each game displayed as a clear, touch-friendly card
- **Minimal text**: Only essential information (teams, date)
- **No status complexity**: All games are simply available for selection
- **Instant navigation**: Tap game → go directly to game screen

---

## Frame 2 – Game Roster (Integrated into Frame 3)

### Description
**NOTE: This functionality is now integrated directly into Frame 3 (Game Screen) as "Setup Mode"**

Player selection is now handled within the game screen itself through modal overlays. When entering a game without players selected, the game screen presents player selection buttons in place of the player list. This allows for more flexible lineup management, including quarter-by-quarter lineup changes.

### League Teams Data (MVP)
**4 Placeholder Teams with Player Rosters:**
- **Lakers**: 12 players with numbers and names
- **Warriors**: 12 players with numbers and names  
- **Bulls**: 12 players with numbers and names
- **Heat**: 12 players with numbers and names

### Components

#### Main Title
- **Description**: Clean main title
- **Type**: Text
- **Location**: Top center
- **Content**: "Select Players"
- **Clickable**: No

#### Team A Section

##### Team A Display
- **Description**: Shows the pre-selected home team with modern card design
- **Type**: Team Card
- **Location**: Left side of screen
- **Content**: "[Team Name] (Home)" with team branding
- **Design**: Card-based layout with team colors
- **Clickable**: No
- **Background**: Dynamic - changes to green when 5 players selected ("Ready" state)

##### Team A Player Grid
- **Description**: Scrollable grid of player cards for instant selection
- **Type**: Scrollable Card Grid
- **Location**: Left side under team display
- **Clickable**: Yes - Instant select/deselect
- **Content**: 12 player cards showing jersey number and name (vertical layout)
- **Scrolling**: Independent scroll for Team A players
- **Design**: 
  - **Unselected**: Light grey background, dark text
  - **Selected**: Blue background, white text (highlighted)
  - **Card Style**: Jersey-style with number on top, name below
  - **Layout**: Single column with consistent height cards
- **Validation**: Maximum 5 players can be selected
- **Feedback**: 
  - Player cards highlight immediately on tap
  - Team section background turns green when exactly 5 selected
  - Selection counter shows "X/5 selected"

#### Team B Section

##### Team B Display  
- **Description**: Shows the pre-selected away team with modern card design
- **Type**: Team Card
- **Location**: Right side of screen
- **Content**: "[Team Name] (Away)" with team branding
- **Design**: Card-based layout with team colors
- **Clickable**: No
- **Background**: Dynamic - changes to green when 5 players selected ("Ready" state)

##### Team B Player Grid
- **Description**: Scrollable grid of player cards for instant selection
- **Type**: Scrollable Card Grid
- **Location**: Right side under team display
- **Clickable**: Yes - Instant select/deselect
- **Content**: 12 player cards showing jersey number and name (vertical layout)
- **Scrolling**: Independent scroll for Team B players
- **Design**: Same styling as Team A player grid
- **Validation**: Maximum 5 players can be selected
- **Feedback**: Same instant feedback as Team A

#### Start Game Button
- **Description**: Large, prominent button that auto-enables when both teams ready
- **Type**: Button
- **Location**: Bottom center, full width
- **Content**: "Start Game"
- **Design**: Large, modern button with rounded corners
- **Clickable**: Yes (auto-enables when both teams have exactly 5 players selected)
- **Visual States**:
  - **Disabled**: Grey background when teams not ready
  - **Enabled**: Green background when both teams ready
- **When clicked**: Proceed directly to live game recording (Frame 3)

#### Start Game Pop-up
- **Description**: A pop-up frame that asks if you are sure you want to start the game
- **Type**: Pop-up
- **Location**: Center
- **Content**:
  - "Are you sure you want to start game?"
  - Yes button
  - No button
- **Clickable**: Yes
- **When clicking Yes button**:
  - Pop-up shows "Game Time!!!"
  - Game status changes to "In Progress"
  - Go to Game frame (live recording interface)
- **When clicking No button**:
  - Return to Game Roster frame
  - Game status remains "Scheduled" (user can return later to complete setup)

### Flow
1. Frame automatically displays pre-selected teams from chosen game in modern card design
2. User sees Team A (Home) and Team B (Away) team cards with player grids
3. User taps player cards to select/deselect - immediate visual highlighting
4. Team A section background turns green when exactly 5 players selected ("Ready")
5. User taps Team B player cards to select/deselect players
6. Team B section background turns green when exactly 5 players selected ("Ready")  
7. "Start Game" button automatically enables when both teams show "Ready" state
8. User taps "Start Game" to proceed to live game recording interface

### Modern Design Principles
- **Instant feedback**: Player selection highlights immediately on tap
- **Visual status**: Team sections show "Ready" state with green background
- **No workflow complexity**: No approve/edit buttons needed
- **Auto-enabling**: Start Game button enables automatically when ready
- **Touch-friendly**: Large player cards optimized for mobile tapping

### Components (When accessed as modal from Frame 3)

The original Frame 2 components are now available as a modal overlay within the game screen, triggered by clicking "Select 5 Players" buttons in Setup Mode. The same instant selection interface with team cards and player grids is maintained.

### Future Feature: Team/Player Management
- **Separate screen** for editing league teams and player rosters
- **Add/remove teams** from league
- **Edit player details** (numbers, names) for each team
- **Roster management** (add/remove players from teams)
- **NOT implemented during game setup** - teams/players managed separately

---

## Frame 3 - Game

### Description
This is the main screen where the live updates happen. The screen now has two distinct modes:

**Setup Mode**: When entering from game selection without players chosen, the team panels show "Select 5 Players" buttons instead of player lists. Event buttons are disabled until both teams have 5 players selected.

**Game Mode**: Once both teams have 5 players selected, all game features become active. The user will click buttons to start and pause time, select players and events to log, click for time out or substitute players etc. Each team will be on one side and the event buttons in the middle for easy and fast clicking.

### UI Layout Structure

#### Setup Mode (No Players Selected)
```
┌─────────────────────────────────────────────────────┐
│  [Score: 0 - 0] [START|10:00] [Q1▼] [Fouls: 0-0]  │
├─────────────────────────────────────────────────────┤
│ Team A        │   Event Panel       │    Team B     │
│               │    (Disabled)       │               │
│  ┌─────────┐  │                     │  ┌─────────┐ │
│  │         │  │  Events disabled    │  │         │ │
│  │ Select  │  │  until both teams   │  │ Select  │ │
│  │    5    │  │  have 5 players     │  │    5    │ │
│  │ Players │  │  selected           │  │ Players │ │
│  │         │  │                     │  │         │ │
│  └─────────┘  │                     │  └─────────┘ │
│               │                     │               │
└─────────────────────────────────────────────────────┘
```

#### Game Mode (Players Selected)
```
┌─────────────────────────────────────────────────────┐
│  [Score: Team A 45 - Team B 38] [START|8:45] [Q2▼] [Fouls: A-3 B-5] │
├─────────────────────────────────────────────────────┤
│ Team A        │    Event Panel      │    Team B     │
│               │                     │               │
│ Player 1 [3]  │  [1P] [2P] [3P]    │  [2] Player 6 │
│ Player 2 [0]  │  [1M] [2M] [3M]    │  [1] Player 7 │
│ Player 3 [1]  │  [OR] [DR] [AST]   │  [4] Player 8 │
│ Player 4 [2]  │  [STL][BLK][TO ]   │  [0] Player 9 │
│ Player 5 [0]  │  [FOUL] [TIMEOUT]  │  [3] Player 10│
│               │                     │               │
│ [TimeOut]     │                     │  [TimeOut]    │
│ [Sub]         │  [Change Lineup]    │  [Sub]        │
└─────────────────────────────────────────────────────┘
```
**Optimized Single-Line Layout**:
- **Single-Line Top Panel**: Maximum space efficiency and clean design
- **Left**: Score Display
- **Center-Left**: Game Control Button + Clock
- **Center-Right**: Quarter Dropdown (Q1/Q2/Q3/Q4 selection)
- **Right**: Team Fouls Display
- Numbers in brackets [ ] next to players show personal fouls
- Team fouls color coded (red when ≥5)
- Event buttons in center 3x4 grid plus timeout
- Player buttons on sides with foul counts
- Team action buttons (TimeOut, Sub) at bottom of each panel

### Top Control Panel
**Description**: Ultra-compact single-line control panel with all game state and control information.
**Location**: Top of screen, above the team panels and event button panel.

#### Layout Structure:
- **Single Line**: Score Display | Game Control Button + Clock | Quarter Dropdown | Team Fouls Display

#### Game Score Display
- **Description**: Live game score for both teams
- **Type**: Text Display
- **Location**: Middle-top section, next to game clock
- **Content**: "Team A: XX - Team B: XX" format
- **Clickable**: No
- **Updates**: Automatically when scoring events are recorded

#### Game Control Button (Single Toggle)
- **Description**: Single button that toggles between start and pause game timer
- **Type**: Toggle Button
- **Location**: Top middle section
- **Content & Visual States**:
  - **When Timer Stopped**: 
    - Content: "START"
    - Background: 🟢 Green ("Ready to Start")
    - State: Available/clickable
  - **When Timer Running**: 
    - Content: "PAUSE"
    - Background: 🔵 Blue ("Ready to Pause" - pleasant during gameplay)
    - State: Available/clickable
- **When clicked (Timer Stopped)**:
  - Start or continue game clock (ensure only ONE timer runs)
  - Button text changes to "PAUSE GAME"
  - Button background becomes blue
  - Clock background becomes green (running state)
- **When clicked (Timer Running)**:
  - Stop game clock (clear any existing timers)
  - Button text changes to "START GAME"
  - Button background becomes green
  - Clock background becomes yellow (paused state)

#### Game Clock
- **Description**: Game clock with visual state indication
- **Type**: Time Display with Background
- **Location**: Middle section, next to score display
- **Content**: Clock (MM:SS format)
- **Background Color States**:
  - **🟢 Green**: Timer is running (game active)
  - **🟡 Yellow**: Timer is paused (game stopped)
- **Clickable**: Yes – long click to edit time if needed
- **When clicked**: Only if long-clicked, user may edit the time

#### Quarter Dropdown
- **Description**: Dropdown selector for current quarter with automatic progression
- **Type**: Spinner/Dropdown
- **Location**: Center-right of single control line
- **Content**: "Q1", "Q2", "Q3", "Q4" options
- **Clickable**: Yes
- **Current Quarter Display**: Shows current quarter (e.g., "Q2") with dropdown arrow
- **When clicked**: Opens dropdown to select different quarter
- **Auto-Progression**: When timer reaches 0:00, automatically advances to next quarter and stops timer
- **Reset Behavior**: Selecting new quarter resets clock to 10:00 (stopped state)

#### Team Fouls
- **Description**: Showing the number of fouls committed by the team per quarter. Once it reaches over 5 there is a FT for any foul
- **Location**: Under start and stop button
- **Content**: Label "TF" content – number of team fouls recorded this quarter
- **Clickable**: No
- **Design**: Number of fouls turns Red from 5 fouls or more

#### Quarter Selection Behavior
- **Direct Selection**: Tap dropdown → select quarter → immediate change (no confirmation)
- **Clock Reset**: Selecting new quarter automatically resets clock to 10:00 and stops timer
- **Auto-Advance**: When timer reaches 0:00:
  - Automatically advance to next quarter (Q1→Q2→Q3→Q4)
  - Reset clock to 10:00
  - Stop timer (user must press START for next quarter)
  - Show notification: "Quarter X Complete! Starting Quarter Y"

### Team Panel (Team A & Team B)
**Description**: Team panels adapt based on whether players are selected:

**Setup Mode**: Shows a large "Select 5 Players" button in place of the player list. Clicking this button opens a modal overlay with the player selection interface.

**Game Mode**: Shows the 5 selected players as buttons, one on top of the other. Big and clear for easy clicking. Team buttons Time out and Subs will be there as well.

#### Player Title
- **Description**: Title of team panel
- **Type**: Text
- **Location**: Top left corner
- **Content**: Team name
- **Clickable**: No


#### Select Players Button (Setup Mode Only)
- **Description**: Large button shown when no players are selected for this team
- **Type**: Button
- **Location**: Center of team panel where player buttons would normally be
- **Content**: "Select 5 Players"
- **Clickable**: Yes
- **When clicked**: Opens modal overlay with player selection interface
- **Visual State**: 
  - Default: Grey background
  - After selection: Replaced with 5 player buttons

#### Player Buttons (Game Mode Only)
- **Description**: 5 buttons, one for each player. These buttons will be used to log the events by clicking on player and then the event that the player did
- **Type**: Button
- **Location**: Side panel. 5 buttons spread evenly one on top of the other
- **Content**: Player name and player number (automatically filled from selected roster)
- **Clickable**: Yes
- **When clicked**: Colors button turns blue


#### Time Out Button
- **Description**: When a time out is called the user will click the button of the team that called the timeout
- **Type**: Button
- **Location**: Side panel, on top of players
- **Content**: "Time Out"
- **Clickable**: Yes
- **When clicked**:
  - Event recorded in log
  - If clock is running, pause the clock
  - Game control toggle shows "START" (green)
  - Clock background turns yellow (paused state)

#### Sub Button
- **Description**: When a player is substituted, the user will click on the Sub button to substitute the player on the system
- **Type**: Button
- **Location**: Side panel, bottom of team section
- **Content**: "Sub"
- **Clickable**: Yes
- **When clicked**: Go to Substitution frame (Frame 4)

#### Personal Foul Title
- **Description**: Title
- **Type**: Text Label
- **Location**: Side panel, on top of the PF numbers
- **Content**: "PF"
- **Clickable**: No

#### Personal Foul Label
- **Description**: Sums the number of fouls a player has recorded in a game. If a player has 5 fouls he is fouled out
- **Type**: Text Label
- **Location**: Side panel, next to player button
- **Content**: Number of personal fouls
- **Clickable**: No



### Event Panel
**Description**: The event panel has the buttons of the events and a live event feed. The panel adapts based on game state:

**Setup Mode**: All event buttons are disabled/greyed out with a message "Select players for both teams to start recording events"

**Game Mode**: When an event appears in the game, the user will click a player and then an event, and it will be stored in the log and shown in the live feed. An event button can be clicked only if a player button is selected. If there is no player button selected, a pop-up will state "Select player". The panel will be between the team panels and under the time panel.

#### Live Event Feed
- **Description**: Shows the last 5 recorded events for immediate feedback and context
- **Location**: Bottom of Event Panel, below event buttons
- **Content**: List showing "Time - Player - Event" format (e.g., "8:45 - #23 LeBron - 2P")
- **Updates**: Automatically when events are recorded
- **Format**: Most recent event at top, scrolls down
- **Team Events**: Shows team name instead of player (e.g., "8:30 - Lakers - TIMEOUT")

#### View Full Log Button
- **Description**: Button to access complete game event log
- **Location**: Bottom right of Event Panel
- **Content**: "View Log"
- **Clickable**: Yes
- **When clicked**: Navigate to Event Log screen (Frame 5) showing all recorded events

#### Scoring Events

##### 1P Button
- **Description**: When a player makes a foul line shot (1 point)
- **Type**: Button
- **Location**: Top left side of the panel
- **Content**: "1P"
- **Clickable**: Yes
- **When clicked**:
  - Button flashes Blue for 0.3 seconds (quick feedback)
  - Event added to live event feed at bottom of Event Panel
  - Tooltip: "1P recorded in log for player [name and number]" (disappears after 2 seconds)
  - All players unclicked and turn grey

##### 2P Button
- **Description**: When a player makes a field goal worth 2 points
- **Type**: Button
- **Location**: Top left side of the panel, next to 1P
- **Content**: "2P"
- **Clickable**: Yes
- **When clicked**:
  - Button flashes Blue for 0.3 seconds (quick feedback)
  - Event added to live event feed at bottom of Event Panel
  - Tooltip: "2P recorded in log for player [name and number]" (disappears after 2 seconds)
  - All players unclicked and turn grey
  - Assist pop-up enabled

##### 3P Button
- **Description**: When a player makes a 3 point shot worth 3 points
- **Type**: Button
- **Location**: Top left side of the panel, next to 2P
- **Content**: "3P"
- **Clickable**: Yes
- **When clicked**:
  - Button flashes Blue for 0.3 seconds (quick feedback)
  - Event added to live event feed at bottom of Event Panel
  - Tooltip: "3P recorded in log for player [name and number]" (disappears after 2 seconds)
  - All players unclicked and turn grey
  - Assist pop-up enabled

##### Assist Button
- **Description**: After a player scores 2 or 3 points, an assist will be recorded
- **Type**: Button
- **Content**: "Assist"
- **Clickable**: Yes
- **When clicked**:
  - Button flashes Blue for 0.3 seconds (quick feedback)
  - Event added to live event feed at bottom of Event Panel
  - Tooltip: "Assist recorded in log for player [name and number]" (disappears after 2 seconds)
  - All players unclicked and turn grey

##### Assist Pop-up (Consider removing to avoid pop-ups)
- **Description**: After a basket is made and 2P or 3P are clicked, a pop-up will open with the names of the team on offense that made the basket
- **Type**: Pop-up
- **Location**: Center
- **Content**:
  - Title: "Assist?"
  - 5 buttons of the players on court for the offensive team
  - Large button on the bottom: "No assist"
- **When clicking player**:
  - Player turns blue
  - Tooltip: "Assist recorded for [player name and number]" (disappears after 3 seconds)
  - Pop-up disappears after 1 second
- **When clicking No assist**: Go back to Game frame. No action

#### Miss Events
##### 1M Button
- **Description**: When a player misses a foul line shot
- **Type**: Button
- **Location**: Under 1P button
- **Content**: "1M"
- **Clickable**: Yes
- **When clicked**:
  - Button flashes Blue for 0.3 seconds (quick feedback)
  - Event added to live event feed at bottom of Event Panel
  - Tooltip: "1M recorded in log for player [name and number]" (disappears after 2 seconds)
  - All players unclicked and turn grey
  - Enable Rebound pop-up

##### 2M Button
- **Description**: When a player misses field goal 2 point shot
- **Type**: Button
- **Location**: Under 2P button
- **Content**: "2M"
- **Clickable**: Yes
- **When clicked**:
  - All other event buttons turn grey
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "2M recorded in log for player [name and number]" (disappears after 3 seconds)
  - All players unclicked and turn grey
  - Enable Rebound pop-up

##### 3M Button
- **Description**: When a player misses 3 point shot
- **Type**: Button
- **Location**: Under 3P button
- **Content**: "3M"
- **Clickable**: Yes
- **When clicked**:
  - All other event buttons turn grey
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "3M recorded in log for player [name and number]" (disappears after 3 seconds)
  - All players unclicked and turn grey
  - Enable Rebound pop-up

#### Rebound Events
##### OR Button (Offensive Rebound)
- **Description**: After a missed shot if the offense catches the rebound
- **Type**: Button
- **Content**: "OR"
- **Clickable**: Yes
- **When clicked**:
  - All other event buttons turn grey
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "OR recorded in log for player [name and number]" (disappears after 3 seconds)
  - All players unclicked and turn grey

##### DR Button (Defensive Rebound)
- **Description**: After a missed shot if the defense catches the rebound
- **Type**: Button
- **Content**: "DR"
- **Clickable**: Yes
- **When clicked**:
  - All other event buttons turn grey
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "DR recorded in log for player [name and number]" (disappears after 3 seconds)
  - All players unclicked and turn grey

##### Rebound Pop-up (Consider removing to avoid pop-ups)
- **Description**: After a miss, a rebound will likely be caught. A pop-up will appear to help with fast rebound selecting
- **Type**: Pop-up
- **Location**: Center
- **Content**:
  - Title: "Rebound?"
  - 5 buttons of players on left for team A and 5 on right for team B
  - Large button on bottom: "No rebound"
- **When clicking player**:
  - Player turns blue
  - If player is on offense, marks offensive rebound. If on defense, marks defensive rebound
  - Tooltip: "Offensive/Defensive rebound recorded for [player name and number]" (disappears after 3 seconds)
  - Pop-up disappears after 1 second
- **When clicking No rebound**: Go back to Game frame. No action

#### Other Events

##### Turnover Button
- **Description**: When a turnover happens
- **Type**: Button
- **Content**: "Turnover"
- **Clickable**: Yes
- **When clicked**:
  - All other event buttons turn grey
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "Turnover recorded in log for player [name and number]" (disappears after 3 seconds)
  - All players unclicked and turn grey
  - Enable Steal pop-up

##### Steal Button
- **Description**: Records a steal
- **Type**: Button
- **Content**: "Steal"
- **Clickable**: Yes
- **When clicked**:
  - All other event buttons turn grey
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "Steal recorded in log for player [name and number]" (disappears after 3 seconds)
  - All players unclicked and turn grey

##### Steal Pop-up (Consider removing to avoid pop-ups)
- **Description**: After a turnover is recorded, there is usually a steal that caused the turnover
- **Type**: Pop-up
- **Location**: Center
- **Content**:
  - Title: "Steal?"
  - 5 buttons of the opposite team of the player who committed the turnover
  - Large button on bottom: "No steal"
- **When clicking player**:
  - Player turns blue
  - Tooltip: "Steal recorded for [player name and number]" (disappears after 3 seconds)
  - Pop-up disappears after 1 second
- **When clicking No steal**: Go back to Game frame. No action
##### Block Button
- **Description**: Records a defensive block
- **Type**: Button
- **Content**: "Block"
- **Clickable**: Yes
- **When clicked**:
  - All other event buttons turn grey
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "Block recorded in log for player [name and number]" (disappears after 3 seconds)
  - All players unclicked and turn grey

##### Foul Button
- **Description**: Records a foul
- **Type**: Button
- **Content**: "Foul"
- **Clickable**: Yes
- **When clicked**:
  - All other event buttons turn grey
  - Button flashes Blue for 0.3 seconds (quick feedback)
  - Event added to live event feed at bottom of Event Panel
  - Tooltip: "Foul recorded in log for player [name and number]" (disappears after 2 seconds)
  - All players unclicked and turn grey

### Flow

#### Setup Mode Flow
1. User enters game screen from Frame 1 game selection
2. Both team panels show "Select 5 Players" buttons
3. User clicks Team A's "Select 5 Players" button
4. Modal overlay appears with Team A's roster (12 players)
5. User selects exactly 5 players with instant visual feedback
6. Modal closes, Team A panel now shows 5 player buttons
7. User repeats for Team B
8. Once both teams have 5 players, event buttons enable and game enters Game Mode

#### Game Mode Flow  
1. The game begins and user clicks Q1, the time will show 10 minutes and start
2. When an event happens the user clicks on the player and then on the event
3. Event is recorded with quick 0.3-second button flash and appears in live event feed
4. If there is a follow up event like rebound or steal the user clicks on the player for follow up
5. The system records the event in the log and live feed shows last 5 events
6. User can tap "View Log" to see complete event history
7. User will apply substitutions and time outs when they happen

#### Quarter Transition Flow
1. When transitioning between quarters (Q1→Q2, Q2→Q3, Q3→Q4)
2. Optional prompt: "Starting Quarter X - Change lineup?"
3. Options:
   - **"Keep Same"** - Continue with current 5 players
   - **"Change Lineup"** - Opens Unified Modal in Quarter Change Mode with current 5 pre-selected

### Enhanced UX Principles
- **Quick Visual Feedback**: 0.3-second button flash for immediate confirmation
- **Live Event Feed**: Always visible last 5 events for context and verification
- **Complete Log Access**: One-tap access to full event history with edit/delete functionality
- **Minimal Disruption**: Fast feedback allows focus to stay on live game action
- **Clear Timer State**: Clock background color immediately shows running (green) vs paused (yellow)
- **Pleasant Single Toggle**: Single game control button eliminates UI complexity
- **Intuitive Button Colors**: 
  - 🟢 Green = "START" (positive action when stopped) / "ON COURT" (current players)
  - 🔵 Blue = "PAUSE" (pleasant during gameplay) / "SELECTED/GOING IN" (player states)
  - 🔴 Red = "COMING OUT" (substitution only)
  - 🩶 Grey = Available/unselected players
  - Clock background = Primary state indicator (green=running, yellow=paused)
- **Always Available Events**: All basketball events can be recorded regardless of timer state
- **Progressive Mode Transition**: Game screen naturally progresses from Setup Mode to Game Mode
- **Unified Player Management**: Single modal interface for all player selection scenarios
- **Learn Once, Use Everywhere**: Same interaction pattern for setup, quarter changes, and substitutions
- **Flexible Lineup Management**: Support for any substitution pattern (1-for-1, 2-for-2, 3-for-3, etc.)
- **Context-Aware Interface**: Modal adapts its display and behavior based on usage scenario

### Unified Player Selection Modal

#### Description
A single, versatile modal that handles all player selection scenarios: initial setup, quarter lineup changes, and substitutions. Same UI, different contexts - clean and consistent.

#### Core Modal Components
- **Context Header**: Shows scenario-specific title
- **Player Grid**: 12 player cards in clean 3x4 layout
- **Status Display**: Shows current selection state
- **Single Action Button**: Context-aware button text
- **Cancel Button**: "Cancel" (returns without changes)

#### Three Usage Modes

##### **Setup Mode** (New Game/Team Setup)
- **Header**: "[Team Name] - Select Starting 5"
- **Player State**: All players grey (unselected)
- **Status Display**: "0/5 selected"
- **Selection**: Tap to select (blue), tap again to deselect
- **Validation**: Must select exactly 5 players
- **Action Button**: "Set Lineup" (enabled when exactly 5 selected)

##### **Quarter Change Mode** (Between Quarters)
- **Header**: "[Team Name] - Quarter X Lineup"
- **Player State**: Current 5 pre-selected (blue), others grey
- **Status Display**: "5/5 selected" (can modify)
- **Selection**: Tap to add/remove players freely
- **Validation**: Must end with exactly 5 players
- **Action Button**: "Update Lineup" (enabled when exactly 5 selected)

##### **Substitution Mode** (During Game)
- **Header**: "[Team Name] - Substitution"
- **Player State**: Current 5 shown as "ON COURT" (green), others available (grey)
- **Status Display**: "Making substitution..."
- **Selection**: 
  - Tap ON COURT player → "COMING OUT" (red)
  - Tap available player → "GOING IN" (blue)
- **Validation**: Equal numbers in/out (1-for-1, 2-for-2, etc.)
- **Action Button**: "Make Substitution" (enabled when valid swap)

#### Universal Selection Behavior
- **Visual States**:
  - **Grey**: Available to select
  - **Blue**: Selected/Going in
  - **Green**: Currently on court (Substitution mode only)
  - **Red**: Coming out (Substitution mode only)
- **Instant Feedback**: Cards change color immediately on tap
- **Clear Status**: Always shows what's happening
- **Simple Interaction**: Just tap players to change their state

#### Benefits of Unified Approach
- **Learn Once, Use Everywhere**: Same interaction pattern for all scenarios
- **Clean UI**: Single modal design, no UI complexity
- **Flexible**: Supports any substitution pattern (1-for-1, 2-for-2, 3-for-3)
- **Fast**: Familiar interface means quick decisions
- **Context-Aware**: Header and status adapt to show relevant information

---

## Frame 4 – Substitutions (Unified Modal)

### Description
**INTEGRATED WITH UNIFIED PLAYER SELECTION MODAL**: Substitutions now use the same clean modal interface as initial setup and quarter changes. No separate substitution screen needed.

### How It Works

#### Accessing Substitutions
- **From Game Screen**: Click "Sub" button in either team panel
- **Opens**: Unified Player Selection Modal in **Substitution Mode**
- **Context**: Modal shows team name and "Substitution" in header

#### Substitution Interface (Using Unified Modal)
- **Current Players**: 5 players on court shown in GREEN ("ON COURT")
- **Available Players**: 7 bench players shown in GREY ("AVAILABLE")
- **Simple Interaction**:
  1. Tap GREEN player → turns RED ("COMING OUT")
  2. Tap GREY player → turns BLUE ("GOING IN")
  3. Must have equal numbers: 1 out + 1 in, or 2 out + 2 in, etc.
- **Status Display**: "Making substitution..." with validation feedback
- **Action Button**: "Make Substitution" (enabled when valid swap)

#### Substitution Flow
1. User clicks "Sub" button on team panel in game screen
2. Unified Modal opens in Substitution Mode
3. User taps current player(s) to remove (GREEN → RED)
4. User taps bench player(s) to bring in (GREY → BLUE)
5. User clicks "Make Substitution"
6. Modal closes, game updates with new lineup
7. Substitution event logged automatically

#### Benefits of Unified Approach
- **No Separate Screen**: Substitutions happen in familiar modal interface
- **Consistent UX**: Same interaction as setup and quarter changes
- **Flexible**: Supports 1-for-1, 2-for-2, or any valid substitution pattern
- **Visual Clarity**: Color coding makes intentions crystal clear
- **Fast**: No navigation between screens, familiar interface

### Removed Components
- **~~Separate Substitution Activity~~**: No longer needed
- **~~Sub Out/In Button Lists~~**: Replaced by unified player grid
- **~~Back to Game Button~~**: Modal closes automatically
- **~~Complex Workflow~~**: Simplified to tap-and-confirm

---


## Frame 5 – Log

### Description
Can view and edit the log of events

### Components

#### Main Title
- **Description**: Main title of frame
- **Type**: Text
- **Location**: Top left corner
- **Content**: "Event log"
- **Clickable**: No

#### Event Log Table
- **Description**: Professional table-format display of all recorded game events with management actions
- **Type**: Interactive Table with Fixed Columns
- **Layout Structure**:
  ```
  ┌────────────────────────────────────────────────┐
  │ Q  │ Time  │ Player        │ Event │ Actions  │
  ├────────────────────────────────────────────────┤
  │ Q1 │ 9:30  │ #23 LeBron    │  2P   │ [Edit][Del] │
  │ Q1 │ 9:15  │ #30 Curry     │  3P   │ [Edit][Del] │
  │ Q2 │ 8:45  │ Lakers        │TIMEOUT│ [Edit][Del] │
  └────────────────────────────────────────────────┘
  ```
- **Table Columns**:
  - **Quarter Column (40dp)**: Q1, Q2, Q3, Q4 - shows which quarter event occurred
  - **Time Column (50dp)**: MM:SS format when event happened
  - **Player Column (flexible)**: "#Number Player Name" or "Team Name" (for team events)
  - **Event Column (50dp)**: Event type with color coding
  - **Actions Column (108dp)**: Edit and Delete buttons
- **Header Row**: Fixed header with column names for clear table structure
- **Color Coding**: Events color-coded by type (green=scoring, red=misses, blue=stats, etc.)
- **Fixed Column Widths**: Consistent alignment regardless of name length
- **Actions per Event**:
  - **Edit Button**: Modify event details (placeholder for MVP)
  - **Delete Button**: Remove event with confirmation dialog
- **Professional UX**: Clean table format eliminates text alignment issues

---

## Frame 6 – Game Stats

### Description
Can view the game statistics with sorting, grouping and filter options by game, season, player etc.

### Components

#### Main Title
- **Description**: Main title of frame
- **Type**: Text
- **Location**: Top left corner
- **Content**: "Stat Sheet"
- **Clickable**: No

#### Player Per Game Button
- **Description**: Opens up player per game table
- **Type**: Button
- **Content**: "Player per game"
- **Clickable**: Yes
- **When clicked**:
  - Player per game stats table will appear
  - Button turns blue, rest of buttons turn light grey

#### Player Per Game Stats Table
- **Description**: Stats of the player per game throughout the season. Table can be sorted and filtered according to any column
- **Type**: Table
- **Clickable**: Filter and sorting
- **Columns**:
  - `#` - Player number
  - `Player name`
  - `Team`
  - `MP` - Minutes played (Time)
  - `Pts` - Points (Number)
  - `FT` - Free throw % (Made/Attempts) e.g., 33% (3/9)
  - `2P` - 2-point % (Made/Attempts) e.g., 33% (3/9)
  - `3P` - 3-point % (Made/Attempts) e.g., 33% (3/9)
  - `Ast` - Assists (Number)
  - `R` - Total rebounds (Number)
  - `OR` - Offensive rebounds (Number)
  - `DR` - Defensive rebounds (Number)
  - `St` - Steals (Number)
- **Note**: All columns get the sum of the event per game

#### Player Season Numbers Button
- **Description**: Opens up player season numbers table
- **Type**: Button
- **Content**: "Player season numbers"
- **Clickable**: Yes
- **When clicked**:
  - Player season numbers stats table will appear
  - Button turns blue, rest of buttons turn light grey

#### Player Season Numbers Stats Table
- **Description**: Stats of the player per season. The accumulation of stats throughout the season. Table can be sorted and filtered according to any column
- **Type**: Table
- **Clickable**: Filter and sorting
- **Columns**: Same as Player Per Game Stats Table
- **Note**: All event columns get the sum of the events per season

#### Player Season Average Button
- **Description**: Opens up player season average table
- **Type**: Button
- **Content**: "Player season average"
- **Clickable**: Yes
- **When clicked**:
  - Player season average stats table will appear
  - Button turns blue, rest of buttons turn light grey

#### Player Season Average Stats Table
- **Description**: Stats of the player per season. The average of stats throughout the season. Table can be sorted and filtered according to any column
- **Type**: Table
- **Clickable**: Filter and sorting
- **Columns**: Same as Player Per Game Stats Table
- **Note**: All event columns get the season average

### Flow
1. User clicks on one of the 3 modes
2. User scrolls through the stat report
3. User filters and sorts for desired view

---

## League Management Interface

### Description
Separate interface for managing league data (games, teams, players). Accessed via "Edit League" button from Frame 1. This is where league administrators set up and maintain the season structure.

### Components

#### Main Title
- **Description**: Main title of interface
- **Type**: Text
- **Location**: Top center
- **Content**: "League Management"
- **Clickable**: No

#### Management Tabs
- **Description**: Tab interface to switch between different management sections
- **Type**: Tab Layout
- **Location**: Under main title
- **Content**: Two tabs - "Games", "Teams" (Players integrated into Teams tab)
- **Clickable**: Yes

### Games Management Tab

#### Add Game Section
- **Description**: Form to add new scheduled games with enhanced UX
- **Components**:
  - Team A Dropdown (select from league teams)
  - Team B Dropdown (select from league teams, different from Team A)
  - Smart Date Input (auto-formatting: DD/MM/YYYY with auto-slashes)
  - Smart Time Input (auto-formatting: HH:MM with auto-colon)
  - Add Game Button
- **Input UX**:
  - **Date**: Auto-adds slashes after day/month (e.g., typing "151224" becomes "15/12/24")
  - **Time**: Auto-adds colon after hour (e.g., typing "1430" becomes "14:30")
  - **Validation**: Real-time format validation, future date checking
- **Validation**: Cannot select same team twice, valid date/time format required

#### Scheduled Games List
- **Description**: Interactive list of all scheduled games with full management
- **Format**: Custom list items showing "DD/MM/YYYY HH:MM - Team A vs Team B"
- **Actions per Game**:
  - **Edit Button**: Modify game details (teams, date, time)
  - **Delete Button**: Remove game with confirmation
- **Functionality**: 
  - Tap Edit → Populate form fields with game data for editing
  - Tap Delete → Confirmation dialog → Remove from list
  - Clear, professional list appearance

### Teams Management Tab

#### Add Team Section
- **Description**: Form to add new teams to the league
- **Components**:
  - Team Name Input Field
  - Add Team Button
- **Validation**: Team name must be unique
- **Behavior**: After adding team, automatically switch to player management for that team

#### Teams List with Management
- **Description**: Interactive list of all league teams with management actions
- **Format**: Team list items showing "Team Name (X players)"
- **Actions per Team**:
  - **Edit Button**: Rename team with validation
  - **Delete Button**: Remove team (only if not used in scheduled games)
  - **Manage Players Button**: Access player roster management (future feature)
- **Functionality**:
  - Tap Edit → Inline editing of team name
  - Tap Delete → Confirmation dialog → Remove if not in use
  - Professional list appearance with clear action buttons

#### Back to Schedule Button
- **Description**: Return to main game schedule
- **Type**: Button
- **Location**: Bottom of interface
- **Content**: "Back to Schedule"
- **When clicked**: Save all changes and return to Frame 1

### League Management Flow
1. User accesses via gear icon "⚙️" button from Frame 1
2. User selects appropriate tab (Games or Teams)
3. **Games Tab**: Add/edit scheduled games with teams, dates, and times
4. **Teams Tab**: Add/edit teams and manage each team's player roster
5. Changes are automatically saved to league database
6. User returns to Frame 1 with updated schedule/teams/players

### Navigation Flow
- **From Frame 1**: Tap gear icon "⚙️" → League Management interface
- **Within Management**: Switch between Games and Teams tabs
- **Return**: Back button or "Back to Schedule" → Frame 1

---

## Future Features (Post-MVP)

### Team Mode
- **Description**: Multi-user mode where multiple people can record statistics simultaneously
- **Features**:
  - Real-time synchronization between devices
  - Role-based access (scorekeeper, assistant, etc.)
  - Conflict resolution for simultaneous entries
  - Enhanced UI optimized for team collaboration

### Advanced Player Management
- **Description**: Enhanced roster and substitution management beyond MVP
- **Features**:
  - Bench players (more than 5 per team)  
  - Starting lineup selection from full roster
  - Advanced substitution tracking with statistics
  - Player rotation optimization recommendations
  - Foul-out handling with automatic forced substitutions
  - Player fatigue tracking and alerts
  - Coach decision support and analytics

### Enhanced Game Features
- **Shot Clock**: 24-second shot clock implementation
- **Advanced Fouls**: Technical fouls, flagrant fouls, coach fouls
- **Overtime Support**: Multiple overtime periods
- **Timeout Management**: Full timeout, 20-second timeout tracking
- **Game Replay**: Step-by-step game event replay
- **Live Commentary**: Text notes during game events

### Statistics & Analytics
- **Advanced Stats**: Player efficiency rating, plus/minus, heat maps
- **Team Analytics**: Possession analysis, pace statistics
- **Comparison Tools**: Player vs player, team vs team comparisons
- **Export Options**: PDF reports, CSV data, integration with league management systems











