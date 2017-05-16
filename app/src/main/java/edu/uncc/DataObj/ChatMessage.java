package edu.uncc.DataObj;

/**
 * Created by kalyan on 4/19/2017.
 */

public class ChatMessage {
    private String id;
    private String message;
    private String senderName;
    private String time;
    private String img_url;
    private String trip_id;
    private String sender_id;
    private String views;

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public void addView(String userId){
        if(views==null){
            views = "";
            views+= userId;
        }else{
            if(!views.contains(userId)){
                views+= ","+userId;
            }
        }
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}
