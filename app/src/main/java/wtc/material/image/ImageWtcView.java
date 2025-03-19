package wtc.material.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.modern.chating.R;
import com.modern.chating.firebase.F2base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.wasabeef.glide.transformations.BlurTransformation;
import wtc.material.progress.GlideApp;
import wtc.material.progress.ProgressInterceptor;

public class ImageWtcView extends FrameLayout {
    private AppCompatImageView imageView;
    private CircularProgressIndicator circularProgressIndicator;
    private Drawable errorDrawable;
    private Drawable placeholderDrawable;
    private boolean downloadOtomatis = false;
    private String imageUrl;
    private Context context;
    private boolean isDownloading = false; // Prevent multi-click download
    private LinearLayout ll_click;
    private int width, height;
    private ImageView.ScaleType scaleType;

    public ImageWtcView(Context context) {
        super(context);
        init(context, null);
    }

    public ImageWtcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ImageWtcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.view_image_wtc, this, true);
        imageView = view.findViewById(R.id.image_view);
        circularProgressIndicator = view.findViewById(R.id.circularProgress);
        ll_click = view.findViewById(R.id.ll_click);
        if (attrs != null) {
            // Mengambil referensi placeholder dan error gambar
            int[] attrsArray = R.styleable.ImageWtcView;
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, attrsArray, 0, 0);

            try {
                // Memeriksa apakah imagePlaceholder ada
                if (a.hasValue(R.styleable.ImageWtcView_imagePlaceholder)) {
                    placeholderDrawable = a.getDrawable(R.styleable.ImageWtcView_imagePlaceholder);
                }

                // Memeriksa apakah imageError ada
                if (a.hasValue(R.styleable.ImageWtcView_imageError)) {
                    errorDrawable = a.getDrawable(R.styleable.ImageWtcView_imageError);
                }
            } finally {
                a.recycle();
            }
        }
        this.setOnClickListener(v -> {

        });
    }



    public void scaleType(ImageView.ScaleType scaleTypeValue) {
        scaleType = ImageView.ScaleType.FIT_CENTER; // Default scaleType
        switch (scaleTypeValue) {
            case CENTER_CROP:
                scaleType = ImageView.ScaleType.CENTER_CROP;
                break;
            case CENTER_INSIDE:
                scaleType = ImageView.ScaleType.CENTER_INSIDE;
                break;
            case FIT_XY:
                scaleType = ImageView.ScaleType.FIT_XY;
                break;
            default:
                scaleType = ImageView.ScaleType.FIT_CENTER;
                break;
        }
        imageView.setScaleType(scaleType);
    }

    public void setImage(String url) {
        this.imageUrl = url;
        loadImageWithBlur(downloadOtomatis, url, 15);
    }

    public void setImage(int resId) {
        circularProgressIndicator.setVisibility(View.VISIBLE);
        imageView.setImageResource(resId);
        circularProgressIndicator.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
    }

    public void loadFile(String filePath, int radius){
        Glide.with(context)
                .load(filePath)
                .override(width, height)
                .apply(new RequestOptions()
                        .frame(1000000)
                        .transform(new RoundedCorners(radius)))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    public void loadImageWithBlur(boolean downloadOtomatis, String imageUrl, int radius) {
        this.downloadOtomatis = downloadOtomatis;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "com.modern.chating/"+getFileNameFromUrl(imageUrl));

        if (file.exists()) {
            Log.d("Image", "Gambar sudah di-download, menampilkan tanpa blur");
            loadFile(file.getPath(), radius);
            circularProgressIndicator.setVisibility(View.GONE);
            return;
        }


        if(downloadOtomatis == false){
            Log.d("Image", "Url Image: "+imageUrl);
            imageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(imageUrl)
                    .override(width, height)
                    .apply(new RequestOptions()
                            .frame(1000000)
                            .transform(new RoundedCorners(20))
                            .transform(new MultiTransformation<>(new BlurTransformation(15), new RoundedCorners(15))))
                    .into(imageView);

        }else{
            Log.d("Image", "Gambar belum di-download");

            if (downloadOtomatis) {
                Log.d("Image", "Download otomatis aktif, mulai mengunduh...");
                downloadImage(imageUrl);
            } else {
                Glide.with(context)
                        .load(imageUrl)
                        .override(width, height)
                        .apply(new RequestOptions()
                                .frame(1000000)
                                .transform(new MultiTransformation<>(new BlurTransformation(15), new RoundedCorners(15))))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                circularProgressIndicator.setVisibility(View.GONE);
                                ll_click.setVisibility(View.VISIBLE);

                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                circularProgressIndicator.setVisibility(View.GONE);
                                ll_click.setVisibility(View.GONE);

                                return false;
                            }
                        })
                        .into(imageView);
            }
        }
    }

    private void downloadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            Log.e("Image", "URL gambar tidak boleh null atau kosong");
            return;
        }

        Log.d("Image", "Mulai mengunduh gambar: " + imageUrl);

        circularProgressIndicator.setVisibility(View.VISIBLE);
        imageView.setClickable(false);

        // Tambahkan listener untuk tracking progress
        ProgressInterceptor.addListener(imageUrl, (bytesRead, totalBytes) -> {
            int progress = (int) ((bytesRead * 100) / totalBytes);
            Log.d("Image", "Progres: " + progress + "%");
            circularProgressIndicator.post(() -> circularProgressIndicator.setProgress(progress));
        });

        // Proses download dengan Glide
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .override(width, height)
                .apply(new RequestOptions()
                        .frame(1000000)
                        .transform(new BlurTransformation(10))
                        .transform(new RoundedCorners(15)))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        imageView.setImageBitmap(resource);
                        saveImageToAppMedia(resource, context, getFileNameFromUrl(imageUrl), isSuccess -> {
                            isDownloading = false;
                            ProgressInterceptor.removeListener(imageUrl); // Hapus listener setelah selesai

                            if (isSuccess) {

                                Log.d("Image", "Gambar berhasil diunduh & disimpan");
                                imageView.setClickable(true);
                                circularProgressIndicator.setVisibility(View.GONE);
                                ll_click.setVisibility(View.GONE);
                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "com.modern.chating/" + getFileNameFromUrl(imageUrl));
                                loadFile(file.getPath(), 10);
                            } else {
                                Log.e("Image", "Gagal mengunduh gambar.");
                            }
                        });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        circularProgressIndicator.setVisibility(View.GONE);
                        ProgressInterceptor.removeListener(imageUrl); // Pastikan listener dihapus
                    }
                });
    }


    public boolean isImageDownloaded(String url) {
        String filePath = Environment.getExternalStorageDirectory() + "/Android/media/com.modern.chating/" + getFileNameFromUrl(url);
        return new File(filePath).exists();
    }

    private void saveImageToAppMedia(Bitmap bitmap, Context context, String fileName, F2base.OnCheckCallback callback) {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "com.modern.chating");

        // Cek apakah folder ada, jika tidak buat
        if (!directory.exists()) {
            boolean isCreated = directory.mkdirs();
            if (!isCreated) {
                Log.e("Image", "Gagal membuat folder untuk menyimpan gambar!");
                callback.onResult(false);
                return;
            }
        }

        File file = new File(directory, fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            Log.d("Image", "Gambar berhasil disimpan di: " + file.getAbsolutePath());
            ll_click.setVisibility(View.GONE);

            // Tambahkan ke galeri
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, (path, uri) -> {
                Log.d("Image", "File ditambahkan ke galeri: " + path);
            });

            callback.onResult(true);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Image", "Gagal menyimpan gambar: " + e.getMessage());
            callback.onResult(false);
        }
    }


    public String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    public interface OnCheckCallback {
        void onResult(boolean isSuccess);
    }
}
