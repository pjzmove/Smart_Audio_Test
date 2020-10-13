/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.adapter;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.model.WifiNetwork;
import com.qualcomm.qti.smartaudio.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>This adapter manages a list of speakers to display within a {@link android.widget.ListView
 * ListView} and the display of each item within the UI.</p>
 */
public class NewSpeakerAdapter extends BaseAdapter {

    // ========================================================================
    // PRIVATE FIELDS

    /**
     * The list of data managed by this adapter.
     */
    private final List<WifiNetwork> mSpeakers;


    // ========================================================================
    // CONSTRUCTOR

    /**
     * Default constructor to build a new instance of this adapter.
     */
    public NewSpeakerAdapter() {
        mSpeakers = new ArrayList<>();
    }


    // ========================================================================
    // OVERRIDE METHODS

    @Override
    public int getCount() {
        return mSpeakers.size();
    }

    @Override
    public Object getItem(int position) {
        return mSpeakers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final WifiNetwork speaker = (WifiNetwork) getItem(position);
        Holder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.list_item_new_speaker, parent, false);
            holder = new Holder(convertView);
        }
        else {
            holder = (Holder) convertView.getTag();
        }

        holder.update(parent.getContext(), speaker);

        return convertView;
    }


    // ========================================================================
    // PUBLIC METHODS

    /**
     * <p>To set the list of potential speakers to display.</p>
     * <p>This method discards the previous list.</p>
     * <p>This method updates the UI.</p>
     *
     * @param speakers
     *         The lists of potential speakers to display.
     */
    public void setList(final List<WifiNetwork> speakers) {
        mSpeakers.clear();
        if (speakers != null) {
            mSpeakers.addAll(speakers);
            Collections.sort(mSpeakers);
        }
        notifyDataSetChanged();
    }


    // ========================================================================
    // INNER CLASS

    /**
     * <p>This view holder binds a speaker to the UI elements of the layout
     * {@link R.layout#list_item_new_speaker list_item_new_speaker}.</p>
     */
    private class Holder {

        /**
         * The maximum number of levels for the signal strength.
         */
        private static final int LEVEL_MAX = 5;
        /**
         * The text view used to display the name of the speaker.
         */
        private TextView mTextView;
        /**
         * The image used to display the strength of the signal.
         */
        private ImageView mImageView;
        /**
         * The view managed by this holder.
         */
        private View mmView;

        /**
         * <p>The constructor which will instantiate the views to use for this holder.</p>
         *
         * @param view
         *         The main view which contains all the views this holder should use.
         */
        Holder(final View view) {
            mmView = view;
            mTextView = view.findViewById(R.id.new_speaker_display_name_text_view);
            mImageView = view.findViewById(R.id.setup_new_speaker_display_level_image_view);
            view.setTag(this);
        }

        /**
         * <p>This method is for refreshing all the values displayed in the corresponding view which show all
         * information
         * related to a speaker.</p>
         *
         * @param context
         *         The context to use to load images for the view.
         * @param speaker
         *         The information to display.
         */
        public void update(Context context, final WifiNetwork speaker) {
            mTextView.setText(speaker.getSSID());
            int signalStrength = WifiManager.calculateSignalLevel(speaker.getLevel(), LEVEL_MAX);
            mImageView.setImageDrawable(context.getDrawable(
                    Utils.getWifiSignalResource(signalStrength, !speaker.isOpenNetwork())));
            mmView.setContentDescription(context.getString(R.string.cont_desc_button_select_speaker,
                                                           speaker.getSSID()));
        }
    }
}
