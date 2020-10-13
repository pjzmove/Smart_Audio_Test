/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.util;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qualcomm.qti.iotcontrollersdk.constants.ZigbeeDeviceType;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;

import com.qualcomm.qti.smartaudio.view.PlayingAnimationView;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTRepository;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTZigbeeDevice;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Utils {

    /**
     * The tag to identify this class when logging.
     */
    private static final String TAG = "Utils";

    /**
     * Check to see if the string is null or empty
     *
     * @param text
     *         the text to check
     *
     * @return true if string is null or empty
     */
    public static boolean isStringEmpty(final String text) {
        return (text == null) || text.trim().isEmpty();
    }

    public static boolean isActivityActive(final BaseActivity baseActivity) {
        return (baseActivity != null) && !baseActivity.isSaveStateCalled() && !baseActivity.isFinishing();
    }

    /**
     * This is a helper function that gets the status bar height.
     *
     * @return the status bar height
     */
    public static int getStatusBarHeight(final BaseActivity baseActivity) {
        if (!isActivityActive(baseActivity)) {
            return 0;
        }
        int resourceId = baseActivity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return (resourceId > 0) ? baseActivity.getResources().getDimensionPixelSize(resourceId) : 0;
    }

    /**
     * This is a helper function to get the bottom nav bar height.
     *
     * @return the bottm nav bar height
     */
    public static int getNavigationBarHeight(final BaseActivity baseActivity) {
        if (!isActivityActive(baseActivity)) {
            return 0;
        }
        int resourceId = baseActivity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return (resourceId > 0) ? baseActivity.getResources().getDimensionPixelSize(resourceId) : 0;
    }

    public static int getActionBarHeight(final BaseActivity baseActivity) {
        if (!isActivityActive(baseActivity)) {
            return 0;
        }
        ActionBar actionBar = baseActivity.getSupportActionBar();
        return (actionBar == null) ? 0 : actionBar.getHeight();
    }

    public static Rect getRectOnScreenFromView(View view) {
        if (view == null) {
            return null;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Rect(location[0], location[1], location[0] + view.getMeasuredWidth(),
                        location[1] + view.getMeasuredHeight());
    }

    public static int getRelativeTop(final View view, final BaseActivity baseActivity) {
        if (view.getParent() == view.getRootView()) {
            return view.getTop() - getStatusBarHeight(baseActivity);
        }
        else {
            return view.getTop() + getRelativeTop((View) view.getParent(), baseActivity);
        }
    }

    public static void expandView(final View view, int height, final int durationMs,
                                  final Interpolator interpolator, final Animator.AnimatorListener listener) {
        if (view == null) {
            return;
        }
        if (height < 0) {
            view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            height = view.getMeasuredHeight();
        }
        final int targetHeight = height;

        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofInt(1, targetHeight);
        animator.setInterpolator(interpolator);
        animator.setDuration(durationMs);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = (int) valueAnimator.getAnimatedValue();
                view.setLayoutParams(layoutParams);
            }
        });
        if (listener != null) {
            animator.addListener(listener);
        }
        animator.start();
    }

    public static void collapseView(final View view, int height, final int durationMs,
                                    final Interpolator interpolator, final Animator.AnimatorListener listener) {
        if (height < 0) {
            height = view.getMeasuredHeight();
        }
        final int currentHeight = height;

        ValueAnimator animator = ValueAnimator.ofInt(currentHeight, 0);
        animator.setInterpolator(interpolator);
        animator.setDuration(durationMs);
        animator.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = (int) valueAnimator.getAnimatedValue();
            view.setLayoutParams(layoutParams);
        });
        if (listener != null) {
            animator.addListener(listener);
        }
        animator.start();
    }

    public static void setNowPlayingSpeakerListItem(final Context context, final View view, final IoTGroup group) {
        if ((context == null) || (view == null) || (group == null)) {
            return;
        }
        RelativeLayout nowPlayingLayout = view.findViewById(R.id.node_now_playing_layout);
        LinearLayout speakerLayout = (LinearLayout) view.findViewById(R.id.speaker_layout);

        final MediaItem item = group.getCurrentItem();

        if (item == null) {
            // If it is idle speakers
            nowPlayingLayout.setVisibility(View.GONE);
            speakerLayout.setBackground(ResourcesCompat.getDrawable(context.getResources(),
                                                                    R.drawable.bgd_list_item_speaker, null));
        }
        else {
            final String itemTitle = ((item != null) && !Utils.isStringEmpty(item.getTitle())) ? item.getTitle() :
                    new String();
            final String itemAlbum = ((item != null) && !Utils.isStringEmpty(item.getAlbum())) ? item.getAlbum() :
                    new String();
            final String itemArtist = ((item != null) && !Utils.isStringEmpty(item.getArtist())) ? item.getArtist() :
                    new String();
            final String itemThumbnail = ((item != null) && !Utils.isStringEmpty(item.getThumbnailUrl())) ?
                    item.getThumbnailUrl() : null;

            nowPlayingLayout.setVisibility(View.VISIBLE);
            speakerLayout.setBackground(ResourcesCompat.getDrawable(context.getResources(),
                                                                    R.drawable.bgd_list_item_speaker_with_now_playing_normal, null));
            // Set the texts and image
            TextView titleText = view.findViewById(R.id.speaker_track_text);
            titleText.setText(itemTitle.toUpperCase());

            TextView albumText = view.findViewById(R.id.speaker_album_text);
            albumText.setText(itemAlbum);

            TextView artistText = view.findViewById(R.id.speaker_artist_text);
            artistText.setText(itemArtist);

            final PlayState playerState = group.getPlayerState();

            ImageView albumImage = view.findViewById(R.id.speaker_album_art);
            Picasso.get()
                    .load(itemThumbnail)
                    .placeholder(R.drawable.ic_album_art_default_med)
                    .error(R.drawable.ic_album_art_default_med)
                    .tag(context)
                    .into(albumImage);

            PlayingAnimationView playingAnimationView = view.findViewById(R.id.speaker_playing_animation);
            playingAnimationView.setPlayerState(playerState);
        }

        final TextView speakerText = view.findViewById(R.id.node_title);
        View mainView = view.findViewById(R.id.node_information_layout);
        mainView.setBackground(ResourcesCompat.getDrawable(context.getResources(),
                                                           R.drawable.bgd_speaker_name_text_layout, null));

        setZoneDisplayNameLayout(speakerText, group);
    }

    public static void setZoneDisplayNameLayout(final TextView speakerText, IoTGroup group) {
        speakerText.setText(getZoneDisplayName(speakerText.getContext(), group));
    }

    /**
     * <p>This method provides the display name of a group/zone: in the case of a single player it returns the
     * signle player display name, in the case of a group it builds the name depending on how many devices are in
     * the group.</p>
     *
     * @param context
     *         This method requires a context to access string resources.
     * @param group
     *         The zone to get a name for.
     *
     * @return The label in upper case.
     */
    public static String getZoneDisplayName(Context context, IoTGroup group) {
        final List<IoTPlayer> players = group.getPlayers();
        String name = group.isSinglePlayer() ? group.getDisplayName() :
                context.getResources().getQuantityString(R.plurals.group_name, players.size(),
                                                         group.getName(), players.size());
        return name.toUpperCase();
    }

    @DrawableRes
    public static int getWifiSignalResource(int signalLevel, boolean isSecure) {
        switch (signalLevel) {
            case 0:
                return isSecure ? R.drawable.ic_signal_level_lock_0_20dp : R.drawable.ic_signal_level_0_20dp;
            case 1:
                return isSecure ? R.drawable.ic_signal_level_lock_1_20dp :
                        R.drawable.ic_signal_level_1_20dp;
            case 2:
                return isSecure ? R.drawable.ic_signal_level_lock_2_20dp :
                        R.drawable.ic_signal_level_2_20dp;
            case 3:
                return isSecure ? R.drawable.ic_signal_level_lock_3_20dp :
                        R.drawable.ic_signal_level_3_20dp;
            case 4:
                return isSecure ? R.drawable.ic_signal_level_lock_4_20dp :
                        R.drawable.ic_signal_level_4_20dp;
            default:
                Log.w(TAG, "[getWifiSignalResource] invalid signal level: " + signalLevel);
                return R.drawable.ic_signal_level_unknown_20dp;
        }
    }

    public static String stripSSIDQuotes(String ssid) {
        if ((ssid != null) && (ssid.length() > 2) &&
                ssid.startsWith("\"") &&
                ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }


    /**
     * <p>This method goes through the type of the item in order to get the icon which corresponds to
     * the element.</p>
     *
     * @param element
     *         the element to get the representation for.
     *
     * @return The corresponding 23dp icon to be displayed in a list.
     */
    public static int getListIconFromIoTRepository(IoTRepository element) {
        switch (element.getType()) {
            case BLUETOOTH_DEVICE:
                return R.drawable.ic_bt_headphones_list_23dp;
            case ZIGBEE_DEVICE:
                return getListIconFromZigBeeType(((IoTZigbeeDevice) element).getZigbeeType());
            case GROUP:
                return R.drawable.ic_group_list_23dp;
            case SPEAKER:
            default:
                return R.drawable.ic_speaker_23dp;
        }
    }


    /**
     * <p>This method gets the icon which corresponds to the Zigbee type.</p>
     *
     * @param type
     *         the type to get the representation for.
     *
     * @return The corresponding 23dp icon to be displayed in a list.
     */
    private static int getListIconFromZigBeeType(ZigbeeDeviceType type) {
        switch (type) {
            case light:
                return R.drawable.ic_lightbulb_23dp;
            case thermostat:
                return R.drawable.ic_thermostat_23dp;
            case unknown:
            default:
                return R.drawable.ic_zigbee_static_23dp;
        }
    }
}
