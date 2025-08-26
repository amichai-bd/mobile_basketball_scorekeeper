#!/bin/bash

# Script to extract web client ID from google-services.json for Google Sign-In

echo "🔍 Extracting Web Client ID from google-services.json..."

if [ ! -f "app/google-services.json" ]; then
    echo "❌ Error: app/google-services.json not found!"
    echo "   Please download the updated google-services.json from Firebase Console"
    exit 1
fi

# Extract web client ID (look for oauth_client with client_type 3)
WEB_CLIENT_ID=$(grep -A 3 '"client_type": 3' app/google-services.json | grep '"client_id"' | cut -d'"' -f4)

if [ -z "$WEB_CLIENT_ID" ]; then
    echo "❌ Error: No web client ID found in google-services.json"
    echo "   Make sure you:"
    echo "   1. Enabled Google Sign-In in Firebase Console"
    echo "   2. Added your SHA-1 fingerprint"
    echo "   3. Downloaded the updated google-services.json"
    exit 1
fi

echo "✅ Found Web Client ID:"
echo "   $WEB_CLIENT_ID"
echo ""
echo "📝 Now updating strings.xml..."

# Update strings.xml with the web client ID
sed -i "s/YOUR_WEB_CLIENT_ID/$WEB_CLIENT_ID/g" app/src/main/res/values/strings.xml

echo "✅ Updated app/src/main/res/values/strings.xml"
echo "🎉 Google Sign-In configuration complete!"
echo ""
echo "📱 Now rebuild and test:"
echo "   ./gradlew installDebug"
