/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.qualcomm.qti.smartaudio.fragment.VoiceControlFragment.SettingsItemType.AVS;
import static com.qualcomm.qti.smartaudio.fragment.VoiceControlFragment.SettingsItemType.CORTANA;
import static com.qualcomm.qti.smartaudio.fragment.VoiceControlFragment.SettingsItemType.DEFAULT_VOICE_SERVICE;
import static com.qualcomm.qti.smartaudio.fragment.VoiceControlFragment.SettingsItemType.GOOGLE;
import static com.qualcomm.qti.smartaudio.fragment.VoiceControlFragment.SettingsItemType.MODULAR;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
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
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.AVSOnboardingErrorAttr;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.AvsOnboardingInfo;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.IoTSysInfo;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.VoiceUIAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.VoiceUIClientAttr;

import java.util.ArrayList;
import java.util.List;

public class VoiceControlFragment extends BaseFragment implements ExpandableListView.OnChildClickListener,
        onVoiceUiListener {

    private static final String TAG = "VoiceControl";
    private static final String EXTRA_ID = "DEVICE_ID";
    private static final String EXTRA_HOST = "HOST_NAME";
    private IoTSysManager mManager;
    private DeviceSettingsAdapter mAdapter;
    private List<SettingsItem> mSettingsItems;
    private String mID;
    private String mHost;

    public static VoiceControlFragment newInstance(String id, String host) {
        VoiceControlFragment fragment = new VoiceControlFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ID, id);
        bundle.putString(EXTRA_HOST, host);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        TextView titleView = view.findViewById(R.id.settings_app_bar_text_view);
        titleView.setText(R.string.voice_control_setting);
        titleView.setContentDescription(getString(R.string.cont_desc_screen_voice_control));

        ExpandableListView expandableListView = view.findViewById(R.id.settings_activity_expand_listview);

        Bundle args = getArguments();
        mID = args != null ? args.getString(EXTRA_ID) : "";
        mHost = args != null ? args.getString(EXTRA_HOST) : "";

        mSettingsItems = new ArrayList<>();
        //SettingsItem item = new SettingsItem(MICROPHONE,getString(R.string.voice_control_setting_microphone),"");
        //mSettingsItems.add(item);
        SettingsItem item = new SettingsItem(MODULAR, getString(R.string.voice_control_qcs40x_voice), "Not linked",
                                             getString(R.string.cont_desc_voice_control_qcs40x_voice));
        mSettingsItems.add(item);
        item = new SettingsItem(AVS, getString(R.string.voice_control_avs), "Not linked",
                                getString(R.string.cont_desc_voice_control_avs));
        mSettingsItems.add(item);
        item = new SettingsItem(GOOGLE, getString(R.string.voice_control_google_assistant), "Not linked",
                                getString(R.string.cont_desc_voice_control_google_assistant));
        mSettingsItems.add(item);
        item = new SettingsItem(CORTANA, getString(R.string.voice_control_microsoft), "Not linked",
                                getString(R.string.cont_desc_voice_control_cortana));
        mSettingsItems.add(item);

        String defaultUiClient = IoTService.getInstance().getDefaultVoiceUIClient(mHost);
        item = new SettingsItem(DEFAULT_VOICE_SERVICE, getString(R.string.voice_control_setting_default),
                                defaultUiClient, getString(R.string.cont_desc_voice_control_default));

        mSettingsItems.add(item);

        mAdapter = new DeviceSettingsAdapter();
        expandableListView.setAdapter(mAdapter);

        expandableListView.setOnChildClickListener(this);


        VoiceUIAttr attribute = IoTService.getInstance().getVoiceSetting(mHost);
        if (attribute != null) {
            for (VoiceUIClientAttr attr : attribute.mVoiceUIClients) {
                Log.d(TAG, String.format("voice client name:%s, version:%s", attr.mName, attr.mVersion));
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mManager = IoTSysManager.getInstance();
            mManager.addVoiceUiListener(this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        List<SettingsItem> items = (List<SettingsItem>) mAdapter.getGroup(0);
        SettingsItemType itemType = items.get(childPosition).getItemType();
        boolean consumed = false;

        switch (itemType) {
            case AVS:
                IoTSysInfo info = IoTService.getInstance().getIoTSysInfo(mHost);
                if (info != null && !info.isAvsOnBoarded) {
                    Log.d(TAG, "Start AVS onboarding for host:" + mHost);
                    IoTService.getInstance().startAvsOnBoarding(mHost, success -> {
                    });
                }
                else if (info != null && info.isAvsOnBoarded) {
                    AvsSettingsFragment fragment = AvsSettingsFragment.newInstance(mID, mHost);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.setting_container,
                                                                                         fragment).addToBackStack(null)
                            .commit();
                }
                consumed = true;
                break;
            //TODO
            case GOOGLE:
            case CORTANA:
            case MODULAR:
            case DEFAULT_VOICE_SERVICE:
                break;
        }
        return consumed;
    }

    @Override
    public void voiceUIEnabledStateDidChange(boolean enabled) {
        UiThreadExecutor.getInstance().execute(() -> mAdapter.notifyDataSetChanged());
    }

    @Override
    public void voiceUIDidProvideAVSAuthenticationCode(String code, String url) {
        UiThreadExecutor.getInstance().execute(() -> {
            AvsOnboardingInfo onboardingInfo = new AvsOnboardingInfo(url, code);
            AvsOnBoardingSetupFragment fragment = AvsOnBoardingSetupFragment.newInstance(onboardingInfo);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.setting_container, fragment)
                    .commit();
        });
    }

    @Override
    public void voiceUIOnboardingDidErrorWithTimeout(AVSOnboardingErrorAttr.Error error, int reattempt) {
        UiThreadExecutor.getInstance().execute(() ->
                                                       Log.e(TAG, "AVS onboarding error:" + error)
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        mManager.removeVoiceUiListener(this);
    }

    @Override
    public void updateState() {

    }

    public enum SettingsItemType {
        MICROPHONE,
        MODULAR,
        AVS,
        GOOGLE,
        CORTANA,
        DEFAULT_VOICE_SERVICE
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
            mContentDescription = description;
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
                TextView textView = convertView.findViewById(R.id.device_settings_child_text_view);
                textView.setText(itemDetails.getTitle());

                convertView.setContentDescription(getString(R.string.cont_desc_menu_item,
                                                            itemDetails.getContentDescription()));

                TextView subTextView = convertView.findViewById(R.id.device_settings_child_sub_text_view);

                Switch switchView = convertView.findViewById(R.id.device_settings_child_switch_view);

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

                    case AVS:

                        if (info.isAvsOnBoarded) {
                            subTextView.setText(getString(R.string.voice_ui_available_state));
                        }
                        else if (info.isVoicdUIEnabled) {
                            subTextView.setText(getString(R.string.voice_ui_enabled));
                        }
                        else {
                            subTextView.setText(getString(R.string.voice_ui_not_linked));
                        }
                        switchView.setVisibility(View.GONE);
                        break;
                    case MODULAR:
                        if (info.isCortanaOnboarded) {
                            subTextView.setText(getString(R.string.voice_ui_available_state));
                        }
                        else if (info.isVoicdUIEnabled) {
                            subTextView.setText(getString(R.string.voice_ui_enabled));
                        }
                        else {
                            subTextView.setText(getString(R.string.voice_ui_not_linked));
                        }
                        switchView.setVisibility(View.GONE);
                        break;
                    case GOOGLE:
                        if (info.isGoogleOnboarded) {
                            subTextView.setText(getString(R.string.voice_ui_available_state));
                        }
                        else if (info.isVoicdUIEnabled) {
                            subTextView.setText(getString(R.string.voice_ui_enabled));
                        }
                        else {
                            subTextView.setText(getString(R.string.voice_ui_not_linked));
                        }
                        switchView.setVisibility(View.GONE);
                        break;
                    case CORTANA:
                        if (info.isCortanaOnboarded) {
                            subTextView.setText(getString(R.string.voice_ui_available_state));
                        }
                        else if (info.isVoicdUIEnabled) {
                            subTextView.setText(getString(R.string.voice_ui_enabled));
                        }
                        else {
                            subTextView.setText(getString(R.string.voice_ui_not_linked));
                        }
                        switchView.setVisibility(View.GONE);
                        break;
                    case MICROPHONE:
                        subTextView.setVisibility(View.GONE);
                        switchView.setVisibility(View.VISIBLE);
                        switchView.setChecked(info.isVoicdUIEnabled);
                        boolean enabled = info.isVoicdUIEnabled;
                        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (!IoTService.getInstance().enableVoiceUI(mID, isChecked, success -> {
                                Log.d(TAG, String.format("%s Voice UI:%b!", isChecked ? "Enabled" : "Disabled",
                                                         success));
                                if (!success) {
                                    switchView.setChecked(enabled);
                                }
                            })) {
                                switchView.setChecked(enabled);
                            }
                        });
                        break;
                    case DEFAULT_VOICE_SERVICE:
                        subTextView.setText(itemDetails.getDescription());
                        switchView.setVisibility(View.GONE);
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
}
