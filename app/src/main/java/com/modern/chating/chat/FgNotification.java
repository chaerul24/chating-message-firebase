package com.modern.chating.chat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.modern.chating.R;
import com.modern.chating.adapter.NotificationAdapter;
import com.modern.chating.firebase.FirebaseExecute;
import com.modern.chating.modal.Notification;

import java.util.ArrayList;

public class FgNotification extends Fragment {
    private View view;
    private SharedPreferences sharedPreferences;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.notification_activity, container, false);

        sharedPreferences = getActivity().getSharedPreferences("MyApp", getActivity().MODE_PRIVATE);
        recyclerView = view.findViewById(R.id.item_notification);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        ArrayList<Notification> array = new ArrayList<>();
        array.add(new Notification(
                "Tono Jono",
                "emailteman@gmail.com",
                "friends",
                "seseorang telah mengirim notifikasi pertemanan.",
                FirebaseExecute.Message.getJamMenit(),
                "https://i.pinimg.com/736x/5d/ac/3d/5dac3dbc73904df22cb56a8778b685aa.jpg"
        ));

        array.add(new Notification(
                "Selamat datang",
                "emailteman@gmail.com",
                "info",
                "halo, selamat datang di aplikasi chatting",
                FirebaseExecute.Message.getJamMenit(),
                "https://i.pinimg.com/736x/5d/ac/3d/5dac3dbc73904df22cb56a8778b685aa.jpg"
        ));

        array.add(new Notification(
                "Selamat datang",
                "emailteman@gmail.com",
                "info",
                "halo, selamat datang di aplikasi chatting",
                FirebaseExecute.Message.getJamMenit(),
                "https://i.pinimg.com/736x/5d/ac/3d/5dac3dbc73904df22cb56a8778b685aa.jpg"
        ));

        NotificationAdapter adapter = new NotificationAdapter(getContext(), array);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
