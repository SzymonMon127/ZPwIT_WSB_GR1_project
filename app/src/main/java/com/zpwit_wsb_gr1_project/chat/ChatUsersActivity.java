package com.zpwit_wsb_gr1_project.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.marsad.stylishdialogs.StylishAlertDialog;
import com.zpwit_wsb_gr1_project.R;
import com.zpwit_wsb_gr1_project.adapter.ChatUserAdapter;
import com.zpwit_wsb_gr1_project.model.ChatUserModel;
import com.zpwit_wsb_gr1_project.model.Users;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ChatUsersActivity extends AppCompatActivity {

    ChatUserAdapter adapter;
    List<ChatUserModel> list;
    FirebaseUser user;
    private ListenerRegistration registration;
    private ListenerRegistration registration2;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users_activity);

        init();

        fetchUserData();

        clickListener();
       getUserSearch(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    void init() {

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search_ET);

        list = new ArrayList<>();
        adapter = new ChatUserAdapter(this, list);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();


    }


    void fetchUserData() {

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");

        registration = reference.whereArrayContains("uid", user.getUid())
                .addSnapshotListener((value, error) -> {

                    if (error != null)
                        return;

                    if (value == null)
                        return;



                    if (value.isEmpty())
                        return;


                    list.clear();
                    for (QueryDocumentSnapshot snapshot : value) {

                        if (snapshot.exists()) {
                            ChatUserModel model = snapshot.toObject(ChatUserModel.class);
                            list.add(model);
                        }

                    }
                    Collections.sort(list, (o1, o2) -> o2.getTime().compareTo(o1.getTime()));
                    adapter.notifyDataSetChanged();

                });


    }

    private void getUserSearch(String id2) {
        FirebaseFirestore.getInstance().collection("Users").document(id2).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot snapshot = task.getResult();
                String searchMy = snapshot.getString("search");
                searchUser(searchMy);
            }
        });
    }

    private void searchUser(String searchString) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                CollectionReference reference2 = FirebaseFirestore.getInstance().collection("Messages");

                reference2.whereArrayContains("uid", user.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            list.clear();

                            for (DocumentSnapshot snapshot : task.getResult()) {
                                if (snapshot.exists()) {
                                    ChatUserModel model = snapshot.toObject(ChatUserModel.class);
                                    List<String> newList = model.getSearch();
                                    newList.remove(searchString);

                                    boolean czyAaZawieraSieWliscie = newList.stream()
                                            .anyMatch(element -> element.contains(query.toLowerCase(Locale.ROOT)));


                                    if (czyAaZawieraSieWliscie) {
                                        list.add(model);
                                    }

                                }

                            }
                            Collections.sort(list, (o1, o2) -> o2.getTime().compareTo(o1.getTime()));
                            adapter.notifyDataSetChanged();

                        }

                    }
                });


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                    if (newText.equals(""))
                    {
                        fetchUserData();
                    }
                return false;
            }
        });

    }

    void clickListener() {

        adapter.OnStartChat((position, uids, chatID) -> {

            String oppositeUID;
            if (!uids.get(0).equalsIgnoreCase(user.getUid())) {
                oppositeUID = uids.get(0);
            } else {
                oppositeUID = uids.get(1);
            }

            Intent intent = new Intent(ChatUsersActivity.this, ChatActivity.class);
            intent.putExtra("uid", oppositeUID);
            intent.putExtra("id", chatID);
            startActivity(intent);
            this.finish();


        });

    }


    @Override
    protected void onResume() {
        updateStatus(true);
        super.onResume();

    }



    @Override
    protected void onStop() {
        if (registration != null) {
            registration.remove();
        }
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) {
            registration.remove();
        }
        if (registration2 != null) {
            registration2.remove();
        }
    }

    void updateStatus(boolean status) {

        Map<String, Object> map = new HashMap<>();
        map.put("online", status);

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .update(map);
    }




}