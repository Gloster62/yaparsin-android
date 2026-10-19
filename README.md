# YAP LAN!

Kotlin + Jetpack Compose ile yazılmış, koyu temalı Android yapılacaklar uygulaması.
Görünüm "Tasarım 4 – Sade ve Anlaşılır"a göre: koyu zemin, tek vurgu rengi (sarı), IBM Plex yazı tipleri.

## Özellikler
- Alt menü: Görevler / Ara / Kategoriler
- Günlük ilerleme ("3/6 bugün bitti") ve cesaret mesajı; görev bitince "Yaptın lan!" kutlaması
- Görevler bölümlere ayrılır: Gecikenler, Bugün, Yarın, Daha sonra, Tarihsiz, Tamamlanan
- Alt sayfadan yeni görev: öncelik, kategori, son tarih (Bugün / Yarın / başka gün) ve saat
- Kaydırarak ya da düzenleme ekranından silme, "Geri al" ile geri getirme
- Kullanıcı tanımlı renkli kategoriler, kategori başına görev sayısı
- Son tarih + o saatte bildirim; telefon yeniden başlasa da hatırlatmalar korunur

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
- `ui/list`, `ui/search`, `ui/edit`, `ui/categories` — ekranlar ve ViewModel'ler
- `ui/components` — ortak parçalar (görev satırı, seçim kutuları, butonlar)
- `ui/theme` — renkler ve yazı tipleri

IBM Plex yazı tipleri SIL Open Font License ile dağıtılır (`app/src/main/assets/licenses/`).
