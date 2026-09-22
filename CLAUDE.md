# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## ⚠️ README.md is stale — do not trust it

`README.md` describes a fully-implemented SMS client (Hilt, Room, WorkManager, Telephony sync,
notifications, MMS plans, `data/`/`domain/`/`framework/` packages). **None of that exists.** All of
it was deleted in commit `b8b7844 "project setup"`, which reset the repo to a fresh scaffold with a
different DI framework, a different package layout, and no SMS functionality at all.

The current tree is ~15 Kotlin files. Read the source, not the README. The README is useful only as
a statement of product intent (an SMS/MMS client), and its dependency table, architecture diagram,
and milestone checklist are all wrong.

## Build

```bash
./gradlew :app:compileDebugKotlin   # fast compile check
./gradlew assembleDebug             # debug APK
./gradlew installDebug              # install on connected device
./gradlew test                      # host unit tests (app/src/test)
./gradlew connectedAndroidTest      # instrumented tests (app/src/androidTest), needs a device
./gradlew lintDebug
```

Single test: `./gradlew test --tests "com.abrarshakhi.smsman.ExampleUnitTest.addition_isCorrect"`

Toolchain: JDK 25 for the Gradle daemon (pinned in `mise.toml`), but Kotlin/Java compile to **17**
(`jvmTarget`/`sourceCompatibility` in `app/build.gradle.kts`). `compileSdk`/`targetSdk` 37,
`minSdk` 30. Gradle 9.6.0, AGP 9.4.x, Kotlin 2.4.20.

Gradle notes: the configuration cache is **on** (`gradle.properties`), so build-script edits force a
reconfigure. `settings.gradle.kts` sets `FAIL_ON_PROJECT_REPOS` — declare repositories only in
`settings.gradle.kts`, never in a module. All versions go through `gradle/libs.versions.toml`
(`libs.*` accessors); don't hardcode versions in `app/build.gradle.kts`.

AGP 9 conventions in use: R8 keep rules live in `app/src/main/keepRules/rules.keep` (not
`proguard-rules.pro`), and release optimization is currently **disabled**
(`buildTypes { release { optimization { enable = false } } }`).

## Running on a device (important)

This app holds the SMS role. **`./gradlew installDebug` drops the role and Android then
auto-revokes every SMS permission**, so the app falls back to onboarding after each install.
Restore it with one command — re-holding the role re-grants the SMS permissions automatically:

```bash
./gradlew installDebug
adb shell cmd role add-role-holder android.app.role.SMS com.abrarshakhi.smsman
```

`adb shell pm grant` does **not** work on the ColorOS test device (shell lacks
`GRANT_RUNTIME_PERMISSIONS`), so the role command is the only scripted path; otherwise permissions
must be granted through the onboarding UI.

Useful checks:

```bash
adb shell cmd role get-role-holders android.app.role.SMS
adb shell dumpsys package com.abrarshakhi.smsman | grep -oE "android.permission.[A-Z_]+: granted=[a-z]+" | sort -u
adb exec-out screencap -p > /tmp/shot.png
```

Reverting to the previous SMS app: Settings → Apps → Default apps → SMS app.

## Architecture

Single module (`:app`), package `com.abrarshakhi.smsman`, split three ways:

- `common/` — app shell: `MainActivity`, `SmsmanApplication` (starts Koin), `main/` (root scaffold
  + chrome contract), `navigation/`, `ui/`, `util/`, `di/`.
- `core/` — shared infrastructure: `telephony/` (provider reads, receivers, service),
  `database/` (Room), `repository/`, `permissions/`, `model/`, `di/`.
- `features/<name>/presentation/` — one package per feature (`conversations` backs both the All and
  Favorite tabs, plus `pinned`, `chat`, `newmessage`, `onboarding`, `settings`). Each owns a
  ViewModel, screen, chrome and Koin module.

### The Chrome pattern (the main thing to understand)

There is exactly **one** `Scaffold`, in `common/main/AppRoot.kt`. Screens do not own their own
scaffold, top bar, bottom bar or FAB. Each route contributes a `ScreenChrome`
(`common/main/ScreenChrome.kt`) — `title`, `topBar`, `bottomBar`, `fab` — and `AppRoot` renders the
chrome for whatever route is on top of the back stack. A shared `pinnedScrollBehavior` is hoisted in
`AppRoot` and reset on every route change.

This is why "FAB only on the All messages tab" needs no conditional: `allMessagesChrome()` supplies
a `fab`, and the Favorite/Pinned chromes leave it at its empty default.

Adding a screen means touching three places:

1. Add the route to `AppRouteKey` (`common/navigation/AppRouteKey.kt`) — `@Serializable`,
   implements `NavKey`. Bottom-bar destinations implement the `AppRouteKey.HomeTab` sub-interface.
2. Add a branch to `AppRouteKey.chrome()` in `ScreenChrome.kt`. The `when` is exhaustive, so the
   compiler catches a missing branch. These factories are plain functions, not composables.
3. Register the screen in the feature's Koin module with `navigation<AppRouteKey.X> { }`.
   **This step has no compile-time check** — a route with chrome but no registration throws
   `IllegalStateException("Unknown screen …")` at navigation time. Step 2 is the safety net.

### Navigation

Navigation 3 (`NavDisplay`), not Navigation Compose, with entries assembled by Koin's
`koinEntryProvider<AppRouteKey>()` from each feature's `navigation<T> {}` declarations. Both are
`@KoinExperimentalAPI`.

The back stack is a plain `SnapshotStateList<AppRouteKey>` — see
`common/navigation/BackStackController.kt`:

- `navigateTo(dest)` — push
- `back()` — pop, guarded so the stack never empties
- `switchTapTo(dest)` — clear and replace; used by the bottom bar, so every tab sits at depth 1
- `currentRoute()` — top of stack

`NavDisplay` only installs its back handler while previous entries exist, so back at a tab root
falls through to the system and exits the app.

Screens reach the back stack through **`LocalAppBackStack`** (provided by `AppRoot`), because Koin's
`navigation<T> {}` builder is `@Composable Scope.(T) -> Unit` and has no parameter slot for it.

Persistence across process death goes through `AppRouteBackStackSaver`, a `listSaver` that
kotlinx-serializes each route, falling back to `AllMessages`. Every `AppRouteKey` must stay
`@Serializable`. Note `kotlinx-serialization-json` is a direct dependency for this reason.

### Data layer

The **Telephony provider is the source of truth** for messages; they are never mirrored into Room.
Room holds only metadata the provider cannot express (favourite per thread, pin per message).

Hard-won constraints, all verified on a real device — violating them causes silent breakage:

- **Always pass an explicit projection and tolerate missing columns** (`core/telephony/CursorExt.kt`).
  The test device runs a ColorOS-customised provider returning vendor columns (`oplus_unread_count`,
  `rcs_top`, a vendor `favourite` column on messages) that do not exist on other devices.
- `content://mms-sms/conversations?simple=true` returns the threads table directly. The parameter is
  **undocumented** — absent from the SDK sources — so guard it and fall back.
- `LIMIT` must be appended to the sort order; the legacy provider never receives
  `QUERY_ARG_SQL_LIMIT`.
- `recipient_ids` are ids into `content://mms-sms/canonical-addresses`, not addresses. That URI has
  no public constant.
- `ThreadsColumns` has **no unread-count column**; counts come from the message table.
- Filter `message_count > 0` — empty stub threads exist and render blank.
- Orphan SMS rows with a null `thread_id` exist; skip them.
- `sub_id` is **not** a slot index (live values here are 2 and 3). Resolve via
  `SubscriptionManager`, and expect ids belonging to removed SIMs.
- Addresses are frequently alphanumeric shortcodes ("GP Combo"), so `PhoneLookup` misses are normal,
  and number formatting must not be applied blindly. Do **not** pre-normalise before `PhoneLookup`.
- Bengali message bodies force UCS-2, i.e. **70 chars per SMS segment, not 160**. Use
  `SmsMessage.calculateLength`, never `length / 160`.

`TelephonyChangeObserver` watches `content://mms-sms/` with descendants and debounces, because one
inbound message fires several `notifyChange` calls. It is only a freshness signal for visible UI —
it dies with the process; durability comes from `SmsDeliverReceiver`.

### Being the default SMS app

Holding the SMS role means **the platform stops writing inbound SMS to the provider** —
`core/telephony/receiver/SmsDeliverReceiver` must persist it or messages are lost. The four
components required for role eligibility are declared in `AndroidManifest.xml`; `BROADCAST_SMS` and
`BROADCAST_WAP_PUSH` are signature-level and sit on the receivers, never in `<uses-permission>`.

## Current state

Working: Koin DI, bottom-nav tabs, the SMS role and permission flow, inbound SMS persistence, Room
metadata, and the conversation list reading real provider data.

Not built yet: the chat screen, pin/favourite UI, sending SMS (so the app cannot send at all yet),
and the new-message screen. `MmsWapPushReceiver` is deliberately inert, so **incoming MMS is not
persisted** while this app is default; the test device has 0 MMS rows. `DataStore` is declared but
unused.

## Conventions

- Kotlin official code style (`kotlin.code.style=official`).
- Feature code under `features/<name>/`; cross-feature infrastructure under `core/`; app shell under
  `common/`. `common/` must not depend on `features/` except `ScreenChrome.kt`, the one intentional
  inversion point.
