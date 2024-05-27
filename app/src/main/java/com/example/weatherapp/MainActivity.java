package com.example.weatherapp;

import android.content.Context;
import android.content.res.Configuration;
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
import androidx.viewpager2.widget.ViewPager2;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

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
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new WeatherPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(pagerAdapter.getItemCount());

        try {
            fetchAllWeatherData(currentCity, isMetric);
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }


        startAutoRefresh();
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
            try {
                fetchAllWeatherData(currentCity, isMetric);
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
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
            try {
                if(isNetworkAvailable()){
                    currentCity = input.getText().toString();
                    fetchAllWeatherData(currentCity, isMetric);
                }
                else{
                    Utils.showToast(this, "Not connected to the internet");
                }
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void toggleUnits() throws JSONException, IOException {
        isMetric = !isMetric;
        fetchAllWeatherData(currentCity, isMetric);
        String unit = isMetric ? "metric" : "imperial";
        Toast.makeText(this, "Units changed to " + unit, Toast.LENGTH_SHORT).show();
    }

    private void fetchAllWeatherData(String cityName, boolean isMetric) throws IOException, JSONException {
        if (isNetworkAvailable()) {
            WeatherFetcher.fetchAllWeatherData(cityName, isMetric, new WeatherFetcher.WeatherDataCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    try {
                        JSONObject latestWeatherData = result.getJSONObject("latestWeather");
                        JSONObject forecastWeatherData = result.getJSONObject("forecastWeather");

                        saveWeatherDataToPreferences("latestWeather", latestWeatherData.toString());
                        saveWeatherDataToPreferences("forecastWeather", forecastWeatherData.toString());

                        new Handler().postDelayed(() -> {
                            try {
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
            Toast.makeText(this, "No internet connection. Using last updated data.", Toast.LENGTH_SHORT).show();

            String latestWeatherString = getWeatherDataFromPreferences("latestWeather");
            String forecastWeatherString = getWeatherDataFromPreferences("forecastWeather");

            if (latestWeatherString != null && forecastWeatherString != null) {
                try {
                    JSONObject latestWeatherData = new JSONObject(latestWeatherString);
                    JSONObject forecastWeatherData = new JSONObject(forecastWeatherString);

                    new Handler().postDelayed(() -> {
                        try {
                            updateWeatherDataInFragments(latestWeatherData, forecastWeatherData);
                        } catch (JSONException | IOException e) {
                            Log.d("fetchAllWeatherData JSONException", e.toString());
                        }
                    }, 50);
                } catch (JSONException e) {
                    Log.d("fetchAllWeatherData JSONException", e.toString());
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
        return getSharedPreferences("weather_prefs", MODE_PRIVATE)
                .getString(key, null);
    }

    private void saveWeatherDataToPreferences(String key, String jsonData) {
        getSharedPreferences("weather_prefs", MODE_PRIVATE)
                .edit()
                .putString(key, jsonData)
                .apply();
    }

    private void startAutoRefresh() {
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    fetchAllWeatherData(currentCity, isMetric);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
        handler.postDelayed(runnable, REFRESH_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        Log.d("onDestroy", "I have been called");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("isMetric", isMetric);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isMetric = savedInstanceState.getBoolean("isMetric");

    }
}