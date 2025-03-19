package com.modern.chating.file;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    public static String getPath(Context context, Uri uri) {
        try {
            // Ambil nama file dari URI
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            String displayName = null;
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                displayName = cursor.getString(nameIndex);
                cursor.close();
            }

            if (displayName == null) {
                displayName = "temp_file";
            }

            // Simpan file sementara di cache directory aplikasi
            File file = new File(context.getCacheDir(), displayName);
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(file)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
            }

            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("FileUtils", "Gagal mendapatkan path dari URI", e);
            return null;
        }
    }
    public static String getFileName(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        String fileName = null;

        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            fileName = cursor.getString(nameIndex);
            cursor.close();
        }

        return fileName != null ? fileName : "unknown_file";
    }

    public static String getFileName(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "unknown.jpg"; // Handle jika URL kosong/null
        }
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }


}
