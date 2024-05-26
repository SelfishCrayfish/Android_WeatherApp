package com.example.weatherapp;

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

import java.util.Date;

public class FragmentForecast extends Fragment {
    private TextView[] dateArray;
    private TextView[] daytimeArray;
    private TextView[] nighttimeArray;
    private TextView[] tempDaytimeArray;
    private TextView[] tempNighttimeArray;
    private TextView[] descDaytimeArray;
    private TextView[] descNighttimeArray;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast, container, false);
        dateArray = new TextView[5];
        daytimeArray = new TextView[5];
        nighttimeArray = new TextView[5];
        tempDaytimeArray = new TextView[5];
        tempNighttimeArray = new TextView[5];
        descDaytimeArray = new TextView[5];
        descNighttimeArray = new TextView[5];
        for (int i = 0; i < 5; i++){
            String date = "dateDay" + (i + 1);
            String daytime = "daytime" + (i + 1);
            String nighttime = "nighttime" + (i + 1);
            String tempDay = "tempDaytime" + (i + 1);
            String tempNight = "tempNighttime" + (i + 1);
            String descDay = "descriptionDaytime" + (i + 1);
            String descNight = "descriptionNighttime" + (i + 1);
            int dateID = getResources().getIdentifier(date, "id", requireActivity().getPackageName());
            int daytimeID = getResources().getIdentifier(daytime, "id", requireActivity().getPackageName());
            int nighttimeID = getResources().getIdentifier(nighttime, "id", requireActivity().getPackageName());
            int tempDayID = getResources().getIdentifier(tempDay, "id", requireActivity().getPackageName());
            int tempNightID = getResources().getIdentifier(tempNight, "id", requireActivity().getPackageName());
            int descDayID = getResources().getIdentifier(descDay, "id", requireActivity().getPackageName());
            int descNightID = getResources().getIdentifier(descNight, "id", requireActivity().getPackageName());
            dateArray[i] = view.findViewById(dateID);
            daytimeArray[i] = view.findViewById(daytimeID);
            nighttimeArray[i] = view.findViewById(nighttimeID);
            tempDaytimeArray[i] = view.findViewById(tempDayID);
            tempNighttimeArray[i] = view.findViewById(tempNightID);
            descDaytimeArray[i] = view.findViewById(descDayID);
            descNighttimeArray[i] = view.findViewById(descNightID);
        }
        Log.d("FragmentForecast", "I am fine haha");
        return view;
    }

    public void updateForecastData(JSONArray weather, boolean isMetric) throws JSONException {
        int daytimeIterator = 0;
        int nighttimeIterator = 0;

        for (int i = 0; i < weather.length(); i++) {
            JSONObject dayForecast = weather.getJSONObject(i);
            String date = dayForecast.getString("dt_txt");
            String hour = date.substring(date.length() - 8);
            if(hour.equals("06:00:00")){
                String pureDate = date.replaceAll("06:00:00", "");
                dateArray[daytimeIterator].setText(pureDate.substring(5));
                daytimeArray[daytimeIterator].setText("6:00");

                double temperature = dayForecast.getJSONObject("main").getDouble("temp");
                tempDaytimeArray[daytimeIterator].setText(String.valueOf(temperature));

                String description = dayForecast.getJSONArray("weather").getJSONObject(0).getString("main");
                descDaytimeArray[daytimeIterator].setText(description);
                daytimeIterator++;
            }
            if(hour.equals("18:00:00")){
                nighttimeArray[nighttimeIterator].setText("18:00");
                tempNighttimeArray[nighttimeIterator].setText(dayForecast.getJSONObject("main").getString("temp"));
                descNighttimeArray[nighttimeIterator].setText(dayForecast.getJSONArray("weather").getJSONObject(0).getString("main"));
                nighttimeIterator++;
            }
        }
    }
}