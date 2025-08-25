package com.basketballstats.app.network;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;
import com.basketballstats.app.sync.SyncManager;
import com.basketballstats.app.sync.SyncQueueManager;
import com.basketballstats.app.models.AppSettings;
import com.basketballstats.app.data.DatabaseController;

/**
 * NetworkManager - Network connectivity detection and background sync triggers
 * 
 * Monitors network state changes and automatically triggers sync operations when connectivity resumes
 * Supports both WiFi and mobile data with user preference settings
 * Integrates with SyncManager and SyncQueueManager for seamless offline-first functionality
 */
public class NetworkManager {
    
    private static final String TAG = "NetworkManager";
    private static NetworkManager instance;
    
    private Context context;
    private ConnectivityManager connectivityManager;
    private DatabaseController dbController;
    private SyncManager syncManager;
    private SyncQueueManager syncQueueManager;
    
    private boolean isWifiConnected = false;
    private boolean isMobileConnected = false;
    private boolean isNetworkAvailable = false;
    
    // Network monitoring
    private ConnectivityManager.NetworkCallback networkCallback;
    private BroadcastReceiver networkReceiver;
    
    // Network state listeners
    public interface NetworkStateListener {
        void onNetworkAvailable(NetworkType networkType);
        void onNetworkLost();
        void onWifiConnected();
        void onMobileConnected();
        void onAllNetworksLost();
    }
    
    public enum NetworkType {
        WIFI,
        MOBILE,
        ETHERNET,
        UNKNOWN
    }
    
    /**
     * Singleton pattern for NetworkManager
     */
    public static synchronized NetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private NetworkManager(Context context) {
        this.context = context;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.dbController = DatabaseController.getInstance(context);
        this.syncManager = SyncManager.getInstance(context);
        this.syncQueueManager = SyncQueueManager.getInstance(context);
        
        initializeNetworkMonitoring();
        updateNetworkState();
        
        Log.d(TAG, "NetworkManager initialized");
    }
    
    // ===== NETWORK MONITORING =====
    
    /**
     * Initialize network monitoring based on Android version
     */
    private void initializeNetworkMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Use modern NetworkCallback for Android 7.0+
            setupNetworkCallback();
        } else {
            // Use legacy BroadcastReceiver for older versions
            setupNetworkReceiver();
        }
    }
    
    /**
     * Setup modern network callback (Android 7.0+)
     */
    private void setupNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    Log.d(TAG, "Network available: " + network);
                    handleNetworkAvailable(network);
                }
                
                @Override
                public void onLost(Network network) {
                    Log.d(TAG, "Network lost: " + network);
                    handleNetworkLost();
                }
                
                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    Log.d(TAG, "Network capabilities changed: " + network);
                    updateNetworkCapabilities(networkCapabilities);
                }
            };
            
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
        }
    }
    
    /**
     * Setup legacy network receiver (Android 6.0 and below)
     */
    private void setupNetworkReceiver() {
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Network state changed (legacy): " + intent.getAction());
                updateNetworkState();
                
                if (isNetworkAvailable()) {
                    handleNetworkAvailable(null);
                } else {
                    handleNetworkLost();
                }
            }
        };
        
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkReceiver, filter);
    }
    
    // ===== NETWORK STATE MANAGEMENT =====
    
    /**
     * Update current network state
     */
    private void updateNetworkState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                updateNetworkCapabilities(capabilities);
            } else {
                isWifiConnected = false;
                isMobileConnected = false;
                isNetworkAvailable = false;
            }
        } else {
            // Legacy method for older Android versions
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            
            if (isNetworkAvailable) {
                isWifiConnected = activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                isMobileConnected = activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            } else {
                isWifiConnected = false;
                isMobileConnected = false;
            }
        }
        
        Log.d(TAG, "Network state: Available=" + isNetworkAvailable + ", WiFi=" + isWifiConnected + ", Mobile=" + isMobileConnected);
    }
    
    /**
     * Update network capabilities (modern Android)
     */
    private void updateNetworkCapabilities(NetworkCapabilities capabilities) {
        if (capabilities != null) {
            isWifiConnected = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            isMobileConnected = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            isNetworkAvailable = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            isWifiConnected = false;
            isMobileConnected = false;
            isNetworkAvailable = false;
        }
    }
    
    // ===== NETWORK EVENT HANDLERS =====
    
    /**
     * Handle network becoming available
     */
    private void handleNetworkAvailable(Network network) {
        updateNetworkState();
        
        if (isNetworkAvailable()) {
            Log.d(TAG, "Network connectivity restored - triggering background sync");
            
            // Check user preferences for automatic sync
            if (shouldAutoSync()) {
                triggerBackgroundSync();
            }
            
            // Process any queued operations
            processQueueWhenConnected();
        }
    }
    
    /**
     * Handle network being lost
     */
    private void handleNetworkLost() {
        updateNetworkState();
        Log.d(TAG, "Network connectivity lost");
    }
    
    // ===== BACKGROUND SYNC TRIGGERS =====
    
    /**
     * Trigger background sync when network becomes available
     */
    private void triggerBackgroundSync() {
        try {
            // Only sync if user preferences allow it
            if (!shouldAutoSync()) {
                Log.d(TAG, "Auto-sync disabled by user preferences");
                return;
            }
            
            // Check if we should sync based on network type
            if (isWifiConnected && shouldSyncOnWifi()) {
                Log.d(TAG, "Triggering background sync on WiFi");
                performBackgroundSync("WiFi connection restored");
                
            } else if (isMobileConnected && shouldSyncOnMobile()) {
                Log.d(TAG, "Triggering background sync on mobile data");
                performBackgroundSync("Mobile data connection restored");
                
            } else {
                Log.d(TAG, "Network type not suitable for auto-sync based on user preferences");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error triggering background sync", e);
        }
    }
    
    /**
     * Perform actual background sync operation
     */
    private void performBackgroundSync(String reason) {
        syncManager.performIncrementalSync(new SyncManager.SyncCallback() {
            @Override
            public void onSyncStarted() {
                Log.d(TAG, "Background sync started: " + reason);
            }

            @Override
            public void onSyncProgress(String message) {
                Log.d(TAG, "Background sync progress: " + message);
            }

            @Override
            public void onSyncSuccess(String message) {
                Log.d(TAG, "Background sync completed successfully: " + message);
            }

            @Override
            public void onSyncError(String errorMessage) {
                Log.w(TAG, "Background sync failed: " + errorMessage);
            }

            @Override
            public void onSyncComplete() {
                Log.d(TAG, "Background sync operation complete");
            }
        });
    }
    
    /**
     * Process sync queue when network becomes available
     */
    private void processQueueWhenConnected() {
        try {
            SyncQueueManager.QueueStatistics stats = syncQueueManager.getQueueStatistics();
            
            if (stats.pendingOperations > 0) {
                Log.d(TAG, "Processing " + stats.pendingOperations + " queued operations");
                
                syncQueueManager.processQueue(new SyncQueueManager.QueueCallback() {
                    @Override
                    public void onQueueProcessingStarted(int totalOperations) {
                        Log.d(TAG, "Queue processing started: " + totalOperations + " operations");
                    }

                    @Override
                    public void onOperationRetried(String operation, int attempt, int maxRetries) {
                        Log.d(TAG, "Retrying operation: " + operation + " (attempt " + attempt + "/" + maxRetries + ")");
                    }

                    @Override
                    public void onOperationSuccess(String operation) {
                        Log.d(TAG, "Operation succeeded: " + operation);
                    }

                    @Override
                    public void onOperationFailed(String operation, String error) {
                        Log.w(TAG, "Operation failed: " + operation + " - " + error);
                    }

                    @Override
                    public void onQueueProcessingComplete(int successful, int failed) {
                        Log.d(TAG, "Queue processing complete: " + successful + " successful, " + failed + " failed");
                    }
                });
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing sync queue", e);
        }
    }
    
    // ===== USER PREFERENCES =====
    
    /**
     * Check if automatic sync is enabled
     */
    private boolean shouldAutoSync() {
        try {
            AppSettings setting = AppSettings.findByKey(dbController.getDatabaseHelper(), "auto_sync_enabled");
            return setting != null ? Boolean.parseBoolean(setting.getSettingValue()) : true; // Default: enabled
        } catch (Exception e) {
            Log.e(TAG, "Error checking auto-sync preference", e);
            return true; // Default to enabled on error
        }
    }
    
    /**
     * Check if sync on WiFi is enabled
     */
    private boolean shouldSyncOnWifi() {
        try {
            AppSettings setting = AppSettings.findByKey(dbController.getDatabaseHelper(), "sync_on_wifi");
            return setting != null ? Boolean.parseBoolean(setting.getSettingValue()) : true; // Default: enabled
        } catch (Exception e) {
            Log.e(TAG, "Error checking WiFi sync preference", e);
            return true; // Default to enabled on error
        }
    }
    
    /**
     * Check if sync on mobile data is enabled
     */
    private boolean shouldSyncOnMobile() {
        try {
            AppSettings setting = AppSettings.findByKey(dbController.getDatabaseHelper(), "sync_on_mobile");
            return setting != null ? Boolean.parseBoolean(setting.getSettingValue()) : false; // Default: disabled
        } catch (Exception e) {
            Log.e(TAG, "Error checking mobile sync preference", e);
            return false; // Default to disabled on error
        }
    }
    
    // ===== PUBLIC API =====
    
    /**
     * Check if network is currently available
     */
    public boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }
    
    /**
     * Check if WiFi is connected
     */
    public boolean isWifiConnected() {
        return isWifiConnected;
    }
    
    /**
     * Check if mobile data is connected
     */
    public boolean isMobileConnected() {
        return isMobileConnected;
    }
    
    /**
     * Get current network type
     */
    public NetworkType getCurrentNetworkType() {
        if (isWifiConnected) return NetworkType.WIFI;
        if (isMobileConnected) return NetworkType.MOBILE;
        return NetworkType.UNKNOWN;
    }
    
    /**
     * Manually trigger sync check (useful for testing)
     */
    public void triggerSyncCheck() {
        if (isNetworkAvailable()) {
            Log.d(TAG, "Manual sync check triggered");
            triggerBackgroundSync();
            processQueueWhenConnected();
        } else {
            Log.d(TAG, "Manual sync check: no network available");
        }
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && networkCallback != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            }
            
            if (networkReceiver != null) {
                context.unregisterReceiver(networkReceiver);
            }
            
            Log.d(TAG, "NetworkManager cleanup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error during NetworkManager cleanup", e);
        }
    }
    
    /**
     * Get network status summary
     */
    public String getNetworkStatusSummary() {
        if (!isNetworkAvailable) {
            return "No network connection";
        }
        
        String type = getCurrentNetworkType().toString();
        return "Connected via " + type;
    }
}
