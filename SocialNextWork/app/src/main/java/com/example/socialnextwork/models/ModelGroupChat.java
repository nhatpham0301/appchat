package com.example.socialnextwork.models;

public class ModelGroupChat {

    public String NameGroup;
    public String idMembers;
    public String idCaptain;
    public String Chat;

    public ModelGroupChat()
    {}

    public ModelGroupChat(String nameGroup, String idMembers, String idCaptain, String chat) {
        NameGroup = nameGroup;
        this.idMembers = idMembers;
        this.idCaptain = idCaptain;
        Chat = chat;
    }

    public String getNameGroup() {
        return NameGroup;
    }

    public void setNameGroup(String nameGroup) {
        NameGroup = nameGroup;
    }

    public String getIdMembers() {
        return idMembers;
    }

    public void setIdMembers(String idMembers) {
        this.idMembers = idMembers;
    }

    public String getIdCaptain() {
        return idCaptain;
    }

    public void setIdCaptain(String idCaptain) {
        this.idCaptain = idCaptain;
    }

    public String getChat() {
        return Chat;
    }

    public void setChat(String chat) {
        Chat = chat;
    }
}
