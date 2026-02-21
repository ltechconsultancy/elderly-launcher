# Elderly Launcher

Een eenvoudige, toegankelijke Android launcher voor ouderen.

## Features

- **Grote knoppen** - Makkelijk te raken touch targets (80dp+)
- **Duidelijke iconen** - Echte app iconen met namen eronder
- **Horizontaal swipen** - 3 pagina's met genummerde indicator
- **Volume bediening** - Met + en − knoppen (geen slider)
- **Snel bellen** - Één-tap contacten met foto's
- **Noodknop** - Altijd zichtbaar
- **Wachtwoord-beveiligde instellingen** - Voorkom per ongeluk wijzigen
- **Kleurthema's** - 12 kleuren om uit te kiezen
- **Meertalig** - Nederlands (primair) + Engels

## Pagina's

1. **Home** - Klok, 4 app-tegels (Bellen, Berichten, Camera, Foto's), noodknop
2. **Volume** - Media, Oproepen, Meldingen, Alarm (met echte AudioManager)
3. **Instellingen** - Beveiligd met wachtwoord (standaard: `1234`)

## Instellingen

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

## Mockups

De `/mockups` folder bevat interactieve HTML prototypes.
PocketDev panels beschikbaar: `launcher-home`, `launcher-volume`

## Licentie

MIT
