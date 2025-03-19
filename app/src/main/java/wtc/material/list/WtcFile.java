package wtc.material.list;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WtcFile {
    private final String type;
    private final Context context;
    private final ArrayList<WtcModal> arrayList;
    private final ExecutorService executorService;

    public WtcFile(Context context, ArrayList<WtcModal> arrayList, String type) {
        this.context = context;
        this.type = type;
        this.arrayList = arrayList;
        this.executorService = Executors.newFixedThreadPool(4); // Gunakan 4 thread untuk paralelisasi
    }

    public void execute() {
        executorService.execute(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    searchFilesUsingMediaStore();
                } else {
                    searchFilesUsingFileSystem();
                }
            } catch (Exception e) {
                Log.e("WtcFile", "Error saat mencari file: " + e.getMessage());
            } finally {
                executorService.shutdown();
            }
        });
    }

    private void searchFilesUsingMediaStore() {
        List<String> validExtensions = getFileExtensions(type);
        String selection = "";

        for (String ext : validExtensions) {
            selection += MediaStore.Files.FileColumns.DATA + " LIKE '%" + ext + "' OR ";
        }
        if (!selection.isEmpty()) {
            selection = selection.substring(0, selection.length() - 4); // Hapus " OR " terakhir
        }

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                new String[]{MediaStore.Files.FileColumns.DATA},
                selection,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(0);
                arrayList.add(new WtcModal(filePath));
                Log.d("MediaStore", "File ditemukan: " + filePath);
            }
            cursor.close();
        }
        Log.d("MediaStore", "Jumlah file ditemukan: " + arrayList.size());
    }

    private void searchFilesUsingFileSystem() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("WtcFile", "Permission ditolak: Tidak dapat mengakses penyimpanan.");
            return;
        }

        File storageDir = Environment.getExternalStorageDirectory();
        if (storageDir.exists() && storageDir.isDirectory()) {
            Log.d("WtcFile", "Memulai pencarian file di: " + storageDir.getAbsolutePath());
            searchFiles(storageDir, getFileExtensions(type));
        } else {
            Log.e("WtcFile", "Direktori penyimpanan tidak ditemukan.");
        }
    }

    private void searchFiles(File dir, List<String> validExtensions) {
        File[] files = dir.listFiles((dir1, name) -> {
            for (String ext : validExtensions) {
                if (name.toLowerCase().endsWith(ext)) {
                    return true;
                }
            }
            return false;
        });

        if (files != null) {
            for (File file : files) {
                arrayList.add(new WtcModal(file.getAbsolutePath()));
                Log.d("FileSystem", "File ditemukan: " + file.getAbsolutePath());
            }
        }

        File[] directories = dir.listFiles(File::isDirectory);
        if (directories != null) {
            for (File subDir : directories) {
                executorService.execute(() -> searchFiles(subDir, validExtensions));
            }
        }

        Log.d("FileSystem", "Jumlah file ditemukan: " + arrayList.size());
    }

    private List<String> getFileExtensions(String type) {
        switch (type) {
            case "image":
                return Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp");
            case "video":
                return Arrays.asList(".mp4", ".mkv", ".avi", ".mov", ".wmv");
            case "document":
                return Arrays.asList(".pdf", ".doc", ".docx", ".txt", ".xls", ".xlsx", ".ppt", ".pptx");
            case "all":
                return Arrays.asList(""); // Semua file tanpa filter
            default:
                return new ArrayList<>();
        }
    }
}
