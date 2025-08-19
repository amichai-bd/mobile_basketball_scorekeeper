General
•	This app is designed for recording basketball statistics in minor and amateur leagues. Due to budget, manpower, and infrastructure limitations, most games currently track only the final score. Our app makes it possible to capture full game statistics using just one or two people, their own smartphones, and a simple, intuitive interface. The focus is on a smooth workflow and a clear UI that allows even a single person to record as many stats as possible with high accuracy.
•	General items:
o	Undo: Any event saved to log can be undone by swiping screen to the left. When happens, an approval pop up will show to make sure that the swipe was intentional.

Frame 1 – Game schedule
Frame description:
User will input the game schedule details for the whole season

Main Title:
•	Description: Main title of frame
•	Type: Text
•	Location: top left corner
•	Content: Summer league name
•	Clickable: no

Table Title:
•	Description: Table title
•	Type: Text
•	Location: top left corner under main title
•	Content: “Game schedule”
•	Clickable: no
•	Design:

Game list table:
•	Description: A list of games for the season with details for each game.  A row for each game
•	Type: table
•	Location: under game schedule title starting from left to right
•	Clickable: Partially – Buttons edit and save only
•	Design:
•	Columns:
o	Game ID
o	Game Date
o	Home team name
o	Away team name
o	W/L
o	Score
o	Edit and save buttons
o	Add row and delete row buttons
•	Cell content: User input
•	Design:
•	Details:
o	User click edit to enable editing.
o	User click save to save data and disable editing
o	User can not edit a row after game starts


Start game button
•	Description: Button that when clicked will start the process of a new game
•	Type: Button
•	Location: right of the game list table. Top of button in line with top of table
•	Content: “Start game”
•	Clickable: yes
•	Design:
•	When clicked:
o	Game row on game list turns grey
o	Enable mode pop up
o	Create new game log table
o	Create new game roster tables

Mode pop up: 
•	Description: a pop up frame that gives you the mode options for inserting data. This will have an impact on the UI and some functionality of the app
•	Type: Pop up
•	Location: Center
•	Content: 
o	Solo button – turns blue when chosen
o	Team button - turns blue when chosen
o	OK button
•	Clickable: yes
•	When clicking OK button:
o	Pop up disappears
o	Goes to chosen mode path
o	Go to game roster frame

Flow: 
•	User fills in the cells with data
•	User marks a row to start game
•	Click start game button
•	Chose mode solo or team
•	Click ok to go to game roster
Solo mode

Frame 2 – Game roster
Frame description:
In this frame the user inserts the rosters of each team into the system.
The players will be inserted one by one and the user will chose the opening 5.

Main Title:
•	Description: Main title of frame
•	Type: Text
•	Location: top left corner
•	Content: “Game roster”
•	Clickable: no
•	Design:

Team A table title
•	Description: Team A table title
•	Type: Text
•	Location: top left under main title
•	Content: Team listed as home in the game schedule table
•	Clickable: no
•	Design:

Team A table:
•	Description: A list of players on the game roster. To fill in by user before game
•	Type: table
•	Location: Left side under team A table title
•	Clickable: no
•	Design:
•	Columns:
o	Player number
o	Player name
•	Cell content: User input
•	Design:
•	Details:
o	Game roster cannot change once game starts

Approve roster button
•	Description: When user complete filling in roster it will be saved after clicking button
•	Type: Button
•	Location: Under team A table
•	Content: “Approve roster”
•	Clickable: yes
•	Design
•	When clicked:
o	Table turns light grey
o	User can not edit

Edit roster button
•	Description: user clicks the button to enable editing the roster table
•	Type: Button
•	Location: Under team A table
•	Content: “Edit roster”
•	Clickable: yes
•	Design:
•	When clicked:
o	Enables editing roster table content
o	Colors cells in white


Team B table title
•	Description: Team B table title
•	Type: Text
•	Location: top left under main title
•	Content: Team listed as home in the game schedule table
•	Clickable: no
•	Design:

Team A table:
•	Description: A list of players on the game roster. To fill in by user before game
•	Type: table
•	Location: Left side under team A table title
•	Clickable: no
•	Design:
•	Columns:
o	Player number
o	Player name
•	Cell content: User input
•	Details:
o	Game roster cannot change once game starts

Approve roster button
•	Description: When user complete filling in roster it will be saved after clicking botton
•	Type: Button
•	Location: Under team A table
•	Content: “Approve roster”
•	Clickable: yes
•	Design
•	When clicked:
o	Table turns light grey
o	User can not edit

Edit roster button
•	Description: user clicks the button to enable editing the roster table
•	Type: Button
•	Location: Under team A table
•	Content: “Edit roster”
•	Clickable: yes
•	Design:
•	When clicked:
o	Enables editing roster table content
o	Colors cells in white

Start game button
•	Description: user clicks the button when rosters are complete and ready to start game
•	Type: Button
•	Location:
•	Content: “Start game”
•	Clickable: yes
•	Design:
•	When clicked: Enable start game pop up
Pop up Start game
•	Description: a pop up frame that asks if you are sure you want to start the game
•	Type: Pop up
•	Location: Center
•	Content: 
o	“Are you sure you want to start game?”
o	Yes button
o	No button
•	Clickable: yes
•	When clicking Yes button:
o	Pop up words “Game Time!!!”
o	Go to frame Game
•	When click No button
o	Go back to Game frame. No action

Flow:
•	Use inserts the players on each tea, that will be participating in todays game.
•	After roster is complete, user will click approve.
•	User can edit roster after clicking edit button.
•	When user has completed both teams rosters, he can click start game.

Frame 3 - Game
Frame description:
This is the main screen where the live updates happen. The user will click buttons to start and pause time, select players and events to log, click for time out or substitute players etc.
Each team will be on one side and the event buttons in the middle for easy and fast clicking.

Clock panel:
Description: The clock panel will include the time, start and stop button and Q buttons.
Location: Clock panel will be in the top center between the team panels and above the event button panel.
Start button:
•	Description: user clicks the button to start the time
•	Type: Button
•	Location: top middle section
•	Content: “Start”
•	Clickable: yes
•	When clicked:
o	Start or continue game clock
o	Colors button in blue
o	Colors Stop button in light grey
•	Design:

 Stop button:
•	Description: user clicks the button to stop the time
•	Type: Button
•	Location: top middle section
•	Content: “Stop”
•	Clickable: yes
•	When clicked:
o	Stop game clock
o	Colors button in blue
o	Colors Start button in light grey
•	Design:

Game clock:
•	Description: Game clock
•	Type: Time
•	Location: Between start and stop buttons
•	Content: Clock
•	Clickable: Yes – long click to edit time if needed
•	When clicked: Only if clicked long click user may edit the time
•	Design

Q1 button (Same for Q2, Q2 and Q4 respectively)
•	Description: there are 4 quarters in the game. The user clicks the button to declare quarter started.
•	Location: under start and stop button in row with Q2, Q3 and Q4
•	Content: “Q1”
•	Clickable: yes
•	When clicked:
o	Enable Pop up Q button
•	Design:
Team fouls
•	Description: Showing the number of fouls committed by the team per quarter. Once it reaches over 5 there is a FT for any foul
•	Location: under start and stop button in row with Q2, Q3 and Q4
•	Content: Label “TF” content – number of team fouls recorded this quarter
•	Clickable: No
•	Design: Number of fouls turns Red from 5 fouls or more

Pop up Q buttons
•	Description: a pop up frame that asks if you meant to start the Q
•	Type: Pop up
•	Location: Center
•	Content: 
o	“Start Q1?”
o	Yes button
o	No button
•	Clickable: yes
•	When clicking Yes button:
o	Clock shows 10 minutes
o	Colors button in blue
o	Color of rest of Q’s in light grey
•	When click No button
o	Go back to Game frame. No action

Team A Panel (Team B panel like team A on the other side)
Description: Team A players that are currently on court in 5 buttons one on top of the other. Big and clear for easy clicking.
Teams buttons Time out and Subs will be there as well.

Player title
•	Description: Title of team panel
•	Type: Text
•	Location: Top left corner 
•	Content: Team A name
•	Clickable: No
•	Design:


Player button
•	Description: there will be 5 buttons. One for each player. These buttons will be used to log the events but clicking on player and then the event that the player did.
Buttons will be split into 2 sections. First on will be name and second will be player number.
•	Type: Button
•	Location: Left side. 5 buttons spread evenly one on top of the other.
•	Content: Player name and player number. The players and their numbers will be automatically filled in according to roster allocation.
•	Clickable: yes
•	When clicked:
o	Colors button turns blue
•	Design:


Time out button
•	Description: When a time out is called the user will click the button of the team called the timeout.
•	Type: Button
•	Location: Left side. On top of players
•	Content: “Time Out”
•	Clickable: yes
•	When clicked:
o	Colors button turns blue
o	Event recorded in log.
o	If clock is still running, Stops clock
o	Start button turns light grey
o	Stop button turns blue
•	Design:

Sub button
•	Description: When a player is substituted, the user will click on the Sub button to substitute the player on the system.
•	Type: Button
•	Location: Left side. On top of players
•	Content: “Sub”
•	Clickable: yes
•	When clicked:
o	Go to Sub frame
•	Design:
Personal foul title
•	Description: Title
•	Type: Text Label
•	Location: Left side. On top of the PF numbers
•	Content: “PF”
•	Clickable: No
•	Design:
Personal foul label
•	Description: Sums the number of fouls a player has recorded in a game. If a player has 5 fouls he is fouled out
•	Type: Text Label
•	Location: Left side. Next to player button
•	Content: Number of personal fouls
•	Clickable: No
•	Design:



Event Panel
Description: The event panel has the buttons of the events. When an event appears in the game, the user will click a player and then an event, and it will be stored in the log.
An even button can be clicked only if a player button is selected. If there is no player button selected, a pop up will state “Select player”
The panel will be between the team panels and under the time panel

1P Button
•	Description: When a player makes a foul line shot he gets 1 point. User clicks players name and then the event button 1P
•	Type: Button
•	Location: Top left side of the panel
•	Content: “1P”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on button of screen - “1P recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
•	Design:

2P Button
•	Description: When a player makes a field goal worth 2 points. User clicks players name and then the event button 2P
•	Type: Button
•	Location: Top left side of the panel, next to 1P
•	Content: “2P”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on button of screen - “2P recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
o	Assist pop up enabled
•	Design:

3P Button
•	Description: When a player makes a 3 point shot worth 3 points. User clicks players name and then the event button 3P
•	Type: Button
•	Location: Top left side of the panel, next to 2P
•	Content: “3P”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on buttom of screen - “3P recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
o	Assist pop up enabled
•	Design:

Assist Button
•	Description: After a player scores 2 or 3 points an assist will be recorded User clicks players name and then the event button 3P
•	Type: Button
•	Location: ?
•	Content: “Assist”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on buttom of screen - “Assist recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
•	Design:
Pop up Assist (consider deleting to avoid pop ups)
•	Description: After a basket is made and 2P or 3P are clicked, a pop up will open a frame with the names of the team on offence that made the basket. The user will then click on the player who made the assist.
•	Type: Pop up
•	Location: Center
•	Content: 
o	Title: “Assist?”
o	5 buttons of the players on court for the offensive team the team who made the basket.
o	A large button on the bottom: “No assist”
•	Clickable: Yes
•	When clicking one of the players:
o	Player turns blue
o	Tooltip on buttom of screen - “Assist recorded for” & player name and number. Disappears after 3 seconds
o	Pop up disappears after 1 second.
•	When click No assist button
o	Go back to Game frame. No action
1M Button
•	Description: When a player misses a foul line shot. User clicks players name and then the event button 1M
•	Type: Button
•	Location: Under 1P button
•	Content: “1M”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on button of screen - “1M recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
o	Enable pop up Rebound
•	Design:

2M Button
•	Description: When a player misses Field goal 2 point shot. User clicks players name and then the event button 2M
•	Type: Button
•	Location: Under 2P button
•	Content: “2M”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on button of screen - “2M recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
o	Enable pop up Rebound
•	Design:

3M Button
•	Description: When a player misses 3 point shot. User clicks players name and then the event button 3M
•	Type: Button
•	Location: Under 3P button
•	Content: “3M”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on button of screen - “3M recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
o	Enable pop up Rebound
OR Button
•	Description: After a missed shot if the offence catch the rebound the user will record an OR.
•	Type: Button
•	Location: ?
•	Content: “OR”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on button of screen - “OR recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
•	Design:

DR Button
•	Description: After a missed shot if the defence catch the rebound the user will record an DR.
•	Type: Button
•	Location: ?
•	Content: “DR”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on button of screen - “DR recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
•	Design:

Pop up Rebound (consider deleting to avoid pop ups)
•	Description: After a miss, a rebound will likely be caught. A pop up will appear to help with fast rebound selecting
•	Type: Pop up
•	Location: Center
•	Content: 
o	Title: “Rebound?”
o	5 buttons of the players on left for team A and 5 on the right for team B
o	A large button on the bottom: “No rebound”
•	Clickable: Yes
•	When clicking one of the players:
o	Player turns blue
o	If player is on offence, it will mark offensive rebound. If player on defense with mark defensive rebound
o	Tooltip on bottom of screen – “Offensive rebound, or Defensive rebound recorded for” & player name and number. Disappears after 3 seconds
o	Pop up disappears after 1 second.
•	When click No rebound button
o	Go back to Game frame. No action

Turnover button
•	Description: When a turn over happens, the user will click player and turnover event
•	Type: Button
•	Location: ?
•	Content: “1Turnover”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on button of screen - “Turnover recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
o	Enable pop up Steal
•	Design:

Steal Button
•	Description: The user records a steal
•	Type: Button
•	Location: ?
•	Content: “Steal”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on button of screen - “Steal recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
•	Design:

Pop up Steal (consider deleting to avoid pop ups)
•	Description: After a turnover is recorded, there is usually a steal that caused the turnover.
•	Type: Pop up
•	Location: Center
•	Content: 
o	Title: “Steal?”
o	5 buttons of the opposite team of the player who committed the turnover
o	A large button on the bottom: “No steal”
•	Clickable: Yes
•	When clicking one of the players:
o	Player turns blue
o	Tooltip on bottom of screen – “Steal recorded for” & player name and number. Disappears after 3 seconds
o	Pop up disappears after 1 second.
•	When click No steal button
o	Go back to Game frame. No action
Block Button
•	Description: The user records a defensive block
•	Type: Button
•	Location: ?
•	Content: “Block”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on button of screen - “Block recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
•	Design:



Foul button
•	Description: Foul
•	Type: Button
•	Location: ?
•	Content: “Foul”
•	Clickable: yes
•	When clicked:
o	All other event buttons turn grey
o	Button turns Blue and goes back to grey after 3 seconds
o	Tooltip on button of screen - “Foul recorded in log for player” & player name and number. Disappears after 3 seconds
o	All players unclicked and turn grey
•	Design:

Flow
•	The game begins and user clicks Q1, the time will show 10 minutes and start.
•	When an event happens the user clicks on the player and then on the event.
•	If there is a follow up event like rebound or steal the user clicks on the player for follow up.
•	The system records the even in the log
•	User will apply substitutions and time outs when they happen

Frame 4 – Substitutions
Frame description
User will substitute players and edit the 5 playing players

Main Title
•	Description: Main title of frame
•	Type: Text
•	Location: top left corner
•	Content: Substitution Team A
•	Clickable: no
•	Design:

Table playing title
•	Description: Table playing title
•	Type: Text
•	Location: top left corner under main title
•	Content: “Sub tea A”
•	Clickable: no
•	Design:

Sub out buttons
•	Description: The 5 players that are currently playing will be displayed in 5 buttons like in the game frame.
•	Type: Button
•	Location: Left side. 5 buttons spread evenly one on top of the other.
•	Content: Player name and player number. The players and their numbers will be automatically filled in according to the players currently playing.
•	Clickable: yes
•	When clicked:
o	Colors button turns blue
•	Design:

Sub in buttons
•	Description: The rest of the players on the roster that are not currently playing
•	Type: Button
•	Location: right side. Buttons spread evenly.
•	Content: Player name and player number. The players and their numbers will be automatically filled in according to the roster and players currently playing.
•	Clickable: yes
•	When clicked:
o	Colors button turns blue
•	Design:


Sub button
•	Description: once the user chose a player in and a player to go out, he will click the sub button to activate the substitution.
•	Type: Button
•	Location: ?
•	Content: “Sub”
•	Clickable: yes
•	When clicked:
o	Creates substitution.
o	Tool tip writing who was subbed. Disappears after 3 seconds.
o	All players go back to grey
•	Design:

Back to game button
•	Description: After completing the substitutions, the user will click to go back to game
•	Type: Button
•	Location: ?
•	Content: “Back to game”
•	Clickable: yes
•	When clicked:
o	Go to game frame
•	Design:
Flow
•	User clicks one of the 5 players playing he wants to substitute.
•	User clicks one of the players that are on the bench.
•	User clicks on button substitute.
•	The players have been substituted and ready to go back to game


Frame 5 – Log
Frame description
Can view and edit the log of events

Main Title
•	Description: Main title of frame
•	Type: Text
•	Location: top left corner
•	Content: Event log
•	Clickable: no
•	Design:

Event Log Table
•	Description: A log recording all the events.
•	Type: table
•	Location: ?
•	Clickable: Partially – Buttons edit and save only
•	Design:
•	Columns:
o	Event ID
o	Game ID
o	Team ID
o	Player ID
o	Q
o	Game Time
o	Event
o	Edit and save buttons
o	Add row and delete row buttons
•	Cell content: Auto
•	Design:
•	Details:
o	Editing is disabled by default. Users can edit data from a row alone and not enable full table for editing. Just one row at a time.

Frame 6 – Game stats
Frame description
Can view the game statistics with sorting grouping  and filter options by game, season, player etc.

Main Title
•	Description: Main title of frame
•	Type: Text
•	Location: top left corner
•	Content: Stat Sheet
•	Clickable: no
•	Design:

Player per game button
•	Description: opens up player per game table
•	Type: Button
•	Location: ?
•	Content: “Player per game”
•	Clickable: Yes
•	When clicked:
o	Player per game Stats table will appear
o	Button turns blue rest of buttons turn light grey
•	Design:

Player per game stats table
•	Description: Stats of the player per game throughout the season. Table could be sorted and filtered according to any column
•	Type: table
•	Location: ?
•	Clickable: Filter and sorting
•	Design:
•	Columns:
o	#
o	Player name
o	Team
o	MP - Time
o	Pts - Number
o	FT – % (Number of made/number of attempts) Exp 33% (3/9)
o	2P - % (Number of made/number of attempts) Exp 33% (3/9)
o	3P - % (Number of made/number of attempts) Exp 33% (3/9)
o	Ast – Number
o	R - Number
o	OR - Number
o	DR - Number
o	St – Number
•	Comment – All columns get the sum of the event per game

Player season numbers button
•	Description: Opens up player season numbers table
•	Type: Button
•	Location: ?
•	Content: “Player season numbers”
•	Clickable: Yes
•	When clicked:
o	Player season numbers Stats table will appear
o	Button turns blue rest of buttons turn light grey
•	Design:

Player season numbers stats table
•	Description: Stats of the player per season. The accumulation of stats throughout the season. Table could be sorted and filtered according to any column
•	Type: table
•	Location: ?
•	Clickable: Filter and sorting
•	Design:
•	Columns:
o	#
o	Player name
o	Team
o	MP - Time
o	Pts - Number
o	FT – % (Number of made/number of attempts) Exp 33% (3/9)
o	2P - % (Number of made/number of attempts) Exp 33% (3/9)
o	3P - % (Number of made/number of attempts) Exp 33% (3/9)
o	Ast - Number
o	R - Number
o	OR - Number
o	DR - Number
o	St – Number
•	Comment – All event columns get the sum of the events per season

Player season Average button
•	Description: Opens up player season average table
•	Type: Button
•	Location: ?
•	Content: “Player season average”
•	Clickable: Yes
•	When clicked:
o	Player season numbers Stats table will appear
o	Button turns blue rest of buttons turn light grey
•	Design:

Player season Average stats table
•	Description: Stats of the player per season. The average of stats throughout the season. Table could be sorted and filtered according to any column
•	Type: table
•	Location: ?
•	Clickable: Filter and sorting
•	Design:
•	Columns:
o	#
o	Player name
o	Team
o	MP - Time
o	Pts - Number
o	FT – % (Number of made/number of attempts) Exp 33% (3/9)
o	2P - % (Number of made/number of attempts) Exp 33% (3/9)
o	3P - % (Number of made/number of attempts) Exp 33% (3/9)
o	Ast - Number
o	R - Number
o	OR - Number
o	DR - Number
o	St – Number
•	Comment – all event columns get the season average
Flow
•	User clicks on one of the 3 modes
•	User scrolles through the stat report.
•	User filters and sorts for desired view.











