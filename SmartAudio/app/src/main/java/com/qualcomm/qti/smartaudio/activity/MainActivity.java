/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.fragment.ZoneBottomMenuFragment;
import com.qualcomm.qti.smartaudio.fragment.SortingBottomMenuFragment;
import com.qualcomm.qti.smartaudio.fragment.DeviceSettingsFragment;
import com.qualcomm.qti.smartaudio.fragment.GroupInfoSpeakerFragment;
import com.qualcomm.qti.smartaudio.fragment.MusicFragment;
import com.qualcomm.qti.smartaudio.fragment.NetworkTreeFragment;
import com.qualcomm.qti.smartaudio.fragment.SpeakersListFragment;
import com.qualcomm.qti.smartaudio.interfaces.BottomDialogResultListener;
import com.qualcomm.qti.smartaudio.interfaces.IFragmentObserver;
import com.qualcomm.qti.smartaudio.interfaces.IMainViewController;
import com.qualcomm.qti.smartaudio.interfaces.OnGroupSelectedListener;
import com.qualcomm.qti.smartaudio.provider.upnp.UpnpProvider;
import com.qualcomm.qti.smartaudio.service.UpnpService;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;

import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.smartaudio.fragment.MusicFragment.OnMusicFragmentFinishedListener;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTSurround;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTRepository;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Set;

import static com.qualcomm.qti.smartaudio.fragment.SortingBottomMenuFragment.SortingBottomMenuSelection;
import static com.qualcomm.qti.smartaudio.fragment.ZoneBottomMenuFragment.ZoneBottomMenuSelection;

public class MainActivity extends BaseActivity implements OnGroupSelectedListener,
        GroupInfoSpeakerFragment.OnGroupSpeakerFragmentListener,
        OnMusicFragmentFinishedListener, IFragmentObserver, IMainViewController,
        BottomDialogResultListener {

    private static final String TAG = "MainActivity";

    @StringDef(value = {
            ExtraKeys.ROUTE_EXTRA, ExtraKeys.ID_EXTRA, ExtraKeys.HOST_EXTRA,
            ExtraKeys.PLAYER_ID_EXTRA, ExtraKeys.HOSTNAME_EXTRA, ExtraKeys.SHEET_ID_EXTRA,
            ExtraKeys.TYPE_EXTRA, ExtraKeys.CURRENT_VIEW_EXTRA
    })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ExtraKeys {

        String ROUTE_EXTRA = "ROUTE_EXTRA";
        String ID_EXTRA = "ID_EXTRA";
        String HOST_EXTRA = "HOST_EXTRA";
        String PLAYER_ID_EXTRA = "PLAYER_ID_EXTRA";
        String HOSTNAME_EXTRA = "HOSTNAME_EXTRA";
        String SHEET_ID_EXTRA = "SHEET_ID_EXTRA";
        String TYPE_EXTRA = "TYPE_EXTRA";
        String CURRENT_VIEW_EXTRA = "CURRENT_VIEW_EXTRA";
    }

    @IntDef(value = {BottomMenuId.BOTTOM_MENU_DEVICE, BottomMenuId.BOTTOM_MENU_HOME})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BottomMenuId {

        int BOTTOM_MENU_HOME = 1001;
        int BOTTOM_MENU_DEVICE = 1002;
    }

    @IntDef(value = {ViewType.VIEW_BY_TYPE, ViewType.VIEW_NETWORK_MAP})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ViewType {

        int VIEW_NONE = -1;
        int VIEW_BY_TYPE = 0;
        int VIEW_NETWORK_MAP = 1;
    }

    @IntDef(value = {ActionBarActions.BOTTOM_MENU, ActionBarActions.WIFI_ONBOARDING})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ActionBarActions {

        int BOTTOM_MENU = R.id.btn_zones;
        int WIFI_ONBOARDING = R.id.btn_wifi_onboarding;
    }

    private SpeakersListFragment mSpeakerFragment = null;
    private MusicFragment mMusicFragment = null;

    private AndroidUpnpService mAndroidUpnpService = null;

    public static final int SHOW_SETTINGS_REQUEST = 1000;

    private int mCurrentView = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrentView = ViewType.VIEW_NONE;

        Fragment fragment = SpeakersListFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment).commit();
        mCurrentView = ViewType.VIEW_BY_TYPE;

//        mMusicFragment = (MusicFragment) getSupportFragmentManager().findFragmentById(R.id.music_fragment);

        getApplicationContext().bindService(
                new Intent(this, UpnpService.class),
                mServiceConnection,
                BIND_AUTO_CREATE
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAllPlayManager != null) {
            List<IoTGroup> groups = mAllPlayManager.getGroups();
            if (!groups.isEmpty() && (mAllPlayManager.getCurrentGroup() == null)) {
                mAllPlayManager.setCurrentZone(groups.get(0));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBlurManager.unblur();
    }

    @Override
    protected void onDestroy() {
        if (mApp.isInit()) {
            mApp.stop();
        }
        getApplicationContext().unbindService(mServiceConnection);
        IoTService.getInstance().dispose();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void setupToolbar(@IdRes int resId) {
        Toolbar toolbar = findViewById(resId);
        if (toolbar == null) {
            return;
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SHOW_SETTINGS_REQUEST:
                if (resultCode == RESULT_RESET) {
                    reboot();
                }
                break;
        }
    }


    public void reboot() {
        if (!isFinishing()) {
            finish();
        }
        startActivity(new Intent(this, StartActivity.class));
    }


    /**
     * <p>This method replaces the {@link R.id#main_layout main_layout} container with the given
     * fragment.</p>
     *
     * @param fragment
     *         The fragment to display in the UI.
     */
    public void showFragmentInUiThread(Fragment fragment) {
        UiThreadExecutor.getInstance().execute(() -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                                    R.anim.slide_in_left, R.anim.slide_out_right);
            fragmentTransaction.replace(R.id.main_layout, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }

    /**
     * This method is called when the overflow menu button of a group has been pressed.
     *
     * @param object
     *         The IoTRepository whose menu button has been pressed.
     */
    @Override
    public void onZoneMenuSelected(IoTRepository object) {
        IoTType type = getActualIoTType(object);
        switch (type) {
            case UNKNOWN:
                return;
            case GROUP:
                IoTGroup group = (IoTGroup) object;
                IoTPlayer player = group.getLeadPlayer();
                if (player != null) {
                    showBottomSheetDeviceDialogFragment(player.getPlayerId(), player.getHostName(), object);
                }
                break;
            case PLAYER:
            case SOUND_BAR:
                player = ((IoTGroup) object).getLeadPlayer();
                if (player != null) {
                    showBottomSheetDeviceDialogFragment(player.getPlayerId(), player.getHostName(), object);
                }
                break;
            case SATELLITE_SPEAKER:
                IoTSurround surround = (IoTSurround) object;
                if (surround != null) {
                    showBottomSheetDeviceDialogFragment(surround.getId(), surround.getName(), object);
                }
                break;
        }
    }

    @Override
    public void onZoneSelected(IoTRepository object) {
        IoTType type = getActualIoTType(object);
        if (mMusicFragment != null) {
            mMusicFragment.updateInUiThread();
        }
        else {
            showFragmentInUiThread(MusicFragment.newInstance(object.getId(), type));
        }
    }

    @Override
    public void onDeviceSetting(String id, String host) {
        showFragmentInUiThread(DeviceSettingsFragment.newInstance(id, host));
    }

    @Override
    public void onGroupSpeakerFragmentSubmit(final String zoneID, final Set<IoTPlayer> playerSet) {
    }

    @Override
    public void onGroupSpeakerFragmentDismiss() {
    }


    @Override
    public void onMusicFragmentFinished() {
        onBackPressed();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mAndroidUpnpService = (AndroidUpnpService) service;
            UpnpProvider upnpProvider = UpnpProvider.getInstance();
            upnpProvider.setUpnpService(mAndroidUpnpService);
            mAndroidUpnpService.getRegistry().addListener(new UpnpRegistryListener());
            mAndroidUpnpService.getControlPoint().search();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mAndroidUpnpService = null;
            UpnpProvider upnpProvider = UpnpProvider.getInstance();
            upnpProvider.setUpnpService(mAndroidUpnpService);
        }
    };

    private class UpnpRegistryListener extends DefaultRegistryListener {

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            if (device.isFullyHydrated() &&
                    (device.findService(new UDAServiceType(UpnpService.CONTENT_DIRECTORY)) != null)) {
                if ((mMusicFragment != null) && mMusicFragment.isVisible()) {
                    mMusicFragment.onUpnpContentDirectoriesChanged();
                }
            }
        }

        public void deviceRemoved(final Device device) {
            if (device.findService(new UDAServiceType(UpnpService.CONTENT_DIRECTORY)) != null) {
                if ((mMusicFragment != null) && mMusicFragment.isVisible()) {
                    mMusicFragment.onUpnpContentDirectoriesChanged();
                }
            }
        }
    }

    @Override
    public void onSortingBottomDialogResult(int bottomSheetId, @SortingBottomMenuSelection int selectionId) {
        if (bottomSheetId == BottomMenuId.BOTTOM_MENU_HOME) {
            switch (selectionId) {
                case SortingBottomMenuSelection.NAVIGATE_ABOUT_PAGE:
                    navigateToAbout();
                    break;
                case SortingBottomMenuSelection.VIEW_BY_TYPE:
                    mCurrentView = ViewType.VIEW_BY_TYPE;
                    viewSpeaker();
                    break;
                case SortingBottomMenuSelection.VIEW_NETWORK_MAP:
                    mCurrentView = ViewType.VIEW_NETWORK_MAP;
                    viewNetworkMap();
                    break;
            }
        }
    }

    @Override
    public void onZoneBottomDialogResult(int bottomSheetId, @ZoneBottomMenuSelection int selectionId, String playerId
            , String playerHostname) {
        if (bottomSheetId == BottomMenuId.BOTTOM_MENU_DEVICE) {
            switch (selectionId) {
                case ZoneBottomMenuSelection.MANAGE_BT:
                    navigateToSettings(SettingsActivity.SettingsRoutes.BLUETOOTH_SETTINGS,
                                       playerId, playerHostname);
                    break;
                case ZoneBottomMenuSelection.MANAGE_ZIGBEE:
                    navigateToSettings(SettingsActivity.SettingsRoutes.ZIGBEE_SETTINGS,
                                       playerId, playerHostname);
                    break;
                case ZoneBottomMenuSelection.NAVIGATE_SETTINGS_PAGE:
                    navigateToSettings(SettingsActivity.SettingsRoutes.DEVICE_SETTINGS,
                                       playerId, playerHostname);
                    break;
                case ZoneBottomMenuSelection.PAIR_BT:
                    navigateToSettings(SettingsActivity.SettingsRoutes.BLUETOOTH_SETTINGS,
                                       playerId, playerHostname);
                    break;
                case ZoneBottomMenuSelection.PAIR_ZIGBEE:
                    navigateToSettings(SettingsActivity.SettingsRoutes.ZIGBEE_SETTINGS,
                                       playerId, playerHostname);
                    break;
            }
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof ZoneBottomMenuFragment) {
            ((ZoneBottomMenuFragment) fragment).setBottomDialogResultListener(this);
        }

        if (fragment instanceof SortingBottomMenuFragment) {
            ((SortingBottomMenuFragment) fragment).setBottomDialogResultListener(this);
        }
    }

    public void navigateToWifiOnboarding() {
        startActivity(new Intent(this, SetupActivity.class));
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
    }

    private void navigateToAbout() {
        startActivity(new Intent(this, AboutActivity.class));
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
    }

    private void navigateToSettings(int route, String id, String host) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(ExtraKeys.ROUTE_EXTRA, route);
        intent.putExtra(ExtraKeys.ID_EXTRA, id);
        intent.putExtra(ExtraKeys.HOST_EXTRA, host);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
    }

    private void viewNetworkMap() {
        NetworkTreeFragment networkTreeFragment = NetworkTreeFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_layout, networkTreeFragment)
                .commitNow();
    }

    private void viewSpeaker() {
        SpeakersListFragment fragment = SpeakersListFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_layout, fragment)
                .commitNow();
    }

    private IoTType getActualIoTType(IoTRepository object) {
        IoTType type = IoTType.UNKNOWN;
        if (object instanceof IoTGroup) {
            IoTGroup group = (IoTGroup) object;
            mAllPlayManager.setCurrentZone(group);
            type = IoTType.GROUP;
        }
        else if (object instanceof IoTPlayer) {
            IoTPlayer player = (IoTPlayer) object;
            type = player.isSoundBar() ? IoTType.SOUND_BAR : IoTType.PLAYER;
        }
        else if (object instanceof IoTSurround) {
            type = IoTType.SATELLITE_SPEAKER;
        }
        return type;
    }

    public void showBottomSheetDialogFragment() {
        if (getSupportFragmentManager() == null) {
            return;
        }
        SortingBottomMenuFragment sortingBottomMenuFragment = new SortingBottomMenuFragment();
        Bundle args = new Bundle();
        args.putInt(ExtraKeys.CURRENT_VIEW_EXTRA, mCurrentView);
        args.putInt(ExtraKeys.SHEET_ID_EXTRA, BottomMenuId.BOTTOM_MENU_HOME);
        sortingBottomMenuFragment.setArguments(args);
        sortingBottomMenuFragment.show(getSupportFragmentManager(), sortingBottomMenuFragment.getTag());
    }

    public void showBottomSheetDeviceDialogFragment(String playerId, String playerHostname, IoTRepository repo) {
        if (getSupportFragmentManager() == null) {
            return;
        }
        IoTType type = getActualIoTType(repo);
        ZoneBottomMenuFragment bottomSheetFragment = new ZoneBottomMenuFragment();
        Bundle args = new Bundle();
        args.putInt(ExtraKeys.SHEET_ID_EXTRA, BottomMenuId.BOTTOM_MENU_DEVICE);
        args.putString(ExtraKeys.PLAYER_ID_EXTRA, playerId);
        args.putString(ExtraKeys.HOSTNAME_EXTRA, playerHostname);
        args.putInt(ExtraKeys.TYPE_EXTRA, type.getValue());
        bottomSheetFragment.setArguments(args);
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }
}
