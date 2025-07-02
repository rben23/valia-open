package com.rben23.valia.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Challenges {
    private int idChallenge;
    private String idUser;
    private String idAddressee;
    private String description;
    private double totalKm;
    private String date;

    // Constructor predeterminado
    public Challenges(){}

    // Getters
    public int getIdChallenge() {
        return idChallenge;
    }

    public String getIdUser() {
        return idUser;
    }

    public String getIdAddressee() {
        return idAddressee;
    }

    public String getDescription() {
        return description;
    }

    public double getTotalKm() {
        return totalKm;
    }

    public String getDate() {
        return date;
    }

    // Setters
    public void setIdChallenge(int idChallenge) {
        this.idChallenge = idChallenge;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public void setIdAddressee(String idAddressee) {
        this.idAddressee = idAddressee;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTotalKm(double totalKm) {
        this.totalKm = totalKm;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
