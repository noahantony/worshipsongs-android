package org.worshipsongs.fragment;


import android.app.SearchManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import org.apache.commons.lang3.StringUtils;
import org.worshipsongs.adapter.NewListAdapter;
import org.worshipsongs.dao.SongDao;
import org.worshipsongs.domain.Song;
import org.worshipsongs.listener.SongSelectionListener;
import org.worshipsongs.utils.CommonUtils;
import org.worshipsongs.worship.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author : Madasamy
 * Version : 3.x
 */

public class NewHomeFragment extends Fragment implements SongSelectionListener
{
    private SongDao songDao;
    private List<Song> songs;
    private NewListAdapter listAdapter;

    public static NewHomeFragment newInstance()
    {
        return new NewHomeFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.new_home, container, false);
        initSetUp();
        setListView(view);
        setHasOptionsMenu(true);
        return view;
    }

    private void initSetUp()
    {
        songDao = new SongDao(getActivity());
        songDao.open();
        songs = songDao.findAll();
    }

    private void setListView(View view)
    {
        ListView titleListView = (ListView) view.findViewById(R.id.title_list_view);
        listAdapter = new NewListAdapter(getActivity(), getFilteredTitles("", songs), getFragmentManager());
        listAdapter.setSongSelectionListener(this);
        titleListView.setAdapter(listAdapter);
    }

    @Override
    public void onClick(String title)
    {
        SongContentPortraitViewFragment youTubePlayerFragment = SongContentPortraitViewFragment.newInstance(title, new ArrayList<String>());
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.song_content_fragment, youTubePlayerFragment).commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.action_bar_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        ImageView image = (ImageView) searchView.findViewById(R.id.search_close_btn);
        Drawable drawable = image.getDrawable();
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                listAdapter.clear();
                listAdapter.setSelectedItem(-1);
                listAdapter.addAll(getFilteredTitles(query, songs));
                listAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                listAdapter.clear();
                listAdapter.setSelectedItem(-1);
                listAdapter.addAll(getFilteredTitles(newText, songs));
                listAdapter.notifyDataSetChanged();
                return true;
            }
        });


        Bitmap resizeBitmapIcon = resizeBitmapImageFn(BitmapFactory.decodeResource(getResources(), R.drawable.ic_filter), 35);
        Drawable resizedDrawable = new BitmapDrawable(getResources(), resizeBitmapIcon);
        menu.getItem(1).setIcon(resizedDrawable);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private Bitmap resizeBitmapImageFn(Bitmap bitmapSource, int maxResolution)
    {
        int iWidth = bitmapSource.getWidth();
        int iHeight = bitmapSource.getHeight();
        int newWidth = iWidth;
        int newHeight = iHeight;
        float rate = 0.0f;

        if (iWidth > iHeight) {
            if (maxResolution < iWidth) {
                rate = maxResolution / (float) iWidth;
                newHeight = (int) (iHeight * rate);
                newWidth = maxResolution;
            }
        } else {
            if (maxResolution < iHeight) {
                rate = maxResolution / (float) iHeight;
                newWidth = (int) (iWidth * rate);
                newHeight = maxResolution;
            }
        }
        return Bitmap.createScaledBitmap(bitmapSource, newWidth, newHeight, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.filter:
                Log.i(NewHomeFragment.class.getSimpleName(), "Filtered songs ");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(this.getClass().getSimpleName(), "Is visible to user ?" + isVisibleToUser);
        if (isVisibleToUser) {
            CommonUtils.hideKeyboard(getActivity());
        }
    }

    List<Song> getFilteredTitles(String text, List<Song> songs)
    {
        Set<Song> filteredSongSet = new HashSet<>();
        if (StringUtils.isNotBlank(text)) {
            for (Song song : songs) {
                if (getTitles(song.getSearchTitle()).toString().toLowerCase().contains(text.toLowerCase())) {
                    filteredSongSet.add(song);
                }
                if (song.getSearchLyrics().toLowerCase().contains(text.toLowerCase())) {
                    filteredSongSet.add(song);
                }
            }
        } else {
            filteredSongSet.addAll(songs);
        }
        List<Song> filteredSongs = new ArrayList<>(filteredSongSet);
        Collections.sort(filteredSongs, new SongComparator());
        return filteredSongs;
    }

    List<String> getTitles(String searchTitle)
    {
        List<String> titles = new ArrayList<>();
        titles.addAll(Arrays.asList(searchTitle.split("@")));
        return titles;
    }

    private class SongComparator implements Comparator<Song>
    {
        @Override
        public int compare(Song song1, Song song2)
        {
            return song1.getTitle().compareTo(song2.getTitle());
        }
    }
}


