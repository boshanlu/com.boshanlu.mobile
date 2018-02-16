package com.boshanlu.mobile.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.boshanlu.mobile.fragment.BaseLazyFragment;

import java.util.List;

public class MainPageAdapter extends FragmentStatePagerAdapter {

    private List<BaseLazyFragment> fragments;

    public MainPageAdapter(FragmentManager fm, List<BaseLazyFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
