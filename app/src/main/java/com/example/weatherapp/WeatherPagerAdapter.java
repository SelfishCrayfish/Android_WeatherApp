package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class WeatherPagerAdapter extends FragmentStateAdapter {
    private Fragment basic;
    private Fragment forecast;
    private Fragment additional;

    public WeatherPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        basic = new FragmentBasic();
        forecast = new FragmentForecast();
        additional = new FragmentAdditional();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return additional;
            case 2:
                return forecast;
            default:
                return basic;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
