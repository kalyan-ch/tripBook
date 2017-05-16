package edu.uncc.hw09;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import edu.uncc.DataObj.Trip;
import edu.uncc.DataObj.TripPlace;

/**
 * Created by kalyan on 5/1/2017.
 */

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.MyViewHolder> {

    ArrayList<TripPlace> places;
    TripActivity activity;

    public PlaceAdapter(ArrayList<TripPlace> places, TripActivity activity) {
        this.places = places;
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_card, null);
        PlaceAdapter.MyViewHolder vOne = new PlaceAdapter.MyViewHolder(v);
        return vOne;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        View v1 = holder.v;
        final TripPlace t = places.get(position);
        ((TextView)v1.findViewById(R.id.place_name)).setText(t.getName());
        ((ImageButton)v1.findViewById(R.id.place_del)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.deletePlace(t);
            }
        });
        ((ImageButton)v1.findViewById(R.id.place_nav)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.goToMaps(t.getLat()+","+t.getLng());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (places != null)?(places.size()):(0);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        View v;

        public MyViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}
