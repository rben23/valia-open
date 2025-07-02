package com.rben23.valia.models.joins;

public class ActivityRouteJoin {

    // Ruta
    private int idRoute;
    private String routeName;
    private String pathRoute;
    private String tiemDuration;
    private double totalKm;

    // Actividad
    private int idActivity;
    private String idUser;
    private String nameType;
    private int idType;
    private String date;

    public ActivityRouteJoin() {
    }

    // Getters
    public int getIdRoute() {
        return idRoute;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getPathRoute() {
        return pathRoute;
    }

    public String getTiemDuration() {
        return tiemDuration;
    }

    public double getTotalKm() {
        return totalKm;
    }

    public int getIdActivity() {
        return idActivity;
    }

    public String getIdUser() {
        return idUser;
    }

    public String getNameType() {
        return nameType;
    }

    public int getIdType() {
        return idType;
    }

    public String getDate() {
        return date;
    }

    // Setters
    public void setIdRoute(int idRoute) {
        this.idRoute = idRoute;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public void setPathRoute(String pathRoute) {
        this.pathRoute = pathRoute;
    }

    public void setTiemDuration(String tiemDuration) {
        this.tiemDuration = tiemDuration;
    }

    public void setTotalKm(double totalKm) {
        this.totalKm = totalKm;
    }

    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public void setNameType(String nameType) {
        this.nameType = nameType;
    }
    public void setIdType(int idType) {
        this.idType = idType;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
