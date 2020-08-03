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


public class RidesAdp extends RecyclerView.Adapter<RidesAdp.MyViewHolder> {

    private List<TripDto> l1;
    private Activity activity;
    private AdpListner adpListner;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout accept_bt;
        public TextView txt1,txt2,txt3;
        public RelativeLayout rl1;


        public MyViewHolder(View view) {
            super(view);
            rl1 = view.findViewById(R.id.rl1);
            txt1 = view.findViewById(R.id.txt1);
            txt2 = view.findViewById(R.id.txt2);
            txt3 = view.findViewById(R.id.txt3);
            accept_bt = view.findViewById(R.id.accept_bt);
        }
    }


    public RidesAdp(Activity activity, List<TripDto> l1) {
        this.l1 = l1;
        this.activity = activity;
        adpListner = (AdpListner) activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ride_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        try {
            TripDto tripDto = l1.get(holder.getAdapterPosition());

            holder.txt1.setText(tripDto.getFrom_str().substring(0,20));
            holder.txt2.setText(tripDto.getTo_str().substring(0,20));
            holder.txt3.setText(Const.getLocalTime(tripDto.getDatetime()));

            if(l1.get(holder.getAdapterPosition()).getStatus().equals(Const.completed)){
                holder.accept_bt.setVisibility(View.VISIBLE);
                final DatabaseReference mDatabaseRating = FirebaseDatabase.getInstance().getReference(Const.review_tbl);
                mDatabaseRating.child(tripDto.getTrip_id()).addValueEventListener(new ValueEventListener() {
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
                    holder.rl1.performClick();
                }
            });

            holder.rl1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adpListner.click1(holder.getAdapterPosition());
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