package compsci290.edu.duke.coconutproject.fragments;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.location.Location;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;

import com.bumptech.glide.Glide;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import compsci290.edu.duke.coconutproject.activities.MainActivity;
import compsci290.edu.duke.coconutproject.utils.DatabaseManager;
import compsci290.edu.duke.coconutproject.models.DukeLocation;
import compsci290.edu.duke.coconutproject.utils.LocationManager;
import compsci290.edu.duke.coconutproject.R;
import compsci290.edu.duke.coconutproject.interfaces.MeetingInfoListener;
import compsci290.edu.duke.coconutproject.models.CurrentMeetings;
import compsci290.edu.duke.coconutproject.models.Meeting;

public class mapTab extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener {

    private static final int REQUEST_LOCATION = 1;
    public static ArrayList<DukeLocation> dukeLocations = new ArrayList<DukeLocation>();

    MapView mMapView;
    Marker userMarker;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;

    private Bitmap imgBitmap;
    public static final String TAG = "MapTab";
    LatLng durham = new LatLng(36.0010, -78.9385);
    LatLng me;
    Map<Marker,ArrayList<Meeting>> markersToMeetingsMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set up GoogleApiClient to track Location
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initializeLocations();

        //Inflate View and set up the GoogleMap
        View rootView = inflater.inflate(R.layout.map_tab, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        final Context myContext = getActivity().getApplicationContext();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override

            public void onMapReady(GoogleMap mMap) {
                mGoogleMap = mMap;
                LocationManager.setmGoogleMap(mGoogleMap);
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

                //bounds of the overlay and bounds of the map
                final LatLngBounds dukeBounds = new LatLngBounds(new LatLng(35.991653, -78.951697), new LatLng(36.013467, -78.907737));
                final LatLngBounds mapBounds = new LatLngBounds(new LatLng(35.993883, -78.949666), new LatLng(36.010969, -78.910179));

                //pan camera to center of Duke
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(durham, 17);
                mGoogleMap.animateCamera(cameraUpdate);

                //fix zoom at level 17 and map bounds
                mGoogleMap.setMinZoomPreference(17);
                mGoogleMap.setMaxZoomPreference(17);
                mGoogleMap.setLatLngBoundsForCameraTarget(mapBounds);

                //settings for Info Windows for the user's location
                mGoogleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {

                        Context context = getActivity().getApplicationContext();

                        LinearLayout info = new LinearLayout(context);
                        info.setOrientation(LinearLayout.VERTICAL);
                        info.setElevation(10);

                        TextView title = new TextView(context);
                        title.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                        title.setTextColor(getResources().getColor(android.R.color.white));
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(MainActivity.font);
                        title.setTextSize(20);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(context);
                        snippet.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                        snippet.setTextColor(getResources().getColor(android.R.color.white));
                        snippet.setGravity(Gravity.CENTER);
                        snippet.setTypeface(MainActivity.font);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);

                        return info;
                    }
                });

                //get the map overlay image
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        if (Looper.myLooper() == null) {
                            Looper.prepare();
                        }

                        try {
                            imgBitmap = Glide.
                                    with(myContext).
                                    load("http://i.imgur.com/oPb1EH3.jpg").
                                    asBitmap().
                                    into(-1, -1).
                                    get();

                        } catch (final ExecutionException e) {
                            Log.e(TAG, "this failed" + e.getMessage());
                        } catch (final InterruptedException e) {
                            Log.e(TAG, "this failed" + e.getMessage());
                        }
                        return null;
                    }

                    //overlay the image onto the map
                    @Override
                    protected void onPostExecute(Void dummy) {
                        if (null != imgBitmap) {
                            GroundOverlayOptions dukeMap = new GroundOverlayOptions()
                                    .image(BitmapDescriptorFactory.fromBitmap(imgBitmap))
                                    .positionFromBounds(dukeBounds);
                            mGoogleMap.addGroundOverlay(dukeMap);

                        };

                    }

                }.execute();
            }
        });

        return rootView;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //check for permission; if no permission then request it
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        //get the user's last location, drop a Marker on it, and pan the map to it
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            me = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            userMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(me)
                    .title("This is me!"));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(me, 17);
            mGoogleMap.animateCamera(cameraUpdate);
        }
        else{
            Log.d(TAG, "it is null");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    //get the meetings that have currently been posted
    private void loadFeedLocations() {
            MeetingInfoListener meetingListener = new MeetingInfoListener() {
                @Override
                public void onHandleMeetingInfo(ArrayList<Meeting> meetings) {
                    placeMarkers(meetings);
                }
            };
            DatabaseManager.getMeetingInformation(meetingListener);
        }

    //put markers on the map for each meeting location
    private void placeMarkers(List<Meeting> meetings) {

        Map<DukeLocation, ArrayList<Meeting>> locationNameToMeetings = new HashMap<>();
        //map each meeting to its location
        for (Meeting meeting : meetings) {
            for (DukeLocation dukeLocation : dukeLocations) {
                if (meeting.getLocation().getBuilding().equals(dukeLocation.getBuilding())) {
                    // add to map
                    if (locationNameToMeetings.containsKey(dukeLocation)) {
                        locationNameToMeetings.get(dukeLocation).add(meeting);
                    } else {
                        ArrayList<Meeting> locationMeetings = new ArrayList<>();
                        locationMeetings.add(meeting);
                        locationNameToMeetings.put(dukeLocation, locationMeetings);
                    }
                }
            }
        }
        //makes a new marker for each location; maps each location's meetings that marker
        for (DukeLocation dukeLocation : locationNameToMeetings.keySet()) {
            Marker newMarker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(dukeLocation.getCoords()[0], dukeLocation.getCoords()[1])));
            markersToMeetingsMap.put(newMarker,locationNameToMeetings.get(dukeLocation));
        }

        //handle marker clicks
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //if the marker is your location, then display the styled default info window
                if (marker.equals(userMarker)) {
                    marker.setTitle(" Your Current Location ");
                    marker.setSnippet("Check out events nearby!");
                    return false;
                }
                //otherwise fire a fragment to display the dialog with the meeting info
                else {
                    FragmentManager mFragmentManager = getFragmentManager();
                    MarkerDialogFragment mDialog = MarkerDialogFragment.newInstance(markersToMeetingsMap.get(marker));
                    mDialog.show(mFragmentManager, "meetingMarkerDialog");
                    return true;
                }
            }
        });
    }

    //reload the markers when the map comes into view
    @Override
    public void setUserVisibleHint(boolean visible){
        super.setUserVisibleHint(visible);
        if (visible){
            loadFeedLocations();
        }
        else {
        }
    }

    //the efficiently stored locations we track on Duke's campus lol
    private void initializeLocations() {
        dukeLocations.add(new DukeLocation(36.012250, -78.949298, "Heights", "Off-Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.010787, -78.947651, "Belmont", "Off-Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.009197, -78.943754, "Trinity Commons", "Off-Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.008163, -78.934505, "Franklin Center", "Central Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.007295, -78.933550, "Trent", "Central Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.008380, -78.928915, "Berkshire Main", "Off-Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.008554, -78.924398, "Berkshire Ninth", "Off-Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.001031, -78.909507, "Shooters", "Off-Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.000394, -78.909366, "Devines", "Off-Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.001990, -78.914705, "Smith Warehouse", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.005278, -78.913889, "White", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.004974, -78.914157, "East Duke", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.004991, -78.915423, "West Duke", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.006666, -78.913556, "Crowell", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.006432, -78.914243, "Friedl", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.006467, -78.915198, "Carr", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.005773, -78.914147, "East", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.005790, -78.915366, "Jarvis", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.005642, -78.916943, "Gilbert-Addoms", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.005928, -78.918037, "Southgate", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.006483, -78.916374, "Blackwell", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.006865, -78.917157, "Randolph", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.006969, -78.918133, "Belltower", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.006023, -78.913187, "Epworth", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.007065, -78.915204, "Giles", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.007056, -78.914281, "Wilson", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.008141, -78.915161, "Alspaugh", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.008141, -78.914238, "Brown", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.008757, -78.915150, "Pegram", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.008722, -78.914195, "Bassett", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.007611, -78.917103, "Brodie Gym", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.008904, -78.914646, "Baldwin", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.007611, -78.915193, "Lilly Library", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.007611, -78.914024, "Marketplace", "East Campus", "Eating"));
        dukeLocations.add(new DukeLocation(36.009219, -78.915986, "Biddle", "East Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.000058, -78.918450, "Arts Annex", "East Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.002931, -78.922202, "Swift", "Central Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.002219, -78.920957, "Central Campus Apts Swift", "Central Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.999156, -78.929038, "Nasher", "Central Campus", "Eating"));
        dukeLocations.add(new DukeLocation(36.004499, -78.925491, "Central Campus Apts Between Alexander & Oregon ", "Central Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.004334, -78.929032, "Dame's", "Central Campus", "Eating"));
        dukeLocations.add(new DukeLocation(36.005627, -78.928442, "Central Campus Apts Between Anderson & Alexander", "Central Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.004533, -78.930727, "Central Campus Apts Between Flowers & Anderson", "Central Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.002736, -78.932828, "Sarah P. Duke Gardens", "Central Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.993074, -78.947465, "Washington Duke Inn", "West Campus", "Eating"));
        dukeLocations.add(new DukeLocation(35.995510, -78.941791, "Wallace Wade", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.995918, -78.944023, "Koskinen Stadium", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.997906, -78.944173, "Jack Coombs Field", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.997681, -78.940043, "Ambler Stadium", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.997186, -78.940987, "Wilson Rec Center", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.996952, -78.939410, "Sheffield", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.997664, -78.942181, "Cameron Indoor", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.999119, -78.947051, "Fuqua", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(35.999214, -78.943540, "Sanford", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.000099, -78.945353, "Law School", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.001392, -78.944752, "Gross Hall", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.012250, -78.949298, "BioSci", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.002889, -78.943524, "French Family Science Center", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.003256, -78.942556, "Math & Physics", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.004436, -78.942191, "LSRC", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.003846, -78.941097, "Teer", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.004108, -78.940433, "Hudson Hall", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.003474, -78.939693, "CIEMAS", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.002962, -78.938384, "Bostock", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.002481, -78.938647, "Perkins", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.002776, -78.937789, "Old Chem", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.001657, -78.938369, "Rubenstein", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.002438, -78.937006, "SocPsych", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.001778, -78.937446, "SocSci", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.001275, -78.937736, "Allen", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.002247, -78.938036, "Languages", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.001995, -78.940482, "Chapel", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.002221, -78.939903, "Divinity School", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.001240, -78.939838, "Page", "West Campus", "Working"));
        dukeLocations.add(new DukeLocation(36.001179, -78.941104, "Bryan Center", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.000771, -78.939441, "West Union", "West Campus", "Eating"));
        dukeLocations.add(new DukeLocation(35.998671, -78.939152, "Wannamaker", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.999582, -78.940107, "Kilgo Quad", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.998983, -78.939957, "Crowell Quad", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.998684, -78.935865, "Edens", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.999443, -78.938993, "Craven Quad", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.999031, -78.937697, "Keohane Quad", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(36.000290, -78.938287, "Few Quad", "West Campus", "Chilling"));
        dukeLocations.add(new DukeLocation(35.999205, -78.936806, "Tower", "West Campus", "Eating"));

    }

}