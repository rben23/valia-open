package com.rben23.valia.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Routes {
    private int idRoute;
    private int idActivity;
    private String name;
    private String pathRoute;
    private String tiemDuration;
    private float totalKm;

    // Constructor predeterminado
    public Routes() {
    }

    // Getters
    public int getIdRoute() {
        return idRoute;
    }

    public int getIdActivity() {
        return idActivity;
    }

    public String getName() {
        return name;
    }

    public String getPathRoute() {
        return pathRoute;
    }

    public String getTiemDuration() {
        return tiemDuration;
    }

    public float getTotalKm() {
        return totalKm;
    }

    // Setters
    public void setIdRoute(int idRoute) {
        this.idRoute = idRoute;
    }

    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPathRoute(String pathRoute) {
        this.pathRoute = pathRoute;
    }

    public void setTiemDuration(String tiemDuration) {
        this.tiemDuration = tiemDuration;
    }

    public void setTotalKm(float totalKm) {
        this.totalKm = totalKm;
    }
}
