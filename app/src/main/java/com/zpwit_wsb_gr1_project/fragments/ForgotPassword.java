package com.zpwit_wsb_gr1_project.fragments;

import static com.zpwit_wsb_gr1_project.fragments.CreateAccountFragment.EMAIL_REGEX;

import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;
import com.zpwit_wsb_gr1_project.FragmentReplacerActivity;
import com.zpwit_wsb_gr1_project.R;


public class ForgotPassword extends Fragment {

    private TextView loginTv;
    private Button recoverBtn;
    private EditText emailEt;

    private FirebaseAuth auth;

    private ProgressBar progressBar;
    Context context1;
    Animation scaleUp, scaleDown;
    AnimationSet s;

    public ForgotPassword() {
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        clickListener();

    }

    private void init(View view){

        loginTv = view.findViewById(R.id.loginTV);
        emailEt = view.findViewById(R.id.emailET);
        recoverBtn = view.findViewById(R.id.recoverBtn);
        progressBar = view.findViewById(R.id.progressBar);
        scaleUp = AnimationUtils.loadAnimation(context1, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(context1, R.anim.scale_down);
        s = new AnimationSet(false);//false means don't share interpolators
        s.addAnimation(scaleDown);
        s.addAnimation(scaleUp);
        auth = FirebaseAuth.getInstance();

    }

    private void clickListener(){

        loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(s);
                ((FragmentReplacerActivity) getActivity()).setFragment(new LoginFragment());
            }
        });

        recoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEt.getText().toString();

                if (email.isEmpty() || !email.matches(EMAIL_REGEX)){
                    emailEt.setError(context1.getResources().getString(R.string.inputValidMail));
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                    Toast.makeText(context1, context1.getResources().getString(R.string.passwordResetSend),
                                            Toast.LENGTH_SHORT).show();
                                    emailEt.setText("");
                                }else {
                                    String errMsg = task.getException().getMessage();
                                    Toast.makeText(context1, "Error: "+errMsg, Toast.LENGTH_SHORT).show();
                                }

                                progressBar.setVisibility(View.GONE);

                            }
                        });


            }
        });

    }

}