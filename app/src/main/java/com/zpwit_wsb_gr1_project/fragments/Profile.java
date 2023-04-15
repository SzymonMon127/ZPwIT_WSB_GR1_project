package com.zpwit_wsb_gr1_project.fragments;

import static com.zpwit_wsb_gr1_project.MainActivity.IS_SEARCHED_USER;
import static com.zpwit_wsb_gr1_project.MainActivity.USER_ID;
import static com.zpwit_wsb_gr1_project.utils.Constants.PREF_DIRECTORY;
import static com.zpwit_wsb_gr1_project.utils.Constants.PREF_NAME;
import static com.zpwit_wsb_gr1_project.utils.Constants.PREF_STORED;
import static com.zpwit_wsb_gr1_project.utils.Constants.PREF_URL;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.marsad.stylishdialogs.StylishAlertDialog;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.zpwit_wsb_gr1_project.FragmentReplacerActivity;
import com.zpwit_wsb_gr1_project.R;
import com.zpwit_wsb_gr1_project.chat.ChatActivity;
import com.zpwit_wsb_gr1_project.model.CommentModel;
import com.zpwit_wsb_gr1_project.model.HomeModel;
import com.zpwit_wsb_gr1_project.model.PostImageModel;
import com.zpwit_wsb_gr1_project.model.Users;
import com.zpwit_wsb_gr1_project.notifications.FCMSend;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineExceptionHandler;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Dispatchers;
import okhttp3.ResponseBody;
import retrofit2.Response;


public class Profile extends Fragment   {
    private TextView nameTv, toolbarNameTv, statusTv, followingCountTv, followersCountTv, postCountTv;
    private CircleImageView profileImage;
    private Button followBtn, startChatBtn;
    private RecyclerView recyclerView;
    private FirebaseUser user;
    private LinearLayout countLayout;
    boolean isMyProfile = true;
    private ImageButton editProfileBtn;
    private String uid;
    String userUID;
    List<String> followersList;
    List<String> followingList;
    List<String> followingList_2;
    boolean isFollowed;
    int count;
    Context context1;
    private  DocumentReference userRef, myRef;
    FirestoreRecyclerAdapter<PostImageModel, PostImageHolder> adapter;
    Animation scaleUp, scaleDown;
    AnimationSet s;
    ImageButton sendButton;
    private String search1, search2;
    private ListenerRegistration registration;
    public Profile() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        context1 = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        init(view);

        myRef = FirebaseFirestore.getInstance().collection("Users").
                document(user.getUid());


        if (IS_SEARCHED_USER) {
            isMyProfile = false;
            userUID = USER_ID;

            loadData();

        } else {
            isMyProfile = true;
            userUID = user.getUid();
        }
        if (isMyProfile) {
            editProfileBtn.setVisibility(View.VISIBLE);
            followBtn.setVisibility(View.GONE);
            countLayout.setVisibility(View.VISIBLE);
            startChatBtn.setVisibility(View.GONE);

        } else {
            followBtn.setVisibility(View.VISIBLE);
          //  countLayout.setVisibility(View.GONE);
            startChatBtn.setVisibility(View.VISIBLE);
            editProfileBtn.setVisibility(View.GONE);
        }


        userRef = FirebaseFirestore.getInstance().collection("Users").document(userUID);

        loadBasicData();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(context1, 3));

        loadPostImages();

        recyclerView.setAdapter(adapter);

        clickListener();

    }

    private void loadData() {
        myRef.addSnapshotListener((value, error) -> {

            if (error != null) {
                Log.e("Tag_b", error.getMessage());
                return;
            }

            if (value == null || !value.exists()) {
                return;
            }

            followingList_2 = (List<String>) value.get("following");


        });
    }

    private void  clickListener()
    {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(s);

                AlertDialog.Builder builder = new AlertDialog.Builder(context1);
                builder.setTitle(context1.getResources().getString(R.string.logout)) // Tytuł dialogu
                        .setMessage(context1.getResources().getString(R.string.areyousure)) // Treść dialogu
                        .setPositiveButton(context1.getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context1, FragmentReplacerActivity.class);
                                FirebaseAuth.getInstance().signOut();
                                startActivity(intent);
                                getActivity().finish();
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


        followBtn.setOnClickListener(v -> {
            v.startAnimation(s);
            if (isFollowed) {

                followersList.remove(user.getUid()); //opposite user

                followingList_2.remove(userUID); //us

                final Map<String, Object> map_2 = new HashMap<>();
                map_2.put("following", followingList_2);


                Map<String, Object> map = new HashMap<>();
                map.put("followers", followersList);


                userRef.update(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followBtn.setText(context1.getResources().getString(R.string.follow));

                        myRef.update(map_2).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(context1,context1.getResources().getString(R.string.unFollowed), Toast.LENGTH_SHORT).show();
                            } else {
                                assert task1.getException() != null;
                                Log.e("Tag_3", task1.getException().getMessage());
                            }
                        });

                    } else {
                        assert task.getException() != null;
                        Log.e("Tag", "" + task.getException().getMessage());
                    }
                });


            } else {

                createNotification();


                followersList.add(user.getUid()); //opposite user

                followingList_2.add(userUID); //us

                final Map<String, Object> map_2 = new HashMap<>();
                map_2.put("following", followingList_2);


                Map<String, Object> map = new HashMap<>();
                map.put("followers", followersList);


                userRef.update(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followBtn.setText(context1.getResources().getString(R.string.unFollow));

                        myRef.update(map_2).addOnCompleteListener(task12 -> {
                            if (task12.isSuccessful()) {
                                Toast.makeText(context1, context1.getResources().getString(R.string.followed), Toast.LENGTH_SHORT).show();
                            } else {
                                assert task12.getException() != null;
                                Log.e("tag_3_1", task12.getException().getMessage());
                            }
                        });


                    } else {
                        assert task.getException() != null;
                        Log.e("Tag", "" + task.getException().getMessage());
                    }
                });


            }

        });




        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showThreeButtonAlertDialog(view);

            }
        });

        startChatBtn.setOnClickListener(v -> {
            queryChat();
        });
    }

    private void showThreeButtonAlertDialog(View view) {
        // Tworzenie obiektu AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context1);

        // Ustawianie tytułu
        builder.setTitle(context1.getResources().getString(R.string.editProfile));

        // Ustawianie treści
        builder.setMessage(context1.getResources().getString(R.string.chooseOne));


        builder.setPositiveButton(context1.getResources().getString(R.string.changingName), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                changeName();
            }
        });

        builder.setNeutralButton(context1.getResources().getString(R.string.newphoto), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                view.startAnimation(s);
                Intent intent = CropImage
                        .activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setFixAspectRatio(true)
                        .setAllowRotation(true)
                        .setAllowCounterRotation(true)
                        .getIntent(context1);

                profileImageresult.launch(intent);
            }
        });

        builder.setNegativeButton(context1.getResources().getString(R.string.description), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                changeDesc();
           }
        });

        // Tworzenie obiektu AlertDialog
        AlertDialog alertDialog = builder.create();

        // Wyświetlanie AlertDialog
        alertDialog.show();
    }

    private void changeDesc() {
        // Tworzenie obiektu AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context1);

        // Ustawianie tytułu
        builder.setTitle(context1.getResources().getString(R.string.editProfile));

        // Ustawianie treści
        builder.setMessage(context1.getResources().getString(R.string.editDescription));


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
                if (!TextUtils.isEmpty(text)) {
                    try {
                        Map<String, Object> map = new HashMap<>();
                        map.put("status", String.valueOf(editText.getText()));

                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(user.getUid())
                                .update(map);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(context1, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    finally {
                        Toast.makeText(context1, context1.getResources().getString(R.string.changedSuccesfull), Toast.LENGTH_SHORT).show();
                    }



                } else {
                    Toast.makeText(context1, context1.getResources().getString(R.string.textCannontBeEmpty), Toast.LENGTH_SHORT).show();
                    changeDesc();
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

    private void changeName() {
        // Tworzenie obiektu AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context1);

        // Ustawianie tytułu
        builder.setTitle(context1.getResources().getString(R.string.editProfile));

        // Ustawianie treści
        builder.setMessage(context1.getResources().getString(R.string.editName));


        // Tworzenie obiektu EditText programowo
        final EditText editText = new EditText(context1);
        editText.setHint(context1.getResources().getString(R.string.newText)); // Ustawianie podpowiedzi w polu EditText

        // Dodawanie pola EditText do AlertDialog
        builder.setView(editText);

        builder.setPositiveButton(context1.getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        String text = editText.getText().toString().trim();

                        // Sprawdzanie, czy pole EditText nie jest puste
                        if (!TextUtils.isEmpty(text)) {
                            Map<String, Object> map = new HashMap<>();
                            String name = String.valueOf(editText.getText());
                            map.put("name", name);
                            map.put("search", name.toLowerCase());
                            FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(user.getUid())
                                    .update(map);

                                changePost(name);
                                changeComments(name);

                        } else {
                            Toast.makeText(context1, context1.getResources().getString(R.string.textCannontBeEmpty), Toast.LENGTH_SHORT).show();
                            changeName();
                        }
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

    private void changeComments(String name2) {

        Map<String, Object> mapComments = new HashMap<>();
        mapComments.put("name", name2);

        final CollectionReference reference = FirebaseFirestore.getInstance().collection("Users");
        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful())
                {
                        return;
                }
                QuerySnapshot querySnapshot = task.getResult();
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();


                for (DocumentSnapshot doc : documents) {
                    Users usersComms = doc.toObject(Users.class);
                    Query query =  FirebaseFirestore.getInstance().collection("Users").document(usersComms.getUid()).collection("Post Images");
                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                            if (!task2.isSuccessful())
                            {
                                return;
                            }
                            QuerySnapshot querySnapshot2 = task2.getResult();

                            List<DocumentSnapshot> documents2 = querySnapshot2.getDocuments();

                            for (DocumentSnapshot doc2 : documents2)
                            {
                                HomeModel homeComms = doc2.toObject(HomeModel.class);
                                Query query2 = FirebaseFirestore.getInstance().collection("Users").document(usersComms.getUid()).collection("Post Images")
                                        .document(homeComms.getId()).collection("Comments");
                                query2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task3) {
                                        if (!task3.isSuccessful())
                                        {
                                            return;
                                        }
                                        QuerySnapshot querySnapshot3 = task3.getResult();

                                        List<DocumentSnapshot> documents3 = querySnapshot3.getDocuments();
                                        for (DocumentSnapshot doc3 : documents3)
                                        {
                                            CommentModel commsComms = doc3.toObject(CommentModel.class);
                                            if (commsComms.getUid().equals(userUID))
                                            {
                                                FirebaseFirestore.getInstance()
                                                        .collection("Users")
                                                        .document(usersComms.getUid())
                                                        .collection("Post Images")
                                                        .document(homeComms.getId()).collection("Comments").document(commsComms.getCommentID()).update(mapComments);
                                            }
                                        }
                                    }
                                });

                            }

                        }
                    });

                }
            }
        });
    }

    private void changePost(String name1) {
        Map<String, Object> mapImages = new HashMap<>();
        mapImages.put("name", name1);


        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(userUID)
                .collection("Post Images");

        reference.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                QuerySnapshot snapshot = task.getResult();

                if (!snapshot.isEmpty())
                {
                    for (DocumentSnapshot snapshotImages : snapshot) {

                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(userUID)
                                .collection("Post Images")
                                .document(snapshotImages.getId()).update(mapImages);
                    }
                }
            }

        });
    }


    void queryChat() {

        assert getContext() != null;
        StylishAlertDialog alertDialog = new StylishAlertDialog(getContext(), StylishAlertDialog.PROGRESS);
        alertDialog.setTitleText("Starting Chat...");
        alertDialog.setCancellable(false);
        alertDialog.show();


        List<String> list = new ArrayList<>();

        list.add(0, user.getUid());
        list.add(1, userUID);
        String id1 = list.get(0)+":"+list.get(1);
        String id2 = list.get(1)+":"+list.get(0);
        List<String> listId = new ArrayList<>();
        listId.add(0, id1);
        listId.add(1, id2);
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");
        reference.whereIn("id", listId)
                .get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                QuerySnapshot snapshot = task.getResult();

                if (snapshot.isEmpty()) {
                    startChat(alertDialog);
                } else {
                    //get chatId and pass
                    alertDialog.dismissWithAnimation();
                    for (DocumentSnapshot snapshotChat : snapshot) {

                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("uid", userUID);
                        intent.putExtra("senderuid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        intent.putExtra("id", snapshotChat.getId()); //return doc id
                        startActivity(intent);
                    }


                }

            } else
                alertDialog.dismissWithAnimation();

        });

    }

    void startChat(StylishAlertDialog alertDialog) {



        getUserSearch(userUID, alertDialog);







    }

    private void getMySearch(String id1, StylishAlertDialog alertDialog) {

        FirebaseFirestore.getInstance().collection("Users").document(id1).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot snapshot = task.getResult();
                search1 = snapshot.getString("search");

                CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");

                List<String> list = new ArrayList<>();

                list.add(0, user.getUid());
                list.add(1, userUID);
                String id = list.get(0)+":"+list.get(1);
                List<String> userSearch = new ArrayList<>();
                userSearch.add(0, search1);
                userSearch.add(1, search2);


                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("lastMessage", "");
                map.put("time", FieldValue.serverTimestamp());
                map.put("uid", list);
                map.put("search", userSearch);

                reference.document(id).update(map).addOnCompleteListener(task1 -> {
                    if (!task1.isSuccessful()) {
                        reference.document(id).set(map);
                    }
                });

                //todo - ---- - -- - -
                //Message
                new Handler().postDelayed(() -> {

                    alertDialog.dismissWithAnimation();

                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("uid", userUID);
                    intent.putExtra("id", id);
                    startActivity(intent);

                }, 3000);
            }
        });
    }

    private void getUserSearch(String id2, StylishAlertDialog alertDialog) {
        FirebaseFirestore.getInstance().collection("Users").document(id2).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot snapshot = task.getResult();
                search2 = snapshot.getString("search");
                getMySearch(user.getUid(), alertDialog);
            }
        });
    }

    ActivityResultLauncher<Intent> profileImageresult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            CropImage.ActivityResult result1 = CropImage.getActivityResult(result.getData());
            if (result1 == null)
                return;
            Uri imageUri = result1.getUri();

            uploadImage(imageUri);
        }
    });

    private void uploadImage(Uri imageUri) {
        StorageReference reference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        reference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageURL = uri.toString();
                            UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();
                            request.setPhotoUri(uri);

                            user.updateProfile(request.build());

                            Map<String, Object> map = new HashMap<>();
                            map.put("profileImage", imageURL);

                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(user.getUid()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                               if (task.isSuccessful())
                               {
                                   Toast.makeText(context1, context1.getResources().getString(R.string.updatedSuccesfull), Toast.LENGTH_SHORT).show();
                               }
                               else
                               {
                                   Toast.makeText(context1, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                               }
                                }
                            });
                        }
                    });
                }
                else
                {
                    Toast.makeText(context1, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadBasicData() {

        userRef.addSnapshotListener((value, error) -> {

            if (error != null) {
                Log.e("Tag_0", error.getMessage());
                return;
            }

            assert value != null;
            if (value.exists()) {

                String name = value.getString("name");
                String status = value.getString("status");

                final String profileURL = value.getString("profileImage");

                nameTv.setText(name);
                toolbarNameTv.setText(name);
                statusTv.setText(status);

                followersList = (List<String>) value.get("followers");
                followingList = (List<String>) value.get("following");


                followersCountTv.setText("" + followersList.size());
                followingCountTv.setText("" + followingList.size());

                try {


                    Glide.with(context1.getApplicationContext())
                            .load(profileURL)
                            .placeholder(R.drawable.ic_person)
                            .circleCrop()
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                    storeProfileImage(bitmap, profileURL);
                                    return false;
                                }
                            })
                            .timeout(6500)
                            .into(profileImage);


                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (followersList.contains(user.getUid())) {
                    followBtn.setText(context1.getResources().getString(R.string.unFollow));
                    isFollowed = true;
                    startChatBtn.setVisibility(View.VISIBLE);


                } else {
                    isFollowed = false;
                    followBtn.setText(context1.getResources().getString(R.string.follow));

                    startChatBtn.setVisibility(View.GONE);

                }


            }

        });

    }

    private void storeProfileImage(Bitmap bitmap, String url) {

        SharedPreferences preferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isStored = preferences.getBoolean(PREF_STORED, false);
        String urlString = preferences.getString(PREF_URL, "");

        SharedPreferences.Editor editor = preferences.edit();

        if (isStored && urlString.equals(url))
            return;

        if (IS_SEARCHED_USER)
            return;

        ContextWrapper contextWrapper = new ContextWrapper(getActivity().getApplicationContext());

        File directory = contextWrapper.getDir("image_data", Context.MODE_PRIVATE);

        if (!directory.exists()) {
            boolean isMade = directory.mkdirs();
            Log.d("Directory", String.valueOf(isMade));
        }


        File path = new File(directory, "profile.png");

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(path);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {

            try {
                assert outputStream != null;
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        editor.putBoolean(PREF_STORED, true);
        editor.putString(PREF_URL, url);
        editor.putString(PREF_DIRECTORY, directory.getAbsolutePath());
        editor.apply();
    }

    private void init(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        assert getActivity() != null;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        nameTv = view.findViewById(R.id.nameTv);
        statusTv = view.findViewById(R.id.statusTV);
        toolbarNameTv = view.findViewById(R.id.toolbarNameTV);
        followersCountTv = view.findViewById(R.id.followersCountTv);
        followingCountTv = view.findViewById(R.id.followingCountTv);
        postCountTv = view.findViewById(R.id.postCountTv);
        profileImage = view.findViewById(R.id.profileImage);
        followBtn = view.findViewById(R.id.followBtn);
        recyclerView = view.findViewById(R.id.recyclerView);
        startChatBtn = view.findViewById(R.id.startChatBtn);
        countLayout = view.findViewById(R.id.countLayout);
        editProfileBtn = view.findViewById(R.id.edit_profileImage);
        sendButton = view.findViewById(R.id.sendBtn);

        scaleUp = AnimationUtils.loadAnimation(context1, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(context1, R.anim.scale_down);
        s = new AnimationSet(false);//false means don't share interpolators
        s.addAnimation(scaleDown);
        s.addAnimation(scaleUp);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    private void loadPostImages() {

            DocumentReference reference = FirebaseFirestore.getInstance()
                    .collection("Users").document(userUID);

        Query query = reference.collection("Post Images").orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<PostImageModel> options = new FirestoreRecyclerOptions.Builder<PostImageModel>()
                .setQuery(query, PostImageModel.class).build();

         adapter = new FirestoreRecyclerAdapter<PostImageModel, PostImageHolder>(options) {
            @NonNull
            @Override
            public PostImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_image_items, parent, false);
                return new PostImageHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PostImageHolder holder, int position, @NonNull PostImageModel model) {

                Random random = new Random();

                int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

                Glide.with(holder.itemView.getContext().getApplicationContext())
                        .load(model.getImageUrl()).placeholder(new ColorDrawable(color))
                        .timeout(6500)
                        .into(holder.imageView);
                count = getItemCount();
                postCountTv.setText("" + count);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context1, FragmentReplacerActivity.class);
                        intent.putExtra("id", model.getId());
                        intent.putExtra("uid", userUID);
                        intent.putExtra("isComment", true);
                        context1.startActivity(intent);
                    }
                });
            }

             @Override
             public int getItemCount() {
                 return super.getItemCount();
             }

        };





        }



    private static class PostImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public PostImageHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);

        }

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
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

                        CollectionReference reference = FirebaseFirestore.getInstance().collection("Notifications");

                        String id = reference.document().getId();
                        Map<String, Object> map = new HashMap<>();
                        map.put("time", FieldValue.serverTimestamp());
                        map.put("notification", users.getName() + " "+context1.getResources().getString(R.string.followedyou));
                        map.put("id", id);
                        map.put("uid", userUID);
                        map.put("idUserFrom", FirebaseAuth.getInstance().getCurrentUser().getUid());


                        reference.document(id).set(map);


                        List<String> userList = new ArrayList<>();
                        userList.add(userUID);
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



                                        FCMSend.pushNotification(context1, users1.getCloudToken(), users.getName(), context1.getResources().getString(R.string.followedyou));
                                    }
                                }
                            }
                        });
                    }

                }
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

    }

}