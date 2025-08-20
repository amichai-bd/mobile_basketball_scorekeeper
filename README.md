# Basketball Statistics App

[![Release APK](https://github.com/amichai-bd/mobile_basketball_scorekeeper/workflows/Release%20APK/badge.svg)](https://github.com/amichai-bd/mobile_basketball_scorekeeper/actions)

A mobile application designed for recording comprehensive basketball statistics in minor and amateur leagues using smartphones and simple, intuitive interfaces.

## 📱 Download

**[⬇️ Download Latest APK](https://github.com/amichai-bd/mobile_basketball_scorekeeper/releases/latest)**

*Enable "Install from unknown sources" on your Android device to install*

## Problem Statement

Most amateur and minor league basketball games currently track only the final score due to budget, manpower, and infrastructure limitations. This app enables 1-2 people to capture complete game statistics using their smartphones, bringing professional-level stat tracking to grassroots basketball.

## Project Overview

**Status**: 🚧 Active Development - Phase 1 (MVP/POC)  
**Platform**: Android (Native Java)  
**Target Users**: Basketball scorekeepers, league organizers, amateur teams  
**Mode**: Solo operation (Team mode planned for future releases)

## MVP Features (v1.0)

### ✅ Core Functionality
- **Game Schedule Management**: Create and manage full season schedules
- **Team Roster Setup**: Configure 5-player teams with basic player information
- **Live Game Recording**: Track all 13+ basketball events in real-time
  - Scoring: 1P, 2P, 3P makes and misses
  - Rebounds: Offensive and defensive rebounds  
  - Playmaking: Assists, steals, blocks
  - Violations: Turnovers, personal fouls
  - Team Events: Timeouts, substitutions
- **Real-time Scoring**: Live score updates with team foul tracking
- **Event Logging**: Complete game event history with editing capabilities
- **Basic Statistics**: Per-player and team statistics with simple reporting

### 📱 User Experience
- **Large Touch Targets**: Optimized for quick tapping during live games
- **Clear Visual Feedback**: Color-coded buttons and status indicators
- **Minimal Disruption**: Streamlined workflows to avoid missing game action
- **Error Recovery**: Undo functionality and event editing capabilities

### 🗄️ Technical Highlights
- **Local-First**: SQLite database, no internet required during games
- **Offline Capable**: Full functionality without network connectivity
- **Data Persistence**: Automatic saving with crash recovery
- **Portrait Optimized**: Mobile-first design for single-handed operation

## Architecture

- **Frontend**: Native Android (Java) with XML layouts
- **Database**: SQLite with optimized schema for basketball events
- **Pattern**: MVC architecture with clear separation of concerns
- **Dependencies**: Minimal external dependencies for reliability

## Documentation

- **📋 Functional Specification**: [`spec/specification.md`](spec/specification.md) - Complete feature requirements and UI specifications
- **🔧 Technical Documentation**: [`spec/dev_spec_and_status.md`](spec/dev_spec_and_status.md) - Architecture, database design, and development progress
- **💻 Development Guide**: [`GUIDE.md`](GUIDE.md) - Android development workflow and WiFi debugging setup

## Quick Start

### For Developers
1. **Setup**: Follow [`GUIDE.md`](GUIDE.md) for Android development environment
2. **Build**: `./gradlew installDebug` to install on connected device
3. **Develop**: Use the 3-step cycle (edit → build → test) outlined in the guide

### For Contributors
1. Review the [functional specification](spec/specification.md) to understand requirements
2. Check the [development status](spec/dev_spec_and_status.md) for current progress and next tasks
3. Follow the development guide for technical setup

## Project Status

### ✅ Completed
- Project architecture and specifications
- Database schema design
- Technical documentation
- Development workflow setup

### 🚧 In Progress (Phase 1)
- Database implementation and model classes
- Core UI layouts and navigation
- Foundation components

### ⏳ Next Up
- Game schedule management (MainActivity)
- Live game recording interface (GameActivity)
- Event logging and basic statistics

### 🎯 Future Phases
- **Phase 2**: Advanced statistics, data export, UI polish
- **Phase 3**: Team mode, cloud sync, advanced analytics

## Target Users

### Primary Users
- **Basketball Scorekeepers**: Volunteers or staff recording games
- **League Organizers**: Managing multiple teams and game schedules
- **Coaches**: Tracking player performance and team statistics

### Use Cases
- **Amateur Leagues**: Community and recreational basketball leagues
- **Youth Sports**: School and club team statistics
- **Tournament Play**: Multi-game events requiring detailed tracking
- **Training Games**: Practice and scrimmage stat recording

## Contributing

This project follows a specification-driven development approach:

1. **Read the specs**: Start with [`spec/specification.md`](spec/specification.md) for functional requirements
2. **Check progress**: Review current status in [`spec/dev_spec_and_status.md`](spec/dev_spec_and_status.md)
3. **Development setup**: Follow [`GUIDE.md`](GUIDE.md) for technical environment
4. **Pick a task**: Choose from Foundation, Game Management, or Event Recording tasks
5. **Test thoroughly**: Use real basketball scenarios for validation

## License

[License to be determined]

---

## Why This App?

Traditional basketball statistics require expensive equipment, dedicated software, or multiple trained operators. This app democratizes comprehensive stat tracking by:

- **Reducing Complexity**: Simple touch interface anyone can learn
- **Minimizing Cost**: Uses existing smartphones, no additional hardware
- **Maximizing Accessibility**: Works offline, no internet or cloud dependency required
- **Optimizing Workflow**: Designed specifically for fast-paced basketball action

The result is professional-quality statistics accessible to any basketball program, regardless of budget or technical expertise.