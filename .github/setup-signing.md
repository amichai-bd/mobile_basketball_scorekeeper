# APK Signing (Optional)

Your current setup builds **unsigned APKs** - perfect for testing and direct distribution.

## Current APKs Work For:
✅ Direct installation (just enable "Install from unknown sources")
✅ Beta testing
✅ Internal distribution

## For Play Store (Future):
You'll need signed APKs. When ready:

1. Generate keystore: `keytool -genkey -v -keystore release.jks`
2. Add signing config to `app/build.gradle`
3. Store keystore securely

**For now, unsigned APKs are perfect!**
