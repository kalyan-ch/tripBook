package edu.uncc.hw09;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import edu.uncc.DataObj.TripPlace;

public class RoundTripActivity extends FragmentActivity implements OnMapReadyCallback {
    GoogleMap mp;
    ArrayList<LatLng> locList;
    DatabaseReference mDb;
    String placeIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round);
        placeIds = getIntent().getStringExtra("placeIds");
        mDb = FirebaseDatabase.getInstance().getReference();
        SupportMapFragment mFra = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapFrag);
        mFra.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mp = googleMap;
        mDb.child("places").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locList = new ArrayList<LatLng>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()  ) {
                    TripPlace tp = snapshot.getValue(TripPlace.class);
                    if(placeIds!=null){
                        if(placeIds.contains(tp.getId())){
                            locList.add(new LatLng(Double.parseDouble(tp.getLat()),Double.parseDouble(tp.getLng())));
                        }
                    }

                }
                drawPath();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Marker drawMarker(LatLng ltLn){
        return mp.addMarker((new MarkerOptions()).position(ltLn));
    }

    public void drawPath(){
        if(mp!=null){
            PolylineOptions poly = new PolylineOptions();
            if(locList.size()>0){
                for(int i=0; i<locList.size();i++){
                    LatLng ln = locList.get(i);
                    poly.add(ln);
                    drawMarker(ln);
                }

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(locList.get(0)).include(locList.get(locList.size()-1));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 11);
                mp.moveCamera(cameraUpdate);
                poly.width(10).color(Color.BLACK).geodesic(true);
                mp.addPolyline(poly);
            }
        }
    }
}
