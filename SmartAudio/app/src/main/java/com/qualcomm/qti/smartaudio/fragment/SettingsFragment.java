/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;

import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupListChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupInfoStateChangedListener;
import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.HomeTheaterChannel;
import com.qualcomm.qti.iotcontrollersdk.constants.UpdateStatus;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.HomeTheaterChannelMap;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;

import java.util.ArrayList;
import java.util.List;

import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnDeviceListChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnDeviceUpdateListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnHomeTheaterChannelChangedListener;

public class SettingsFragment extends BaseFragment implements OnGroupInfoStateChangedListener, ExpandableListView.OnChildClickListener,
        OnGroupListChangedListener, OnDeviceUpdateListener, OnDeviceListChangedListener,
        OnHomeTheaterChannelChangedListener {

    private static final String TAG = "SettingsFragment";
    private SettingsAdapter mAdapter;
    private OnSettingsListener mSettingsListener;
    private static final String RESET_TAG = "ResetTag";


    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }


    @Override
    public void onHomeTheaterChannelUpdate(IoTPlayer player, HomeTheaterChannelMap channelMap) {

    }

    @Override
    public void onHomeTheaterChannelFirmwareUpdateAvailable(IoTPlayer player, HomeTheaterChannel channel) {
        Log.d(TAG, "[onHomeTheaterChannelFirmwareUpdateAvailable] channel " + channel);
        updateInUiThread();
    }

    @Override
    public void onHomeTheaterChannelFirmwareUpdateStatusChanged(IoTPlayer player, HomeTheaterChannel channel, UpdateStatus updateStatus) {

    }

    @Override
    public void onHomeTheaterChannelFirmwareUpdateProgressChanged(IoTPlayer player, HomeTheaterChannel channel, double progress) {

    }

    public enum SettingsItemType {
        UPDATE,
        SPEAKERS,
        GENERAL
    }

    ;

    public interface OnSettingsListener {
        void onSpeakerSelected(String id, String host);

        void onSettingsItemClicked(SettingsItemType itemType);

        void onZoneChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSettingsListener = (OnSettingsListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.settings_activity_expand_listview);

        mAdapter = new SettingsAdapter();
        expandableListView.setAdapter(mAdapter);

        expandableListView.setOnChildClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if ((mApp != null) && mApp.isInit()) {
            mAllPlayManager.addOnZoneListChangedListener(this);
            mAllPlayManager.addOnZoneStateChangedListener(this);
            mAllPlayManager.addOnDeviceUpdateListener(this);
            for (IoTDevice device : mAllPlayManager.getDevices()) {
                checkFirmwareUpdate(device);
            }
            updateState();
        }
    }

    private void checkFirmwareUpdate(final IoTDevice device) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IoTPlayer player = mAllPlayManager.getPlayer(device.getId());
                if ((player != null) && player.isSoundBar()) {
                    Log.d(TAG, "check for left and right surround updateState");
                    player.checkNewHomeTheaterChannelFirmwareUpdate(HomeTheaterChannel.LEFT_SURROUND);
                    player.checkNewHomeTheaterChannelFirmwareUpdate(HomeTheaterChannel.RIGHT_SURROUND);
                }
                device.checkForNewFirmware();
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAllPlayManager != null) {
            mAllPlayManager.removeOnZoneListChangedListener(this);
            mAllPlayManager.removeOnZoneStateChangedListener(this);
            mAllPlayManager.removeOnDeviceUpdateListener(this);
        }
    }

    private List<SettingsItem> createSettingsItems() {
        List<SettingsItem> settingsItems = new ArrayList<SettingsItem>();

        if (isFirmwareUpdateAvailable()) {
            List<String> firmwareChildItems = new ArrayList<String>();
            firmwareChildItems.add(getString(R.string.update_available));

            SettingsItem firmwareUpdateItem = new SettingsItem(SettingsItemType.UPDATE, getString(R.string.firmware_update), firmwareChildItems);
            settingsItems.add(firmwareUpdateItem);
        }

        final List<IoTPlayer> players = mAllPlayManager.getPlayers();

        if ((players != null) && (players.size() != 0)) {
            List<String> speakersChildItems = new ArrayList<String>();
            for (IoTPlayer player : players) {
                speakersChildItems.add(mAllPlayManager.getPlayerName(player));
            }

            SettingsItem speakersItem = new SettingsItem(SettingsItemType.SPEAKERS, getString(R.string.speakers), speakersChildItems);
            settingsItems.add(speakersItem);
        }

        List<String> generalChildItems = new ArrayList<String>();
        //generalChildItems.add(getString(R.string.general_warn));
        generalChildItems.add(getString(R.string.general_reset));

        SettingsItem generalItem = new SettingsItem(SettingsItemType.GENERAL, getString(R.string.general), generalChildItems);
        settingsItems.add(generalItem);

        return settingsItems;
    }

    private boolean isFirmwareUpdateAvailable() {
        List<IoTDevice> devices = mApp.getAllPlayManager().getDevices();
        for (IoTDevice device : devices) {
            IoTPlayer player = mAllPlayManager.getPlayer(device.getId());

            if (((device.getUpdateStatus() == UpdateStatus.NONE) && device.haveNewFirmware()) ||
                    ((device.getUpdateStatus() == UpdateStatus.NONE) && device.isPhysicalRebootRequired()) || ((player != null) && ((player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.LEFT_SURROUND)) || (player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.RIGHT_SURROUND))))) {
                Log.d(TAG, "fw updateState " + device.getName());
                return true;
            }
        }
        Log.d(TAG, "no fw updateState");
        return false;
    }

    public void updateState() {
        mSettingsListener.onZoneChanged();
        final List<SettingsItem> settingsItems = createSettingsItems();
        mAdapter.updateSettings(settingsItems);
    }

    @Override
    public void onGroupInfoStateChanged() {
        updateInUiThread();
    }

    @Override
    public void onZoneListChanged() {
        updateInUiThread();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        switch (((SettingsItem) parent.getExpandableListAdapter().getGroup(groupPosition)).getType()) {
            case UPDATE:
                mSettingsListener.onSettingsItemClicked(SettingsItemType.UPDATE);
                break;
            case SPEAKERS:
                IoTPlayer player = mAllPlayManager.getPlayers().get(childPosition);
                String playerId = player.getPlayerId();
                String host = player.getHostName();
                mSettingsListener.onSpeakerSelected(playerId, host);
                break;
            case GENERAL:
                CustomDialogFragment restartDialogFragment = CustomDialogFragment.newDialog(RESET_TAG, getString(R.string.settings_reset_title), getString(R.string.settings_reset_message), getString(R.string.settings_reset), getString(R.string.cancel));
                restartDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                    @Override
                    public void onPositiveButtonClicked(String tag) {
                        mSettingsListener.onSettingsItemClicked(SettingsItemType.GENERAL);
                    }

                    @Override
                    public void onNegativeButtonClicked(String tag) {
                    }
                });
                mBaseActivity.showDialog(restartDialogFragment, RESET_TAG);

                break;
        }
        return true;
    }

    @Override
    public void onDeviceAutoUpdateChanged(IoTDevice device, boolean autoUpdate) {

    }

    @Override
    public void onDeviceUpdateAvailable(IoTDevice device) {
        Log.d(TAG, "onDeviceUpdateAvailable");
        updateInUiThread();
    }

    @Override
    public void onDeviceUpdateStatusChanged(IoTDevice device, UpdateStatus updateStatus) {
        Log.d(TAG, "onDeviceUpdateStatusChanged");
        updateInUiThread();
    }

    @Override
    public void onDeviceUpdateProgressChanged(IoTDevice device, double progress) {

    }

    @Override
    public void onDeviceUpdatePhysicalRebootRequired(IoTDevice device) {

    }

    @Override
    public void onDeviceListChanged() {
        Log.d(TAG, "[onDeviceListChanged]");
        updateInUiThread();
    }

    private class SettingsItem {
        private SettingsItemType mType;
        private String mTitle;
        private List<String> mChildItems;

        public SettingsItem(SettingsItemType type, String title, List<String> childItems) {
            mType = type;
            mTitle = title;
            mChildItems = childItems;
        }

        public SettingsItemType getType() {
            return mType;
        }

        public String getTitle() {
            return mTitle;
        }

        public List<String> getChildItems() {
            return mChildItems;
        }
    }

    private class SettingsAdapter extends BaseExpandableListAdapter {

        List<SettingsItem> mSettingsItems = new ArrayList<SettingsItem>();

        public void updateSettings(List<SettingsItem> settingsItems) {
            mSettingsItems = settingsItems;
            notifyDataSetChanged();
        }

        @Override
        public int getGroupCount() {
            return mSettingsItems.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mSettingsItems.get(groupPosition).getChildItems().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mSettingsItems.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mSettingsItems.get(groupPosition).getChildItems().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item_settings_headers, parent, false);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.settings_header_text_view);
            textView.setText(((SettingsItem) getGroup(groupPosition)).getTitle());

            ExpandableListView listView = (ExpandableListView) parent;
            listView.expandGroup(groupPosition);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item_settings_child, parent, false);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.settings_child_text_view);
            textView.setText((String) getChild(groupPosition, childPosition));

            ImageView imageView = (ImageView) convertView.findViewById(R.id.settings_child_image_view);
            imageView.setVisibility(View.GONE);

            Switch switchView = (Switch) convertView.findViewById(R.id.settings_child_switch_view);
            switchView.setVisibility(View.GONE);

            switch (((SettingsItem) getGroup(groupPosition)).getType()) {
                case UPDATE:
                    imageView.setVisibility(View.VISIBLE);
                    break;
                case GENERAL:
					/*if (childPosition == 0) {
						switchView.setVisibility(View.VISIBLE);
						switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								// TODO : implement toggle change
							}
						});
					}
					break;*/
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
