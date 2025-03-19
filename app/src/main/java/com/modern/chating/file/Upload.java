package com.modern.chating.file;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.modern.chating.BuildConfig;
import com.modern.chating.firebase.F2base;
import com.modern.chating.firebase.FirebaseExecute;
import com.modern.chating.firebase.send.Messages;
import com.modern.chating.modal.Chat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Upload {
    private Uri uri;
    private Context context;
    private String fileType = "file"; // Default
    private final String uploadUrl = "https://image.chaerul.biz.id/api/upload"; // URL server
    private String sender;
    private String receiver, fileName;
    private Chat.Size fileSize;

    public Upload(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceive(String receive){
        this.receiver = receive;
    }

    public void setQuery(String type, OnResponse onResponse) {
        Log.d("Upload", "Memulai Upload...");
        Log.d("Upload", "Sender: " + sender);
        Log.d("Upload", "Receiver: " + receiver);
        Log.d("Upload", "Type: " + type);
        new F2base().isCheck(sender.replace(".", "_"), receiver.replace(".", "_"), new F2base.OnCheckCallback() {
            @Override
            public void onResult(boolean isRegistered) {
                if(!isRegistered) {
                    String query = sender.replace(".", "_")+"_"+receiver.replace(".", "_");
                    Log.i("Upload", "Valid: " + query);
                    uploadPost(new OnStatusUpload() {
                        @Override
                        public void onSuccess(String messages, String image) {

                            add(type, image, query, new OnResponse() {
                                @Override
                                public void onSuccess(String data, String messages) {
                                    onResponse.onSuccess(data, messages);
                                }

                                @Override
                                public void onFailed(String error) {
                                    onResponse.onFailed(error);
                                }
                            });
                        }

                        @Override
                        public void onFailed(String error) {

                        }
                    });
                }else{

                }
            }
        });
        new F2base().isCheck(receiver.replace(".", "_"), sender.replace(".", "_"), new F2base.OnCheckCallback() {
            @Override
            public void onResult(boolean isRegistered) {
                if(!isRegistered) {
                    String query = receiver.replace(".", "_")+"_"+sender.replace(".", "_");
                    Log.i("Upload", "Valid: " + query);
                    uploadPost(new OnStatusUpload() {
                        @Override
                        public void onSuccess(String messages, String image) {

                            add(type, image, query, new OnResponse() {
                                @Override
                                public void onSuccess(String data, String messages) {
                                    onResponse.onSuccess(data, messages);
                                }

                                @Override
                                public void onFailed(String error) {
                                    onResponse.onFailed(error);
                                }
                            });
                        }

                        @Override
                        public void onFailed(String error) {
                            onResponse.onFailed(error);
                        }
                    });
                }else{
                }
            }
        });
    }

    public void setFileSize(Chat.Size size){
        this.fileSize = size;
    }

    private void add(String type, String imageUrl, String chatId, OnResponse onResponse) {
        Log.d("Upload", "imageUrl: " + imageUrl);

        if (imageUrl.isEmpty()) {
            Log.d("Messages", "messages is empty");
            onResponse.onFailed("messages is empty");
            return;
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("id", System.currentTimeMillis() + "");

        if(type.equals("image")){
            data.put("foto", BuildConfig.IMAGE+fileName);
            data.put("file", "null");
            data.put("message", "Mengirim gambar.");
        }
        if(type.equals("video")){
            data.put("foto", "null");
            data.put("file", BuildConfig.IMAGE+fileName);
            data.put("message", "Mengirim video.");
        }
        if(type.equals("audio")){
            data.put("foto", "null");
            data.put("file",  BuildConfig.IMAGE+fileName);
            data.put("message", "Mengirim audio.");
        }
        if(type.equals("document")){
            data.put("foto", "null");
            data.put("file",  BuildConfig.IMAGE+fileName);
            data.put("message", "Mengirim file dokumen.");
        }
        if(fileSize == null){
            data.put("file_size", "null");
        }else{
            data.put("file_size", fileSize.fileSize);
        }
        data.put("width", fileSize.width);
        data.put("height", fileSize.height);
        data.put("vn", "null");
        data.put("sender", sender.replace("_", "."));
        data.put("receive", receiver.replace("_", "."));
        data.put("latitude", 0.0);
        data.put("longitude", 0.0);
        data.put("time", FirebaseExecute.Message.getJamMenit());
        data.put("read", false);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages");

        databaseReference.child(chatId).push().setValue(data)
                .addOnSuccessListener(unused -> {
                    onResponse.onSuccess( imageUrl,"Berhasil menambahkan message ke chat yang ada");
                    Log.d("Upload", "Berhasil menambahkan message ke chat yang ada");

                })
                .addOnFailureListener(e -> {
                    onResponse.onFailed("Gagal menambahkan message ke chat yang ada");
                    Log.e("Upload", "Gagal menambahkan message ke chat yang ada", e);
                });
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public interface OnResponse {
        void onSuccess(String data, String messages);
        void onFailed(String error);
    }

    public void setFormat(String format) {
        switch (format) {
            case "image":
            case "video":
            case "audio":
            case "pdf":
                fileType = format;
                break;
            default:
                fileType = "file";
        }
    }

    private void uploadPost(OnStatusUpload onStatusUpload) {
        if (uri == null) {
            onStatusUpload.onFailed("File tidak ditemukan");
            showToast("File tidak ditemukan");
            return;
        }

        String filePath = FileUtils.getPath(context, uri);
        if (filePath == null) {
            onStatusUpload.onFailed("Gagal mendapatkan path file");
            showToast("Gagal mendapatkan path file");
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            onStatusUpload.onFailed("File tidak ditemukan");
            showToast("File tidak ditemukan");
            return;
        }

        // **Cek apakah file sudah ada sebelum upload**
        if (isFileAlreadyUploaded(file.getName())) {
            onStatusUpload.onSuccess("File sudah diunggah sebelumnya", getExistingFileUrl(file.getName()));
            showToast("File sudah diunggah sebelumnya");
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    long t1 = System.nanoTime();
                    Log.d("UPLOAD", String.format("Sending request %s", request.url()));
                    Response response = chain.proceed(request);
                    long t2 = System.nanoTime();
                    Log.d("UPLOAD", String.format("Received response for %s in %.1fms", response.request().url(), (t2 - t1) / 1e6d));
                    return response;
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse(fileType), file))
                .addFormDataPart("type", fileType)
                .build();

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    onStatusUpload.onFailed("Upload gagal: " + e.getMessage());
                    showToast("Upload gagal: " + e.getMessage());
                });
                Log.e("UPLOAD", "Gagal mengunggah file", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = Objects.requireNonNull(response.body()).string();
                Log.d("UPLOAD", "Response: " + responseBody);

                new Handler(Looper.getMainLooper()).post(() -> {
                    String uploadedUrl = extractFileUrl(responseBody);
                    if (uploadedUrl != null) {
                        onStatusUpload.onSuccess("Upload sukses!", uploadedUrl);
                        showToast("Upload sukses!");
                    } else {
                        onStatusUpload.onFailed("Gagal mendapatkan URL file");
                        showToast("Gagal mendapatkan URL file");
                    }
                });
            }
        });
    }

    private boolean isFileAlreadyUploaded(String fileName) {
        // Simpan daftar file yang sudah di-upload (Bisa diganti dengan penyimpanan di database atau shared preferences)
        File cacheDir = new File(context.getCacheDir(), "uploaded_files");
        File uploadedFile = new File(cacheDir, fileName);
        return uploadedFile.exists();
    }

    private String getExistingFileUrl(String fileName) {
        return "https://image.chaerul.biz.id/uploads/" + fileName; // Sesuaikan dengan path penyimpanan server
    }

    private String extractFileUrl(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);

            // Periksa apakah key "file" ada dalam JSON
            if (jsonObject.has("file")) {
                String fileName = jsonObject.getString("file");
                return "https://image.chaerul.biz.id/image/" + fileName; // Sesuaikan dengan path penyimpanan di server
            }

            return null;
        } catch (JSONException e) {
            Log.e("UPLOAD", "JSON Parsing error", e);
            return null;
        }
    }


    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    public interface OnStatusUpload {
        void onSuccess(String messages, String image);
        void onFailed(String error);
    }
}
