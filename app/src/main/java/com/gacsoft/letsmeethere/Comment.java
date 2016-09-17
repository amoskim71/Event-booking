package com.gacsoft.letsmeethere;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Gacsoft on 9/17/2016.
 */
public class Comment {
    private String key = "";
    private String event = "";
    private String name = "";
    private String email = "";
    private Date when;
    private String post = "";

    public Comment(String key, String event, String name, String email, Date when, String post) {
        this.key = key;
        this.event = event;
        this.name = name;
        this.email = email;
        this.when = when;
        this.post = post;
    }

    public Comment(String event, String name, String email, Date when, String post) {
        this.key = UUID.randomUUID().toString();
        this.event = event;
        this.name = name;
        this.email = email;
        this.when = when;
        this.post = post;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }
}
