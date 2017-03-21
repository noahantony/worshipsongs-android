package org.worshipsongs.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.worshipsongs.component.HomeViewerPageAdapter;
import org.worshipsongs.component.SlidingTabLayout;
import org.worshipsongs.worship.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * author:Seenivasan, Madasamy
 * version:2.1.0
 */
public class HomeTabFragment extends Fragment
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = (View) inflater.inflate(R.layout.home_tab_layout, container, false);
        List<String> titles = Arrays.asList(getResources().getString(R.string.titles), getResources().getString(R.string.artists), getResources().getString(R.string.playlists));
        // Creating The HomeViewerPageAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.ome
        Log.i(this.getClass().getSimpleName(), "Preparing to load home view fragment");
        HomeViewerPageAdapter adapter = new HomeViewerPageAdapter(getChildFragmentManager(), titles);
        adapter.notifyDataSetChanged();

        // Assigning ViewPager View and setting the adapter
        ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        setTabs(view, pager);
       // setSongContentFragment();
        Log.i(this.getClass().getSimpleName(), "Finished loading home fragment");
        return view;
    }

    private void setTabs(View view, ViewPager pager)
    {
        SlidingTabLayout tabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        tabs.setDistributeEvenly(getResources().getBoolean(R.bool.isTablet));
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer()
        {
            @Override
            public int getIndicatorColor(int position)
            {
                return getResources().getColor(android.R.color.white);
            }
        });
        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
    }

    private void setSongContentFragment()
    {
        if (getResources().getBoolean(R.bool.isTablet)) {
//            SongContentPortraitViewFragment songContentPortraitViewFragment = SongContentPortraitViewFragment.newInstance("", new ArrayList<String>());
//            FragmentTransaction transaction = getFragmentManager().beginTransaction();
//            transaction.add(R.id.content_fragment, songContentPortraitViewFragment).commit();
        }
    }
}