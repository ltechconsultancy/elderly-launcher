# Elderly Launcher

Een eenvoudige, toegankelijke Android launcher voor ouderen.

## Features

- **Grote knoppen** - Makkelijk te raken touch targets; geen swipe-gebaren
- **Duidelijke iconen** - Echte app iconen met namen eronder
- **Pagina's via knoppen** - Tik op nummer 1–6 onderaan; geen swipe
- **Volume bediening** - Geluid en helderheid met + en − (helderheid minimaal 40%, standaard 100%)
- **Snel bellen** - Één-tap contacten met foto's
- **Noodknop** - Altijd zichtbaar op telefoons met sim
- **Wachtwoord-beveiligde instellingen** - Voorkom per ongeluk wijzigen
- **Kleurthema's** - 12 kleuren om uit te kiezen
- **Meertalig** - Nederlands (primair) + Engels
- **Tablet** - Landscape + portrait, POCO Pad / Pad X1 / M1 / C1 (HyperOS)
- **Startscherm / apps** - Extra tegels via Vorige/Volgende in het midden, niet onderaan
- **Foto-slideshow** - Wisselt elke 4 seconden; Vorige/Volgende links en rechts in het midden, Pauze op de foto
- **Update** - In Instellingen: haalt de nieuwste APK van GitHub Releases op en installeert die

## Pagina's

1. **Home** - Klok, grote app-tegels, snelcontacten, noodknop
2. **Apps** - Alle geïnstalleerde apps
3. **Spelletjes** - Gekozen games
4. **Foto's** - Diavoorstelling
5. **Volume** - Media, meldingen, alarm
6. **Instellingen** - Beveiligd met wachtwoord (standaard: `1234`)

## Instellingen

- **Update** - Nieuwste GitHub-release downloaden en installeren
- **Kleuren** - Kies uit 12 thema-kleuren
- **Taal** - Nederlands / Engels
- **Zichtbare apps** - Selecteer welke apps getoond worden
- **Snelle contacten** - Beheer één-tap bel-contacten
- **Noodnummer** - Configureer noodoproep nummer
- **Wachtwoord** - Wijzig het instellingen-wachtwoord

## Tech Stack

- **Kotlin** - Moderne Android ontwikkeling
- **Jetpack Compose** - Declaratieve UI
- **Material Design 3** - Moderne design systeem
- **DataStore** - Persistente instellingen opslag
- **Coil** - Async image loading voor contact foto's
- **Minimum SDK 26** - Android 8.0+

## Project Structuur

```
app/src/main/
├── java/com/elderlylauncher/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── AppInfo.kt           # Data classes
│   │   ├── AppRepository.kt     # Installed apps laden
│   │   ├── ContactsRepository.kt # Contacten laden
│   │   └── SettingsDataStore.kt # Persistente settings
│   ├── ui/
│   │   ├── LauncherApp.kt       # Main app met pager
│   │   ├── LauncherViewModel.kt # State management
│   │   ├── home/
│   │   │   └── HomeScreen.kt
│   │   ├── volume/
│   │   │   └── VolumeScreen.kt
│   │   ├── contacts/
│   │   │   └── ContactsScreen.kt
│   │   ├── settings/
│   │   │   ├── SettingsScreen.kt
│   │   │   └── ColorPickerDialog.kt
│   │   └── theme/
│   │       ├── Theme.kt
│   │       └── Typography.kt
│   └── utils/
└── res/
    ├── values/
    │   ├── strings.xml      # English
    │   ├── colors.xml
    │   └── themes.xml
    └── values-nl/
        └── strings.xml      # Dutch (primary)
```

## Permissies

```xml
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```

## Download APK

De nieuwste debug-APK staat bij de [GitHub Releases](https://github.com/ltechconsultancy/elderly-launcher/releases/latest).

Bestand: `elderly-launcher-v1.0.17.apk`

## Bouwen

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Installeren

```bash
# Via Gradle
./gradlew installDebug

# Of direct APK via ADB
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Als Home Screen Instellen

Na installatie:
1. Druk op Home knop
2. Selecteer "Elderly Launcher" of "Eenvoudige Launcher"
3. Kies "Altijd"

POCO / Xiaomi HyperOS: **Instellingen → Apps → Standaard-apps → Startscherm-app** (of Launcher).

De meeste POCO-tablets zijn wifi-only. De noodknop wordt dan vervangen door een melding dat bellen niet kan.

## Mockups

De `/mockups` folder bevat interactieve HTML prototypes.
PocketDev panels beschikbaar: `launcher-home`, `launcher-volume`

## Licentie

MIT
