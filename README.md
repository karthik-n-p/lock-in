# Lock-In Android App

A modern Android application built with Kotlin, Jetpack Compose, CameraX, and Google ML Kit Pose Detection. This repository demonstrates a real-time camera-based motion or posture tracking app with local persistence and background task support.

## Key Features

- **Android Kotlin App** using `org.jetbrains.kotlin.android` and Android Gradle Plugin `8.13.2`
- **Jetpack Compose UI** for fast, declarative user interfaces
- **CameraX Integration** for live camera capture and preview
- **ML Kit Pose Detection** for body pose analysis and detection
- **Room Database** support with Kotlin Symbol Processing (KSP) for local data storage
- **WorkManager** background processing support
- **Android 17 / Java 17** compatibility

## Project Structure

- `app/` - Android app module
- `app/src/main/AndroidManifest.xml` - app manifest with camera and notification permissions
- `app/build.gradle.kts` - module build configuration and dependencies
- `gradle/libs.versions.toml` - version catalog for plugins and libraries
- `settings.gradle.kts` - Gradle project settings

## Technical Stack

- Kotlin + Android SDK
- Jetpack Compose
- CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`)
- Google ML Kit Pose Detection (`pose-detection`, `pose-detection-accurate`)
- Room Persistence Library (`room-runtime`, `room-ktx`, `room-compiler`)
- WorkManager (`work-runtime-ktx`)
- AndroidX Navigation Compose
- AndroidX Lifecycle + ViewModel Compose

## Build & Run

1. Clone the repository:

   ```bash
   git clone https://your-repo-url.git
   cd lock-in
   ```

2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Run the `app` module on an Android device or emulator with camera support.

## Permissions

This app requests the following runtime permissions:

- `android.permission.CAMERA`
- `android.permission.POST_NOTIFICATIONS`

The manifest also declares optional camera hardware support using `android.hardware.camera`.

## Notes

- The application package namespace is `in.karthiknp.myapplication`.
- Minimum supported API level is `26` and target SDK is `34`.
- Compose compiler extension version is `1.5.11`.

## SEO Keywords

Android Kotlin app, Jetpack Compose, CameraX, ML Kit Pose Detection, Room Database Android, WorkManager Android, real-time pose tracking, Android developer sample, Android camera app, Kotlin Android project.

## License

This repository does not include a license file. Add a license if you want to share or distribute the code publicly.
