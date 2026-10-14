# SocialAgent - AI Social Opportunity & Viral Content Agent

An intelligent, multi-format social content creation and opportunity scouting Android app built with **Kotlin** and **Jetpack Compose (Material 3)**.

---

## 🚀 Getting Started (GitHub Clone & Local Setup)

### System Requirements
* **Android Studio**: Android Studio Koala / Ladybug / Meerkat (or newer)
* **Java Development Kit (JDK)**: JDK 17 or JDK 21 (configured in Android Studio under *Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK*)
* **Android SDK**: `compileSdk = 36`, `targetSdk = 36`, `minSdk = 24` (Android 7.0+)
* **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`) and Version Catalog (`gradle/libs.versions.toml`)

---

### Step-by-Step Installation

1. **Clone or Download the Repository:**
   ```bash
   git clone <your-github-repo-url>.git
   cd <project-folder>
   ```

2. **Configure Environment Variables (Optional):**
   Copy the provided `.env.example` file to `.env`:
   ```bash
   cp .env.example .env
   ```
   Add your Gemini API Key or configure it later directly within the app's **Settings** screen:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   META_APP_ID=
   META_APP_SECRET=
   META_REDIRECT_URI=socialagent://meta-callback
   ```
   *(Note: The app runs with full local offline fallback even if no external API key is provided!)*

3. **Open in Android Studio:**
   - Open Android Studio.
   - Click **Open** and select the repository folder.
   - Allow Gradle to sync dependencies automatically.

4. **Build and Run:**
   - Select your Android device or emulator.
   - Click the green **Run (▶)** button, or execute from terminal:
     ```bash
     ./gradlew assembleDebug
     ```
   - To run all automated unit and Robolectric tests:
     ```bash
     ./gradlew testDebugUnitTest
     ```

---

## ✨ Core Features & Functionality

### 1. 📰 Live Daily News Channels Integration
* **Supported Portals**: Real-time scouting from **News Live Digital** (`newslivetv.com`), **Pratidin Time** (`pratidintime.com`), **Prag News** (`pragnews.com`), and **The Assam Tribune** (`assamtribune.com`).
* **Daily Freshness Guarantee**: Automatically filters for strictly same-day, fresh news so outdated or expired announcements are never published.

### 2. 🛡️ Source Verification Service (Google Search Grounding)
* **Official Primary Domain Cross-Referencing**: Uses Google Search grounding to verify announcements against official government portals (`.gov.in`, `.nic.in`, `ghconline.gov.in`, `slprbassam.in`, `apsc.nic.in`, `sewasetu.assam.gov.in`).
* **Expired Deadline Detection**: Proactively parses deadlines and dates. Outdated recruitment notices (e.g., past dates) are flagged as `SUSPICIOUS_OR_EXPIRED`, preventing misinformation.
* **Interactive UI Card**: On-demand verification card in the **Generator** and **Dashboard** tabs allows checking any URL or headline before publishing.

### 3. 🔔 Once-Per-Day "Top 10 Big News" Push Notification
* **Automated Daily Trigger**: Scheduled for 09:00 AM daily with a verified morning digest of government schemes, recruitment, and big news.
* **Anti-Spam & Rate Limiting**: Built-in 18-hour cooldown window ensures zero redundant or spammy notifications.
* **Reboot Resilience**: Auto-restores notification alarm schedules across device restarts (`BOOT_COMPLETED`).
* **Settings & Preview**: Full user control in **Settings** to toggle alerts, change delivery times, or preview safe test notifications.

### 4. 🎨 Multi-Format AI Content Creation Engine
* **Formats Supported**: Government Job Alerts, Daily News Posts, Scheme Guides, Cultural Features, Educational Memes, and Multi-Scene Reels/Shorts.
* **Custom Aspect Ratios**: Instant preview and export in Square (`1:1`), Story/Reel (`9:16`), Landscape (`16:9`), and Portrait (`4:5`).
* **Copy & Share**: One-tap copy to clipboard and direct Android system share sheet integration.

---

## 📁 Project Architecture

```
app/src/main/java/com/example/
├── data/
│   ├── local/              # Room database, DataStore settings, logging
│   ├── model/              # Domain models (Content, Opportunity, Settings)
│   ├── remote/
│   │   ├── ai/             # Gemini API client & Search Grounding
│   │   └── scout/          # Live news & portal scout engines
│   └── repository/         # Single source of truth repositories
├── domain/
│   ├── automation/         # FreeTierGuard, Auto-scheduler & quotas
│   ├── engine/             # ContentCreationEngine, MemeEngine, ReelEngine
│   ├── notification/       # DailyTop10NewsNotificationManager & Receiver
│   └── verification/       # SourceVerificationService & Safety filters
└── ui/
    ├── navigation/         # Compose navigation graph
    ├── screens/            # Dashboard, Generator, History, Analytics, Settings
    ├── theme/              # Material 3 colors, typography, shapes
    └── viewmodel/          # AppViewModel & reactive StateFlows
```

---

## 🧪 Testing & Verification
All unit tests are written with JUnit 4 and Robolectric for fast local JVM execution:
```bash
./gradlew testDebugUnitTest
```
* `SourceVerificationServiceTest`: Tests official government domain mappings, expired deadline detection, and risk level assignment.
* `DailyTop10NewsNotificationTest`: Tests notification channel creation, once-per-day anti-spam guard, and receiver lifecycle events.
* `ContentCreationTest` & `ReelEngineTest`: Verifies multi-format content generation, safety filters, and quota rules.
