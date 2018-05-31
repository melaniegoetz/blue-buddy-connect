package compsci290.edu.duke.coconutproject.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.*;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import compsci290.edu.duke.coconutproject.interfaces.LocationChangedListener;
import compsci290.edu.duke.coconutproject.models.DukeLocation;

/**
 * Created by Scott on 4/30/17.
 */

public class LocationManager {


    private static GoogleMap mGoogleMap;
    private static ArrayList<DukeLocation> mLocations;

    private static android.location.LocationManager mLocationManager;
    private static LocationListener mLocationListener;
    private static LocationChangedListener mLocationChangedListener;


    public static void setmGoogleMap(GoogleMap map) {
        mGoogleMap = map;
    }

    public static void getCurrentLocation(Activity activity, Context context, final LocationChangedListener locationListener) {
        mLocationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("LocationManager", "Have location: " + location.toString() + " with lat: " + location.getLatitude() + " and long: " + location.getLongitude());
                locationListener.onLocationChanged(LocationManager.identifyBuildingLocation(new LatLng(location.getLatitude(), location.getLongitude())));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        try {
            if (isGPSEnabled) {
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    mLocationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
                } else {
                    mLocationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
                }
            }
        } catch (Exception e) {
                Log.d("Location Manager", "Exception: " + e);
        }
    }



    public static @Nullable
    DukeLocation identifyBuildingLocation(LatLng currentLocationCoord) {

        for (DukeLocation loc : getLocations()) {
            if (PolyUtil.containsLocation(currentLocationCoord, loc.getmMapPolygon().getPoints(), true)) {
                return loc;
            }

        }
        return null;
    }


    private static final Map<String, LatLng[]> namesToMapPoints = new HashMap<>();
    static {
        namesToMapPoints.put("West Union", new LatLng[]{new LatLng(36.000651, -78.939819), new LatLng(36.001150, -78.939487), new LatLng(36.000885, -78.938955), new LatLng(36.000399, -78.939299)});
        namesToMapPoints.put("Perkins Library", new LatLng[]{new LatLng(36.002289, -78.939076), new LatLng(36.001990, -78.938358), new LatLng(36.002485, -78.938098), new LatLng(36.002715, -78.938809)});
        namesToMapPoints.put("Rubenstein Library", new LatLng[]{new LatLng(36.001716, -78.939095), new LatLng(36.001470, -78.938521), new LatLng(36.001934, -78.938258), new LatLng(36.002107, -78.938841)});
        namesToMapPoints.put("Bryan Center", new LatLng[]{new LatLng(36.001161, -78.942035), new LatLng(36.000402, -78.940653), new LatLng(36.001145, -78.940509), new LatLng(36.001599, -78.941606)});
        namesToMapPoints.put("Wilson Rec Center", new LatLng[]{new LatLng(35.997379, -78.941830), new LatLng(35.996454, -78.940912), new LatLng(35.997008, -78.940153), new LatLng(35.997618, -78.941641)});
        namesToMapPoints.put("Sarah P. Duke Gardens", new LatLng[]{new LatLng(36.005355, -78.933456), new LatLng(36.000903, -78.936805), new LatLng(35.999795, -78.933842), new LatLng(36.000852, -78.930169), new LatLng(36.003804, -78.930119), new LatLng(36.003834, -78.931862), new LatLng(36.005244, -78.931788)});
    }


    private static ArrayList<DukeLocation> getLocations() {
        if (mLocations != null) {
            return mLocations;
        } else {
            ArrayList<DukeLocation> locations = new ArrayList<>();

            for (String locationName : namesToMapPoints.keySet()) {
                Log.d("Location Manager", locationName);
                Polygon poly = mGoogleMap.addPolygon(new PolygonOptions().add(namesToMapPoints.get(locationName)).visible(false));
                DukeLocation loc = new DukeLocation(locationName, poly);
                locations.add(loc);
            }

            mLocations = locations;
            return locations;
        }
    }

}
