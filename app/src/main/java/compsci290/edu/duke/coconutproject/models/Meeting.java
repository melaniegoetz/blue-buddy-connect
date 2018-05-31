package compsci290.edu.duke.coconutproject.models;


import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Meeting class; Object for all info needed for each posted event
public class Meeting {

    private String mTitle;
    private User mHost;
    private ArrayList<String> mAttendees;

    private String mTime;
    private DukeLocation mLocation;
    private String mImage;
    private Bitmap mBitmap;
    private String mDescription;
    private String mHostImageURL;
    private String mEventImageURL;
    private String mMeetingPath;


    //TO DO: PLZ REFACTOR MOST OF THESE OUT
    public Meeting(User host, String title, ArrayList<String> attendees, String time, DukeLocation location, String description) {
        this(host, title, attendees, time, location, null, null, description);
    }

    public Meeting(User host, String title, ArrayList<String> attendees, String time, DukeLocation location, @Nullable String image, @Nullable Bitmap bitmap, String description) {
        this.mTitle = title;
        this.mHost = host;
        this.mAttendees = attendees;
        this.mTime = time;
        this.mLocation = location;
        this.mImage = image;
        this.mBitmap = bitmap;
        this.mDescription = description;
    }

    public Meeting(User host, String title, ArrayList<String> attendees, String time, DukeLocation location, String description, String imageURL) {
        this.mTitle = title;
        this.mHost = host;
        this.mAttendees = attendees;
        this.mTime = time;
        this.mLocation = location;
        this.mDescription = description;
        this.mHostImageURL = imageURL;
    }

    public Meeting(User host, String title, ArrayList<String> attendees, String time, DukeLocation location, @Nullable String image, @Nullable Bitmap bitmap, String description, String imageURL, String eventImageURL) {
        this.mTitle = title;
        this.mHost = host;
        this.mAttendees = attendees;
        this.mTime = time;
        this.mLocation = location;
        this.mImage = image;
        this.mDescription = description;
        this.mHostImageURL = imageURL;
        this.mEventImageURL = eventImageURL;
        this.mBitmap = bitmap;
    }

    public Meeting(User host, String title, ArrayList<String> attendees, String time, DukeLocation location, @Nullable String image, String description, String profileImageURL, @Nullable String meetingPath) {
        this.mHost = host;
        this.mTitle = title;
        this.mAttendees = attendees;
        this.mTime = time;
        this.mLocation = location;
        this.mImage = image; // this is the URL of Firebase image
        this.mDescription = description;
        this.mHostImageURL = profileImageURL;
        this.mMeetingPath = meetingPath;
    }

    public String getmMeetingPath() {
        return mMeetingPath;
    }
    public void setMeetingPath(String path) { mMeetingPath = path; }
    public String getmEventImageURL() {
        return mEventImageURL;
    }

    public void setmEventImageURL(String url) {
        mEventImageURL = url;
    }


    public String getTitle() {return mTitle;}
    public void editTitle(String newTitle) {
        this.mTitle = newTitle;
    }

    public User getHost() {return mHost;}

    public ArrayList<String> getAttendees() {return mAttendees;}
    public void addAttendee(String newAttendee) {
        this.mAttendees.add(newAttendee);
    }
    public void removeAttendee(String oldAttendee) {
        this.mAttendees.remove(oldAttendee);
    }

    public String getTime() {return mTime;}

    public DukeLocation getLocation() {return mLocation;}

    public String getImage() {return mImage;}

    public Bitmap getmBitmap() {
        return mBitmap;
    }

    public String getmDescription() {
        return mDescription;
    }

    public Map getMeetingInfoMap() {
        Map<String, Object> meetingInfo = new HashMap<>();
        meetingInfo.put("host", this.mHost.getFirstName() + " " + this.mHost.getLastName());
        meetingInfo.put("hostID", this.mHost.getmUser_id());
        meetingInfo.put("title", this.mTitle);
        meetingInfo.put("time", this.mTime);
        meetingInfo.put("locationName", this.mLocation.getBuilding());
        meetingInfo.put("imageName", this.mImage);
        meetingInfo.put("description", this.mDescription);
        meetingInfo.put("eventImageURL", this.mEventImageURL);
        meetingInfo.put("hostImageURL", this.mHostImageURL);

        return meetingInfo;
    }


}