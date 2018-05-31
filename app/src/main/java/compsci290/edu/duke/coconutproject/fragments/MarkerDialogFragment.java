package compsci290.edu.duke.coconutproject.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.support.v4.app.DialogFragment;
import android.view.View;


import com.google.gson.Gson;

import java.util.ArrayList;

import compsci290.edu.duke.coconutproject.R;
import compsci290.edu.duke.coconutproject.adapters.FeedUpdateAdapter;
import compsci290.edu.duke.coconutproject.models.Meeting;


/**
 * Created by SPalumbo on 5/4/17.
 */

public class MarkerDialogFragment extends DialogFragment {

    RecyclerView rv;
    FeedUpdateAdapter adapter;

    public MarkerDialogFragment() {

    };

    //set the Meetings as arguments and pass them through Gson/Json
    public static MarkerDialogFragment newInstance(ArrayList<Meeting> meetingsOnMarker) {
        MarkerDialogFragment frag = new MarkerDialogFragment();
        Bundle args = new Bundle();
        ArrayList<String> GsonMeetings = new ArrayList<>();
        for (int i = 0; i < meetingsOnMarker.size(); i++) {
            GsonMeetings.add(new Gson().toJson(meetingsOnMarker.get(i)));
        }
        args.putStringArrayList("meetingsOnMarker", GsonMeetings);
        frag.setArguments(args);
        return frag;
    }

    //set up the dialog to be displayed
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the Meeting Data
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        ArrayList<String> myArgs = getArguments().getStringArrayList("meetingsOnMarker");
        ArrayList<Meeting> myMeetings = new ArrayList<>();
        for (String arg : myArgs) {
            myMeetings.add(new Gson().fromJson(arg, Meeting.class));
        }

        //setup the views/layout with the same adapter used for the feed updates just in an alert dialog view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.meeting_marker_dialog, null);
        adapter = new FeedUpdateAdapter(myMeetings);
        rv = (RecyclerView) rootView.findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);
        builder.setView(rootView);
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // cancel button
            }
        });

        return builder.create();
    }

}
