package com.example.cabbooking.rider.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.cabbooking.rider.R;
import com.example.cabbooking.rider.dto.JoinDto;
import com.example.cabbooking.rider.dto.TripDto;
import com.example.cabbooking.rider.dto.UserDto;
import com.example.cabbooking.rider.other.ApiCall;
import com.example.cabbooking.rider.other.Const;
import com.example.cabbooking.rider.other.MapConst;
import com.example.cabbooking.rider.other.MapTasks;
import com.example.cabbooking.rider.other.MySharedPref;
import com.example.cabbooking.rider.services.BgProcess;
import com.example.cabbooking.rider.services.GPSTracker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TrackingScreen extends BaseActivity implements OnMapReadyCallback {
    private ValueEventListener listener;
    private UserDto driver = null;
    private TripDto tripDto = null;

    private List<LatLng> markers = new ArrayList<>();
    private HashMap<String, String> markerLocation = new HashMap<>();
    private ProgressDialog pd = null;
    private TextView fare_txt,distance_txt,duration_txt,pay_type_txt,txt2,txt21,txt1,txt22,join_ride,veh_cat,veh_no;
    private String tripId;
    private  JoinDto joinDto = null;
    private GoogleMap mMap;
    private Polyline my_line = null;
    private int rtype = 0;
    private Location my_location1 = null;



    private void initPD() {
        pd = new ProgressDialog(this);
        pd.setCancelable(true);
        pd.setMessage(getString(R.string.loading));
    }



    @Override
    protected void setLocation1(Location location) {
        if(my_location1==null){
            my_location1 = location;
            startTracking();
        }
    }


    private void initUI(){
        fare_txt = findViewById(R.id.fare_txt);
        join_ride = findViewById(R.id.join_ride);
         distance_txt = findViewById(R.id.distance_txt);
         duration_txt = findViewById(R.id.duration_txt);
         pay_type_txt = findViewById(R.id.pay_type_txt);
        txt1 = findViewById(R.id.txt1);
        txt2 = findViewById(R.id.txt2);
        txt21 = findViewById(R.id.txt21);
        txt22 = findViewById(R.id.txt22);
        veh_cat = findViewById(R.id.veh_cat);
        veh_no = findViewById(R.id.veh_no);

        findViewById(R.id.fab2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(findViewById(R.id.ride_est_pop).getVisibility()==View.VISIBLE){
                    findViewById(R.id.ride_est_pop).setVisibility(View.GONE);
                    ((FloatingActionButton)findViewById(R.id.fab2)).setImageResource(R.drawable.up_icon);
                }
                else{
                    findViewById(R.id.ride_est_pop).setVisibility(View.VISIBLE);
                    ((FloatingActionButton)findViewById(R.id.fab2)).setImageResource(R.drawable.down_icon);
                }
            }
        });


    }
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_track;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPD();
        initUI();
        ((ImageView)findViewById(R.id.right_icon)).setImageResource(R.drawable.cross_icon);
        tripId = getIntent().getExtras().getString(Const.tripId);
        rtype = getIntent().getExtras().getInt(Const.rtype);
        ((ImageView)findViewById(R.id.right_icon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecivedNow(null);
            }
        });
        join_ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = getString(R.string.join_ride);
                if(join_ride.getText().toString().equals(getString(R.string.join_ride))) {
                    str = getString(R.string.join_ride);
                }
                else{
                    str = getString(R.string.cancel);
                }

                new AlertDialog.Builder(TrackingScreen.this)
                        .setTitle(str)
                        .setMessage("Are you sure you want to "+str+"?")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                JoinNow();
                            }
                        })

                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void JoinNow() {
        try{
            if(tripDto!=null && tripDto.getStatus().equals(Const.pending)) {
                UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata, "")
                        , UserDto.class);

                DatabaseReference mDatabaseJoin = FirebaseDatabase.getInstance().getReference(Const.join_tbl);

                if(join_ride.getText().toString().equals(getString(R.string.join_ride))) {
                    joinDto  = new JoinDto();
                    joinDto.setJoin_id(mDatabaseJoin.push().getKey());
                    joinDto.setDriver_id(tripDto.getDriver_id());
                    joinDto.setRider_id(ldata.getUserId());
                    joinDto.setTrip_id(tripDto.getTrip_id());
                    joinDto.setDatetime(Const.getUtcTime());
                    joinDto.setStatus("0");

                    mDatabaseJoin.child(joinDto.getJoin_id()).setValue(joinDto);
                    join_ride.setText(getString(R.string.cancel));

                    BgProcess.sendNotification(getApplicationContext()
                            , joinDto.getDriver_id(), tripDto.getTrip_id()
                            , ldata.getFname() + " " + ldata.getLname(), "has sent you join request");
                }
                else{
                    if(joinDto!=null){
                        mDatabaseJoin.child(joinDto.getJoin_id()).removeValue();
                        join_ride.setText(getString(R.string.join_ride));

                        BgProcess.sendNotification(getApplicationContext()
                                , joinDto.getDriver_id(), tripDto.getTrip_id()
                                , ldata.getFname() + " " + ldata.getLname(), "has cancelled you join request");
                    }
                }


            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void RecivedNow(View v){
        System.out.println("hejj---------------");
        if(tripDto!=null){
            System.out.println("hejj-------1--------"+tripDto.getStatus());
            if(tripDto.getStatus().equals(Const.accepted)
                    || tripDto.getStatus().equals(Const.received)
                    || tripDto.getStatus().equals(Const.pending)){
                new AlertDialog.Builder(this)
                        .setTitle("Cancel Ride")
                        .setMessage("Are you sure you want to cancel?")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.trip_tbl);
                                if(rtype==1){
                                    mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.trip_tbl1);
                                }
                                mDatabaseUser.child(tripId).child(Const.status).setValue(Const.canceled);
                                Toast.makeText(getApplicationContext(),getString(R.string.ride_canceled_msg),6).show();

                                UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata,"")
                                        , UserDto.class);

                                BgProcess.sendNotification(getApplicationContext(),
                                        driver.getUserId()
                                ,tripId
                                ,ldata.getFname()+" "+ldata.getLname()
                                ,"Has canceled ride");

                                finish();

                            }
                        })

                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                startTracking();
            }
        });
    }

    private void startTracking() {
        if (my_location1 != null) {
            final UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata,"")
                    , UserDto.class);
            DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.trip_tbl);
            if (rtype == 1) {
                mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.trip_tbl1);
            }
            mDatabaseUser.child(tripId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        tripDto = dataSnapshot.getValue(TripDto.class);

                        txt2.setText(tripDto.getFrom_str());

                        txt21.setText(tripDto.getTo_str());
                        fare_txt.setText(tripDto.getFare());
                        distance_txt.setText(tripDto.getDistance());
                        duration_txt.setText(tripDto.getDuration());
                        pay_type_txt.setText(tripDto.getPay_type());

                        if(rtype==0) {
                            join_ride.setVisibility(View.GONE);
                            findViewById(R.id.right_icon).setVisibility(View.VISIBLE);
                            if (tripDto.getStatus().equals(Const.accepted) == false
                                    && tripDto.getStatus().equals(Const.received) == false) {
                                getDirationsCall(tripDto.getFrom_lat_lng()
                                        , tripDto.getTo_lat_lng()
                                        , tripDto.getFrom_str()
                                        , tripDto.getTo_str());
                            }
                        }
                        else{
                            join_ride.setVisibility(View.VISIBLE);
                            findViewById(R.id.right_icon).setVisibility(View.GONE);


                            if(tripDto.getRider_id().contains(ldata.getUserId())){
                                join_ride.setText(getString(R.string.cancel));
                            }
                            else{
                                join_ride.setText(getString(R.string.join_ride));
                            }
                        }
                        if (tripDto.getStatus().equals(Const.completed)) {
                            ((ImageView) findViewById(R.id.right_icon)).setVisibility(View.GONE);
                        }
                        DatabaseReference mDatabaseJoin = FirebaseDatabase.getInstance().getReference(Const.user_tbl);
                        mDatabaseJoin.orderByChild(Const.rider_id).equalTo(ldata.getUserId()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    try {
                                        joinDto = dataSnapshot.getValue(JoinDto.class);
                                        if(joinDto!=null){
                                            join_ride.setText(getString(R.string.cancel));
                                        }
                                        else{
                                            join_ride.setText(getString(R.string.join_ride));
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();

                                        join_ride.setText(getString(R.string.join_ride));
                                    }
                                }
                                else{

                                    join_ride.setText(getString(R.string.join_ride));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                join_ride.setText(getString(R.string.join_ride));
                            }
                        });
                        DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.user_tbl);
                        mDatabaseUser.child(tripDto.getDriver_id()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    try {
                                        driver = dataSnapshot.getValue(UserDto.class);
                                        txt1.setText(driver.getFname() + " " + driver.getLname());
                                        txt22.setText(driver.getMobile());
                                        findViewById(R.id.veh_cat_rl).setVisibility(View.VISIBLE);
                                        findViewById(R.id.veh_no_rl).setVisibility(View.VISIBLE);
                                        veh_cat.setText(driver.getVeh_cat());
                                        veh_no.setText(driver.getVeh_no());



                                        String locs[] = driver.getLocation().split(",");

                                        if(rtype==0) {
                                            if (tripDto.getStatus().equals(Const.accepted)) {
                                                getDirationsCall(locs[0] + "," + locs[1]
                                                        , tripDto.getFrom_lat_lng()
                                                        , driver.getFname() + " " + driver.getLname()
                                                        , tripDto.getFrom_str());
                                            } else if (tripDto.getStatus().equals(Const.received)) {
                                                getDirationsCall(locs[0] + "," + locs[0]
                                                        , tripDto.getTo_lat_lng()
                                                        , driver.getFname() + " " + driver.getLname()
                                                        , tripDto.getTo_str());
                                            }
                                        }
                                        else{
                                            getDirationsCall(tripDto.getFrom_lat_lng()
                                                    , tripDto.getTo_lat_lng()
                                                    , tripDto.getFrom_str()
                                                    , tripDto.getTo_str());
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    private void getDirationsCall(String from ,String to,String fromName ,String toName) {
        try {

            mMap.clear();
            markers = new ArrayList<>();
            markerLocation = new HashMap<>();

            String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=" + from +
                        "&destination=" + to +
                        "&key=" + Const.google_api_key;


            LatLng latLng = MapTasks.coordinateForMarker(Double.parseDouble(from.split(",")[0])
                    , Double.parseDouble(from.split(",")[1]), 2, markerLocation);


            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
            markerLocation.put(from.split(",")[0] + "", "" + from.split(",")[1]);
            marker.setTitle(fromName);
            try {

                marker.setIcon(MapTasks.loadMarkImg(TrackingScreen.this, R.drawable.loc_icon1));
            } catch (Exception e) {
                e.printStackTrace();
            }

            markers.add(latLng);
            latLng = MapTasks.coordinateForMarker(Double.parseDouble(to.split(",")[0])
                    , Double.parseDouble(to.split(",")[1]), 2, markerLocation);


            marker = mMap.addMarker(new MarkerOptions().position(latLng));
            markerLocation.put(to.split(",")[0] + "", "" + to.split(",")[1]);
            marker.setTitle(toName);
            try {

                marker.setIcon(MapTasks.loadMarkImg(TrackingScreen.this, R.drawable.loc_icon2));
            } catch (Exception e) {
                e.printStackTrace();
            }

            markers.add(latLng);


            MapTasks.zoomCamera(TrackingScreen.this,mMap,markers);


                ApiCall.makeGET(this, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String mainObj) {
                        try {
                            System.out.println("asdfg---------------" + mainObj);
                            JSONObject jk = new JSONObject(mainObj);
                            if (jk.getString(Const.status).equals("OK")) {
                                JSONArray routes = jk.getJSONArray(MapConst.routes);
                                JSONObject overview_polyline = routes.getJSONObject(0).getJSONObject(MapConst.overview_polyline);
                                String encodedPoly = overview_polyline.getString(MapConst.points);
                                my_line = MapTasks.showPolyOnMap(TrackingScreen.this, encodedPoly, mMap, my_line);

                                JSONObject legs = routes.getJSONObject(0)
                                        .getJSONArray(MapConst.legs)
                                        .getJSONObject(0);



                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("asdfg----------error-----");
                    }
                });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onBackPressed() {
        finish();
    }

    public void NavNow(View v){
        finish();
    }

    @Override
    public void click1(int position) {

    }

    @Override
    public void click2(int position) {

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
    public void click8(int position) {

    }

    @Override
    public void click9(int position) {

    }

    @Override
    public void click10(int position) {

    }
}
