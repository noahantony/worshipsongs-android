package org.worshipsongs.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.worshipsongs.CommonConstants;
import org.worshipsongs.WorshipSongApplication;
import org.worshipsongs.activity.SongContentViewActivity;
import org.worshipsongs.dao.SongDao;
import org.worshipsongs.dialog.ListDialogFragment;
import org.worshipsongs.domain.Setting;
import org.worshipsongs.domain.Song;
import org.worshipsongs.listener.SongSelectionListener;
import org.worshipsongs.service.CustomTagColorService;
import org.worshipsongs.service.UserPreferenceSettingService;
import org.worshipsongs.utils.CommonUtils;
import org.worshipsongs.worship.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Author : Madasamy
 * Version : 3.x
 */

public class NewListAdapter extends ArrayAdapter<Song>
{
    private RelativeLayout relativeLayout;

    private int selectedItem = -1;
    private List<Song> songs;
    private final FragmentManager fragmentManager;
    private SongSelectionListener songSelectionListener;
    private Activity activity;

    private UserPreferenceSettingService preferenceSettingService = new UserPreferenceSettingService();
    private CustomTagColorService customTagColorService = new CustomTagColorService();
    private SongDao songDao = new SongDao(getContext());

    public NewListAdapter(@NonNull Activity activity, List<Song> songs, FragmentManager fragmentManager)
    {
        super(activity, R.layout.songs_listview_content, songs);
        this.songs = songs;
        this.fragmentManager = fragmentManager;
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(R.layout.songs_listview_content, null);
        }
        setRelativeLayout(view);
        Song song = getItem(position);
        setTextView(view, song.getTitle(), position);
        setPlayImageView(view, song);
        setImageView(view, song.getTitle());
        return view;
    }

    private void setRelativeLayout(View view)
    {
        relativeLayout = (RelativeLayout) view.findViewById(R.id.song_list_layout);
    }

    private void setTextView(View rowView, final String title, final int position)
    {
        final TextView textView = (TextView) rowView.findViewById(R.id.songsTextView);
        textView.setText(title);

        textView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View arg0)
            {
                CommonUtils.hideKeyboard(activity);
                setSelectedItem(position);
                notifyDataSetChanged();
                displaySelectedSong(title, position);
            }
        });
        if (selectedItem == position) {
            textView.setBackgroundResource(R.color.gray);
            relativeLayout.setBackgroundColor(getContext().getResources().getColor(R.color.gray));
        } else {
            textView.setBackgroundResource(R.color.white);
            relativeLayout.setBackgroundColor(getContext().getResources().getColor(R.color.white));
        }
    }

    private void displaySelectedSong(String title, int position)
    {
        Setting.getInstance().setPosition(position);
        if (getContext().getResources().getBoolean(R.bool.isTablet)) {
            songSelectionListener.onSelectSong(title);
        } else {
            Intent intent = new Intent(WorshipSongApplication.getContext(), SongContentViewActivity.class);
            Bundle bundle = new Bundle();
            ArrayList<String> songList = new ArrayList<String>();
            for (Song song : songs) {
                songList.add(song.getTitle());
            }
            bundle.putStringArrayList(CommonConstants.TITLE_LIST_KEY, new ArrayList<String>(songList));
            // Setting.getInstance().setPosition(position);
            //bundle.putInt(CommonConstants.POSITION_KEY, position);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            WorshipSongApplication.getContext().startActivity(intent);
        }
    }

    private void setPlayImageView(final View rowView, final Song song)
    {
        ImageView imageView = (ImageView) rowView.findViewById(R.id.play_imageview);
        final String urlKey = song.getUrlKey();
        imageView.setVisibility(View.GONE);
        if (urlKey != null && urlKey.length() > 0 && preferenceSettingService.isPlayVideo()) {
            imageView.setVisibility(View.VISIBLE);
        }
        imageView.setOnClickListener(onClickPopupListener(song.getTitle()));
    }

    private void setImageView(View rowView, final String songTitle)
    {
        ImageView imageView = (ImageView) rowView.findViewById(R.id.optionMenuIcon);
        imageView.setOnClickListener(onClickPopupListener(songTitle));
    }

    private View.OnClickListener onClickPopupListener(final String title)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showPopupmenu(view, title, true);
            }
        };
    }

    public void showPopupmenu(View view, final String songName, boolean hidePlay)
    {
        Context wrapper = new ContextThemeWrapper(WorshipSongApplication.getContext(), R.style.PopupMenu_Theme);
        final PopupMenu popupMenu;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            popupMenu = new PopupMenu(wrapper, view, Gravity.RIGHT);
        } else {
            popupMenu = new PopupMenu(wrapper, view);
        }
        popupMenu.getMenuInflater().inflate(R.menu.favourite_share_option_menu, popupMenu.getMenu());
        final Song song = songDao.findContentsByTitle(songName);
        final String urlKey = song.getUrlKey();
        MenuItem menuItem = popupMenu.getMenu().findItem(R.id.play_song);

        menuItem.setVisible(urlKey != null && urlKey.length() > 0 && preferenceSettingService.isPlayVideo() && hidePlay);
//        MenuItem presentSongMenuItem = popupMenu.getMenu().findItem(R.id.present_song);
//        presentSongMenuItem.setVisible(false);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            public boolean onMenuItemClick(final MenuItem item)
            {
                switch (item.getItemId()) {
                    case R.id.addToList:
                        ListDialogFragment listDialogFragment = ListDialogFragment.newInstance(songName);
                        listDialogFragment.show(fragmentManager, "");
                        return true;
                    case R.id.share_whatsapp:
                        shareSongInSocialMedia(songName, song);
                        return true;
                    case R.id.play_song:
                        // showYouTube(urlKey, songName);
                        return true;
//                    case R.id.present_song:
//                        // startPresentActivity(songName);
//                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void shareSongInSocialMedia(String songName, Song song)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(songName).append("\n").append("\n");
        for (String content : song.getContents()) {
            builder.append(customTagColorService.getFormattedLines(content));
            builder.append("\n");
        }
        builder.append(WorshipSongApplication.getContext().getString(R.string.share_info));

        Intent textShareIntent = new Intent(Intent.ACTION_SEND);
        textShareIntent.putExtra(Intent.EXTRA_TEXT, builder.toString());
        textShareIntent.setType("text/plain");
        Intent intent = Intent.createChooser(textShareIntent, "Share " + songName + " with...");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        WorshipSongApplication.getContext().startActivity(intent);
    }


    public int getSelectedItem()
    {
        return selectedItem;
    }

    public void setSelectedItem(int selectedItem)
    {
        this.selectedItem = selectedItem;
    }

    public void setSongSelectionListener(SongSelectionListener songSelectionListener)
    {
        this.songSelectionListener = songSelectionListener;
    }
}
