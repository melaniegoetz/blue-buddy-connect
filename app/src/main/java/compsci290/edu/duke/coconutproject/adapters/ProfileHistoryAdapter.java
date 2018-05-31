package compsci290.edu.duke.coconutproject.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import compsci290.edu.duke.coconutproject.R;
import compsci290.edu.duke.coconutproject.activities.MainActivity;
import compsci290.edu.duke.coconutproject.models.Meeting;

//adapter for the Recycler Grid View of past events posted on one's profile
public class ProfileHistoryAdapter extends RecyclerView.Adapter<ProfileHistoryAdapter.ProfileHistoryViewHolder>{
    List<Meeting> updates;

    public ProfileHistoryAdapter(List<Meeting> updates) {
        this.updates=updates;
    }

    @Override
    public ProfileHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_profile_history, parent, false);
        ProfileHistoryViewHolder phvh = new ProfileHistoryViewHolder(v);
        return phvh;
    }

    //set all the views with the appropriate data
    @Override
    public void onBindViewHolder(final ProfileHistoryViewHolder holder, int position) {
        holder.meetingLocation.setText(updates.get(position).getLocation().getBuilding());
        holder.meetingTime.setText(updates.get(position).getTime());
        holder.meetingAttendance.setText("attended by " + (1+ updates.get(position).getAttendees().size()));
        Glide.with(holder.itemView.getContext()).load(updates.get(position).getImage()).override(600,600).centerCrop().into(holder.meetingPhoto);
        if(updates.get(position).getmBitmap()!=null){
            holder.meetingPhoto.setAdjustViewBounds(true);
            holder.meetingPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.meetingPhoto.setImageBitmap(updates.get(position).getmBitmap());
        }
    }

    @Override
    public int getItemCount() {
        return updates.size();
    }

    public static class ProfileHistoryViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView meetingLocation;
        TextView meetingTime;
        TextView meetingAttendance;
        ImageView meetingPhoto;


        ProfileHistoryViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.profile_history_card_view);
            meetingLocation = (TextView)itemView.findViewById(R.id.profile_history_meeting_location);
            meetingLocation.setTypeface(MainActivity.font);
            meetingTime = (TextView)itemView.findViewById(R.id.profile_history_meeting_time);
            meetingAttendance = (TextView)itemView.findViewById(R.id.profile_history_meeting_attendees);
            meetingPhoto = (ImageView)itemView.findViewById(R.id.profile_history_meeting_image);
            meetingPhoto.setScaleType(ImageView.ScaleType.CENTER);

        }
    }
}
