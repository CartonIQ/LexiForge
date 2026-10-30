# LexiForge

Offline SM-2 spaced-repetition vocabulary app. Kotlin, Jetpack Compose (Material 3), Room + KSP, WorkManager, AdMob (test IDs).

## Build
    gradle assembleDebug        # needs Gradle 8.9, JDK 17, Android SDK 34
    # or, after generating the wrapper jar once:  gradle wrapper --gradle-version 8.9  &&  ./gradlew assembleDebug

APK: app/build/outputs/apk/debug/app-debug.apk

### Termux / ARM
AGP's bundled aapt2 is x86. Uncomment `android.aapt2FromMavenOverride` in gradle.properties and
point it at a native ARM aapt2 from your SDK build-tools.

## Beyond the spec
Deck create/edit/delete, per-deck TTS language, card edit/delete/search/duplicate check, CSV import/export,
Statistics screen (7-day chart, retention, card maturity), streak freezes (earned on level-up),
in-session retry of "Again" cards, interval previews on rating buttons, double-XP rewarded ad,
auto-pronounce, haptics toggle, daily goal slider, reminder time picker, starter Spanish deck.

## Before publishing
Swap the AdMob test IDs (ads/AdManager.kt + manifest APPLICATION_ID) and add a consent flow (UMP) for EEA users.
