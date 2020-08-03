package com.example.cabbooking.rider.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.cabbooking.rider.R;
import com.example.cabbooking.rider.adapters.AutoSuggestAdapter1;
import com.example.cabbooking.rider.dto.UserConst;
import com.example.cabbooking.rider.dto.UserDto;
import com.example.cabbooking.rider.other.ApiCall;
import com.example.cabbooking.rider.other.Const;
import com.example.cabbooking.rider.other.MapTasks;
import com.example.cabbooking.rider.other.MySharedPref;
import com.example.cabbooking.rider.services.GPSTracker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Roshani
 * its about screen or we can say content screen where we will show content
 * category wise like terms and condition ,about us etc
 */

public class LocationActivtiy extends BaseActivity implements OnMapReadyCallback {
    /*
     * init Ui in class
     *
     * */
    public void NavNow(View v){
        finish();
    }
    private boolean update_pos = true;
    private AutoCompleteTextView search_et;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter1 autoSuggestAdapter1 = null;
    private GoogleMap mMap = null;
    private GoogleMap.OnCameraChangeListener listner = null;
    private Location my_location1 = null;


    @Override
    protected void setLocation1(Location location) {
        if(my_location1==null
                && mMap!=null){
            my_location1 = location;
            MapTasks.zoomCamera(mMap, my_location1);
        }
    }

    private PlacesClient placesClient = null;




    public void NextNow(View v){
        if (my_location1 != null) {
            UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata, "")
                    , UserDto.class);

            ldata.setLocation(my_location1.getLatitude() + "," + my_location1.getLongitude());

            new MySharedPref().saveData(getApplicationContext(), Const.ldata, new Gson().toJson(ldata) + "");
            DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference(Const.user_tbl);
            mDatabaseUser.child(ldata.getUserId()).child(UserConst.location).setValue(my_location1.getLatitude() + "," + my_location1.getLongitude());
            Const.hideKeyboardFrom(LocationActivtiy.this, findViewById(R.id.rl2));

            finish();
        }
        else{
            Toast.makeText(getApplicationContext(), "Please select location", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected int getLayoutResourceId() {
        return R.layout.location_screen;
    }
    /*
     * this method connects Ui xml file with java file
     * */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();
        try{
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
            placesClient = Places.createClient(this);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /*
     * loading content in Webview because text align justify doesnot works in android
     * and this code is supportable for all devices 4.4 to max
     * */
    private void initUI() {
        search_et = findViewById(R.id.search_et);

        autoSuggestAdapter1 = new AutoSuggestAdapter1(this,
                android.R.layout.simple_dropdown_item_1line);

        search_et.setThreshold(2);
        search_et.setAdapter(autoSuggestAdapter1);


        search_et.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            final int position, long id) {
                        try {
                            update_pos = false;
                            mMap.setOnCameraChangeListener(null);
                            System.out.println("dfccc------------"+autoSuggestAdapter1.getObject(position));
                            search_et.setText(autoSuggestAdapter1.getObject(position).getString("description"));
                            try{
                                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

                                FetchPlaceRequest request = FetchPlaceRequest.newInstance(autoSuggestAdapter1.getObject(position).getString("place_id"), placeFields);

                                placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                                    @Override
                                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                                        my_location1 = new Location("");
                                        my_location1.setLatitude(fetchPlaceResponse.getPlace().getLatLng().latitude);
                                        my_location1.setLongitude(fetchPlaceResponse.getPlace().getLatLng().longitude);
                                        MapTasks.zoomCamera(mMap, my_location1);
                                    }
                                });

                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        update_pos = true;
                        mMap.setOnCameraChangeListener(listner);
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
    }
    private void makeApiCallConFilter(AutoCompleteTextView autoCompleteTextView, final AutoSuggestAdapter1 asADP) {
        try {
            if(my_location!=null) {
                String url = "";
                url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?" +
                        "input=" + autoCompleteTextView.getText().toString() +
                        "&radius=" + Const.radius +
                        "&location=" + my_location.getLatitude() + "," + my_location.getLongitude() +
                        "&key=" + Const.google_api_key;


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
            }
            else{
                Toast.makeText(getApplicationContext(),getString(R.string.fetching_loc),6).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private class MyBgTask extends AsyncTask {
        private CameraPosition cameraPosition;
        public MyBgTask(CameraPosition cameraPosition) {
            this.cameraPosition = cameraPosition;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        search_et.setText(MapTasks.getAddress(LocationActivtiy.this
                                , cameraPosition.target.latitude, cameraPosition.target.longitude));
                    }
                });
                my_location1 = new Location("");
                my_location1.setLatitude(cameraPosition.target.latitude);
                my_location1.setLongitude(cameraPosition.target.longitude);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                try {
                    UserDto ldata = new Gson().fromJson(new MySharedPref().getData(getApplicationContext(), Const.ldata, "")
                            , UserDto.class);
                    try {
                        if (ldata.getLocation() != null && ldata.getLocation().length() > 0) {
                            try {
                                my_location1 = new Location("");
                                String loc[] = ldata.getLocation().split(",");
                                my_location1.setLatitude(Double.parseDouble(loc[0]));
                                my_location1.setLongitude(Double.parseDouble(loc[1]));
                                MapTasks.zoomCamera(mMap, my_location1);
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (my_location != null) {
                                    my_location1 = my_location;
                                    MapTasks.zoomCamera(mMap, my_location1);
                                }
                            }
                        } else {
                            if (my_location != null) {
                                my_location1 = my_location;
                                MapTasks.zoomCamera(mMap, my_location1);
                            }
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        if (my_location != null) {
                            my_location1 = my_location;
                            MapTasks.zoomCamera(mMap, my_location1);
                        }
                    }
                    listner = new GoogleMap.OnCameraChangeListener() {
                        @Override
                        public void onCameraChange(CameraPosition cameraPosition) {
                            try {
                                try {
                                    if (update_pos) {
                                        new MyBgTask(cameraPosition).execute();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mMap.setOnCameraChangeListener(listner);

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

        });
       }


}
