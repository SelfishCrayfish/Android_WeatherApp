package com.example.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CityUtils {

    private static final String PREFS_NAME = "favorite_cities";
    private static final String WEATHER_PREFS = "weather_prefs";
    private static final String KEY_CITIES = "cities";

    public static List<String> getCities(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> citySet = prefs.getStringSet(KEY_CITIES, new HashSet<>());
        return new ArrayList<>(citySet);
    }

    public static void addCity(Context context, String city) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> citySet = new HashSet<>(prefs.getStringSet(KEY_CITIES, new HashSet<>()));
        citySet.add(city);
        prefs.edit().putStringSet(KEY_CITIES, citySet).apply();
    }

    public static void removeCity(Context context, String city) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> citySet = new HashSet<>(prefs.getStringSet(KEY_CITIES, new HashSet<>()));
        citySet.remove(city);
        prefs.edit().putStringSet(KEY_CITIES, citySet).apply();
        removeWeatherDataForCity(context, city);
    }

    private static void removeWeatherDataForCity(Context context, String city) {
        SharedPreferences weatherPrefs = context.getSharedPreferences(WEATHER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = weatherPrefs.edit();
        editor.remove(city + "_latestWeather");
        editor.remove(city + "_forecastWeather");
        editor.apply();
    }

}
