# SafetyWristband — Smart Safety Wristband Monitor

Native Android app (Kotlin + Jetpack Compose) for a graduation project: real-time monitoring of a smart safety wristband via Firebase Realtime Database, with live map tracking, geofencing, alert history, and settings.

## Features

- **Real-time Dashboard** — Connection status ring with animated pulses, live telemetry (location, speed, altitude, GPS accuracy, movement, battery), color-coded metric icons
- **Live Map** — Free detailed map tiles via OpenFreeMap + MapLibre, auto-follow wristband, compact bottom info card
- **Geofencing** — Tap-to-set safe zone center, adjustable radius, inside/outside status with Haversine formula
- **Alert History** — Color-coded alerts (SOS, fall, band removal, geofence, connection) with staggered entrance animations
- **Settings** — Theme selector (Light/Dark/Auto), Firebase connection status, danger zone (clear history)
- **Multi-language** — English & Arabic (RTL support)
- **Dark/Light Theme** — Full Material 3 dynamic theming with custom cool UI colors

## Stack

- Kotlin 2.4.10, Jetpack Compose, Material 3
- MVVM + Clean Architecture (presentation / domain / data layers), Repository pattern
- Kotlin Coroutines + StateFlow
- Hilt for dependency injection
- Firebase Realtime Database (live wristband telemetry)
- **MapLibre Native** (free, open-source maps engine)
- **OpenFreeMap** (free vector tiles — full street detail)
- Room (local alert history), DataStore Preferences (settings + geofence config)
- Gradle Kotlin DSL (`build.gradle.kts`) + Version Catalog (`gradle/libs.versions.toml`)
- AGP 9.3.1, minSdk 26, targetSdk / compileSdk 36

## Setup

### 1. Open in Android Studio

Open the project folder directly in Android Studio (Quail | 2026.1.1 or newer). Let it sync — the Gradle wrapper JAR isn't checked in; Android Studio will offer to regenerate it, or run `gradle wrapper` once if you have a local Gradle install.

### 2. Firebase

- Create a Firebase project, add an Android app with package name `com.safetywristband.tracker`.
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

### 3. MapLibre + OpenFreeMap (No API key needed!)

The app uses **MapLibre Native** with **OpenFreeMap** free vector tiles. **No Google Maps API key, no billing account, no signup required.**

The map style URL is hardcoded in both `LiveMapScreen.kt` and `GeofenceScreen.kt`:
```kotlin
"https://tiles.openfreemap.org/styles/liberty"
```

### 4. Build & run

Select a device/emulator and run the `app` configuration. The app works on both physical devices and emulators.

## Architecture / folder structure

```
app/src/main/java/com/safetywristband/tracker/
  SafetyWristbandApplication.kt    # MapLibre one-time initialization
  di/                            # Hilt modules (Firebase, Room, DataStore, repository bindings)
  domain/                        # model/ repository interfaces/ usecase/ — pure Kotlin, no Android deps
  data/                          # remote/ (Firebase data source + DTOs), local/ (Room + DataStore), repository/ (impls)
  presentation/
    theme/                       # Color.kt, Theme.kt, Type.kt — Material 3 + custom cool colors
    navigation/                  # NavHost, bottom bar, screen definitions
    components/                  # Reusable UI (LoadingIndicator, ErrorView)
    dashboard/                   # DashboardScreen — connection ring, metrics, alerts
    map/                         # LiveMapScreen — MapLibre map, auto-follow, compact info card
    geofence/                    # GeofenceScreen — tap-to-set, radius slider, save button
    alerts/                      # AlertsHistoryScreen — color-coded alert cards
    settings/                    # SettingsScreen — theme, connection, danger zone
```

Each screen follows the same pattern: `XyzViewModel` (StateFlow-based UI state) + `XyzScreen` (stateless Compose UI reading `collectAsState()`).

## UI Design System

### Cool Dashboard Colors (Theme-aware)
The dashboard uses a custom "cool" color scheme that adapts to light/dark themes:

| Element | Dark Theme | Light Theme |
|---------|-----------|-------------|
| Background | `#0B1120` (deep navy) | `#F0F4F8` (soft gray) |
| Cards | `#151B2B` (dark surface) | `#FFFFFF` (white) |
| Text Primary | `#E2E8F0` (off-white) | `#1A202C` (near-black) |
| Teal Accent | `#00D9C0` (bright cyan) | `#0D9488` (deep teal) |
| Orange Accent | `#FFA726` (vibrant) | `#DD6B20` (burnt orange) |

### Alert Color Coding
Each alert type has a unique vibrant accent color that works in both themes:

| Alert Type | Color | Hex |
|-----------|-------|-----|
| SOS / Fall | 🔴 Emergency Red | `#F43F5E` |
| Band Removed | 🟠 Warning Amber | `#F59E0B` |
| Geofence Exit | 🟣 Violet | `#8B5CF6` |
| Geofence Enter | 🟢 Success Green | `#10B981` |
| Connection Lost | 🔵 Info Blue | `#3B82F6` |
| Connection Restored | 🔷 Cyan | `#06B6D4` |

### Metric Icon Colors
Icons change color based on thresholds (no tile highlighting):

| Metric | Safe | Warning | Critical |
|--------|------|---------|----------|
| Speed | 🟢 Green (≤2 m/s) | 🟠 Amber (>2) | 🔴 Red (>5) |
| GPS Accuracy | 🟢 Green (≤5m) | 🟠 Amber (>5m) | 🔴 Red (>20m) |
| Battery | 🟢 Green (≥50%) | 🟠 Amber (<50%) | 🔴 Red (<20%) |

## Localization

The app supports **English** (default) and **Arabic** (RTL). String resources are in:
- `app/src/main/res/values/strings.xml` — English
- `app/src/main/res/values-ar/strings.xml` — Arabic

To add a new language, create a new `values-XX` folder with a `strings.xml` file.

## Notes

- Battery simulation is local-only (drains 85%→0% over 100 minutes, loops). Replace `rememberBatterySimulation()` with real data when available.
- The geofence check uses the Haversine formula (`util/DistanceCalculator.kt`) exclusively — no Google Geofencing API is used.
- Alert history is local-only (Room), matching the requirement for a local chronological event log.
