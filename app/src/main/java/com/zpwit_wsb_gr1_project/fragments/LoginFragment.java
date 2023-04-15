package com.zpwit_wsb_gr1_project.fragments;

import static com.zpwit_wsb_gr1_project.fragments.CreateAccountFragment.EMAIL_REGEX;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.zpwit_wsb_gr1_project.FragmentReplacerActivity;
import com.zpwit_wsb_gr1_project.MainActivity;
import com.zpwit_wsb_gr1_project.R;
import com.zpwit_wsb_gr1_project.ViewStoryActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoginFragment extends Fragment {


    private EditText emailEt, passwordEt;
    private TextView signUpTv, forgotPasswordTv;
    private Button loginBtn, googleSignInBtn;
    private ProgressBar progressBar;
    View parentLayout ;
    Animation scaleUp, scaleDown;
    AnimationSet s;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth auth;
    Context context1;
    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        context1 = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        parentLayout = view;
        clickListener();

    }

    private void clickListener() {

        forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(s);
                ((FragmentReplacerActivity) getActivity()).setFragment(new ForgotPassword());
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(s);
                String email = emailEt.getText().toString();
                String password = passwordEt.getText().toString();

                if (email.isEmpty() || !email.matches(EMAIL_REGEX)) {
                    emailEt.setError(context1.getResources().getString(R.string.inputValidMail));
                    return;
                }

                if (password.isEmpty() || password.length() < 6) {
                    passwordEt.setError(context1.getResources().getString(R.string.inputValidPassword));
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    FirebaseUser user = auth.getCurrentUser();

                                    Map<String, Object> map = new HashMap<>();

                                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {
                                            if (!task.isSuccessful()) {
                                                return;
                                            }
                                            String token = task.getResult();
                                            map.put("cloudToken", token);
                                            FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                                                    .update(map)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                assert getActivity() != null;
                                                                progressBar.setVisibility(View.GONE);
                                                                sendUserToMainActivity();

                                                                if (!user.isEmailVerified()) {
                                                                    Toast.makeText(context1, context1.getResources().getString(R.string.pleaseVerifyEmail), Toast.LENGTH_SHORT).show();
                                                                }
                                                                sendUserToMainActivity();
                                                            } else {
                                                                progressBar.setVisibility(View.GONE);
                                                                Toast.makeText(context1, "Error: " + task.getException().getMessage(),
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    });

                                } else {
                                    String exception = "Error: " + task.getException().getMessage();
                                    Toast.makeText(context1, exception, Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }

                            }
                        });

            }
        });

        signUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(s);
                ((FragmentReplacerActivity) getActivity()).setFragment(new CreateAccountFragment());
            }
        });

        googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(s);
                loginResultHandler.launch(new Intent(mGoogleSignInClient.getSignInIntent()));
            }
        });

    }
    private void sendUserToMainActivity() {

        if (getActivity() == null)
            return;

        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
        getActivity().finish();

    }

    private void init(View view) {

        emailEt = view.findViewById(R.id.emailET);
        passwordEt = view.findViewById(R.id.passwordET);
        loginBtn = view.findViewById(R.id.loginBtn);
        googleSignInBtn = view.findViewById(R.id.googleSignInBtn);
        signUpTv = view.findViewById(R.id.signUpTV);
        forgotPasswordTv = view.findViewById(R.id.forgotTV);
        progressBar = view.findViewById(R.id.progressBar);

        scaleUp = AnimationUtils.loadAnimation(context1, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(context1, R.anim.scale_down);
        s = new AnimationSet(false);//false means don't share interpolators
        s.addAnimation(scaleDown);
        s.addAnimation(scaleUp);

        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

    }
     ActivityResultLauncher<Intent> loginResultHandler = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
         @Override
         public void onActivityResult(ActivityResult result) {
             Intent intent = result.getData();
             Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
             try {
                 GoogleSignInAccount account = task.getResult(ApiException.class);
                 assert account != null;
                 firebaseAuthWithGoogle(account.getIdToken());

             } catch (ApiException e) {
                 e.printStackTrace();
                 Snackbar snackbar =  Snackbar.make(parentLayout, context1.getResources().getString(R.string.failedLoginGoogle), 2000);
                 snackbar.setTextMaxLines(10);
                 snackbar.show();

             }
         }
     });





    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();
                            updateUi(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Snackbar snackbar =  Snackbar.make(parentLayout, context1.getResources().getString(R.string.failedLoginGoogle), 2000);
                            snackbar.setTextMaxLines(10);
                            snackbar.show();
                        }

                    }
                });

    }

    private void updateUi(FirebaseUser user) {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());

        Map<String, Object> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        List<String> list1 = new ArrayList<>();

        List<String> userList = new ArrayList<>();
        userList.add(user.getUid());
        Query query = FirebaseFirestore.getInstance().collection("Users");
        query.whereIn("uid", userList).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    return;
                                }
                                String token = task.getResult();
                                map.put("name", account.getDisplayName());
                                map.put("email", account.getEmail());
                                map.put("profileImage", String.valueOf(account.getPhotoUrl()));
                                map.put("uid", user.getUid());
                                map.put("status", " ");
                                map.put("search", account.getDisplayName().toLowerCase());
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
                                                    sendUserToMainActivity();
                                                } else {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(context1, "Error: " + task.getException().getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        });
                    } else {
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    return;
                                }
                                String token = task.getResult();
                                map.put("cloudToken", token);
                                FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                                        .update(map)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    assert getActivity() != null;
                                                    progressBar.setVisibility(View.GONE);
                                                    sendUserToMainActivity();
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
                } else {
                    Log.d("Error: ", task.getException().getMessage());
                }
            }
        });
    }


}