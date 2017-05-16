package edu.uncc.hw09;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import edu.uncc.DataObj.Trip;
import edu.uncc.DataObj.UserProfile;

/**
 * Created by kalyan on 4/21/2017.
 */

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.MyViewHolder> {
    ArrayList<Trip> trips;
    MainActivity activity;
    ProfileActivity pActivity;
    public TripAdapter(ArrayList<Trip> trips, MainActivity activity, ProfileActivity pActivity) {
        this.trips = trips;
        this.activity = activity;
        this.pActivity = pActivity;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        View v;

        public MyViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_card, null);
        MyViewHolder vOne = new MyViewHolder(v);
        return vOne;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        View v1 = holder.v;
        final Trip t = trips.get(position);
        ((TextView)v1.findViewById(R.id.card_name)).setText(t.getTitle());
        Picasso.with(v1.getContext()).load(t.getPhotoURL()).into((ImageView)v1.findViewById(R.id.card_pic));
        ((Button)v1.findViewById(R.id.card_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activity!=null){
                    activity.gotoTrip(t);
                }else if(pActivity!=null){
                    pActivity.gotoTrip(t);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return (trips != null)?(trips.size()):(0);
    }
}
