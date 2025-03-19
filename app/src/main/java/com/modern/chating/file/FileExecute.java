package com.modern.chating.file;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileExecute {

    private Context context;

    public FileExecute(Context context) {
        this.context = context;
    }

    // 🔹 Membaca ukuran file dari URL
    public long getFileSize(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");  // Hanya ambil header, tidak perlu unduh
            conn.connect();
            return conn.getContentLengthLong(); // Mengembalikan ukuran file dalam byte
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Jika gagal, return -1
        }
    }

    // 🔹 Membaca ukuran file dari `res/raw/`
    public long getFileSize(int rawResId) {
        try {
            Resources res = context.getResources();
            return res.openRawResourceFd(rawResId).getLength();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // 🔹 Membaca ukuran file dari penyimpanan lokal (`/sdcard/`)
    public long getFileSize(String localPath, boolean isExternal) {
        File file;
        if (isExternal) {
            file = new File(Environment.getExternalStorageDirectory(), localPath);
        } else {
            file = new File(localPath);
        }

        if (file.exists()) {
            return file.length(); // Mengembalikan ukuran file dalam byte
        } else {
            return -1; // File tidak ditemukan
        }
    }

    // 🔹 Mendapatkan nama file dari URL
    public static String getFileName(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }

    // 🔹 Mendapatkan nama file dari `res/raw/`
    public String getFileName(int rawResId) {
        return context.getResources().getResourceEntryName(rawResId);
    }

    // 🔹 Mendapatkan nama file dari penyimpanan lokal (`/sdcard/`)
    public String getFileName(String localPath, boolean isExternal) {
        File file;
        if (isExternal) {
            file = new File(Environment.getExternalStorageDirectory(), localPath);
        } else {
            file = new File(localPath);
        }

        if (file.exists()) {
            return file.getName();
        } else {
            return "File Not Found";
        }
    }

    // 🔹 AsyncTask untuk mengunduh file dari URL ke penyimpanan lokal
    public static class FileDownloader extends AsyncTask<String, Void, Boolean> {
        private Context context;
        private String savePath;
        private DownloadCallback callback;

        public interface DownloadCallback {
            void onDownloadComplete(boolean success, String path);
        }

        public FileDownloader(Context context, String savePath, DownloadCallback callback) {
            this.context = context;
            this.savePath = savePath;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String fileUrl = params[0];

            try {
                URL url = new URL(fileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("FileDownloader", "Server returned HTTP " + conn.getResponseCode());
                    return false;
                }

                InputStream inputStream = conn.getInputStream();
                File file = new File(savePath);

                // Buat direktori jika belum ada
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }

                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }

                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (callback != null) {
                callback.onDownloadComplete(success, savePath);
            }
        }
    }
}
