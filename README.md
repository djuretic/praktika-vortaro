# Praktika Vortaro

This is the repository of the Android application Praktika Vortaro, an Esperanto dictionary based on Reta Vortaro. It works offline, without Internet access. 

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.esperantajvortaroj.app/)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="80">](https://play.google.com/store/apps/details?id=com.esperantajvortaroj.app)

## Screenshots

<p float="left">
<img src="metadata/en-US/images/phoneScreenshots/1.png" alt="Search word" width="250" />
<img src="metadata/en-US/images/phoneScreenshots/2.png" alt="Definition" width="250" />
<img src="metadata/en-US/images/phoneScreenshots/3.png" alt="Word definition popup" width="250" />
</p>

## Build

The sqlite database used is built using https://github.com/djuretic/praktika-vortaro-dicts and copied to `app/src/main/assets/databases/vortaro.db`.

## Update version

Update `app/build.gradle` and bump `versionCode` and `versionName` inside `android.defaultConfig`.

If vortaro.db was updated, also bump `DB_VERSION` in `app/src/main/kotlin/com/esperantajvortaroj/app/db/DatabaseHelper.kt`.

## License

GPL-3.0
