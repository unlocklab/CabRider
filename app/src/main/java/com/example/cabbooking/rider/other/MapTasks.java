package com.example.cabbooking.rider.other;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.example.cabbooking.rider.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapTasks {
    static final float COORDINATE_OFFSET = 0.00002f;

    public static LatLng coordinateForMarker(double latitude, double longitude, int MAX_NUMBER_OF_MARKERS
            , HashMap<String, String> markerLocation) {

        LatLng location = null;

        for (int i = 0; i <= MAX_NUMBER_OF_MARKERS; i++) {

            if (mapAlreadyHasMarkerForLocation((latitude + i
                    * COORDINATE_OFFSET)
                    + "," + (longitude + i * COORDINATE_OFFSET),markerLocation)) {

                // If i = 0 then below if condition is same as upper one. Hence, no need to execute below if condition.
                if (i == 0)
                    continue;

                if (mapAlreadyHasMarkerForLocation((latitude - i
                        * COORDINATE_OFFSET)
                        + "," + (longitude - i * COORDINATE_OFFSET),markerLocation)) {

                    continue;

                } else {

                    location = new LatLng((latitude - (i * COORDINATE_OFFSET)),(longitude - (i * COORDINATE_OFFSET)));

                    break;
                }

            } else {

                location = new LatLng((latitude - (i * COORDINATE_OFFSET)),(longitude - (i * COORDINATE_OFFSET)));
                break;
            }
        }

        return location;
    }

    // Return whether marker with same location is already on map
    private static boolean mapAlreadyHasMarkerForLocation(String location, HashMap<String, String> markerLocation) {
        return (markerLocation.containsValue(location));
    }

    public static BitmapDescriptor loadMarkImg(Activity activity, int loc_icon) {
        try{
            int height = 100;
            int width = 100;
            if(loc_icon==R.drawable.car_icon) {
                width = 70;
            }
            else if(loc_icon==R.drawable.loc_icon1
                    || loc_icon==R.drawable.loc_icon2) {
                height = 70;
                width = 50;
            }
            BitmapDrawable bitmapdraw = (BitmapDrawable)activity.getResources().getDrawable(loc_icon);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

            Paint paint = new Paint();
            paint.setColorFilter(new PorterDuffColorFilter(activity.getResources().getColor(R.color.app_color), PorterDuff.Mode.SRC_IN));


            return BitmapDescriptorFactory.fromBitmap(smallMarker);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static String getStrAddress(Activity activity,double latitude, double longitude) {
        try{
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(activity, Locale.getDefault());

            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String str = "";

            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();

            if(knownName!=null){
                str = str+""+knownName;
            }

            if(address!=null){
                str = str+", "+address;
            }

            if(city!=null){
                str = str+", "+city;
            }

            if(state!=null){
                str = str+", "+state;
            }

            if(country!=null){
                str = str+", "+country;
            }

            if(postalCode!=null){
                str = str+", "+postalCode;
            }

            return str;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static Polyline showPolyOnMap(Activity activity,String encodedPoly, GoogleMap mMap,Polyline my_line) {
        try{

            if(my_line!=null){
                my_line.remove();
                my_line = null;
            }

            List<LatLng> poly = decodePoly(encodedPoly);

            my_line = mMap.addPolyline(new PolylineOptions()
                    .addAll(poly)
                    .width(5)
                    .color(activity.getResources().getColor(R.color.app_color)));

            zoomCamera(activity,mMap,poly);

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return my_line;
    }
    public static void zoomCamera(GoogleMap mMap, Location location){
        try{
            if(location!=null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.0f));

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void zoomCamera(Activity activity,GoogleMap mMap,List<LatLng> l1){
        try{
            System.out.println("zoom--------------");

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (LatLng point : l1) {
                builder.include(point);
            }

            LatLngBounds bounds = builder.build();

            int width = activity.getResources().getDisplayMetrics().widthPixels;
            int height = activity.getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.10);

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

            mMap.moveCamera(cu);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    public static String getAddress(Activity activity, double lat, double lng){
        String fullAdd=null;
        try{
            Geocoder geocoder= new Geocoder(activity.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat,lng,1);
            if(addresses.size()>0){
                Address address = addresses.get(0);
                fullAdd = address.getAddressLine(0);
            }


        }catch(Exception ex){
            ex.printStackTrace();
        }
        return fullAdd;
    }

    public static List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public static Location getLocBYLatLng(String lat_lng) {
        Location location = new Location("");
        try{
            String loc[] = lat_lng.split(",");

            location.setLatitude(Double.parseDouble(loc[0]));
            location.setLongitude(Double.parseDouble(loc[1]));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return location;
    }
}
