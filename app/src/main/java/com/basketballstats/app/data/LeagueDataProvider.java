// This file has been deprecated and replaced by SQLite database with DatabaseController
// All data is now stored persistently in SQLite with full CRUD operations
// See DatabaseController.java and the following models for SQLite persistence:
// - Team.java
// - TeamPlayer.java  
// - Game.java
// - Event.java
// 
// The old in-memory storage approach has been completely replaced by:
// 1. SQLite primary database (offline-first)
// 2. Firebase Firestore sync (cloud backup)
// 3. Robust data persistence and integrity
//
// This file will be removed in a future cleanup.