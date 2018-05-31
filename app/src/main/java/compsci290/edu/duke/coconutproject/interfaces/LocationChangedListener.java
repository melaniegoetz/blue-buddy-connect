package compsci290.edu.duke.coconutproject.interfaces;

import compsci290.edu.duke.coconutproject.models.DukeLocation;

/**
 * Created by Scott on 5/1/17.
 */

public interface LocationChangedListener {
    public void onLocationChanged(DukeLocation location);
}
