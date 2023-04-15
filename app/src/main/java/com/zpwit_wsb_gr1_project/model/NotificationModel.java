package com.zpwit_wsb_gr1_project.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationModel {

    String id, notification, idUserFrom;

    @ServerTimestamp
    Date time;

    public NotificationModel() {
    }

    public NotificationModel(String id, String notification, String idUserFrom, Date time) {
        this.id = id;
        this.notification = notification;
        this.idUserFrom = idUserFrom;
        this.time = time;
    }

    public String getIdUserFrom() {
        return idUserFrom;
    }

    public void setIdUserFrom(String idUserFrom) {
        this.idUserFrom = idUserFrom;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
