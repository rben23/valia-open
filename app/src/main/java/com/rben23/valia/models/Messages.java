package com.rben23.valia.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Messages {
    private int idMessage;
    private String conversationUid;
    private String idSender;
    private String idAddressee;
    private String message;
    private String date;
    private int idType;
    private String pathFile;

    // Constructor predeterminado
    public Messages() {}

    // Getters
    public int getIdMessage() {
        return idMessage;
    }

    public String getConversationUid(){return conversationUid;}

    public String getIdSender() {
        return idSender;
    }

    public String getIdAddressee() {
        return idAddressee;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public int getIdType() {
        return idType;
    }

    public String getPathFile() {
        return pathFile;
    }

    // Setters
    public void setIdMessage(int idMessage) {
        this.idMessage = idMessage;
    }

    public void setConversationUid(String conversationUid){this.conversationUid = conversationUid;}

    public void setIdSender(String idSender) {this.idSender = idSender;}

    public void setIdAddressee(String idAddressee) {this.idAddressee = idAddressee;}

    public void setMessage(String message) {this.message = message;}

    public void setDate(String date) {this.date = date;}

    public void setIdType(int idType) {this.idType = idType;}

    public void setPathFile(String pathFile) {this.pathFile = pathFile;}
}
