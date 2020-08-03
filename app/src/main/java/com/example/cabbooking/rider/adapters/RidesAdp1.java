package com.example.cabbooking.rider.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cabbooking.rider.R;
import com.example.cabbooking.rider.dto.ReviewDto;
import com.example.cabbooking.rider.dto.TripDto;
import com.example.cabbooking.rider.intr.AdpListner;
import com.example.cabbooking.rider.other.Const;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.List;


public class RidesAdp1 extends RecyclerView.Adapter<RidesAdp1.MyViewHolder> {

    private List<TripDto> l1;
    private Activity activity;
    private AdpListner adpListner;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout accept_bt;
        public RelativeLayout rl1;
        public TextView txt1,txt2,txt3,track_txt;
        public MyViewHolder(View view) {
            super(view);
            rl1 = view.findViewById(R.id.rl1);
            accept_bt = view.findViewById(R.id.accept_bt);
            txt1 = view.findViewById(R.id.txt1);
            txt2 = view.findViewById(R.id.txt2);
            txt3 = view.findViewById(R.id.txt3);
            track_txt = view.findViewById(R.id.track_txt);
        }
    }


    public RidesAdp1(Activity activity, List<TripDto> l1) {
        this.l1 = l1;
        this.activity = activity;
        adpListner = (AdpListner) activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ride_item1, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        try {

            holder.txt1.setText(l1.get(holder.getAdapterPosition()).getFrom_str().substring(0,20));
            holder.txt2.setText(l1.get(holder.getAdapterPosition()).getTo_str().substring(0,20));
            holder.txt3.setText(Const.getLocalTime(l1.get(holder.getAdapterPosition()).getDatetime()));

            if(l1.get(holder.getAdapterPosition()).getStatus().equals(Const.completed)){
                holder.track_txt.setText(activity.getString(R.string.give_review));
                final DatabaseReference mDatabaseRating = FirebaseDatabase.getInstance().getReference(Const.review_tbl);
                mDatabaseRating.child(l1.get(holder.getAdapterPosition()).getTrip_id()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()!=null){
                            ReviewDto reviewDto = dataSnapshot.getValue(ReviewDto.class);
                            if(reviewDto!=null){
                                holder.accept_bt.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            holder.accept_bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adpListner.click1(holder.getAdapterPosition());
                }
            });

            holder.rl1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adpListner.click2(holder.getAdapterPosition());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public int getItemCount() {
        return l1.size();
    }

}