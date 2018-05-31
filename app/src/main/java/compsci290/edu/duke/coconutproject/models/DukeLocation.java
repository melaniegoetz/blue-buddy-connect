package compsci290.edu.duke.coconutproject.models;

import com.google.android.gms.maps.model.Polygon;
import android.support.annotation.Nullable;

/**
 * Created by SPalumbo on 4/18/17.
 */

////DukeLocation class; Object for attaching buildings on Duke's campus to their coordinates
public class DukeLocation {
    private double[] mCoords = new double[2];
    private String mCampus;
    private String mBuilding;
    private Polygon mMapPolygon;
    private String mCategory;

    public DukeLocation(double latCoord, double longCoord, @Nullable String building, @Nullable String campus, @Nullable String category) {
        this.mCoords[0] = latCoord;
        this.mCoords[1] = longCoord;
        this.mBuilding = building;
        this.mCampus = campus;
        this.mCategory = category;
    }

    //for locations of which we've Polygon bounded
    public DukeLocation(String building, Polygon poly) {
        this.mMapPolygon = poly;
        this.mCampus = null;
        this.mCategory = null;
        this.mBuilding = building;
    }

    public DukeLocation(String name) {
        this.mBuilding = name;
    }

    public double[] getCoords() {
        return mCoords;
    }
    public String getCampus() {
        return mCampus;
    }
    public String getBuilding() {
        return mBuilding;
    }

    public void setmBuildingName(String building) {
        this.mBuilding = building;
    }

    public void setmMapPolygon(Polygon poly) {
        this.mMapPolygon = poly;
    }
    public String getmCategory() { return mCategory; }

    public Polygon getmMapPolygon() {
        return mMapPolygon;
    }
}