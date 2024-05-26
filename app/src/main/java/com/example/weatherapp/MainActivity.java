package com.example.weatherapp;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new WeatherPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        try {
            fetchAllWeatherData(currentCity, isMetric);
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
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
            Toast.makeText(this, "Refreshing weather data...", Toast.LENGTH_SHORT).show();
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
            currentCity = input.getText().toString();
            try {
                fetchAllWeatherData(currentCity, isMetric);
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
        WeatherFetcher.fetchAllWeatherData(cityName, isMetric, new WeatherFetcher.WeatherDataCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    JSONObject latestWeatherData = result.getJSONObject("latestWeather");
                    JSONObject forecastWeatherData = result.getJSONObject("forecastWeather");
                    updateWeatherDataInFragments(latestWeatherData, forecastWeatherData);
                } catch (JSONException e) {
                    Log.d("onSuccess JSONException", e.toString());
                }
            }
            @Override
            public void onFailure(Exception e) {
                Log.d("onFailure Exception", e.toString());
            }
        });
    }

    private void updateWeatherDataInFragments(JSONObject latestWeatherData, JSONObject forecastWeatherData) throws JSONException {
        JSONArray forecastWeatherArray = forecastWeatherData.getJSONArray("list");

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof FragmentBasic) {
                ((FragmentBasic) fragment).updateBasicData(latestWeatherData, isMetric);
            } else if (fragment instanceof FragmentAdditional) {
                ((FragmentAdditional) fragment).updateAdditionalData(latestWeatherData, isMetric);
            } else if (fragment instanceof FragmentForecast) {
                ((FragmentForecast) fragment).updateForecastData(forecastWeatherArray, isMetric);
            }
        }
    }

    private void saveWeatherData(String data) {
    }
}
