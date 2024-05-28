package com.example.weatherapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class FragmentBasic extends Fragment {
    private TextView cityNameTextView, temperatureTextView, pressureTextView, windSpeedTextView, windDirectionTextView, humidityTextView, visibilityTextView;
    private ImageView weatherImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basic, container, false);
        cityNameTextView = view.findViewById(R.id.cityName);
        temperatureTextView = view.findViewById(R.id.temperature);
        pressureTextView = view.findViewById(R.id.pressure);
        windSpeedTextView = view.findViewById(R.id.windSpeed);
        windDirectionTextView = view.findViewById(R.id.windDirection);
        humidityTextView = view.findViewById(R.id.humidity);
        visibilityTextView = view.findViewById(R.id.visibility);
        weatherImage = view.findViewById(R.id.weatherImage);
        return view;
    }

    public void updateBasicData(JSONObject weather, boolean isMetric) throws JSONException {
        cityNameTextView.setText(weather.getString("name"));
        double temperature = weather.getJSONObject("main").getDouble("temp");
        char thingy = isMetric ? 'C' : 'F';
        if (!isMetric) {
            temperature = ((temperature * 9) / 5) + 32;
        }

        temperatureTextView.setText(String.format("Temperature: %.2f°%c", temperature, thingy));

        int pressure = weather.getJSONObject("main").getInt("pressure");
        pressureTextView.setText(String.format("Pressure: %s hPa", pressure));

        double windSpeed = weather.getJSONObject("wind").getDouble("speed");
        String thingyButWindy = isMetric ? "km/h" : "mph";
        if (!isMetric) {
            windSpeed = 0.6214 * windSpeed;
        }
        windSpeedTextView.setText(String.format("Wind Speed: %.2f %s", windSpeed, thingyButWindy));

        int windDirection = weather.getJSONObject("wind").getInt("deg");
        windDirectionTextView.setText(String.format("Wind Direction: %s°", windDirection));

        int humidity = weather.getJSONObject("main").getInt("humidity");
        humidityTextView.setText(String.format("Humidity: %s%%", humidity));

        int clouds = weather.getJSONObject("clouds").getInt("all");
        visibilityTextView.setText(String.format("Cloudiness: %s%%", clouds));

        String imageString = weather.getJSONArray("weather").getJSONObject(0).getString("icon");
        String imageUrl = "https://openweathermap.org/img/wn/" + imageString + "@4x.png";

        Picasso.get().load(imageUrl).into(weatherImage);
    }
}