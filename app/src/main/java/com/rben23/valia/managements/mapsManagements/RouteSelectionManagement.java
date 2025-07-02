package com.rben23.valia.managements.mapsManagements;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.adapters.SelectActivitiesAdapter;
import com.rben23.valia.adapters.ActivityTypes;
import com.rben23.valia.R;

public class RouteSelectionManagement extends AppCompatActivity {

    private ImageView goBack;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_selection_management);

        initViews();
        initListeners();
        setupRecyclerView();
    }

    private void initViews() {
        goBack = findViewById(R.id.imgGoBack);
        recyclerView = findViewById(R.id.showActivities);
    }

    private void initListeners() {
        // Volvemos a la actividad anterior
        goBack.setOnClickListener(view -> finish());
    }

    private void setupRecyclerView() {
        // Configuramos RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Añadimos el adaptador a recyclerView
        SelectActivitiesAdapter selectActivitiesAdapter = new SelectActivitiesAdapter(this, ActivityTypes.values(), activity -> {
            Log.d("DEBUG", "Enviando actividad: " + activity.getName());

            Intent intent = new Intent(RouteSelectionManagement.this, MapManagement.class);
            intent.putExtra("activityIcon", activity.getIcon());
            intent.putExtra("activityName", activity.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(selectActivitiesAdapter);
    }
}