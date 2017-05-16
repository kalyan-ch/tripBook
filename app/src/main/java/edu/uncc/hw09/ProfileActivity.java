package edu.uncc.hw09;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

import edu.uncc.DataObj.Trip;
import edu.uncc.DataObj.UserProfile;

public class ProfileActivity extends AppCompatActivity {
    UserProfile up;
    UserProfile logUp;
    DatabaseReference mDb;
    SharedPreferences shpr;
    String log_email;
    ArrayList<UserProfile> friends;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("Your Profile");
        shpr = getSharedPreferences("login",MODE_PRIVATE);
        log_email = shpr.getString("user_email","");

        final String email = getIntent().getStringExtra("email");
        mDb = FirebaseDatabase.getInstance().getReference();
        mDb.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserProfile u = snapshot.getValue(UserProfile.class);
                    String uEmail = u.getEmail();
                    if(email.equals(uEmail)){
                        up = u;
                    }
                    if(log_email.equals(uEmail)){
                        logUp = u;
                    }
                }

                if(log_email.equals(email)){
                    up = logUp;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDb.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(up.getFriends()!= null){
                    String[] fnId = up.getFriends().split(",");
                    friends = new ArrayList<UserProfile>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserProfile u = snapshot.getValue(UserProfile.class);
                        if(Arrays.asList(fnId).contains(u.getUser_id())){
                            friends.add(u);
                        }
                    }
                }
                setData();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDb.child("trips").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Trip> trips = new ArrayList<Trip>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()  ) {
                    Trip t = snapshot.getValue(Trip.class);
                    String peeps = t.getPeople();
                    if(peeps!=null){
                        if(peeps.contains(up.getUser_id())){
                            trips.add(t);
                        }
                    }

                }
                setTrips(trips);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setTrips(ArrayList<Trip> trips){
        RecyclerView mRecView = (RecyclerView)findViewById(R.id.profTripRec);
        mRecView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mRecView.setAdapter(new TripAdapter(trips,null,this));
    }

    public void gotoTrip(Trip t){
        Intent i2 = new Intent(ProfileActivity.this,TripActivity.class);
        i2.putExtra("trip",t);
        startActivity(i2);
    }

    public void goToProfile(String emailId){
        Intent i2 = new Intent(ProfileActivity.this,ProfileActivity.class);
        i2.putExtra("email",emailId);
        startActivity(i2);
    }

    public void setData(){
        Picasso.with(this).load(up.getDisplayPic()).into((ImageView) findViewById(R.id.profile_pic));
        ((TextView)findViewById(R.id.profile_name)).setText(up.getFirstName()+" "+up.getLastName());
        ((TextView)findViewById(R.id.profile_email)).setText(up.getEmail());
        if(up.getGender()!=null){
            ((TextView)findViewById(R.id.profile_gender)).setText(up.getGender());
        }else{
            ((TextView)findViewById(R.id.profile_gender)).setText("Not set");
        }

        RecyclerView mRecView = (RecyclerView)findViewById(R.id.profFrndsRec);
        mRecView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
        mRecView.setAdapter(new FriendsAdapter(friends,null,this,null));

        if(up.getEmail().equals(log_email)){
            ((Button)findViewById(R.id.add_friend)).setVisibility(View.INVISIBLE);
            (findViewById(R.id.edit_profile)).setVisibility(View.VISIBLE);
        }else{
            ((Button)findViewById(R.id.add_friend)).setVisibility(View.VISIBLE);
            (findViewById(R.id.edit_profile)).setVisibility(View.INVISIBLE);
            String frnds = logUp.getFriends();
            if(frnds!=null){
                if(frnds.contains(up.getUser_id())){
                    ((Button)findViewById(R.id.add_friend)).setText("Friends");
                    ((Button)findViewById(R.id.add_friend)).setBackgroundColor(getResources().getColor(R.color.cardview_shadow_start_color));
                    ((Button)findViewById(R.id.add_friend)).setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark_focused));
                    ((Button)findViewById(R.id.add_friend)).setClickable(false);
                }
            }
        }
    }

    public void addFriend(View v){
        logUp.addFriend(up.getUser_id());
        up.addFriend(logUp.getUser_id());
        mDb.child("users").child(logUp.getUser_id()).setValue(logUp);
        mDb.child("users").child(up.getUser_id()).setValue(up);
        Log.d("demo","added successfully!");
        Toast.makeText(this, "Added Successfully!", Toast.LENGTH_SHORT).show();
        ((Button)v).setText("Friends");
        ((Button)v).setBackgroundColor(getResources().getColor(R.color.cardview_shadow_start_color));
        ((Button)v).setTextColor(getResources().getColor(R.color.common_google_signin_btn_text_dark_focused));
        ((Button)v).setClickable(false);
    }

    public void editProfile(View v){
        Intent inte = new Intent(ProfileActivity.this, EditProfileActivity.class);
        inte.putExtra("user",logUp);
        startActivity(inte);
    }
}
