# Android Development Guide - WiFi Testing Workflow

A practical guide for Android app development using WiFi debugging - perfect for WSL environments or when you prefer wireless development.

## Prerequisites

- Android Studio or Android SDK installed
- Java Development Kit (JDK)
- An Android device for testing
- Both device and computer on the same WiFi network

## One-Time Setup

### 1. Enable Developer Options on Your Phone
1. Go to **Settings** → **About phone**
2. Find **Build number** and tap it 7 times
3. You'll see a message saying "You are now a developer!"

### 2. Enable Development Features
1. Go to **Settings** → **Developer options**
2. Enable **USB debugging**
3. Enable **Install via USB** (if available)
4. Enable **Wireless debugging**

### 3. Set Up WiFi Connection
This is the recommended method for development, especially from WSL!

1. **On your phone:**
   - Go to **Settings** → **Developer options**
   - Tap **Wireless debugging** to enter the menu
   - Note the IP address and port number displayed

2. **On your computer:**
   ```bash
   # Connect using the IP and port from your phone
   adb connect [YOUR_PHONE_IP]:[PORT]
   # Example: adb connect 192.168.1.100:37521
   ```

3. **Verify connection:**
   ```bash
   adb devices
   ```
   You should see your device listed as connected

**Important:** The port number changes each time you enable wireless debugging, so always check the current port in your phone's settings.

### 4. First Build Setup
```bash
# Make gradlew executable (Linux/WSL)
chmod +x gradlew

# Initial build to verify everything works
./gradlew build
```

---

## Development Workflow

### The 3-Step Development Cycle

#### 1. Make Changes
Edit your source code:
- **Java/Kotlin files**: `app/src/main/java/`
- **UI layouts**: `app/src/main/res/layout/`
- **Resources**: `app/src/main/res/values/`
- **App config**: `app/src/main/AndroidManifest.xml`

#### 2. Build and Deploy
After making changes, rebuild and install on your device:
```bash
# Build and install debug version
./gradlew installDebug
```

**Note:** Android apps require full rebuild for code changes - there's no hot reload like web development.

#### 3. Test Functionality
- The app will be updated automatically on your phone
- Open the app from your device's app drawer
- Test your changes
- Check device logs if needed:
  ```bash
  adb logcat | grep "com.example.myapp"
  ```

### Quick Commands Reference

```bash
# Check connected devices
adb devices

# Build and install (main command you'll use)
./gradlew installDebug

# Clean build (use if you encounter build issues)
./gradlew clean build

# View live device logs
adb logcat

# Filter logs for your app only
adb logcat | grep "YourAppName"

# Uninstall app from device
adb uninstall com.example.myapp

# Disconnect device
adb disconnect [DEVICE_IP]
```

---

## Troubleshooting

### WiFi Connection Issues
- **Device not connecting**: Ensure both devices are on the same WiFi network
- **Connection keeps dropping**: Check that **Wireless debugging** is still enabled
- **Wrong port**: The port changes each time - always verify current port in phone settings
- **ADB issues**: Restart ADB daemon:
  ```bash
  adb kill-server
  adb start-server
  ```

### Build Issues
- **Gradle errors**: Try clean build: `./gradlew clean build`
- **Permission errors**: Make sure gradlew is executable: `chmod +x gradlew`
- **Out of space**: Check available storage on your device

### App Installation Issues
- **Install failed**: Check device storage and try uninstalling previous version
- **Permission denied**: Ensure "Install from unknown sources" is enabled in Developer options
- **App crashes**: Check logs with `adb logcat` for error messages

---

## Development Tips

### Efficient Testing
1. **Keep Wireless debugging always on** - saves time reconnecting
2. **Use filtered logs** - `adb logcat | grep "YourApp"` shows only relevant output
3. **Test on real device** - emulators don't reflect real performance
4. **Keep phone plugged in** - debugging can drain battery quickly

### WiFi Development Benefits
- **No USB cables needed** - especially useful for WSL/Linux development
- **Freedom of movement** - test app while moving around naturally
- **No driver issues** - common problem with USB debugging on Linux/WSL
- **Multiple devices** - can connect several test devices simultaneously

### Development Cycle Optimization
```bash
# Create an alias for common commands (add to ~/.bashrc)
alias ainstall='./gradlew installDebug'
alias alog='adb logcat | grep "MyApp"'
alias adevices='adb devices'
```

---

## Project Structure (Generic)
```
android_project/
├── app/
│   ├── src/main/
│   │   ├── java/com/package/app/          # Java/Kotlin source code
│   │   ├── res/                           # Resources
│   │   │   ├── layout/                    # UI layouts
│   │   │   ├── values/                    # Strings, colors, styles
│   │   │   └── drawable/                  # Images and icons
│   │   └── AndroidManifest.xml            # App configuration
│   └── build.gradle                       # App dependencies and config
├── gradlew                                # Gradle wrapper script
├── gradlew.bat                           # Gradle wrapper (Windows)
└── build.gradle                          # Project-level configuration
```

---

## Notes
- **Native Android development** requires rebuilding after every code change
- **WiFi debugging is ideal for WSL** - avoids USB driver complications
- **Always test on real hardware** when possible for accurate performance
- **Keep your phone on charger** during extended development sessions
- **Port numbers change** every time you enable wireless debugging
