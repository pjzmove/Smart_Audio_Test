/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.adapter.WifiNetworksAdapter;
import com.qualcomm.qti.smartaudio.app.SmartAudioApplication;
import com.qualcomm.qti.smartaudio.manager.onboarding.models.AuthType;
import com.qualcomm.qti.smartaudio.manager.onboarding.OnBoardingManager;
import com.qualcomm.qti.smartaudio.manager.onboarding.models.ConnectingStatus;
import com.qualcomm.qti.smartaudio.manager.onboarding.models.ConnectionStatus;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.NetworksListError;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.OnBoardingError;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.OnBoardingStep;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.RequestResult;
import com.qualcomm.qti.smartaudio.model.WifiNetwork;
import com.qualcomm.qti.smartaudio.view.StepView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static com.qualcomm.qti.smartaudio.manager.onboarding.WifiConnector.ConnectionError;

public class ChooseWifiFragment extends SetupListFragment {

    // ========================================================================
    // CONSTANTS

    /**
     * The tag to use to display logs.
     */
    private static final String TAG = "ChooseWifiFragment";

    private static final int PASSWORD_LENGTH_MIN = 8;


    // ========================================================================
    // FIELDS

    /**
     * The SSID of the WiFi network to be connected with - this should correspond to the speaker gateway.
     */
    private String mSsidSpeaker = "";
    /**
     * A specific item which displays the "add a network" option.
     */
    private WifiNetwork mAddNetworkItem;
    /**
     * The dialog which manages to display the process of the on boarding.
     */
    private OnBoardingDialogFragment mOnBoardingDialog;
    /**
     * The on boarding manager in order to be able to connect to a speaker.
     */
    private OnBoardingManager mOnBoardingManager;
    /**
     * The adapter which manages the display of WiFi Networks the speaker can board with.
     */
    private WifiNetworksAdapter mAdapter;
    /**
     * To be notified of on boarding events.
     */
    private OnBoardingManager.OnBoardingListener mOnBoardingListener = new OnBoardingManager.OnBoardingListener() {

        @Override
        public void onStepComplete(OnBoardingStep step) {
            mOnBoardingDialog.setStep(step.ordinal(), StepView.StepState.SUCCESS);
        }

        @Override
        public void onStepStart(OnBoardingStep step) {
            mOnBoardingDialog.setStep(step.ordinal(), StepView.StepState.PROGRESS);
        }

        @Override
        public void onError(OnBoardingStep step, OnBoardingError error, Object[] args) {
            onBoardingError(step, error, args);
        }

        @Override
        public void onComplete() {
            onBoardingComplete();
        }
    };
    /**
     * To be notified of events when requesting the list of networks from the speaker.
     */
    private OnBoardingManager.NetworksListener mNetworkListener = new OnBoardingManager.NetworksListener() {
        @Override
        public void onError(NetworksListError error, Object[] args) {
            dismissDialog(Tags.DIALOG_PROGRESS_UPDATING_NETWORKS);
            onNetworksListError(error, args);
        }

        @Override
        public void onComplete(List<WifiNetwork> networks) {
            dismissDialog(Tags.DIALOG_PROGRESS_UPDATING_NETWORKS);
            onNetworkListUpdated(networks);
        }
    };


    // ========================================================================
    // ENUMERATIONS

    /**
     * All tags used in this fragment to display dialogs.
     */
    @StringDef({Tags.DIALOG_ON_BOARDING_STEPS, Tags.DIALOG_NETWORK_LIST_REQUEST_ERROR,
            Tags.DIALOG_COMMUNICATION_ERROR, Tags.DIALOG_NETWORK_LIST_NETWORK_ERROR, Tags.DIALOG_REQUEST_ERROR,
            Tags.DIALOG_PASSWORD, Tags.DIALOG_ADD_WIFI_NETWORK, Tags.DIALOG_NETWORK_LIST_UNKNOWN_ERROR,
            Tags.DIALOG_PROGRESS_UPDATING_NETWORKS})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Tags {

        String DIALOG_ON_BOARDING_STEPS = "DIALOG_ON_BOARDING_STEPS";
        String DIALOG_COMMUNICATION_ERROR = "DIALOG_COMMUNICATION_ERROR";
        String DIALOG_NETWORK_LIST_REQUEST_ERROR = "DIALOG_NETWORK_LIST_REQUEST_ERROR";
        String DIALOG_NETWORK_LIST_NETWORK_ERROR = "DIALOG_NETWORK_LIST_NETWORK_ERROR";
        String DIALOG_NETWORK_LIST_UNKNOWN_ERROR = "DIALOG_NETWORK_LIST_UNKNOWN_ERROR";
        String DIALOG_REQUEST_ERROR = "DIALOG_REQUEST_ERROR";
        String DIALOG_PASSWORD = "DIALOG_PASSWORD";
        String DIALOG_ADD_WIFI_NETWORK = "DIALOG_ADD_WIFI_NETWORK";
        String DIALOG_PROGRESS_UPDATING_NETWORKS = "DIALOG_PROGRESS_UPDATING_NETWORKS";
    }

    /**
     * All keys used to read arguments set when setting this fragment.
     */
    @StringDef({Keys.EXPECTED_NETWORK})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Keys {

        String EXPECTED_NETWORK = "EXPECTED_NETWORK";
    }

    /**
     * List of actions to recognise when the user interacts with the buttons of the different dialogs displayed within
     * this fragment.
     */
    @IntDef({Actions.GET_NETWORKS_LIST, Actions.ON_BOARD, Actions.BOARDING_COMPLETE, Actions.ON_BOARDING_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Actions {

        int GET_NETWORKS_LIST = 0;
        int ON_BOARD = 1;
        int BOARDING_COMPLETE = 2;
        int ON_BOARDING_ERROR = 3;
    }


    // ========================================================================
    // INSTANCE BUILDER

    public static ChooseWifiFragment newInstance(String tag, String expectedNetwork) {
        ChooseWifiFragment fragment = new ChooseWifiFragment();
        Bundle args = new Bundle();
        args.putString(SETUP_TAG_KEY, tag);
        args.putString(Keys.EXPECTED_NETWORK, expectedNetwork);
        fragment.setArguments(args);
        return fragment;
    }


    // ========================================================================
    // FRAGMENT METHODS

    @Override // Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        Bundle args = getArguments();

        if (args != null && args.containsKey(Keys.EXPECTED_NETWORK)) {
            mSsidSpeaker = args.getString(Keys.EXPECTED_NETWORK, "");
        }

        if (mOnBoardingManager == null) {
            mOnBoardingManager = ((SmartAudioApplication) context.getApplicationContext()).getOnBoardingManager();
        }
    }

    @Override // Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mActionBarTitleTextView.setText(getString(R.string.choose_wifi_network));
        mActionBarTitleTextView.setContentDescription(getString(R.string.cont_desc_screen_on_boarding_choose_network));

        mAddNetworkItem = new WifiNetwork(getString(R.string.add_wifi), 0, true);
        mAdapter = new WifiNetworksAdapter(mAddNetworkItem);
        mExpandableListView.setAdapter(mAdapter);

        TextView listFooterTextView = (TextView) inflater.inflate(R.layout.list_footer, mExpandableListView, false);
        listFooterTextView.setText(getString(R.string.choose_wifi_list_footer));
        mExpandableListView.setFooterDividersEnabled(false);
        mExpandableListView.addFooterView(listFooterTextView);

        mExpandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            WifiNetwork network = mAdapter.getChild(groupPosition, childPosition);
            onNetworkSelected(network);
            return true;
        });

        // init bottom menu with a refresh button
        setOneButtonSetup();
        mBottomButton.setText(getString(R.string.refresh));

        // init top instruction
        mInstructionTextView.setText(getString(R.string.choose_wifi_network));

        return view;
    }

    @Override // Fragment
    public void onResume() {
        super.onResume();
        updateNetworksList();
    }

    @Override // Fragment
    public void onPause() {
        super.onPause();
        // if the dialog is still visible, it is dismissed.
        dismissDialog(Tags.DIALOG_PROGRESS_UPDATING_NETWORKS);
    }

    @Override // BaseFragment
    protected void updateState() {
        super.updateState();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override // SetupFragment
    public void onBottomButtonClicked() {
        onRefresh();
    }


    // ========================================================================
    // UI PRIVATE METHODS

    private void showPasswordDialog(WifiNetwork network) {
        final EditTextDialogFragment passwordDialogFragment = PasswordEditTextDialogFragment
                .newEditTextDialog(Tags.DIALOG_PASSWORD, getString(R.string.password_title,
                                                                   network.getSSID()),
                                   getString(R.string.password_hint),
                                   getString(R.string.button_connect),
                                   getString(R.string.button_cancel), PASSWORD_LENGTH_MIN);

        passwordDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked(String tag) {
                onboardDevice(network.getSSID(), passwordDialogFragment.getText(), AuthType.SECURE);
            }

            @Override
            public void onNegativeButtonClicked(String tag) {
                // nothing to do
            }
        });

        passwordDialogFragment.setEditTextKeyClickedListener((tag, editText) -> onboardDevice(network.getSSID(),
                                                                                              editText.getText().toString(), AuthType.SECURE));
        mBaseActivity.showDialog(passwordDialogFragment, Tags.DIALOG_PASSWORD);
    }

    private void showAddWifiDialog() {
        final AddWifiDialogFragment addWifiNetworkDialogFragment = AddWifiDialogFragment
                .newAddWifiNetworkDialog(Tags.DIALOG_ADD_WIFI_NETWORK,
                                         getString(R.string.add_wifi_network_title),
                                         getString(R.string.button_connect),
                                         getString(R.string.button_cancel), null, PASSWORD_LENGTH_MIN);

        addWifiNetworkDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked(String tag) {
                String ssid = addWifiNetworkDialogFragment.getSsid();
                AuthType authType = addWifiNetworkDialogFragment.getAuthType();
                String password = addWifiNetworkDialogFragment.getPassword();
                onboardDevice(ssid, password, authType);
            }

            @Override
            public void onNegativeButtonClicked(String tag) {

            }
        });

        addWifiNetworkDialogFragment.setEditTextKeyClickedListener((tag, passwordEditText) -> {
            String ssid = addWifiNetworkDialogFragment.getSsid();
            AuthType authType = addWifiNetworkDialogFragment.getAuthType();
            String password = addWifiNetworkDialogFragment.getPassword();
            onboardDevice(ssid, password, authType);
        });
        mBaseActivity.showDialog(addWifiNetworkDialogFragment, Tags.DIALOG_ADD_WIFI_NETWORK);
    }

    private void showOnBoardingDialog(String connectedNetwork, String ssid) {
        // set the dialog parameters
        String tag = Tags.DIALOG_ON_BOARDING_STEPS;
        String title = getString(R.string.on_boarding_dialog_title);
        String message = getString(R.string.connecting_message_connection_to_network,
                                   connectedNetwork, ssid);
        String negativeButton = getString(R.string.button_cancel);

        // build the dialog
        mOnBoardingDialog = OnBoardingDialogFragment
                .newOnBoardingDialog(tag, title, message, negativeButton);
        mOnBoardingDialog.setButtonClickedListener(buildCustomDialogListener(Actions.ON_BOARD));

        // show the dialog
        mBaseActivity.showDialog(mOnBoardingDialog, tag);
    }

    private void showConnectionError(OnBoardingStep step, ConnectionStatus connectionStatus) {
        // the connectionStatus is only success or failed at the moment => no failure reason to display
        showOnBoardingError(step, addStatusToMessage(getString(R.string.on_boarding_connection_error),
                                                     connectionStatus.toString(), connectionStatus.ordinal()));
    }

    private void showBoardingWifiNetworkError(OnBoardingStep step, String ssid, @ConnectionError int connectionError) {
        String message = connectionError == ConnectionError.CONNECTION_POOR_LINK ?
                getString(R.string.on_boarding_error_poor_link_message, ssid)
                : getString(R.string.on_boarding_error_wifi_connection_message, ssid);
        showOnBoardingError(step, message);
    }

    private void showNetworkListRequestError(RequestResult requestResult, Object[] args) {
        String title = getString(R.string.networks_list_request_error_title);
        String message = getString(R.string.networks_list_request_error_message, mSsidSpeaker,
                                   getErrorReason(requestResult, args));

        showDialog(Tags.DIALOG_NETWORK_LIST_REQUEST_ERROR, title, message, getString(R.string.try_again),
                   getString(R.string.button_exit_setup), buildCustomDialogListener(Actions.GET_NETWORKS_LIST));
    }

    private void showNetworkListConnectionError(String ssid, @ConnectionError int connectionError) {
        String title, message;
        switch (connectionError) {
            case ConnectionError.CONNECTION_POOR_LINK:
                title = getString(R.string.network_list_error_poor_link_title);
                message = getString(R.string.network_list_error_wifi_connection_message, ssid);
                break;
            case ConnectionError.CONNECTION_BLOCKED:
            case ConnectionError.CONNECTION_FAILED:
            default:
                title = getString(R.string.network_list_error_wifi_connection_title);
                message = getString(R.string.network_list_error_wifi_connection_message, ssid);
                break;
        }

        showDialog(Tags.DIALOG_NETWORK_LIST_NETWORK_ERROR, title, message, getString(R.string.try_again),
                   getString(R.string.button_exit_setup), buildCustomDialogListener(Actions.GET_NETWORKS_LIST));
    }

    private void showBoardingRequestError(OnBoardingStep step, RequestResult requestResult, Object[] args) {
        String message = getString(R.string.on_boarding_request_error_message, step.getRequestCommand(),
                                   getErrorReason(requestResult, args));
        showOnBoardingError(step, message);
    }

    private void showOnBoardingError(OnBoardingStep step, String message) {
        showOnBoardingError(step, message, getString(R.string.exit_setup), getString(R.string.try_again));
    }

    private void showOnBoardingError(OnBoardingStep step, String message, String negativeAction,
                                     String positiveAction) {
        mOnBoardingDialog.showStepError(step.ordinal(), message, negativeAction, positiveAction,
                                        buildCustomDialogListener(Actions.ON_BOARDING_ERROR));
    }


    // ========================================================================
    // PRIVATE METHODS

    private String addStatusToMessage(String message, String statusLabel, int statusValue) {
        return message + " " + getString(R.string.on_boarding_error_status, statusLabel, statusValue);
    }

    private void onNetworkSelected(WifiNetwork network) {
        // if it is add wifi
        if (mAddNetworkItem.equals(network)) {
            // add a wifi network
            showAddWifiDialog();
            return;
        }

        if (network.isOpenNetwork()) {
            onboardDevice(network.getSSID(), "", AuthType.OPEN);
            return;
        }

        // secure Wifi selected
        showPasswordDialog(network);
    }

    private void updateNetworksList() {
        showProgressDialog(Tags.DIALOG_PROGRESS_UPDATING_NETWORKS, getString(R.string.searching_for_networks), null);
        if (!mOnBoardingManager.requestNetworksList(mSsidSpeaker, mNetworkListener)) {
            Log.i(TAG, "[updateNetworksList] request returned false.");
            dismissDialog(Tags.DIALOG_PROGRESS_UPDATING_NETWORKS);
            showDialog(Tags.DIALOG_COMMUNICATION_ERROR, getString(R.string.communication_error_title),
                       getString(R.string.communication_error_message),
                       getString(R.string.try_again), getString(R.string.exit_setup),
                       buildCustomDialogListener(Actions.GET_NETWORKS_LIST));
        }

    }

    private void onboardDevice(String ssid, String password, AuthType authType) {
        // show progress
        showOnBoardingDialog(mSsidSpeaker, ssid);
        mOnBoardingManager.requestOnBoarding(mSsidSpeaker, mOnBoardingListener, authType == AuthType.WPS,
                                             ssid, password);
    }

    private void onPositiveButton(@Actions int action) {
        switch (action) {
            case Actions.BOARDING_COMPLETE:
                mSetupListFragmentListener.onItemClicked(mTag, mSsidSpeaker);
                return;

            case Actions.GET_NETWORKS_LIST:
                // try again: updating the network list
                updateNetworksList();
                return;

            case Actions.ON_BOARD:
                // unexpected
                Log.w(TAG, "[onPositiveButton] Unexpected tap on positive button for action ON_BOARD.");
                // cancelling all action
                mOnBoardingManager.cancel();
                return;

            case Actions.ON_BOARDING_ERROR:
                // try again: return to network selection
                // cancelling all action
                mOnBoardingManager.cancel();
                return;

            default:
                // do nothing
                Log.w(TAG, String.format("[onPositiveButton] Unexpected tap on positive button for unknown action " +
                                                 "(%d).", action));
                break;
        }
    }

    private void onNegativeButton(@Actions int action) {
        switch (action) {
            case Actions.ON_BOARD:
                // cancel
                mOnBoardingManager.cancel();
                // user needs to select and typed in again
                return;

            case Actions.BOARDING_COMPLETE:
                // unexpected
                Log.w(TAG, "[onNegativeButton] Unexpected tap on negative button for action BOARDING_COMPLETE.");
            case Actions.ON_BOARDING_ERROR:
            case Actions.GET_NETWORKS_LIST:
                // exit setup
                exitSetup();
                return;

            default:
                // unexpected
                Log.w(TAG, String.format("[onNegativeButton] Unexpected tap on negative button for unknown action " +
                                                 "(%d).", action));
                // exit setup
                exitSetup();
                break;
        }
    }

    private void exitSetup() {
        mOnBoardingManager.cancel();
        getBaseActivity().finish();
    }

    /**
     * <p>To refresh the list of networks: clear the displayed list and start the scan.</p>
     */
    private void onRefresh() {
        onNetworkListUpdated(new ArrayList<>());
        updateNetworksList();
    }

    private void onNetworkListUpdated(@NonNull List<WifiNetwork> networks) {
        List<WifiNetwork> recommended = new ArrayList<>();
        List<WifiNetwork> others = new ArrayList<>();

        for (WifiNetwork network : networks) {
            if (mOnBoardingManager.isConfiguredNetwork(network.getSSID())) {
                WifiNetwork.addNetwork(recommended, network);
            }
            else {
                WifiNetwork.addNetwork(others, network);
            }
        }

        others.add(mAddNetworkItem);

        mAdapter.updateList(recommended, others);
    }

    private void onBoardingComplete() {
        mOnBoardingDialog.showSuccess(getString(R.string.ok), buildCustomDialogListener(Actions.BOARDING_COMPLETE));
    }

    private void onBoardingError(OnBoardingStep step, OnBoardingError error, Object[] args) {
        switch (error) {
            case WIFI_NETWORK_ERROR:
                String networkSsid = args != null && args.length > 0 ? (String) args[0] : "UNKNOWN SSID";
                @ConnectionError int connectionError = args != null && args.length > 1 ? (int) args[1] :
                        ConnectionError.CONNECTION_FAILED;
                showBoardingWifiNetworkError(step, networkSsid, connectionError);
                return;

            case REQUEST_ERROR:
                RequestResult requestError = args != null && args.length > 0 ? (RequestResult) args[0]
                        : RequestResult.UNKNOWN;
                showBoardingRequestError(step, requestError, args);
                return;

            case INIT_REQUEST_FAILED:
                showOnBoardingError(step, getString(R.string.on_boarding_request_error_message,
                                                    step.getRequestCommand(),
                                                    getString(R.string.request_error_reason_request_failed)));
                return;

            case PROCESS_FAILED:
                showOnBoardingError(step, getString(R.string.on_boarding_request_error_message,
                                                    step.getRequestCommand(),
                                                    getString(R.string.request_error_reason_process_failed)));
                return;

            case ON_VERIFY_CONNECTING_FAILURE:
                ConnectingStatus connectingStatus = args != null && args.length >= 1 ? (ConnectingStatus) args[0]
                        : ConnectingStatus.UNKNOWN;
                onVerifyConnectingFailureError(step, connectingStatus);
                return;

            case CONNECTION_FAILED_REASON:
                ConnectionStatus connectionStatus = args != null && args.length >= 1 ? (ConnectionStatus) args[0]
                        : ConnectionStatus.UNKNOWN;
                showConnectionError(step, connectionStatus);
                return;

            case WPS_FAILED:
                showOnBoardingError(step, getString(R.string.on_boarding_wps_failed));
                return;

            default:
                showOnBoardingError(step, getString(R.string.on_boarding_unknown_error));
        }
    }

    private void onVerifyConnectingFailureError(OnBoardingStep step, ConnectingStatus connectingStatus) {
        String message, negativeAction, positiveAction;
        if (ConnectingStatus.CONNECTING.equals(connectingStatus)) {
            message = getString(R.string.on_boarding_verify_connecting_failure_connecting);
            // in this case the check_connect_status request is done, so the process is not exiting yet
            negativeAction = null;
            positiveAction = getString(R.string.cancel);
        }
        else {
            message = addStatusToMessage(getString(R.string.on_boarding_verify_connecting_failure),
                                         connectingStatus.toString(), connectingStatus.ordinal());
            negativeAction = getString(R.string.exit_setup);
            positiveAction = getString(R.string.try_again);
        }

        showOnBoardingError(step, message, negativeAction, positiveAction);
    }

    private void onNetworksListError(NetworksListError error, Object[] args) {
        switch (error) {
            case WIFI_NETWORK_ERROR:
                String networkSsid = args != null && args.length >= 1 ? (String) args[0] : "unknown";
                @ConnectionError int connectionError = args != null && args.length >= 2 ? (int) args[1] :
                        ConnectionError.CONNECTION_FAILED;
                showNetworkListConnectionError(networkSsid, connectionError);
                return;

            case REQUEST_ERROR:
                RequestResult requestResult = args != null && args.length >= 1 ? (RequestResult) args[0]
                        : RequestResult.UNKNOWN;
                showNetworkListRequestError(requestResult, args);
                return;

            default:
                showDialog(Tags.DIALOG_NETWORK_LIST_UNKNOWN_ERROR,
                           getString(R.string.networks_list_unknown_error_title),
                           getString(R.string.networks_list_unknown_error_message), getString(R.string.try_again),
                           getString(R.string.exit_setup), buildCustomDialogListener(Actions.GET_NETWORKS_LIST));
                break;
        }
    }

    private String getErrorReason(RequestResult requestResult, Object[] args) {
        switch (requestResult) {
            case BUILDING_REQUEST_FAILED:
            case SENDING_REQUEST_FAILED:
                return getString(R.string.request_error_reason_request_failed);

            case HTTP_REQUEST_FAILED:
                int statusCode = args.length >= 2 ? (int) args[1] : 0;
                return getString(R.string.request_error_reason_unsuccessful_http_status_code, statusCode);

            case READING_RESPONSE_FAILED:
                return getString(R.string.request_error_reason_malformed_response);

            case REQUEST_TIMEOUT:
                long timeoutMs = args.length > 1 ? (int) args[1] : -1;
                return getString(R.string.request_error_reason_time_out, timeoutMs / 1000);

            case OVER_MAX_ATTEMPTS:
                int attempts = args.length > 1 ? (int) args[1] : -1;
                long timeMs = args.length > 2 ? (long) args[2] : -1;
                return getString(R.string.request_error_reason_over_max_attempts, attempts, timeMs / 1000);

            case CREATE_REQUEST_FAILED:
                // unexpected as it is development dependant...
            case SUCCESS:
                // unexpected, really unexpected...
            case UNKNOWN:
            default:
                return getString(R.string.request_error_reason_unknown);
        }
    }

    private CustomDialogFragment.OnCustomDialogButtonClickedListener buildCustomDialogListener(@Actions int action) {
        return new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked(String tag) {
                onPositiveButton(action);
            }

            @Override
            public void onNegativeButtonClicked(String tag) {
                onNegativeButton(action);
            }
        };
    }

}
