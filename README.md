# SafeWristband — Smart Safety Wristband Monitor

Native Android app (Kotlin + Jetpack Compose) for a graduation project: real-time monitoring of a smart safety wristband via Firebase Realtime Database, with live map tracking, geofencing, alert history, and settings.

This project lives outside the Replit-managed workspace packages — it is a standalone Android Studio / Gradle project. Download this `android-wristband-app/` folder and open it directly in Android Studio.

## Stack

- Kotlin 2.4.0, Jetpack Compose, Material 3
- MVVM + Clean Architecture (presentation / domain / data layers), Repository pattern
- Kotlin Coroutines + StateFlow
- Hilt for dependency injection
- Firebase Realtime Database (live wristband telemetry)
- Google Maps Compose (live map + geofence drawing)
- Room (local alert history), DataStore Preferences (settings + geofence config)
- Gradle Kotlin DSL (`build.gradle.kts`) + Version Catalog (`gradle/libs.versions.toml`)
- AGP 9.2.1, minSdk 26, targetSdk / compileSdk 36

## Setup

1. **Open in Android Studio** (Quail | 2026.1.1 or newer). Let it sync — the Gradle wrapper JAR isn't checked in; Android Studio will offer to regenerate it, or run `gradle wrapper` once if you have a local Gradle install.

2. **Firebase**
   - Create a Firebase project, add an Android app with package name `com.safewristband.tracker`.
   - Download the real `google-services.json` and place it at `app/google-services.json` (a placeholder template is at `app/google-services.json.example` — replace it, don't just rename it).
   - Enable **Realtime Database** in the Firebase console. Data is expected at:
     ```
     wristbands/
       wristband_01/
         latitude: Double
         longitude: Double
         altitude: Double
         accuracy: Double
         speed: Double
         movement: "STATIONARY" | "WALKING" | "RUNNING"
         fallDetected: Boolean
         bandRemoved: Boolean
         sos: Boolean
         timestamp: Long (epoch millis)
     ```
   - Set Realtime Database rules appropriately for your testing needs (the app currently reads without authentication, per the project's "no auth required" requirement — tighten rules before any real deployment).

3. **Google Maps API key**
   - Get an Android API key from Google Cloud Console with the "Maps SDK for Android" enabled.
   - Replace the placeholder in `app/src/main/res/values/strings.xml`:
     ```xml
     <string name="google_maps_api_key">YOUR_GOOGLE_MAPS_API_KEY_HERE</string>
     ```

4. **Build & run** — select a device/emulator with Google Play services and run the `app` configuration.

## Architecture / folder structure

```
app/src/main/java/com/safewristband/tracker/
  di/            Hilt modules (Firebase, Room, DataStore, repository bindings)
  domain/        model/ repository interfaces/ usecase/  — pure Kotlin, no Android deps
  data/          remote/ (Firebase data source + DTOs), local/ (Room + DataStore), repository/ (impls)
  presentation/  theme/ navigation/ components/ + one package per screen (dashboard, map, geofence, alerts, settings)
```

Each screen follows the same pattern: `XyzViewModel` (StateFlow-based UI state) + `XyzScreen` (stateless Compose UI reading `collectAsState()`).

## Extending to multiple wristbands / new sensors

- `WristbandRepository.observeWristbandData(wristbandId)` already takes a `wristbandId` — the Firebase path is `wristbands/{wristbandId}`, so adding a second wristband is just writing to a new child node and calling the same repository with a different ID.
- `SettingsRepository` persists a `selectedWristbandId`; wiring a wristband picker into the Settings screen (a dropdown that calls `setSelectedWristbandId`) is the only change needed to support switching between multiple bands from the UI.
- To add a new sensor field: extend `WristbandDto` and `WristbandData`, map it through `WristbandDto.toDomain()`, and surface it on `DashboardScreen` as an additional `StatusCard` — no repository or ViewModel contract changes required.

## Notes

- The geofence check uses the Haversine formula (`util/DistanceCalculator.kt`) exclusively — no Google Geofencing API is used, per the project requirement.
- Alert history is local-only (Room), matching the requirement for a local chronological event log.
- Runtime location permission requests (`ACCESS_FINE_LOCATION`) are declared in the manifest; wire up an Accompanist Permissions prompt in `LiveMapScreen`/`GeofenceScreen` before relying on the device's own location layer (the wristband's own GPS from Firebase does not require this permission — it's only needed if you later add "show my phone's location" on the map).
