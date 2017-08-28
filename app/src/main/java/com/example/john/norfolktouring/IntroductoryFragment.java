package com.example.john.norfolktouring;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Shows an introductory video from YouTube using the YouTube Android Player API.
 */
public class IntroductoryFragment extends Fragment implements YouTubePlayer.OnInitializedListener,
        YouTubePlayer.PlaybackEventListener, YouTubePlayer.PlayerStateChangeListener {
    /*** Member Variables ***/
    private MainActivity mActivity;

    /**
     * YouTube Player
     **/
    private YouTubePlayerFragment mYouTubePlayerFragment;
    private YouTubePlayer mYouTubePlayer;
    private String mVideoId = "mlqCDeaGyCY";
    private int mSeekTimeMillis;

    /**
     * Media Session
     **/
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private NotificationManager mNotificationManager;
    private boolean mAllowMediaNotifications = true;

    /**
     * Constants
     **/

    private static final String LOG_TAG = IntroductoryFragment.class.getCanonicalName();
    public static final String FRAGMENT_LABEL = "introduction";
    private static final String MEDIA_NOTIFICATION_TAG = "mediaNotification";

    /*** Methods ***/

    /**
     * Lifecycle Methods
     **/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOG_TAG, "Calling onCreateView()!");
        mActivity = (MainActivity) getActivity();

        NorfolkTouring.setActionBarTitleToAppName((AppCompatActivity) mActivity);
        View rootView = inflater.inflate(R.layout.introduction, container, false);

        // Record that this Fragment is the currently displayed one in `MainActivity`.
        mActivity.setCurrentFragment(this);

        initializeMediaSession();
        initializeYouTubePlayerFragment();

        return rootView;
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "Calling onResume()!");
        super.onResume();
        // Allow Media Session notifications.
        mAllowMediaNotifications = true;
        // Reinitialize the YouTubePlayerFragment -
        // this allows the Media Style notification to work properly after `onStop()`.
        initializeYouTubePlayerFragment();
    }

    @Override
    public void onPause() {
        Log.i(LOG_TAG, "Calling onPause()!");
        super.onPause();
        // Remove the Media Session notification.
        mAllowMediaNotifications = false;
        if (mNotificationManager != null)
            mNotificationManager.cancel(MEDIA_NOTIFICATION_TAG, 0);
        updateSeekTime();
    }

    @Override
    public void onStop() {
        Log.i(LOG_TAG, "Calling onStop()!");
        super.onStop();
        mYouTubePlayerFragment = null;
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "Calling onDestroy()!");
        super.onDestroy();
        // Clean up the Media Session.
        mMediaSession.setActive(false);
        mMediaSession.release();
    }

    private void initializeYouTubePlayerFragment() {
        if (mYouTubePlayerFragment == null) {
            mYouTubePlayerFragment = new YouTubePlayerFragment();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.youtube_fragment, mYouTubePlayerFragment,
                            "YouTube Player")
                    .commit();
        }
        mYouTubePlayerFragment.initialize(NorfolkTouring.YOUTUBE_DEVELOPER_KEY, this);
    }

    /** `YouTubePlayer.OnInitializedListener` Methods **/

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                        YouTubePlayer player,
                                        boolean wasRestored) {
        Log.i(LOG_TAG, "Calling onInitializationSuccess()!");
        mYouTubePlayer = player;
        mYouTubePlayer.setPlaybackEventListener(this);
        mYouTubePlayer.setPlayerStateChangeListener(this);
        if (!wasRestored)
            mYouTubePlayer.cueVideo(mVideoId);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult youTubeInitializationResult) {
        Log.i(LOG_TAG, "Calling onInitializationFailure()!");
        Log.e(LOG_TAG, "YouTube player failed to initialize!");
    }

    /** `YouTubePlayer.PlaybackEventListener` Methods **/

    @Override
    public void onPlaying() {
        Log.i(LOG_TAG, "Calling onPlaying()!");
        updateSeekTime();
        updateMediaSessionAndNotification(PlaybackStateCompat.STATE_PLAYING);
    }

    @Override
    public void onPaused() {
        Log.i(LOG_TAG, "Calling onPaused()!");
        updateSeekTime();
        updateMediaSessionAndNotification(PlaybackStateCompat.STATE_PAUSED);
    }

    @Override
    public void onStopped() {
        Log.i(LOG_TAG, "Calling onStopped()!");
        // This is called when the video has finished cuing (coincidentally), so don't overwrite
        // the seek time before seeking to it in `onLoaded()` after reinitializing
        // the `YouTubePlayerFragment`.
        if (mYouTubePlayer.getCurrentTimeMillis() != 0)
            updateSeekTime();
        updateMediaSessionAndNotification(PlaybackStateCompat.STATE_STOPPED);
    }

    @Override
    public void onBuffering(boolean b) {
        Log.i(LOG_TAG, "Calling onBuffering()!");
        updateSeekTime();
        updateMediaSessionAndNotification(PlaybackStateCompat.STATE_BUFFERING);
    }

    @Override
    public void onSeekTo(int i) {
        Log.i(LOG_TAG, "Calling onSeekTo()!");
        updateSeekTime();
    }

    /** YouTubePlayer.PlayerStateChangeListener **/

    @Override
    public void onLoading() {}

    @Override
    public void onLoaded(String s) {
        Log.i(LOG_TAG, "Calling onLoaded()!");
        // This is called when the video has finished cuing, so check if we need to seek
        // to the current time after reinitializing the YouTubePlayerFragment.
        if (mYouTubePlayer.getCurrentTimeMillis() == 0 && mSeekTimeMillis != 0)
            mYouTubePlayer.seekToMillis(mSeekTimeMillis);
    }

    @Override
    public void onAdStarted() {}

    @Override
    public void onVideoStarted() {}

    @Override
    public void onVideoEnded() {}

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {}

    /** Media Session **/

    /**
     * Initializes the Media Session to be enabled to accept input
     * from media buttons and transport controls.
     * <p>
     * Derived from Udacity - Advanced Android App Development - Lesson 6: Media Playback.
     */
    private void initializeMediaSession() {
        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(mActivity, LOG_TAG);

        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState that allows the player to be restarted, played, and paused.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(mStateBuilder.build());

        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);
    }

    /**
     * Shows a Media Style notification, with an action that depends on the current MediaSession
     * PlaybackState.
     * <p>
     * Derived from Udacity - Advanced Android App Development - Lesson 6: Media Playback.
     *
     * @param state The PlaybackState of the MediaSession.
     */
    private void showMediaNotification(PlaybackStateCompat state) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity);

        // Choose icons to display for play/pause button.
        int icon;
        String play_pause;
        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
            icon = R.drawable.ic_pause_white_24dp;
            play_pause = getString(R.string.pause);
        } else {
            icon = R.drawable.ic_play_arrow_white_24dp;
            play_pause = getString(R.string.play);
        }

        // Create notification Actions for play/pause toggle and restart.
        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(mActivity,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));

        NotificationCompat.Action restartAction = new NotificationCompat.Action(
                R.drawable.ic_skip_previous_white_24dp, getString(R.string.restart),
                MediaButtonReceiver.buildMediaButtonPendingIntent
                        (mActivity, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        // Create the notification.
        builder.setContentTitle(getString(R.string.introductory_video_media_notification_title))
                .setContentText(getString(R.string.introductory_video_media_notification_text))
                .setSmallIcon(R.drawable.ic_music_note)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(restartAction)
                .addAction(playPauseAction)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1));
        Notification mediaStyleNotification = builder.build();
        // Issue the notification.
        mNotificationManager = (NotificationManager) mActivity.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(MEDIA_NOTIFICATION_TAG, 0, mediaStyleNotification);
    }

    /**
     * Updates the seek time (in milliseconds) for the YouTube video.
     */
    private void updateSeekTime() {
        if (mYouTubePlayer != null)
            mSeekTimeMillis = mYouTubePlayer.getCurrentTimeMillis();
        else
            mSeekTimeMillis = 0;
    }

    /**
     * Updates the Media Session and the notification that allows control of the video playback.
     */
    private void updateMediaSessionAndNotification(int state) {
        mStateBuilder.setState(state,
                mYouTubePlayer.getCurrentTimeMillis(), 1f);
        updateMediaSessionAndNotification();
    }

    /**
     * Updates the Media Session and the notification that allows control of the video playback.
     */
    private void updateMediaSessionAndNotification() {
        mMediaSession.setPlaybackState(mStateBuilder.build());
        if (mAllowMediaNotifications)
            showMediaNotification(mStateBuilder.build());
    }

    /**
     * Media Session Callbacks, where all external clients control the YouTube player.
     * <p>
     * Derived from Udacity - Advanced Android App Development - Lesson 6: Media Playback.
     */
    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mYouTubePlayer.play();
        }

        @Override
        public void onPause() {
            mYouTubePlayer.pause();
        }

        @Override
        public void onSkipToPrevious() {
            mYouTubePlayer.seekToMillis(0);
        }
    }

    /**
     * The Media Receiver, which forwards MediaButton Intents to MediaSession callbacks.
     */
    public static class MediaReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }
}