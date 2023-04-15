package com.zpwit_wsb_gr1_project.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.zpwit_wsb_gr1_project.R;
import com.zpwit_wsb_gr1_project.adapter.GalleryAdapter;
import com.zpwit_wsb_gr1_project.model.GalleryImages;
import com.zpwit_wsb_gr1_project.model.Users;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Add extends Fragment {

    Uri imageUri;

    Dialog dialog;

    private EditText descET;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ImageButton  nextBtn;
    private FirebaseUser user;
    private GalleryAdapter adapter;
    private List<GalleryImages> list;
    private LinearLayout linearView;
    Context context1;
    Animation scaleUp, scaleDown;
    AnimationSet s;
    private Users usersModel;
    private ListenerRegistration registration;

    public Add() {
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
        return inflater.inflate(R.layout.fragment_add, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setHasFixedSize(true);

        list = new ArrayList<>();
        adapter = new GalleryAdapter(list);

        recyclerView.setAdapter(adapter);

        clickListener();

    }

    private void clickListener() {
        adapter.SendImage(new GalleryAdapter.SendImage() {
            @Override
            public void onSend(Uri picUri) {




                Intent intent = CropImage
                        .activity(picUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(4, 3)
                        .getIntent(context1);



                cropActivityResult.launch(intent);


            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(s);

                FirebaseStorage storage = FirebaseStorage.getInstance();
                final StorageReference storageReference = storage.getReference().child("Post Images/" + System.currentTimeMillis());

                dialog.show();

                storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    uploadData(uri.toString());
                                }
                            });
                        }
                        else
                        {
                            dialog.dismiss();
                            Toast.makeText(context1,context1.getResources().getString(R.string.failedUploadPost), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    ActivityResultLauncher<Intent> cropActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            try {
                Intent intent = result.getData();

                CropImage.ActivityResult result1 = CropImage.getActivityResult(intent);
                imageUri = result1.getUri();

                Glide.with(context1).load(imageUri).into(imageView);
                linearView.setVisibility(View.VISIBLE);
                nextBtn.setVisibility(View.VISIBLE);

            }
            catch (Exception e)
            {
                Log.e("TAG", "Error: " + e.getMessage());
            }
        }
    });

    private void uploadData(String imageURL) {


        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid()).collection("Post Images");


        List<String> list = new ArrayList<>();



        String description = String.valueOf(descET.getText());
        String id = reference.document().getId();;

        Map<String, Object> map = new HashMap<>();
        map.put("description", description);
        map.put("id", id);
        map.put("imageUrl", imageURL);
        map.put("timestamp", FieldValue.serverTimestamp());

        map.put("name", usersModel.getName());
        map.put("profileImage", usersModel.getProfileImage());
        map.put("likes", list);
        map.put("uid", user.getUid());




        reference.document(id).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(context1,context1.getResources().getString(R.string.uploadedPost), Toast.LENGTH_SHORT).show();
                    System.out.println();
                    imageUri = null;
                    descET.setText("");
                    nextBtn.setVisibility(View.GONE);
                    linearView.setVisibility(View.GONE);

                }
                else
                {
                    Toast.makeText(context1, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
            Dexter.withContext(context1)
                    .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {
                    if (report.areAllPermissionsGranted())
                    {

                        File file = new File(Environment.getExternalStorageDirectory().toString()+"/Download");
                        if (file.exists())
                        {

                            File[] files = file.listFiles();
                            assert files != null;

                            list.clear();
                            for (File file1 : files)
                            {
                                if (file1.getAbsolutePath().endsWith(".jpg") || file1.getAbsolutePath().endsWith(".png"))
                                {
                                    list.add(new GalleryImages(Uri.fromFile(file1)));
                                    adapter.notifyDataSetChanged();

                                }
                            }
                        }

                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                }
            }).check();
        }
    });
    }

    private void init(View view) {

        descET = view.findViewById(R.id.descriptionET);
        imageView = view.findViewById(R.id.imageView);
        recyclerView = view.findViewById(R.id.recyclerView);
        nextBtn = view.findViewById(R.id.nextBtn);
        linearView = view.findViewById(R.id.LinearImage);
        user = FirebaseAuth.getInstance().getCurrentUser();

        scaleUp = AnimationUtils.loadAnimation(context1, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(context1, R.anim.scale_down);
        s = new AnimationSet(false);//false means don't share interpolators
        s.addAnimation(scaleDown);
        s.addAnimation(scaleUp);

        dialog = new Dialog(context1);
        dialog.setContentView(R.layout.laoding_dialog);
        dialog.getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(context1.getResources(), R.drawable.dialog_bg, null));
        dialog.setCancelable(false);



        final DocumentReference reference2 = FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        registration = reference2.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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