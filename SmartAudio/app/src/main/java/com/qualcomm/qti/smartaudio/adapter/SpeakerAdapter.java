/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTRepository;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.smartaudio.view.PlayPauseButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>This class binds the data of a list of {@link IoTGroup IoTGroup} with an
 * {@link ExpandableListView ExpandableListView} in order to display IoT resources within two categories: playing
 * zones and available (other) zones/speakers.</p>
 */
public class SpeakerAdapter extends BaseExpandableListAdapter {

    // ========================================================================
    // FIELDS

    /**
     * The listener to dispatch actions and get information for the adapter.
     */
    private final @NonNull
    SpeakerAdapterListener mListener;
    /**
     * The list of zones which currently playing on the network.
     */
    private List<IoTGroup> mNowPlayingGroups = Collections.synchronizedList(new ArrayList<>());
    /**
     * The list of zones which are available and not playing.
     */
    private List<IoTGroup> mIdleGroups = Collections.synchronizedList(new ArrayList<>());


    // ========================================================================
    // CONSTRUCTOR

    /**
     * Default constructor to build a new instance of this adapter.
     */
    public SpeakerAdapter(SpeakerAdapterListener listener) {
        this.mListener = listener;
    }


    // ========================================================================
    // OVERRIDE METHODS

    @Override // BaseExpandableListAdapter
    public int getGroupCount() {
        int count = 0;
        count += (mNowPlayingGroups.size() > 0) ? 1 : 0;
        count += (mIdleGroups.size() > 0) ? 1 : 0;
        return count;
    }

    @Override // BaseExpandableListAdapter
    public int getChildrenCount(int groupId) {
        if (groupId == 0) {
            if (mNowPlayingGroups.size() > 0) {
                return mNowPlayingGroups.size();
            }
        }
        return mIdleGroups.size();
    }

    @Override // BaseExpandableListAdapter
    public Object getGroup(int position) {
        if (position == 0) {
            if (mNowPlayingGroups.size() > 0) {
                return mListener.getContext().getString(R.string.what_is_playing);
            }
        }
        return mListener.getContext().getString(R.string.available_speakers);
    }

    @Override // BaseExpandableListAdapter
    public Object getChild(int groupId, int position) {
        if (groupId == 0) {
            if (mNowPlayingGroups.size() > 0) {
                return (position < mNowPlayingGroups.size()) ? mNowPlayingGroups.get(position) : null;
            }
        }
        return (position < mIdleGroups.size()) ? mIdleGroups.get(position) : null;
    }

    @Override // BaseExpandableListAdapter
    public long getGroupId(int position) {
        return position;
    }

    @Override // BaseExpandableListAdapter
    public long getChildId(int groupId, int position) {
        return position;
    }

    @Override // BaseExpandableListAdapter
    public boolean hasStableIds() {
        return false;
    }

    @Override // BaseExpandableListAdapter
    public View getGroupView(int position, boolean b, View convertView, ViewGroup parent) {
        // Here we create the sections
        if (convertView == null) {
            convertView = LayoutInflater
                    .from(parent.getContext()).inflate(R.layout.list_item_speaker_section, parent, false);
        }

        TextView sectionText = convertView.findViewById(R.id.speaker_section_view);
        sectionText.setText((String) getGroup(position));

        int description = position == 0 ? R.string.cont_desc_section_what_is_playing :
                R.string.cont_desc_section_speakers;
        convertView.setContentDescription(parent.getResources().getString(description));

        ExpandableListView listView = (ExpandableListView) parent;
        listView.expandGroup(position);

        return convertView;
    }

    @Override // BaseExpandableListAdapter
    public View getChildView(int groupId, int position, boolean b, View convertView, ViewGroup parent) {
        final IoTGroup zone = (IoTGroup) getChild(groupId, position);

        if (convertView == null) {
            convertView = LayoutInflater
                    .from(parent.getContext()).inflate(R.layout.list_item_speaker, parent, false);
        }

        ImageView speakerIndicator = convertView.findViewById(R.id.node_icon);
        ImageButton expandBtn = convertView.findViewById(R.id.node_options_button);
        if (zone.isSinglePlayer()) {
            speakerIndicator.setImageResource(R.drawable.ic_speaker_23dp);
            IoTPlayer player = zone.getLeadPlayer();
            if (player != null) {
                expandBtn.setVisibility(View.VISIBLE);
                expandBtn.setContentDescription(parent.getResources().getString(R.string.cont_desc_zone_menu,
                                                                                zone.getDisplayName()));
                expandBtn.setOnClickListener(view -> {
                    mListener.onZoneMenuSelected(zone);
                });
            }
        }
        else {
            expandBtn.setVisibility(View.INVISIBLE);
            speakerIndicator.setImageResource(R.drawable.ic_group_list_23dp);
        }

        final View itemView = convertView;
        Utils.setNowPlayingSpeakerListItem(mListener.getContext(), itemView, zone);

        PlayPauseButton playPauseButton = itemView.findViewById(R.id.speaker_album_art_play_pause_button);
        playPauseButton.setBaseActivity(mListener.getBaseActivity());
        playPauseButton.setGroup(zone);

        boolean pauseEnabled = zone.isInterruptible() && zone.isPauseEnabled();
        double alpha = pauseEnabled ? 1.0 : 0.5;
        playPauseButton.setEnabled(pauseEnabled);
        playPauseButton.setAlpha((float) alpha);

        // display BT and ZB indicators
        showIndicators(itemView, zone);

        // add content description
        itemView.setContentDescription(parent.getResources().getString(R.string.cont_desc_zone_music_selection,
                                                                       zone.getDisplayName()));

        // add action
        itemView.setOnClickListener(view -> {
            mListener.onZoneSelected(zone);
        });

        return itemView;
    }

    @Override // BaseExpandableListAdapter
    public boolean isChildSelectable(int groupId, int position) {
        return true;
    }


    // ========================================================================
    // PUBLIC METHODS

    /**
     * This function updates the internal data and notifies changes
     *
     * @param nowPlayingZones
     *         the now playing zones
     * @param idleZones
     *         the idle zones
     */
    public void updateSpeakers(final List<IoTGroup> nowPlayingZones, final List<IoTGroup> idleZones) {
        mNowPlayingGroups.clear();
        if (nowPlayingZones != null) {
            mNowPlayingGroups.addAll(nowPlayingZones);
        }
        mIdleGroups.clear();
        if (idleZones != null) {
            mIdleGroups.addAll(idleZones);
        }
        notifyDataSetChanged();
    }

    public void setGroupFragmentZoneID(final String zoneID) {
        synchronized (SpeakerAdapter.this) {
            notifyDataSetChanged();
        }
    }


    // ========================================================================
    // PRIVATE METHODS

    /**
     * <p>This method sets up all the view BT and ZB indicators of the given IoT element.</p>
     *
     * @param view
     *         The view which represents the given IoT element.
     * @param group
     *         The element to display the indicators for.
     */
    private void showIndicators(View view, IoTGroup group) {
        // get the list of children types
        List<IoTType> childrenTypes = getZoneChildrenTypes(group.isSinglePlayer() ? group.getLeadPlayer() : group);

        // set up the indicators
        setIndicatorVisibility(view, IoTType.BLUETOOTH_DEVICE, childrenTypes);
        setIndicatorVisibility(view, IoTType.ZIGBEE_DEVICE, childrenTypes);
    }

    /**
     * <p>This method sets up the visibility of the indicator which corresponds to the given type
     * depending on the presence of the given type in the children.</p>
     *
     * @param parent
     *         The view which contains the indicator view.
     * @param type
     *         The type to display.
     * @param childrenTypes
     *         The types of all children.
     */
    private void setIndicatorVisibility(View parent, IoTType type, List<IoTType> childrenTypes) {
        // get the indicator resource id
        int id = type.equals(IoTType.BLUETOOTH_DEVICE) ? R.id.node_bluetooth_indicator :
                type.equals(IoTType.ZIGBEE_DEVICE) ? R.id.node_zigbee_indicator : -1;

        // get the indicator view
        View indicator = parent.findViewById(id);
        if (indicator == null) {
            return;
        }

        // set up the visibility
        indicator.setVisibility(childrenTypes != null && childrenTypes.contains(type) ?
                                        View.VISIBLE : View.GONE);
    }

    /**
     * <p>To get the list of types the children of the given zone has.</p>
     * <p>This method gets the list of children of the zone and builds the list of sub types from
     * the children.</p>
     *
     * @param zone
     *         The zone to get the children types from.
     *
     * @return The list of subtypes for the zone.
     */
    private List<IoTType> getZoneChildrenTypes(IoTRepository zone) {
        // get list of child devices
        List<IoTRepository> subDeviceList = buildChildrenList(zone);

        if (subDeviceList == null || subDeviceList.isEmpty()) {
            // the device has no child
            return null;
        }

        // prepare list of subtypes
        List<IoTType> subTypes = new ArrayList<>();

        for (IoTRepository subDevice : subDeviceList) {
            if (subDevice != null) {
                // keep the type of the device
                IoTType type = subDevice.getType();
                subTypes.add(type); // types can be duplicated
            }
        }

        return subTypes;
    }

    /**
     * <p>To build the list of children devices of the given parent.</p>
     * <p>This method looks for physical elements which are children of the given parent.</p>
     *
     * @param parent
     *         The parent to get the list of children elements for.
     *
     * @return The list of children.
     */
    private List<IoTRepository> buildChildrenList(IoTRepository parent) {
        if (parent == null) {
            // no element
            return null;
        }

        // get list of child devices
        List<IoTRepository> childrenList = new ArrayList<>();

        // if the iot element is a player, gets its corresponding IoTDevice to also get the children devices
        if (parent instanceof IoTPlayer) {
            IoTDevice device = mListener.getDeviceByHostName(((IoTPlayer) parent).getPlayerHost());
            if (device != null) {
                childrenList.addAll(device.getList());
            }
        }

        return childrenList;
    }


    // ========================================================================
    // INTERFACE

    /**
     * This interface allows the adapter to communicate with the element which controls the
     * RecyclerView. Such as a fragment or an activity.
     */
    public interface SpeakerAdapterListener {

        /**
         * <p>To get the context of the application.</p>
         *
         * @return The context of the application.
         */
        Context getContext();

        /**
         *
         */
        void onZoneSelected(final IoTRepository object);

        /**
         *
         */
        void onZoneMenuSelected(final IoTRepository object);

        /**
         *
         */
        BaseActivity getBaseActivity();

        /**
         * <p></p>
         */
        IoTDevice getDeviceByHostName(String host);

    }
}
