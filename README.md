# 🐦 Penlingu – İngilizceyi Kalıcı Öğren

**Penlingu**, İngilizce kelimeleri uzun vadede kalıcı şekilde öğretmeyi hedefleyen, yapay zekâ destekli, eğitici ve etkileşimli bir mobil uygulamadır.

<!-- ![Penlingu Logo](https://your-image-link.com/logo.png) İstersen buraya bir logo linki koyabilirsin -->

---

## 🚀 Özellikler

### 📚 Akıllı Quiz Sistemi
- Firestore’dan **Kullanıcının belirlediği kadar kelime** çekilir.
- Her kelime için ilgili bir **görsel ve o kelimenin karşılığı** gösterilir.
- Kullanıcı bilmediği kelimeyi atlayabilir.
- Quiz bittikten sonra yanlış yaptığı kelimeye geri dönüp doğrusunu görebilir.
- Quiz bittiğinde doğru,yanlış ve çözülen soru sayısını görebilir.
- Dilerse quizden onay verdiği taktirde çıkış yapabilir.

### 🧠 Kalıcı Öğrenme Aşamaları (Spaced Repetition)
Her kelimenin "öğrenilmiş" sayılması için şu 6 zaman aralığında doğru cevaplanması gerekir:
- 1 Gün  
- 1 Hafta  
- 1 Ay  
- 3 Ay  
- 6 Ay  
- 1 Yıl
Bu aşamaların herhangi birinde yanlış yapması o kelime için başa dönülmesine sebep olur.
(Sıralama sisteminde adil olması açısından yanlış yaptığında o kelime için doğru sayısı sıfırlanır.)

### 🖋️ Kelime ekleme sayfası
Kullanıcılar kendileri sisteme kelime ekleyebilirler kelime içeriği:
- Kelimeye ilişkin görsel
- Kelimenin ingilizcesi ve türkçe karşılığı
- İngilizce kelimenin kullanıldığı iki adet cümle

### 🅰️ Sözlük sayfası
Eklenen kelimeler aşfabetik olarak listelenir listelenen içerik :
- Kelimenin ingilizcesi ve türkçesi
- İngilizce kelimenin baş harfi
  **Arama özelliği**
  -Hem ingilizce hemde türkçe kelimelere ilişkin arama özelliği
  -Kullanıcının aradığı kelimeyi hem baş harfi ile hemde arama ile kolayca bulabilmesi sağlanmıştır
- **Tıklanma**
- Listelenen kelimeye tıklandığında :
  -Kelimenin türkçe ve ingilizcesi ,
  -Kelimeye ilişkin görsel,
  -Kelimenin geçtiği iki adet cümle gösterilir.
  -TTS özelliği aşağıda verilmiştir.


### 🔊 TTS (Text To Spech)
- Kelimeye ve cümleye tıklanınca:
  - İngilizcesi yüksek sesle okunur.
  - Eş zamanlı olarak **Lottie animasyonu** oynar.

### 

### 👤 Profil Sayfası
- Firebase Authentication ile giriş
- Kullanıcı bilgileri (isim, mail, profil fotoğrafı)
- **İki sekmeli görünüm:**
  - 🧠 Quizde karşılaşılan kelimeler
  - 🎓 Tam öğrenilen (6. aşamaya gelen) kelimeler
- Ayarlar, admin girişi ve e-posta ile iletişim butonları

### ✨ Yapay Zekâ Destekli Hikâye Üretimi
- Kullanıcı 5 İngilizce kelime seçer
- Bu kelimelerden:
  - OpenAI API ile **hikâye yazılır**
  - Wiro.ai ile **hikâyeye uygun görsel** üretilir

---

## 🛠️ Teknolojiler

| Katman        | Teknoloji                |
|---------------|--------------------------|
| Backend       | Firebase (Auth, Firestore) |
| Görsel Üretim | Wiro.ai API              |
| Hikâye Üretim | OpenAI API (GPT)         |
| Android       | Kotlin + Jetpack         |
| UI/UX         | XML, Lottie, Material UI |

---

## 🔒 Güvenli İçerik Politikası
- Çocuklar için uygundur.
- Şu içerikler filtrelenir:  
  ❌ Küfür  
  ❌ Çıplaklık  
  ❌ Politik söylem  
  ❌ Kötüye kullanım

---

## 📦 Kurulum (Geliştirici İçin)

```bash
git clone https://github.com/username/penlingu.git
cd penlingu
