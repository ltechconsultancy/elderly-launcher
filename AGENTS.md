# Klaro / Elderly Launcher

Lees dit vóór elke wijziging, commit en pull request. Feedback over één scherm geldt voor elk vergelijkbaar scherm.

## Tegels

- Een app-tegel is groot. Het icoon is ongeveer 62% van de kortste zijde van de kaart (tussen 72 en 220 dp), niet een vast klein icoon in een leeg vlak.
- Dezelfde verhouding geldt op het startscherm, de apps-pagina, de spelletjes-pagina en elke latere app-tegel. Een fix voor alleen de standaardtegels is geen fix.
- De kaartkleur komt uit het icoon. Een groen WhatsApp-icoon geeft een groene kaart. Geen palet dat per plek roteert.
- Standaardtegels Bellen, Berichten, Camera en Foto's houden hun vaste kleur.
- De kaart gebruikt de ruimte die er is, binnen een minimum en een maximum. Restruimte is een kleine marge. Geen `SpaceBetween` dat kaarten tot platte stroken uitrekt, en geen piepklein icoon in een grote lege kaart.
- Telefoon en tablet, staand en liggend. Tablet staand: startscherm in 2 kolommen, zodat de kaarten breed genoeg zijn voor een groot icoon.

## Bediening

- Geen vegen. Alles is tikken. Tikdoelen minstens 48 dp.
- Wissen, openen en andere knoppen blijven boven de pagina-knoppen. Niets mag onder de navigatie verdwijnen.
- Elke melding heeft een groene knop **App openen** naast **Wis**.
- Teksten in de app zijn Nederlands. De app-naam blijft Klaro.

## Bouwen

- Geen extra GitHub Actions-jobs of workflows. De bestaande lint- en release-workflow zijn genoeg.
- Paparazzi is de visuele check. Geen emulator. Screenshots niet committen en niet aan een release hangen.
- Een zichtbare wijziging krijgt een hoger versionCode en versionName, daarna een tag die gelijk is aan die versionName.
- Oude releases blijven niet liggen: de release-workflow bewaart de twee nieuwste APK's.
