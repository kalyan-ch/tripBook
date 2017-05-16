package edu.uncc.DataObj;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by kalyan on 4/16/2017.
 */

public class UserProfile implements Serializable {
    private String user_id;
    private String firstName;
    private String lastName;
    private String gender;
    private String displayPic;
    private String email;
    private String friends;
    private boolean onTrip;

    public boolean isOnTrip() {
        return onTrip;
    }

    public void setOnTrip(boolean onTrip) {
        this.onTrip = onTrip;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDisplayPic() {
        return displayPic;
    }

    public void setDisplayPic(String displayPic) {
        this.displayPic = displayPic;
    }

    public String getFriends() {
        return friends;
    }

    public void addFriend(String friendId) {
        if(friends==null){
            friends = "";
            friends+= friendId;
        }else{
            if(!friends.contains(friendId)){
                friends+= ","+friendId;
            }
        }
    }
}
