# Fluid Tracker Pro

<div align="center">

<img src="app/src/main/res/drawable/app_logo.png" alt="Fluid Tracker Pro Logo" width="140" style="border-radius: 28px;" />

### A Luxurious 3D Minimalist Liquid-Glass Hydration & Wellness Ecosystem for Android

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026--35)-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20On--Device%20DataStore-brightgreen.svg)](app/src/main/AndroidManifest.xml)
[![Permissions](https://img.shields.io/badge/Network-0%20Internet%20Permissions-success.svg)](app/src/main/AndroidManifest.xml)
[![Author](https://img.shields.io/badge/Developed%20By-S.%20M.%20Mahmud%20Iqbal-6366f1.svg)](https://github.com/SMMahmudIqbal)

</div>

---

## Overview

**Fluid Tracker Pro** is a state-of-the-art Android hydration and wellness application crafted with a **translucent liquid-glass aesthetic**, real-time fluid dynamics, and comprehensive habit analytics. Built natively with modern **Jetpack Compose** and **Material 3**, it delivers a tactile, fluid user experience with zero emojis, elegant geometric visual accents, and 100% private, on-device local storage.

---

## Visual Design & Architecture

The application is architected around two synchronized core interfaces:

### 1. Hydro Vessel Experience (Interactive Fluid Tracker)
* **Real-Time Wave Simulation**: Double-sine fluid wave dynamics rendered smoothly via Jetpack Compose `Canvas`.
* **Hardware Gyroscope & Accelerometer Physics**: Slosh amplitude and liquid tilt dynamically respond to physical device movements with a low-pass filter for silky smooth fluidity.
* **Live Interval CountDown Timer**: Displays remaining time until your next target hydration milestone.
* **Instant Volume Quick-Adds**: One-tap logging (150 ml, 250 ml, 330 ml, 500 ml) plus custom intake input.
* **Fluid Compression Feedback**: Dynamic fluid squash and spring physics on water intake logging.

### 2. Analytics & Streak Dashboard
* **Hero Circular Dial**: 260-degree luminous sweep arc tracking today's progress against your daily target.
* **Horizontal Stat Capsules**: Instant glance metrics for Current Streak, Target %, Vitality Points, and Total Log Count.
* **7-Day Spline Activity Curve**: Bezier spline trend line charting intake volume across the week with glowing circular nodes.
* **Hydration Profile Management**: Customize your username, view dynamic monogram avatars, and manage records.
* **Intake Log History**: Individual chronological drink entries with one-tap removal.

---

## Liquid Glass Home Screen Widget Suite

Fluid Tracker Pro features a complete suite of three interactive home screen widgets, styled with authentic **frosted acrylic translucency (50% alpha)**, specular meniscus highlights, and real-time bidirectional synchronization:

| Widget | Dimensions | Highlights |
| :--- | :--- | :--- |
| **Card Hub** | `4 × 2` | Comprehensive dashboard displaying current volume (`1,250 ml`), target readout (`of 2,000 ml (63%)`), dynamic streak badge (`5D STREAK`), user profile badge, smooth liquid progress bar, and 1-tap quick log button. |
| **Quick Glass Orb** | `2 × 2` / `1 × 1` | 3D circular orb with liquid refraction glow, showing percentage (`63%`), current volume, and 1-tap quick add. |
| **Minimal Glass Pill** | `3 × 1` / `4 × 1` | Ultra-compact horizontal glass capsule with intake readout, streak pill, horizontal water gauge, and quick-add button. |

*Tapping any widget or logging in-app updates all widgets and app screens simultaneously without launcher delays.*

---

## 12 Luxury Bespoke Themes

Every screen, modal, sheet, and home screen widget seamlessly adapts across 12 meticulously crafted themes:

1. **Celestial Blue** *(Signature deep cyan-blue crystal)*
2. **Deep Ocean** *(Abyssal sapphire blue with high contrast)*
3. **Aero Glass** *(Translucent sky glass with cyan highlights)*
4. **Cyber Neon** *(High-energy electric cyan and neon blue)*
5. **Pure Light** *(Pristine frosted white & glacier blue)*
6. **OLED Black** *(True #000000 pitch black for battery optimization)*
7. **Emerald Aura** *(Botanical mint-sage glass)*
8. **Sunset Velvet** *(Warm amber-coral twilight)*
9. **Amethyst Dusk** *(Neon violet-magenta gradient)*
10. **Obsidian Frost** *(Monochrome midnight slate)*
11. **Rose Gold** *(Warm luxury copper crystal)*
12. **Aurora Borealis** *(Electric teal-violet radiance)*

---

## Viral "Hydration Wrapped" Story Generator

* In-memory 1080×1920 HD graphic compilation combining fluid wave canvas, concentric ripple rings, and milestone badges.
* Native Android share intent for direct posting to Instagram Stories, WhatsApp, Snapchat, or Telegram via secure `FileProvider`.

---

## 100% On-Device Privacy

Fluid Tracker Pro is built with an uncompromising commitment to privacy:
* **Zero Network Permissions**: The app does **not** declare `android.permission.INTERNET`. It cannot connect to any external server.
* **No Analytics or Trackers**: Completely free of Firebase, telemetry SDKs, trackers, or advertising beacons.
* **Local DataStore Persistence**: Hydration records and preferences are encrypted and stored solely on your device via **Android Jetpack DataStore**.
* **Complete User Ownership**: Export your data or wipe all records with a single tap.

See [PRIVACY_POLICY.md](PRIVACY_POLICY.md) for full legal documentation.

---

## Installation & Google Play Protect

When installing the release APK downloaded from GitHub, your Android device may display:
> **"Blocked by Play Protect: Play Protect doesn't recognize this app's developer."**

### Why This Happens
Google Play Protect automatically flags any newly released APK installed outside the Google Play Store whose private cryptographic certificate has not yet been indexed in Google's commercial database. Because Fluid Tracker Pro is an open-source project hosted on GitHub, this is standard behavior.

### How to Install Cleanly
1. When the **"Blocked by Play Protect"** dialog appears, tap **"More details"** (or the small arrow `⌵`).
2. Tap **"Install anyway"**.
3. The app will install cleanly, grant full home screen widget functionality, and your phone will remember your approval.

---

## Tech Stack & Architecture

* **Language:** Kotlin 1.9+
* **UI Toolkit:** Jetpack Compose (BOM 2024.02.00)
* **Design System:** Material 3 with Custom Translucent Glass Shaders
* **Architecture:** Clean Architecture / MVVM with Kotlin Coroutines & StateFlow
* **Data Persistence:** AndroidX DataStore Preferences
* **Hardware Integration:** Android `SensorManager` (Accelerometer & Low-Pass Filter)
* **Background Work:** Android `AlarmManager` & Broadcast Receivers
* **Media Generation:** Native Android `Canvas`, `Bitmap`, and `FileProvider`

---

## Building from Source

### Prerequisites
* Android Studio Iguana / Jellyfish (or Gradle 8.7+)
* JDK 17 (Microsoft OpenJDK / Oracle / Temurin)
* Android SDK (Compile target: API 35, Minimum: API 26)

### Command Line Build

1. **Clone the repository:**
   ```bash
   git clone https://github.com/SMMahmudIqbal/fluid-tracker-pro.git
   cd fluid-tracker-pro
   ```

2. **Build Debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Build Signed Release APK:**
   ```bash
   ./gradlew assembleRelease
   ```
   The compiled APK will be output to:
   `app/build/outputs/apk/release/app-release.apk`

---

## Release Verification & Keystore

Official release builds are signed with dual **APK Signature Scheme v2 & v3**:
* **Signer:** `CN=S. M. Mahmud Iqbal, OU=Mobile, O=FluidTracker, L=Dhaka, C=BD`
* **Algorithm:** RSA 2048-bit with SHA-256 digest
* **Validity:** Through 2054 (28 years)
* **Zipalign:** 4-byte boundary verified

---

## License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

<div align="center">
Architected & Developed with pride by <strong>S. M. Mahmud Iqbal</strong>.
</div>
