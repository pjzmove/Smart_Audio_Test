/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 *  ************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.interfaces.IFragmentControl;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnCurrentGroupVolumeChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnPlayerVolumeChangedListener;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.PlayerGroupInfo;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.IoTSysInfo;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTRepository;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceDetailsFragment extends BaseFragment implements OnPlayerVolumeChangedListener,
        OnCurrentGroupVolumeChangedListener {

    private static final String TAG = "DeviceDetailsFragment";

    /*package*/ final static String EXTRA_KEY_IOT_OBJECT_ID = "IOT_OBJECT_ID_FOR_DD";
    /*package*/ final static String EXTRA_KEY_IOT_OBJECT_TYPE = "IOT_OBJECT_TYPE_FOR_DD";

    private String mIoTRepositoryId;
    private IoTType mIoTObjectType;

    private DeviceActionAdapter mAdapter;
    private WeakReference<IFragmentControl> mController;

    public static DeviceDetailsFragment newInstance(String id, IoTType type) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_KEY_IOT_OBJECT_ID, id);
        bundle.putInt(EXTRA_KEY_IOT_OBJECT_TYPE, type.getValue());
        DeviceDetailsFragment fragment = new DeviceDetailsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setController(IFragmentControl controller) {
        mController = new WeakReference<>(controller);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_device_details, container, false);
        Bundle bundle = getArguments();

        mIoTRepositoryId = bundle.getString(EXTRA_KEY_IOT_OBJECT_ID);
        mIoTObjectType = IoTType.fromValue(bundle.getInt(EXTRA_KEY_IOT_OBJECT_TYPE));

        String name = "";
        String host = "";

        //TODO: Implement functionality in controller SDK
        String manufacturerName = "";
        String modelNumber = "";

        int imageResource = -1;
        IoTPlayer player = null;
        IoTGroup group = null;

        List<ActionType> actionList = new ArrayList<>();
        actionList.add(ActionType.VOLUME_CONTROL);
        actionList.add(ActionType.AUDIO_SOURCE);
        actionList.add(ActionType.OUTPUT_DESTINATION);

        switch (mIoTObjectType) {
            case GROUP: {
                group = IoTService.getInstance().getGroupById(mIoTRepositoryId);
                if (group != null) {
                    name = group.getDisplayName();
                    if (group.getLeadPlayer() != null) {
                        host = group.getLeadPlayer().getHostName();
                    }
                }
                imageResource = R.drawable.ic_device_group_131x117dp;
            }
            break;
            case PLAYER:
            case SATELLITE_SPEAKER:
            case SPEAKER: {
                player = IoTService.getInstance().getPlayerById(mIoTRepositoryId);
                if (player != null) {
                    name = player.getName();
                    host = player.getHostName();
                }
                imageResource = R.drawable.ic_device_speaker_70x95dp;
            }
            break;
            case SOUND_BAR: {
                player = IoTService.getInstance().getPlayerById(mIoTRepositoryId);
                host = player.getHostName();
                name = player.getName();
                actionList.add(ActionType.SURROUND_SYSTEM);
                imageResource = R.drawable.ic_device_soundbar_256x58dp;
            }
        }

        // set up the image depending on device's type
        if (imageResource != -1) {
            ImageView image = view.findViewById(R.id.device_image);
            image.setImageResource(imageResource);
        }

        TextView playerNameText = view.findViewById(R.id.player_name);
        playerNameText.setText(name);
        playerNameText.setContentDescription(getString(R.string.cont_desc_details_zone_name, name));

        RecyclerView actionListView = view.findViewById(R.id.device_action_list_view);
        actionListView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        actionListView.setLayoutManager(layoutManager);
        DividerItemDecoration decoration = new DividerItemDecoration(actionListView.getContext(),
                                                                     layoutManager.getOrientation());
        actionListView.addItemDecoration(decoration);

        if (group != null || (player != null && player.isLicensed())) {
            actionList.add(ActionType.GROUP_DEVICE);
        }

        mAdapter = new DeviceActionAdapter(actionList);
        actionListView.setAdapter(mAdapter);

        showIndicators(view, host);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mAllPlayManager != null) {
            mAllPlayManager.addOnCurrentZoneVolumeChangedListener(this);
            mAllPlayManager.addOnPlayerVolumeChangedListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAllPlayManager != null) {
            mAllPlayManager.removeOnCurrentZoneVolumeChangedListener(this);
            mAllPlayManager.removeOnPlayerVolumeChangedListener(this);
        }
    }

    public enum ActionType {
        VOLUME_CONTROL,
        AUDIO_SOURCE,
        OUTPUT_DESTINATION,
        SURROUND_SYSTEM,
        GROUP_DEVICE
    }

    private class ActionItemViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mContainer;
        TextView mTitle;
        TextView mSubTitle;
        LinearLayout mSeekBarControl;
        SeekBar mVolumeControl;
        ImageView mIndicator;

        public ActionItemViewHolder(View v) {
            super(v);
            mContainer = v.findViewById(R.id.device_action_layout);
            mTitle = v.findViewById(R.id.device_action_title);
            mSubTitle = v.findViewById(R.id.device_action_subtitle);
            mSeekBarControl = v.findViewById(R.id.seekbar_control);
            mVolumeControl = v.findViewById(R.id.volume_control_seekbar);
            mIndicator = v.findViewById(R.id.indicator);
        }
    }

    private class DeviceActionAdapter extends RecyclerView.Adapter<ActionItemViewHolder> {

        List<ActionType> mActionList;

        public DeviceActionAdapter(List<ActionType> list) {
            mActionList = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ActionItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.device_detail_action_item_view, parent, false);
            ActionItemViewHolder vh = new ActionItemViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull ActionItemViewHolder viewHolder, int i) {
            ActionType type = mActionList.get(i);
            switch (type) {
                case VOLUME_CONTROL:
                    viewHolder.mIndicator.setVisibility(View.GONE);
                    viewHolder.mSubTitle.setVisibility(View.GONE);
                    viewHolder.mSeekBarControl.setVisibility(View.VISIBLE);
                    IoTGroup group = null;
                    IoTPlayer player = null;
                    if (mIoTObjectType == IoTType.GROUP) {
                        group = IoTService.getInstance().getGroupById(mIoTRepositoryId);
                        if (group != null) {
                            viewHolder.mVolumeControl.setMax(group.getMaxMasterVolume());
                            viewHolder.mVolumeControl.setProgress(group.getVolume());
                            viewHolder.mTitle.setText(String.format(getString(R.string.group_detail_volume_control),
                                                                    group.getVolume(),
                                                                    getString(R.string.percentage_symbol)));
                            viewHolder.mContainer.setContentDescription(getString(R.string.cont_desc_menu_item,
                                                                                  getString(R.string.cont_desc_details_group_volume)));
                        }
                    }
                    else if (mIoTObjectType == IoTType.PLAYER ||
                            mIoTObjectType == IoTType.SPEAKER ||
                            mIoTObjectType == IoTType.SOUND_BAR ||
                            mIoTObjectType == IoTType.SATELLITE_SPEAKER) {
                        player = IoTService.getInstance().getPlayerById(mIoTRepositoryId);
                        if (player != null) {
                            viewHolder.mVolumeControl.setMax(player.getMaxVolume());
                            viewHolder.mVolumeControl.setProgress(player.getVolume());
                            viewHolder.mTitle.setText(String.format(getString(R.string.player_detail_volume_control),
                                                                    player.getVolume(),
                                                                    getString(R.string.percentage_symbol)));
                            viewHolder.mContainer.setContentDescription(getString(R.string.cont_desc_menu_item,
                                                                                  getString(R.string.cont_desc_details_player_volume)));
                        }
                    }

                    IoTPlayer tempPlayer = player;
                    IoTGroup tempGroup = group;
                    viewHolder.mVolumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (!fromUser) {
                                return;
                            }
                            viewHolder.mTitle.setText(String.format(getString(R.string.player_detail_volume_control),
                                                                    progress,
                                                                    getString(R.string.percentage_symbol)));
                            viewHolder.mContainer.setContentDescription(getString(R.string.cont_desc_menu_item,
                                                                                  getString(R.string.cont_desc_details_player_volume)));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            if (tempGroup != null) {
                                tempGroup.setVolume(seekBar.getProgress());
                            }
                            else if (tempPlayer != null) {
                                tempPlayer.setVolume(((double) seekBar.getProgress()) / 100.0f, success -> {
                                });
                            }
                        }
                    });

                    break;
                case AUDIO_SOURCE:
                    viewHolder.mTitle.setText(getString(R.string.device_detail_audio_source));
                    viewHolder.mContainer.setContentDescription(getString(R.string.cont_desc_menu_item,
                                                                          getString(R.string.cont_desc_details_audio_source)));
                    viewHolder.mIndicator.setVisibility(View.VISIBLE);
                    viewHolder.mSeekBarControl.setVisibility(View.GONE);
                    group = mAllPlayManager.getCurrentGroup();
                    if (group != null) {
                        player = group.getLeadPlayer();
                        if (player != null) {
                            String inputSource = player.getActiveInputSource();
                            if (inputSource != null && !inputSource.isEmpty()) {
                                viewHolder.mSubTitle.setVisibility(View.VISIBLE);
                            }
                            viewHolder.mSubTitle.setText(inputSource);
                        }
                        else {
                            viewHolder.mSubTitle.setVisibility(View.GONE);
                        }
                    }
                    viewHolder.mContainer.setOnClickListener(v -> mController.get().onDeviceDetailsItemClick(ActionType.AUDIO_SOURCE));
                    break;
                case OUTPUT_DESTINATION:
                    viewHolder.mTitle.setText(getString(R.string.output_source_selection));
                    viewHolder.mContainer.setContentDescription(getString(R.string.cont_desc_menu_item,
                                                                          getString(R.string.cont_desc_details_output_destinations)));
                    viewHolder.mIndicator.setVisibility(View.VISIBLE);
                    viewHolder.mSeekBarControl.setVisibility(View.GONE);
                    group = mAllPlayManager.getCurrentGroup();
                    if (group != null) {
                        player = group.getLeadPlayer();
                        if (player != null) {
                            List<String> outputs = player.getActiveOutputSource();
                            String outputInfo = "";
                            for (String output : outputs) {
                                if (output != null && !output.isEmpty()) {
                                    outputInfo += output + " ";
                                }
                            }

                            if (outputInfo.equalsIgnoreCase("")) {
                                viewHolder.mSubTitle.setVisibility(View.GONE);
                            }
                            else {
                                viewHolder.mSubTitle.setVisibility(View.VISIBLE);
                                viewHolder.mSubTitle.setText(outputInfo);
                            }
                        }
                    }
                    viewHolder.mContainer.setOnClickListener(v -> mController.get().onDeviceDetailsItemClick(ActionType.OUTPUT_DESTINATION));
                    break;
                case SURROUND_SYSTEM:
                    viewHolder.mTitle.setText(getString(R.string.device_detail_surround_system));
                    viewHolder.mContainer.setContentDescription(getString(R.string.cont_desc_menu_item,
                                                                          getString(R.string.cont_desc_details_surround_system)));
                    viewHolder.mIndicator.setVisibility(View.VISIBLE);
                    viewHolder.mSubTitle.setText(getString(R.string.device_detail_surround_system_detail));
                    viewHolder.mSeekBarControl.setVisibility(View.GONE);
                    viewHolder.mContainer.setOnClickListener(v -> mController.get().onDeviceDetailsItemClick(ActionType.SURROUND_SYSTEM));
                    break;
                case GROUP_DEVICE:
                    viewHolder.mIndicator.setVisibility(View.VISIBLE);
                    viewHolder.mSeekBarControl.setVisibility(View.GONE);
                    if (mIoTObjectType == IoTType.GROUP) {
                        group = IoTService.getInstance().getGroupById(mIoTRepositoryId);
                        if (group != null) {
                            viewHolder.mTitle.setText(getString(R.string.devices_in_group));
                            viewHolder.mContainer.setContentDescription(getString(R.string.cont_desc_menu_item,
                                                                                  getString(R.string.cont_desc_details_group_list)));
                            viewHolder.mSubTitle.setText(group.getName());
                        }
                    }
                    else if (mIoTObjectType == IoTType.PLAYER ||
                            mIoTObjectType == IoTType.SPEAKER ||
                            mIoTObjectType == IoTType.SOUND_BAR ||
                            mIoTObjectType == IoTType.SATELLITE_SPEAKER) {

                        List<PlayerGroupInfo> infoList = IoTService.getInstance().getGroupInfo(mIoTRepositoryId);
                        if (infoList == null || infoList.isEmpty()) {
                            viewHolder.mTitle.setText(getString(R.string.group_with_other_speakers));
                            viewHolder.mContainer.setContentDescription(getString(R.string.cont_desc_menu_item,
                                                                                  getString(R.string.cont_desc_details_group_with_other_speakers)));
                            viewHolder.mSubTitle.setText(getString(R.string.ungrouped));

                        }
                        else {
                            List<PlayerGroupInfo> matchedInfoList = infoList.stream().
                                    filter(info -> info.mMembers.stream().anyMatch(member -> member.deviceId.equalsIgnoreCase(mIoTRepositoryId)))
                                    .collect(Collectors.toList());

                            if (matchedInfoList != null && !matchedInfoList.isEmpty()) {
                                String groups = "";
                                int size = matchedInfoList.size();
                                int idx = 0;
                                for (PlayerGroupInfo info : matchedInfoList) {
                                    groups += info.mGroupName;
                                    if (idx < (size - 1)) {
                                        groups += ",";
                                    }
                                }
                                viewHolder.mTitle.setText(getString(R.string.devices_in_group));
                                viewHolder.mContainer.setContentDescription(getString(R.string.cont_desc_menu_item,
                                                                                      getString(R.string.cont_desc_details_group_list)));
                                viewHolder.mSubTitle.setText(groups);
                            }
                            else {
                                viewHolder.mTitle.setText(getString(R.string.group_with_other_speakers));
                                viewHolder.mContainer.setContentDescription(getString(R.string.cont_desc_menu_item,
                                                                                      getString(R.string.cont_desc_details_group_with_other_speakers)));
                                viewHolder.mSubTitle.setText(getString(R.string.ungrouped));
                            }
                        }
                    }
                    viewHolder.mContainer.setOnClickListener(v -> mController.get().onDeviceDetailsItemClick(ActionType.GROUP_DEVICE));
                    break;
                default:
                    break;
            }
        }

        @Override
        public int getItemCount() {
            if (mActionList == null) {
                return 0;
            }
            return mActionList.size();
        }
    }

    @Override
    public void onCurrentGroupVolumeStateChanged(int volume, boolean user) {
        UiThreadExecutor.getInstance().execute(() -> mAdapter.notifyDataSetChanged());
    }

    @Override
    public void onCurrentZoneVolumeEnabledChanged(boolean enabled) {

    }

    @Override
    public void onCurrentZoneMuteStateChanged(boolean muted) {

    }

    @Override
    public void onPlayerVolumeStateChanged(IoTPlayer player, int volume, boolean user) {
        if (player.getPlayerId().equalsIgnoreCase(mIoTRepositoryId)) {
            UiThreadExecutor.getInstance().execute(() -> mAdapter.notifyDataSetChanged());
        }
    }

    @Override
    public void onPlayerVolumeEnabledChanged(IoTPlayer player, boolean enabled) {

    }

    @Override
    public void onPlayerMuteStateChanged(IoTPlayer player, boolean muted) {

    }

    /**
     * <p>This method sets up all the view BT and ZB indicators of the given IoT element.</p>
     *
     * @param view
     *         The view which represents the given IoT element.
     * @param host
     *         The host to display the indicators for.
     */
    private void showIndicators(View view, String host) {
        // get the list of children types
        List<IoTType> childrenTypes = getZoneChildrenTypes(host);

        // set up the BT & ZB indicators
        boolean connectivityVisible = setIndicatorVisibility(view, IoTType.BLUETOOTH_DEVICE, childrenTypes)
                || setIndicatorVisibility(view, IoTType.ZIGBEE_DEVICE, childrenTypes);

        // set up the AVS indicator
        IoTSysInfo info = IoTService.getInstance().getDeviceByHost(host);
        View avsIndicator = view.findViewById(R.id.avs_ind);
        boolean voiceVisible = info != null && info.isAvsOnBoarded;
        avsIndicator.setVisibility(voiceVisible ? View.VISIBLE : View.GONE);

        // display the middle divider
        int dividerVisibility = connectivityVisible && voiceVisible ? View.VISIBLE : View.GONE;
        view.findViewById(R.id.indicators_vertical_divider).setVisibility(dividerVisibility);

        // display the container
        int visibility = connectivityVisible || voiceVisible ? View.VISIBLE : View.GONE;
        view.findViewById(R.id.indicators_top_divider).setVisibility(visibility);
        view.findViewById(R.id.indicators_container).setVisibility(visibility);
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
     *
     * @return True if the indicator is visible, false otherwise.
     */
    private boolean setIndicatorVisibility(View parent, IoTType type, List<IoTType> childrenTypes) {
        // get the indicator resource id
        int id = type.equals(IoTType.BLUETOOTH_DEVICE) ? R.id.bluetooth_ind :
                type.equals(IoTType.ZIGBEE_DEVICE) ? R.id.zigbee_ind : -1;

        // get the indicator view
        View indicator = parent.findViewById(id);
        if (indicator == null) {
            return false;
        }

        // set up the visibility
        int visibility = childrenTypes != null && childrenTypes.contains(type) ?
                View.VISIBLE : View.GONE;
        indicator.setVisibility(visibility);
        return visibility == View.VISIBLE;
    }

    /**
     * <p>To get the list of types the children of the given host has.</p>
     * <p>This method gets the list of children of the host and builds the list of sub types from
     * the children.</p>
     *
     * @param host
     *         The host to get the children types from.
     *
     * @return The list of subtypes for the zone.
     */
    private List<IoTType> getZoneChildrenTypes(String host) {
        IoTDevice device = mAllPlayManager.getDeviceByHostName(host);

        if (device == null) {
            Log.d(TAG, "getZoneChildrenTypes: null device");
            return null;
        }

        // get list of child devices
        List<IoTRepository> subDeviceList = new ArrayList<>(device.getList());

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
}
