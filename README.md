# SMS Man

An Android SMS client built with Jetpack Compose, presenting a Material 3 Expressive interface
modelled on Google Messages. The application operates as a default SMS handler, reading from and
writing to the system Telephony provider.

**Application ID:** `com.abrarshakhi.smsman`

---

## Overview

SMS Man is a single-module Android application that implements the complete lifecycle of a default
SMS handler: receiving and persisting inbound messages, presenting conversations and message
threads, composing and transmitting messages across multiple SIM subscriptions, and notifying the
user of incoming traffic.

The Telephony provider is treated as the authoritative source of message data. A local Room database
stores only supplementary metadata that the provider is unable to represent, namely per-message pin
state and per-conversation favourite state. Message content is never mirrored.

---

## Features

| Capability | Description |
|---|---|
| Default SMS handler | Declares the four components required for SMS role eligibility and requests the role via `RoleManager`. |
| Inbound persistence | Writes received messages to the Telephony provider, including reassembly of multipart PDUs. |
| Conversation list | Thread-based listing with contact resolution, unread counts, snippets and relative timestamps. |
| Message thread | Grouped message bubbles with day separators and per-message detail disclosure. |
| Message detail | Exact timestamp, originating SIM and delivery status, revealed on selection. |
| Transmission | Sends via an explicit subscription, with automatic segmentation and per-part status tracking. |
| Multi-SIM support | Resolves subscriptions through `SubscriptionManager`; permits SIM selection at composition time. |
| Delivery reporting | Parses delivery report PDUs and reflects them in message status. |
| Message composition | Recipient entry with contact search, or free-form number entry. |
| Pinned messages | Individual messages may be pinned; pinned items are aggregated across all conversations. |
| Favourite conversations | Conversations may be marked as favourites and filtered accordingly. |
| Notifications | `MessagingStyle` notifications with inline reply and mark-as-read actions. |
| Read state | Threads are marked read on open and via the notification action. |
| Theming | Material 3 Expressive with dynamic colour, and light and dark schemes. |

### Navigation structure

The application presents three primary destinations in a bottom navigation bar:

| Destination | Contents |
|---|---|
| Messages | All conversations. The only destination presenting a compose action. |
| Favorite | Conversations marked as favourites. |
| Pinned | Individual pinned messages across all conversations. |

---

## Requirements

| Component | Version |
|---|---|
| Minimum SDK | 30 (Android 11) |
| Target and compile SDK | 37 |
| Java toolchain (Gradle daemon) | JDK 25 |
| Java and Kotlin bytecode target | 17 |
| Gradle | 9.6.0 |
| Android Gradle Plugin | 9.4.1 |
| Kotlin | 2.4.20 |

The Gradle daemon toolchain is pinned in `mise.toml`.

---

## Technology stack

| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose (BOM) | 2026.09.00 | User interface |
| Material 3 | 1.5.0-alpha28 | Design system, including Expressive components |
| Material Icons Core | 1.7.8 | Icon set |
| Navigation 3 | 1.1.7 | Navigation host and back stack |
| Lifecycle ViewModel Navigation 3 | 2.11.0 | ViewModel scoping per navigation entry |
| Koin | 4.2.2 | Dependency injection, including navigation entry registration |
| Room | 2.8.5 | Local metadata persistence |
| KSP | 2.3.10 | Annotation processing for Room |
| kotlinx.serialization | 1.11.0 | Back stack persistence |

Dependency coordinates and versions are declared exclusively in `gradle/libs.versions.toml`.

---

## Architecture

### Module layout

The project comprises a single Gradle module, `:app`, organised into three source layers.

```
common/     Application shell
  di/               Koin module aggregation
  main/             Root scaffold and chrome contract
  navigation/       Route definitions, back stack, navigation host
  ui/               Theme and shared components
  util/             Formatting helpers

core/       Shared infrastructure
  database/         Room database, entities and data access objects
  di/               Infrastructure bindings
  model/            Domain models
  notification/     Notification construction and action receivers
  permissions/      Runtime permissions and SMS role management
  repository/       Composition of provider and database sources
  telephony/        Provider access, transmission, broadcast receivers

features/   Feature presentation
  chat/ conversations/ newmessage/ onboarding/ pinned/ settings/
```

Each feature package contains its view model, composable screen, chrome definition and Koin module.

### Presentation

A single `Scaffold`, declared in `common/main/AppRoot.kt`, hosts the entire application. Screens do
not declare their own scaffold, application bar or floating action button. Each route instead
contributes a `ScreenChrome` value describing its title, top bar, bottom bar and floating action
button, resolved through an exhaustive expression over the route type.

This arrangement is the mechanism by which the compose action is restricted to a single destination:
the Messages chrome supplies a floating action button, and the remaining chromes retain the empty
default.

### Navigation

Navigation is performed by Navigation 3. Feature modules register their own destinations through
Koin's `navigation<T> { }` builder, which are assembled into a single entry provider. The back stack
is a `SnapshotStateList` managed directly by the application, persisted across process death by
serialising each route.

Because the Koin navigation builder provides no parameter through which to pass the back stack to a
screen, the back stack is exposed to screens through a composition local supplied by `AppRoot`.

### Data

Conversations are read from `content://mms-sms/conversations`, with recipient identifiers resolved
against the canonical address table and unread counts derived from the message table. Messages
within a thread are read from `content://sms`. A single content observer on the `mms-sms` authority,
debounced, drives refresh.

Room stores two entities: conversation metadata keyed by thread identifier, and pinned message
records keyed by message identifier. Pinned records additionally retain a content-derived
fingerprint, since provider identifiers are stable only for the lifetime of a row and are
renumbered by backup restoration or message import.

Metadata may reference provider rows that have since been deleted by another application.
Consumers therefore reconcile on each refresh and remove records that no longer resolve.

---

## Building

```bash
./gradlew assembleDebug        # Assemble the debug variant
./gradlew installDebug         # Install on a connected device
./gradlew :app:compileDebugKotlin
./gradlew lintDebug
```

### Installation and the SMS role

Reinstalling the application relinquishes the SMS role, and Android consequently revokes the
associated runtime permissions. The application will present its onboarding flow until the role is
restored. Reassigning the role restores the permissions automatically:

```bash
./gradlew installDebug
adb shell cmd role add-role-holder android.app.role.SMS com.abrarshakhi.smsman
```

Note that `adb shell pm grant` is rejected on some vendor builds, which withhold
`GRANT_RUNTIME_PERMISSIONS` from the shell user. Where that applies, the role command above is the
only non-interactive route; otherwise permissions must be granted through the onboarding interface.

The previously configured SMS application may be restored at any time through
**Settings → Apps → Default apps → SMS app**.

---

## Testing

```bash
./gradlew test                      # Host unit tests
./gradlew connectedDebugAndroidTest # Instrumented tests; requires a connected device
```

Instrumented tests cover the Room data access layer, message segmentation, telephone number
normalisation and notification construction. Several of these behaviours depend on platform APIs and
cannot be meaningfully exercised on the host JVM:

| Suite | Subject |
|---|---|
| `MessageMetadataRepositoryTest` | Generated data access queries, pin scoping, ordering, pruning and fingerprint stability |
| `SmsSegmentsTest` | Segment calculation, including UCS-2 encoding thresholds |
| `PhoneNumbersTest` | Equivalence of local and international number forms, and exclusion of alphanumeric addresses from loose matching |
| `MessageNotifierTest` | Notification construction, action composition and dismissal |

---

## Permissions

| Permission | Purpose |
|---|---|
| `READ_SMS` | Read conversations and messages from the provider |
| `SEND_SMS` | Transmit messages |
| `RECEIVE_SMS` | Receive inbound message broadcasts |
| `RECEIVE_MMS`, `RECEIVE_WAP_PUSH` | Required of a default SMS handler |
| `READ_CONTACTS` | Resolve display names and search recipients |
| `READ_PHONE_STATE` | Enumerate active SIM subscriptions |
| `POST_NOTIFICATIONS` | Present notifications (runtime-requestable from API 33) |

`BROADCAST_SMS` and `BROADCAST_WAP_PUSH` are signature-level permissions. They are declared on the
corresponding receivers to restrict delivery to the system, and are not requested by the
application.

---

## Limitations

The following are known and intentional omissions in the present revision.

- **Multimedia messaging is not supported.** The WAP push receiver is inert, as decoding requires a
  PDU codec that has not been implemented. Consequently, inbound MMS is not persisted while this
  application holds the SMS role. MMS threads appear in the conversation list, but their contents
  are not rendered.
- **Contact photographs are not displayed.** Avatars present an initial, or a generic glyph where
  the address is not a saved contact.
- **Results are not paginated.** The conversation list is limited to 200 threads and a message
  thread to 500 messages.
- **Search is not implemented.**
- **The settings screen is a placeholder.**
- Conversations may be marked as favourites only from the message thread, not from the conversation
  list.
- No backup or export facility is provided.

---

## Licence

No licence has been declared for this repository. In the absence of a licence, default copyright
applies and no permissions are granted to third parties.
