/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager.onVoiceUiListener;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.IoTSysInfo;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.state.VoiceUiState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTVoiceUIClient;

import java.util.ArrayList;
import java.util.List;

public class AvsSettingsFragment extends BaseFragment implements ExpandableListView.OnChildClickListener,
        onVoiceUiListener {

    private static final String TAG = "AvsSettingsFragment";
    private static final String EXTRA_ID = "DEVICE_ID";
    private static final String EXTRA_HOST = "HOST_NAME";
    private final static String DIALOG_CONFIRMATION_TAG = "SIGN_OUT_CONFIRMATION_DIALOG";

    private String mID;
    private String mHost;
    private List<SettingsItem> mSettingsItems;
    private DeviceSettingsAdapter mAdapter;

    public enum SettingsItemType {
        ON_BOARDED,
        HOTWORD,
        COUNTRY,
        SIGN_OUT
    }

    public static AvsSettingsFragment newInstance(String id, String host) {
        AvsSettingsFragment fragment = new AvsSettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ID, id);
        bundle.putString(EXTRA_HOST, host);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        List<SettingsItem> items = (List<SettingsItem>) mAdapter.getGroup(0);
        SettingsItemType itemType = items.get(childPosition).getItemType();
        boolean consumed = false;

        switch (itemType) {
            case SIGN_OUT:
                consumed = true;
                getActivity().getSupportFragmentManager().popBackStackImmediate(0,
                                                                                FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
        }
        return consumed;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        TextView titleView = view.findViewById(R.id.settings_app_bar_text_view);
        titleView.setText(R.string.voice_ui_avs_settings_title);
        titleView.setContentDescription(getString(R.string.cont_desc_screen_avs_settings));

        ExpandableListView expandableListView = view.findViewById(R.id.settings_activity_expand_listview);

        Bundle arg = getArguments();
        mID = arg.getString(EXTRA_ID);
        mHost = arg.getString(EXTRA_HOST);

        mSettingsItems = new ArrayList<>();
        SettingsItem item = new SettingsItem(SettingsItemType.ON_BOARDED,
                                             getString(R.string.avs_setting_enabled), "",
                                             getString(R.string.cont_desc_avs_setting_service_on_off));
        mSettingsItems.add(item);
        item = new SettingsItem(SettingsItemType.HOTWORD, getString(R.string.avs_voice_setting_hot_word), "",
                                getString(R.string.cont_desc_avs_setting_hot_word));
        mSettingsItems.add(item);
        item = new SettingsItem(SettingsItemType.COUNTRY, getString(R.string.avs_voice_setting_country), "",
                                getString(R.string.cont_desc_avs_setting_country));
        mSettingsItems.add(item);
        item = new SettingsItem(SettingsItemType.SIGN_OUT, getString(R.string.avs_voice_setting_sign_out), "",
                                getString(R.string.cont_desc_avs_setting_sign_out));
        mSettingsItems.add(item);

        mAdapter = new DeviceSettingsAdapter();
        expandableListView.setAdapter(mAdapter);
        expandableListView.setOnChildClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mIoTSysManager = IoTSysManager.getInstance();
            mIoTSysManager.addVoiceUiListener(this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mIoTSysManager.removeVoiceUiListener(this);
    }

    private class SettingsItem {

        private SettingsItemType mItemType;
        private String mTitle;
        private String mDescription;
        private String mContentDescription;

        SettingsItem(SettingsItemType type, String title, String description, String contentDescription) {
            mItemType = type;
            mTitle = title;
            mDescription = description;
            mContentDescription = contentDescription;
        }

        SettingsItemType getItemType() {
            return mItemType;
        }

        String getTitle() {
            return mTitle;
        }

        String getDescription() {
            return mDescription;
        }

        String getContentDescription() {
            return mContentDescription;
        }
    }

    private class DeviceSettingsAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return 1;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mSettingsItems.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mSettingsItems;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mSettingsItems.get(childPosition);
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
                convertView = layoutInflater.inflate(R.layout.voice_control_setting_item, parent, false);
                convertView.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
            }

            SettingsItem itemDetails = ((SettingsItem) getChild(groupPosition, childPosition));

            if (itemDetails != null) {
                String contentDescription = itemDetails.getContentDescription();
                convertView.setContentDescription(getString(R.string.cont_desc_setting_item, contentDescription));

                TextView textView = convertView.findViewById(R.id.device_settings_child_text_view);
                textView.setText(itemDetails.getTitle());
                textView.setContentDescription(getString(R.string.cont_desc_item_title, contentDescription));

                TextView subTextView = convertView.findViewById(R.id.device_settings_child_sub_text_view);
                subTextView.setContentDescription(getString(R.string.cont_desc_item_description, contentDescription));

                Switch switchView = convertView.findViewById(R.id.device_settings_child_switch_view);
                switchView.setContentDescription(getString(R.string.cont_desc_item_control, contentDescription));

                RelativeLayout textViewLayout = convertView.findViewById(R.id.device_settings_child_text_view_layout);
                ViewGroup.LayoutParams layoutParams = textViewLayout.getLayoutParams();
                layoutParams.height =
                        (int) getResources().getDimension(R.dimen.settings_activity_list_item_settings_child_height);

                if (!Utils.isStringEmpty(itemDetails.getDescription())) {
                    layoutParams.height =
                            (int) getResources().getDimension(R.dimen.device_settings_activity_list_item_settings_sub_child_height);
                    subTextView.setVisibility(View.VISIBLE);
                    subTextView.setText(itemDetails.getDescription());
                }

                textViewLayout.setLayoutParams(layoutParams);
                IoTSysInfo info = IoTService.getInstance().getIoTSysInfo(mHost);

                switch ((itemDetails.getItemType())) {
                    case ON_BOARDED:
                        switchView.setVisibility(View.VISIBLE);
                        switchView.setChecked(info.isAvsOnBoarded);
                        subTextView.setVisibility(View.INVISIBLE);

                    case HOTWORD:
                        switchView.setVisibility(View.VISIBLE);
                        switchView.setChecked(info.isAVSWakeWord);
                        boolean isWakeWordEnabled = info.isAVSWakeWord;
                        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (!IoTService.getInstance().enableWakeWord(mID, VoiceUiState.AVSClientName, isChecked,
                                                                         success -> {
                                                                             if (!success) {
                                                                                 switchView.setEnabled(isWakeWordEnabled);
                                                                             }
                                                                         })) {
                                switchView.setEnabled(isWakeWordEnabled);
                            }
                        });

                        break;

                    case COUNTRY:
                        subTextView.setVisibility(View.VISIBLE);
                        subTextView.setText(info.mAVSLanguage);
                        switchView.setVisibility(View.GONE);
                        break;

                    case SIGN_OUT:
                        subTextView.setVisibility(View.INVISIBLE);
                        switchView.setVisibility(View.GONE);
                        textViewLayout.setOnClickListener(v -> signOutConfirmation());
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
    public void voiceUIClientsDidChange(List<IoTVoiceUIClient> voiceUIClients) {
        UiThreadExecutor.getInstance().execute(() -> mAdapter.notifyDataSetChanged());
    }

    private void signOutConfirmation() {
        final String title = getString(R.string.avs_sign_out_title);
        final String message = getString(R.string.avs_sign_out_confirmation);
        final String positiveText = getString(R.string.delete_button_text);
        final String negativeText = getString(R.string.cancel_button_text);
        CustomDialogFragment customDialogFragment = CustomDialogFragment.newDialog(DIALOG_CONFIRMATION_TAG,
                                                                                   title, message, positiveText,
                                                                                   negativeText);
        customDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked(String tag) {
                IoTService.getInstance().removeAVSCredential(mID, success -> {
                    if (success) {
                        showAlertDialog();
                    }
                });
            }

            @Override
            public void onNegativeButtonClicked(String tag) {

            }
        });
        mBaseActivity.showDialog(customDialogFragment, DIALOG_CONFIRMATION_TAG);

    }


    private void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.avs_sign_out_alert_title);
        builder.setMessage(getString(R.string.avs_sign_out_alert_message));
        builder.setPositiveButton(getResources().getString(R.string.ok),
                                  (paramDialogInterface, paramInt) -> paramDialogInterface.dismiss());
        builder.setCancelable(true);
        final AlertDialog dialog = builder.create();
        dialog.show();

    }
}
