package com.project.mygallary.models;

import java.util.ArrayList;

/**
 * Created by andrew on 05/08/17.
 */

public class Album {
    private String name;
    private ArrayList<String> imageUri;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getImageUri() {
        return imageUri;
    }

    public void setImageUri(ArrayList<String> imageUri) {
        this.imageUri = imageUri;
    }

}
