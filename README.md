# My First Android App

A simple Android application for testing development setup between WSL and Android devices using WiFi debugging.

## Prerequisites

- Android Studio or Android SDK installed
- Java Development Kit (JDK)
- An Android device for testing

## Setting Up Your Phone for Development

### 1. Enable Developer Options
1. Go to **Settings** → **About phone**
2. Find **Build number** and tap it 7 times
3. You'll see a message saying "You are now a developer!"

### 2. Enable USB Debugging
1. Go to **Settings** → **Developer options**
2. Enable **USB debugging**
3. Enable **Install via USB** (if available)

### 3. Connect Your Phone via WiFi (Recommended)

#### WiFi Connection (Wireless Debugging)
This is the easiest method for development, especially when working from WSL!

1. **Ensure your phone and computer are on the same WiFi network**

2. **Enable Wireless Debugging on your phone:**
   - Go to **Settings** → **Developer options**
   - Find **Wireless debugging** and enable it
   - Tap on **Wireless debugging** to enter the menu
   - You'll see your device's IP address and port

3. **Connect from your computer:**
   ```bash
   # Connect using the IP and port shown in Wireless debugging
   adb connect [YOUR_PHONE_IP]:[PORT]
   # Example: adb connect 192.168.1.100:37521
   ```

4. **Verify connection:**
   ```bash
   adb devices
   ```
   You should see your device listed as connected

**Note:** The port number changes each time you enable wireless debugging, so check it in your phone's settings.

#### Alternative: USB Connection (Optional)
If WiFi connection doesn't work, you can use USB:
1. Connect your phone to your computer via USB cable
2. Accept the "Allow USB debugging?" prompt on your phone
3. Verify with `adb devices`

## Building and Running the App

### First Time Setup
```bash
# Make gradlew executable
chmod +x gradlew

# Build the app
./gradlew build
```

### Install and Run
```bash
# Build and install the debug version on your connected device
./gradlew installDebug
```

The app will be installed on your phone. Open it manually from your app drawer.

## Making and Testing Changes

### 1. Edit Your Code
For example, to change the displayed text:
- Open `app/src/main/java/com/example/myapp/MainActivity.java`
- Modify the text in `tv.setText("Your new text here");`
- Save the file

### 2. Rebuild and Deploy
After making changes to Java code, you need to rebuild and reinstall:
```bash
./gradlew installDebug
```

### 3. View the Changes
- The app will be updated on your phone
- Open the app to see your changes
- Note: Native Android apps don't have hot reload - you must rebuild after Java/Kotlin changes

## Quick Development Commands

```bash
# Check connected devices
adb devices

# Build and install debug version
./gradlew installDebug

# Clean build (if you encounter issues)
./gradlew clean
./gradlew build

# View device logs (helpful for debugging)
adb logcat | grep "com.example.myapp"

# Uninstall the app
adb uninstall com.example.myapp
```

## Troubleshooting

### WiFi Connection Issues
- **Ensure both devices are on the same network**
- Check that **Wireless debugging** is enabled in Developer options
- The port number changes each time - always check current port in phone settings
- Try disabling and re-enabling Wireless debugging
- Check firewall settings on your computer
- Run `adb kill-server` then `adb start-server` to restart ADB

### Device Not Found
- Ensure **USB debugging** and **Wireless debugging** are enabled
- For WiFi: Check you're using the correct IP and port from your phone
- Try `adb disconnect` then reconnect
- Restart ADB: `adb kill-server` then `adb start-server`

### App Not Installing
- Check available storage on your device
- Ensure "Install from unknown sources" is enabled
- Try uninstalling the previous version first:
  ```bash
  adb uninstall com.example.myapp
  ```

## Project Structure
```
my_first_app/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/myapp/
│   │       │   └── MainActivity.java    # Main activity file
│   │       ├── res/                     # Resources (layouts, values, etc.)
│   │       └── AndroidManifest.xml      # App configuration
│   └── build.gradle                      # App-level build configuration
├── build.gradle                          # Project-level build configuration
└── README.md                             # This file
```

## Notes
- This is a native Android app (Java)
- Changes require rebuilding - no hot reload like web apps
- Development is done over WiFi - no USB cable needed!
- Always test on a real device when possible for accurate performance
- WiFi debugging is perfect for WSL development since it avoids USB driver issues
