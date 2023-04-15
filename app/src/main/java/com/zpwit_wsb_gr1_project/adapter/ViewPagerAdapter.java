package com.zpwit_wsb_gr1_project.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.zpwit_wsb_gr1_project.fragments.Add;
import com.zpwit_wsb_gr1_project.fragments.Home;
import com.zpwit_wsb_gr1_project.fragments.Notification;
import com.zpwit_wsb_gr1_project.fragments.Profile;
import com.zpwit_wsb_gr1_project.fragments.Search;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private String[] titles = new String[]{"Home", "Search", "Add","Notifications", "Profile"};

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){

            default:
            case 0:
                return new Home();

            case 1:
                return new Search();

            case 2:
                return new Add();

            case 3:
                return new Notification();

            case 4:
                return new Profile();
        }

    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
