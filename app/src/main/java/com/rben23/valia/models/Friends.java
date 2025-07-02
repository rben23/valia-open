package com.rben23.valia.models;

import android.database.Cursor;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.IgnoreExtraProperties;
import com.rben23.valia.ValiaSQLiteHelper;

@IgnoreExtraProperties
public class Friends {
    private String idUser;
    private String idFriend;
    private String friendshipDate;

    // Constructor predeterminado
    public Friends() {
    }

    // Getters
    public String getIdUser() {
        return idUser;
    }

    public String getIdFriend() {
        return idFriend;
    }

    public String getFriendshipDate() {
        return friendshipDate;
    }

    // Setters
    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public void setIdFriend(String idFriend) {
        this.idFriend = idFriend;
    }

    public void setFriendshipDate(String friendshipDate) {
        this.friendshipDate = friendshipDate;
    }
}
