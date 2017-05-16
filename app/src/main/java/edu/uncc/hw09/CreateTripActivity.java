package edu.uncc.hw09;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import edu.uncc.DataObj.Trip;
import edu.uncc.DataObj.UserProfile;

public class CreateTripActivity extends AppCompatActivity {
    String uploadUrl;
    private StorageReference mStr;
    private DatabaseReference mDatabase;
    ArrayList<UserProfile> friends;
    UserProfile logUser;
    String[] frand = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);
        SharedPreferences shpr = getSharedPreferences("login",MODE_PRIVATE);
        mStr = FirebaseStorage.getInstance().getReference();
        final String log_email = shpr.getString("user_email","");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()  ) {
                    UserProfile u = snapshot.getValue(UserProfile.class);
                    if(log_email.equals(u.getEmail())){
                       logUser = u;
                        break;
                    }
                }
                if(logUser!=null){
                    final String friend = logUser.getFriends();

                    if(friend!=null){
                        frand = friend.split(",");
                    }
                    if(frand!=null){
                        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot1) {
                                friends = new ArrayList<UserProfile>();
                                for (DataSnapshot snapshot : dataSnapshot1.getChildren()  ) {
                                    UserProfile u1 = snapshot.getValue(UserProfile.class);

                                    if(Arrays.asList(frand).contains(u1.getUser_id())){
                                        friends.add(u1);
                                    }
                                }

                                setRecyclerViewData();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setRecyclerViewData(){
        RecyclerView mRecView = (RecyclerView)findViewById(R.id.trip_people);
        mRecView.setLayoutManager(new LinearLayoutManager(this));
        mRecView.setAdapter(new TripPeopleAdapter(friends));
    }

    public void createTrip(View v){
        final String title = ((EditText)findViewById(R.id.tripTitle)).getText().toString();
        if("".equals(title)){
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
            return;
        }
        final String location = ((EditText)findViewById(R.id.tripLocation)).getText().toString();
        if("".equals(location)){
            Toast.makeText(this, "Enter location", Toast.LENGTH_SHORT).show();
            return;
        }

        Trip trip= new Trip();

        trip.setTitle(title);
        trip.setLocation(location);
        trip.setId(UUID.randomUUID().toString());
        trip.setPhotoURL(uploadUrl);
        trip.addPeople(logUser.getUser_id());
        for (UserProfile u: friends){
            if(u.isOnTrip()){
                trip.addPeople(u.getUser_id());
            }
        }

        mDatabase.child("trips").child(trip.getId()).setValue(trip);
        Toast.makeText(this, "New trip created Successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
    public void cancel(View v){
        finish();
    }

    public void openGall(View v){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent,2);
    }

    @Override
    @SuppressWarnings("VisibleForTests")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && resultCode == RESULT_OK ){
            try{
                Uri uri = data.getData();
                InputStream is = getContentResolver().openInputStream(uri);
                byte[] inData = getBytes(is);
                StorageReference picPath = mStr.child(UUID.randomUUID().toString());
                UploadTask task = picPath.putBytes(inData);
                task.addOnSuccessListener(CreateTripActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(CreateTripActivity.this, "Location Photo uploaded successfully", Toast.LENGTH_SHORT).show();
                        Uri url = taskSnapshot.getDownloadUrl();
                        uploadUrl = url.toString();
                        Picasso.with(CreateTripActivity.this).load(uploadUrl).into((ImageView)findViewById(R.id.trip_pic));
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
