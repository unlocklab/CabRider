package com.example.cabbooking.rider.activities;

import androidx.annotation.NonNull;
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
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
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
import com.example.cabbooking.rider.services.GPSTracker;
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

public class MainActivity extends AppCompatActivity implements  OnMapReadyCallback, AdpListner {

    private DrawerLayout drawer;
    private Polyline my_line = null;
    private NavigationView navigationView;
    private AutoCompleteTextView search_et, search_et1;
    private AutoSuggestAdapter1 autoSuggestAdapter1 = null;
    private AutoSuggestAdapter1 autoSuggestAdapter2 = null;
    private BottomSheetDialog dialog1 = null,dialog2 = null;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private PriceDto priceDto = null;
    private Handler handler, handler1;
    private TextView category_txt;
    private String category_txt1 = "", price_est1 = "",date_str = "";
    private int ride_type = 0;
    private boolean start = false,economy = false,comfort = false;
    private DatePickerDialog datePickerDialog = null;
    private RecyclerView menu_lv1;
    private List<CatDto> categories = new ArrayList<>();
    private List<UserDto> drivers1 = new ArrayList<>();
    private ValueEventListener listener = null;
    private List<Location> drivers = new ArrayList<>();
    private List<String> driverIds = new ArrayList<>();
    private List<Marker> driverMarkers = new ArrayList<>();
    private List<LatLng> markers = new ArrayList<>();
    private GoogleMap mMap;
    private Location my_location = null;
    private List<MenuData> menus = new ArrayList<>();
    private ProgressDialog pd;
    private JSONObject legs = null;
    private HashMap<String, String> markerLocation = new HashMap<>();
    private GPSTracker gps = null;
    String str[] = new String[]{
            android.Manifest.permission.INTERNET
            , android.Manifest.permission.ACCESS_NETWORK_STATE
            , Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean checkIfAlreadyhavePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true;
        } else {
            return false;
        }
    }

    private void requestReadPhoneStatePermission() {
        ActivityCompat.requestPermissions(MainActivity.this, str,
                1);
    }

    public void ProNow(View v){
        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        initPD();
        initUI();
        setMenu();
        setupAutoCom();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        loadCats();
        loadPrice();
    }
    private void setMenu() {
        try {
            menus = new ArrayList<>();
            menus.add(new MenuData(getString(R.string.home),R.drawable.home_icon));
            menus.add(new MenuData(getString(R.string.ride_history),R.drawable.history_icon));
            menus.add(new MenuData(getString(R.string.schedule_ride),R.drawable.user_icon));
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

    private void setupAutoCom() {
        try {
            autoSuggestAdapter1 = new AutoSuggestAdapter1(this,
                    android.R.layout.simple_dropdown_item_1line);

            autoSuggestAdapter2 = new AutoSuggestAdapter1(this,
                    android.R.layout.simple_dropdown_item_1line);

            search_et.setThreshold(2);
            search_et.setAdapter(autoSuggestAdapter1);

            search_et1.setThreshold(2);
            search_et1.setAdapter(autoSuggestAdapter2);

            search_et.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                final int position, long id) {
                            try {
                                search_et.setText(autoSuggestAdapter1.getObject(position).getString("description"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

            search_et1.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                final int position, long id) {
                            try {
                                search_et1.setText(autoSuggestAdapter2.getObject(position).getString("description"));
                                showFirstRidePop();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

            search_et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int
                        count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                    handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                            AUTO_COMPLETE_DELAY);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            search_et1.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int
                        count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    handler1.removeMessages(TRIGGER_AUTO_COMPLETE);
                    handler1.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                            AUTO_COMPLETE_DELAY);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            /*f
             *handler for api calling
             * */
            handler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.what == TRIGGER_AUTO_COMPLETE) {
                        if (!TextUtils.isEmpty(search_et.getText())) {
                            makeApiCallConFilter(search_et, autoSuggestAdapter1);
                        }
                    }
                    return false;
                }
            });

            handler1 = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.what == TRIGGER_AUTO_COMPLETE) {
                        if (!TextUtils.isEmpty(search_et1.getText())) {
                            makeApiCallConFilter(search_et1, autoSuggestAdapter2);
                        }
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeApiCallConFilter(AutoCompleteTextView autoCompleteTextView, final AutoSuggestAdapter1 asADP) {
        try {
            String url = "";
            if (my_location != null) {
                url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?" +
                        "input=" + autoCompleteTextView.getText().toString() +
                        "&location=" + my_location.getLatitude() + "," + my_location.getLongitude() +
                        "&radius=" + Const.radius +
                        "&key=" + Const.google_api_key;
            } else {
                url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?" +
                        "input=" + autoCompleteTextView.getText().toString() +
                        "&radius=" + Const.radius +
                        "&key=" + Const.google_api_key;
            }


            ApiCall.makeGET(this, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String mainObj) {

                    try {
                        System.out.println("asdfg---------------" + mainObj);
                        JSONObject jk = new JSONObject(mainObj);
                        if (jk.getString(Const.status).equals("OK")) {
                            JSONArray results = jk.getJSONArray(Const.predictions);
                            asADP.setData(results);
                            asADP.notifyDataSetChanged();
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
    protected void onResume() {
        super.onResume();
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
                startTracking(ldata);
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
            if (my_location != null) {
                if (findViewById(R.id.search_rl1).getVisibility() == View.GONE) {
                    url = "https://maps.googleapis.com/maps/api/directions/json?" +
                            "origin=" + my_location.getLatitude() + "," + my_location.getLongitude() +
                            "&destination=" + search_et.getText().toString() +
                            "&key=" + Const.google_api_key;
                } else {
                    url = "https://maps.googleapis.com/maps/api/directions/json?" +
                            "origin=" + my_location.getLatitude() + "," + my_location.getLongitude() +
                            "&destination=" + search_et1.getText().toString() +
                            "&key=" + Const.google_api_key;
                }


                ApiCall.makeGET(this, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String mainObj) {
                        pd.dismiss();
                        try {
                            findViewById(R.id.loc_icon).setVisibility(View.GONE);
                            mMap.setOnCameraChangeListener(null);
                            System.out.println("asdfg---------------" + mainObj);
                            JSONObject jk = new JSONObject(mainObj);
                            if (jk.getString(Const.status).equals("OK")) {
                                JSONArray routes = jk.getJSONArray(MapConst.routes);
                                JSONObject overview_polyline = routes.getJSONObject(0).getJSONObject(MapConst.overview_polyline);
                                String encodedPoly = overview_polyline.getString(MapConst.points);
                                my_line = MapTasks.showPolyOnMap(MainActivity.this, encodedPoly, mMap, my_line);

                                legs = routes.getJSONObject(0)
                                        .getJSONArray(MapConst.legs)
                                        .getJSONObject(0);
                                System.out.println("testr-----0---------");

                                final LatLng myLoc = new LatLng(my_location.getLatitude(), my_location.getLongitude());
//
                                Marker marker1 = mMap.addMarker(new MarkerOptions().position(myLoc));
                                markerLocation.put(my_location.getLatitude() + "", "" + my_location.getLongitude());
                                try {

                                    marker1.setIcon(MapTasks.loadMarkImg(MainActivity.this, R.drawable.mark1));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                drivers1 = Const.getNerestDriver(my_location,drivers1,start,economy,comfort);

                                chooseDriverPop(drivers1,legs);
//                                showRideEstPop(legs);

                                Const.hideKeyboardFrom(MainActivity.this, findViewById(R.id.rl2));
                                search_et.setAdapter(null);
                                search_et1.setAdapter(null);
                                if (findViewById(R.id.search_rl1).getVisibility() == View.GONE) {
                                    findViewById(R.id.search_rl).setVisibility(View.VISIBLE);
                                    findViewById(R.id.search_rl1).setVisibility(View.VISIBLE);
                                    search_et1.setText(search_et.getText().toString());
                                    search_et.setText(MapTasks.getStrAddress(MainActivity.this
                                            , my_location.getLatitude()
                                            , my_location.getLongitude()));

                                    search_et.setHint(getString(R.string.from));
                                    search_et1.setHint(getString(R.string.to));
                                }
                                if(dialog2!=null && dialog2.isShowing()) {
                                    dialog2.dismiss();
                                    dialog2 = null;
                                }
                                setupAutoCom();
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

    private void chooseDriverPop(List<UserDto> drivers, JSONObject legs) {
        try{
            dialog1 = new BottomSheetDialog(MainActivity.this);
            dialog1.setCancelable(true);
            dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog1.setContentView(R.layout.list_pop);
            dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            RecyclerView lv1 = dialog1.findViewById(R.id.lv1);

            DriverAdp catAdp = new DriverAdp(MainActivity.this, drivers,legs,priceDto);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            lv1.setLayoutManager(mLayoutManager);
            lv1.setItemAnimator(new DefaultItemAnimator());
            lv1.setNestedScrollingEnabled(false);

            lv1.setAdapter(catAdp);

            dialog1.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadCats() {
        final DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.cat_tbl);
        mDatabaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    categories = new ArrayList<>();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        CatDto catDto = dataSnapshot1.getValue(CatDto.class);
                        categories.add(catDto);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showFirstRidePop() {
        try {
            dialog2 = new BottomSheetDialog(MainActivity.this);
            dialog2.setCancelable(true);
            dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog2.setContentView(R.layout.first_ride_pop);
            dialog2.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            ImageView right_icon = dialog2.findViewById(R.id.right_icon);
            category_txt = dialog2.findViewById(R.id.category_txt);
            RelativeLayout category_rl = dialog2.findViewById(R.id.category_rl);
            final EditText price_est = dialog2.findViewById(R.id.price_est);
            final TextView chk1 = dialog2.findViewById(R.id.chk1);
            final TextView chk2 = dialog2.findViewById(R.id.chk2);
            final TextView chk3 = dialog2.findViewById(R.id.chk3);

            Const.setChecked(MainActivity.this,chk1,start);
            Const.setChecked(MainActivity.this,chk2,economy);
            Const.setChecked(MainActivity.this,chk3,comfort);

            Const.setChkHandler(MainActivity.this,chk1);
            Const.setChkHandler(MainActivity.this,chk2);
            Const.setChkHandler(MainActivity.this,chk3);

            price_est.setText(price_est1);
            category_txt.setText(category_txt1);

            category_rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    category_txt.performClick();
                }
            });

            category_txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CatPop();
                }
            });

            right_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Ride Type")
                            .setMessage("Please select ride type")

                            .setPositiveButton(R.string.one_time, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog1, int which) {

                                    start  = Const.getChecked(chk1);
                                    economy  = Const.getChecked(chk2);
                                    comfort  = Const.getChecked(chk3);

                                    price_est1  = price_est.getText().toString();
                                    category_txt1 = category_txt.getText().toString();
                                    ride_type = 0;
                                    shceduleRide();
                                }
                            })

                            .setNegativeButton(R.string.future_ride, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog1, int which) {


                                    start  = Const.getChecked(chk1);
                                    economy  = Const.getChecked(chk2);
                                    comfort  = Const.getChecked(chk3);

                                    price_est1  = price_est.getText().toString();
                                    category_txt1 = category_txt.getText().toString();
                                    ride_type = 1;
                                    shceduleRide();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();


                }
            });

            dialog2.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void shceduleRide() {
        if (findViewById(R.id.search_rl1).getVisibility() == View.GONE) {
            if (search_et.getText().toString().length() > 0) {
                if (category_txt.getText().toString().length() > 0) {
                    if(ride_type==1){
                        datePickerDialog.show();
                    }
                    else {
                        getDirationsCall();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please Choose Vehicle Category", 6).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.where_hint), 6).show();
            }
        } else {
            if (search_et1.getText().toString().length() > 0) {
                if (category_txt.getText().toString().length() > 0) {

                    if(ride_type==1){
                        datePickerDialog.show();
                    }
                    else {
                        getDirationsCall();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please Choose Vehicle Category", 6).show();
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
            final BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
            dialog.setCancelable(true);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            View contentView = View.inflate(MainActivity.this, R.layout.ride_est_pop, null);
            dialog.setContentView(contentView);

            ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));



            ImageView right_icon = dialog.findViewById(R.id.right_icon);

            TextView txt1 = dialog.findViewById(R.id.txt1);
            TextView txt2 = dialog.findViewById(R.id.txt2);
            TextView txt21 = dialog.findViewById(R.id.txt21);

            final TextView fare_txt = dialog.findViewById(R.id.fare_txt);
            final TextView distance_txt = dialog.findViewById(R.id.distance_txt);
            final TextView duration_txt = dialog.findViewById(R.id.duration_txt);
            final TextView pay_type_txt = dialog.findViewById(R.id.pay_type_txt);
            RatingBar rt1 = dialog.findViewById(R.id.rt1);

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
                if(fair>Double.parseDouble(price_est1)){
                    Toast.makeText(getApplicationContext(),getString(R.string.low_est_fair_msg),6).show();
                }


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

                            search_et.setText("");
                            search_et1.setText("");
                            findViewById(R.id.search_rl1).setVisibility(View.GONE);

                            getNearByDrivers(my_location);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                dialog.show();
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

        UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata, "")
                , UserDto.class);
        startTracking(ldata);

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                try {
                    search_et.setText(MapTasks.getAddress(MainActivity.this
                            , cameraPosition.target.latitude, cameraPosition.target.longitude));

                    my_location = new Location("");
                    my_location.setLatitude(cameraPosition.target.latitude);
                    my_location.setLongitude(cameraPosition.target.longitude);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startTracking(UserDto ldata) {
        System.out.println("get-----------par----"+checkIfAlreadyhavePermission());
        if (checkIfAlreadyhavePermission()) {
            if (gps == null) {
                try {
                    gps = new GPSTracker(MainActivity.this, ldata);

                    System.out.println("get-----------gps----"+gps.canGetLocation());
                    if (gps.canGetLocation()) {
                        getLoca();
                    } else {

                        System.out.println("get-----------sett----");
                        gps.showSettingsAlert();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else{
                if (gps.canGetLocation()) {
                    getLoca();
                } else {

                    System.out.println("get-----------sett----");
                    gps.showSettingsAlert();
                }
            }
        } else {
            requestReadPhoneStatePermission();
        }
    }

    private void getLoca() {
        System.out.println("get-----------loc----");
        my_location = gps.getLocation();
        if(my_location!=null) {
            markerLocation = new HashMap<>();

            getNearByDrivers(my_location);
        }
        else{
            getLoca();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata, "")
                , UserDto.class);
        startTracking(ldata);
    }

    private void getNearByDrivers(final Location location1) {
        try {
            markers = new ArrayList<>();
            final LatLng myLoc = new LatLng(location1.getLatitude(), location1.getLongitude());
//
//            Marker marker1 = mMap.addMarker(new MarkerOptions().position(myLoc));
//            markerLocation.put(location1.getLatitude() + "", "" + location1.getLongitude());
//            try {
//
//                marker1.setIcon(MapTasks.loadMarkImg(MainActivity.this, R.drawable.loc_icon2));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

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


//                        Marker marker1 = mMap.addMarker(new MarkerOptions().position(myLoc));
//                        markerLocation.put(location1.getLatitude() + "", "" + location1.getLongitude());
//                        try {
//
//                            marker1.setIcon(MapTasks.loadMarkImg(MainActivity.this, R.drawable.loc_icon2));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }


                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            try {
                                UserDto userDto = dataSnapshot1.getValue(UserDto.class);
                                if (userDto.getStatus().equals("1")) {
                                    JSONObject jk = new JSONObject(userDto.getLocation());
                                    Location location = new Location("");

                                    location.setLatitude(jk.getDouble("mLatitude"));
                                    location.setLongitude(jk.getDouble("mLongitude"));
                                    location.setAltitude(jk.getDouble("mAltitude"));



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
                            }

                            MapTasks.zoomCamera(mMap, markers);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mDatabaseUser.removeEventListener(listener);
                    MapTasks.zoomCamera(mMap, markers);
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

        findViewById(R.id.search_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_et.setText("");
            }
        });

        findViewById(R.id.search_icon1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_et1.setText("");
            }
        });

        findViewById(R.id.right_arr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Validations.valEdit(MainActivity.this,search_et,getString(R.string.your_location))
                && Validations.valEdit(MainActivity.this,search_et1,getString(R.string.destination))
                && Const.isOnline(MainActivity.this))
                showFirstRidePop();
            }
        });


    }

    private void CatPop() {
        try {
            dialog1 = new BottomSheetDialog(MainActivity.this);
            dialog1.setCancelable(true);
            dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog1.setContentView(R.layout.list_pop);
            dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            RecyclerView lv1 = dialog1.findViewById(R.id.lv1);

            CatAdp catAdp = new CatAdp(MainActivity.this, categories);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            lv1.setLayoutManager(mLayoutManager);
            lv1.setItemAnimator(new DefaultItemAnimator());
            lv1.setNestedScrollingEnabled(false);

            lv1.setAdapter(catAdp);

            dialog1.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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
        try {
            category_txt.setText(categories.get(position).getCat_name());
            if (dialog1 != null) {
                dialog1.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void click2(int position) {
        switch (menus.get(position).getIcon()) {
            case R.drawable.home_icon:

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
        try{
            showRideEstPop(legs,drivers1.get(position));
        }
        catch (Exception e){
            e.printStackTrace();
        }
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
