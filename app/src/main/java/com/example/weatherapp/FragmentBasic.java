package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FragmentBasic extends Fragment {
    private TextView cityNameTextView, temperatureTextView, pressureTextView, descriptionTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basic, container, false);
        cityNameTextView = view.findViewById(R.id.cityName);
        temperatureTextView = view.findViewById(R.id.temperature);
        pressureTextView = view.findViewById(R.id.pressure);
        descriptionTextView = view.findViewById(R.id.description);
        return view;
    }

    public void updateBasicData(JSONObject weather, boolean isMetric) throws JSONException {
        cityNameTextView.setText(weather.getString("name"));
        Log.d("WeatherLatest", weather.toString());
        double temperature = weather.getJSONObject("main").getDouble("temp");
        temperatureTextView.setText(String.format("Temperature: %s°%c", temperature, isMetric ? 'C' : 'F'));

        int pressure = weather.getJSONObject("main").getInt("pressure");
        pressureTextView.setText(String.format("Pressure: %s hPa", pressure));

        String description = weather.getJSONArray("weather").getJSONObject(0).getString("description");
        descriptionTextView.setText(description);
    }

}