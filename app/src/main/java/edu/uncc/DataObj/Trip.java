package edu.uncc.DataObj;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;

import edu.uncc.hw09.R;
import edu.uncc.hw09.TripAdapter;

/**
 * Created by kalyan on 4/16/2017.
 */

public class Trip implements Serializable{
    private String title, location, photoURL;
    private String id, placeIds, people;

    public String getPlaceIds() {
        return placeIds;
    }

    public void addPlaceId(String placeId) {
        if(placeIds==null){
            placeIds = "";
            placeIds+= placeId;
        }else{
            if(!placeIds.contains(placeId)){
                placeIds+= ","+placeId;
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getPeople() {
        return people;
    }

    public void addPeople(String userId) {
        if(people==null){
            people = "";
            people+= userId;
        }else{
            if(!people.contains(userId)){
                people+= ","+userId;
            }
        }
    }

    public void setPlaceIds(String placeIds) {
        this.placeIds = placeIds;
    }
}
