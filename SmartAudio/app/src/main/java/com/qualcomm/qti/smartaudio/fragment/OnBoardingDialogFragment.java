/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.smartaudio.view.StepView;

import static com.qualcomm.qti.smartaudio.view.StepView.StepState;

public class OnBoardingDialogFragment extends CustomDialogFragment {

    private static final String TAG = "OnBoardingDialogFragment";
    private static final int CUSTOM_ON_BOARDING_DIALOG = 7;
    private LinearLayout mStepContainer;
    private View mView;
    private boolean mIsInit = false;

    public static OnBoardingDialogFragment newOnBoardingDialog(final String tag, final String title,
                                                               final String message, final String negativeButton) {
        OnBoardingDialogFragment fragment = new OnBoardingDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, CUSTOM_ON_BOARDING_DIALOG);
        args.putString(KEY_TAG, tag);
        if (!Utils.isStringEmpty(title)) {
            args.putString(KEY_TITLE, title.trim());
        }
        if (!Utils.isStringEmpty(message)) {
            args.putString(KEY_MESSAGE, message.trim());
        }
        if (!Utils.isStringEmpty(negativeButton)) {
            args.putString(KEY_NEGATIVE_BUTTON, negativeButton.trim());
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setStep(int step, @StepState int state) {
        if (!mIsInit || step < 0 || mStepContainer.getChildCount() <= step) {
            return;
        }

        StepView stepView = (StepView) mStepContainer.getChildAt(step);

        if (stepView == null) {
            return;
        }

        stepView.updateState(state);
    }

    public void showStepError(int step, String errorMessage, String negativeButton, String positiveButton,
                              OnCustomDialogButtonClickedListener listener) {
        if (!mIsInit) {
            return;
        }

        StepView stepView = (StepView) mStepContainer.getChildAt(step);
        stepView.showErrorMessage(errorMessage);

        updateButtons(negativeButton, positiveButton, listener);
    }

    public void showSuccess(String positiveButton, OnCustomDialogButtonClickedListener listener) {
        if (!mIsInit) {
            return;
        }

        updateButtons(null, positiveButton, listener);
    }

    @Override
    protected Dialog createDialogFromType() {
        if (mTypeDialog != CUSTOM_ON_BOARDING_DIALOG) {
            Log.w(TAG, "[createDialogFromType] called with type not CUSTOM_ON_BOARDING_DIALOG");
            return null;
        }

        return buildDialog();
    }

    private View buildDialogView() {
        // init inflater for layouts
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflating main dialog view: the custom dialog
        mView = inflater.inflate(R.layout.dialog_custom, null);

        // add specific layout to the main view
        final FrameLayout frameLayout = mView.findViewById(R.id.custom_dialog_frame);
        inflater.inflate(R.layout.frame_wifi_on_boarding_steps_dialog, frameLayout, true);

        // setup the dialog with title, message and a cancel button
        setTitle(mView, Gravity.CENTER | Gravity.START);
        setMessage(mView);
        setButtons(mView);

        // get the components
        mStepContainer = mView.findViewById(R.id.setup_steps_list);

        // init the steps to the first one
        resetSteps();

        mIsInit = true;

        return mView;
    }

    private Dialog buildDialog() {
        final Dialog dialog = new Dialog(getContext(), R.style.CustomDialog);

        // Set the view
        View view = buildDialogView();
        dialog.setContentView(view);

        // View cannot be dismissed by the user
        dialog.setCancelable(false);

        return dialog;
    }

    private void updateButtons(String negativeButton, String positiveButton,
                               OnCustomDialogButtonClickedListener listener) {
        mPositiveTitle = positiveButton;
        mNegativeTitle = negativeButton;
        setButtons(mView);
        setButtonClickedListener(listener);
    }

    private void resetSteps() {
        int count = mStepContainer.getChildCount();
        // step 0 to be in progress
        ((StepView) mStepContainer.getChildAt(0)).updateState(StepState.PROGRESS);

        // other steps to be in "WAIT" state
        for (int i = 1; i < count; i++) {
            // put a check for previous step
            StepView stepView = (StepView) mStepContainer.getChildAt(i);
            stepView.updateState(StepView.StepState.WAIT);
        }
    }
}
