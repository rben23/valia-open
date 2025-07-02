package com.rben23.valia.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Users {
    private String Uid;
    private String UserId;
    private String ProfileImage;
    private String Name;
    private String Email;
    private String CreationDate;
    private int RoleId;

    // Constructor predeterminado
    public Users() {
        this.ProfileImage = "";
    }

    // Getters
    public String getUid(){return Uid;}

    public String getUserId() {
        return UserId;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public String getName() {
        return Name;
    }

    public String getEmail() {
        return Email;
    }

    public int getRoleId() {
        return RoleId;
    }

    public String getCreationDate() {
        return CreationDate;
    }

    // Setters
    public void setUid(String uid){this.Uid = uid;}

    public void setUserId(String userId) {
        this.UserId = userId;
    }

    public void setProfileImage(String profileImage) {
        this.ProfileImage = profileImage;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public void setRoleId(int roleId) {
        this.RoleId = roleId;
    }

    public void setCreationDate(String creationDate) {
        this.CreationDate = creationDate;
    }
}
