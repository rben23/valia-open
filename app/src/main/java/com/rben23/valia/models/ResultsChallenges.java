package com.rben23.valia.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ResultsChallenges {
    private int idResult;
    private int idChallenge;
    private String idUser;
    private String timeDuration;
    private String date;

    // Constructor predeterminado
    public ResultsChallenges() {
    }

    // Getters
    public int getIdResult() {
        return idResult;
    }

    public int getIdChallenge() {
        return idChallenge;
    }

    public String getIdUser() {
        return idUser;
    }

    public String getTimeDuration() {
        return timeDuration;
    }

    public String getDate() {
        return date;
    }

    // Setters
    public void setIdResult(int idResult) {
        this.idResult = idResult;
    }

    public void setIdChallenge(int idChallenge) {
        this.idChallenge = idChallenge;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public void setTimeDuration(String timeDuration) {
        this.timeDuration = timeDuration;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
