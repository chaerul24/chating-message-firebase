package wtc.material;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.modern.chating.R;

import java.io.InputStream;
import java.text.DecimalFormat;

public class WtcSize {

    private final ContentResolver contentResolver;

    public WtcSize(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;

    }

    public void fileSize(Uri uri, OnCallback callback) {
        if (uri == null || callback == null) {
            return;
        }

        try {
            String fileName = null;
            long fileSizeBytes = 0;
            String fileExtension = "";

            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                    fileSizeBytes = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE));

                    // Mengambil ekstensi file dari nama file
                    if (fileName != null && fileName.contains(".")) {
                        fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
                    }
                }
            }

            // Konversi ukuran file ke format yang lebih mudah dibaca
            String formattedSize = formatFileSize(fileSizeBytes);

            // Mendapatkan dimensi gambar jika file adalah gambar
            int[] dimensions = getImageDimensions(uri);
            int width = dimensions[0];
            int height = dimensions[1];

            Log.d("WtcSize", fileExtension);
            // Format ikon berdasarkan ekstensi file
            String type = formatFile(fileExtension);

            // Callback dengan data
            callback.onResponse(fileName, type, formattedSize, width, height);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("WtcSize", "Error getting file info: " + e.getMessage());
        }
    }


    private int[] getImageDimensions(Uri uri) {
        try (InputStream inputStream = contentResolver.openInputStream(uri)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            return new int[]{options.outWidth, options.outHeight};
        } catch (Exception e) {
            e.printStackTrace();
            return new int[]{0, 0}; // Jika terjadi error
        }
    }


    private String formatFileSize(long bytes) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (bytes < 1024) {
            return bytes + " B"; // Byte
        } else if (bytes < 1024 * 1024) {
            return decimalFormat.format(bytes / 1024.0) + " KB"; // Kilobyte
        } else if (bytes < 1024 * 1024 * 1024) {
            return decimalFormat.format(bytes / (1024.0 * 1024.0)) + " MB"; // Megabyte
        } else {
            return decimalFormat.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB"; // Gigabyte
        }
    }

    public interface OnCallback {
        void onResponse(String fileName,String type, String fileSize, int width, int height);
    }

    private String formatFile(String extension) {
        if (extension == null) return "";

        switch (extension.toLowerCase()) {
            case "pdf":
                return "pdf";
            case "doc":
                return "doc";
            case "docx":
               return "docx";
            case "xls":
                return "xls";
            case "xlsx":
                return "xlsx";
            case "apk":
                return "apk";
            case "mp3":
                return "mp3";
            case "mp4":
                return "mp4";
            default:
                return "file";
        }
    }

}
