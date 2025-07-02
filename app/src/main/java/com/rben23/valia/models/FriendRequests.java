package com.rben23.valia.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class FriendRequests {
    private int idRequest;
    private String idUser;
    private String idAddressee;
    private int idState;
    private String dateRequest;

    // Constructor predeterminado
    public FriendRequests(){}

    // Getters
    public int getIdRequest() {
        return idRequest;
    }

    public String getIdUser() {
        return idUser;
    }

    public String getIdAddressee() {
        return idAddressee;
    }

    public int getIdState() {
        return idState;
    }

    public String getDateRequest() {
        return dateRequest;
    }

    // Setters
    public void setIdRequest(int idRequest) {
        this.idRequest = idRequest;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public void setIdAddressee(String idAddressee) {
        this.idAddressee = idAddressee;
    }

    public void setIdState(int idState) {
        this.idState = idState;
    }

    public void setDateRequest(String dateRequest) {
        this.dateRequest = dateRequest;
    }
}
