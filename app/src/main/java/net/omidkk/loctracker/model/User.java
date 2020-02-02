package net.omidkk.loctracker.model;

import android.location.Location;
import android.net.Uri;

public class User {

    private String name;
    private Uri photo;
    private Location location;
    private static User user;

    public User(String name, Uri photo, Location location) {
        this.name = name;
        this.photo = photo;
        this.location = location;
    }

    public User(String name, Uri photo) {
        this.name = name;
        this.photo = photo;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }


    public static User getUser(){
        return user;
    }

    public static void setUser(String name,Uri photo){
        user=new User(name,photo);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getPhoto() {
        return photo;
    }

    public void setPhoto(Uri photo) {
        this.photo = photo;
    }

}
