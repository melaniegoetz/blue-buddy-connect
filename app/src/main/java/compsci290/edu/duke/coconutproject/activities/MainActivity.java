package compsci290.edu.duke.coconutproject.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import compsci290.edu.duke.coconutproject.adapters.PagerAdapter;
import compsci290.edu.duke.coconutproject.R;

public class MainActivity extends FragmentActivity {

    private TabLayout tabLayout;
    public static Typeface font;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        font = Typeface.createFromAsset(getAssets(), "centurygothic.ttf");

        TabLayout.Tab feed = tabLayout.newTab().setText("Feed");
        tabLayout.addTab(feed);

        TabLayout.Tab map = tabLayout.newTab().setText("Map");
        tabLayout.addTab(map);

        TabLayout.Tab profile = tabLayout.newTab().setText("Profile");
        tabLayout.addTab(profile);

        changeTabsFont();
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.setCurrentItem(1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
       // EventBus.getDefault().register(this);
    }

    private void changeTabsFont() {

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(font);
                    ((TextView) tabViewChild).setTextColor(Color.parseColor("#ffffff"));
                }
            }
        }
    }
}