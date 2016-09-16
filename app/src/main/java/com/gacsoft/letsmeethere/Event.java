package com.gacsoft.letsmeethere;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Gacsoft on 9/5/2016.
 */
public class Event {
    private String id;
    private String key;
    private String name;
    private boolean owned;
    private Date when;
    private double longitude;
    private double latitude;
    private boolean isNew = false;
    private boolean isModified = false;

    public Event() {
        this.key = UUID.randomUUID().toString();
    }

    public Event (String key, String id, String name, boolean owned, Date when, double longitude, double latitude) {
        this.id = id;
        this.name = name;
        this.owned = owned;
        this.when = when;
        this.setLongitude(longitude);
        this.setLatitude(latitude);
        this.key = key;
    }

    public Event(String id, String name, boolean owned, Date when, double longitude, double latitude) {
        this.id = id;
        this.name = name;
        this.owned = owned;
        this.when = when;
        this.setLongitude(longitude);
        this.setLatitude(latitude);
        this.key = UUID.randomUUID().toString();
    }

    public String getKey() { return key; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOwned() {
        return owned;
    }

    public void setOwned(boolean owned) {
        this.owned = owned;
    }

    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setNew(boolean isNew) { this.isNew = isNew;}

    public boolean isNew() { return isNew;}

    public void setModified(boolean modified) { this.isModified = modified;}

    public boolean isModified() { return isModified;}
}
