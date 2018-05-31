package compsci290.edu.duke.coconutproject.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.login.LoginManager;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import compsci290.edu.duke.coconutproject.R;
import compsci290.edu.duke.coconutproject.activities.CreateEventActivity;
import compsci290.edu.duke.coconutproject.activities.LoginActivity;
import compsci290.edu.duke.coconutproject.activities.ViewFriendProfileActivity;
import compsci290.edu.duke.coconutproject.adapters.FeedUpdateAdapter;
import compsci290.edu.duke.coconutproject.adapters.GoToFriendProfileEvent;
import compsci290.edu.duke.coconutproject.interfaces.MeetingInfoListener;
import compsci290.edu.duke.coconutproject.models.CurrentMeetings;
import compsci290.edu.duke.coconutproject.models.Meeting;
import compsci290.edu.duke.coconutproject.models.NewEventPostedEvent;
import compsci290.edu.duke.coconutproject.models.User;
import compsci290.edu.duke.coconutproject.utils.DatabaseManager;


public class feedTab extends Fragment {
    List<Meeting> updates;
    FeedUpdateAdapter adapter;
    RecyclerView rv;
    SwipeRefreshLayout swipeRefresh;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updates = new ArrayList<>();
        adapter = new FeedUpdateAdapter(updates);
    }

    @Override
    //set the View and the RecyclerView with the adapter
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.feed_tab, container, false);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        rv = (RecyclerView) rootView.findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        rv.setAdapter(adapter);
        ImageView createNewEvent = (ImageView) rootView.findViewById(R.id.create_new_post_button);
        createNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), CreateEventActivity.class);
                startActivity(i);
            }
        });

        //refresh feed on swipe
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeed();
            }
        });

        //add the top bar with the new post and setting buttons
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
        refreshFeed();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //refresh 2 seconds after post
    @Override
    public void onResume() {
        super.onResume();
        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                refreshFeed();
            }
        }.start();    }

    @Override
    public void onPause() {
        super.onPause();
    }

    //handle going to friend profile
    @Subscribe
    public void onGoToFriendProfileEvent(GoToFriendProfileEvent goToFriendProfileEvent){
        User friend = goToFriendProfileEvent.friend;
        Intent i = new Intent(getActivity(), ViewFriendProfileActivity.class);
        i.putExtra("User", new Gson().toJson(friend));
        startActivity(i);

    }

    //refresh the feed by listening for new meetings, notifying the adapter of the change, and resetting recycler view
    private void refreshFeed() {
        MeetingInfoListener meetingListener = new MeetingInfoListener() {
            @Override
            public void onHandleMeetingInfo(ArrayList<Meeting> meetings) {
                List<Meeting> pastUpdates = updates;
                List<Meeting> newUpdates = new ArrayList<>();

                newUpdates.addAll(pastUpdates);
                newUpdates.addAll(meetings);

                Collections.reverse(newUpdates);
                CurrentMeetings.meetings = updates;

                adapter = new FeedUpdateAdapter(newUpdates);
                adapter.notifyDataSetChanged();
                rv.setAdapter(adapter);
            }
        };
        DatabaseManager.getMeetingInformation(meetingListener);
        swipeRefresh.setRefreshing(false);
    }

}