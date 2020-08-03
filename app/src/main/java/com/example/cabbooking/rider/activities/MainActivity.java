package com.example.cabbooking.rider.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import com.android.volley.VolleyError;
import com.example.cabbooking.rider.R;
import com.example.cabbooking.rider.adapters.AutoSuggestAdapter1;
import com.example.cabbooking.rider.adapters.CatAdp;
import com.example.cabbooking.rider.adapters.DriverAdp;
import com.example.cabbooking.rider.adapters.MenuAdp;
import com.example.cabbooking.rider.dto.CatDto;
import com.example.cabbooking.rider.dto.MenuData;
import com.example.cabbooking.rider.dto.PriceDto;
import com.example.cabbooking.rider.dto.TripDto;
import com.example.cabbooking.rider.dto.UserConst;
import com.example.cabbooking.rider.dto.UserDto;
import com.example.cabbooking.rider.intr.AdpListner;
import com.example.cabbooking.rider.other.ApiCall;
import com.example.cabbooking.rider.other.Const;
import com.example.cabbooking.rider.other.MapConst;
import com.example.cabbooking.rider.other.MapTasks;
import com.example.cabbooking.rider.other.MySharedPref;
import com.example.cabbooking.rider.other.Validations;
import com.example.cabbooking.rider.services.BgProcess;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends BaseActivity implements  OnMapReadyCallback {

    private DrawerLayout drawer;
    private Polyline my_line = null;
    private NavigationView navigationView;
    private TextView search_et, search_et1;
    private BottomSheetDialog dialog1 = null,dialog2 = null;
    private PriceDto priceDto = null;
    private int SEARCH_ET = 0,SEARCH_ET1 = 1;
//    private TextView category_txt;
    private String date_str = "";
    private int ride_type = 0;
    private boolean active = false;
    private boolean start = false,economy = false,comfort = false;
    private DatePickerDialog datePickerDialog = null;
    private RecyclerView menu_lv1;
//    private List<CatDto> categories = new ArrayList<>();
    private List<UserDto> drivers1 = new ArrayList<>();
    private ValueEventListener listener = null;
    private boolean go = true;
    private List<Location> drivers = new ArrayList<>();
    private List<String> driverIds = new ArrayList<>();
    private List<Marker> driverMarkers = new ArrayList<>();
    private List<LatLng> markers = new ArrayList<>();
    private GoogleMap mMap;
    private List<MenuData> menus = new ArrayList<>();
    private ProgressDialog pd;
    private Location my_location1 = null;
    private JSONObject legs = null;
    private HashMap<String, String> markerLocation = new HashMap<>();


    public void ProNow(View v){
        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main1;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPD();
        initUI();
        setMenu();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        loadCats();
        loadPrice();
    }

    private void setMenu() {
        try {
            menus = new ArrayList<>();
            menus.add(new MenuData(getString(R.string.home),R.drawable.home_icon));
            menus.add(new MenuData(getString(R.string.ride_history),R.drawable.history_icon));
            menus.add(new MenuData(getString(R.string.schedule_ride),R.drawable.user_icon));
            menus.add(new MenuData(getString(R.string.future_ride),R.drawable.future_icon));
            menus.add(new MenuData(getString(R.string.settings),R.drawable.settings_icon));
            menus.add(new MenuData(getString(R.string.language),R.drawable.about_icon));
            menus.add(new MenuData(getString(R.string.logout),R.drawable.logout_icon));



            MenuAdp catAdp = new MenuAdp(MainActivity.this,menus);

            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
            menu_lv1.setLayoutManager(layoutManager);
            menu_lv1.setItemAnimator(new DefaultItemAnimator());
            menu_lv1.setNestedScrollingEnabled(false);
            menu_lv1.setAdapter(catAdp);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void loadPrice() {
        try {
            final DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.price_tbl);
            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        priceDto = dataSnapshot.getValue(PriceDto.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPD() {
        pd = new ProgressDialog(this);
        pd.setCancelable(true);
        pd.setMessage(getString(R.string.loading));
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        active = true;
//        try {
//            Const.hideKeyboardFrom(MainActivity.this, findViewById(R.id.rl2));
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
        try{
            UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata, "")
                    , UserDto.class);

            ((TextView)navigationView.getRootView().findViewById(R.id.txt1_head)).setText(ldata.getFname()+" "+ldata.getLname());
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(Const.user_tbl);
            FirebaseInstanceId instanceID = FirebaseInstanceId.getInstance();
            String token = instanceID.getToken();
            mDatabase.child(ldata.getUserId()).child(Const.token).setValue(token);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try{
            if(mMap!=null){
                System.out.println("get-----------stt----");
                UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata, "")
                        , UserDto.class);
                if(ldata.getLocation()!=null
                        && ldata.getLocation().length()>0) {
                    my_location1 =  Const.getLocation(ldata.getLocation());
                    getNearByDrivers(my_location1);
                }
                else{
                    if(my_location1!=null){
                        getNearByDrivers(my_location1);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getDirationsCall() {
        try {
            pd.show();
            String url = "";
            if (my_location1 != null) {
                url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=" + search_et.getText().toString() +
                        "&destination=" + search_et1.getText().toString() +
                        "&key=" + Const.google_api_key;

                ApiCall.makeGET(this, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String mainObj) {
                        pd.dismiss();
                        try {
                            mMap.setOnCameraChangeListener(null);
                            System.out.println("asdfg---------------" + mainObj);
                            JSONObject jk = new JSONObject(mainObj);
                            if (jk.getString(Const.status).equals("OK")) {
                                JSONArray routes = jk.getJSONArray(MapConst.routes);
                                JSONObject overview_polyline = routes.getJSONObject(0).getJSONObject(MapConst.overview_polyline);
                                String encodedPoly = overview_polyline.getString(MapConst.points);
                                my_line = MapTasks.showPolyOnMap(MainActivity.this, encodedPoly, mMap, my_line);
                                go = false;
                                legs = routes.getJSONObject(0)
                                        .getJSONArray(MapConst.legs)
                                        .getJSONObject(0);
                                System.out.println("testr-----0---------");

                                final LatLng myLoc = new LatLng(my_location1.getLatitude(), my_location1.getLongitude());
//
                                Marker marker1 = mMap.addMarker(new MarkerOptions().position(myLoc));
                                markerLocation.put(my_location1.getLatitude() + "", "" + my_location1.getLongitude());
                                try {
                                    marker1.setIcon(MapTasks.loadMarkImg(MainActivity.this, R.drawable.loc_icon1));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                UserDto driver = Const.getNerestDriver(my_location1,drivers1,start,economy,comfort);


                                if(driver!=null) {
//                                    showRideEstPop(legs,driver);

                                    System.out.println("testr----------ff-----");
                                    showFirstRidePop(legs,driver);

                                    System.out.println("testr----------ff1-----");
//                                    if (dialog2 != null && dialog2.isShowing()) {
//                                        dialog2.dismiss();
//                                        dialog2 = null;
//                                    }
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),getString(R.string.no_driver_avail),6).show();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        System.out.println("testr----------error-----");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void chooseDriverPop(List<UserDto> drivers, JSONObject legs) {
//        try{
//            dialog1 = new BottomSheetDialog(MainActivity.this,R.style.CustomBottomSheetDialogTheme);
//            dialog1.setCancelable(true);
//            dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog1.setContentView(R.layout.list_pop);
//            dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//
//            RecyclerView lv1 = dialog1.findViewById(R.id.lv1);
//
//            DriverAdp catAdp = new DriverAdp(MainActivity.this, drivers,legs,priceDto);
//
//            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
//            lv1.setLayoutManager(mLayoutManager);
//            lv1.setItemAnimator(new DefaultItemAnimator());
//            lv1.setNestedScrollingEnabled(false);
//
//            lv1.setAdapter(catAdp);
//
//            dialog1.show();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//    }

//    private void loadCats() {
//        final DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.cat_tbl);
//        mDatabaseUser.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getValue() != null) {
//                    categories = new ArrayList<>();
//                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
//                        CatDto catDto = dataSnapshot1.getValue(CatDto.class);
//                        categories.add(catDto);
//                    }
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void showFirstRidePop(final JSONObject ride_data, final UserDto driverDt) {
        try {

            System.out.println("testr----------ff--2---");
            dialog2 = new BottomSheetDialog(MainActivity.this,R.style.CustomBottomSheetDialogTheme);
            dialog2.setCancelable(true);
            dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog2.setContentView(R.layout.first_ride_pop);
            dialog2.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


            System.out.println("testr----------ff--3---");
            Button right_icon = dialog2.findViewById(R.id.right_icon);

            final TextView txt1 = dialog2.findViewById(R.id.txt1);
            final TextView txt2 = dialog2.findViewById(R.id.txt2);

            final TextView t1 = dialog2.findViewById(R.id.t1);
            final TextView t2 = dialog2.findViewById(R.id.t2);
            final TextView t3 = dialog2.findViewById(R.id.t3);


            System.out.println("testr----------ff--4---");
            double fair =  Double.parseDouble(priceDto.getPrice_pkm());

            t1.setText("$"+fair);
            t2.setText("$"+(fair+50));
            t3.setText("$"+(fair+100));

            final CardView card1 = dialog2.findViewById(R.id.card1);
            final CardView card2 = dialog2.findViewById(R.id.card2);
            final CardView card3 = dialog2.findViewById(R.id.card3);

            System.out.println("testr----------ff--5---");
            Const.setChecked(MainActivity.this,dialog2,start,economy,comfort);
            Const.setChkHandler(MainActivity.this,card1,card2,card3);


            txt1.setText(search_et.getText().toString().substring(0,20));
            txt2.setText(search_et1.getText().toString().substring(0,20));

            System.out.println("testr----------ff--6---");
            right_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

//
                    start  = Const.getChecked(card1);
                    economy  = Const.getChecked(card2);
                    comfort  = Const.getChecked(card3);

                    ride_type = 0;
                    showRideEstPop(ride_data,driverDt);

                }
            });

            System.out.println("testr----------ff--7---");
            dialog2.show();

            System.out.println("testr----------ff--8---");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void shceduleRide() {
        if (findViewById(R.id.search_rl1).getVisibility() == View.GONE) {
            if (search_et.getText().toString().length() > 0) {
                if(ride_type==1){
                    datePickerDialog.show();
                }
                else {
                    getDirationsCall();
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.where_hint), 6).show();
            }
        } else {
            if (search_et1.getText().toString().length() > 0) {
                if(ride_type==1){
                    datePickerDialog.show();
                }
                else {
                    getDirationsCall();
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.where_hint), 6).show();
            }
        }
    }


    private void showRideEstPop(final JSONObject ride_data, final UserDto driverDt) {
        try {
            if(dialog2!=null && dialog2.isShowing()){
                dialog2.dismiss();
            }
            dialog2 = new BottomSheetDialog(MainActivity.this,R.style.CustomBottomSheetDialogTheme);
            dialog2.setCancelable(true);
            dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);

            View contentView = View.inflate(MainActivity.this, R.layout.ride_est_pop, null);
            dialog2.setContentView(contentView);

            ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));



            ImageView right_icon = dialog2.findViewById(R.id.right_icon);

            TextView txt1 = dialog2.findViewById(R.id.txt1);
            TextView txt2 = dialog2.findViewById(R.id.txt2);
            TextView txt21 = dialog2.findViewById(R.id.txt21);

            final TextView fare_txt = dialog2.findViewById(R.id.fare_txt);
            final TextView distance_txt = dialog2.findViewById(R.id.distance_txt);
            final TextView duration_txt = dialog2.findViewById(R.id.duration_txt);
            final TextView pay_type_txt = dialog2.findViewById(R.id.pay_type_txt);
            RatingBar rt1 = dialog2.findViewById(R.id.rt1);

            System.out.println("testr-----1---------");

            System.out.println("testr-----2---------");

            if(driverDt!=null) {
                txt1.setText(driverDt.getFname() + " " + driverDt.getLname());


                distance_txt.setText(ride_data.getJSONObject(MapConst.distance).getString(MapConst.text));
                duration_txt.setText(ride_data.getJSONObject(MapConst.duration).getString(MapConst.text));

                txt2.setText(ride_data.getString(MapConst.start_address));

                txt21.setText(ride_data.getString(MapConst.end_address));

                int distance_val = (int) Math.round(ride_data.getJSONObject(MapConst.distance).getLong(MapConst.value) / 1000);

                double fair = distance_val * Double.parseDouble(priceDto.getPrice_pkm());

                double dis = Double.parseDouble(priceDto.getDiscount());

                if (dis > 0) {
                    fair = fair - ((fair * dis) / 100);
                }

                fare_txt.setText("$" + String.format("%.2f", fair));



                right_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            System.out.println("testbk----------error---2--");
                            DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.trip_tbl);
                            if(ride_type==1){
                                mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.trip_tbl1);
                            }

                            UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata, "")
                                    , UserDto.class);

                            JSONObject start_location = ride_data.getJSONObject(MapConst.start_location);
                            JSONObject end_location = ride_data.getJSONObject(MapConst.end_location);

                            TripDto tripDto = new TripDto();
                            tripDto.setTrip_id(mDatabaseUser.push().getKey());
                            tripDto.setDriver_id(driverDt.getUserId());
                            tripDto.setDate_str(date_str);
                            tripDto.setRider_id(ldata.getUserId());
                            tripDto.setFrom_str(ride_data.getString(MapConst.start_address));
                            tripDto.setTo_str(ride_data.getString(MapConst.end_address));
                            tripDto.setDistance(distance_txt.getText().toString());
                            tripDto.setFare(fare_txt.getText().toString());
                            tripDto.setPay_type(pay_type_txt.getText().toString());
                            tripDto.setStatus(Const.pending);
                            tripDto.setDuration(duration_txt.getText().toString());
                            tripDto.setRide_type(ride_type);
                            tripDto.setTo_str(ride_data.getString(MapConst.end_address));
                            tripDto.setFrom_lat_lng(start_location.getDouble(MapConst.lat) + "," + start_location.getDouble(MapConst.lng));
                            tripDto.setTo_lat_lng(end_location.getDouble(MapConst.lat) + "," + end_location.getDouble(MapConst.lng));
                            tripDto.setDatetime(Const.getUtcTime());

                            mDatabaseUser.child(tripDto.getTrip_id()).setValue(tripDto);

                            BgProcess.sendNotification(getApplicationContext(), driverDt.getUserId(), tripDto.getTrip_id(), ldata.getFname() + " " + ldata.getLname(), "has hired you for ride");



                            Intent intent = new Intent(getApplicationContext(), TrackingScreen.class);
                            intent.putExtra(Const.tripId, tripDto.getTrip_id());
                            if(ride_type==1){
                                intent.putExtra(Const.rtype,1);
                            }
                            else{
                                intent.putExtra(Const.rtype,0);
                            }
                            startActivity(intent);


                            getNearByDrivers(my_location1);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                dialog2.show();
            }
            else{
                Toast.makeText(getApplicationContext(),"No Driver is avail now please wait",6).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                try {
                    UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata, "")
                            , UserDto.class);
                    if (ldata.getLocation() != null
                            && ldata.getLocation().length() > 0) {
                        try {
                            my_location1 = Const.getLocation(ldata.getLocation());
                            getNearByDrivers(my_location1);
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (my_location != null) {
                                my_location1 = my_location;
                                getNearByDrivers(my_location1);
                            }
                        }
                    } else {
                        if (my_location != null) {
                            my_location1 = my_location;
                            getNearByDrivers(my_location1);
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (my_location != null) {
                        my_location1 = my_location;
                        getNearByDrivers(my_location1);
                    }
                }
            }
        });
    }


    private class MyBgTask extends AsyncTask {


        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        search_et.setText(MapTasks.getAddress(MainActivity.this
                                , my_location1.getLatitude(), my_location1.getLongitude()));
                    }
                });

            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }


    private void getNearByDrivers(final Location location1) {
        try {
            new MyBgTask().execute();
//            pd.show();
            markers = new ArrayList<>();
            final LatLng myLoc = new LatLng(location1.getLatitude(), location1.getLongitude());

            markers.add(myLoc);

            final DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.user_tbl);
            listener = new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mDatabaseUser.removeEventListener(listener);
                    if (dataSnapshot.getValue() != null) {
                        drivers = new ArrayList<>();
                        drivers1 = new ArrayList<>();
                        driverIds = new ArrayList<>();
                        driverMarkers = new ArrayList<>();
                        markerLocation = new HashMap<>();
                        mMap.clear();

                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            try {
                                UserDto userDto = dataSnapshot1.getValue(UserDto.class);
                                if (userDto.getStatus().equals("1")
                                        && userDto.getLocation().length()>0) {
                                    Location location = MapTasks.getLocBYLatLng(userDto.getLocation());

                                    System.out.println("test_driver------1--------"+location1.distanceTo(location));

                                    if (location1.distanceTo(location) < (10 * 1000)) {
                                        drivers.add(location);
                                        drivers1.add(userDto);
                                        driverIds.add(dataSnapshot1.getKey());

                                        LatLng latLng = MapTasks.coordinateForMarker(location.getLatitude(), location.getLongitude(), 2, markerLocation);


                                        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
                                        markerLocation.put(location.getLatitude() + "", "" + location.getLongitude());
                                        System.out.println("data----1------" + marker.getId());

                                        try {

                                            marker.setIcon(MapTasks.loadMarkImg(MainActivity.this, R.drawable.car_icon));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        driverMarkers.add(marker);

                                        markers.add(myLoc);
                                        System.out.println("data----done------" + marker.getId());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
//                                UserDto userDto = dataSnapshot1.getValue(UserDto.class);
//                                drivers.add(null);
//                                drivers1.add(userDto);
//                                driverIds.add(dataSnapshot1.getKey());
                            }

                            MapTasks.zoomCamera(MainActivity.this,mMap, markers);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mDatabaseUser.removeEventListener(listener);
                    MapTasks.zoomCamera(MainActivity.this,mMap, markers);
                }
            };

            mDatabaseUser.orderByChild(UserConst.user_type).equalTo(Const.driver).addValueEventListener(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        search_et = findViewById(R.id.search_et);
        search_et1 = findViewById(R.id.search_et1);

        search_et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(active) {
                    Intent i = new Intent(MainActivity.this, LocationChooser.class);
                    i.putExtra(Const.title, getString(R.string.your_location));
                    try {
                        i.putExtra(Const.data, my_location1.getLatitude() + "," + my_location1.getLongitude());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(i, SEARCH_ET);
                }
                return true;
            }
        });
        findViewById(R.id.search_icon).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(active) {
                    Intent i = new Intent(MainActivity.this, LocationChooser.class);
                    i.putExtra(Const.title, getString(R.string.your_location));
                    try {
                        i.putExtra(Const.data, my_location1.getLatitude() + "," + my_location1.getLongitude());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(i, SEARCH_ET);
                }
                return true;
            }
        });
        findViewById(R.id.search_rl).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(active) {
                    Intent i = new Intent(MainActivity.this, LocationChooser.class);
                    i.putExtra(Const.title, getString(R.string.your_location));
                    try {
                        i.putExtra(Const.data, my_location1.getLatitude() + "," + my_location1.getLongitude());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(i, SEARCH_ET);
                }
                return true;
            }
        });
        findViewById(R.id.search_icon1).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(active) {
                    Intent i = new Intent(MainActivity.this, LocationChooser.class);
                    i.putExtra(Const.title, getString(R.string.where_hint));
                    try {
                        i.putExtra(Const.data, my_location1.getLatitude() + "," + my_location1.getLongitude());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(i, SEARCH_ET1);
                }
                return true;
            }
        });
        search_et1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(active) {
                    Intent i = new Intent(MainActivity.this, LocationChooser.class);
                    i.putExtra(Const.title, getString(R.string.where_hint));
                    try {
                        i.putExtra(Const.data, my_location1.getLatitude() + "," + my_location1.getLongitude());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(i, SEARCH_ET1);
                }
                return true;
            }
        });
        findViewById(R.id.search_rl1).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(active) {
                    Intent i = new Intent(MainActivity.this, LocationChooser.class);
                    i.putExtra(Const.title, getString(R.string.where_hint));
                    try {
                        i.putExtra(Const.data, my_location1.getLatitude() + "," + my_location1.getLongitude());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(i, SEARCH_ET1);
                }
                return true;
            }
        });
        menu_lv1 = navigationView.getRootView().findViewById(R.id.menu_lv1);


        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());


        datePickerDialog = new DatePickerDialog(
                MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                String day = dayOfMonth+"";
                String month1 = month+"";

                if(dayOfMonth<10){
                    day = "0"+dayOfMonth;
                }

                if(month<10){
                    month1 = "0"+month;
                }

                date_str = day+"-"+month1+"-"+year;

                Toast.makeText(getApplicationContext(),""+date_str,5).show();
                getDirationsCall();

            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));


        findViewById(R.id.right_arr1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.right_arr).performClick();
            }
        });
        findViewById(R.id.right_arr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Validations.valEdit2(MainActivity.this,search_et,getString(R.string.your_location))
                && Validations.valEdit2(MainActivity.this,search_et1,getString(R.string.destination))
                && Const.isOnline(MainActivity.this))
//                showFirstRidePop();
                shceduleRide();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            System.out.println("result---------------"+data.getStringExtra("result"));

            if (requestCode == SEARCH_ET) {
                search_et.setText(data.getStringExtra("result"));
            }
            else{
                search_et1.setText(data.getStringExtra("result"));
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
//            Toast.makeText(getApplicationContext(),"Can't Fetch Location",6).show();
        }

    }

//    private void CatPop() {
//        try {
//            dialog1 = new BottomSheetDialog(MainActivity.this,R.style.CustomBottomSheetDialogTheme);
//            dialog1.setCancelable(true);
//            dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog1.setContentView(R.layout.list_pop);
//            dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//
//            RecyclerView lv1 = dialog1.findViewById(R.id.lv1);
//
//            CatAdp catAdp = new CatAdp(MainActivity.this, categories);
//
//            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
//            lv1.setLayoutManager(mLayoutManager);
//            lv1.setItemAnimator(new DefaultItemAnimator());
//            lv1.setNestedScrollingEnabled(false);
//
//            lv1.setAdapter(catAdp);
//
//            dialog1.show();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")

                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })

                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    public void NavNow(View v) {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.openDrawer(GravityCompat.START);
        }
    }



    @Override
    public void click1(int position) {
//        try {
//            category_txt.setText(categories.get(position).getCat_name());
//            if (dialog1 != null) {
//                dialog1.dismiss();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void click2(int position) {
        switch (menus.get(position).getIcon()) {
            case R.drawable.home_icon:

                break;
            case R.drawable.future_icon:
                startActivity(new Intent(getApplicationContext(), NearByRides.class));
                break;
            case R.drawable.history_icon:
                startActivity(new Intent(getApplicationContext(), MyRides.class));
                break;

            case R.drawable.settings_icon:
                startActivity(new Intent(getApplicationContext(), UserSettings.class));
                break;
            case R.drawable.user_icon:
                startActivity(new Intent(getApplicationContext(), RecentRides.class));
                break;
            case R.drawable.logout_icon:
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new MySharedPref().saveData(getApplicationContext(), Const.ldata, "");
                                startActivity(new Intent(getApplicationContext(), SplashScreen.class));
                                finish();
                            }
                        })

                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;


            default:
                break;
        }

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

    }

    @Override
    public void click3(int position) {
//        try{
//            showRideEstPop(legs,drivers1.get(position));
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
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
    protected void setLocation1(Location location) {
        try{
            if(drivers==null || drivers.size()<=0 && go) {
                my_location1 = location;
                if (my_location1 != null) {
                    markerLocation = new HashMap<>();
                    UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata, "")
                            , UserDto.class);
                    if (ldata.getLocation() != null
                            && ldata.getLocation().length() <= 0) {
                        ldata.setLocation(my_location1.getLatitude() + "," + my_location1.getLongitude());
                        new MySharedPref().saveData(getApplicationContext(), Const.ldata, new Gson().toJson(ldata) + "");
                        DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.user_tbl);
                        mDatabaseUser.child(ldata.getUserId()).child(UserConst.location).setValue(my_location1.getLatitude() + "," + my_location1.getLongitude());
                    }
                    getNearByDrivers(my_location1);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
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
