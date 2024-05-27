package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FragmentBasic extends Fragment {
    private TextView cityNameTextView, temperatureTextView, pressureTextView, descriptionTextView, windSpeedTextView, windDirectionTextView, humidityTextView, visibilityTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("onCreateView", "I have been called");
        View view = inflater.inflate(R.layout.fragment_basic, container, false);
        cityNameTextView = view.findViewById(R.id.cityName);
        temperatureTextView = view.findViewById(R.id.temperature);
        pressureTextView = view.findViewById(R.id.pressure);
        descriptionTextView = view.findViewById(R.id.description);
        windSpeedTextView = view.findViewById(R.id.windSpeed);
        windDirectionTextView = view.findViewById(R.id.windDirection);
        humidityTextView = view.findViewById(R.id.humidity);
        visibilityTextView = view.findViewById(R.id.visibility);
        return view;
    }

    public void updateBasicData(JSONObject weather, boolean isMetric) throws JSONException {
        cityNameTextView.setText(weather.getString("name"));
        Log.d("WeatherLatest", weather.toString());
        double temperature = weather.getJSONObject("main").getDouble("temp");
        char thingy = isMetric ? 'C' : 'F';
        if(!isMetric){
            temperature = ((temperature*9)/5)+32;
        }

        temperatureTextView.setText(String.format("Temperature: %.2f°%c", temperature, thingy));

        int pressure = weather.getJSONObject("main").getInt("pressure");
        pressureTextView.setText(String.format("Pressure: %s hPa", pressure));

        String description = weather.getJSONArray("weather").getJSONObject(0).getString("description");
        descriptionTextView.setText(description);

        double windSpeed = weather.getJSONObject("wind").getDouble("speed");
        String thingyButWindy = isMetric ? "km/h" : "mph";
        if(!isMetric){
            windSpeed = 0.6214 * windSpeed;
        }
        windSpeedTextView.setText(String.format("Wind Speed: %.2f %s", windSpeed, thingyButWindy));

        int windDirection = weather.getJSONObject("wind").getInt("deg");
        windDirectionTextView.setText(String.format("Wind Direction: %s°", windDirection));

        int humidity = weather.getJSONObject("main").getInt("humidity");
        humidityTextView.setText(String.format("Humidity: %s%%", humidity));

        int clouds = weather.getJSONObject("clouds").getInt("all");
        visibilityTextView.setText(String.format("Cloudiness: %s%%", clouds));
    }
}