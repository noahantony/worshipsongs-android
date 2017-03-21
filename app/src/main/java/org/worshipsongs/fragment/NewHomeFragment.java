package org.worshipsongs.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.youtube.player.YouTubePlayerFragment;

import org.worshipsongs.adapter.NewListAdapter;
import org.worshipsongs.dao.SongDao;
import org.worshipsongs.domain.Song;
import org.worshipsongs.listener.SongSelectionListener;
import org.worshipsongs.worship.R;

import java.util.ArrayList;
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

    public static NewHomeFragment newInstance()
    {
        return new NewHomeFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.new_home, container, false);
        initSetUp();
        setListView(view);
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
        NewListAdapter listAdapter = new NewListAdapter(getContext(), getFilteredTitles("", songs), getFragmentManager());
        listAdapter.setSongSelectionListener(this);
        titleListView.setAdapter(listAdapter);
    }

    List<Song> getFilteredTitles(String text, List<Song> songs)
    {
        Set<Song> filteredSongSet = new HashSet<>();
        for (Song song : songs) {
            filteredSongSet.add(song);
        }
        List<Song> filteredSongs = new ArrayList<>(filteredSongSet);
        Collections.sort(filteredSongs, new SongComparator());
        return filteredSongs;
    }

    @Override
    public void onClick(String title)
    {
        SongContentPortraitViewFragment youTubePlayerFragment = SongContentPortraitViewFragment.newInstance(title, new ArrayList<String>());
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.song_content_fragment, youTubePlayerFragment).commit();
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


