# GaitXplore – Person Motion Measurement App

This project accompanies the MECN4006 report, delivering an Android app that records motion data to analyse human gait with a single smartphone Inertial Measurement Unit (IMU). The app captures linear acceleration, gyroscope, orientation (rotation vector), and GPS readings, then streams them to Firebase for later processing and mapping to gait phases.

## MECN4006 project at a glance
- **Goal:** Demonstrate that a single IMU in a smartphone can identify stance and swing phases for gait recognition, with applications in healthcare, security, and fitness.
- **Test conditions:** Relaxed walk, fast walk, and jogging with the phone either in the left pocket or held in the left hand.
- **Data collected:** 3-axis linear acceleration, gyroscope angular velocity, orientation (pitch/roll/yaw), GPS latitude/longitude, speed, and cumulative distance. Sampling defaults to 50 Hz and can be adjusted in the UI.
- **Processing approach:** Clean and interpolate raw traces, verify GPS paths, apply trapezoidal numerical integration to derive velocity/distance from IMU data, and align signals to stance/swing and arm-swing phases.
- **Key findings:** IMU-derived gait phases matched literature definitions; IMU distance closely tracked GPS distance; arm swing patterns were identifiable across speeds. A single-sensor setup proved practical for real-world gait analysis.
- **Recommendations:** Broaden participant diversity, add in-app data cleaning, and provide real-time feedback and UI refinements for broader usability.

## App workflow
1. **Start motion sensing:** Toggle “Run Motion Sensing” to register linear acceleration, rotation vector, gyroscope, and GPS listeners.
2. **Log a session:** Set a sampling rate (Hz) and tap “Log Motion into Database” to stream readings to Firebase under incremental `SensorData` sessions.
3. **Review data:** Data can be exported from Firebase for downstream processing (e.g., gait phase mapping as described in the report).
4. **Reset:** Use “Clear Database” to remove `SensorData*` nodes before a new experiment.

## Project structure
- `app/src/main/java/com/example/gaitxplore/MainActivity.kt` – Sensor capture, UI wiring, Firebase logging, GPS handling.
- `app/src/main/res/layout/activity_main.xml` – Dashboard showing live sensor readouts and controls.
- `app/google-services.json` – Firebase configuration placeholder; replace with your project settings.
- `MECN4006 OSCA KHOLOPHA ATK9 PROJECT.pdf` – Full report detailing methodology and results.

## Build and run
Prerequisites:
- Android Studio (AGP 8.7)
- Android SDK 34
- A JDK compatible with the configured Gradle plugin (JDK 17 recommended)

The Gradle wrapper is checked in as executable. If your checkout clears the execute bit, re-apply it before building:

```bash
# From the repository root
chmod +x gradlew
./gradlew assembleDebug   # or open in Android Studio and run the app
```

Deploy to a device or emulator with motion sensors and grant fine/coarse location permissions when prompted. Logging requires network access to reach Firebase. Supply your own `google-services.json` if you are using a different Firebase project.

## Testing
Run unit tests with:
```bash
./gradlew test
```
Instrumentation tests can be run from Android Studio on a connected device or emulator.
