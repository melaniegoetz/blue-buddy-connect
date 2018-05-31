package compsci290.edu.duke.coconutproject.adapters;

import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import compsci290.edu.duke.coconutproject.R;
import compsci290.edu.duke.coconutproject.models.CurrentUser;
import compsci290.edu.duke.coconutproject.models.Meeting;
import compsci290.edu.duke.coconutproject.utils.DatabaseManager;
import de.hdodenhof.circleimageview.CircleImageView;


public class FeedUpdateAdapter extends RecyclerView.Adapter<FeedUpdateAdapter.FeedUpdateViewHolder>{
    List<Meeting> updates;
    Typeface font;

    public FeedUpdateAdapter(List<Meeting> updates) {
        this.updates=updates;
    }

    @Override
    public FeedUpdateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_update_view_holder, parent, false);
        FeedUpdateViewHolder pvh = new FeedUpdateViewHolder(v);

        font = Typeface.createFromAsset(parent.getContext().getAssets(), "LSR.ttf");
        return pvh;
    }

    @Override
    public void onBindViewHolder(final FeedUpdateViewHolder holder, final int position) {

        //fill the layout with meeting information
        holder.userText.setText("                    " + updates.get(position).getTitle());
        if(updates.get(position).getTitle().equals("")){
            holder.userText.setText("                    " + "Meetup at " + updates.get(position).getLocation().getBuilding());
        }
        holder.userName.setText(updates.get(position).getHost().getFirstName() + " " + updates.get(position).getHost().getLastName());
        holder.userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new GoToFriendProfileEvent(updates.get(position).getHost()));
            }
        });
        holder.userLocation.setText(updates.get(position).getLocation().getBuilding());
        holder.timeStamp.setText(updates.get(position).getTime());
        Glide.with(holder.itemView.getContext()).load(updates.get(position).getHost().getProfilePicUrl()).centerCrop().into(holder.personPhoto);
        holder.personPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new GoToFriendProfileEvent(updates.get(position).getHost()));
            }
        });
        Glide.with(holder.itemView.getContext()).load(updates.get(position).getImage()).fitCenter().into(holder.updatePhoto);
        if(updates.get(position).getmBitmap()!=null){
            holder.updatePhoto.setAdjustViewBounds(true);
            holder.updatePhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.updatePhoto.setImageBitmap(updates.get(position).getmBitmap());
        }

        final ArrayList<String> attendees = updates.get(position).getAttendees();

        if (attendees != null && attendees.size() > 0) {
            if (attendees.size() == 1) {
                holder.attendance.setText(attendees.get(0) + " is going!");
            } else if (attendees.size() == 2) {
                holder.attendance.setText(attendees.get(0) + " and " + attendees.get(1) + " are going!");
            } else {
                holder.attendance.setText(attendees.get(0) + " and " + (attendees.size() - 1) + " other people are going!");
            }
        }

        //handling attendance and such
        boolean currentUserAttending = false;

        Log.d("feedUpdate", "hey " + CurrentUser.currentUser.getFirstName());

        for (String attendeeName : updates.get(position).getAttendees()) {
            if (CurrentUser.currentUser.getFirstName().equals(attendeeName)) {
                currentUserAttending = true;
            }
        }

        if (currentUserAttending) {
            holder.inactiveComing.setVisibility(View.GONE);
            holder.activeComing.setVisibility(View.VISIBLE);
            holder.inactiveComing.setEnabled(false);
        }else {
            holder.activeComing.setVisibility(View.GONE);
            holder.inactiveComing.setVisibility(View.VISIBLE);
            holder.activeComing.setEnabled(false);
        }


        holder.inactiveComing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.inactiveComing.setVisibility(View.GONE);
                holder.activeComing.setVisibility(View.VISIBLE);
                updates.get(position).addAttendee(CurrentUser.currentUser.getUsername());
                Log.d("FeedUpdateAdapater", "Going");
//                Log.d("FeedUpdate", CurrentUser.currentUser.getUsername());
//                Log.d("FeedUpdate", updates.get(position).toString());
                updateAttendees(holder, position, attendees);
                DatabaseManager.writeUserAsAttending(updates.get(position).getmMeetingPath(), CurrentUser.currentUser);
            }
        });

        holder.activeComing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.activeComing.setVisibility(View.GONE);
                holder.inactiveComing.setVisibility(View.VISIBLE);
                updates.get(position).removeAttendee(CurrentUser.currentUser.getUsername());
                updateAttendees(holder, position, attendees);
                Log.d("FeedUpdateAdapater", "Not Going");
                DatabaseManager.removeUserFromAttending(updates.get(position).getmMeetingPath(), CurrentUser.currentUser);
            }
        });


    }

    //display who/how many people are going
    public void updateAttendees(final FeedUpdateViewHolder holder, final int position, ArrayList<String> newAttendees) {

        if (newAttendees != null) {
            if (newAttendees.size() == 1) {
                holder.attendance.setText(newAttendees.get(0) + " is going!");
            } else if (newAttendees.size() == 0){
                holder.attendance.setText("No one is currently attending");
            }
            else if (newAttendees.size() == 2) {
                holder.attendance.setText(newAttendees.get(0) + " and " + newAttendees.get(1) + " are going!");
            } else {
                holder.attendance.setText(newAttendees.get(0) + " and " + (newAttendees.size() - 1) + " other people are going!");
            }
        }
    }

    @Override
    public int getItemCount() {
        return updates.size();
    }

    //set all the views in the layout
    public static class FeedUpdateViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView userName;
        TextView userLocation;
        TextView userText;
        TextView attendance;
        TextView timeStamp;
        CircleImageView personPhoto;
        ImageView updatePhoto;
        RelativeLayout inactiveComing;
        RelativeLayout activeComing;

        FeedUpdateViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.feed_update_card_view);
            userName = (TextView)itemView.findViewById(R.id.feed_update_user_name);
            userLocation = (TextView)itemView.findViewById(R.id.feed_update_user_location);
            personPhoto = (CircleImageView) itemView.findViewById(R.id.feed_update_profile_image);
            userText = (TextView)itemView.findViewById(R.id.feed_update_user_text);
            attendance = (TextView)itemView.findViewById(R.id.social_attendance);
            updatePhoto = (ImageView)itemView.findViewById(R.id.feed_update_image);
            timeStamp = (TextView)itemView.findViewById(R.id.feed_update_time_stamp);
            inactiveComing = (RelativeLayout)itemView.findViewById(R.id.feed_update_coming_button_inactive);
            activeComing = (RelativeLayout)itemView.findViewById(R.id.feed_update_coming_button_active);

        }
    }
}
