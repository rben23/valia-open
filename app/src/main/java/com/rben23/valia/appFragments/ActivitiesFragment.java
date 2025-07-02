package com.rben23.valia.appFragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rben23.valia.adapters.ListActivitiesRoutesAdapter;
import com.rben23.valia.DAO.RoutesDAO;
import com.rben23.valia.R;
import com.rben23.valia.managements.ActivityManagement;
import com.rben23.valia.managements.mapsManagements.RouteSelectionManagement;
import com.rben23.valia.models.joins.ActivityRouteJoin;

import java.util.ArrayList;
import java.util.List;

public class ActivitiesFragment extends Fragment {
    private Button btnStartActivity;
    private RecyclerView recyclerRoutes;
    private ConstraintLayout savedActivitiesEmpty;
    private ListActivitiesRoutesAdapter activitiesRoutesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Mostrar vista
        View view = inflater.inflate(R.layout.fragment_activities, container, false);

        // Elementos de la vista
        initViews(view);

        // Ir a la actividad NuevaRuta
        initListeners(view);

        updateUI();
        return view;
    }

    private void initViews(View view) {
        btnStartActivity = view.findViewById(R.id.btnStartActivity);
        recyclerRoutes = view.findViewById(R.id.recyclerRoutes);
        savedActivitiesEmpty = view.findViewById(R.id.savedActivitiesEmpty);
    }

    private void initListeners(View view) {
        btnStartActivity.setOnClickListener(btnStartActivity -> {
            Intent intent = new Intent(getActivity(), RouteSelectionManagement.class);
            startActivity(intent);
        });

        recyclerRoutes.setLayoutManager(new LinearLayoutManager(this.getContext()));
        activitiesRoutesAdapter = new ListActivitiesRoutesAdapter(new ArrayList<>(), item -> {
            int idRoute = item.getIdRoute();
            Context context = view.getContext();
            Intent intent = new Intent(context, ActivityManagement.class);
            intent.putExtra("idRoute", idRoute);
            context.startActivity(intent);
        });
        recyclerRoutes.setAdapter(activitiesRoutesAdapter);
    }

    // Obtenemos la lista y mostramos u ocultamos los views
    private void updateUI() {
        new Thread(() -> {
            List<ActivityRouteJoin> data = new RoutesDAO().getActivityRoutes();

            if (getView() != null) {
                // Comprobamos que hay datos para mostrar, si hay se muestran y si no se muestra layout empty
                getView().post(() -> {
                    if (data != null && !data.isEmpty()) {
                        recyclerRoutes.setVisibility(VISIBLE);
                        savedActivitiesEmpty.setVisibility(GONE);

                        activitiesRoutesAdapter.updateItems(data);
                    } else {
                        recyclerRoutes.setVisibility(GONE);
                        savedActivitiesEmpty.setVisibility(VISIBLE);
                    }
                });
            }
        }).start();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        List<ActivityRouteJoin> newData = new RoutesDAO().getActivityRoutes();
        activitiesRoutesAdapter.updateItems(newData);
        updateUI();
    }
}