package compsci290.edu.duke.coconutproject.activities;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import compsci290.edu.duke.coconutproject.R;
import compsci290.edu.duke.coconutproject.adapters.ProfileHistoryAdapter;
import compsci290.edu.duke.coconutproject.interfaces.SingleMeetingListener;
import compsci290.edu.duke.coconutproject.interfaces.UserInfoListener;
import compsci290.edu.duke.coconutproject.models.Meeting;
import compsci290.edu.duke.coconutproject.models.User;
import compsci290.edu.duke.coconutproject.utils.DatabaseManager;
import compsci290.edu.duke.coconutproject.utils.LocationManager;

public class ViewFriendProfileActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_LOCATION = 1;

    private LatLng me;
    private String currentLocation;

    private TextView name;
    private TextView username;
    private TextView displayLocation;
    private TextView displayHistory;

    private ImageView backgroundImg;
    private ImageView profileBackgroundImg;
    private ImageView profileImg;

    private List<Meeting> updates;
    private RecyclerView rv;
    private ProfileHistoryAdapter adapter;

    private User mUser;


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onStart() {
        super.onStart();

        mGoogleApiClient.connect();

        Configuration c = getResources().getConfiguration();
        if(c.orientation == Configuration.ORIENTATION_PORTRAIT ) {
            name.setTextColor(getResources().getColor(R.color.grayText));
            username.setTextColor(getResources().getColor(R.color.grayText));
        } else if(c.orientation == Configuration.ORIENTATION_LANDSCAPE ){
            name.setTextColor(getResources().getColor(android.R.color.white));
            username.setTextColor(getResources().getColor(android.R.color.white));
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String jsonMyObject = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonMyObject = extras.getString("User");
        }

        // set user
        User user = new Gson().fromJson(jsonMyObject, User.class);
        mUser = user;

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        setContentView(R.layout.activity_view_friend_profile);


        name = (TextView) findViewById(R.id.name);
        username = (TextView) findViewById(R.id.username);
        displayLocation = (TextView) findViewById (R.id.display_location);
        displayHistory = (TextView) findViewById(R.id.display_history);
        backgroundImg = (ImageView) findViewById(R.id.background_img);
        profileImg = (ImageView) findViewById(R.id.profile_img);
        profileBackgroundImg = (ImageView) findViewById(R.id.profile_background_image);

        rv = (RecyclerView) findViewById(R.id.rv);
        GridLayoutManager glm = new GridLayoutManager(getBaseContext(),2);
        rv.setLayoutManager(glm);

        updates = new ArrayList<>();
        adapter = new ProfileHistoryAdapter(updates);
        rv.setAdapter(adapter);

        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              finish();
            }
        });

        name.setText(mUser.getFirstName() + " " + mUser.getLastName());
        Glide.with(getBaseContext()).load(mUser.getProfilePicUrl()).override(120, 120).centerCrop().into(profileImg);
        username.setText(mUser.getUsername());

        UserInfoListener userListener = new UserInfoListener() {
            @Override
            public void onHandleUserInfo(User user) {
                mUser = user;
                displayHistory.setText(mUser.getFirstName() + " has hosted " + mUser.getMeetingsHosted().size() + " meetings and attended " + mUser.getMeetingsAttended().size() + " meetings.");

                ArrayList<String> meetingPaths = mUser.getMeetingsHosted();

                for (String meetingPath : meetingPaths) {
                    SingleMeetingListener meetingListener = new SingleMeetingListener() {
                        @Override
                        public void onHandleMeetingInfo(Meeting meeting) {
                            Collections.reverse(updates);
                            updates.add(meeting);
                            Collections.reverse(updates);
                            adapter = new ProfileHistoryAdapter(updates);
                            adapter.notifyDataSetChanged();
                            rv.setAdapter(adapter);
                        }
                    };

                    DatabaseManager.getSingleMeetingInformation(meetingListener, meetingPath);
                }
            }
        };

        DatabaseManager.getUserInformation(userListener, mUser.getmUser_id());




        Log.d("profileTab", Profile.getCurrentProfile().getId());



        // Get Location Data
//            LocationChangedListener locationChangedListener = new LocationChangedListener() {
//                @Override
//                public void onLocationChanged(DukeLocation location) {
//
//                    if (location != null && location.getBuilding() != null) {
//                        displayLocation.setText("Currently in " + location.getBuilding());
//                    }
//                }
//            };
//
//            LocationManager.getCurrentLocation(getActivity(), getContext(), locationChangedListener);


        profileBackgroundImg.setImageResource(R.drawable.profileimgbackground);
        profileBackgroundImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        profileImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        backgroundImg.setImageResource(R.drawable.duke_chapel);
        name.setTypeface(MainActivity.font);
        username.setTypeface(MainActivity.font);

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        LatLngBounds EastCampus = new LatLngBounds(new LatLng(35.998493, -78.920061), new LatLng(36.009969, -78.911545));
        LatLngBounds CentralCampus = new LatLngBounds(new LatLng(35.997989, -78.932669), new LatLng(36.005807, -78.919974));
        LatLngBounds WestCampus = new LatLngBounds(new LatLng(35.991744, -78.950402), new LatLng(36.008651, -78.933658));

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            me = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            if (LocationManager.identifyBuildingLocation(me) != null) {
                currentLocation = "in " + LocationManager.identifyBuildingLocation(me).getBuilding();
            }
            else {
                if (EastCampus.contains(me)) {
                    currentLocation = "on East Campus";
                } else if (CentralCampus.contains(me)) {
                    currentLocation = "on Central Campus";
                } else if (WestCampus.contains(me)) {
                    currentLocation = "on West Campus";
                } else {
                    currentLocation = "Off Campus";
                }
            }
            displayLocation.setText("~ Currently " + currentLocation + " ~");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}