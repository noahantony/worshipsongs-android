package org.worshipsongs.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaRouter;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import org.worshipsongs.dialog.RemoteSongPresentation;
import org.worshipsongs.domain.Setting;
import org.worshipsongs.domain.Song;

import java.util.Set;

/**
 * Author : Madasamy
 * Version : 3.x
 */

public class PresentationScreenService
{
    private Context context;
    private RemoteSongPresentation remoteSongPresentation;
    private PresentationScreenService.DefaultMediaRouterCallBack songMediaRouterCallBack = new PresentationScreenService.DefaultMediaRouterCallBack();
    private MediaRouter mediaRouter;


    public PresentationScreenService(Context context)
    {
        this.context = context;
        mediaRouter = (MediaRouter) context.getSystemService(Context.MEDIA_ROUTER_SERVICE);
    }

    public void onResume()
    {
        if (isJellyBean()) {
            mediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, songMediaRouterCallBack);
            updatePresentation();
        }
    }


    public void onPause()
    {
        if (isJellyBean()) {
            mediaRouter.removeCallback(songMediaRouterCallBack);
        }
    }

    public void onStop()
    {
        if (remoteSongPresentation != null) {
            remoteSongPresentation.dismiss();
            remoteSongPresentation = null;
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    class DefaultMediaRouterCallBack extends MediaRouter.SimpleCallback
    {

        @Override
        public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info)
        {
            updatePresentation();

        }

        @Override
        public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info)
        {
            updatePresentation();

        }

        @Override
        public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info)
        {
            updatePresentation();
        }

    }

    private final DialogInterface.OnDismissListener remoteDisplayDismissListener =
            new DialogInterface.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog)
                {
                    if (dialog == remoteSongPresentation) {
                        remoteSongPresentation = null;
                    }

                }
            };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updatePresentation()
    {
        Display selectedDisplay = getSelectedDisplay();
        if (remoteSongPresentation != null && remoteSongPresentation.getDisplay() != selectedDisplay) {
            remoteSongPresentation.dismiss();
            remoteSongPresentation = null;
        }

        if (remoteSongPresentation == null && selectedDisplay != null) {
            remoteSongPresentation = new RemoteSongPresentation(context, selectedDisplay);
            remoteSongPresentation.setOnDismissListener(remoteDisplayDismissListener);
            try {
                remoteSongPresentation.show();

            } catch (WindowManager.InvalidDisplayException ex) {
                // Couldn't show presentation - display was already removed
                remoteSongPresentation = null;
            }
        }
        if(remoteSongPresentation!= null && selectedDisplay != null){
            if(Setting.getInstance().getSong() != null) {
                showNextVerse(Setting.getInstance().getSong(), Setting.getInstance().getSlidePosition());
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private Display getSelectedDisplay()
    {
        MediaRouter.RouteInfo selectedRoute = mediaRouter.getSelectedRoute(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        Display selectedDisplay = null;
        if (selectedRoute != null) {
            selectedDisplay = selectedRoute.getPresentationDisplay();
        }
        return selectedDisplay;
    }


    private boolean isJellyBean()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public void showNextVerse(Song song, int position)
    {
        if (remoteSongPresentation != null) {
            remoteSongPresentation.setVerseVisibility(View.VISIBLE);
            remoteSongPresentation.setImageViewVisibility(View.GONE);
            remoteSongPresentation.setVerse(song.getContents().get(position));
            remoteSongPresentation.setSongTitleAndChord(song.getTitle(), song.getChord());
            remoteSongPresentation.setAuthorName(song.getAuthorName());
            remoteSongPresentation.setSlidePosition(position, song.getContents().size());
            Setting.getInstance().setSong(song);
            Setting.getInstance().setSlidePosition(position);
        }
    }

    public RemoteSongPresentation getPresentation(){
        return remoteSongPresentation;
    }

}
