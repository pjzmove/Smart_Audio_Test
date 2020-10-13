/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.activity.MainActivity;
import com.qualcomm.qti.smartaudio.app.SmartAudioApplication;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.smartaudio.util.Utils;

import java.lang.ref.WeakReference;


public class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";
    protected boolean mIsSaveStateCalled = false;
    protected WeakReference<BaseActivity> mBaseActivityRef = null;
    protected BaseActivity mBaseActivity; // TODO delete and replace by getBaseActivity()
    protected SmartAudioApplication mApp = null;
    protected AllPlayManager mAllPlayManager = null;
    protected IoTSysManager mIoTSysManager = null;
    protected static final String EXTRA_ID = "DEVICE_ID";
    protected static final String EXTRA_HOST = "HOST_NAME";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mApp = (SmartAudioApplication) context.getApplicationContext();
        mBaseActivityRef = new WeakReference<>((BaseActivity) context);
        mBaseActivity = mBaseActivityRef.get();
        if ((mApp != null) && mApp.isInit()) {
            mAllPlayManager = mApp.getAllPlayManager();

            try {
                mIoTSysManager = IoTSysManager.getInstance();
            }
            catch (Exception e) {
                Log.w(TAG, "[onAttach] getting IoTSysManager failed with exception\n" + e.getMessage());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mIsSaveStateCalled = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsSaveStateCalled = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsSaveStateCalled = true;
    }

    /**
     * Update state objects in asynchronous method call
     */
    protected void updateState() {
    }

    /**
     * <p>This method is called by {@link #updateInUiThread(Object...) updateInUiThread(Object...)} and must be
     * implemented by child class if child class uses the calling method.</p>
     * <p>This method allows to run tasks within the UI Thread.</p>
     *
     * @param arguments
     *         The <code>arguments</code> required to update the state(s). These arguments must be given to the
     *         calling method.
     */
    protected void updateState(Object... arguments) {
    }

    /**
     * <p>This method places the given fragment into the view.</p>
     * <p>Depending on the activity this fragment is attached to, it either place it into its view directly
     * or asks the activity to do it.</p>
     *
     * @param fragment
     *         The fragment to display in the UI.
     */
    protected void showFragment(Fragment fragment) {
        BaseActivity refActivity = getBaseActivity();

        if (refActivity instanceof MainActivity) {
            ((MainActivity) refActivity).showFragmentInUiThread(fragment);
        }
        else {
            FragmentActivity activity = getActivity();
            FragmentManager manager = activity != null ? activity.getSupportFragmentManager() : null;
            if (manager == null) {
                Log.w(TAG, "[showFragment] fragment manager is null.");
                return;
            }

            FragmentTransaction trans = manager.beginTransaction();
            trans.replace(R.id.setting_container, fragment);
            trans.addToBackStack(null);
            trans.commit();
        }
    }

    /**
     * Update UI when state objects were updated
     */
    protected void updateUI() {
    }

    public void updateInUiThread() {
        if (Utils.isActivityActive(getBaseActivity())) {
            UiThreadExecutor.getInstance().execute(() -> {
                if (Utils.isActivityActive(getBaseActivity())) {
                    updateState();
                }
            });
        }
    }

    /**
     * <p>This method runs {@link #updateState(Object...) updateState(Object...)} in the UI thread and passes it the
     * given arguments.</p>
     *
     * @param arguments
     *         The arguments to pass to {@link #updateState(Object...) updateState(Object...)}
     */
    public void updateInUiThread(Object... arguments) {
        if (Utils.isActivityActive(getBaseActivity())) {
            UiThreadExecutor.getInstance().execute(() -> {
                if (Utils.isActivityActive(getBaseActivity())) {
                    updateState(arguments);
                }
            });
        }
    }

    /**
     * <p>To get the reference to the {@link BaseActivity} this fragment has been attached to.</p>
     *
     * @return the corresponding activity using its {@link WeakReference}.
     */
    protected BaseActivity getBaseActivity() {
        return mBaseActivityRef.get();
    }

    /**
     * <p>This method displays a dialog which includes an undeterminate progress bar.</p>
     * <p>The dialog can be dismissed by calling {@link #dismissDialog(String)}.</p>
     *
     * @param tag
     *         The tag to identify the dialog.
     * @param title
     *         The title to display, can be null.
     * @param message
     *         The message to display, can be null.
     *
     * @return The dialog fragment which is goign to be displayed within the UI.
     */
    protected CustomDialogFragment showProgressDialog(@NonNull final String tag, final String title, final String message) {
        CustomDialogFragment dialogFragment = CustomDialogFragment.newProgressDialog(tag, title, message, true);
        showDialog(tag, dialogFragment);
        return dialogFragment;
    }


    /**
     * <p>This method displays an informative dialog.</p>
     * <p>The dialog can be dismissed by calling {@link #dismissDialog(String)}.</p>
     *
     * @param tag
     *         The tag to identify the dialog.
     * @param title
     *         The title to display, can be null.
     * @param message
     *         The message to display, can be null.
     */
    protected void showDialog(@NonNull final String tag, final String title, final String message) {
        showDialog(tag, title, message, null, null, null);
    }


    /**
     * <p>This method displays an informative dialog.</p>
     * <p>The dialog can be dismissed by calling {@link #dismissDialog(String)}.</p>
     *
     * @param tag
     *         The tag to identify the dialog.
     * @param title
     *         The title to display, can be null.
     * @param message
     *         The message to display, can be null.
     * @param positiveButton
     *         The label for the dialog positive button, can be null.
     * @param negativeButton
     *         The label for the dialog negative button, can be null.
     * @param listener
     *         The listener to be notified when the user presses the negative or the positive button.
     */
    protected void showDialog(@NonNull final String tag, final String title, final String message,
                              final String positiveButton, final String negativeButton,
                              CustomDialogFragment.OnCustomDialogButtonClickedListener listener) {
        CustomDialogFragment dialogFragment = CustomDialogFragment.newDialog(tag, title, message, positiveButton,
                                                                             negativeButton, true);
        dialogFragment.setButtonClickedListener(listener);
        showDialog(tag, dialogFragment);
    }

    /**
     * <p>To hide a dialog from the UI.</p>
     *
     * @param tag
     *         The tag to identify the dialog to dismiss.
     */
    protected void dismissDialog(@NonNull final String tag) {
        BaseActivity activity = getBaseActivity();

        if (activity == null) {
            Log.i(TAG, "[dismissDialog] Activity is null, tag=" + tag);
            return;
        }

        activity.dismissDialog(tag);
    }

    /**
     * <p>This method commits the given dialog into the UI.</p>
     * <p>The dialog can be dismissed by calling {@link #dismissDialog(String)}.</p>
     *
     * @param tag
     *         The tag to identify the dialog.
     * @param dialogFragment
     *         The dialog to display in the UI.
     */
    private void showDialog(@NonNull final String tag, final CustomDialogFragment dialogFragment) {
        BaseActivity activity = getBaseActivity();

        if (activity == null) {
            Log.i(TAG, "[showDialog] Activity is null, tag=" + tag);
            return;
        }

        activity.showDialog(dialogFragment, tag);
    }

}
