package wtc.material;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.modern.chating.R;
import com.modern.chating.file.FileNameExtractor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExoPlayerCustom extends FrameLayout {
    private PlayerView playerView;
    private ExoPlayer player;
    private ImageView playButton, thumbnail;
    private ProgressBar progressBar;
    private boolean isPlaying = false;
    private Handler handler;
    private ProgressBar prog;

    public ExoPlayerCustom(Context context) {
        super(context);
        init(context);
    }

    public ExoPlayerCustom(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.exo_player_custom, this, true);

        playerView = findViewById(R.id.playerView);
        playButton = findViewById(R.id.playButton);
        thumbnail = findViewById(R.id.thumbnail);
        prog = findViewById(R.id.progress);
        progressBar = findViewById(R.id.progressBarCircular);
        progressBar.setVisibility(View.VISIBLE);

        player = new ExoPlayer.Builder(context).build();
        playerView.setPlayer(player);

        playButton.setOnClickListener(v -> play());
    }

    public void setVideo(String url) {
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
        player.setMediaItem(mediaItem);
        player.prepare();
        String fileName = FileNameExtractor.getFileNameFromUrl(url).replace(".mp4", ".jpg");
        File file = new File("/storage/emulated/0/Android/data/com.modern.chating/files/Pictures/com.modern.chating/"+ fileName);
        Log.d("RetrieveFrame", "File Name: " + fileName);
        if(file.exists()){
            prog.setVisibility(View.GONE);
            Log.d("RetrieveFrame", "Gambar sudah ada");
            progressBar.setVisibility(View.GONE);
            Glide.with(getContext())
                    .load(file)
                    .into(thumbnail);
        }else{
            prog.setVisibility(View.GONE);
            Log.d("RetrieveFrame", "Gambar belum ada");
            new RetrieveVideoFrameTask(getContext(),thumbnail, progressBar).execute(url);
        }
    }

    public void setParams(RelativeLayout.LayoutParams params) {
        playerView.setLayoutParams(params);
        thumbnail.setLayoutParams(params);
    }

    public void play() {
        if (!isPlaying) {
            thumbnail.setVisibility(GONE);
            playButton.setVisibility(GONE);
            playerView.setVisibility(VISIBLE);
            player.play();
            isPlaying = true;
        }
    }


    public void uploadPost(String filePath) {
        prog.setVisibility(View.VISIBLE);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        File file = new File(filePath);
        if (!file.exists()) {
            Log.e("UPLOAD", "File tidak ditemukan");
            prog.setVisibility(View.GONE);
            return;
        }

        // Custom RequestBody untuk menghitung progress upload
        RequestBody fileBody = RequestBody.create(MediaType.parse("video/mp4"), file);
        CountingRequestBody countingBody = new CountingRequestBody(fileBody, (bytesWritten, contentLength) -> {
            int progress = (int) ((100 * bytesWritten) / contentLength);
            handler.post(() -> prog.setProgress(progress));
        });

        // Multipart request
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), countingBody)
                .build();

        Request request = new Request.Builder()
                .url("https://image.chaerul.biz.id/api/upload")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handler.post(() -> {
                    Log.e("UPLOAD", "Gagal mengunggah file", e);
                    prog.setVisibility(View.GONE);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = Objects.requireNonNull(response.body()).string();
                handler.post(() -> {
                    Log.d("UPLOAD", "Response: " + responseBody);
                    prog.setVisibility(View.GONE);
                });
            }
        });
    }


}