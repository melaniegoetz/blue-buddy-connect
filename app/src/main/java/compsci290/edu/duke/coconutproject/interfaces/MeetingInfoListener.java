package compsci290.edu.duke.coconutproject.interfaces;

import java.util.ArrayList;

import compsci290.edu.duke.coconutproject.models.Meeting;

/**
 * Created by Scott on 5/1/17.
 */

public interface MeetingInfoListener {
    public void onHandleMeetingInfo(ArrayList<Meeting> meetings);
}
