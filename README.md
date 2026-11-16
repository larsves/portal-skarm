# Portal-skärmen

Klocka, väder (SMHI) och veckans träning (Strava) på en sida, för Portal+ på kontoret.
Utan Strava-koppling visar sidan exempeldata.

## 1. Lägg upp på GitHub Pages
1. Skapa ett nytt repo, t.ex. `portal-skarm`, och ladda upp alla filer (även mappen `.github`).
2. Settings → Pages → Source: *Deploy from a branch*, branch `main`, mapp `/ (root)`.
3. Sidan finns sedan på `https://larsves.github.io/portal-skarm/`.

Ändra `lat`, `lon` och `place` högst upp i skriptet i `index.html` till rätt plats.

> GitHub Pages är publikt. `training.json` (passnamn, minuter, snittpuls) blir läsbar för den som har adressen.

## 2. Koppla Strava (en gång)
1. Gå till https://www.strava.com/settings/api och skapa en app. *Authorization Callback Domain*: `localhost`.
2. Öppna i webbläsaren (byt `DITT_ID`):
   `https://www.strava.com/oauth/authorize?client_id=DITT_ID&response_type=code&redirect_uri=http://localhost&approval_prompt=force&scope=activity:read_all`
3. Godkänn. Webbläsaren hamnar på en sida som inte laddar – kopiera `code=...` ur adressfältet.
4. I Terminal:
   ```
   curl -X POST https://www.strava.com/oauth/token \
     -d client_id=DITT_ID -d client_secret=DIN_SECRET \
     -d code=KODEN -d grant_type=authorization_code
   ```
   Kopiera `refresh_token` ur svaret.
5. I repot: Settings → Secrets and variables → Actions → *New repository secret*:
   `STRAVA_CLIENT_ID`, `STRAVA_CLIENT_SECRET`, `STRAVA_REFRESH_TOKEN`.
   Valfritt under fliken *Variables*: `EASY_HR` (standard 150) – pass med lägre snittpuls räknas som lugna.
6. Actions → *Hämta Strava* → *Run workflow*. Därefter körs den två gånger i timmen.

## 3. Koppla Spotify (nu spelas)
Kräver att ditt Spotify-konto har Premium (Spotifys regel för egna appar sedan 2026).
1. Gå till https://developer.spotify.com/dashboard → *Create app*.
   - Redirect URI: `https://larsves.github.io/portal-skarm/` (exakt, med snedstreck på slutet)
   - API: kryssa i *Web API*
2. Kopiera *Client ID* och klistra in i `spotifyClientId: ""` högst upp i skriptet i `index.html`.
3. Öppna sidan **på Portalen** och tryck *Logga in på Spotify*. Inloggningen sparas i Portalens webbläsare.
   Spotify kräver ny inloggning ungefär var sjätte månad – då dyker knappen upp igen.

Tryck på musikraden för att öppna Spotify-appen.

## 4. Hämtning av barnen
Nedräkningen visas vardagar från 08:00 fram till `pickupTime` (15:40) och blir orange de sista 15 minuterna.
Tryck ✕ för att dölja den resten av dagen. Ångrat dig? Tryck *🚗 Visa hämtning*. Ändra tider och dagar högst upp i `index.html`.

## 5. Bakgrund och dygnsfärger
Färgerna glider mjukt mellan natt, gryning, dag, solnedgång och kväll. Nyckeltiderna finns i `DAYCOLORS` i `index.html`.
Standard är ett ritat vinterlandskap. Bakgrundsbilderna (`bg.jpg`, `bg1.jpg` …) roterar var 10:e minut med mjuk övertoning. Lägg till eller ta bort bilder i repot
och i listan `backgroundImages` högst upp i `index.html`; ändra takten med `rotateEveryMin`. Bilderna får samma dygnstoning ovanpå.
Håll bilderna runt 2400 px breda så laddar de snabbt på Portalen.

## 6. Portal-appen (APK)
Mappen `android/` är en liten app som visar sidan i helskärm, håller skärmen vaken och öppnar Spotify-appen säkert.
Innehållet hämtas fortfarande från GitHub Pages – appen installerar du bara en gång.

1. När du laddar upp repot bygger GitHub appen automatiskt (fliken **Actions** → *Bygg Portal-appen*, ca 3 min).
   Starta manuellt med *Run workflow* om det behövs.
2. Öppna den färdiga körningen och ladda ner **portal-skarm-apk** längst ner (en zip med `app-debug.apk`).
3. Packa upp och installera:
   ```
   adb install -r ~/Downloads/app-debug.apk
   ```
4. Appen heter **Kontoret** och ligger under Apps på Portalen.

Adressen till sidan står i `HOME_URL` i `android/app/src/main/java/se/larsves/portalskarm/MainActivity.java`.
`android/debug.keystore` är en signeringsnyckel bara för den här appen; den gör att nya byggen kan installeras ovanpå gamla.

## 7. Visa i webbläsaren i stället
Öppna adressen i Portalens webbläsare. Eller via Macen:
```
adb shell am start -a android.intent.action.VIEW -d https://larsves.github.io/portal-skarm/
```
