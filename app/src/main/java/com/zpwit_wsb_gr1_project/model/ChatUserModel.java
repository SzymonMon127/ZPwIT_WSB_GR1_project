package com.zpwit_wsb_gr1_project.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class ChatUserModel {

    private String id, lastMessage;
    private List<String> uid;
    private List<String> search;

    @ServerTimestamp
    private Date time;


    public ChatUserModel() {
    }


    public ChatUserModel(String id, String lastMessage, List<String> uid, List<String> search, Date time) {
        this.id = id;
        this.lastMessage = lastMessage;
        this.uid = uid;
        this.search = search;
        this.time = time;
    }

    public List<String> getSearch() {
        return search;
    }

    public void setSearch(List<String> search) {
        this.search = search;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public List<String> getUid() {
        return uid;
    }

    public void setUid(List<String> uid) {
        this.uid = uid;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
