package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeatherCitiesAdapter extends RecyclerView.Adapter<WeatherCitiesAdapter.CityViewHolder> {

    private final List<String> cities;
    private final CityClickListener listener;

    public interface CityClickListener {
        void onCityClicked(String city);
        void onCityRemoved(String city);
    }

    public WeatherCitiesAdapter(List<String> cities, CityClickListener listener) {
        this.cities = cities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cities, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        String city = cities.get(position);
        holder.cityName.setText(city);
        holder.cityName.setOnClickListener(v -> listener.onCityClicked(city));
        holder.deleteButton.setOnClickListener(v -> listener.onCityRemoved(city));
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView cityName;
        ImageButton deleteButton;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.city_name);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
