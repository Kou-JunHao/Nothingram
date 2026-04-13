# Nothingram

A third-party Telegram client for Android with useful modifications.

**[Why was this project created?](#why)**

## API, Protocol Documentation

Telegram API manuals: https://core.telegram.org/api

MTProto protocol manuals: https://core.telegram.org/mtproto

## Building

1. Clone the repository:
   ```bash
   git clone https://github.com/Kou-JunHao/Nothingram.git
   ```

2. Configure signing keys in `local.properties`:
   - `storeFile` - path to your keystore
   - `storePassword` - keystore password
   - `keyAlias` - key alias name
   - `keyPassword` - key password

3. Set up Firebase:
   - Create two apps at [Firebase Console](https://console.firebase.google.com/):
     - `uno.skkk.nothingram`
     - `uno.skkk.nothingram.beta`
   - Enable Firebase Cloud Messaging for both
   - Download `google-services.json` and place it in the `TMessagesProj` folder

4. Open the project in Android Studio (use "Open", not "Import")

5. Configure app extras in `TMessagesProj/src/main/java/tw/nekomimi/nekogram/Extra.java`:
   - Follow the links in the file to obtain each required API key/credential

6. Build the project using Gradle


## Dependencies

- Telegram source code
- Firebase Cloud Messaging
- Standard Android development tools (Android Studio, Gradle, NDK, etc.)

---

## Why was this project created? {#why}

This project was created in response to **serious security concerns** regarding the original Nekogram application.

### Security Issue: Malicious Code in Release Binaries

A security researcher published detailed evidence ([GitHub Issue #336](https://github.com/Nekogram/Nekogram/issues/336)) showing that the official Nekogram release APKs contain **spyware functionality** that was **not present in the public source code**.

**What was discovered:**

- The release APKs contain obfuscated malicious code that silently exfiltrates users' sensitive data
- Specifically, the app collects **phone numbers** and **user IDs** from all logged-in accounts (up to 8 accounts)
- This data is sent to a third-party Telegram bot (`@nekonotificationbot`, ID: `1190800416`) without user consent or knowledge
- The exfiltration happens automatically during normal app operation and uses inline bot queries to avoid detection in chat history
- The malicious code is **only present in the release builds**, meaning it was **intentionally injected during the build process** and deliberately hidden from the open-source repository

**Evidence:**
- Technical analysis: https://thebadinteger.github.io/nekogram-phone-exfiltration/
- Proof of concept: https://github.com/RomashkaTea/nekogram-proof-of-logging
- The deobfuscated malicious code shows data being sent with hash `741ad28818eab17668bc2c70bd419fc25ff56481758a4ac87e7ca164fb6ae1b1` containing collected phone numbers

### What this fork does

- **Full transparency**: All build artifacts are reproducible from the source code
- **No hidden code**: Any modifications from the upstream Telegram source are clearly documented
- **Community ownership**: This fork is community-driven with no single point of control

### Recommendation

If you previously used Nekogram from official sources, **we strongly recommend**:

1. **Stop using the official Nekogram APK immediately**
2. **Revoke API access** to `@nekonotificationbot` for your Telegram account
3. **Consider changing your phone number** if you used Nekogram with a sensitive number
4. **Use the official Telegram app** or other trusted open-source alternatives like [Telegram-FOSS](https://github.com/Telegram-FOSS-Team/Telegram-FOSS)

This fork aims to provide a safe, community-controlled alternative without any hidden data collection.
