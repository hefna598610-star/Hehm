# MagnoScan - Modern Electromagnetic Field Detector & Smart Magnet Trigger

A modern, precision EMF (Electromagnetic Field) radar and magnet detector built with Jetpack Compose and Material 3. Features real-time magnetic field measurements (µT), dynamic multi-axis (X/Y/Z) visualization, radar sweep animations, acoustic/haptic feedback, and a smart panic trigger to send the app to the background or trigger stealth alerts.

### User Review & Critical Decisions

> [!IMPORTANT]
> The user confirmed the following directions during the clarification phase:
> - **Primary Concept**: Advanced EMF & Electromagnetic Field Detector / Radar (كاشف مجال مغناطيسي ورادار كهرومغناطيسي متطور)
> - **Aesthetic Direction**: Material 3 elegant, balanced with modern subtle/calm palette (تصميم ماتيريال 3 أنيق ومتناسق بألوان عصرية هادئة)
> - **Trigger Action**: Customizable response upon magnet proximity (Send app to background / stealth hide with haptics & audio alerts, with full user configuration).

---

### 1. Overview & Core Concept

- **What It Does**: Reads the device's hardware magnetometer (`Sensor.TYPE_MAGNETIC_FIELD`), computes total magnitude ($\sqrt{x^2 + y^2 + z^2}$ in microteslas, µT), visualizes raw and calibrated values in real time on an interactive gauge, radar radar sweep, and rolling waveform chart, and fires user-configured panic triggers (e.g. `moveTaskToBack(true)` for emergency hiding or rapid audio/haptic pulse).
- **Target Audience**: Users wanting an elegant EMF detector, scientific magnetism meter, stud/metal finder helper, or a stealth privacy tool activated by proximity to a magnet.
- **Key Value**: Turns a raw 30-line snippet into a sleek, professional, and accessible Android tool with zero latency, full Arabic and English localization, and smooth 60fps Compose animations.

---

### 2. User Experience & Visual Design

- **Visual Theme & Atmosphere**:
  - Material 3 Dark & Calm palette: Deep charcoal surface (`#12161A`), muted slate containers (`#1D232A`), emerald teal active accents (`#00E5A3` / `#00BFA5`), warning amber (`#FFB300`), and alert crimson (`#FF5252`).
  - High-precision gauge with dynamic color shifts as field strength approaches and surpasses user-defined thresholds.
- **Key Screens & Views**:
  1. **Radar & Gauge View (Main)**:
     - Prominent circular EMF gauge showing total field strength in $\mu\text{T}$, peak value, and ambient baseline.
     - 3-axis mini-bars ($X, Y, Z$) showing spatial distribution of the magnetic field.
     - Rotating radar sweep circle that blips faster when magnetic field increases.
     - Status badge indicating normal ambient (30-60 µT), elevated field, or Magnet Detected (Over Threshold).
  2. **History Waveform & Stats Tab**:
     - Real-time rolling Canvas chart plotting the last 60 sensor readings.
     - Min, Max, and Average field strength statistics.
  3. **Trigger & Sensitivity Settings**:
     - Interactive slider for threshold limit (from 50 µT to 400 µT, default 120 µT).
     - Trigger Action Selector:
       - *Instant Hide (Move to Background)*: Original `moveTaskToBack(true)`.
       - *Haptic / Pulse Alert*: Vibrate phone with increasing frequency.
       - *Audible Geiger Tick*: Synthesized tone clicks based on proximity.
       - *Visual Flash / Stealth Mode*.
     - Calibration button to zero-out local ambient baseline.

---

### 3. Key Product Decisions & Trade-Offs

- **Real Hardware Sensor with Graceful Fallback**:
  - Reads `Sensor.TYPE_MAGNETIC_FIELD`.
  - When running in an environment or emulator without a physical magnetometer, provides an interactive "Sensor Simulator Mode" toggle so the user can test the gauge, slider, and panic actions smoothly.
- **Safe Area & Edge-to-Edge**:
  - Uses `enableEdgeToEdge()` with proper `statusBarsPadding()` and `navigationBarsPadding()` to guarantee touch targets and layouts look pristine on all devices.
- **RTL & Arabic Support**:
  - Full support for both Arabic and English strings for natural readability.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   Android Device                       │
│    Hardware Magnetometer (Sensor.TYPE_MAGNETIC_FIELD)   │
└──────────────────────────┬─────────────────────────────┘
                           │ SensorEvent (x, y, z)
                           ▼
┌────────────────────────────────────────────────────────┐
│               MagnetometerManager                      │
│  - Computes magnitude = sqrt(x² + y² + z²)             │
│  - Smooths readings with low-pass filter               │
│  - Emits StateFlow<MagnetData>                         │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                MainViewModel                           │
│  - Holds threshold, peak, history points, settings     │
│  - Evaluates trigger conditions & cooldowns            │
│  - Triggers triggerAction(MoveTaskToBack / Haptics)    │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│            Jetpack Compose UI (M3)                     │
│  ├── EmfGaugeComposable (Canvas arc + dynamic color)   │
│  ├── RadarSweepComposable (Rotating beam + blips)      │
│  ├── AxisBreakdownView (X, Y, Z bars)                  │
│  ├── WaveformHistoryCanvas (Rolling 60-point plot)     │
│  └── SensitivitySettingsSheet (Threshold & Actions)    │
└────────────────────────────────────────────────────────┘
```

- **Manifest Permissions**:
  - `android.permission.VIBRATE` for haptic alerts.
- **App Metadata**:
  - Label: `MagnoScan` (كاشف المجال المغناطيسي)
