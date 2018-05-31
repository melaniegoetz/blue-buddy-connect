package compsci290.edu.duke.coconutproject.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import compsci290.edu.duke.coconutproject.fragments.feedTab;
import compsci290.edu.duke.coconutproject.fragments.mapTab;
import compsci290.edu.duke.coconutproject.fragments.profileTab;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    //set the tabs for the main layout: one for feed, map, and profile
    @Override
    public Fragment getItem(int position) {

        mapTab map = new mapTab();
        feedTab feed = new feedTab();
        profileTab profile = new profileTab();

        switch (position) {
            case 0:
                return feed;
            case 1:
                return map;
            case 2:
                return profile;
            default:
                return map;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}