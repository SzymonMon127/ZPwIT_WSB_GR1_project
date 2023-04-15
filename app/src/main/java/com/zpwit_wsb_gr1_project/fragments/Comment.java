package com.zpwit_wsb_gr1_project.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.zpwit_wsb_gr1_project.MainActivity;
import com.zpwit_wsb_gr1_project.R;
import com.zpwit_wsb_gr1_project.ViewStoryActivity;
import com.zpwit_wsb_gr1_project.adapter.CommentAdapter;
import com.zpwit_wsb_gr1_project.adapter.NotificationAdapter;
import com.zpwit_wsb_gr1_project.model.CommentModel;
import com.zpwit_wsb_gr1_project.model.HomeModel;
import com.zpwit_wsb_gr1_project.model.NotificationModel;
import com.zpwit_wsb_gr1_project.model.Users;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class Comment extends Fragment {

    EditText commentEt;
    ImageButton sendBtn;
    RecyclerView recyclerView;
    CommentAdapter commentAdapter;
    List<CommentModel> list;
    FirebaseUser user;
    String id, uid;
    CollectionReference reference;

    TextView commentCountTv;
    Context context1;

    private  CircleImageView profileImage;
    private TextView userNameTv;
    private  TextView timeTv;
    private  TextView likeCountTv;
    private TextView descriptionTv;
    private ImageView imageView;
    private CheckBox likeCheckBox;
    private  ImageButton shareBtn;
    private ListenerRegistration registration;
    private ListenerRegistration registration1;
    private Users usersModel;
    OnDataPass3 onDataPass3;
    private ImageView editButton, deleteButton;
    private HomeModel model1;
    public Comment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        context1 = context;
        onDataPass3 = (OnDataPass3) context;

    }

    public interface OnDataPass3 {
        void onChange3(String uid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        reference = FirebaseFirestore.getInstance().collection("Users")
                .document(uid)
                .collection("Post Images")
                .document(id)
                .collection("Comments");

        loadDataFromFirestore();
        loadCommentData();
        clickListener();

    }


    private void clickListener() {

        sendBtn.setOnClickListener(v -> {

            String comment = commentEt.getText().toString();

            if (comment.isEmpty()) {
                Toast.makeText(context1, context1.getResources().getString(R.string.getComment), Toast.LENGTH_SHORT).show();
                return;
            }




            String commentID = reference.document().getId();

            Map<String, Object> map = new HashMap<>();
            map.put("uid", user.getUid());
            map.put("comment", comment);
            map.put("commentID", commentID);
            map.put("postID", id);
            map.put("timestamp", FieldValue.serverTimestamp());
            map.put("name", usersModel.getName());
            map.put("profileImageUrl", usersModel.getProfileImage());

            reference.document(commentID)
                    .set(map)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            commentEt.setText("");

                        } else {

                            assert  task.getException() != null;
                            Toast.makeText(context1, context1.getResources().getString(R.string.failedComment) + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                        }

                    });

        });

        commentAdapter.OnComClicked(new CommentAdapter.OnComClicked() {
            @Override
            public void onClicked(String uid) {
                onDataPass3.onChange3(uid);
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDataPass3.onChange3(uid);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    String calculateTime(Date date) {
        long millis = date.toInstant().toEpochMilli();
        return DateUtils.getRelativeTimeSpanString(millis, System.currentTimeMillis(), 60000, DateUtils.FORMAT_ABBREV_TIME).toString();
    }

    @SuppressLint("SetTextI18n")
    private void loadCommentData() {

        reference.addSnapshotListener((value, error) -> {

            if (error != null)
                return;

            if (value == null) {
                Toast.makeText(context1, context1.getResources().getString(R.string.noComments), Toast.LENGTH_SHORT).show();
                return;
            }
            list.clear();

            for (DocumentSnapshot snapshot : value) {

                CommentModel model = snapshot.toObject(CommentModel.class);
                list.add(model);
            }
            Collections.sort(list, new Comparator<CommentModel>() {
                public int compare(CommentModel o1, CommentModel o2) {
                    if (o1.getTimestamp() == null || o2.getTimestamp() == null) {
                        return 0;
                    }
                    return o1.getTimestamp().compareTo(o2.getTimestamp());
                }
            });
            commentCountTv.setText(context1.getString(R.string.comments) + value.size());
            commentAdapter.notifyDataSetChanged();

        });

    }

    private void init(View view) {

        commentEt = view.findViewById(R.id.commentET);
        sendBtn = view.findViewById(R.id.sendBtn);
        recyclerView = view.findViewById(R.id.commentRecyclerView);
        commentCountTv = view.findViewById(R.id.commentCount);
        user = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView.setLayoutManager(new LinearLayoutManager(context1));

        list = new ArrayList<>();
        commentAdapter = new CommentAdapter(context1, list);
        recyclerView.setAdapter(commentAdapter);

        if (getArguments() == null)
            return;

        id = getArguments().getString("id");
        uid = getArguments().getString("uid");


        profileImage = view.findViewById(R.id.profileImage);
        imageView = view.findViewById(R.id.imageView);
        userNameTv = view.findViewById(R.id.nameTv);
        timeTv = view.findViewById(R.id.timeTv);
        likeCountTv = view.findViewById(R.id.likeCountTv);
        likeCheckBox = view.findViewById(R.id.likeBtn);
        shareBtn = view.findViewById(R.id.shareBtn);
        descriptionTv = view.findViewById(R.id.descTv);
        deleteButton = view.findViewById(R.id.delete);
        editButton = view.findViewById(R.id.edit);


        descriptionTv.setMovementMethod(new ScrollingMovementMethod());

        descriptionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (descriptionTv.getMaxLines() == 3) {
                    descriptionTv.setMaxLines(30);

                } else {
                    descriptionTv.setMaxLines(3);
                }
            }
        });
    }

    private void loadDataFromFirestore() {
        final DocumentReference reference1 = FirebaseFirestore.getInstance().collection("Users")
                .document(uid)
                .collection("Post Images")
                .document(id);

        registration = reference1.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.d("Error: ", error.getMessage());
                return;
            }

            if (value == null)
            {
                return;
            }


           model1 = value.toObject(HomeModel.class);
            if (model1==null)
            {
                return;
            }
            if(model1.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            {
                deleteButton.setVisibility(View.VISIBLE);
                editButton.setVisibility(View.VISIBLE);
            }
            else
            {
                deleteButton.setVisibility(View.GONE);
                editButton.setVisibility(View.GONE);

            }

            shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String imageUrl = model1.getImageUrl();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, imageUrl);
                    intent.setType("text/*");
                    context1.startActivity(Intent.createChooser(intent, context1.getResources().getString(R.string.Sharelink)));
                }
            });

                    userNameTv.setText(model1.getName());
                     timeTv.setText(calculateTime(model1.getTimestamp()));

                     List<String> likeList = model1.getLikes();
                     int count = likeList.size();

            if (count == 0) {
                likeCountTv.setText(context1.getResources().getString(R.string.zeroLikes));
            } else if (count == 1) {
                likeCountTv.setText(count + context1.getResources().getString(R.string.onelike));
            } else {
                likeCountTv.setText(count + context1.getResources().getString(R.string.moreLikes));
            }

            if (likeList.contains(user.getUid()))
            {
                likeCheckBox.setChecked(true);
            }
            else
            {
                likeCheckBox.setChecked(false);
            }


            assert user != null;
            likeCheckBox.setChecked(likeList.contains(user.getUid()));

            descriptionTv.setText(model1.getDescription());

            Random random = new Random();

            int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

            Glide.with(context1)
                    .load(model1.getProfileImage())
                    .placeholder(R.drawable.ic_person)
                    .timeout(6500)
                    .into(profileImage);

            Glide.with(context1)
                    .load(model1.getImageUrl())
                    .placeholder(new ColorDrawable(color))
                    .timeout(7000)
                    .into(imageView);


            likeCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                            .document(uid)
                            .collection("Post Images")
                            .document(id);

                    if (likeList.contains(user.getUid()) ) {
                        likeList.remove(user.getUid()); // unlike
                    } else {
                        likeList.add(user.getUid()); // like
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("likes", likeList);

                    reference.update(map);
                }
            });

            });


        final DocumentReference reference2 = FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        registration1 = reference2.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value1, @Nullable FirebaseFirestoreException error1) {
                if (error1 != null) {
                    Log.d("Error: ", error1.getMessage());
                    return;
                }

                if (value1 == null)
                {
                    return;
                }

                usersModel = value1.toObject(Users.class);

            }
        });


        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tworzenie obiektu AlertDialog.Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(context1);

                // Ustawianie tytułu
                builder.setTitle(context1.getResources().getString(R.string.editDescription));

                // Ustawianie treści
                builder.setMessage(context1.getResources().getString(R.string.areyousure));


                // Tworzenie obiektu EditText programowo
                final EditText editText = new EditText(context1);
                editText.setHint(context1.getResources().getString(R.string.newText)); // Ustawianie podpowiedzi w polu EditText

                // Dodawanie pola EditText do AlertDialog
                builder.setView(editText);

                builder.setPositiveButton(context1.getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String text = editText.getText().toString().trim();

                        // Sprawdzanie, czy pole EditText nie jest puste
                        if (TextUtils.isEmpty(text)) {
                                text = "";
                        }
                        try {
                            Map<String, Object> mapPostdesc = new HashMap<>();
                            mapPostdesc.put("description", String.valueOf(editText.getText()));

                            DocumentReference referenceDelete = FirebaseFirestore.getInstance().collection("Users").
                                    document(model1.getUid()).collection("Post Images").document(model1.getId());

                            referenceDelete.update(mapPostdesc).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(context1, context1.getResources().getString(R.string.updatedSuccesfull), Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(context1, context1.getResources().getString(R.string.updatedFailed), Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(context1, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        finally {
                            Toast.makeText(context1, context1.getResources().getString(R.string.changedSuccesfull), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                builder.setNegativeButton(context1.getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                // Tworzenie obiektu AlertDialog
                AlertDialog alertDialog = builder.create();

                // Wyświetlanie AlertDialog
                alertDialog.show();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context1);
                builder.setTitle(context1.getResources().getString(R.string.deletingPost)) // Tytuł dialogu
                        .setMessage(context1.getResources().getString(R.string.areyousure)) // Treść dialogu
                        .setPositiveButton(context1.getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DocumentReference referenceDelete = FirebaseFirestore.getInstance().collection("Users").
                                        document(model1.getUid()).collection("Post Images").document(model1.getId());

                                referenceDelete.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(context1, context1.getResources().getString(R.string.deleted), Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(context1.getApplicationContext(), MainActivity.class);
                                                startActivity(intent);
                                                getActivity().finish();
                                            }
                                            else
                                            {
                                                Toast.makeText(context1, context1.getResources().getString(R.string.deletedfailed), Toast.LENGTH_SHORT).show();

                                            }
                                    }
                                });
                            }
                        })
                        .setNegativeButton(context1.getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create()
                        .show();
            }
        });

    }


    @Override
    public void onPause() {
        super.onPause();
        onDestroyView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Usunięcie nasłuchiwania w momencie opuszczania fragmentu
        if (registration != null) {
            registration.remove();
        }
        if (registration1 != null) {
            registration1.remove();
        }
    }

}