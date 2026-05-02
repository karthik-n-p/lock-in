# Lock-In Android App

Lock-In is a Kotlin-based Android fitness application focused on helping users track pushups and plank workouts using real-time camera pose detection. The app blends Jetpack Compose UI, CameraX live preview, ML Kit pose analysis, and on-device progress tracking to create an interactive strength training experience.

## What This App Does

- Tracks **pushup repetitions** using camera-based pose detection and an elbow-angle state machine.
- Tracks **plank hold duration** with live posture validation and a timer that only runs while good form is detected.
- Provides **real-time feedback** for pushup and plank form, including posture corrections and angle metrics.
- Saves workout sessions locally using **Room database persistence**.
- Includes progress, workout history, and achievement screens for pushup and plank goals.

## Core Features

- **Pushup Rep Counting**
  - Counts reps when the user starts from full extension, lowers into a valid down position, and returns to extension.
  - Validates full body position, hip alignment, and elbow angle before counting a rep.
  - Displays live elbow angle feedback and "ghost" posture overlay for better form.

- **Plank Hold Tracking**
  - Detects plank posture from the front camera in portrait mode.
  - Validates shoulder and hip alignment, body angle, and holding stability.
  - Starts the plank timer only when good form is maintained.
  - Shows live body angle, shoulder angle, and form issues such as hips too high or low.

- **Real-Time Pose Guidance**
  - Uses Google ML Kit Pose Detection to extract landmarks in real time.
  - Displays a skeleton overlay plus ideal posture ghost hints.
  - Provides instant feedback messages like "Keep hips level!" and "Fire rep X!"

- **Local Persistence & Progress**
  - Stores workout sessions with rep count and plank duration.
  - Tracks cumulative pushup totals and plank hold seconds.
  - Includes achievements for milestone goals like 50/100/200 pushups and 30/60/120 second planks.

- **Modern Android Architecture**
  - Built with **Jetpack Compose** for UI.
  - Uses **CameraX** for camera feed and image analysis.
  - Integrates **Room** and **KSP** for local storage.
  - Supports **WorkManager** for periodic reminders and background tasks.

## App Flow

1. **Home Screen**
   - Start a pushup session.
   - Start a plank session.
   - Review progress and achievements.

2. **Workout Screen**
   - Live camera preview with skeleton and posture overlays.
   - Mode selector for `PUSHUPS` or `PLANK`.
   - Central stats display for reps or plank time.
   - Instant feedback badge and finish button.

3. **Progress & Achievements**
   - View workout history and cumulative performance.
   - Unlock badges for pushup and plank milestones.

## Technical Overview

- Android Gradle Plugin: `8.13.2`
- Kotlin version: `1.9.23`
- Compose compiler extension: `1.5.11`
- Min SDK: `26`
- Target SDK: `34`
- Package namespace: `in.karthiknp.myapplication`

### Key Modules

- `app/src/main/java/in/karthiknp/myapplication/pose`
  - `PushupDetector.kt` — pushup rep counting and form analysis.
  - `PlankDetector.kt` — plank posture detection and hold timer.

- `app/src/main/java/in/karthiknp/myapplication/ui/screens/workout`
  - `WorkoutViewModel.kt` — workout state management and feedback.
  - `WorkoutScreen.kt` — Compose UI for workout tracking.

- `app/src/main/java/in/karthiknp/myapplication/camera`
  - `CameraPreview.kt` — live camera feed integration.
  - `PoseAnalyzer.kt` — ML Kit image analysis pipeline.

- `app/src/main/java/in/karthiknp/myapplication/data/local`
  - Room database schema and entity models.
  - Workout history and achievement persistence.

## Installation

1. Clone the repository:

   ```bash
   git clone https://your-repo-url.git
   cd lock-in
   ```

2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Run the `app` module on a device with a camera.

## Permissions

This app requires runtime permission for:

- `android.permission.CAMERA`
- `android.permission.POST_NOTIFICATIONS` (Android 13+)

The manifest also marks camera hardware as optional so the app can gracefully handle devices without a rear camera.

## Usage Tips

- For **pushups**, place the phone to the side so the full body is visible from head to ankles.
- For **planks**, position the front camera at chest height so shoulders and hips are clearly visible.
- Keep the frame stable and ensure good lighting for accurate pose detection.

## Keywords

pushup tracker app, plank workout tracker, Android pose detection app, camera-based fitness app, ML Kit plank timer, ML Kit pushup counter, Jetpack Compose fitness app, CameraX workout tracker, Android workout progress, pushup and plank form feedback.

## License

No license file is included in this repository. Add a license if you plan to publish or share this project publicly.
