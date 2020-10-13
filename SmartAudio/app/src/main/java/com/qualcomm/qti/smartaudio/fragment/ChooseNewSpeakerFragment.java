/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.manager.onboarding.OnBoardingManager;
import com.qualcomm.qti.smartaudio.manager.onboarding.WifiConnector;
import com.qualcomm.qti.smartaudio.manager.onboarding.WifiScanner;
import com.qualcomm.qti.smartaudio.model.WifiNetwork;
import com.qualcomm.qti.smartaudio.adapter.NewSpeakerAdapter;
import com.qualcomm.qti.smartaudio.app.SmartAudioApplication;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static com.qualcomm.qti.smartaudio.manager.onboarding.WifiConnector.ConnectionError;
import static com.qualcomm.qti.smartaudio.manager.onboarding.WifiConnector.ConnectionState;
import static com.qualcomm.qti.smartaudio.manager.onboarding.WifiConnector.RequestStatus;

/**
 * This fragment displays a list of opened wifi networks found by a {@link WifiScanner WifiScanner} and connects to
 * the one selected by a user using {@link OnBoardingManager#connectToWifiNetwork(WifiNetwork)}.
 */
public class ChooseNewSpeakerFragment extends SetupListFragment {

    // ========================================================================
    // CONSTANTS

    /**
     * The list of all the tags used in this fragment to display other UI elements such as dialogs.
     */
    @StringDef({DialogTags.CONNECTING, DialogTags.SEARCHING, DialogTags.CONNECTION_FAILED, DialogTags.MESSAGE,
            DialogTags.POOR_LINK, DialogTags.SCANNING_FAILED})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DialogTags {

        String CONNECTING = "DIALOG_CONNECTING_TAG";
        String SEARCHING = "DIALOG_SEARCHING_TAG";
        String SCANNING_FAILED = "DIALOG_SCANNING_FAILED_TAG";
        String CONNECTION_FAILED = "DIALOG_CONNECTION_FAILED_TAG";
        String MESSAGE = "DIALOG_MESSAGE_TAG";
        String POOR_LINK = "DIALOG_POOR_LINK_TAG";
    }


    // ========================================================================
    // FIELDS

    /**
     * The text view to display the "no speaker found" message.
     */
    private TextView mTextViewNoSpeakerFoundMessage = null;
    /**
     * The text view to display the "no speaker found" title.
     */
    private TextView mTextViewNoSpeakerFoundTitle = null;
    /**
     * The image view to display when no speaker have been found.
     */
    private ImageView mImageViewNoSpeakerFound = null;
    /**
     * This is set to true when the scan is initiated by the app or the user in order to show/hide the searching
     * dialog. This is set to true by default as when this fragment starts a scan is initiated.
     */
    private boolean mScanInitiated = true;
    /**
     * The adapter to display a list of {@link WifiNetwork}.
     */
    private NewSpeakerAdapter mAdapter;
    /**
     * The scanner to be able to start and stop the scan of wifi networks as well as get the list of scanned networks.
     */
    private WifiScanner mWifiScanner;
    /**
     * The onboarding manager in order to be able to connect to a speaker.
     */
    private OnBoardingManager mOnBoardingManager;
    /**
     * The listener to get events from the wifi scanner.
     */
    WifiScanner.WifiScannerListener mWifiScannerListener = new WifiScanner.WifiScannerListener() {
        @Override
        public void onNetworksUpdated(List<WifiNetwork> networks) {
            if (mScanInitiated) {
                dismissDialog(DialogTags.SEARCHING);
                mScanInitiated = false;
            }
            updateInUiThread(networks);
        }

        @Override
        public void onError(@WifiScanner.WifiScannerError int error) {
            if (mScanInitiated) {
                dismissDialog(DialogTags.SEARCHING);
                mScanInitiated = false;
            }
            showScanningErrorDialog();
        }
    };
    /**
     * The listener to get events from the onboarding manager regarding the connection to a wifi network.
     */
    private WifiConnector.WifiConnectorListener mWifiConnectorListener = new WifiConnector.WifiConnectorListener() {
        @Override
        public void onConnectionStateUpdated(@NotNull String ssid, @ConnectionState int state) {
            if (state == ConnectionState.CONNECTED) {
                mOnBoardingManager.unsubscribeWifiConnectorListener(mWifiConnectorListener);
                dismissDialog(DialogTags.CONNECTING);
                onConnected(ssid);
            }
        }

        @Override
        public void onError(WifiNetwork network, @ConnectionError int error) {
            mOnBoardingManager.unsubscribeWifiConnectorListener(mWifiConnectorListener);
            dismissDialog(DialogTags.CONNECTING);
            onConnectionError(network, error);
        }
    };


    // ========================================================================
    // PUBLIC STATIC METHODS

    /**
     * <p>To build a new instance of this fragment. This method bundles the parameters to prepare the fragment when
     * it will be populated.</p>
     *
     * @param tag
     *         The tag to identify this fragment.
     *
     * @return A new instance of the fragment.
     */
    public static ChooseNewSpeakerFragment newInstance(String tag) {
        ChooseNewSpeakerFragment fragment = new ChooseNewSpeakerFragment();
        Bundle args = new Bundle();
        args.putString(SETUP_TAG_KEY, tag);
        fragment.setArguments(args);
        return fragment;
    }


    // ========================================================================
    // FRAGMENT METHODS

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mWifiScanner == null) {
            mWifiScanner = new WifiScanner(context.getApplicationContext());
        }
        if (mOnBoardingManager == null) {
            mOnBoardingManager = ((SmartAudioApplication) context.getApplicationContext()).getOnBoardingManager();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View frameView = inflater.inflate(R.layout.frame_setup_added, mEmptyFrameLayout, true);

        mImageViewNoSpeakerFound = frameView.findViewById(R.id.setup_added_image);
        mTextViewNoSpeakerFoundTitle = frameView.findViewById(R.id.setup_great_text);
        mTextViewNoSpeakerFoundMessage = frameView.findViewById(R.id.setup_added_text);

        mActionBarTitleTextView.setText(R.string.new_speakers);
        mActionBarTitleTextView.setContentDescription(getString(R.string.cont_desc_screen_on_boarding_choose_speaker));
        mInstructionTextView.setVisibility(View.GONE);
        mExpandableListView.setVisibility(View.GONE);

        setOneButtonSetup();

        mBottomButton.setText(getString(R.string.refresh));
        mBottomButton.setContentDescription(getString(R.string.cont_desc_button_refresh_list));

        mListView.setVisibility(View.VISIBLE);

        mAdapter = new NewSpeakerAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener((parent, view1, position, id) -> {
            WifiNetwork speaker = (WifiNetwork) parent.getItemAtPosition(position);
            onSpeakerSelected(speaker);
        });

        TextView listFooterTextView = (TextView) inflater.inflate(R.layout.list_footer, mListView, false);
        listFooterTextView.setText(getString(R.string.new_speaker_list_footer));
        mListView.setFooterDividersEnabled(false);
        mListView.addFooterView(listFooterTextView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateInUiThread();
        startScanning();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScanning();
    }


    // ========================================================================
    // BASE FRAGMENT METHODS

    @Override
    protected void updateState() {
        if (!mScanInitiated) {
            List<WifiNetwork> speakers = mWifiScanner != null ? mWifiScanner.getNetworks() : null;
            updateSpeakers(speakers);
        }
    }

    @Override
    protected void updateState(Object... arguments) {
        if (arguments != null && arguments.length >= 1 && arguments[0] instanceof List) {
            List<WifiNetwork> speakers = (List<WifiNetwork>) arguments[0];
            updateSpeakers(speakers);
        }
    }

    @Override // forces the tag to be one of @DialogTags
    protected void showDialog(@NonNull @DialogTags String tag, String title, String message) {
        super.showDialog(tag, title, message);
    }

    @Override // forces the tag to be one of @DialogTags
    protected void showDialog(@NonNull @DialogTags String tag, String title, String message, String positiveButton,
                              String negativeButton,
                              CustomDialogFragment.OnCustomDialogButtonClickedListener listener) {
        super.showDialog(tag, title, message, positiveButton, negativeButton, listener);
    }

    @Override // forces the tag to be one of @DialogTags
    protected void dismissDialog(@NonNull @DialogTags String tag) {
        super.dismissDialog(tag);
    }

    @Override // forces the tag to be one of @DialogTags and make the view not cancellable
    protected CustomDialogFragment showProgressDialog(@NonNull @DialogTags String tag, String title, String message) {
        CustomDialogFragment dialog = super.showProgressDialog(tag, title, message);
        dialog.setCancelable(false);
        return dialog;
    }


    // ========================================================================
    // SETUP FRAGMENT METHODS

    @Override
    public void onBottomButtonClicked() {
        onRefresh();
    }


    // ========================================================================
    // PRIVATE METHODS - EVENTS

    /**
     * <p>This method analyses the given status and acts upon it by displaying a dialog in case of an errors.</p>
     *
     * @param network
     *         The network for which the request failed to be initiated.
     * @param status
     *         The status of the request initialisation.
     */
    private void onConnectionRequestFailed(WifiNetwork network, @RequestStatus int status) {
        switch (status) {
            case RequestStatus.SUCCESS:
                // sanity check
                return;

            case RequestStatus.CANNOT_REMOVE_NETWORK:
                showMessageDialog(getString(R.string.connect_error_message_forget_network));
                return;
            case RequestStatus.UNEXPECTED_PARAMETER:
            case RequestStatus.CONFIGURATION_NOT_CREATED:
            case RequestStatus.FAIL:
                showTryAgainDialog(network);
                return;
            case RequestStatus.NOT_AN_OPEN_NETWORK:
                showMessageDialog(getString(R.string.connect_error_message_not_an_open_network));
                return;
            default:
                showMessageDialog(getString(R.string.connect_error_message_not_an_open_network));
                break;
        }
    }

    /**
     * <p>This method analyses the given error and acts upon it by displaying the corresponding dialog.</p>
     *
     * @param network
     *         The network for which the connection error occurred.
     * @param error
     *         The connection error which occurred.
     */
    private void onConnectionError(WifiNetwork network, @ConnectionError int error) {
        switch (error) {
            case ConnectionError.CONNECTION_POOR_LINK:
                showPoorLinkMessageDialog(network);
                break;
            case ConnectionError.CONNECTION_BLOCKED:
            case ConnectionError.CONNECTION_FAILED:
            default:
                showTryAgainDialog(network);
        }
    }

    /**
     * <p>This method initiates the connection to the selected network and prepare the UI for it.</p>
     *
     * @param speaker
     *         The speaker which has been selected by the user.
     */
    private void onSpeakerSelected(WifiNetwork speaker) {
        showConnectingDialog(speaker.getSSID());
        stopScanning();
        connectToSpeaker(speaker);
    }

    /**
     * <p>This method informs any listener that a speaker has been successfully connected.</p>
     *
     * @param ssid
     *         The SSID of the speaker which has been connected.
     */
    private void onConnected(String ssid) {
        mSetupListFragmentListener.onItemClicked(mTag, ssid);
    }


    // ========================================================================
    // PRIVATE METHODS - ACTIONS

    /**
     * <p>This method updates the list of displayed speakers with the given list.</p>
     * <p>If the given list is null or empty, this method displays that no speaker were found.</p>
     *
     * @param speakers
     *         The list of speakers to display within the UI.
     */
    private void updateSpeakers(List<WifiNetwork> speakers) {
        // updating instructions and no speaker found messages
        if (speakers == null || speakers.size() == 0) {
            showNoSpeakersFound();
        }
        else {
            mInstructionTextView.setVisibility(View.VISIBLE);
            mInstructionTextView.setText(getString(R.string.choose_new_speaker));
        }
        // updating list
        if (mAdapter != null) {
            mAdapter.setList(speakers);
        }
    }

    /**
     * <p>To refresh the list of displayed possible speakers to on board: clear the displayed list and start the
     * scan.</p>
     */
    private void onRefresh() {
        updateSpeakers(null);
        // user should not be able to click on the button if the scan is not initiated
        startScanning();
    }

    /**
     * <p>This method starts the scanning process by informing the user that a scan is in progress and asking the
     * WifiManager to scan for speakers.</p>
     */
    private void startScanning() {
        mScanInitiated = true;
        showProgressDialog(DialogTags.SEARCHING, getString(R.string.searching_for_new_speakers), null);
        Context context = getContext();
        if (context == null || !mWifiScanner.startScanning(context.getApplicationContext(), mWifiScannerListener)) {
            dismissDialog(DialogTags.SEARCHING);
            mScanInitiated = false;
            showScanningErrorDialog();
        }
    }

    /**
     * <p>This method stops the scanning process, it updates the UI if any scan was initiated and it asks the
     * WifiScanner to stop to scan for speakers.</p>
     */
    private void stopScanning() {
        if (mScanInitiated) {
            mScanInitiated = false;
            dismissDialog(DialogTags.SEARCHING);
        }
        Context context = getContext();
        if (mWifiScanner != null && context != null) {
            mWifiScanner.stopScanning(context.getApplicationContext());
        }
    }

    /**
     * <p>This method initiates the connection process to a speaker. It also registers a connection listener to get
     * connection events.</p>
     * <p>If the initialisation of the request fails, this method updates the UI accordingly.</p>
     *
     * @param speaker
     *         The speaker to connect with.
     */
    private void connectToSpeaker(WifiNetwork speaker) {
        mOnBoardingManager.subscribeWifiConnectorListener(mWifiConnectorListener);
        @RequestStatus int status = mOnBoardingManager.connectToWifiNetwork(speaker);

        if (status != RequestStatus.SUCCESS) {
            mOnBoardingManager.unsubscribeWifiConnectorListener(mWifiConnectorListener);
            dismissDialog(DialogTags.CONNECTING); // if already displayed
            onConnectionRequestFailed(speaker, status);
        }
        // wait for mOnBoardingManagerListener to get an update in case of SUCCESS
    }


    // ========================================================================
    // PRIVATE METHODS - UI

    /**
     * This method displays an undeterminate progress dialog to inform the user that a connection is in progress.
     *
     * @param name
     *         The SSID of the speaker as displayed when the user selected it.
     */
    private void showConnectingDialog(String name) {
        showProgressDialog(DialogTags.CONNECTING, getString(R.string.connecting),
                           getString(R.string.connecting_parametrised, name));
    }

    /**
     * This method displays a scanning error dialog.
     */
    private void showScanningErrorDialog() {
        CustomDialogFragment.OnCustomDialogButtonClickedListener listener =
                new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                    @Override
                    public void onPositiveButtonClicked(String tag) {
                        onRefresh();
                    }

                    @Override
                    public void onNegativeButtonClicked(String tag) {
                    }
                };
        showDialog(DialogTags.SCANNING_FAILED, getString(R.string.scanning_fail_title),
                   getString(R.string.scanning_fail_message),
                   getString(R.string.try_again), getString(R.string.cancel), listener);
    }

    /**
     * This method displays a dialog with the given message.
     *
     * @param message
     *         The message to display to the user.
     */
    private void showMessageDialog(String message) {
        CustomDialogFragment.OnCustomDialogButtonClickedListener listener =
                new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                    @Override
                    public void onPositiveButtonClicked(String tag) {
                        startScanning();
                    }

                    @Override
                    public void onNegativeButtonClicked(String tag) {
                    }
                };
        showDialog(DialogTags.MESSAGE, getString(R.string.connect_error_title), message,
                   getString(R.string.ok), null, listener);
    }

    /**
     * <p>This method shows a dialog which gives the possibility to the user to try again to connect with the given
     * network.</p>
     *
     * @param network
     *         The network to attempt a new connection with.
     */
    private void showTryAgainDialog(WifiNetwork network) {
        CustomDialogFragment.OnCustomDialogButtonClickedListener listener =
                new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                    @Override
                    public void onPositiveButtonClicked(String tag) {
                        showConnectingDialog(network.getSSID());
                        connectToSpeaker(network);
                    }

                    @Override
                    public void onNegativeButtonClicked(String tag) {
                        startScanning();
                    }
                };
        showDialog(DialogTags.CONNECTION_FAILED, getString(R.string.connect_error_title),
                   getString(R.string.connect_error_message),
                   getString(R.string.try_again), getString(R.string.cancel), listener);
    }

    /**
     * <p>This method shows a dialog to inform the user that the given network has a poor link. If the user decides
     * to continue anyway, this method validates the connection.</p>
     *
     * @param network
     *         The network which has a poor link.
     */
    private void showPoorLinkMessageDialog(WifiNetwork network) {
        CustomDialogFragment.OnCustomDialogButtonClickedListener listener =
                new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                    @Override
                    public void onPositiveButtonClicked(String tag) {
                        onConnected(network.getSSID());
                    }

                    @Override
                    public void onNegativeButtonClicked(String tag) {
                        startScanning();
                    }
                };
        showDialog(DialogTags.POOR_LINK, getString(R.string.poor_link_title),
                   getString(R.string.poor_link_message),
                   getString(R.string.button_continue), getString(R.string.cancel), listener);
    }

    /**
     * This method displays a message to inform the user that no speaker where found during the latest scanning.
     */
    private void showNoSpeakersFound() {
        mInstructionTextView.setVisibility(View.GONE);
        // updateState ui with no speaker found text and updateState adapter data
        mImageViewNoSpeakerFound.setImageResource(R.drawable.ic_error_question);
        mTextViewNoSpeakerFoundTitle.setText(getString(R.string.unable_to_find_allplay_speakers_header));
        mTextViewNoSpeakerFoundMessage.setText(getString(R.string.unable_to_find_allplay_speakers_message));
    }

}
