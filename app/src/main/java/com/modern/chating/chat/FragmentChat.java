package com.modern.chating.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.modern.chating.R;
import com.modern.chating.adapter.ChatAdapter;
import com.modern.chating.file.Upload;
import com.modern.chating.firebase.F2base;
import com.modern.chating.firebase.FirebaseExecute;
import com.modern.chating.firebase.send.ListMessages;
import com.modern.chating.firebase.send.Messages;
import com.modern.chating.format.SampleFormat;
import com.modern.chating.modal.Chat;

import java.util.ArrayList;

import wtc.material.WtcSize;

public class FragmentChat extends Fragment {
    private NestedScrollView scrollView;
    private TextInputEditText inputMessage;
    private LinearLayout ll_keyboard, ll_btn_action, ll_show_action;
    private ImageView image_action, image_camera_action, image_file_show;
    private View view;
    private String imageUrl;
    private ArrayList<Chat> chats;
    private ChatAdapter adapter;
    private SampleFormat time;
    private boolean isShow = false;
    private FirebaseExecute firebase;
    private String email;
    private SharedPreferences shared;
    private String nama, namaSender;
    private FirebaseExecute.Message message;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> pickVideoLauncher;
    private ActivityResultLauncher<Intent> pickDocumentLauncher;
    private Toolbar toolbar;
    boolean isSelected = false;
    private String category;

    public FragmentChat(Toolbar toolbar, String namaSender, String nama, String email, String category, String imageUrl) {
        this.namaSender = namaSender;
        this.nama = nama;
        this.email = email;
        this.toolbar = toolbar;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frame_chat, container, false);


        shared = getContext().getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        firebase = new FirebaseExecute();
        message = new FirebaseExecute.Message();



        // Inisialisasi komponen UI
        scrollView = view.findViewById(R.id.scrollView);
        inputMessage = view.findViewById(R.id.input_message);
        ll_keyboard = view.findViewById(R.id.ll_keyboard);
        image_action = view.findViewById(R.id.image_action);
        ll_btn_action = view.findViewById(R.id.ll_action);
        image_camera_action = view.findViewById(R.id.image_camera_action);
        ll_show_action = view.findViewById(R.id.ll_show_action);
        LinearLayout ll_action_video = view.findViewById(R.id.ll_action_video);
        LinearLayout ll_action_document = view.findViewById(R.id.ll_action_document);

        ll_action_document.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*"); // Bisa diubah ke "application/pdf" atau lainnya
            pickDocumentLauncher.launch(intent);
        });


        ll_action_video.setOnClickListener(v-> {
            selectVideo();
        });


        image_file_show = view.findViewById(R.id.image_file_show);


        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadFile(imageUri, "image");

                        }
                    }
                }
        );

        pickDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadFile(imageUri, "document");

                        }
                    }
                }
        );

        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri videoUri = result.getData().getData();
                        if (videoUri != null) {
                            uploadFile(videoUri, "video");
                        }
                    }
                }
        );


        LinearLayout image_upload_gallery = view.findViewById(R.id.image_upload_gallery);
        image_upload_gallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        time = new SampleFormat();
        chats = new ArrayList<>();

        // Handle klik tombol untuk menampilkan/menyembunyikan ll_show_action
        image_file_show.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            ll_show_action.setVisibility(isShow ? View.GONE : View.VISIBLE);
            isShow = !isShow;
        });

        // Deteksi keyboard muncul atau tidak
        final View rootLayout = requireActivity().getWindow().getDecorView();
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootLayout.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootLayout.getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                ViewGroup.LayoutParams params = ll_keyboard.getLayoutParams();
                params.height = keypadHeight - 100;
                ll_keyboard.setLayoutParams(params);
                ll_keyboard.setVisibility(View.VISIBLE);
            } else {
                ll_keyboard.setVisibility(View.GONE);
            }
        });

        inputMessage.setOnClickListener(v -> {
            ll_show_action.setVisibility(View.GONE);
            isShow = false;
        });

        // Set listener untuk mengubah tombol berdasarkan teks input
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                boolean isEmpty = editable.toString().trim().isEmpty();
                image_action.setImageResource(isEmpty ? R.drawable.baseline_mic_24 : R.drawable.baseline_send_24);
                image_camera_action.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            }
        });

        // Set listener untuk tombol kirim
        image_action.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            sendMessage(message);
        });

        recycler_chat();



        return view;
    }

    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        pickVideoLauncher.launch(intent);
    }


    private void uploadFile(Uri fileUri, String typeDocument) {
        ll_show_action.setVisibility(View.GONE);
        Upload upload = new Upload(getContext(), fileUri);



        WtcSize wtcSize = new WtcSize(getContext().getContentResolver());
        wtcSize.fileSize(fileUri, new WtcSize.OnCallback() {
            @Override
            public void onResponse(String fileName, String type, String fileSize, int width, int height) {
                if(!fileName.isEmpty()){
                    Log.d("WtcSize", "File Name: " + fileName);
                    Log.d("WtcSize", "File Size: " + fileSize + " KB");
                    Log.d("WtcSize", "Width: " + width);
                    Log.d("WtcSize", "Height: " + height);
                    Log.d("WtcSize", "Type: " + type);

                    Chat.Size size = new Chat.Size(fileSize, width, height);
                    upload.setFileSize(size);
                    Chat tempChat = new Chat(
                            System.currentTimeMillis() + "",
                            "",
                            new Chat.Map(0, 0),
                            new Chat.Images(fileUri.toString(), "loading", size, 10, 0),
                            "",
                            FirebaseExecute.Message.getJamMenit(),
                            new Chat.File(fileName, type, "upload"),
                            new Chat.User(System.currentTimeMillis() + "", "", email, "", true)
                    );

                    chats.add(tempChat);
                    int index = chats.size() - 1;
                    adapter.notifyItemInserted(index);

                    upload.setSender(shared.getString("email", ""));
                    upload.setReceive(email);
                    upload.setFormat(typeDocument);
                    upload.setFileName(fileName);
                    upload.setQuery(typeDocument, new Upload.OnResponse() {
                        @Override
                        public void onSuccess(String data, String messages) {
                            Log.d("Upload", "Success: " + data);
                        }

                        @Override
                        public void onFailed(String error) {
                            Log.e("Upload", "Failed: " + error);
                            if (index >= 0 && index < chats.size()) {
                                chats.remove(index);
                                adapter.notifyItemRemoved(index);
                            }
                        }
                    });
                }
            }
        });





    }


    private void sendMessage(String message_) {
        if (!message_.isEmpty()) {

            chats.add(new Chat(
                    System.currentTimeMillis() + "",
                    message_,
                    new Chat.Map(0, 0),
                    new Chat.Images("", "null",new Chat.Size("", 0,0),0, 0),
                    "",
                    FirebaseExecute.Message.getJamMenit(),
                    new Chat.File("", "", ""),
                    new Chat.User(System.currentTimeMillis() + "", "", email, "", true)
            ));
            adapter.notifyItemInserted(chats.size() - 1);

            Messages messages = new Messages(getContext());
            if(category.isEmpty()){
                messages.setSender(shared.getString("email", ""));
                messages.setReceiver(email);
            }else{
                messages.setCategory(category);
                messages.setSender(shared.getString("email", ""));
                messages.setReceiver(email.replace(".", "_"));
            }

            messages.sendMessages(message_);
            inputMessage.setText("");
        }
    }

    private void recycler_chat() {
        // Contoh pesan bawaan
//        chats.add(new Chat("1", "", "", "", "10:00 AM",
//                new Chat.File("https://chaerul.biz.id/pdf/Dewi_Lestari_Dee_Perahu_Kertas.pdf", "pdf"),
//                new Chat.User("1", "MR. ROBOT", imageUrl, true)));
//
//        chats.add(new Chat("1", "", "https://wallpapercave.com/wp/VM5TgKU.jpg", "", "10:00 AM",
//                new Chat.File("", ""),
//                new Chat.User("1", "MR. ROBOT", imageUrl, true)));
//
//        chats.add(new Chat("1", "", "", "s", "20:50 PM",
//                new Chat.File("", ""),
//                new Chat.User("1", "MR. ROBOT", imageUrl, true)));
//
//        chats.add(new Chat("2", "https://chaerul.biz.id", "", "", "05:30 AM",
//                new Chat.File("", ""),
//                new Chat.User("2", "Chaerul", "", true)));
//
//        chats.add(new Chat("1", "test https://google.com", "", "", "22:56 PM",
//                new Chat.File("", ""),
//                new Chat.User("1", "MR. ROBOT", imageUrl, true)));

        // Inisialisasi RecyclerView
        RecyclerView recycler = view.findViewById(R.id.recycler_chating);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        recycler.setLayoutManager(linearLayoutManager);
        adapter = new ChatAdapter(getContext(), email, chats, new ChatAdapter.OnLongClickListener() {
            @Override
            public void onLongClick(Chat chat, View view, RelativeLayout rl_bg) {
                Chat data = chat;
                Log.d("FragmentChat", "=====================LONG CLICK==========================");
                Log.d("FragmentChat", "Id: " + data.id);
                Log.d("FragmentChat", "Message: " + data.message);
                Log.d("FragmentChat", "User: " + data.user);
                Log.d("FragmentChat", "Email: " + data.user.email);
                Log.d("FragmentChat", "==================================================");
                toolbar.setVisibility(View.VISIBLE);
                if(isSelected){
                    toolbar.setVisibility(View.VISIBLE);
                    isSelected = false;
                    rl_bg.setBackgroundColor(getContext().getResources().getColor(R.color.selected));
                }else{
                    toolbar.setVisibility(View.GONE);
                    isSelected = true;
                    rl_bg.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
                }
            }
        });
        recycler.setAdapter(adapter);
        new F2base().isCheck(email, shared.getString("email", ""), new F2base.OnCheckCallback() {
            @Override
            public void onResult(boolean isRegistered) {
                if(isRegistered){
                    ListMessages listMessage = new ListMessages(getContext(), chats, adapter);
                    listMessage.setData(nama, imageUrl, email.replace(".", "_"), shared.getString("email", "").replace(".", "_"));
                }
            }
        });

        new F2base().isCheck(shared.getString("email", ""), email.replace(".", "_"), new F2base.OnCheckCallback() {
            @Override
            public void onResult(boolean isRegistered) {
                if(isRegistered){
                    ListMessages listMessage = new ListMessages(getContext(), chats, adapter);
                    listMessage.setData(nama, imageUrl, shared.getString("email", ""), email.replace(".", "_"));
                }
            }
        });

        new F2base().isCheckChatbot(
                category.replace(".", "_").replace(" ", "_"), new F2base.OnCheckCallback() {
            @Override
            public void onResult(boolean isRegistered) {
                if(isRegistered){
                    Log.d("FrgamentActivity", "Di temukan: "+category);
                    ListMessages listMessage = new ListMessages(getContext(), chats, adapter);
                    listMessage.setDataChatbot(
                            nama,
                            imageUrl,
                            category.replace(".", "_").replace(" ", "_"),
                            "chatbot@app.com", email.replace(".", "_")
                    );
                }
            }
        });
    }
}
