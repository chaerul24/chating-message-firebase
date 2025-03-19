package com.modern.chating.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.modern.chating.R;
import com.modern.chating.adapter.StatusAdapter;
import com.modern.chating.firebase.F2base;
import com.modern.chating.firebase.FirebaseExecute;
import com.modern.chating.modal.Status;

import java.util.ArrayList;

public class FgStatus extends Fragment {
    private View view;
    private ArrayList<Status> array;
    private RecyclerView recyclerView;
    private SharedPreferences shared;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.status_activity, container, false);

        shared = getContext().getSharedPreferences("MyApp", Context.MODE_PRIVATE);

        recyclerView = view.findViewById(R.id.recycler_status);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        
        array = new ArrayList<Status>();

        FirebaseExecute.Status execute = new FirebaseExecute.Status(
                getContext(),
                shared.getString("email", ""),
                array,
                new StatusAdapter(getContext(), array),
                recyclerView);
        execute.load();



        return view;
    }

}
