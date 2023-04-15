package com.zpwit_wsb_gr1_project;

import static android.view.View.GONE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gowtham.library.utils.CompressOption;
import com.gowtham.library.utils.TrimType;
import com.gowtham.library.utils.TrimVideo;
import com.marsad.stylishdialogs.StylishAlertDialog;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class StoryAddActivity extends AppCompatActivity {

    VideoView videoView;
    ImageButton uploadButton;

    FirebaseUser user;
    StylishAlertDialog alertDialog;

    ImageView imageView;


    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK &&
                        result.getData() != null) {


                    Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData()));

                    videoView.setVideoURI(uri);
                    videoView.start();

                    uploadButton.setVisibility(View.VISIBLE);
                    uploadButton.setOnClickListener(v -> {
                        uploadButton.setVisibility(GONE);

                        videoView.pause();

                        uploadFileToStorage(uri, "video");

                    });


                } else {
                    Toast.makeText(this, "Data is null", Toast.LENGTH_SHORT).show();
                    finish();
                }

            });


    ActivityResultLauncher<Intent> activityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            try {
                Intent intent = result.getData();
                assert intent != null;
                Uri uri = intent.getData();

                if (uri.toString().contains("image")) {

                    videoView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);

                    Glide.with(StoryAddActivity.this)
                            .load(uri)
                            .into(imageView);

                    uploadButton.setVisibility(View.VISIBLE);
                    uploadButton.setOnClickListener(v -> {

                        uploadButton.setVisibility(View.GONE);

                        uploadFileToStorage(uri, "image");

                    });

                } else if (uri.toString().contains("video")) {
                    TrimVideo.activity(String.valueOf(uri))
                            .setCompressOption(new CompressOption())
                            .setTrimType(TrimType.MIN_MAX_DURATION)
                            .setMinToMax(5, 30)
                            .setHideSeekBar(true)
                            .start(StoryAddActivity.this, startForResult);

                }

            }
            catch (Exception e)
            {
                Log.e("TAG", "Error: " + e.getMessage());
            }
        }
    });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_add);

        init();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/* video/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, "image/* video/*");
        activityResult.launch(intent);
    }



    void uploadFileToStorage(Uri uri, String type) {

        alertDialog = new StylishAlertDialog(this, StylishAlertDialog.PROGRESS);
        alertDialog.setTitleText("Uploading...")
                .setCancelable(false);

        alertDialog.show();

        String fileName;

        if (type.contains("image")) {
            fileName = System.currentTimeMillis() + ".png";
            uploadImageToStorage(fileName, uri, type);
        } else {
            fileName = System.currentTimeMillis() + ".mp4";
            uploadVideoToStorage(fileName, uri, type);
        }


    }

    void uploadImageToStorage(String fileName, Uri uri, String type) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("Stories/" + fileName);


        storageReference.putFile(uri).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                assert task.getResult() != null;
                task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(uri1 -> {
                            uploadVideoDataToFirestore(String.valueOf(uri1), type);
                        }
                );

            } else {
                alertDialog.dismissWithAnimation();
                assert task.getException() != null;
                String error = task.getException().getMessage();
                Toast.makeText(StoryAddActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }

        });

    }

    void uploadVideoToStorage(String fileName, Uri uri, String type) {

        File file = new File(uri.getPath());


        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("Stories/" + fileName);


        storageReference.putFile(Uri.fromFile(file)).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                assert task.getResult() != null;
                task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(uri1 -> {
                            uploadVideoDataToFirestore(String.valueOf(uri1), type);
                        }
                );

            } else {
                alertDialog.dismissWithAnimation();
                assert task.getException() != null;
                String error = task.getException().getMessage();
                Toast.makeText(StoryAddActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }

        });

    }

    void uploadVideoDataToFirestore(String url, String type) {
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Stories");

        String id = reference.document().getId();

        Map<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("id", id);
        map.put("uid", user.getUid());
        map.put("type", type);
        map.put("name", user.getDisplayName());

        reference.document(id)
                .set(map);

        alertDialog.dismissWithAnimation();

        finish();

    }

    void init() {

        videoView = findViewById(R.id.videoView);
        uploadButton = findViewById(R.id.uploadStoryBtn);
        imageView = findViewById(R.id.imageView);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

    }
}