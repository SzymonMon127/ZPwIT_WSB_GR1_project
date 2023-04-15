package com.zpwit_wsb_gr1_project.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.zpwit_wsb_gr1_project.FragmentReplacerActivity;
import com.zpwit_wsb_gr1_project.MainActivity;
import com.zpwit_wsb_gr1_project.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CreateAccountFragment extends Fragment {

    public static final String EMAIL_REGEX = "^(.+)@(.+)$";
    private EditText nameEt, emailEt, passwordEt, confirmPasswordEt;
    private ProgressBar progressBar;
    private TextView loginTv;
    private Button signUpBtn;
    private FirebaseAuth auth;
    Animation scaleUp, scaleDown;
    AnimationSet s;
    Context context1;
    public CreateAccountFragment() {
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
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        clickListener();

    }

    private void clickListener() {
        loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(s);
                ((FragmentReplacerActivity) getActivity()).setFragment(new LoginFragment());
            }
        });


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(s);
                String name = nameEt.getText().toString();
                String email = emailEt.getText().toString();
                String password = passwordEt.getText().toString();
                String confirmPassword = confirmPasswordEt.getText().toString();

                if (name.isEmpty() || name.equals(" ")) {
                    nameEt.setError(context1.getResources().getString(R.string.inputValidName));
                    return;
                }

                if (email.isEmpty() || !email.matches(EMAIL_REGEX)) {
                    emailEt.setError(context1.getResources().getString(R.string.inputValidMail));
                    return;
                }

                if (password.isEmpty() || password.length() < 6) {
                    passwordEt.setError(context1.getResources().getString(R.string.inputValidPassword));
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    confirmPasswordEt.setError(context1.getResources().getString(R.string.passwordNotMatch));
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                createAccount(name, email, password);


            }
        });

    }

    private void createAccount(final String name, final String email, String password) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            FirebaseUser user = auth.getCurrentUser();

                            String image = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRwp--EwtYaxkfsSPIpoSPucdbxAo6PancQX1gw6ETSKI6_pGNCZY4ts1N6BV5ZcN3wPbA&usqp=CAU";


                            UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();
                            request.setDisplayName(name);
                            request.setPhotoUri(Uri.parse(image));
                            user.updateProfile(request.build());

                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(context1, context1.getResources().getString(R.string.emailverSend), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            uploadUser(user, name, email);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            String exception = task.getException().getMessage();
                            Toast.makeText(context1, "Error: " + exception, Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }


    private void uploadUser(FirebaseUser user, String name, String email) {

        List<String> list = new ArrayList<>();
        List<String> list1 = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();



        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful())
                {
                    return;
                }

                String token = task.getResult();

                map.put("name", name);
                map.put("email", email);
                map.put("profileImage", " ");
                map.put("uid", user.getUid());


                map.put("status", " ");
                map.put("search", name.toLowerCase());
                map.put("followers", list);
                map.put("following", list1);
                map.put("cloudToken", token);

                FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                        .set(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    assert getActivity() != null;
                                    progressBar.setVisibility(View.GONE);
                                    startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                                    getActivity().finish();

                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(context1, "Error: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        });



    }

    private void init(View view) {

        nameEt = view.findViewById(R.id.nameET);
        emailEt = view.findViewById(R.id.emailET);
        passwordEt = view.findViewById(R.id.passwordET);
        confirmPasswordEt = view.findViewById(R.id.confirmPassET);
        loginTv = view.findViewById(R.id.loginTV);
        signUpBtn = view.findViewById(R.id.signUpBtn);
        progressBar = view.findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        scaleUp = AnimationUtils.loadAnimation(context1, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(context1, R.anim.scale_down);
        s = new AnimationSet(false);//false means don't share interpolators
        s.addAnimation(scaleDown);
        s.addAnimation(scaleUp);
    }


}