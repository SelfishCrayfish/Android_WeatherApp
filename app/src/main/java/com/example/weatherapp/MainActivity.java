package com.example.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "weather_prefs";
    private static final String KEY_CURRENT_CITY = "currentCity";
    private static final String KEY_IS_METRIC = "isMetric";
    private ViewPager2 viewPager;
    private WeatherPagerAdapter pagerAdapter;
    private String currentCity = "Lodz";
    private boolean isMetric = true;
    private Handler handler = new Handler();
    private Runnable runnable;
    private static final long REFRESH_INTERVAL = 5 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadPreferences();

        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_basic_container) != null) {
            loadFragmentsForTablet();
        } else {
            viewPager = findViewById(R.id.viewPager);
            pagerAdapter = new WeatherPagerAdapter(this);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setOffscreenPageLimit(pagerAdapter.getItemCount());
        }

        updateWeatherForCity(currentCity);


        startAutoRefresh();
    }

    private void loadFragmentsForTablet() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment basicFragment = new FragmentBasic();
        Fragment forecastFragment = new FragmentForecast();
        Fragment additionalFragment = new FragmentAdditional();

        fragmentTransaction.replace(R.id.fragment_basic_container, basicFragment);
        fragmentTransaction.replace(R.id.fragment_forecast_container, forecastFragment);
        fragmentTransaction.replace(R.id.fragment_additional_container, additionalFragment);

        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeatherForCity(currentCity);
            return true;
        } else if (id == R.id.action_change_location) {
            showChangeLocationDialog();
            return true;
        } else if (id == R.id.action_change_units) {
            try {
                toggleUnits();
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChangeLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Location");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            if (isNetworkAvailable()) {
                currentCity = input.getText().toString();
                updateWeatherForCity(currentCity);
            } else {
                Utils.showToast(this, "Not connected to the internet");
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void toggleUnits() throws JSONException, IOException {
        isMetric = !isMetric;
        updateWeatherForCity(currentCity);
        String unit = isMetric ? "metric" : "imperial";
        Toast.makeText(this, "Units changed to " + unit, Toast.LENGTH_SHORT).show();
    }

    public void updateWeatherForCity(String cityName) {
        if (isNetworkAvailable()) {
            WeatherFetcher.fetchAllWeatherData(cityName, new WeatherFetcher.WeatherDataCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    try {
                        JSONObject latestWeatherData = result.getJSONObject(cityName + "_latestWeather");
                        JSONObject forecastWeatherData = result.getJSONObject(cityName + "_forecastWeather");

                        saveWeatherDataToPreferences(cityName + "_latestWeather", latestWeatherData.toString());
                        saveWeatherDataToPreferences(cityName + "_forecastWeather", forecastWeatherData.toString());

                        new Handler().postDelayed(() -> {
                            try {
                                currentCity = cityName;
                                updateWeatherDataInFragments(latestWeatherData, forecastWeatherData);
                            } catch (JSONException | IOException e) {
                                Log.d("fetchAllWeatherData JSONException", e.toString());
                            }
                        }, 50);
                    } catch (JSONException e) {
                        Log.d("onSuccess JSONException", e.toString());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d("onFailure Exception", e.toString());
                }
            });
        } else {

            String latestWeatherString = getWeatherDataFromPreferences(cityName + "_latestWeather");
            String forecastWeatherString = getWeatherDataFromPreferences(cityName + "_forecastWeather");

            if (latestWeatherString != null && forecastWeatherString != null) {
                Toast.makeText(this, "No internet connection. Using cached data.", Toast.LENGTH_SHORT).show();
                try {
                    JSONObject latestWeatherData = new JSONObject(latestWeatherString);
                    JSONObject forecastWeatherData = new JSONObject(forecastWeatherString);

                    new Handler().postDelayed(() -> {
                        try {
                            currentCity = cityName;
                            updateWeatherDataInFragments(latestWeatherData, forecastWeatherData);
                        } catch (JSONException | IOException e) {
                            Log.d("updateWeatherForCity JSONException", e.toString());
                        }
                    }, 50);
                } catch (JSONException e) {
                    Log.d("updateWeatherForCity JSONException", e.toString());
                }
            } else {
                Toast.makeText(this, "No cached data available.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateWeatherDataInFragments(JSONObject latestWeatherData, JSONObject forecastWeatherData) throws JSONException, IOException {
        JSONArray forecastWeatherArray = forecastWeatherData.getJSONArray("list");

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof FragmentBasic) {
                ((FragmentBasic) fragment).updateBasicData(latestWeatherData, isMetric);
            } else if (fragment instanceof FragmentForecast) {
                ((FragmentForecast) fragment).updateForecastData(forecastWeatherArray, isMetric);
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String getWeatherDataFromPreferences(String key) {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(key, null);
    }

    private void saveWeatherDataToPreferences(String key, String jsonData) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(key, jsonData)
                .apply();
    }

    private void startAutoRefresh() {
        runnable = new Runnable() {
            @Override
            public void run() {
                updateWeatherForCity(currentCity);
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
        handler.postDelayed(runnable, REFRESH_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private void loadPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentCity = preferences.getString(KEY_CURRENT_CITY, currentCity);
        isMetric = preferences.getBoolean(KEY_IS_METRIC, isMetric);
    }

    private void savePreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_CURRENT_CITY, currentCity);
        editor.putBoolean(KEY_IS_METRIC, isMetric);
        editor.apply();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        savePreferences();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loadPreferences();
        updateWeatherForCity(currentCity);
    }
}