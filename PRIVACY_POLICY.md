# Privacy Policy for Fluid Tracker Pro

**Effective Date:** October 2, 2026  
**Developer:** S. M. Mahmud Iqbal  
**Application:** Fluid Tracker Pro (Android)

---

## 1. Overview
Fluid Tracker Pro is designed from the ground up with a **local-first, privacy-by-default architecture**. We believe that your personal wellness and daily hydration records should remain strictly yours. 

The application collects **zero** personal telemetry, transmits **no data** over the network, and requires **no account registration or login**.

---

## 2. Information Storage & Processing
- **100% On-Device Local Storage:** All hydration logs, user profile names, daily water intake targets, streak records, vitality scores, and theme preferences are saved locally on your device using encrypted Android Jetpack DataStore.
- **No Cloud Servers:** Fluid Tracker Pro does not operate remote servers, cloud databases, or third-party synchronization services. Your data never leaves your device sandbox.
- **No Third-Party Analytics or Trackers:** The application contains zero advertising SDKs, tracking pixels, crash analytics beacons, or third-party telemetry tools.

---

## 3. Device Permissions & Sensor Usage
Fluid Tracker Pro requests only minimal, essential system permissions strictly required for core user-facing functionality:

| Permission / Sensor | Purpose | Network Access |
|---|---|---|
| `POST_NOTIFICATIONS` | Delivers local periodic hydration reminders configured by the user in Settings. | None (100% Local AlarmManager) |
| `RECEIVE_BOOT_COMPLETED` | Restores scheduled reminders upon device reboot. | None |
| `VIBRATE` | Provides optional haptic feedback when logging water intake. | None |
| `Hardware Accelerometer` | Calculates device tilt angle to simulate dynamic fluid wave surface slosh in real-time. | None (Data processed locally in memory) |

---

## 4. Social Sharing & Media Generation
When you use the "Hydration Wrapped" or "Share to Story" feature:
- The milestone summary graphic is rendered locally in-memory into an Android `Bitmap`.
- The temporary image file is written to your app's private cache directory and shared via standard Android `Intent.ACTION_SEND` using `FileProvider`.
- No image or story data is ever uploaded to any server by Fluid Tracker Pro.

---

## 5. Data Retention & Deletion
You retain complete control over your data at all times:
- Individual drink logs can be deleted directly from the Activity Log interface.
- Resetting daily counters can be done via the Hydro vessel screen.
- Clearing application data via Android System Settings (`Settings > Apps > Fluid Tracker > Storage > Clear Data`) permanently purges all local logs and preferences.

---

## 6. Open Source Transparency
Fluid Tracker Pro is open source and licensed under the **MIT License**. The entire source code is auditable on GitHub, allowing anyone to verify our privacy and security guarantees.

---

## 7. Contact & Inquiries
For questions or inquiries regarding this Privacy Policy or the open source application:
- **Lead Developer:** S. M. Mahmud Iqbal
- **Project Repository:** https://github.com/SMMahmudIqbal/fluid-tracker-pro
