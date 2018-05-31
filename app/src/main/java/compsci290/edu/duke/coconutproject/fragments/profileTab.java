package compsci290.edu.duke.coconutproject.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.res.Configuration;
import com.facebook.Profile;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import java.util.ArrayList;
import com.bumptech.glide.Glide;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Collections;
import java.util.List;

import compsci290.edu.duke.coconutproject.activities.CreateEventActivity;
import compsci290.edu.duke.coconutproject.utils.DatabaseManager;
import compsci290.edu.duke.coconutproject.utils.LocationManager;
import compsci290.edu.duke.coconutproject.activities.LoginActivity;
import compsci290.edu.duke.coconutproject.activities.MainActivity;
import compsci290.edu.duke.coconutproject.adapters.ProfileHistoryAdapter;
import compsci290.edu.duke.coconutproject.R;
import compsci290.edu.duke.coconutproject.interfaces.SingleMeetingListener;
import compsci290.edu.duke.coconutproject.interfaces.UserInfoListener;
import compsci290.edu.duke.coconutproject.models.CurrentUser;
import compsci290.edu.duke.coconutproject.models.Meeting;
import compsci290.edu.duke.coconutproject.models.User;

public class profileTab extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener {

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
        //set up the View and all the fields to be displayed
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.profile_tab, container, false);

            name = (TextView) rootView.findViewById(R.id.name);
            username = (TextView) rootView.findViewById(R.id.username);
            displayLocation = (TextView) rootView.findViewById (R.id.display_location);
            displayHistory = (TextView) rootView.findViewById(R.id.display_history);
            backgroundImg = (ImageView) rootView.findViewById(R.id.background_img);
            profileImg = (ImageView) rootView.findViewById(R.id.profile_img);
            profileBackgroundImg = (ImageView) rootView.findViewById(R.id.profile_background_image);

            rv = (RecyclerView) rootView.findViewById(R.id.rv);
            GridLayoutManager glm = new GridLayoutManager(getContext(),2);
            rv.setLayoutManager(glm);

            updates = new ArrayList<>();
            adapter = new ProfileHistoryAdapter(updates);
            rv.setAdapter(adapter);

            //configure the top bar with the settings and new post buttons
            ImageView createNewEvent = (ImageView) rootView.findViewById(R.id.create_new_post_button);
            createNewEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getContext(), CreateEventActivity.class);
                    startActivity(i);
                }
            });
            ImageView createNewEvent2 = (ImageView) rootView.findViewById(R.id.settings_button);
            createNewEvent2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom)
                            .setTitle("Log out?")
                            .setMessage("Do you want to log out?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    LoginManager.getInstance().logOut();
                                    Intent i = new Intent(getContext(), LoginActivity.class);
                                    startActivity(i);                            }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
            return rootView;
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
        }


        @Override
        public void onStart() {
            super.onStart();

            mGoogleApiClient.connect();

            //if its in landscape mode, change the text color so the picture doesn't hide it
            Configuration c = getResources().getConfiguration();
            if(c.orientation == Configuration.ORIENTATION_PORTRAIT ) {
                name.setTextColor(getResources().getColor(R.color.grayText));
                username.setTextColor(getResources().getColor(R.color.grayText));
            } else if(c.orientation == Configuration.ORIENTATION_LANDSCAPE ){
                name.setTextColor(getResources().getColor(android.R.color.white));
                username.setTextColor(getResources().getColor(android.R.color.white));
            }


            //get all the User info
            UserInfoListener userListener = new UserInfoListener() {
                @Override
                public void onHandleUserInfo(User user) {
                    mUser = user;
                    CurrentUser.currentUser = user;
                    name.setText(user.getFirstName() + " " + user.getLastName());
                    Glide.with(getView().getContext()).load(user.getProfilePicUrl()).override(120, 120).centerCrop().into(profileImg);
                    username.setText(user.getUsername());
                    displayHistory.setText("You have hosted " + mUser.getMeetingsHosted().size() + " meetings and attended " + mUser.getMeetingsAttended().size() + " meetings.");

                    ArrayList<String> meetingsAttended = mUser.getMeetingsAttended();
                    ArrayList<String> meetingsHosted = mUser.getMeetingsHosted();
                    ArrayList<String> meetingPaths = new ArrayList<>();
//                    meetingPaths.addAll(meetingsAttended);
                    meetingPaths.addAll(meetingsHosted);

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

            Log.d("profileTab", Profile.getCurrentProfile().getId());
            DatabaseManager.getUserInformation(userListener, Profile.getCurrentProfile().getId());


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

        //set up GoogleApiClient to track Location
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
        }

        //get location, check for permission and ask if they don't have it
        @Override
        public void onConnected(Bundle connectionHint) {
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

            //set bounds for east, central, and west campus
            LatLngBounds EastCampus = new LatLngBounds(new LatLng(35.998493, -78.920061), new LatLng(36.009969, -78.911545));
            LatLngBounds CentralCampus = new LatLngBounds(new LatLng(35.997989, -78.932669), new LatLng(36.005807, -78.919974));
            LatLngBounds WestCampus = new LatLngBounds(new LatLng(35.991744, -78.950402), new LatLng(36.008651, -78.933658));

            //get User Location
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                me = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                //if the Location is in one of our popular bounded spots, display that they're there
                if (LocationManager.identifyBuildingLocation(me) != null) {
                    currentLocation = "in " + LocationManager.identifyBuildingLocation(me).getBuilding();
                }
                //else say if there on east, central, or west campus or if they're off campus
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