package com.example.cabbooking.rider.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cabbooking.rider.R;
import com.example.cabbooking.rider.adapters.RidesAdp;
import com.example.cabbooking.rider.adapters.RidesAdp1;
import com.example.cabbooking.rider.dto.ReviewDto;
import com.example.cabbooking.rider.dto.TripDto;
import com.example.cabbooking.rider.dto.UserDto;
import com.example.cabbooking.rider.intr.AdpListner;
import com.example.cabbooking.rider.other.Const;
import com.example.cabbooking.rider.other.MySharedPref;
import com.example.cabbooking.rider.other.Validations;
import com.example.cabbooking.rider.services.BgProcess;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyRides extends AppCompatActivity implements AdpListner {

    private RecyclerView lv1;
    private List<TripDto> l1 = new ArrayList<>();
    private BottomSheetDialog dialog1 = null;
    private ProgressDialog pd;
    private UserDto userDto = null;

    private void initPD() {
        pd = new ProgressDialog(this);
        pd.setCancelable(true);
        pd.setMessage(getString(R.string.loading));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rides);
        userDto = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(),Const.ldata,"")
                , UserDto.class);
        initPD();
        initUI();
        loadData();
    }

    private void loadData() {
        try{
            DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.trip_tbl);
            mDatabaseUser.orderByChild(Const.rider_id).equalTo(userDto.getUserId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null){
                        l1 = new ArrayList<>();
                        for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            l1.add(dataSnapshot1.getValue(TripDto.class));
                        }

                        addAdp();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void addAdp() {

        Collections.reverse(l1);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        lv1.setLayoutManager(mLayoutManager);
        lv1.setItemAnimator(new DefaultItemAnimator());
        lv1.setNestedScrollingEnabled(false);
        RidesAdp1 mainAdp = new RidesAdp1(this, l1);
        lv1.setAdapter(mainAdp);
    }

    private void initUI() {
       lv1  = findViewById(R.id.lv1);




    }

    public void LoginNow(View v) {
        finish();
    }


    public void NavNow(View v) {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void click1(int position) {
        if(l1.get(position).getStatus().equals(Const.completed)){
            giveFeedback(l1.get(position));
        }
        else {
            Intent intent = new Intent(getApplicationContext(), TrackingScreen.class);
            intent.putExtra(Const.tripId, l1.get(position).getTrip_id());
            intent.putExtra(Const.rtype, 0);
            startActivity(intent);
        }
    }

    private void giveFeedback(final TripDto tripDto) {
        try{
            if(dialog1!=null && dialog1.isShowing()){
                dialog1.dismiss();
            }


            dialog1 = new BottomSheetDialog(MyRides.this);
            dialog1.setCancelable(true);
            dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog1.setContentView(R.layout.feedback_pop);

            ImageView right_icon = dialog1.findViewById(R.id.right_icon);

            final EditText et1 = dialog1.findViewById(R.id.et1);
            final RatingBar rt1 = dialog1.findViewById(R.id.rt1);


            right_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if (Validations.valEdit(MyRides.this, et1, getString(R.string.feedback))
                                && Const.isOnline(MyRides.this)) {
                           pd.show();
                            final DatabaseReference mDatabaseRating = FirebaseDatabase.getInstance().getReference(Const.review_tbl);
                            ReviewDto reviewDto = new ReviewDto();

                            reviewDto.setRw_id(mDatabaseRating.push().getKey());
                            reviewDto.setDatetime(Const.getUtcTime());
                            reviewDto.setFeedback(et1.getText().toString());
                            reviewDto.setRating(rt1.getRating()+"");
                            reviewDto.setRider_id(userDto.getUserId());
                            reviewDto.setDriver_id(tripDto.getDriver_id());
                            reviewDto.setTrip_id(tripDto.getTrip_id());
                            reviewDto.setTrip_type(tripDto.getRide_type()+"");

                            mDatabaseRating.child(reviewDto.getTrip_id()).setValue(reviewDto);

                            BgProcess.sendNotification(getApplicationContext()
                                    , tripDto.getDriver_id(), tripDto.getTrip_id()
                                    , userDto.getFname() + " " + userDto.getLname(), "has provided you feedback");


                            Toast.makeText(getApplicationContext(),getString(R.string.th_for_feedback),6).show();
                            pd.dismiss();
                            dialog1.dismiss();
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            dialog1.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void OtpPop(final TripDto tripDto){
        if(dialog1!=null && dialog1.isShowing()){
            dialog1.dismiss();
        }


        dialog1 = new BottomSheetDialog(MyRides.this);
        dialog1.setCancelable(true);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.ride_est_pop);

        ImageView right_icon = dialog1.findViewById(R.id.right_icon);

        final TextView txt1 = dialog1.findViewById(R.id.txt1);
        TextView txt2 = dialog1.findViewById(R.id.txt2);
        TextView txt21 = dialog1.findViewById(R.id.txt21);

        final TextView fare_txt = dialog1.findViewById(R.id.fare_txt);
        final TextView distance_txt = dialog1.findViewById(R.id.distance_txt);
        final TextView duration_txt = dialog1.findViewById(R.id.duration_txt);
        final TextView pay_type_txt = dialog1.findViewById(R.id.pay_type_txt);
        RatingBar rt1 = dialog1.findViewById(R.id.rt1);

        txt2.setText(tripDto.getFrom_str());
        txt21.setText(tripDto.getTo_str());

        fare_txt.setText(tripDto.getFare());
        distance_txt.setText(tripDto.getDistance());
        duration_txt.setText(tripDto.getDuration());
        pay_type_txt.setText(tripDto.getPay_type());

        DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.user_tbl);
        mDatabaseUser.child(tripDto.getDriver_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    UserDto userDto = dataSnapshot.getValue(UserDto.class);
                    txt1.setText(userDto.getFname()+" "+userDto.getLname());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        right_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),TrackingScreen.class);
                intent.putExtra(Const.tripId,tripDto.getTrip_id());
                intent.putExtra(Const.rtype,0);
                startActivity(intent);
            }
        });

        dialog1.show();
    }

    @Override
    public void click2(int position) {
        OtpPop(l1.get(position));
    }

    @Override
    public void click3(int position) {

    }

    @Override
    public void click4(int position) {

    }

    @Override
    public void click5(int position) {

    }

    @Override
    public void click6(int position) {

    }

    @Override
    public void click7(int position) {

    }

    @Override
    public void click8(int position) {

    }

    @Override
    public void click9(int position) {

    }

    @Override
    public void click10(int position) {

    }
}
