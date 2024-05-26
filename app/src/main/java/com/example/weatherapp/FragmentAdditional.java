package com.example.weatherapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class FragmentAdditional extends Fragment {
    private TextView windSpeedTextView, windDirectionTextView, humidityTextView, visibilityTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_additional, container, false);
        windSpeedTextView = view.findViewById(R.id.windSpeed);
        windDirectionTextView = view.findViewById(R.id.windDirection);
        humidityTextView = view.findViewById(R.id.humidity);
        visibilityTextView = view.findViewById(R.id.visibility);
        return view;
    }

    public void updateAdditionalData(JSONObject weather, boolean isMetric) throws JSONException {
        double windSpeed = weather.getJSONObject("wind").getDouble("speed");
        windSpeedTextView.setText(String.format("Wind Speed: %s %s", windSpeed, isMetric ? "m/s" : "mph"));

        int windDirection = weather.getJSONObject("wind").getInt("deg");
        windDirectionTextView.setText(String.format("Wind Direction: %s°", windDirection));

        int humidity = weather.getJSONObject("main").getInt("humidity");
        humidityTextView.setText(String.format("Humidity: %s%%", humidity));

        int clouds = weather.getJSONObject("clouds").getInt("all");
        visibilityTextView.setText(String.format("Cloudiness: %s%%", clouds));
    }

}