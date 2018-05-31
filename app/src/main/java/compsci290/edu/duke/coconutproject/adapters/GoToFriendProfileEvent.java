package compsci290.edu.duke.coconutproject.adapters;

import compsci290.edu.duke.coconutproject.models.User;

/**
 * Created by melaniegoetz on 5/3/17.
 */

public class GoToFriendProfileEvent {
    public User friend;
    public GoToFriendProfileEvent(User user) {
        this.friend=user;

    }
}
