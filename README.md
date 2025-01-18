# Praktika Vortaro

This is the repository of the Android application Praktika Vortaro.

## Screenshots

<p float="left">
<img src="metadata/en-US/images/phoneScreenshots/1.png" alt="Search word" style="zoom:20%;" />
<img src="metadata/en-US/images/phoneScreenshots/2.png" alt="Definition" style="zoom:20%;" />
<img src="metadata/en-US/images/phoneScreenshots/3.png" alt="Word definition popup" style="zoom:20%;" />
</p>


## Build

The sqlite database used is built using https://github.com/djuretic/praktika-vortaro-dicts and copied to `app/src/main/assets/databases/vortaro.db`.

## Update version

Update `app/build.gradle` and bump `versionCode` and `versionName` inside `android.defaultConfig`.

If vortaro.db was updated, also bump `DB_VERSION` in `app/src/main/kotlin/com/esperantajvortaroj/app/db/DatabaseHelper.kt`.

## License

GPL-3.0
