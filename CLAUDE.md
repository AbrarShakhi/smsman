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

## Architecture

Single module (`:app`), package `com.abrarshakhi.smsman`, split two ways:

- `common/` — app shell owned by nobody in particular: `MainActivity`, `SmsmanApplication`,
  `main/` (root scaffold + chrome contract), `navigation/`, `ui/theme/`.
- `features/<feature>/presentation/` — one package per feature (`home`, `chat`, `onboarding`,
  `settings`). Only `presentation/` layers exist so far; data/domain layers are unwritten.

### The Chrome pattern (the main thing to understand)

There is exactly **one** `Scaffold`, in `common/main/AppRoot.kt`. Screens do not own their own
scaffold, top bar, or FAB. Instead each route contributes a `ScreenChrome`
(`common/main/ScreenChrome.kt`) — a data class of `title`, a `topBar` composable, and a `fab`
composable — and `AppRoot` renders the chrome belonging to whatever route is on top of the back
stack. A shared `pinnedScrollBehavior` is hoisted in `AppRoot` and reset on every route change.

Wiring a new screen therefore means touching three places:

1. Add the route to the `AppRouteKey` sealed interface (`common/navigation/AppRouteKey.kt`) —
   `@Serializable`, implements `NavKey`.
2. Add a branch to `AppRouteKey.chrome()` in `ScreenChrome.kt` (exhaustive `when`; the compiler
   will catch a missing branch), returning a `xxxChrome()` factory from the feature's
   `presentation/` package. Note these factories are plain functions, not composables.
3. Register the entry in the `entryProvider { }` block in `common/navigation/AppNavigation.kt`.

### Navigation

Navigation 3 (`NavDisplay`), not Navigation Compose. The back stack is a plain
`SnapshotStateList<AppRouteKey>` that the app manages itself — see
`common/navigation/BackStackController.kt` for the vocabulary:

- `navigateTo(dest)` — push
- `back()` — pop, guarded so the stack never empties
- `switchTapTo(dest)` — clear and replace (tab-style switch)
- `currentRoute()` — top of stack

Persistence across process death goes through `AppRouteBackStackSaver`, a `listSaver` that
kotlinx-serializes each `AppRouteKey`; a route that fails to decode is dropped and an empty result
falls back to `Home`. This is why every `AppRouteKey` member must stay `@Serializable`.

`NavDisplay` is configured with `rememberSaveableStateHolderNavEntryDecorator()` and
`rememberViewModelStoreNavEntryDecorator()`, so per-entry ViewModels are scoped to the nav entry.

## Current state — known incomplete wiring

These are deliberate holes in the scaffold, not bugs to be surprised by:

- **Koin is not initialized.** `koin-android` is a dependency and `MainActivity` calls
  `koinViewModel()`, but there is no module definition and no `startKoin` anywhere —
  `SmsmanApplication` is an empty `Application` subclass. The app will throw at launch until a
  Koin module is defined and started from `SmsmanApplication`.
- **`entryProvider { }` is empty** in `AppNavigation.kt`, so `NavDisplay` renders nothing. No
  feature screen composables exist yet — the `features/*/presentation/` packages contain only
  `*Chrome.kt` files with empty top bars and FABs.
- **Ktor and DataStore are declared but unused.** No `HttpClient`, no `DataStore` usage in source.
- **KSP is applied but has no processors** (`kspDebugKotlin` is SKIPPED).
- **No SMS anything.** The manifest declares only a LAUNCHER activity — no SMS permissions, no
  receivers, no default-SMS-app intent filters.
- Theme is the unmodified Android Studio template (Purple/Pink placeholder colors, single
  `bodyLarge` typography override).

## Conventions

- Kotlin official code style (`kotlin.code.style=official`).
- Feature code goes under `features/<name>/`; anything shared by more than one feature goes under
  `common/`. Keep `common/` from depending on `features/` except in `ScreenChrome.kt`, which is the
  one intentional inversion point.
