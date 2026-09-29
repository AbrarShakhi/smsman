<div align="center">

# SmsMan

**A clean, modern SMS app for Android, built with Jetpack Compose and Material 3.**

[![Release](https://img.shields.io/github/v/release/AbrarShakhi/smsman?style=flat-square)](https://github.com/AbrarShakhi/smsman/releases/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)
![Min SDK](https://img.shields.io/badge/minSdk-30-green?style=flat-square)
![Kotlin](https://img.shields.io/badge/Kotlin-Compose-7F52FF?style=flat-square&logo=kotlin&logoColor=white)

</div>

---

## Screenshots

<p align="center">
  <img src="public/1.jpeg" width="18%" />
  <img src="public/2.jpeg" width="18%" />
  <img src="public/3.jpeg" width="18%" />
  <img src="public/4.jpeg" width="18%" />
  <img src="public/5.jpeg" width="18%" />
</p>

## Features

- **Full default SMS app** – send, receive and store SMS, with delivery and sent status tracking
- **Conversations** – browse all threads, with separate **Favorite** and **Pinned** tabs
- **Search** – quickly find messages and conversations
- **New chat** – start a conversation with any contact or number
- **Multi-SIM support** – pick which SIM to send from
- **Rich notifications** – reply and mark as read directly from the notification
- **Quick reply** – respond to incoming calls via message
- **Customizable look** – light, dark or system theme, dynamic color (Material You) or preset accent colors, and selectable fonts
- **Guided onboarding** – simple permission and default-app setup

## Download

Grab the latest APK from the **[Releases page](https://github.com/AbrarShakhi/smsman/releases/latest)**.

1. Download the `.apk` file from the latest release.
2. Open it on your Android device and allow installation from unknown sources if prompted.
3. Launch SmsMan, grant the requested permissions, and set it as your default SMS app.

> **Requirements:** Android 11 (API 30) or newer.

## Permissions

| Permission | Why it's needed |
|---|---|
| `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS` | Read, send and receive text messages |
| `RECEIVE_MMS`, `RECEIVE_WAP_PUSH` | Required for the default SMS app role |
| `READ_CONTACTS` | Show contact names and pick recipients |
| `READ_PHONE_STATE` | Detect available SIM cards |
| `POST_NOTIFICATIONS` | Notify you of new messages |

All data stays on your device.

## Building from Source

```bash
git clone https://github.com/AbrarShakhi/smsman.git
cd smsman
./gradlew assembleDebug
```

The APK will be generated in `app/build/outputs/apk/debug/`.

## Tech Stack

- **Kotlin** & **Jetpack Compose** with **Material 3**
- **Room** for local metadata (pins, favorites)
- **Koin** for dependency injection
- Android Telephony provider APIs

## Contributing

Issues and pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

## License

Distributed under the [MIT License](LICENSE). © 2026 AbrarShakhi
