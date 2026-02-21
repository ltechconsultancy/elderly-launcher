# Elderly Launcher - Development Plan

## Phase 1: Interactive Mockups (HTML/CSS/JS)

Build browser-based mockups to nail down the UX before writing Kotlin.

### Screens to Mock

1. **Home Screen**
   - Large clock/date (top)
   - Wallpaper carousel (swipeable backgrounds)
   - Volume control button (visible on home)
   - App grid (4-6 large icons)
   - Favorites dock (bottom)

2. **App Drawer**
   - Alphabetical list with big icons
   - Search bar (large, simple)
   - Back button prominent

3. **Contacts Screen**
   - Favorite contacts with photos
   - One-tap to call
   - Emergency SOS button (always visible)

4. **Settings Screen**
   - Large toggles
   - Simple language: "Geluid" not "Audio Settings"
   - Wallpaper picker

### Design Principles

- **Minimum touch target**: 48dp (we'll use 64px+ for elderly)
- **Font size**: 18px minimum, 24px+ for key elements
- **Contrast ratio**: 4.5:1 minimum (WCAG AA)
- **Colors**: Soft, warm palette with high contrast text
- **Feedback**: Visual + haptic indication on every tap

### Tech Stack (Mockups)

- HTML5 + CSS3 (Tailwind for speed)
- Alpine.js (interactivity)
- Mobile-first, 360px-412px viewport
- PWA-ready structure

### i18n Structure

```
/locales
  /nl.json  (Dutch - primary)
  /en.json  (English - fallback)
```

### File Structure

```
/workspace/launcher/mockups/
  index.html          # Home screen
  drawer.html         # App drawer
  contacts.html       # Contacts/SOS
  settings.html       # Settings
  /css/
    styles.css        # Tailwind + custom
  /js/
    app.js            # Alpine components
    i18n.js           # Translation helper
  /locales/
    nl.json
    en.json
  /assets/
    /wallpapers/      # Sample wallpapers
    /icons/           # App icons
```

---

## Phase 2: Native Android (Kotlin) — Later

- Android 8+ (API 26)
- Launcher3 as reference
- Full launcher APIs
- Wallpaper manager integration
- Volume control integration

---

## Immediate Next Steps

1. Create mockup folder structure
2. Build Home Screen mockup with:
   - Clock/date
   - Wallpaper carousel (swipe)
   - Volume button
   - App grid (6 icons)
3. Dutch translations from start
4. Test responsiveness (small phone → tablet)
