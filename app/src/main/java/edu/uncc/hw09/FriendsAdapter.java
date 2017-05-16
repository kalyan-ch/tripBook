package edu.uncc.hw09;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import edu.uncc.DataObj.UserProfile;

/**
 * Created by kalyan on 4/20/2017.
 */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolderOne>{

    ArrayList<UserProfile> friends;
    MainActivity activity;
    ProfileActivity pActivity;
    TripActivity tActivity;

    public FriendsAdapter(ArrayList<UserProfile> frinds, MainActivity activity, ProfileActivity pActivity, TripActivity tActivity){
        this.friends = frinds;
        this.activity = activity;
        this.pActivity = pActivity;
        this.tActivity = tActivity;
    }

    static class ViewHolderOne extends RecyclerView.ViewHolder{
        View v;

        public ViewHolderOne(View v1){
            super(v1);
            this.v = v1;
        }
    }

    @Override
    public ViewHolderOne onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        ViewHolderOne vOne = null;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_card, null);
        vOne = new ViewHolderOne(v);
        return vOne;
    }

    @Override
    public void onBindViewHolder(ViewHolderOne holder, int position) {
        View v1 = holder.v;
        final UserProfile u = friends.get(position);
        ((TextView)v1.findViewById(R.id.card_name)).setText(u.getFirstName()+" "+u.getLastName());
        Picasso.with(v1.getContext()).load(u.getDisplayPic()).into((ImageView)v1.findViewById(R.id.card_pic));
        ((Button)v1.findViewById(R.id.card_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activity!=null){
                    activity.goToProfile(u.getEmail());
                }else if(pActivity!=null){
                    pActivity.goToProfile(u.getEmail());
                }else if(tActivity!=null){
                    tActivity.goToProfile(u.getEmail());
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return ((null!=friends)? (friends.size()) : (0)) ;
    }
}
