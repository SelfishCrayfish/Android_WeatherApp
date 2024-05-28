package com.example.weatherapp;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherFetcher {
    public static void fetchAllWeatherData(final String cityName, final WeatherDataCallback callback) {
        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                try {
                    String apiKey = "df4d8ceb5e6f3304d37b838b0d1d024e";
                    String forecastUrlString = "https://api.openweathermap.org/data/2.5/forecast?q=" + cityName + "&appid=" + apiKey + "&units=metric";
                    String latestUrlString = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + apiKey + "&units=metric";
                    URL forecastUrl = new URL(forecastUrlString);
                    URL latestUrl = new URL(latestUrlString);
                    HttpURLConnection forecastUrlConnection = (HttpURLConnection) forecastUrl.openConnection();
                    HttpURLConnection latestUrlConnection = (HttpURLConnection) latestUrl.openConnection();
                    try {
                        InputStream forecastInputStream = new BufferedInputStream(forecastUrlConnection.getInputStream());
                        BufferedReader forecastBufferedReader = new BufferedReader(new InputStreamReader(forecastInputStream));
                        StringBuilder forecastResult = new StringBuilder();
                        String forecastLine;
                        while ((forecastLine = forecastBufferedReader.readLine()) != null) {
                            forecastResult.append(forecastLine);
                        }
                        JSONObject forecastJson = new JSONObject(forecastResult.toString());

                        InputStream latestInputStream = new BufferedInputStream(latestUrlConnection.getInputStream());
                        BufferedReader latestBufferedReader = new BufferedReader(new InputStreamReader(latestInputStream));
                        StringBuilder latestResult = new StringBuilder();
                        String latestLine;
                        while ((latestLine = latestBufferedReader.readLine()) != null) {
                            latestResult.append(latestLine);
                        }
                        JSONObject latestJson = new JSONObject(latestResult.toString());

                        JSONObject combinedJson = new JSONObject();
                        combinedJson.put(cityName + "_latestWeather", latestJson);
                        combinedJson.put(cityName + "_forecastWeather", forecastJson);

                        return combinedJson;
                    } finally {
                        forecastUrlConnection.disconnect();
                        latestUrlConnection.disconnect();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onFailure(new Exception("Failed to fetch weather data"));
                }
            }
        }.execute();
    }

    public interface WeatherDataCallback {
        void onSuccess(JSONObject data);
        void onFailure(Exception e);
    }
}
