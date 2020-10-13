/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager.onZigbeeListener;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr.CoordinatorState;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr.JoiningState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTZigbeeDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.utils.ControllerSdkUtils;

import java.util.ArrayList;
import java.util.List;

public class ZigbeeConfigurationFragment extends BaseFragment implements ExpandableListView.OnChildClickListener,
        onZigbeeListener {

    private static final String TAG = "ZGConfiguration";
    private static final String DIALOG_ZIGBEE_RENAME_TAG = "DialogRenameZigbeee";

    private String mID;
    private String mHost;

    private ZigbeeSettingsAdapter mAdapter;
    private List<SettingsItem> mSettingsItem;

    public static ZigbeeConfigurationFragment newInstance(String id, String host) {
        ZigbeeConfigurationFragment fragment = new ZigbeeConfigurationFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ID, id);
        bundle.putString(EXTRA_HOST, host);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Bundle arg = getArguments();
        mID = arg.getString(EXTRA_ID);
        mHost = arg.getString(EXTRA_HOST);
        mHost = ControllerSdkUtils.stripHostName(mHost);

        TextView titleView = view.findViewById(R.id.settings_app_bar_text_view);
        titleView.setText(getText(R.string.zigbee_setting_title));
        titleView.setContentDescription(getString(R.string.cont_desc_screen_zigbee));

        ExpandableListView expandableListView = view.findViewById(R.id.settings_activity_expand_listview);

        expandableListView.setOnChildClickListener(this);

        if (mSettingsItem == null) {
            mSettingsItem = new ArrayList<>();
        }
        else {
            mSettingsItem.clear();
        }

        SettingsItem item = new SettingsItem(ZigbeeItemType.ENABLED, getString(R.string.zigbee_enable),
                                             getString(R.string.cont_desc_settings_item_zigbee_service));
        mSettingsItem.add(item);

        updateJoinedDevice();

        mAdapter = new ZigbeeSettingsAdapter();
        expandableListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIoTSysManager != null) {
            mIoTSysManager.addZigbeeiListener(this);
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        if (mIoTSysManager != null) {
            mIoTSysManager.removeZigbeeListener(this);
        }
        super.onPause();
    }

    private void updateJoinedDevice() {
        IoTDevice selectedDevice = mAllPlayManager.getDeviceByHostName(mHost);
        if (selectedDevice != null && mSettingsItem != null && !mSettingsItem.isEmpty()) {

            mSettingsItem.removeIf(item -> (ZigbeeItemType.START_JOINING == item.mItemType
                    || ZigbeeItemType.JOINED_DEVICE_TITLE == item.mItemType
                    || ZigbeeItemType.COORDINATOR == item.mItemType
                    || ZigbeeItemType.JOINED_DEVICE == item.mItemType));

            if (selectedDevice.isZigbeeEnabled()) {

                SettingsItem item = new SettingsItem(ZigbeeItemType.COORDINATOR,
                                                     getString(R.string.zigbee_coordinator_setting),
                                                     getString(R.string.cont_desc_settings_item_zigbee_coordinator));
                mSettingsItem.add(item);

                if (selectedDevice.getZGJoinedDevices().size() > 0) {
                    item = new SettingsItem(ZigbeeItemType.JOINED_DEVICE_TITLE,
                                            getString(R.string.zigbee_paired_devices_setting),
                                            getString(R.string.cont_desc_settings_item_zigbee_devices_list));
                    mSettingsItem.add(item);
                }

                List<IoTZigbeeDevice> zDevices = selectedDevice.getZGJoinedDevices();
                if (zDevices != null && !zDevices.isEmpty()) {
                    for (int i = 0; i < zDevices.size(); i++) {
                        String name = zDevices.get(i).getName();
                        if (name == null || name.isEmpty()) {
                            name = getString(R.string.zigbee_unknown_device);
                        }
                        item = new SettingsItem(ZigbeeItemType.JOINED_DEVICE, name,
                                                zDevices.get(i).getDeviceIdentifier(),
                                                getString(R.string.cont_desc_zigbee_device, name));
                        mSettingsItem.add(item);
                    }
                }

                item = new SettingsItem(ZigbeeItemType.START_JOINING,
                                        getString(R.string.zigbee_add_zigbee_devices),
                                        getString(R.string.cont_desc_settings_item_zigbee_add_device));
                mSettingsItem.add(item);
            }
        }
    }

    @Override
    public void updateState() {
        UiThreadExecutor.getInstance().execute(() -> {
            updateJoinedDevice();
            mAdapter.notifyDataSetChanged();
        });
    }

    private class SettingsItem {

        private ZigbeeItemType mItemType;
        private String mTitle;
        private int mIndex;
        private String mContentDescription;

        SettingsItem(ZigbeeItemType type, String title, String contentDescription) {
            mItemType = type;
            mTitle = title;
            mIndex = -1;
            mContentDescription = contentDescription;
        }

        SettingsItem(ZigbeeItemType type, String title, int index, String contentDescription) {
            mItemType = type;
            mTitle = title;
            mIndex = index;
            mContentDescription = contentDescription;
        }

        ZigbeeItemType getItemType() {
            return mItemType;
        }

        String getTitle() {
            return mTitle;
        }

        int getIndex() {
            return mIndex;
        }

        String getContentDescription() {
            return mContentDescription;
        }

    }

    private enum ZigbeeItemType {
        ENABLED,
        COORDINATOR,
        JOINED_DEVICE_TITLE,
        JOINED_DEVICE,
        START_JOINING
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

        List<SettingsItem> items = (List<SettingsItem>) parent.getExpandableListAdapter().getGroup(groupPosition);
        SettingsItem childItem = items.get(childPosition);
        ZigbeeItemType itemType = childItem.getItemType();
        switch (itemType) {
            case START_JOINING:
                //Check if Coordinator existed!
                checkJoining();
                return true;
            case JOINED_DEVICE:
                break;
            default:
                break;
        }
        return false;
    }

    private class ZigbeeSettingsAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return 1;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mSettingsItem.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mSettingsItem;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mSettingsItem.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
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
                LayoutInflater layoutInflater =
                        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item_settings_headers, parent, false);
            }
            ExpandableListView listView = (ExpandableListView) parent;
            listView.expandGroup(groupPosition);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                                 ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item_device_settings_child, parent, false);
            }

            IoTDevice selectedDevice = mAllPlayManager.getDeviceByHostName(mHost);

            SettingsItem itemDetails = ((SettingsItem) getChild(groupPosition, childPosition));
            if (itemDetails != null) {
                String contentDescription = itemDetails.getContentDescription();
                convertView.setContentDescription(getString(R.string.cont_desc_setting_item, contentDescription));

                TextView textView = convertView.findViewById(R.id.device_settings_child_text_view);
                textView.setText(itemDetails.getTitle());
                textView.setContentDescription(getString(R.string.cont_desc_item_title, contentDescription));

                TextView subTextView = convertView.findViewById(R.id.device_settings_child_sub_text_view);
                subTextView.setVisibility(View.GONE);
                subTextView.setContentDescription(getString(R.string.cont_desc_item_description, contentDescription));

                TextView statusCheck = convertView.findViewById(R.id.device_status_check);
                statusCheck.setContentDescription(getString(R.string.cont_desc_zigbee_check));

                Switch switchView = convertView.findViewById(R.id.device_settings_child_switch_view);
                switchView.setContentDescription(getString(R.string.cont_desc_item_control, contentDescription));

                RelativeLayout textViewLayout = convertView.findViewById(R.id.device_settings_child_text_view_layout);
                ViewGroup.LayoutParams layoutParams = textViewLayout.getLayoutParams();
                layoutParams.height =
                        (int) getResources().getDimension(R.dimen.settings_activity_list_item_settings_child_height);

                LinearLayout itemContainer = convertView.findViewById(R.id.item_container);

                textViewLayout.setLayoutParams(layoutParams);

                switch ((itemDetails.getItemType())) {
                    case ENABLED:
                        switchView.setVisibility(View.VISIBLE);
                        switchView.setEnabled(selectedDevice != null && selectedDevice.isZigbeeAvailable());
                        final boolean checked = selectedDevice != null && selectedDevice.isZigbeeEnabled();
                        switchView.setChecked(checked);
                        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (selectedDevice != null && !selectedDevice.setZbAdapterEnabled(isChecked, success -> {
                                boolean result = !success ? checked : isChecked;
                                UiThreadExecutor.getInstance()
                                        .execute(() -> switchView.setChecked(result));
                            })) {
                                buttonView.setChecked(checked);
                            }
                        });
                        break;
                    case COORDINATOR:
                        if (selectedDevice != null && IoTService.getInstance().isZbCoordinator(selectedDevice.getId())) {
                            statusCheck.setVisibility(View.VISIBLE);
                            statusCheck.setText("✓");
                        }
                        else {
                            statusCheck.setVisibility(View.GONE);
                        }
                        break;
                    case JOINED_DEVICE:
                        switchView.setVisibility(View.GONE);
                        if (selectedDevice != null
                                && selectedDevice.getCoordinatorState() == CoordinatorState.kNominated) {

                            itemContainer.setTag(itemDetails);
                            itemContainer.setOnClickListener(v -> {
                                                                 SettingsItem item = (SettingsItem) v.getTag();
                                                                 renameDevice(item);
                                                             }
                            );
                        }
                        break;
                    case JOINED_DEVICE_TITLE:
                        switchView.setVisibility(View.GONE);
                        break;
                    case START_JOINING:
                        switchView.setVisibility(View.GONE);
                        if (selectedDevice != null && selectedDevice.getJoinZbJoiningState() == JoiningState.kAllowed) {
                            textView.setTextColor(convertView.getResources()
                                                          .getColor(R.color.settings_activity_list_item_settings_child_text));
                        }
                        else {
                            textView.setTextColor(convertView.getResources()
                                                          .getColor(R.color.device_settings_activity_list_item_settings_child_sub_text));
                        }
                        break;
                }
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    public void onZbAdapterStateChanged(IoTDevice device) {
        Log.d(TAG, "onZbAdapterStateChanged notify device ID:" + device.getId() + ", current id " + mID);
        if (device.getId() != null && device.getId().equalsIgnoreCase(mID)) {
            UiThreadExecutor.getInstance().execute(() -> {
                                                       updateJoinedDevice();
                                                       mAdapter.notifyDataSetChanged();
                                                   }
            );
        }
    }

    @Override
    public void onZbCoordinatorStateDidChanged(IoTDevice device) {
        Log.d(TAG, "onZbCoordinatorStateDidChanged notify device ID:" + device.getId() + ", current id " + mID + "," +
                "host:" + mHost);
        if (device.getId() != null && device.getId().equalsIgnoreCase(mID)) {
            UiThreadExecutor.getInstance().execute(() -> mAdapter.notifyDataSetChanged()
            );
        }
    }

    @Override
    public void onZbJoinedDevicesDidChanged(IoTDevice device) {
        Log.d(TAG, "onZbJoinedDevicesDidChanged notify device ID:" + device.getId() + ", current id " + mID + ",host" +
                ":" + mHost);
        if (device.getId() != null && device.getId().equalsIgnoreCase(mID)) {
            UiThreadExecutor.getInstance().execute(() -> {
                                                       updateJoinedDevice();
                                                       mAdapter.notifyDataSetChanged();
                                                   }
            );
        }
    }

    @Override
    public void OnZbJoiningStateDidChanged(IoTDevice device, boolean allowed) {
        Log.d(TAG,
              "OnZbJoiningStateDidChanged notify device ID:" + device.getId() + ", current id " + mID + ",host:" + mHost);
        if (device.getId() != null && device.getId().equalsIgnoreCase(mID)) {
            UiThreadExecutor.getInstance().execute(() -> mAdapter.notifyDataSetChanged()
            );
        }
    }

    private void checkJoining() {
        final IoTDevice device = mAllPlayManager.getDeviceByHostName(mHost);

        if (device != null && device.getCoordinatorState() == CoordinatorState.kNominated) {
            Log.d(TAG, "This is coordinator!");
            //return;
        }

        if (device != null) {
            if (mIoTSysManager.isZigbeeCoordinatorExisted(mID)) {
                android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(
                        getContext())
                        .setTitle(getString(R.string.zigbee_check_joining_dialog_title))
                        .setMessage(getString(R.string.zigbee_check_joining_coordinator_message))
                        .setPositiveButton("OK", (dialog, id) -> dialog.cancel());
                alertDialog.setCancelable(false);
                alertDialog.create().show();
            }
            else {
                android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(
                        getContext())
                        .setTitle(getString(R.string.zigbee_check_joining_dialog_title))
                        .setMessage(getString(R.string.zigbee_check_joining_not_coordinator_message))
                        .setPositiveButton("OK", (dialog, id) -> {
                            showFragment(ZigbeeOnboardingFragment.newInstance(mID, mHost));
                            dialog.cancel();
                        })
                        .setNegativeButton("CANCEL", (dialog, id) -> dialog.dismiss());
                alertDialog.setCancelable(false);
                alertDialog.create().show();
            }

        }
    }


    private void renameDevice(final SettingsItem item) {

        if (item != null) {

            final EditTextDialogFragment customNameDialogFragment = EditTextDialogFragment
                    .newEditTextDialog(DIALOG_ZIGBEE_RENAME_TAG, getString(R.string.custom_name_title), item.mTitle,
                                       "", getString(R.string.button_set), getString(R.string.button_cancel));

            customNameDialogFragment
                    .setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {

                        @Override
                        public void onPositiveButtonClicked(String tag) {
                            String name = customNameDialogFragment.getEditText().getText().toString();
                            try {
                                IoTSysManager.getInstance().setZigbeeName(mHost, name, item.getIndex(),
                                                                          success -> Log.d(TAG, "Set Zigbee device " +
                                                                                  "name :" + success));
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNegativeButtonClicked(String tag) {
                        }
                    });

            mBaseActivity.showDialog(customNameDialogFragment, DIALOG_ZIGBEE_RENAME_TAG);
        }
    }

}
