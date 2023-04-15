package com.zpwit_wsb_gr1_project.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.zpwit_wsb_gr1_project.FragmentReplacerActivity;
import com.zpwit_wsb_gr1_project.MainActivity;
import com.zpwit_wsb_gr1_project.R;
import com.zpwit_wsb_gr1_project.adapter.ChatAdapter;
import com.zpwit_wsb_gr1_project.model.ChatModel;
import com.zpwit_wsb_gr1_project.model.Users;
import com.zpwit_wsb_gr1_project.notifications.FCMSend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    FirebaseUser user;
    CircleImageView imageView;
    TextView name, status;
    EditText chatET;
    ImageView sendBtn;
    RecyclerView recyclerView;
    String oppositeUID;
    String myUID;
    ChatAdapter adapter;
    List<ChatModel> list;
    private ListenerRegistration registration;


    String chatID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();

        loadUserData();

        loadMessages();



        sendBtn.setOnClickListener(v -> {

            String message = chatET.getText().toString().trim();

            if (message.isEmpty()) {
                return;
            }

            CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");


            Map<String, Object> map = new HashMap<>();

            map.put("lastMessage", message);
            map.put("time", FieldValue.serverTimestamp());


            reference.document(chatID).update(map);

            String messageID = reference
                    .document(chatID)
                    .collection("Messages")
                    .document()
                    .getId();

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", messageID);
            messageMap.put("message", message);
            messageMap.put("senderID", user.getUid());
            messageMap.put("time", FieldValue.serverTimestamp());


            reference.document(chatID).collection("Messages").document(messageID).set(messageMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            chatET.setText("");
                            createNotification();
                        } else {
                            Toast.makeText(ChatActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                        }
                    });

        });

        onClickListener();


    }

    private void onClickListener() {

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String USER_ID = oppositeUID;
                boolean IS_SEARCHED_USER = false;
                if (!USER_ID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                {
                    IS_SEARCHED_USER = true;
                }
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                intent.putExtra("uid", USER_ID);
                intent.putExtra("user", IS_SEARCHED_USER);
                intent.putExtra("data", 4);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    void createNotification() {


        List<String> userList = new ArrayList<>();
        userList.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Query query = FirebaseFirestore.getInstance().collection("Users");
        query.whereIn("uid", userList).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    for (final QueryDocumentSnapshot snapshot1 : task.getResult()) {
                        if (!snapshot1.exists())
                        {
                            return;
                        }
                        Users users = snapshot1.toObject(Users.class);

                        List<String> userList = new ArrayList<>();
                        userList.add(oppositeUID);
                        Query query = FirebaseFirestore.getInstance().collection("Users");
                        query.whereIn("uid", userList).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                                if (task1.isSuccessful())
                                {
                                    for (final QueryDocumentSnapshot snapshot2 : task1.getResult())
                                    {
                                        if (!snapshot2.exists())
                                        {
                                            return;
                                        }
                                        Users users1 = snapshot2.toObject(Users.class);



                                        FCMSend.pushNotification(getApplicationContext(), users1.getCloudToken(), users.getName(), getResources().getString(R.string.newMessage));
                                    }
                                }
                            }
                        });
                    }

                }
            }
        });

    }

    void init() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        imageView = findViewById(R.id.profileImage);
        name = findViewById(R.id.nameTV);
        status = findViewById(R.id.statusTV);
        chatET = findViewById(R.id.chatET);
        sendBtn = findViewById(R.id.sendBtn);

        recyclerView = findViewById(R.id.recyclerView);

        list = new ArrayList<>();
        adapter = new ChatAdapter(this, list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }


    void loadUserData() {

        oppositeUID = getIntent().getStringExtra("uid");

        FirebaseFirestore.getInstance().collection("Users").document(oppositeUID)
                .addSnapshotListener((value, error) -> {

                    if (error != null)
                        return;

                    if (value == null)
                        return;


                    if (!value.exists())
                        return;

                    //
                    boolean isOnline = Boolean.TRUE.equals(value.getBoolean("online"));
                    status.setText(isOnline ? "Online" : "Offline");

                    Glide.with(getApplicationContext()).load(value.getString("profileImage")).placeholder(R.drawable.ic_person).into(imageView);

                    name.setText(value.getString("name"));


                });

    }


    void loadMessages() {

        chatID = getIntent().getStringExtra("id");



        CollectionReference reference = FirebaseFirestore.getInstance()
                .collection("Messages")
                .document(chatID)
                .collection("Messages");

        reference
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {


                    if (error != null) return;

                    if (value == null || value.isEmpty()) return;

                    list.clear();

                    for (QueryDocumentSnapshot snapshot : value) {
                        ChatModel model = snapshot.toObject(ChatModel.class);
                        list.add(model);

                    }
                    adapter.notifyDataSetChanged();
                    int itemCount = adapter.getItemCount();
                    if (itemCount>0)
                    {
                        recyclerView.smoothScrollToPosition(itemCount-1);

                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus(true);
    }

    @Override
    protected void onPause() {
        updateStatus(false);
        super.onPause();
    }

    void updateStatus(boolean status) {

        Map<String, Object> map = new HashMap<>();
        map.put("online", status);

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .update(map);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ChatUsersActivity.class);
        startActivity(intent);
        this.finish();
        super.onBackPressed();
    }
}