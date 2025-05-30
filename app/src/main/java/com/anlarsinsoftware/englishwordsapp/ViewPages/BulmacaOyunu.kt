package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.Util.BaseCompact
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class BulmacaOyunu : BaseCompact() {
    private val veritabani = Firebase.firestore
    private val kullanici = Firebase.auth.currentUser
    private var kelimeListesi: MutableList<String> = mutableListOf()
    private var ogrenilmisKelimeListesi: MutableList<String> = mutableListOf()
    private var karsilasilanKelimeListesi: MutableList<String> = mutableListOf()
    private var gizliKelime = ""
    private var mevcutSatir = 0
    private var skor = 0
    private var oynananOyunSayisi = 0
    private var kullanilanHak = 0
    private lateinit var yeniOyunButonu: Button
    private lateinit var skorGostergesi: TextView
    private lateinit var hakGostergesi: TextView
    private lateinit var ipucuButonu: Button
    private lateinit var izgaraAlani: GridLayout
    private lateinit var hucreler: Array<Array<EditText>>
    private lateinit var tahminButonu: Button
    private lateinit var geriBildirimAlani: TextView
    private lateinit var yuklemeGostergesi: ProgressBar
    private lateinit var settingsButton: ImageButton

    private enum class KelimeKaynagi {
        TUM_KELIMELER,
        OGRENILMIS_KELIMELER,
        KARSILASILAN_KELIMELER
    }

    private var kelimeKaynagi = KelimeKaynagi.TUM_KELIMELER

    private val dogruRenk: Int by lazy { ContextCompat.getColor(this, R.color.teal_700) }
    private val yanlisYerdeRenk: Int by lazy { ContextCompat.getColor(this, R.color.amber_500) }
    private val yanlisRenk: Int by lazy { ContextCompat.getColor(this, R.color.black_overlay) }
    private val varsayilanRenk: Int by lazy { ContextCompat.getColor(this, R.color.lightOrange) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bulmaca_oyunu)
        initializeViews()
        setupListeners()
        izgarayiOlustur()
        kelimeleriYukle()
    }

    private fun initializeViews() {
        izgaraAlani = findViewById(R.id.letterGrid)
        tahminButonu = findViewById(R.id.guessBtn)
        geriBildirimAlani = findViewById(R.id.feedback)
        yuklemeGostergesi = findViewById(R.id.progress)
        yeniOyunButonu = findViewById(R.id.yeniOyunBtn)
        skorGostergesi = findViewById(R.id.skorGosterge)
        hakGostergesi = findViewById(R.id.hakGosterge)
        ipucuButonu = findViewById(R.id.ipucuBtn)
        settingsButton = findViewById(R.id.settingsButton)
    }

    private fun setupListeners() {
        tahminButonu.setOnClickListener { tahminYap() }
        yeniOyunButonu.setOnClickListener { yeniOyunBaslat() }
        ipucuButonu.setOnClickListener { ipucuGoster() }
        settingsButton.setOnClickListener { ayarlariGoster() }
    }

    private fun ayarlariGoster() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val wordSourceGroup = dialogView.findViewById<RadioGroup>(R.id.wordSourceGroup)
        val saveButton = dialogView.findViewById<Button>(R.id.saveSettingsButton)

        when (kelimeKaynagi) {
            KelimeKaynagi.TUM_KELIMELER -> wordSourceGroup.check(R.id.allWordsOption)
            KelimeKaynagi.OGRENILMIS_KELIMELER -> wordSourceGroup.check(R.id.learnedWordsOption)
            KelimeKaynagi.KARSILASILAN_KELIMELER -> wordSourceGroup.check(R.id.seenWordsOption)
        }

        saveButton.setOnClickListener {
            kelimeKaynagi = when (wordSourceGroup.checkedRadioButtonId) {
                R.id.learnedWordsOption -> KelimeKaynagi.OGRENILMIS_KELIMELER
                R.id.seenWordsOption -> KelimeKaynagi.KARSILASILAN_KELIMELER
                else -> KelimeKaynagi.TUM_KELIMELER
            }
            dialog.dismiss()
            yeniOyunBaslat()
        }

        dialog.show()
    }

    private fun kelimeleriYukle() {
        yuklemeGostergesi.visibility = View.VISIBLE
        geriBildirimAlani.text = "Kelimeler yükleniyor..."
        tahminButonu.isEnabled = false

        when (kelimeKaynagi) {
            KelimeKaynagi.OGRENILMIS_KELIMELER -> ogrenilmisKelimeleriYukle()
            KelimeKaynagi.KARSILASILAN_KELIMELER -> karsilasilanKelimeleriYukle()
            KelimeKaynagi.TUM_KELIMELER -> tumKelimeleriYukle()
        }
    }

    private fun tumKelimeleriYukle() {
        veritabani.collection("kelimeler")
            .get()
            .addOnSuccessListener { sonuc ->
                kelimeListesi.clear()
                for (belge in sonuc.documents) {
                    val kelime = belge.getString("ingilizceKelime")?.trim()?.uppercase(Locale.ENGLISH)
                    if (kelime != null && kelime.length == 5) {
                        kelimeListesi.add(kelime)
                    }
                }
                if (kelimeListesi.isEmpty()) {
                    showError("Uygun kelime bulunamadı!")
                } else {
                    gizliKelimeSec()
                }
            }
            .addOnFailureListener { hata ->
                Log.e("Bulmaca", "Veritabanı hatası", hata)
                showError("İnternet bağlantınızı kontrol edin ve tekrar deneyin.")
            }
    }

    private fun karsilasilanKelimeleriYukle() {
        if (kullanici == null) {
            showError("Kullanıcı girişi gerekli")
            return
        }

        veritabani.collection("kullanici_karsilasilan_kelimeler")
            .document(kullanici.uid)
            .collection("kelimeler")
            .get()
            .addOnSuccessListener { querySnapshot ->
                karsilasilanKelimeListesi.clear()
                for (document in querySnapshot.documents) {
                    val kelime = document.getString("ingilizceKelime")?.uppercase(Locale.ENGLISH)
                    if (kelime != null && kelime.length == 5) {
                        karsilasilanKelimeListesi.add(kelime)
                    }
                }
                kelimeListesi = karsilasilanKelimeListesi
                if (kelimeListesi.isEmpty()) {
                    showError("Karşılaşılan kelime bulunamadı")
                } else {
                    gizliKelimeSec()
                }
            }
            .addOnFailureListener { hata ->
                Log.e("Bulmaca", "Karşılaşılan kelimeler yüklenirken hata", hata)
                showError("Karşılaşılan kelimeler yüklenemedi")
            }
    }

    private fun ogrenilmisKelimeleriYukle() {
        if (kullanici == null) {
            showError("Kullanıcı girişi gerekli")
            return
        }

        veritabani.collection("kullanici_kelimeler")
            .document(kullanici.uid)
            .collection("tum_kelimeler")
            .whereEqualTo("ogrenmeSeviyesi", 6) // 6 aşamayı tamamlamış kelimeler
            .get()
            .addOnSuccessListener { sonuc ->
                ogrenilmisKelimeListesi.clear()
                for (belge in sonuc.documents) {
                    val kelime = belge.getString("ingilizceKelime")?.trim()?.uppercase(Locale.ENGLISH)
                    if (kelime != null && kelime.length == 5) {
                        ogrenilmisKelimeListesi.add(kelime)
                    }
                }
                kelimeListesi = ogrenilmisKelimeListesi
                if (kelimeListesi.isEmpty()) {
                    showError("Öğrenilmiş (6 seviye tamamlanmış) kelime bulunamadı")


                } else {
                    gizliKelimeSec()
                }
            }
            .addOnFailureListener { hata ->
                Log.e("Bulmaca", "Öğrenilmiş kelimeler yüklenirken hata", hata)
                showError("Öğrenilmiş kelimeler yüklenemedi")
            }
    }
    private fun gizliKelimeSec() {
        if (kelimeListesi.isNotEmpty()) {
            gizliKelime = kelimeListesi.random()
            Log.d("Bulmaca", "Gizli kelime: $gizliKelime")
            kelimeYuklemeTamamlandi()
        } else {
            showError("Oynanacak kelime bulunamadı")
        }
    }

    private fun kelimeYuklemeTamamlandi() {
        yuklemeGostergesi.visibility = View.GONE
        geriBildirimAlani.text = "Hazır! İlk tahminini yap"
        tahminButonu.isEnabled = true
        hakGostergesi.text = "Hak: 0/6"
        ipucuButonu.isEnabled = true
        ipucuButonu.alpha = 1f
    }

    private fun showError(message: String) {
        yuklemeGostergesi.visibility = View.GONE
        geriBildirimAlani.text = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun yeniOyunBaslat() {
        mevcutSatir = 0
        kullanilanHak = 0
        geriBildirimAlani.text = ""
        tahminButonu.isEnabled = true
        hakGostergesi.text = "Hak: 0/6"
        ipucuButonu.isEnabled = true
        ipucuButonu.alpha = 1f

        for (satir in 0 until 6) {
            for (sutun in 0 until 5) {
                hucreler[satir][sutun].apply {
                    setText("")
                    setBackgroundColor(varsayilanRenk)
                    isEnabled = false
                    setTextColor(Color.BLACK)
                }
            }
        }

        for (sutun in 0 until 5) {
            hucreler[0][sutun].isEnabled = true
        }
        hucreler[0][0].requestFocus()

        kelimeleriYukle()
    }

    private fun izgarayiOlustur() {
        hucreler = Array(6) { satir ->
            Array(5) { sutun ->
                EditText(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 70.dpToPx()
                        height = 70.dpToPx()
                        rowSpec = GridLayout.spec(satir)
                        columnSpec = GridLayout.spec(sutun)
                        setMargins(5, 5, 5, 5)
                    }
                    gravity = Gravity.CENTER
                    textSize = 24f
                    filters = arrayOf(InputFilter.LengthFilter(1))
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                    setBackgroundColor(varsayilanRenk)
                    isSingleLine = true
                    setTextColor(Color.BLACK)

                    addTextChangedListener(object : android.text.TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: android.text.Editable?) {
                            if (s?.length == 1 && sutun < 4) {
                                hucreler[satir][sutun + 1].requestFocus()
                            }
                        }
                    })

                    setOnKeyListener { _, tusKodu, olay ->
                        if (olay.action == android.view.KeyEvent.ACTION_DOWN) {
                            when (tusKodu) {
                                android.view.KeyEvent.KEYCODE_DEL -> {
                                    if (text.isEmpty() && sutun > 0) {
                                        hucreler[satir][sutun - 1].apply {
                                            requestFocus()
                                            setSelection(length())
                                        }
                                    }
                                }
                                android.view.KeyEvent.KEYCODE_ENTER -> {
                                    if (satir == mevcutSatir) {
                                        tahminYap()
                                        return@setOnKeyListener true
                                    }
                                }
                            }
                        }
                        false
                    }
                }.also { izgaraAlani.addView(it) }
            }
        }
    }

    private fun tahminYap() {
        if (mevcutSatir >= 6) return

        val tahmin = satirdanTahminAl(mevcutSatir)

        if (tahmin.length != 5) {
            geriBildirimAlani.text = "Lütfen 5 harfli bir kelime girin!"
            return
        }

        if (!kelimeListesi.contains(tahmin)) {
            geriBildirimAlani.text = "Bu kelime listemizde yok!"
            return
        }

        kullanilanHak++
        hakGostergesi.text = "Hak: $kullanilanHak/6"

        satirIcinGeriBildirimVer(mevcutSatir, tahmin)

        if (tahmin == gizliKelime) {
            // Doğru tahmin
            skor++
            oynananOyunSayisi++
            skorGostergesi.text = "Skor: $skor/$oynananOyunSayisi"
            geriBildirimAlani.text = "Tebrikler! $kullanilanHak denemede bildiniz!"
            tahminButonu.isEnabled = false
            ipucuButonu.isEnabled = false
            kazanimAnimasyonuGoster()



        } else {
            mevcutSatir++
            if (mevcutSatir == 6) {
                geriBildirimAlani.text = "Maalesef! Doğru kelime: $gizliKelime"
                tahminButonu.isEnabled = false
                ipucuButonu.isEnabled = false
                oynananOyunSayisi++
                skorGostergesi.text = "Skor: $skor/$oynananOyunSayisi"
            } else {
                for (sutun in 0 until 5) {
                    hucreler[mevcutSatir][sutun].isEnabled = true
                }
                hucreler[mevcutSatir][0].requestFocus()
            }
        }
    }

    private fun satirdanTahminAl(satir: Int): String {
        return hucreler[satir].joinToString("") { it.text.toString().uppercase(Locale.ENGLISH) }
    }

    private fun satirIcinGeriBildirimVer(satir: Int, tahmin: String) {
        val gizliHarfler = gizliKelime.toCharArray()
        val tahminHarfleri = tahmin.toCharArray()
        val geriBildirim = IntArray(5) { 0 }


        for (i in 0..4) {
            if (tahminHarfleri[i] == gizliHarfler[i]) {
                geriBildirim[i] = 2
                gizliHarfler[i] = ' '
            }
        }


        for (i in 0..4) {
            if (geriBildirim[i] != 0) continue

            val index = gizliHarfler.indexOf(tahminHarfleri[i])
            if (index != -1) {
                geriBildirim[i] = 1
                gizliHarfler[index] = ' '
            }
        }


        for (i in 0..4) {
            val hucre = hucreler[satir][i]
            hucre.isEnabled = false

            when (geriBildirim[i]) {
                2 -> {
                    hucre.setBackgroundColor(dogruRenk)
                    hucre.setTextColor(Color.WHITE)
                }
                1 -> {
                    hucre.setBackgroundColor(yanlisYerdeRenk)
                    hucre.setTextColor(Color.WHITE)
                }
                else -> {
                    hucre.setBackgroundColor(yanlisRenk)
                    hucre.setTextColor(Color.WHITE)
                }
            }



            hucre.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(200)
                .withEndAction {
                    hucre.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
                .start()
        }
    }

    private fun ipucuGoster() {
        if (gizliKelime.isEmpty()) return


        val bosHucreIndeksleri = mutableListOf<Int>()
        for (i in 0 until 5) {
            if (hucreler[mevcutSatir][i].text.isEmpty()) {
                bosHucreIndeksleri.add(i)
            }
        }

        if (bosHucreIndeksleri.isEmpty()) {
            Toast.makeText(this, "Bu satırda zaten tüm harfler dolu!", Toast.LENGTH_SHORT).show()
            return
        }


        val rastgeleIndex = bosHucreIndeksleri.random()
        val ipucuHarfi = gizliKelime[rastgeleIndex]


        hucreler[mevcutSatir][rastgeleIndex].apply {
            setText(ipucuHarfi.toString())
            setTextColor(Color.GREEN)
            setBackgroundColor(dogruRenk)
        }


        ipucuButonu.isEnabled = false
        ipucuButonu.alpha = 0.5f

        Toast.makeText(this, "İpucu: $ipucuHarfi harfi doğru yerinde!", Toast.LENGTH_SHORT).show()
    }

    private fun kelimeEkle(kelime: String) {
        if (kullanici == null) return

        veritabani.collection("kullanici_karsilasilan_kelimeler")
            .document(kullanici.uid)
            .collection("kelimeler")
            .document(kelime)
            .set(mapOf("ingilizceKelime" to kelime))
            .addOnSuccessListener {
                Log.d("Bulmaca", "Karşılaşılan kelime eklendi: $kelime")
            }
            .addOnFailureListener { hata ->
                Log.e("Bulmaca", "Karşılaşılan kelime eklenemedi", hata)
            }
    }

    private fun kelimeEkleOgrenilmis(kelime: String) {
        if (kullanici == null) return

        veritabani.collection("ogrenilmis_kelimeler")
            .document(kullanici.uid)
            .collection("kelimeler")
            .document(kelime)
            .set(mapOf("ingilizceKelime" to kelime))
            .addOnSuccessListener {
                Log.d("Bulmaca", "Öğrenilmiş kelime eklendi: $kelime")
            }
            .addOnFailureListener { hata ->
                Log.e("Bulmaca", "Öğrenilmiş kelime eklenemedi", hata)
            }
    }

    private fun kazanimAnimasyonuGoster() {
        for (i in 0 until 5) {
            hucreler[mevcutSatir][i].postDelayed({
                hucreler[mevcutSatir][i].animate()
                    .scaleY(1.5f)
                    .scaleX(1.5f)
                    .setDuration(200)
                    .withEndAction {
                        hucreler[mevcutSatir][i].animate()
                            .scaleY(1f)
                            .scaleX(1f)
                            .setDuration(200)
                            .start()
                    }
                    .start()
            }, i * 100L)
        }
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }}