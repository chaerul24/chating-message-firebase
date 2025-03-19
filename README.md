# Chating

Chating adalah aplikasi chatting real-time yang menggunakan teknologi Firebase untuk autentikasi, Brevo untuk pengiriman OTP email, dan backend PHP untuk menyimpan file/gambar di server.

## Fitur Utama

- 🔥 **Autentikasi Tanpa Password**: Pengguna cukup memasukkan email dan kode OTP yang dikirim ke email mereka.
- ✉️ **Verifikasi OTP dengan Brevo**: OTP dikirim ke email pengguna untuk meningkatkan keamanan.
- 💬 **Chat Real-Time**: Percakapan berlangsung secara langsung dengan komunikasi antara aplikasi dan backend PHP.
- 📤 **Kirim Pesan & Gambar**: Pengguna dapat mengirim dan menerima pesan serta mengunggah gambar yang disimpan di server.
- 🔔 **Notifikasi**: Notifikasi dikirim saat pengguna menerima pesan baru.

## Teknologi yang Digunakan

- **Java (Android Studio)** - Bahasa pemrograman utama.
- **Firebase Authentication** - Untuk autentikasi pengguna.
- **Brevo (Sendinblue API)** - Untuk pengiriman OTP ke email pengguna.
- **Backend PHP** - Untuk mengelola autentikasi OTP, penyimpanan pesan, dan file/gambar.
- **MySQL** - Untuk menyimpan data pengguna dan pesan.logika bisnis dan UI.

## Instalasi dan Konfigurasi

### 1. Clone Repository

```sh
git clone https://github.com/chaerul24/chating-message-firebase
cd chating-message-firebase
```

### 2. Buka di Android Studio

- Buka Android Studio
- Pilih **Open an Existing Project**, lalu pilih folder proyek

### 3. Instalasi Dependencies

Pastikan semua dependencies sudah terpasang di `build.gradle (Module: app)`, lalu sync project.

### 4. Konfigurasi Firebase

- Buat project di [Firebase Console](https://console.firebase.google.com/)
- Tambahkan aplikasi Android dan daftarkan package name
- Unduh `google-services.json` dan letakkan di folder `app/`
- Tambahkan dependency Firebase Authentication di `build.gradle (Module: app)`

### 5. Konfigurasi Brevo (Sendinblue)

- Buat akun di [Brevo](https://www.brevo.com/)
- Dapatkan API Key dari dashboard Brevo
- Simpan API Key di `chating/local.properties` 
- Serta setting configurtaion

```sh
TWILIO_ACCESS_TOKEN=
TWILIO_SID=
TWILIO_PHONE_NUMBER=
BASE_URL=https://example.com/api/
BASE_URL_EMAIL_SENDER=
BASE_URL_EMAIL_VERIFY=
BASE_URL_CALL_STATUS=
BASE_URL_CALL_END=
BASE_URL_TOKEN=
BASE_URL_INDEX=
BASE_URL_IAMGE=
```

---

### 6.Struktur Backend(PHP)
📂 Backend (PHP)
│── 📂 brevo/           # Folder untuk integrasi dengan Brevo (Sendinblue API)
│── 📂 db/              # Folder database, kemungkinan berisi koneksi dan skema MySQL
│── 📂 vendor/          # Folder untuk library PHP (Composer)
│── .env               # File konfigurasi lingkungan (API keys, database credentials, dll.)
│── call_status.php    # Skrip PHP untuk memeriksa status panggilan
│── check_status.php   # Skrip PHP untuk memeriksa status OTP atau pengguna
│── composer.json      # File konfigurasi Composer untuk dependensi PHP
│── composer.lock      # File yang mengunci versi dependensi Composer
│── config.php         # Konfigurasi utama backend, seperti koneksi database
│── data.json         # File penyimpanan sementara data dalam format JSON
│── end_call.php      # Skrip untuk mengakhiri panggilan
│── index.php         # Skrip utama, mungkin sebagai endpoint utama API
│── send_otp.php      # Skrip PHP untuk mengirim kode OTP ke email pengguna
│── token.php         # Skrip untuk mengelola token autentikasi pengguna
│── verify.php        # Skrip untuk verifikasi OTP pengguna


**Dibuat dengan ❤️ oleh Chaerul Wahyu Iman Syah**

📸 Instagram: [zona.erul](https://instagram.com/zona.erul)\
🎵 TikTok: [chaerulhome21](https://www.tiktok.com/@chaerulhome21)

