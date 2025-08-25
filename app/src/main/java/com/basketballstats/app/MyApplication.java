package com.basketballstats.app;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;

/**
 * Application class for Firebase initialization
 */
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Initialize Firebase first
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed", e);
        }
    }
}
