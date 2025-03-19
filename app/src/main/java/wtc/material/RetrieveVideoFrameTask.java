package wtc.material;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.modern.chating.file.FileNameExtractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class RetrieveVideoFrameTask extends AsyncTask<String, Integer, File> {
    private final ImageView imageView;
    private final ProgressBar progressBar;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final File saveDirectory;
    private final Context context;

    public RetrieveVideoFrameTask(Context context, ImageView imageView, ProgressBar progressBar) {
        this.imageView = imageView;
        this.progressBar = progressBar;
        this.context = context;
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        // Tentukan lokasi penyimpanan gambar
        this.saveDirectory = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "com.modern.chating");
        if (!saveDirectory.exists()) {
            saveDirectory.mkdirs(); // Buat folder jika belum ada
        }
    }

    @Override
    protected void onPreExecute() {
        handler.post(() -> {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        });
    }

    @Override
    protected File doInBackground(String... urls) {
        String videoUrl = urls[0];
        String fileName = FileNameExtractor.getFileNameFromUrl(videoUrl).replace(".mp4", ".jpg");
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoUrl, new HashMap<>());

            // Simulasikan progress
            for (int i = 1; i <= 100; i += 20) {
                Thread.sleep(300);
                publishProgress(i);
            }

            // Ambil frame dari video pada detik ke-1 (1000000 microseconds)
            Bitmap frame = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST);
            if (frame == null) return null;

            // Simpan gambar ke file dengan fileName yang sesuai
            return saveBitmapToFile(frame, fileName);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        handler.post(() -> progressBar.setProgress(values[0]));
    }

    @Override
    protected void onPostExecute(File file) {
        handler.post(() -> {
            progressBar.setVisibility(View.GONE);
            if (file != null) {
                Log.d("RetrieveFrame", "Gambar disimpan di: " + file.getAbsolutePath());

                // Tampilkan gambar yang telah disimpan
                Glide.with(context)
                        .load(file)
                        .into(imageView);
            } else {
                Log.e("RetrieveFrame", "Gagal menyimpan frame");
            }
        });
    }

    /**
     * Fungsi untuk menyimpan Bitmap ke file dengan nama tertentu
     */
    private File saveBitmapToFile(Bitmap bitmap, String fileName) {
        File imageFile = new File(saveDirectory, fileName);
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
