package edu.uncc.hw09;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

import edu.uncc.DataObj.Trip;
import edu.uncc.DataObj.TripPlace;
import edu.uncc.DataObj.UserProfile;

public class TripActivity extends AppCompatActivity {
    Trip actTrip;
    DatabaseReference mDb;
    String friends = "";
    String logUId = "";
    ArrayList<UserProfile> pplOnTrip;
    int PLACE_PICKER_REQUEST = 1;
    ArrayList<TripPlace> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        actTrip = (Trip)getIntent().getSerializableExtra("trip");
        friends = actTrip.getPeople();
        mDb = FirebaseDatabase.getInstance().getReference();
        getUsers();
        getPlaces();
    }

    public void getPlaces(){
        final String placeIds = actTrip.getPlaceIds();
        mDb.child("places").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                places = new ArrayList<TripPlace>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()  ) {
                    TripPlace tp = snapshot.getValue(TripPlace.class);
                    if(placeIds!=null){
                        if(placeIds.contains(tp.getId())){
                            places.add(tp);
                        }
                    }

                }
                setPlaces();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void goToProfile(String emailId){
        Intent i2 = new Intent(TripActivity.this,ProfileActivity.class);
        i2.putExtra("email",emailId);
        startActivity(i2);
    }

    public void getUsers(){
        SharedPreferences shpr = getSharedPreferences("login",MODE_PRIVATE);
        final String log_email = shpr.getString("user_email","");
        mDb.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] fnId = actTrip.getPeople().split(",");
                pplOnTrip = new ArrayList<UserProfile>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()  ) {
                    UserProfile u = snapshot.getValue(UserProfile.class);
                    if(Arrays.asList(fnId).contains(u.getUser_id())){
                        pplOnTrip.add(u);
                    }
                    if(log_email.equals(u.getEmail())){
                        logUId = u.getUser_id();
                    }
                }
                setTripData();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setTripData(){
        ((TextView)findViewById(R.id.trip_v_title)).setText(actTrip.getTitle());
        ((TextView)findViewById(R.id.trip_v_loc)).setText(actTrip.getLocation());
        Picasso.with(this).load(actTrip.getPhotoURL()).into((ImageView)findViewById(R.id.trip_v_pic));

        if(friends.contains(logUId)){
            (findViewById(R.id.joinBtn)).setVisibility(View.INVISIBLE);
        }else{
            (findViewById(R.id.chatBtn)).setVisibility(View.INVISIBLE);
        }
        RecyclerView mRecView = (RecyclerView)findViewById(R.id.pplOnTripRec);
        mRecView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mRecView.setAdapter(new FriendsAdapter(pplOnTrip,null,null,this));

    }

    public void setPlaces(){
        RecyclerView mRecView = (RecyclerView)findViewById(R.id.plcOnTripRec);
        mRecView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mRecView.setAdapter(new PlaceAdapter(places,this));
    }

    public void gotoChat(View v){
        Intent i2 = new Intent(TripActivity.this,ChatActivity.class);
        i2.putExtra("trip",actTrip);
        startActivity(i2);
    }

    public void joinTrip(View v){
        actTrip.addPeople(logUId);
        mDb.child("trips").child(actTrip.getId()).setValue(actTrip);
        getUsers();
        finish();
        startActivity(getIntent());
    }

    public void viewRoundTrip(View v){
        Intent inte = new Intent(this, RoundTripActivity.class);
        inte.putExtra("placeIds",actTrip.getPlaceIds());
        startActivity(inte);
    }

    public void goToMaps(String location){
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void pickAPlace(View v){

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try{
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                TripPlace tp = new TripPlace();
                LatLng ln = place.getLatLng();
                tp.setLat(""+ln.latitude);
                tp.setLng(""+ln.longitude);
                tp.setName(place.getName().toString());
                tp.setId(place.getId());
                actTrip.addPlaceId(place.getId());

                mDb.child("places").child(tp.getId()).setValue(tp);
                mDb.child("trips").child(actTrip.getId()).child("placeIds").setValue(actTrip.getPlaceIds());
                getPlaces();
                String toastMsg = place.getName().toString();
                Toast.makeText(this, toastMsg+" added to trip successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void deletePlace(TripPlace t){
        mDb.child("places").child(t.getId()).removeValue();
        String[] places = actTrip.getPlaceIds().split(",");
        String newPlaces = "";
        for(int i=0; i<places.length;i++){
            String s = places[i];
            if(!s.equals(t.getId())){
                if(i!=0){
                    newPlaces += ","+s;
                }else{
                    newPlaces += s;
                }
            }
        }

        actTrip.setPlaceIds(newPlaces);

        mDb.child("trips").child(actTrip.getId()).setValue(actTrip);

    }

}
