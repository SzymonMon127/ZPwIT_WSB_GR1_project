package com.zpwit_wsb_gr1_project;

import static com.zpwit_wsb_gr1_project.utils.Constants.PREF_DIRECTORY;
import static com.zpwit_wsb_gr1_project.utils.Constants.PREF_NAME;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.zpwit_wsb_gr1_project.adapter.ViewPagerAdapter;
import com.zpwit_wsb_gr1_project.fragments.Comment;
import com.zpwit_wsb_gr1_project.fragments.Home;
import com.zpwit_wsb_gr1_project.fragments.Notification;
import com.zpwit_wsb_gr1_project.fragments.Profile;
import com.zpwit_wsb_gr1_project.fragments.Search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements Search.OnDataPass, Notification.OnDataPass1, Home.OnDataPass2 {

    private TabLayout tabLayout;
    private ViewPager2 viewPage2;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public static String USER_ID;
    public static boolean IS_SEARCHED_USER = false;
    private int tabOpenInt=0;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init();
        
        addTabs();

        Intent intent = getIntent();
        if (intent != null) {
            tabOpenInt = intent.getIntExtra("data", 0);
            if (tabOpenInt==4)
            {
                USER_ID = intent.getStringExtra("uid");
                IS_SEARCHED_USER = intent.getBooleanExtra("user", true);
            }
            tabLayout.selectTab(tabLayout.getTabAt(tabOpenInt));
            viewPage2.setCurrentItem(tabOpenInt);

        }


    }




    private void addTabs() {

        List<Integer> drawableResList = new ArrayList<>();
        drawableResList.add(R.drawable.ic_home_fill);
        drawableResList.add(R.drawable.ic_search);
        drawableResList.add(R.drawable.ic_add);
        drawableResList.add(R.drawable.ic_notification);
        drawableResList.add(R.drawable.ic_profile);



        for (int i = 0; i < 5; i++) {
            tabLayout.addTab(tabLayout.newTab().setIcon(drawableResList.get(i)));
        }




        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);


        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPage2.setAdapter(viewPagerAdapter);
        viewPage2.setUserInputEnabled(false);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPage2.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {

                    case 0:
                        tab.setIcon(R.drawable.ic_home_fill);
                        IS_SEARCHED_USER = false;
                        USER_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        break;

                    case 1:
                        tab.setIcon(R.drawable.ic_search_fill);
                        IS_SEARCHED_USER = false;
                        USER_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        break;

                    case 2:
                        tab.setIcon(R.drawable.ic_add_fill);
                        IS_SEARCHED_USER = false;
                        USER_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        break;

                    case 3:
                        tab.setIcon(R.drawable.notification_fill);
                        IS_SEARCHED_USER = false;
                        USER_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        break;
                    case 4:
                        tab.setIcon(R.drawable.ic_profile_fill);
                        // Reset the Profile fragment
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + 4);
                        if (fragment != null && fragment instanceof Profile) {
                            getSupportFragmentManager().beginTransaction().detach(fragment).commitNow();
                            getSupportFragmentManager().beginTransaction().attach(fragment).commitNow();
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {

                    case 0:
                        tab.setIcon(R.drawable.ic_home);
                        break;

                    case 1:
                        tab.setIcon(R.drawable.ic_search);
                        break;

                    case 2:
                        tab.setIcon(R.drawable.ic_add);
                        break;

                    case 3:
                        tab.setIcon(R.drawable.ic_notification);
                        break;
                    case 4:
                        tab.setIcon(R.drawable.ic_profile);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {

                    case 0:
                        tab.setIcon(R.drawable.ic_home_fill);
                        break;

                    case 1:
                        tab.setIcon(R.drawable.ic_search_fill);
                        break;

                    case 2:
                        tab.setIcon(R.drawable.ic_add_fill);
                        break;

                    case 3:
                        tab.setIcon(R.drawable.notification_fill);
                        break;
                    case 4:
                        tab.setIcon(R.drawable.ic_profile_fill);
                        break;

                }
            }
        });

    }


    private void init() {

        viewPage2 = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);


    }




    @Override
    public void onChange(String uid) {
        USER_ID = uid;

        if (!USER_ID.equals(user.getUid()))
        {
            IS_SEARCHED_USER = true;
        }
        else
        {
            IS_SEARCHED_USER = false;
        }
        tabLayout.selectTab(tabLayout.getTabAt(4));
        viewPage2.setCurrentItem(4);
    }

    @Override
    public void onChange1(String uid1) {
        USER_ID = uid1;
        if (!USER_ID.equals(user.getUid()))
        {
            IS_SEARCHED_USER = true;
        }
        else
        {
            IS_SEARCHED_USER = false;
        }
        tabLayout.selectTab(tabLayout.getTabAt(4));
        viewPage2.setCurrentItem(4);
    }

    @Override
    public void onChange2(String uid2) {
        USER_ID = uid2;
        if (!USER_ID.equals(user.getUid()))
        {
            IS_SEARCHED_USER = true;
        }
        else
        {
            IS_SEARCHED_USER = false;
        }
        tabLayout.selectTab(tabLayout.getTabAt(4));
        viewPage2.setCurrentItem(4);
    }


    @Override
    public void onBackPressed() {

        if (viewPage2.getCurrentItem() >=0 && viewPage2.getCurrentItem() <=4) {
            tabLayout.selectTab(tabLayout.getTabAt(0));
            viewPage2.setCurrentItem(0);
            IS_SEARCHED_USER = false;

        } else
        {
            super.onBackPressed();

        }

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
}