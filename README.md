# YAP LAN!

Kotlin + Jetpack Compose ile yazılmış, koyu temalı Android yapılacaklar uygulaması.

## Özellikler
- Görev ekleme, düzenleme, tamamlama, kaydırarak silme (geri alınabilir)
- Öncelik (Yüksek / Orta / Düşük) ve kullanıcı tanımlı renkli kategoriler
- Son tarih + o saatte bildirim; telefon yeniden başlasa da hatırlatmalar korunur
- Tümü / Yapılacak / Tamamlanan sekmeleri, kategori filtresi, arama

## Derleme ve çalıştırma
```fish
set -x JAVA_HOME /opt/android-studio/jbr
./gradlew assembleDebug testDebugUnitTest

~/Android/Sdk/emulator/emulator -avd Medium_Phone &
~/Android/Sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Ya da klasörü Android Studio ile açıp ▶ Run'a basın.

## Yapı
- `data/` — Room veritabanı (görevler, kategoriler) ve repository
- `reminder/` — alarm kurma, bildirim gösterme, açılışta alarmları yeniden kurma
- `ui/list`, `ui/edit`, `ui/categories` — ekranlar ve ViewModel'ler
