package com.example.weatherapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FragmentForecast extends Fragment {
    private LinearLayout forecastLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast, container, false);
        forecastLayout = view.findViewById(R.id.forecastLayout);
        return view;
    }

    public void updateForecastData(JSONArray weather, boolean isMetric) throws JSONException {
        forecastLayout.removeAllViews();

        for (int i = 0; i < weather.length(); i++) {
            JSONObject dayForecast = weather.getJSONObject(i);

            if(dayForecast.getString("dt_txt").contains("12:00:00")) {
                View forecastItem = LayoutInflater.from(getContext()).inflate(R.layout.item_forecast, forecastLayout, false);

                TextView dateTextView = forecastItem.findViewById(R.id.date);
                TextView tempTextView = forecastItem.findViewById(R.id.temperature);
                TextView descriptionTextView = forecastItem.findViewById(R.id.description);

                String date = dayForecast.getString("dt_txt").replaceAll("12:00:00", "");
                dateTextView.setText(String.format("%s", date));

                char unit = isMetric ? 'C' : 'F';
                double temperature = dayForecast.getJSONObject("main").getDouble("temp");
                tempTextView.setText(String.format("%s°%c", temperature, unit));

                String description = dayForecast.getJSONArray("weather").getJSONObject(0).getString("description");
                descriptionTextView.setText(description);

                forecastLayout.addView(forecastItem);
            }
        }
    }
}