package compsci290.edu.duke.coconutproject.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Map;

import compsci290.edu.duke.coconutproject.interfaces.MeetingImageUploadListener;
import compsci290.edu.duke.coconutproject.interfaces.MeetingInfoListener;
import compsci290.edu.duke.coconutproject.interfaces.SingleMeetingListener;
import compsci290.edu.duke.coconutproject.interfaces.UserExistListener;
import compsci290.edu.duke.coconutproject.interfaces.UserInfoListener;
import compsci290.edu.duke.coconutproject.models.DukeLocation;
import compsci290.edu.duke.coconutproject.models.Meeting;
import compsci290.edu.duke.coconutproject.models.NewEventPostedEvent;
import compsci290.edu.duke.coconutproject.models.User;

/**
 * Created by Scott on 4/19/17.
 */

public class DatabaseManager {

    private static DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    private static StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();
    private static String TAG = "DatabaseManager";

    // Write to Firebase Database at user level
    public static void writeToUserDatabase(User user, Map<String, Object> value) {
        mDatabaseReference.child("users").child(user.getmUser_id()).setValue(value);
    }

    // Checks if user exists in Database, could refactor to make more general reads to database
    public static void doesUserExist(final UserExistListener listener, final String uid) {
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean userExists = dataSnapshot.child(uid).exists();
                Log.d(TAG, ": userExists = " + userExists);
                listener.onHandleUserExist(userExists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting snapshot failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabaseReference.child("users").addListenerForSingleValueEvent(userListener);
    }


    // Write to Firebase Database to meeting and user level
    public static void writeEventToDatabase(User user, Map<String, Object> value) {
        String key = mDatabaseReference.child("meetings").push().getKey();
        mDatabaseReference.child("meetings").child(key).setValue(value);
        mDatabaseReference.child("users").child(user.getmUser_id()).child("meetingsHosted").push().setValue(key);
    }

    public static void writeUserAsAttending(String meetingPath, User currentUser) {

        mDatabaseReference.child("meetings").child(meetingPath).child("attendees").push().setValue(currentUser.getFirstName());

        // Write to user's own attending Meetings
        mDatabaseReference.child("users").child(currentUser.getmUser_id()).child("meetingsAttended").push().setValue(meetingPath);
    }

    public static void removeUserFromAttending(final String meetingPath, final User currentUser) {

        mDatabaseReference.child("meetings").child(meetingPath).child("attendees").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot :  dataSnapshot.getChildren())
                {
                    if(snapshot.getValue().equals(currentUser.getFirstName()))
                    {
                        snapshot.getRef().setValue(null);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "removeAttendee:onCancelled", databaseError.toException());
            }
        });

        //Delete in user's own attending Meetings
        mDatabaseReference.child("users").child(currentUser.getmUser_id()).child("meetingsAttended").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    if(snapshot.getValue().equals(meetingPath)){
                        snapshot.getRef().setValue(null);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "removeAttendee:onCancelled", databaseError.toException());
            }
        });
    }


    public static void uploadPhoto(Bitmap image, String fileName, final MeetingImageUploadListener listener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mStorageReference.child(fileName).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d("DatabaseManager: ", "Unsuccessful Upload" + exception.getLocalizedMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Log.d("DatabaseManager: ", "Successful Upload");
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                listener.onHandleMeetingImageUpload(downloadUrl.toString());
            }
        });

    }

    public static void getUserInformation(final UserInfoListener listener, final String uid) {
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firstName = dataSnapshot.child("firstName").getValue(String.class);
                String lastName = dataSnapshot.child("lastName").getValue(String.class);
                String userName = dataSnapshot.child("userName").getValue(String.class);
                String profilePictureString = dataSnapshot.child("profilePictureURL").getValue(String.class);

                ArrayList<String> hostedMeetingPaths = new ArrayList<>();

                for (DataSnapshot meetings : dataSnapshot.child("meetingsHosted").getChildren()) {
                    hostedMeetingPaths.add(meetings.getValue(String.class));
                }

                ArrayList<String> attendedMeetingPaths = new ArrayList<>();

                for (DataSnapshot meetings : dataSnapshot.child("meetingsAttended").getChildren()) {
                    attendedMeetingPaths.add(meetings.getValue(String.class));
                }

                // Get location and meetings data
                listener.onHandleUserInfo(new User(userName, firstName, lastName, uid, profilePictureString, hostedMeetingPaths, attendedMeetingPaths));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting snapshot failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabaseReference.child("users").child(uid).addListenerForSingleValueEvent(userListener);
    }

    public static void getSingleMeetingInformation(final SingleMeetingListener listener, final String meetingPath){
        ValueEventListener singleMeetingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String host = dataSnapshot.child("host").getValue(String.class);
                final String hostID = dataSnapshot.child("hostID").getValue(String.class);
                ArrayList<String> attendees = new ArrayList<>();
                for (DataSnapshot attendant : dataSnapshot.child("attendees").getChildren()) {
                    attendees.add(attendant.getValue(String.class));
                }
                final String locationName = dataSnapshot.child("locationName").getValue(String.class);
                final String title = dataSnapshot.child("title").getValue(String.class);
                final String time = dataSnapshot.child("time").getValue(String.class);
                final String description = dataSnapshot.child("description").getValue(String.class);
                final String imageName = dataSnapshot.child("eventImageURL").getValue(String.class);
                final String profilePicURL = dataSnapshot.child("hostImageURL").getValue(String.class);
                final String meetingPath = dataSnapshot.getKey();

                String[] names = host.split(" ");
                User meetingUser = new User(names[0] + names[1] + hostID.substring(0, 1), names[0], names[1], hostID, profilePicURL);
                Meeting newMeeting = new Meeting(meetingUser, title, attendees, time, new DukeLocation(locationName), imageName, description, profilePicURL, meetingPath);
                listener.onHandleMeetingInfo(newMeeting);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseReference.child("meetings").child(meetingPath).addListenerForSingleValueEvent(singleMeetingListener);
    }

    public static void getMeetingInformation(final MeetingInfoListener listener) {
        ValueEventListener meetingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<Meeting> meetingsList = new ArrayList<>();

                for (DataSnapshot snapshotChild : dataSnapshot.getChildren()) {

                    final String host = snapshotChild.child("host").getValue(String.class);
                    final String hostID = snapshotChild.child("hostID").getValue(String.class);
                    final String locationName = snapshotChild.child("locationName").getValue(String.class);
                    final String title = snapshotChild.child("title").getValue(String.class);
                    final String time = snapshotChild.child("time").getValue(String.class);
                    final String description = snapshotChild.child("description").getValue(String.class);
                    final String imageName = snapshotChild.child("eventImageURL").getValue(String.class);
                    final String profilePicURL = snapshotChild.child("hostImageURL").getValue(String.class);
                    final String meetingPath = snapshotChild.getKey();


                    // Attendees
                    ArrayList<String> attendees = new ArrayList<>();

                    for (DataSnapshot attendant : snapshotChild.child("attendees").getChildren()) {
                        attendees.add(attendant.getValue(String.class));
                    }

                    String[] names = host.split(" ");
                    User meetingUser = new User(names[0] + names[1] + hostID.substring(0, 1), names[0], names[1], hostID, profilePicURL);
                    meetingsList.add(new Meeting(meetingUser, title, attendees, time, new DukeLocation(locationName), imageName, description, profilePicURL, meetingPath));


                }
                Log.d("DatabaseManager", meetingsList.toString());
                listener.onHandleMeetingInfo(meetingsList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabaseReference.child("meetings").addListenerForSingleValueEvent(meetingListener);
    }


}
