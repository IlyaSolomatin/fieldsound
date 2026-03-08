# FieldSound

Android app that turns your phone's magnetometer into an audible instrument. Point your device at magnetic fields and hear them as sound — stronger fields produce higher-pitched tones.

## How it works

**Sensor** — `MagnetometerReader` registers a `TYPE_MAGNETIC_FIELD` sensor listener at game-rate (~20 ms). Each reading provides x/y/z components in microtesla; the magnitude `sqrt(x² + y² + z²)` is emitted as a `StateFlow<Float>`.

**Mapping** — `FrequencyMapper` linearly maps the magnitude from the 0–200 µT range to 100–4000 Hz. Values outside the range are clamped.

**Audio** — `SineWaveGenerator` runs a dedicated high-priority thread that streams PCM float samples to an `AudioTrack` (44100 Hz, mono, low-latency mode). Two techniques keep the output click-free:
- *Phase continuity* — a `Double` phase accumulator is never reset, only wrapped modulo 2π, so frequency changes bend the waveform smoothly.
- *Exponential smoothing* — the current frequency interpolates toward the target per-sample (`α = 0.002`), reaching 99% of a step change in ~57 ms.

**UI** — single-screen Jetpack Compose app showing the current µT reading, mapped Hz, a progress bar, and a start/stop toggle. Audio and sensor automatically stop on pause.

## Requirements

- Android 8.0+ (API 26)
- Device with a magnetometer (most phones have one; emulators do not)
- No special permissions needed

## Building

```
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`
