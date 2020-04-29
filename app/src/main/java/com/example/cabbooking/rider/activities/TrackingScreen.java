package com.example.cabbooking.rider.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

public class TrackingScreen extends AppCompatActivity implements OnMapReadyCallback {
    private ValueEventListener listener;
    private UserDto driver = null;
    private TripDto tripDto = null;

    private List<LatLng> markers = new ArrayList<>();
    private HashMap<String, String> markerLocation = new HashMap<>();
    private ProgressDialog pd = null;
    private TextView fare_txt,distance_txt,duration_txt,pay_type_txt,txt2,txt21,txt1;
    private String tripId;
    private Location my_location;
    private GoogleMap mMap;
    private GPSTracker gps = null;
    private Polyline my_line = null;
    private int rtype = 0;


    String str[] = new String[]{
            Manifest.permission.INTERNET
            , Manifest.permission.ACCESS_NETWORK_STATE
            , Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.ACCESS_FINE_LOCATION
    };

    private void initPD() {
        pd = new ProgressDialog(this);
        pd.setCancelable(true);
        pd.setMessage(getString(R.string.loading));
    }

    private boolean checkIfAlreadyhavePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true;
        } else {
            return false;
        }
    }

    private void requestReadPhoneStatePermission() {
        ActivityCompat.requestPermissions(TrackingScreen.this, str,
                1);
    }
    private void initUI(){
         fare_txt = findViewById(R.id.fare_txt);
         distance_txt = findViewById(R.id.distance_txt);
         duration_txt = findViewById(R.id.duration_txt);
         pay_type_txt = findViewById(R.id.pay_type_txt);
        txt1 = findViewById(R.id.txt1);
        txt2 = findViewById(R.id.txt2);
        txt21 = findViewById(R.id.txt21);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata,"")
                , UserDto.class);
        startTracking(ldata);
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startTracking(UserDto ldata) {
        if(checkIfAlreadyhavePermission()) {
            if (gps == null) {
                try {
                    gps = new GPSTracker(TrackingScreen.this, ldata);

                    // check if GPS enabled
                    if (gps.canGetLocation()) {

                         my_location = gps.getLocation();

                        DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.trip_tbl);
                        if(rtype==1){
                            mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.trip_tbl1);
                        }
                        mDatabaseUser.child(tripId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue()!=null){
                                    tripDto = dataSnapshot.getValue(TripDto.class);

                                    txt2.setText(tripDto.getFrom_str());

                                    txt21.setText(tripDto.getTo_str());
//                                    if(rtype==1) {
//                                        txt3.setVisibility(View.VISIBLE);
//                                        txt3.setText(tripDto.getDate_str());
//                                    }
//                                    else{
//                                        txt3.setVisibility(View.GONE);
//                                        txt3.setText("");
//                                    }
                                    fare_txt.setText(tripDto.getFare());
                                    distance_txt.setText(tripDto.getDistance());
                                    duration_txt.setText(tripDto.getDuration());
                                    pay_type_txt.setText(tripDto.getPay_type());


                                    if(tripDto.getStatus().equals(Const.accepted)==false
                                    && tripDto.getStatus().equals(Const.received)==false){
                                        getDirationsCall(tripDto.getFrom_lat_lng()
                                                , tripDto.getTo_lat_lng()
                                                , tripDto.getFrom_str()
                                                , tripDto.getTo_str());
                                    }
                                    if(tripDto.getStatus().equals(Const.completed)){
                                        ((ImageView)findViewById(R.id.right_icon)).setVisibility(View.GONE);
                                    }

                                    DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.user_tbl);
                                    mDatabaseUser.child(tripDto.getDriver_id()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.getValue()!=null){
                                                try {
                                                    driver = dataSnapshot.getValue(UserDto.class);
                                                    txt1.setText(driver.getFname() + " " + driver.getLname());

                                                    JSONObject jk = new JSONObject(driver.getLocation());


                                                    if (tripDto.getStatus().equals(Const.accepted)) {
                                                        getDirationsCall(jk.getDouble("mLatitude") + "," +jk.getDouble("mLongitude")
                                                                , tripDto.getFrom_lat_lng()
                                                        ,driver.getFname()+" "+driver.getLname()
                                                        ,tripDto.getFrom_str());
                                                    }
                                                    else if (tripDto.getStatus().equals(Const.received)) {
                                                        getDirationsCall(jk.getDouble("mLatitude") + "," + jk.getDouble("mLongitude")
                                                                , tripDto.getTo_lat_lng()
                                                                ,driver.getFname()+" "+driver.getLname()
                                                                ,tripDto.getTo_str());
                                                    }
                                                }
                                                catch (Exception e){
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
                        
                    } else {

                        gps.showSettingsAlert();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            requestReadPhoneStatePermission();
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

                marker.setIcon(MapTasks.loadMarkImg(TrackingScreen.this, R.drawable.mark1));
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

                marker.setIcon(MapTasks.loadMarkImg(TrackingScreen.this, R.drawable.mark1));
            } catch (Exception e) {
                e.printStackTrace();
            }

            markers.add(latLng);


            MapTasks.zoomCamera(mMap,markers);


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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(),Const.ldata,"")
                , UserDto.class);
        startTracking(ldata);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void NavNow(View v){
        finish();
    }
}
