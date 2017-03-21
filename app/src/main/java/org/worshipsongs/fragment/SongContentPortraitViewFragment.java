package org.worshipsongs.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.youtube.player.YouTubePlayer;

import org.apache.commons.lang3.StringUtils;
import org.worshipsongs.CommonConstants;
import org.worshipsongs.WorshipSongApplication;
import org.worshipsongs.activity.CustomYoutubeBoxActivity;
import org.worshipsongs.adapter.PresentSongCardViewAdapter;
import org.worshipsongs.dao.AuthorSongDao;
import org.worshipsongs.dao.SongDao;
import org.worshipsongs.domain.AuthorSong;
import org.worshipsongs.domain.Setting;
import org.worshipsongs.domain.Song;
import org.worshipsongs.service.CustomTagColorService;
import org.worshipsongs.service.PresentationScreenService;
import org.worshipsongs.service.SongListAdapterService;
import org.worshipsongs.service.UserPreferenceSettingService;
import org.worshipsongs.worship.R;

import java.util.ArrayList;

/**
 * Author: Madasamy
 * version: 1.0.0
 */

public class SongContentPortraitViewFragment extends Fragment
{
    public static final String KEY_VIDEO_TIME = "KEY_VIDEO_TIME";
    private String title;
    private ArrayList<String> tilteList;
    private int millis;
    private YouTubePlayer youTubePlayer;
    private UserPreferenceSettingService preferenceSettingService;
    private SongDao songDao = new SongDao(WorshipSongApplication.getContext());
    private AuthorSongDao authorSongDao;
    private SongListAdapterService songListAdapterService;
    private FloatingActionsMenu floatingActionMenu;
    private Song song;
    private ListView listView;
    private PresentSongCardViewAdapter presentSongCardViewAdapter;
    private FloatingActionButton nextButton;
    private FloatingActionButton previousButton;
    //private int currentPosition;
    private FloatingActionButton presentSongFloatingButton;
    private PresentationScreenService presentationScreenService;
    private CustomTagColorService customTagColorService = new CustomTagColorService();


    public static SongContentPortraitViewFragment newInstance(String title, ArrayList<String> titles)
    {
        SongContentPortraitViewFragment songContentPortraitViewFragment = new SongContentPortraitViewFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(CommonConstants.TITLE_LIST_KEY, titles);
        bundle.putString(CommonConstants.TITLE_KEY, title);
        songContentPortraitViewFragment.setArguments(bundle);
        return songContentPortraitViewFragment;
    }

//    public static SongContentPortraitViewFragment newInstance()
//    {
//        return new SongContentPortraitViewFragment();
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        final View view = inflater.inflate(R.layout.song_content_portrait_view, container, false);
        initSetUp();
        setBackImageView(view);
        setTitleTextView(view);
        setOptionsImageView(view);
        setListView(view, song);
        setFloatingActionMenu(view, song);
        setNextButton(view);
        setPreviousButton(view);
        view.setOnTouchListener(new SongContentPortraitViewTouchListener());
        return view;
    }

    private void initSetUp()
    {
        showStatusBar();
        Bundle bundle = getArguments();
        title = bundle.getString(CommonConstants.TITLE_KEY);
        tilteList = bundle.getStringArrayList(CommonConstants.TITLE_LIST_KEY);
        if (bundle != null ) {
            millis = bundle.getInt(KEY_VIDEO_TIME);
            Log.i(this.getClass().getSimpleName(), "Video time " + millis);
        }
        song = songDao.findContentsByTitle(title);
        authorSongDao = new AuthorSongDao(getContext());
        AuthorSong authorSong = authorSongDao.findByTitle(song.getTitle());
        song.setAuthorName(authorSong.getAuthor().getDisplayName());
        preferenceSettingService = new UserPreferenceSettingService();
    }

    private void showStatusBar()
    {
        if (Build.VERSION.SDK_INT < 16) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

//    private void setActionBarLayout(View view)
//    {
//        actionBarlinearLayout = (LinearLayout) view.findViewById(R.id.action_bar_linear_Layout);
//    }

    private void setBackImageView(View view)
    {
        ImageView imageView = (ImageView) view.findViewById(R.id.back_navigation);
        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().finish();
            }
        });
    }

    private void setTitleTextView(View view)
    {
        TextView textView = (TextView) view.findViewById(R.id.song_title);
        textView.setText(title);
    }

    private void setOptionsImageView(View view)
    {
        ImageView optionMenu = (ImageView) view.findViewById(R.id.optionMenu);
        optionMenu.setOnClickListener(new OptionsImageClickListener());
    }


    private void setListView(View view, final Song song)
    {
        listView = (ListView) view.findViewById(R.id.content_list);
        presentSongCardViewAdapter = new PresentSongCardViewAdapter(getActivity(), song.getContents());
        listView.setAdapter(presentSongCardViewAdapter);
        listView.setOnItemClickListener(new ListViewOnItemClickListener());
        listView.setOnItemLongClickListener(new ListViewOnItemLongClickListener());
    }

    private void setFloatingActionMenu(final View view, Song song)
    {
        floatingActionMenu = (FloatingActionsMenu) view.findViewById(R.id.floating_action_menu);
        if (isPlayVideo(song.getUrlKey()) && isPresentSong()) {
            floatingActionMenu.setVisibility(View.VISIBLE);
            floatingActionMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener()
            {
                @Override
                public void onMenuExpanded()
                {
                    int color = R.color.gray_transparent;
                    setListViewForegroundColor(ContextCompat.getColor(getActivity(), color));
                }

                @Override
                public void onMenuCollapsed()
                {
                    int color = 0x00000000;
                    setListViewForegroundColor(color);
                }
            });
            setPlaySongFloatingMenuButton(view, song.getUrlKey());
            setPresentSongFloatingMenuButton(view);
        } else {
            floatingActionMenu.setVisibility(View.GONE);
            if (isPresentSong()) {
                setPresentSongFloatingButton(view);
            }
            if (isPlayVideo(song.getUrlKey())) {
                setPlaySongFloatingButton(view, song.getUrlKey());
            }
        }
    }

    private void setPlaySongFloatingMenuButton(View view, final String urrlKey)
    {
        FloatingActionButton playSongFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.play_song_floating_menu_button);
        if (isPlayVideo(urrlKey)) {
            playSongFloatingActionButton.setVisibility(View.VISIBLE);
            playSongFloatingActionButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    showYouTube(urrlKey);
                    if (floatingActionMenu.isExpanded()) {
                        floatingActionMenu.collapse();
                    }
                }
            });
        }
    }


    private void setPresentSongFloatingMenuButton(View view)
    {
        final FloatingActionButton presentSongFloatingMenuButton = (FloatingActionButton) view.findViewById(R.id.present_song_floating_menu_button);
        presentSongFloatingMenuButton.setVisibility(View.VISIBLE);
        presentSongFloatingMenuButton.setOnClickListener(new View.OnClickListener()
        {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View view)
            {
                if (floatingActionMenu.isExpanded()) {
                    floatingActionMenu.collapse();
                }
                if (presentationScreenService.getPresentation() != null) {
//                    currentPosition = 0;
//                    getPresentationScreenService().showNextVerse(song, currentPosition);
//                    presentSongCardViewAdapter.setItemSelected(0);
//                    presentSongCardViewAdapter.notifyDataSetChanged();
                    presentSelectedVerse(0);
                    floatingActionMenu.setVisibility(View.GONE);
                    // nextButton.setVisibility(View.VISIBLE);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    Toast.makeText(getActivity(), "Your device is not connected to any remote display", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setPresentSongFloatingButton(View view)
    {
        presentSongFloatingButton = (FloatingActionButton) view.findViewById(R.id.present_song_floating_button);
        presentSongFloatingButton.setVisibility(View.VISIBLE);
        presentSongFloatingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (presentationScreenService.getPresentation() != null) {
//                    currentPosition = 0;
//                    getPresentationScreenService().showNextVerse(song, currentPosition);
                    presentSongFloatingButton.setVisibility(View.GONE);
//                    presentSongCardViewAdapter.setItemSelected(0);
//                    presentSongCardViewAdapter.notifyDataSetChanged();
//                    nextButton.setVisibility(View.VISIBLE);
                    presentSelectedVerse(0);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    Toast.makeText(getActivity(), "Your device is not connected to any remote display", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setPlaySongFloatingButton(View view, final String urlKey)
    {
        FloatingActionButton playSongFloatingButton = (FloatingActionButton) view.findViewById(R.id.play_song_floating_button);
        playSongFloatingButton.setVisibility(View.VISIBLE);
        playSongFloatingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showYouTube(urlKey);
            }
        });
    }

    private void showYouTube(String urlKey)
    {
        Log.i(this.getClass().getSimpleName(), "Url key: " + urlKey);
        Intent youTubeIntent = new Intent(getActivity(), CustomYoutubeBoxActivity.class);
        youTubeIntent.putExtra(CustomYoutubeBoxActivity.KEY_VIDEO_ID, urlKey);
        youTubeIntent.putExtra("title", title);
        getActivity().startActivity(youTubeIntent);
    }


    private void setNextButton(View view)
    {
        nextButton = (FloatingActionButton) view.findViewById(R.id.next_verse_floating_button);
        nextButton.setVisibility(View.GONE);
        nextButton.setOnClickListener(new NextButtonOnClickListener());
    }

    private void setPreviousButton(View view)
    {
        previousButton = (FloatingActionButton) view.findViewById(R.id.previous_verse_floating_button);
        previousButton.setVisibility(View.GONE);
        previousButton.setOnClickListener(new PreviousButtonOnClickListener());
    }

    private class NextButtonOnClickListener implements View.OnClickListener
    {

        @Override
        public void onClick(View v)
        {
//            currentPosition = currentPosition + 1;
//            if ((song.getContents().size() - 1) == currentPosition) {
//                nextButton.setVisibility(View.GONE);
//            }
//            if (song.getContents().size() > currentPosition) {
//                getPresentationScreenService().showNextVerse(song, currentPosition);
//                listView.smoothScrollToPositionFromTop(currentPosition, 2);
//                previousButton.setVisibility(View.VISIBLE);
//                presentSongCardViewAdapter.setItemSelected(currentPosition);
//                presentSongCardViewAdapter.notifyDataSetChanged();
//
//            }

            int position = presentSongCardViewAdapter.getSelectedItem() + 1;
            listView.smoothScrollToPositionFromTop(position, 2);
            presentSelectedVerse(position <= song.getContents().size() ? position : (position - 1));
        }
    }

    private class PreviousButtonOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
//            currentPosition = currentPosition - 1;
//            if (currentPosition == song.getContents().size()) {
//                currentPosition = currentPosition - 1;
//            }
//            if (currentPosition <= song.getContents().size() && currentPosition >= 0) {
//                getPresentationScreenService().showNextVerse(song, currentPosition);
//                listView.smoothScrollToPosition(currentPosition, 2);
//                nextButton.setVisibility(View.VISIBLE);
//                presentSongCardViewAdapter.setItemSelected(currentPosition);
//                presentSongCardViewAdapter.notifyDataSetChanged();
//            }
//            if (currentPosition == 0) {
//                previousButton.setVisibility(View.GONE);
//            }
            int position = presentSongCardViewAdapter.getSelectedItem() - 1;
            int previousPosition = position >= 0 ? position : 0;
            listView.smoothScrollToPosition(previousPosition, 2);
            presentSelectedVerse(previousPosition);
        }
    }

    private class ListViewOnItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
//            if (isPlayVideo(song.getUrlKey())) {
//                if (floatingActionMenu != null && floatingActionMenu.getVisibility() == View.GONE && isPresentSong()) {
//                    presentSelectedVerse(position);
//                }
//            } else {
//                if (presentSongFloatingButton != null && presentSongFloatingButton.getVisibility() == View.GONE) {
//                    presentSelectedVerse(position);
//                }
//            }
            if (previousButton.getVisibility() == View.VISIBLE || nextButton.getVisibility() == View.VISIBLE) {
                listView.smoothScrollToPositionFromTop(position, 2);
                presentSelectedVerse(position);
            }
            if (floatingActionMenu != null && floatingActionMenu.isExpanded()) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                floatingActionMenu.collapse();
                int color = 0x00000000;
                setListViewForegroundColor(color);
            }
        }

//        private void presentSelectedVerse(int position)
//        {
//           // currentPosition = position;
//            getPresentationScreenService().showNextVerse(song, position);
//            listView.smoothScrollToPositionFromTop(position, 2);
//            presentSongCardViewAdapter.setItemSelected(position);
//            presentSongCardViewAdapter.notifyDataSetChanged();
//
//            previousButton.setVisibility(position <= 0 ? View.GONE : View.VISIBLE);
//            nextButton.setVisibility(position >= song.getContents().size() - 1 ? View.GONE : View.VISIBLE);
////            if (presentSongFloatingButton != null) {
////                presentSongFloatingButton.setVisibility(View.GONE);
////            }
////
////            if (position == 0) {
////                previousButton.setVisibility(View.GONE);
////                nextButton.setVisibility(View.VISIBLE);
////            } else if (song.getContents().size() == (position + 1)) {
////                nextButton.setVisibility(View.GONE);
////                previousButton.setVisibility(View.VISIBLE);
////            } else {
////                nextButton.setVisibility(View.VISIBLE);
////                previousButton.setVisibility(View.VISIBLE);
////            }
//        }
    }

    private void presentSelectedVerse(int position)
    {
        if (presentationScreenService.getPresentation() != null) {
            // currentPosition = position;
            getPresentationScreenService().showNextVerse(song, position);
            presentSongCardViewAdapter.setItemSelected(position);
            presentSongCardViewAdapter.notifyDataSetChanged();
            previousButton.setVisibility(position <= 0 ? View.GONE : View.VISIBLE);
            nextButton.setVisibility(position >= song.getContents().size() - 1 ? View.GONE : View.VISIBLE);
//            if (presentSongFloatingButton != null) {
//                presentSongFloatingButton.setVisibility(View.GONE);
//            }
//
//            if (position == 0) {
//                previousButton.setVisibility(View.GONE);
//                nextButton.setVisibility(View.VISIBLE);
//            } else if (song.getContents().size() == (position + 1)) {
//                nextButton.setVisibility(View.GONE);
//                previousButton.setVisibility(View.VISIBLE);
//            } else {
//                nextButton.setVisibility(View.VISIBLE);
//                previousButton.setVisibility(View.VISIBLE);
//            }
        }
    }

    private class ListViewOnItemLongClickListener implements AdapterView.OnItemLongClickListener
    {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            if (isCopySelectedVerse()) {
                String selectedVerse = song.getContents().get(position);
                presentSongCardViewAdapter.setItemSelected(position);
                presentSongCardViewAdapter.notifyDataSetChanged();
                shareSongInSocialMedia(selectedVerse);
            }
            return false;
        }

        void shareSongInSocialMedia(String selectedText)
        {
            String formattedContent = song.getTitle() + "\n\n" +
                    customTagColorService.getFormattedLines(selectedText) + "\n" + String.format(getString(R.string.verse_share_info), getString(R.string.app_name));
            Intent textShareIntent = new Intent(Intent.ACTION_SEND);
            textShareIntent.putExtra(Intent.EXTRA_TEXT, formattedContent);
            textShareIntent.setType("text/plain");
            Intent intent = Intent.createChooser(textShareIntent, "Share verse with...");
            getActivity().startActivity(intent);
        }

        boolean isCopySelectedVerse()
        {
            return !isPresentSong() || ((isPlayVideo(song.getUrlKey()) && floatingActionMenu != null && floatingActionMenu.getVisibility() == View.VISIBLE) ||
                    (presentSongFloatingButton != null && presentSongFloatingButton.getVisibility() == View.VISIBLE));

        }
    }

    private void setListViewForegroundColor(int color)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            listView.setForeground(new ColorDrawable(color));
        }
    }

    private boolean isPlayVideo(String urrlKey)
    {
        boolean playVideoStatus = preferenceSettingService.isPlayVideo();
        return urrlKey != null && urrlKey.length() > 0 && playVideoStatus;
    }

    private boolean isPresentSong()
    {
        return presentationScreenService != null && presentationScreenService.getPresentation() != null;
    }

//    private void setColorSettingsLayout(View view)
//    {
//        colorSettingsLayout = (LinearLayout) view.findViewById(R.id.color_settings);
//        colorSettingsLayout.setVisibility(View.GONE);
//    }

//    private void setPresentationBackgroundColor(View view)
//    {
//        final ImageView presentationBackgroundColor = (ImageView) view.findViewById(R.id.background_color);
//        presentationBackgroundColor.setColorFilter(preferenceSettingService.getPresentationBackgroundColor());
//        presentationBackgroundColor.setOnClickListener(new ColorSettingsOnClickListener(presentationBackgroundColor, "presentationBackgroundColor"));
//    }
//
//    private void setPresentationPrimaryColor(View view)
//    {
//        final ImageView presentationPrimaryColor = (ImageView) view.findViewById(R.id.primary_text_color);
//        presentationPrimaryColor.setColorFilter(preferenceSettingService.getPresentationPrimaryColor());
//        presentationPrimaryColor.setOnClickListener(new ColorSettingsOnClickListener(presentationPrimaryColor, "presentationPrimaryColor"));
//    }
//
//    private void setPresentationSecondaryColor(View view)
//    {
//        final ImageView presentationSecondaryColor = (ImageView) view.findViewById(R.id.secondary_text_color);
//        presentationSecondaryColor.setColorFilter(preferenceSettingService.getPresentationSecondaryColor());
//        presentationSecondaryColor.setOnClickListener(new ColorSettingsOnClickListener(presentationSecondaryColor, "presentationSecondaryColor"));
//    }
//
//    private class ColorSettingsOnClickListener implements View.OnClickListener
//    {
//
//        private final ImageView imageView;
//        private final String key;
//
//        private ColorSettingsOnClickListener(ImageView imageView, String key)
//        {
//            this.imageView = imageView;
//            this.key = key;
//        }
//
//        @Override
//        public void onClick(View view)
//        {
//            ColorPickerDialog dialog = new ColorPickerDialog(getContext(), preferenceSettingService.getPresentationSecondaryColor());
//            dialog.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener()
//            {
//                @Override
//                public void onColorChanged(int color)
//                {
//                    sharedPreferences.edit().putInt(key, color).apply();
//                    imageView.setColorFilter(color);
//                }
//            });
//            dialog.show();
//        }
//    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (youTubePlayer != null) {
            outState.putInt(KEY_VIDEO_TIME, youTubePlayer.getCurrentTimeMillis());
            Log.i(this.getClass().getSimpleName(), "Video duration: " + youTubePlayer.getCurrentTimeMillis());
        }
    }

    private class SongContentPortraitViewTouchListener implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            int position = tilteList.indexOf(title);
            Setting.getInstance().setPosition(position);
            return true;
        }
    }

    private class OptionsImageClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            songListAdapterService = new SongListAdapterService();
            songListAdapterService.showPopupmenu(view, title, getFragmentManager(), false);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        if (nextButton != null) {
            nextButton.setVisibility(View.GONE);
        }
        if (previousButton != null) {
            previousButton.setVisibility(View.GONE);
        }
        if (song != null && isPlayVideo(song.getUrlKey()) && isPresentSong() && floatingActionMenu != null) {
            floatingActionMenu.setVisibility(View.VISIBLE);
        } else if (presentSongFloatingButton != null) {
            presentSongFloatingButton.setVisibility(View.VISIBLE);
        }
        if (presentSongCardViewAdapter != null) {
            presentSongCardViewAdapter.setItemSelected(-1);
            presentSongCardViewAdapter.notifyDataSetChanged();
        }
        if (listView != null) {
            listView.smoothScrollToPosition(0);
        }
    }

    public PresentationScreenService getPresentationScreenService()
    {
        return presentationScreenService;
    }

    public void setPresentationScreenService(PresentationScreenService presentationScreenService)
    {
        this.presentationScreenService = presentationScreenService;
    }

}
