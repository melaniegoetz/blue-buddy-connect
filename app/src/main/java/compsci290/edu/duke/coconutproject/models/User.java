package compsci290.edu.duke.coconutproject.models;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//User class to keep track of their personal info and the meetings they've attended
public class User {

    private String mUsername;
    private String mProfilePic;
    private String mFirstName;
    private String mLastName;
    private String mUser_id;
    private DukeLocation mUserLocation;
    private ArrayList<String> mMeetingsHosted;
    private ArrayList<String> mMeetingsAttended;

    public User(String username, String firstName, String lastName, String user_id, String profilePicURL) {
        this(username, firstName, lastName, user_id, null, profilePicURL, null, null);
    }

    public User(String username, String firstName, String lastName, String user_id, String profilePicURL, ArrayList<String> hostedMeetings, ArrayList<String> attendedMeetings) {
        this(username, firstName, lastName, user_id, null, profilePicURL, hostedMeetings, attendedMeetings);
    }


    public User(String username, String firstName, String lastName, String user_id, @Nullable DukeLocation userLocation, @Nullable String mProfilePic, @Nullable ArrayList<String> meetingsHosted, @Nullable ArrayList<String> meetingsAttended) {

        this.mUsername = username;
        this.mProfilePic = mProfilePic;
        this.mFirstName = firstName;
        this.mLastName = lastName;
        this.mUser_id = user_id;
        this.mUserLocation = userLocation;
        this.mMeetingsHosted = (meetingsHosted == null) ? new ArrayList<String>() : meetingsHosted;
        this.mMeetingsAttended = (meetingsAttended == null) ? new ArrayList<String>() : meetingsAttended;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void editFirstName(String newFirstName) {
        this.mFirstName = newFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void editLastName(String newLastName) {
        this.mLastName = newLastName;
    }

    public String getmUser_id() {
        return mUser_id;
    }

    public ArrayList<String> getMeetingsAttended() {
        return mMeetingsAttended;
    }

//    public void attendMeeting(Meeting newMeeting) {
//        this.mMeetingsAttended.add(newMeeting);
//    }

    public void unattendMeeting(Meeting oldMeeting) {
        this.mMeetingsAttended.remove(oldMeeting);
    }

    public ArrayList<String> getMeetingsHosted() {
        return mMeetingsHosted;
    }

//    public void hostMeeting(String newMeeting) {
//        this.mMeetingsHosted.add(newMeeting);
//    }

    public String getProfilePicUrl(){
        return mProfilePic;
    }

    public void cancelMeeting(Meeting oldMeeting) {
        this.mMeetingsHosted.remove(oldMeeting);
    }

    public DukeLocation getUserLocation() {
        return mUserLocation;
    }

    public void updateUserLocation(DukeLocation newUserLocation) {
        this.mUserLocation = newUserLocation;
    }

    public Map getUserInfoMap() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userName", this.mUsername);
        userInfo.put("firstName", this.mFirstName);
        userInfo.put("lastName", this.mLastName);
        userInfo.put("profilePictureURL", this.mProfilePic);
        userInfo.put("location", this.mUserLocation);
        userInfo.put("meetingsHosted", mMeetingsHosted);
        userInfo.put("meetingsAttended", mMeetingsAttended);

        return userInfo;
    }
}