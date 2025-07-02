package com.rben23.valia.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Activities {
    private int idActivity;
    private String idUser;
    private String name;
    private int idType;
    private String date;

    // Constructor predeterminado
    public Activities() {}

    // Getters
    public int getIdActivity() {
        return idActivity;
    }

    public String getIdUser() {
        return idUser;
    }

    public String getName() {
        return name;
    }

    public int getIdType() {
        return idType;
    }

    public String getDate() {
        return date;
    }

    // Setters
    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdType(int idType) {
        this.idType = idType;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
