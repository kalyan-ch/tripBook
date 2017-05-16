package edu.uncc.hw09;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import edu.uncc.DataObj.UserProfile;

/**
 * Created by kalyan on 4/21/2017.
 */

public class TripPeopleAdapter extends RecyclerView.Adapter<TripPeopleAdapter.MyViewHolder>{

    ArrayList<UserProfile> users;

    public TripPeopleAdapter(ArrayList<UserProfile> users) {
        this.users = users;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        View v;

        public MyViewHolder(View v1){
            super(v1);
            this.v = v1;
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_people, null);
        MyViewHolder vOne = new MyViewHolder(v);
        return vOne;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final View v = holder.v;
        final UserProfile use = users.get(position);
        TextView tv = (TextView)v.findViewById(R.id.trip_people_name);
        (tv).setText(use.getFirstName()+" "+use.getLastName());
        Picasso.with(v.getContext()).load(use.getDisplayPic()).into((ImageView)v.findViewById(R.id.trip_people_pic));
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                use.setOnTrip(!use.isOnTrip());
                v.setBackgroundColor(use.isOnTrip()? Color.GRAY : Color.WHITE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (users != null)?(users.size()):(0);
    }
}
