package com.modern.chating.chat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.modern.chating.ChatActivity;
import com.modern.chating.R;
import com.modern.chating.adapter.ItemAdapter;
import com.modern.chating.file.FileUtils;
import com.modern.chating.file.Upload;
import com.modern.chating.firebase.FirebaseExecute;
import com.modern.chating.modal.ModalArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

public class FgChat extends Fragment {
    private View view;
    private SharedPreferences shared;
    private ArrayList<ModalArray> array;
    private RecyclerView recycler_view;
    private FloatingActionButton floatActionButton;
    private String nama;
    private ImageView imageView; // Variabel imageView dibuat di luar agar bisa diakses di pickLauncher
    private ActivityResultLauncher<Intent> pickLauncher;
    private String uploadedImage = "";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Registrasi launcher untuk memilih gambar
        pickLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageView != null) {
                            imageView.setImageURI(imageUri);
                            uploadPost(imageUri, new Upload.OnStatusUpload() {
                                @Override
                                public void onSuccess(String messages, String image) {
                                    uploadedImage = image;
                                    Toast.makeText(getContext(), "Berhasil mengunggah gambar", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailed(String error) {
                                    Log.d("Upload", "Gagal mengunggah gambar");
                                    Toast.makeText(getContext(), "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.chat_activity, container, false);
        shared = getContext().getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        String email = shared.getString("email", "");
        Log.d("DataActivity", "Email: " + email);

        array = new ArrayList<>();
        recycler_view = view.findViewById(R.id.recycler_view);
        recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler_view.setHasFixedSize(true);
        floatActionButton = view.findViewById(R.id.add_friend);

        floatActionButton.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
            View dialogView = getLayoutInflater().inflate(R.layout.alert_dialog_custom, null);

            EditText editText = dialogView.findViewById(R.id.input_email);
            EditText editText_name = dialogView.findViewById(R.id.edittext_name);
            EditText editText_description = dialogView.findViewById(R.id.edittext_description);
            AppCompatButton btnSave = dialogView.findViewById(R.id.btn_save);
            RadioButton radioTeman = dialogView.findViewById(R.id.radiobutton_teman);
            RadioButton radioChatbot = dialogView.findViewById(R.id.radiobutton_chatbot);
            LinearLayout llChatbot = dialogView.findViewById(R.id.ll_chatbot);
            LinearLayout llFriends = dialogView.findViewById(R.id.ll_friends);
            RelativeLayout rl_select_image = dialogView.findViewById(R.id.rl_select_image);
            imageView = dialogView.findViewById(R.id.image_chatbot); // Simpan imageView di variabel global
            Button btn_simpan = dialogView.findViewById(R.id.btn_simpan);
            btn_simpan.setOnClickListener(view1 -> {
                if (!uploadedImage.isEmpty()) {
                    String name = editText_name.getText().toString().trim();
                    String description = editText_description.getText().toString().trim();
                    uploadData(uploadedImage, shared.getString("email", ""), name, description);
                }
            });
            // Pemilihan gambar
            rl_select_image.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                pickLauncher.launch(intent);
            });

            // Listener untuk RadioButton
            View.OnClickListener radioListener = view1 -> {
                boolean isTemanChecked = view1.getId() == R.id.radiobutton_teman;
                radioTeman.setChecked(isTemanChecked);
                radioChatbot.setChecked(!isTemanChecked);
                llChatbot.setVisibility(isTemanChecked ? View.GONE : View.VISIBLE);
                llFriends.setVisibility(isTemanChecked ? View.VISIBLE : View.GONE);
            };

            radioTeman.setOnClickListener(radioListener);
            radioChatbot.setOnClickListener(radioListener);

            // Cegah tombol save jika email kosong
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    boolean isEmpty = editable.toString().trim().isEmpty();
                    editText.setError(isEmpty ? "Email masih kosong!" : null);
                    btnSave.setEnabled(!isEmpty);
                }
            });

            btnSave.setOnClickListener(view12 -> {
                String emailTeman = editText.getText().toString().trim();
                FirebaseExecute.User user = new FirebaseExecute.User();
                user.addFriends(shared.getString("email", ""), emailTeman, isRegistered -> {
                    String message = isRegistered ? "Berhasil menambahkan teman" : "Gagal menambahkan teman";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                });
            });

            alertDialog.setView(dialogView);
            alertDialog.show();
        });

        FirebaseExecute firebase = new FirebaseExecute(getContext());
        firebase.user.dataProfile(shared.getString("email", ""), new FirebaseExecute.User.OnDataProfile() {
            @Override
            public void onSuccess(String name, String avatar, boolean status, String email) {
                nama = name;
                ItemAdapter adapter = new ItemAdapter(getContext(), name, email, array, (position, data) -> {
                    Log.d("MainActivity", "Clicked on item: ");
                    Log.d("MainActivity", "Name: " + data.title);
                    Log.d("MainActivity", "Email: " + data.message);
                    Log.d("MainActivity", "Image URL: " + data.urlImage);

                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        intent.putExtra("imageUrl", data.urlImage);
                        intent.putExtra("name", data.title);
                        intent.putExtra("email", data.message);
                        intent.putExtra("me", nama);
                        if(!data.category.isEmpty()){
                            intent.putExtra("category", data.category);
                        }
                        startActivity(intent);
                    }
                });
                recycler_view.setAdapter(adapter);
                new FirebaseExecute.User().listUser(shared.getString("email", "").replace(".", "_"), array, adapter, recycler_view);
                new FirebaseExecute.User().listChatbot(shared.getString("email", "").replace(".", "_"), array, adapter, recycler_view);
            }

            @Override
            public void onFailure(String message) {
                Log.e("Firebase", "Gagal mendapatkan data: " + message);
            }
        });

        return view;
    }


    private void uploadData(String image, String email, String name, String description) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users").child(email.replace(".", "_"));

        // Generate unique key untuk chatbot
        String chatbotKey = database.child("chatbot").push().getKey();

        if (chatbotKey == null) {
            Log.e("Upload", "Gagal mendapatkan chatbot key.");
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("owner", email);
        map.put("name", name);
        map.put("description", description);
        map.put("image", image);
        map.put("avatar", "https://png.pngtree.com/png-clipart/20230401/original/pngtree-smart-chatbot-cartoon-clipart-png-image_9015126.png");

        database.child("chatbot").child(chatbotKey).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");

                // Generate unique key untuk messages
                String messageKey = email.replace(".", "_") + "_" + name.replace(".", "_").replace(" ", "_").replace("#", "_");

                HashMap<String, Object> data = new HashMap<>();
                data.put("id", String.valueOf(System.currentTimeMillis())); // Pastikan ID dalam bentuk string
                data.put("message", "null"); // Jangan simpan DatabaseReference, seharusnya teks pesan kosong atau awal
                data.put("foto", image);
                data.put("vn", "null");
                data.put("sender", "chatbot@app.com");
                data.put("receive", email.replace(".", "_"));
                data.put("latitude", 0.0);
                data.put("longitude", 0.0);
                data.put("width", 0);
                data.put("height", 0);
                data.put("file_size", "null");
                data.put("time", FirebaseExecute.Message.getJamMenit());
                data.put("file", "null");
                data.put("read", false);

                messagesRef.child(messageKey).child(messagesRef.push().getKey()).setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Upload", "Berhasil membuat message baru");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Upload", "Gagal menyimpan message: " + e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Upload", "Gagal menyimpan chatbot: " + e.getMessage());
                Toast.makeText(getContext(), "Gagal menambahkan teman", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void uploadPost(Uri uri, Upload.OnStatusUpload onStatusUpload) {
        if (uri == null) {
            onStatusUpload.onFailed("File tidak ditemukan");
            return;
        }

        String filePath = FileUtils.getPath(getContext(), uri);
        if (filePath == null) {
            onStatusUpload.onFailed("Gagal mendapatkan path file");
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            onStatusUpload.onFailed("File tidak ditemukan");
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
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("file"), file))
                .addFormDataPart("type", "file")
                .build();

        Request request = new Request.Builder()
                .url("https://image.chaerul.biz.id/api/upload")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    onStatusUpload.onFailed("Upload gagal: " + e.getMessage());
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

                    } else {
                        onStatusUpload.onFailed("Gagal mendapatkan URL file");

                    }
                });
            }
        });
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
}
