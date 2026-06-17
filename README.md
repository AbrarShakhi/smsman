# SMS Man

A production-grade Android SMS/MMS client built in Jetpack Compose, modeled visually on Google's
[Jetchat](https://github.com/android/compose-samples/tree/main/Jetchat) sample. Clean Architecture
+ MVI throughout.

## Status

| Milestone | Description | State |
|---|---|---|
| M0 | Foundation: Hilt + Room + Nav3 + Compose adaptive + Coil + WorkManager + DataStore + theme | ✅ |
| M1.0–M1.5 | Nav routes, real Room schema, onboarding (default-SMS-app role), SMS receive + Telephony sync, contacts cache, conversation list | ✅ |
| M2.0–M2.2 | Chat screen (Jetchat-style bubbles), SMS send, sent/delivered status tracking, drafts, long-press actions | ✅ |
| M3.0 | MessagingStyle notifications with reply (RemoteInput), mark-as-read, delete actions + conversation shortcuts | ✅ |
| M5a | Cross-conversation search | ✅ |
| M5b | Real Settings screen (theme picker, dynamic color, receipts, default-app banner) | ✅ |
| M5c | Scheduled send (WorkManager) | ✅ |
| M5d | Blocked numbers management | ✅ |
| M5e | Delivery-report opt-out wired to SmsSender | ✅ |
| M4 | MMS (PDU codec, transport, attachments, group threading) | ⏸ deferred |
| M5f | Backup & restore (XML export/import) | ⏸ deferred |
| M5g | Adaptive list-detail layout for tablets/foldables | ⏸ deferred |
| M0.7 / M0.8 | Convention plugins + multi-module split | ⏸ deferred (single-module is fine for v1) |

## Build & run

```bash
# Debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Unit tests
./gradlew test

# Lint
./gradlew lintDebug

# Release APK (R8 minified)
./gradlew assembleRelease
```

Requires Android SDK 36 (with minor API level 1), JVM 11 toolchain, JDK 21 for the Gradle daemon
(auto-provisioned via foojay-resolver-convention).

## Tech stack

- **Kotlin** 2.3.10 with KSP 2.3.8
- **AGP** 9.2.1 on **Gradle** 9.4.1
- **Compose BOM** 2026.02.01 + **Material 3** + **material3-adaptive** 1.2.0
- **Hilt** 2.59.2 (DI), with **hilt-work** for `@HiltWorker`
- **Room** 2.7.1 (KSP) — local mirror of the Telephony provider
- **Navigation 3** 1.1.2 — Compose-first nav
- **DataStore** 1.1.2 — user settings + onboarding flag
- **WorkManager** 2.11.2 — scheduled send dispatch
- **Coil** 3.2.0 — image loading (contact photos, future MMS attachments)
- **kotlinx-serialization** 1.8.0 — scheduled message payload JSON
- **Google Fonts Compose** (Karla + Montserrat — same fonts as Jetchat)

## Architecture

```
data/
  datastore/          OnboardingTracker, SettingsRepositoryImpl
  db/                 SmsManDatabase + 6 entities + 6 DAOs + mappers
  repository/         Conversation, Message, Contacts, Search, Blocked, Scheduled impls
  telephony/          TelephonyImporter, SmsInboundHandler, TelephonySyncCoordinator
domain/
  model/              Conversation, Message + enums, Attachment, Contact, BlockedNumber,
                      ScheduledMessage, Sim, SendRequest, SearchHit, AppSettings, ThemeMode
  repository/         8 interfaces
di/                   DatabaseModule, RepositoryModule, DispatchersModule
framework/            Android-platform integration
  notification/       Channels + MessageNotificationCoordinator (MessagingStyle + Person + shortcuts)
  receiver/           SmsDeliver, MmsWapPush, NotificationReply/MarkRead/Delete, SentStatus, DeliveredStatus
  sender/             SmsSender (dual-SIM, multipart, sentIntent + optional deliveryIntent)
  service/            RespondViaMessageService (default-app role component)
  worker/             ScheduledSendWorker (@HiltWorker)
ui/                   Compose UI
  components/         ContactAvatar
  nav/                Nav3 Routes + AppNavHost
  screens/            onboarding/ conversations/ chat/ chat/components/ search/ settings/
                      blocked/ scheduled/ (+ Placeholders.kt for the deferred screens)
  theme/              Color, Type (Karla/Montserrat), Theme (light/dark/AMOLED + dynamic)
  util/               RelativeTime
SmsManApplication     @HiltAndroidApp + Configuration.Provider (WorkManager + HiltWorkerFactory)
MainActivity          @AndroidEntryPoint + splash + Nav3 host + deep link parser
MainViewModel         Initial route + settings flow into theme
```

### Data flow

The **Telephony provider** (`content://sms`, `content://mms-sms`) is the **system source of truth**
for messages. **Room mirrors** it for fast UI queries and adds our own metadata (pin, archive,
block, scheduled). On startup `TelephonySyncCoordinator` does a bulk import (most recent 1000) and
registers a `ContentObserver` for incremental updates. Inbound SMS arrives via `SmsDeliverReceiver`
which writes both stores; outbound sends write Telephony OUTBOX first, then `SmsManager` fires the
sent/delivered PendingIntents which update the row status.

### MVI

Every screen has:
- `data class ...State(...)` — single immutable state
- `sealed interface ...Intent { ... }` — input from UI
- `StateFlow<State>` + `SharedFlow<Effect>` exposed by the ViewModel
- `fun onIntent(intent: ...)` as the single entry point

## Permissions

The app requests SMS/MMS, Contacts, Phone State, Phone Numbers, Post Notifications, and
Schedule/Use Exact Alarm at runtime. Required SMS-default-handler intent filters are declared on
`MainActivity` (SENDTO/SEND), `SmsDeliverReceiver`, `MmsWapPushReceiver` (stub), and
`RespondViaMessageService`. The app must be set as the default SMS app for send/receive to work —
this is a Google Play policy + Android system requirement.

## Known limitations (v1)

- **No MMS yet**. Incoming MMS WAP-push is logged but not decoded. M4 brings vendored AOSP
  `com.google.android.mms` PDU code, transport via `ConnectivityManager.requestNetwork`, and
  attachment storage under `filesDir/mms/<messageId>/`.
- **No backup/restore**. Conversation data lives in Telephony + Room locally only.
- **No tablet list-detail layout** — the adaptive deps are wired but the navigation host isn't
  wrapped in `NavigableListDetailPaneScaffold` yet.
- **Scheduled send "from chat"** isn't surfaced in the chat overflow yet — schedule infrastructure
  works (WorkManager + repository + cancelable list view); a date/time picker entry point lands in
  M5c polish.
- **Single-module** project structure. The original plan called for multi-module + convention
  plugins; package boundaries (`data/`, `domain/`, `framework/`, `ui/`) are strictly observed so
  the refactor remains a mechanical split when needed.

## License

Apache 2.0 (font cert array sourced verbatim from Google's `compose-samples/Jetchat` under the
same license; see `res/values/font_certs.xml`).
