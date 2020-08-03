package com.example.cabbooking.rider.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
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
import android.view.MotionEvent;
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
import com.example.cabbooking.rider.other.Validations;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.util.List;


public class LocationChooser extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener {
    /*
     * init Ui in class
     *
     * */
    private WebView wv1;
    public void NavNow(View v){
        finish();
    }
    private AutoCompleteTextView search_et;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter1 autoSuggestAdapter1 = null;
    private GoogleMap mMap = null;
    private PlacesClient placesClient = null;
    private boolean update_pos = true;
    private GoogleMap.OnCameraChangeListener linster = null;
    private Location my_location1 = null;
    private String title = "";

    @Override
    protected void setLocation1(Location location) {
//        if(title.equals(getString(R.string.your_location))) {
            if (mMap != null && my_location1==null) {
                my_location1 = location;
                MapTasks.zoomCamera(mMap, my_location1);
            }
//        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.location_ch_screen;
    }
    /*
     * this method connects Ui xml file with java file
     * */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();
        System.out.println("dfcv-----------1-----");
        title = getIntent().getExtras().getString(Const.title);
        System.out.println("dfcv----------------"+title);
        search_et.setHint(title);
        try{
            String data[] = getIntent().getExtras().getString(Const.data).split(",");
            my_location1 = new Location("");
            my_location1.setLatitude(Double.parseDouble(data[0]));
            my_location1.setLongitude(Double.parseDouble(data[1]));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try{
            Places.initialize(getApplicationContext(), getString(R.string.places_key));

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

        findViewById(R.id.search_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_et.setText("");
            }
        });
        search_et.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            final int position, long id) {
                        try {
                            mMap.setOnCameraChangeListener(null);
                            update_pos = false;
                            System.out.println("dfccc------------"+autoSuggestAdapter1.getObject(position).getString("place_id"));
                            search_et.setText(autoSuggestAdapter1.getObject(position).getString("description"));
                            try{
                                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

                                FetchPlaceRequest request = FetchPlaceRequest
                                        .newInstance(autoSuggestAdapter1.getObject(position).getString("place_id"), placeFields);

                                placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                                    @Override
                                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                                        System.out.println("dfccc-----------succ-"+fetchPlaceResponse.getPlace().getLatLng().latitude);
                                        my_location1 = new Location("");
                                        my_location1.setLatitude(fetchPlaceResponse.getPlace().getLatLng().latitude);
                                        my_location1.setLongitude(fetchPlaceResponse.getPlace().getLatLng().longitude);
                                        System.out.println("dfccc-----------succ1-");

                                        MapTasks.zoomCamera(mMap, my_location1);
                                        System.out.println("dfccc-----------succ2-");

                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println("dfccc-----------failed-"+e.getMessage());

                                        e.printStackTrace();
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
                        mMap.setOnCameraChangeListener(linster);
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
    public void onCameraMoveStarted(int reason) {
        update_pos = false;
    }

    @Override
    public void onCameraMove() {
        update_pos = false;
    }

    @Override
    public void onCameraMoveCanceled() {
        update_pos = true;
    }

    @Override
    public void onCameraIdle() {
        update_pos = true;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                try {
//                    if(title.equals(getString(R.string.your_location))) {
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
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (my_location != null) {
                                my_location1 = my_location;
                                MapTasks.zoomCamera(mMap, my_location1);
                            }
                        }
//                    }
                    linster = new GoogleMap.OnCameraChangeListener() {
                        @Override
                        public void onCameraChange(CameraPosition cameraPosition) {
                            try {
                                if (update_pos) {
                                    System.out.println("change");
                                    new MyBgTask(cameraPosition).execute();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mMap.setOnCameraChangeListener(linster);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
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


    private class MyBgTask extends AsyncTask{
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
                       search_et.setText(MapTasks.getAddress(LocationChooser.this
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

    public void NextNow(View view){
        try{
            if(Validations.valEdit(this,search_et,getString(R.string.location))) {
                Const.hideKeyboardFrom(LocationChooser.this, findViewById(R.id.rl2));
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", search_et.getText().toString());
                setResult(Activity.RESULT_OK, returnIntent);

                finish();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
