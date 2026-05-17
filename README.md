# 🔥 Lock-In — AI-Powered Offline Fitness Tracker

> **Real-time pushup counting & plank tracking using on-device pose detection. Zero data leaves your phone.**

[![Android](https://img.shields.io/badge/Platform-Android-red?style=flat-square&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-E63946?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![ML Kit](https://img.shields.io/badge/ML%20Kit-Pose%20Detection-FF8C42?style=flat-square)](https://developers.google.com/ml-kit)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

---

## 🏋️ What Is Lock-In?

**Lock-In** is a premium, 100% offline Android fitness app that uses your phone's camera and Google ML Kit Pose Detection to **automatically count pushup reps** and **time plank holds** with real-time form feedback. No gym equipment, no subscriptions, no cloud — just you and your phone.

Built with **Jetpack Compose**, **CameraX**, and a psychology-driven UI designed to build lasting fitness habits through streak tracking, achievements, and motivational feedback loops.

---

## ✨ Features

### 🎯 AI Pose Detection
- **Pushup rep counting** — automatic detection using elbow angle state machine
- **Plank hold timer** — starts only when correct form is detected
- **2-second grace period** — timer pauses after 2s of form break, not instantly
- **Real-time form indicators** — Green (good) / Yellow (fix form) / Red (paused)
- **Ghost posture overlay** — see ideal body position overlaid on camera feed
- **Live angle metrics** — elbow, body, and shoulder angles displayed in real-time

### 🔥 Streak & Gamification
- **Unlimited day streaks** — no cap, streak grows as long as you show up
- **3 monthly Streak Fixes** — bridge a missed day without losing progress
- **Customizable day-end time** — night owls can set reset time up to 6 AM
- **Achievement badges** — unlock milestones for pushup and plank goals
- **Personal best celebrations** — animated overlay with motivational quotes
- **Daily goal progress bars** — visual pushup and plank targets

### 🎨 Premium Bento UI
- **Dark OLED-optimized** design with warm red-orange "Ember" palette
- **Bento grid layout** — modular, clean card-based interface
- **Real calendar streak map** — proper month view with day alignment
- **Spring animations** — bouncy, satisfying micro-interactions
- **Minimal history log** — clean rows with red accent bars

### 🔔 Smart Notifications
- **9 AM morning motivation** — curated quotes to start your day
- **8 PM streak warning** — reminder only if you haven't worked out
- **Achievement alerts** — celebrate badge unlocks
- Never spammy — notifications only fire when relevant

### 🔒 Privacy First
- **100% offline** — no internet required, ever
- **Zero data collection** — nothing leaves your device
- **No accounts** — no sign-up, no login, no tracking
- **Local Room database** — all data stored on-device only

---

## 📱 Screenshots

| Home | Workout | Progress | Achievements |
|------|---------|----------|--------------|
| Bento grid with streak, goals, calendar | Live camera with pose overlay | Avatar evolution, charts, records | Badge grid with glow effects |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **UI** | Jetpack Compose, Material 3 |
| **Camera** | CameraX (ImageAnalysis) |
| **AI/ML** | Google ML Kit Pose Detection |
| **Database** | Room + KSP |
| **Background** | WorkManager |
| **Architecture** | MVVM, StateFlow |
| **Language** | Kotlin 2.1 |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 34 (Android 14) |

---

## 📂 Project Structure

```
app/src/main/java/in/karthiknp/myapplication/
├── camera/              # CameraX preview & ML Kit analyzer
├── data/local/          # Room database, DAO, entities, preferences
├── pose/                # PushupDetector, PlankDetector algorithms
├── ui/
│   ├── components/      # PoseOverlay, GhostPostureOverlay
│   ├── screens/
│   │   ├── home/        # Bento grid home screen
│   │   ├── workout/     # Live workout with form tracking
│   │   ├── progress/    # Charts, avatar, consistency ring
│   │   ├── achievements/# Badge grid with animations
│   │   ├── history/     # Minimal workout log
│   │   └── settings/    # Day-end time, notifications
│   └── theme/           # Ember red-orange color system
├── util/                # ReminderWorker notifications
├── LockInWidgetProvider.kt  # Home screen widget
└── MainActivity.kt      # Navigation & permissions
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2024.1) or later
- JDK 17+
- Android device with camera (emulator works but limited)

### Build & Run

```bash
git clone https://github.com/karthik-n-p/lock-in.git
cd lock-in
./gradlew assembleDebug
```

Then install `app/build/outputs/apk/debug/app-debug.apk` on your device.

### Permissions Required
- `CAMERA` — for real-time pose detection
- `POST_NOTIFICATIONS` (Android 13+) — for smart workout reminders

---

## 💡 Usage Tips

| Exercise | Camera Position | Notes |
|----------|----------------|-------|
| **Pushups** | Side view, full body visible | Place phone 4-6 feet away at floor level |
| **Plank** | Front camera, chest height | Hold phone at arm's length or prop on surface |

- Ensure **good lighting** for accurate landmark detection
- Wear **fitted clothing** — baggy clothes can obscure joint landmarks
- The ghost overlay shows **ideal posture** — align your body to match

---

## 🎨 Design Philosophy

Lock-In uses a **psychology-driven "Ember" color palette**:

| Color | Hex | Purpose |
|-------|-----|---------|
| Cherry Red | `#E63946` | Urgency, passion — drives daily return |
| Warm Amber | `#FF8C42` | Energy, warmth — rewards & streaks |
| Soft Coral | `#FF6B6B` | Soft accent for secondary elements |
| Gold Reward | `#FFB74D` | Achievement dopamine — celebrations |
| Ember Black | `#0C0808` | Warm OLED-friendly dark background |

The **bento grid layout** creates a premium, organized feel while maximizing information density without visual clutter.

---

## 🗺️ Roadmap

- [ ] Rest day tracking with recovery tips
- [ ] Social sharing for milestones
- [ ] Custom workout goals per day
- [ ] More exercise types (squats, sit-ups)
- [ ] Wear OS companion app
- [ ] Weekly / monthly progress reports

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 🔑 Keywords

`pushup counter app` `plank timer app` `AI fitness tracker` `offline workout app` `Android pose detection` `ML Kit fitness` `camera workout tracker` `rep counter app` `form feedback fitness` `streak tracker` `habit building app` `Jetpack Compose fitness` `CameraX workout` `real-time pose estimation` `privacy-first fitness app` `OLED dark fitness app` `bento UI design` `gamified workout tracker`

---

<p align="center">
  <b>Built with 🔥 by <a href="https://github.com/karthik-n-p">Karthik N P</a></b><br>
  <sub>Lock In. Show Up. Get Stronger.</sub>
</p>
