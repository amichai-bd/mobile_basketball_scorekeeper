# Basketball Statistics App Specification

## Overview

This app is designed for recording basketball statistics in minor and amateur leagues. Due to budget, manpower, and infrastructure limitations, most games currently track only the final score. Our app makes it possible to capture full game statistics using just one or two people, their own smartphones, and a simple, intuitive interface. The focus is on a smooth workflow and a clear UI that allows even a single person to record as many stats as possible with high accuracy.

### General Features
- **Undo**: Any event saved to log can be undone by swiping screen to the left. When this happens, an approval pop-up will show to make sure that the swipe was intentional.

---

## Frame 1 – Game Schedule

### Description
User will input the game schedule details for the whole season

### Components

#### Main Title
- **Description**: Main title of frame
- **Type**: Text
- **Location**: Top left corner
- **Content**: Summer league name
- **Clickable**: No

#### Table Title
- **Description**: Table title
- **Type**: Text
- **Location**: Top left corner under main title
- **Content**: "Game schedule"
- **Clickable**: No

#### Game List Table
- **Description**: A list of games for the season with details for each game. A row for each game
- **Type**: Table
- **Location**: Under game schedule title starting from left to right
- **Clickable**: Partially – Buttons edit and save only
- **Columns**:
  - Game ID
  - Game Date
  - Home team name
  - Away team name
  - W/L
  - Score
  - Edit and save buttons
  - Add row and delete row buttons
- **Cell content**: User input
- **Details**:
  - User clicks edit to enable editing
  - User clicks save to save data and disable editing
  - User cannot edit a row after game starts


#### Start Game Button
- **Description**: Button that when clicked will start the process of a new game
- **Type**: Button
- **Location**: Right of the game list table. Top of button in line with top of table
- **Content**: "Start game"
- **Clickable**: Yes
- **When clicked**:
  - Game row on game list turns grey
  - Enable mode pop-up
  - Create new game log table
  - Create new game roster tables

#### Mode Pop-up (MVP: Solo Mode Only)
- **Description**: For MVP/POC, only Solo mode is supported. Team mode is a future feature.
- **Type**: Pop-up
- **Location**: Center
- **Content**:
  - Solo button – automatically selected
  - OK button
- **Clickable**: Yes
- **When clicking OK button**:
  - Pop-up disappears
  - Go to game roster frame

### Flow
1. User fills in the cells with data
2. User marks a row to start game
3. Click start game button
4. Solo mode is automatically selected (MVP)
5. Click OK to go to game roster

---

## Frame 2 – Game Roster

### Description
In this frame the user inserts the rosters of each team into the system. For MVP/POC, each team has exactly 5 players (no bench management).

### Components

#### Main Title
- **Description**: Main title of frame
- **Type**: Text
- **Location**: Top left corner
- **Content**: "Game roster"
- **Clickable**: No

#### Team A Section

##### Team A Table Title
- **Description**: Team A table title
- **Type**: Text
- **Location**: Top left under main title
- **Content**: Team listed as home in the game schedule table
- **Clickable**: No

##### Team A Table
- **Description**: A list of players on the game roster. To fill in by user before game
- **Type**: Table
- **Location**: Left side under team A table title
- **Clickable**: No
- **Columns**:
  - Player number
  - Player name
- **Cell content**: User input
- **Details**: Game roster cannot change once game starts

##### Approve Roster Button (Team A)
- **Description**: When user completes filling in roster it will be saved after clicking button
- **Type**: Button
- **Location**: Under team A table
- **Content**: "Approve roster"
- **Clickable**: Yes
- **When clicked**:
  - Table turns light grey
  - User cannot edit

##### Edit Roster Button (Team A)
- **Description**: User clicks the button to enable editing the roster table
- **Type**: Button
- **Location**: Under team A table
- **Content**: "Edit roster"
- **Clickable**: Yes
- **When clicked**:
  - Enables editing roster table content
  - Colors cells in white


#### Team B Section
*[Team B components mirror Team A structure with same functionality]*

#### Start Game Button
- **Description**: User clicks the button when rosters are complete and ready to start game
- **Type**: Button
- **Content**: "Start game"
- **Clickable**: Yes
- **When clicked**: Enable start game pop-up

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
  - Go to Game frame
- **When clicking No button**:
  - Go back to Game frame. No action

### Flow
1. User inserts the players on each team that will be participating in today's game
2. After roster is complete, user clicks approve
3. User can edit roster after clicking edit button
4. When user has completed both teams rosters, user clicks start game

---

## Frame 3 - Game

### Description
This is the main screen where the live updates happen. The user will click buttons to start and pause time, select players and events to log, click for time out or substitute players etc. Each team will be on one side and the event buttons in the middle for easy and fast clicking.

### UI Layout Structure
```
┌─────────────────────────────────────────────────────┐
│  [Score: Team A 45 - Team B 38] [Clock: 8:45] [Q2] │
│  [Start] [Stop] [Team Fouls: A-3  B-5]             │
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
│ [Sub]         │                     │  [Sub]        │
└─────────────────────────────────────────────────────┘
```
**Layout Notes**:
- Numbers in brackets [ ] next to players show personal fouls
- Score and clock are prominently displayed at top
- Team fouls shown with color coding (red when ≥5)
- Event buttons in center 3x4 grid plus timeout
- Player buttons on sides with foul counts
- Team action buttons (TimeOut, Sub) at bottom of each panel

### Clock Panel
**Description**: The clock panel will include the game score, time, start and stop button and Q buttons.
**Location**: Clock panel will be in the top center between the team panels and above the event button panel.

#### Game Score Display
- **Description**: Live game score for both teams
- **Type**: Text Display
- **Location**: Middle-top section, next to game clock
- **Content**: "Team A: XX - Team B: XX" format
- **Clickable**: No
- **Updates**: Automatically when scoring events are recorded

#### Start Button
- **Description**: User clicks the button to start the time
- **Type**: Button
- **Location**: Top middle section
- **Content**: "Start"
- **Clickable**: Yes
- **When clicked**:
  - Start or continue game clock
  - Colors button in blue
  - Colors Stop button in light grey

#### Stop Button
- **Description**: User clicks the button to stop the time
- **Type**: Button
- **Location**: Top middle section
- **Content**: "Stop"
- **Clickable**: Yes
- **When clicked**:
  - Stop game clock
  - Colors button in blue
  - Colors Start button in light grey

#### Game Clock
- **Description**: Game clock
- **Type**: Time
- **Location**: Middle section, next to score display
- **Content**: Clock (MM:SS format)
- **Clickable**: Yes – long click to edit time if needed
- **When clicked**: Only if long-clicked, user may edit the time

#### Quarter Buttons (Q1, Q2, Q3, Q4)
- **Description**: There are 4 quarters in the game. The user clicks the button to declare quarter started
- **Location**: Under start and stop button in row
- **Content**: "Q1", "Q2", "Q3", "Q4"
- **Clickable**: Yes
- **When clicked**: Enable Quarter pop-up

#### Team Fouls
- **Description**: Showing the number of fouls committed by the team per quarter. Once it reaches over 5 there is a FT for any foul
- **Location**: Under start and stop button
- **Content**: Label "TF" content – number of team fouls recorded this quarter
- **Clickable**: No
- **Design**: Number of fouls turns Red from 5 fouls or more

#### Quarter Pop-up
- **Description**: A pop-up frame that asks if you meant to start the Q
- **Type**: Pop-up
- **Location**: Center
- **Content**:
  - "Start Q[1-4]?"
  - Yes button
  - No button
- **Clickable**: Yes
- **When clicking Yes button**:
  - Clock shows 10 minutes
  - Colors button in blue
  - Color of rest of Q's in light grey
- **When clicking No button**:
  - Go back to Game frame. No action

### Team Panel (Team A & Team B)
**Description**: Team players that are currently on court in 5 buttons one on top of the other. Big and clear for easy clicking. Teams buttons Time out and Subs will be there as well.

#### Player Title
- **Description**: Title of team panel
- **Type**: Text
- **Location**: Top left corner
- **Content**: Team name
- **Clickable**: No


#### Player Buttons
- **Description**: 5 buttons, one for each player. These buttons will be used to log the events by clicking on player and then the event that the player did
- **Type**: Button
- **Location**: Side panel. 5 buttons spread evenly one on top of the other
- **Content**: Player name and player number (automatically filled from roster)
- **Clickable**: Yes
- **When clicked**: Colors button turns blue


#### Time Out Button
- **Description**: When a time out is called the user will click the button of the team that called the timeout
- **Type**: Button
- **Location**: Side panel, on top of players
- **Content**: "Time Out"
- **Clickable**: Yes
- **When clicked**:
  - Colors button turns blue
  - Event recorded in log
  - If clock is still running, stops clock
  - Start button turns light grey
  - Stop button turns blue

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
**Description**: The event panel has the buttons of the events. When an event appears in the game, the user will click a player and then an event, and it will be stored in the log. An event button can be clicked only if a player button is selected. If there is no player button selected, a pop-up will state "Select player". The panel will be between the team panels and under the time panel.

#### Scoring Events

##### 1P Button
- **Description**: When a player makes a foul line shot (1 point)
- **Type**: Button
- **Location**: Top left side of the panel
- **Content**: "1P"
- **Clickable**: Yes
- **When clicked**:
  - All other event buttons turn grey
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "1P recorded in log for player [name and number]" (disappears after 3 seconds)
  - All players unclicked and turn grey

##### 2P Button
- **Description**: When a player makes a field goal worth 2 points
- **Type**: Button
- **Location**: Top left side of the panel, next to 1P
- **Content**: "2P"
- **Clickable**: Yes
- **When clicked**:
  - All other event buttons turn grey
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "2P recorded in log for player [name and number]" (disappears after 3 seconds)
  - All players unclicked and turn grey
  - Assist pop-up enabled

##### 3P Button
- **Description**: When a player makes a 3 point shot worth 3 points
- **Type**: Button
- **Location**: Top left side of the panel, next to 2P
- **Content**: "3P"
- **Clickable**: Yes
- **When clicked**:
  - All other event buttons turn grey
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "3P recorded in log for player [name and number]" (disappears after 3 seconds)
  - All players unclicked and turn grey
  - Assist pop-up enabled

##### Assist Button
- **Description**: After a player scores 2 or 3 points, an assist will be recorded
- **Type**: Button
- **Content**: "Assist"
- **Clickable**: Yes
- **When clicked**:
  - All other event buttons turn grey
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "Assist recorded in log for player [name and number]" (disappears after 3 seconds)
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
  - All other event buttons turn grey
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "1M recorded in log for player [name and number]" (disappears after 3 seconds)
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
  - Button turns Blue and goes back to grey after 3 seconds
  - Tooltip: "Foul recorded in log for player [name and number]" (disappears after 3 seconds)
  - All players unclicked and turn grey

### Flow
1. The game begins and user clicks Q1, the time will show 10 minutes and start
2. When an event happens the user clicks on the player and then on the event
3. If there is a follow up event like rebound or steal the user clicks on the player for follow up
4. The system records the event in the log
5. User will apply substitutions and time outs when they happen

---

## Frame 4 – Substitutions (MVP Feature)

### Description
User will substitute players and edit the 5 playing players. This allows for basic player rotation during the game.

### Components

#### Main Title
- **Description**: Main title of frame
- **Type**: Text
- **Location**: Top left corner
- **Content**: "Substitution Team A"
- **Clickable**: No

#### Table Playing Title
- **Description**: Table playing title
- **Type**: Text
- **Location**: Top left corner under main title
- **Content**: "Sub team A"
- **Clickable**: No

#### Sub Out Buttons
- **Description**: The 5 players that are currently playing will be displayed in 5 buttons like in the game frame
- **Type**: Button
- **Location**: Left side. 5 buttons spread evenly one on top of the other
- **Content**: Player name and player number (automatically filled from currently playing)
- **Clickable**: Yes
- **When clicked**: Colors button turns blue

#### Sub In Buttons
- **Description**: The rest of the players on the roster that are not currently playing
- **Type**: Button
- **Location**: Right side. Buttons spread evenly
- **Content**: Player name and player number (automatically filled from roster and currently playing)
- **Clickable**: Yes
- **When clicked**: Colors button turns blue


#### Sub Button
- **Description**: Once the user chose a player in and a player to go out, clicks to activate substitution
- **Type**: Button
- **Content**: "Sub"
- **Clickable**: Yes
- **When clicked**:
  - Creates substitution
  - Tooltip showing who was subbed (disappears after 3 seconds)
  - All players go back to grey

#### Back to Game Button
- **Description**: After completing substitutions, user clicks to go back to game
- **Type**: Button
- **Content**: "Back to game"
- **Clickable**: Yes
- **When clicked**: Go to game frame

### Flow
1. User clicks one of the 5 players playing he wants to substitute
2. User clicks one of the players that are on the bench
3. User clicks on button substitute
4. The players have been substituted and ready to go back to game

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
- **Description**: A log recording all the events
- **Type**: Table
- **Clickable**: Partially – Buttons edit and save only
- **Columns**:
  - Event ID
  - Game ID
  - Team ID
  - Player ID
  - Q (Quarter)
  - Game Time
  - Event
  - Edit and save buttons
  - Add row and delete row buttons
- **Cell content**: Auto
- **Details**: Editing is disabled by default. Users can edit data from a row alone and not enable full table for editing. Just one row at a time.

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











