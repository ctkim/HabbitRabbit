# HabbitRabbit

An Android habit tracker with weekly and monthly views, plus a home-screen widget for quick check-ins.

## Features

- Weekly and monthly habit grids
- Add, edit, reorder, and color-code habits
- Home-screen widget with one-tap toggling (Glance)
- Light/dark theme preference
- Local persistence via DataStore

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- Hilt for DI
- DataStore + kotlinx.serialization
- Glance for the app widget
- WorkManager for midnight widget refresh

## Build

```
./gradlew assembleDebug
```

Requirements: Android SDK 36, `minSdk` 34, JDK 11.

Open the project in Android Studio and run the `app` configuration on a device/emulator.
