package com.modern.chating.file;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.modern.chating.firebase.F2base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class Image {
    public void loadImageWithBlur(
            boolean downloadOtomatis,
            CircularProgressIndicator progressIndicator,
            ImageView imageView,
            String imageUrl,
            int radius,
            Context context) {

        RequestOptions options;
        Log.d("Image", "loadImageWithBlur dipanggil, Download Otomatis: " + downloadOtomatis);

        File file = new File(Environment.getExternalStorageDirectory(), "Android/media/com.modern.chating/"+FileNameExtractor.getFileNameFromUrl(imageUrl));

        if (file.exists()) {
            Log.d("Image", "Gambar sudah di-download, menampilkan tanpa blur");

            options = new RequestOptions().transform(new RoundedCorners(radius))
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            Glide.with(context)
                    .load(file)
                    .apply(options)
                    .into(imageView);

            if (progressIndicator != null) {
                progressIndicator.setVisibility(View.GONE); // Sembunyikan progress setelah selesai
            }
        } else {
            Log.d("Image", "Gambar belum di-download");

            if (downloadOtomatis) {
                Log.d("Image", "Download otomatis diaktifkan, mulai mengunduh...");

                if (progressIndicator != null) {
                    progressIndicator.setVisibility(View.VISIBLE); // Tampilkan progress
                }

                Glide.with(context)
                        .asBitmap()
                        .load(imageUrl)
                        .apply(new RequestOptions()
                                .transform(new RoundedCorners(radius))
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                imageView.setImageBitmap(resource);
                                String nameFile = FileNameExtractor.getFileNameFromUrl(imageUrl);

                                saveImageToAppMedia(resource, context, nameFile, isSuccess -> {
                                    if (isSuccess) {
                                        Log.d("Image", "Gambar berhasil disimpan dengan nama: " + nameFile);
                                    } else {
                                        Log.e("Image", "Gagal menyimpan gambar.");
                                    }

                                    if (progressIndicator != null) {
                                        progressIndicator.setVisibility(View.GONE); // Sembunyikan progress setelah selesai
                                    }
                                });
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });
            } else {
                options = new RequestOptions().transform(new RoundedCorners(radius))
                        .transform(new BlurTransformation(15))
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                Glide.with(context)
                        .load(file)
                        .apply(options)
                        .into(imageView);
                Log.d("Image", "Download otomatis dinonaktifkan, gambar tidak diunduh.");
            }
        }
    }




    public boolean isImageDownloaded(String filePath) {
        File file = new File(filePath);
        return file.exists(); // Return true jika file sudah ada
    }


    public void saveImageToAppMedia(Bitmap bitmap, Context context, String fileName, F2base.OnCheckCallback callback) {
        if (fileName == null || fileName.isEmpty()) {
            fileName = "IMG_" + System.currentTimeMillis() + ".jpg"; // Nama default
        }

        File mediaDir = new File(Environment.getExternalStorageDirectory(), "Android/media/com.modern.chating/");

        if (!mediaDir.exists()) {
            mediaDir.mkdirs(); // Buat folder jika belum ada
        }

        File imageFile = new File(mediaDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Log.d("Image", "Gambar berhasil disimpan: " + imageFile.getAbsolutePath());
            callback.onResult(true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Image", "Gagal menyimpan gambar", e);
            callback.onResult(false); // Tambahkan callback jika gagal menyimpan
        }
    }




}
