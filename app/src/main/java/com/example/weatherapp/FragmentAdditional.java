package com.example.weatherapp;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FragmentAdditional extends Fragment implements WeatherCitiesAdapter.CityClickListener {

    private RecyclerView recyclerView;
    private WeatherCitiesAdapter adapter;
    private List<String> cities;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_additional, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_cities);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cities = CityUtils.getCities(getContext());
        adapter = new WeatherCitiesAdapter(cities, this);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.button_add_city).setOnClickListener(v -> showAddCityDialog());

        return view;
    }

    private void showAddCityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add city");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String city = input.getText().toString().trim();
            if (!city.isEmpty() && isNetworkAvailable()) {
                CityUtils.addCity(getContext(), city);
                cities.add(city);
                adapter.notifyItemInserted(cities.size() - 1);
            } else {
                Utils.showToast(getContext(), "No internet connection or empty city name");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onCityClicked(String city) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.updateWeatherForCity(city);
        }
    }

    @Override
    public void onCityRemoved(String city) {
        CityUtils.removeCity(getContext(), city);
        int position = cities.indexOf(city);
        if (position != -1) {
            cities.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }
}
