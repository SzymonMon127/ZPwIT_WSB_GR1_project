package com.zpwit_wsb_gr1_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.zpwit_wsb_gr1_project.fragments.Comment;
import com.zpwit_wsb_gr1_project.fragments.CreateAccountFragment;
import com.zpwit_wsb_gr1_project.fragments.LoginFragment;

public class FragmentReplacerActivity extends AppCompatActivity implements Comment.OnDataPass3 {

    private FrameLayout frameLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_replacer);

        frameLayout = findViewById(R.id.frameLayout);

        setFragment( new LoginFragment());

        boolean isComment = getIntent().getBooleanExtra("isComment", false);

        if (isComment)
            setFragment(new Comment());
        else
            setFragment(new LoginFragment());
    }

    public  void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        if (fragment instanceof CreateAccountFragment) {
            fragmentTransaction.addToBackStack(null);
        }

        if (fragment instanceof Comment){
            String id = getIntent().getStringExtra("id");
            String uid = getIntent().getStringExtra("uid");

            Bundle bundle = new Bundle();
            bundle.putString("id", id);
            bundle.putString("uid", uid);
            fragment.setArguments(bundle);
        }

        fragmentTransaction.replace(frameLayout.getId(), fragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onChange3(String uid3) {
        String USER_ID = uid3;
        boolean IS_SEARCHED_USER = false;
        if (!USER_ID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
        {
            IS_SEARCHED_USER = true;
        }
        else
        {
            IS_SEARCHED_USER = false;
        }
        Intent intent = new Intent(FragmentReplacerActivity.this, MainActivity.class);
        intent.putExtra("uid", USER_ID);
        intent.putExtra("user", IS_SEARCHED_USER);
        intent.putExtra("data", 4);
        startActivity(intent);
        this.finish();
    }
}