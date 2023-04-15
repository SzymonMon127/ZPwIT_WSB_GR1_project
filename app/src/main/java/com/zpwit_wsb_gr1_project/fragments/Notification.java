package com.zpwit_wsb_gr1_project.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.zpwit_wsb_gr1_project.R;
import com.zpwit_wsb_gr1_project.adapter.NotificationAdapter;
import com.zpwit_wsb_gr1_project.adapter.UserAdapter;
import com.zpwit_wsb_gr1_project.model.NotificationModel;
import com.zpwit_wsb_gr1_project.model.StoriesModel;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Notification extends Fragment {

    RecyclerView recyclerView;
    NotificationAdapter adapter;
    List<NotificationModel> list;
    FirebaseUser user;
    Context context1;
    OnDataPass1 onDataPass1;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        context1 = context;
        onDataPass1 = (OnDataPass1) context;
    }

    public interface OnDataPass1 {
        void onChange1(String uid);
    }

    public Notification() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        loadNotification();
        clickListener();
    }


    private void clickListener() {
        adapter.OnNotClicked(new NotificationAdapter.OnNotClicked() {
            @Override
            public void onClicked(String uid) {
                onDataPass1.onChange1(uid);
            }
        });
    }

    void init(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context1));

        list = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), list);

        recyclerView.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();

    }

    void loadNotification() {

        List<String> userList = new ArrayList<>();
        userList.add(user.getUid());

        Query query = FirebaseFirestore.getInstance().collection("Notifications");
        query.whereIn("uid", userList).addSnapshotListener((value, error) -> {

            if (error != null) {
                Log.d("Error: ", error.getMessage());
            }

            if (value == null)
                return;

            list.clear();
            for (QueryDocumentSnapshot snapshot : value) {

                if (!value.isEmpty()) {
                    NotificationModel model = snapshot.toObject(NotificationModel.class);
                    list.add(model);
                }

                Collections.sort(list, new Comparator<NotificationModel>() {
                    @Override
                    public int compare(NotificationModel o1, NotificationModel o2) {
                        return o2.getTime().compareTo(o1.getTime());
                    }
                });

            }
            adapter.notifyDataSetChanged();

        });

    }

}