/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.util.SpannableStringParcel;
import com.qualcomm.qti.smartaudio.util.Utils;

import java.io.Serializable;
import java.util.Iterator;

import com.qualcomm.qti.smartaudio.manager.BlurManager.BlurListener;

public class CustomDialogFragment extends DialogFragment implements BlurListener {

    private static final String TAG = "CustomDialogFragment";

    // Internal dialog type IDs
    private static final int CUSTOM_DIALOG = 0;
    private static final int CUSTOM_PROGRESS_DIALOG = 1;
    private static final int CUSTOM_NO_WIFI_PROGRESS_DIALOG = 2;
    private static final int CUSTOM_SPANNABLE_STRING_DIALOG = 3;
    private static final int CUSTOM_FIRMWARE_UPDATE_DIALOG = 4;
    private static final int CUSTOM_DONE_DIALOG = 5;

    // Internal dialog keys
    protected static final String KEY_TYPE = "keyType";
    protected static final String KEY_DIM = "keyDim";
    protected static final String KEY_TAG = "keyTag";
    protected static final String KEY_TITLE = "keyTitle";
    protected static final String KEY_MESSAGE = "keyMessage";
    private static final String KEY_SPANNABLE_STRING_PARCEL = "keySpannableStringParcel";
    protected static final String KEY_ZONE_ID = "keyZoneID";
    protected static final String KEY_POSITIVE_BUTTON = "keyPositiveButton";
    protected static final String KEY_NEGATIVE_BUTTON = "keyNegativeButton";
    protected static final String KEY_LEFT_SURROUND_UPDATE_AVAILABLE = "keyLeftSurroundUpdateAvailable";
    protected static final String KEY_RIGHT_SURROUND_UPDATE_AVAILABLE = "keyRightSurroundUpdateAvailable";

    // Internal dialog values
    protected int mTypeDialog;
    protected String mTitle = null;
    protected String mMessage = null;
    protected SpannableStringParcel mSpannableMessage = null;
    protected String mTag = null;
    protected String mPositiveTitle = null;
    protected String mNegativeTitle = null;
    protected boolean mUseDefaultDim = false;

    private CheckBox mLeftSurroundCheckBox;
    private CheckBox mRightSurroundCheckBox;

    protected BaseActivity mBaseActivity = null;

    private OnCustomDialogButtonClickedListener mOnCustomDialogButtonClickedListener = null;
    private CreateViewListener mCreateViewListener = null;
    private TextView mMessageView;


    /**
     * <p>To build a dialog to indicate a successful event. The dialog can have optional title,
     * message and positive button. The dialog displays the image
     * {@link R.drawable#ic_success ic_succes} to indicate the success.</p>
     *
     * @param tag
     *         the tag to identify the dialog.
     * @param title
     *         an optional title to display in the dialog.
     * @param message
     *         an optional message to display in the UI.
     * @param positiveButton
     *         an optional label for the positive button. If no label is present,the
     *         positive button is not added to the dialog.
     *
     * @return the dialog built with the given parameters.
     */
    public static CustomDialogFragment newDoneDialog(final String tag, final String title,
                                                     final String message, final String positiveButton) {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TAG, tag);
        args.putInt(KEY_TYPE, CUSTOM_DONE_DIALOG);

        if (!Utils.isStringEmpty(title)) {
            args.putString(KEY_TITLE, title.trim());
        }
        if (!Utils.isStringEmpty(message)) {
            args.putString(KEY_MESSAGE, message.trim());
        }
        if (!Utils.isStringEmpty(positiveButton)) {
            args.putString(KEY_POSITIVE_BUTTON, positiveButton);
        }

        fragment.setArguments(args);
        return fragment;
    }

    public static CustomDialogFragment newProgressDialog(final String tag,
                                                         final String title, final String message) {
        return newProgressDialog(tag, title, message, false);
    }

    public static CustomDialogFragment newProgressDialog(final String tag, final String title, final String message,
                                                         final boolean useDefaultDim) {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, CUSTOM_PROGRESS_DIALOG);
        args.putString(KEY_TAG, tag);
        args.putBoolean(KEY_DIM, useDefaultDim);

        if (!Utils.isStringEmpty(title)) {
            args.putString(KEY_TITLE, title.trim());
        }
        if (!Utils.isStringEmpty(message)) {
            args.putString(KEY_MESSAGE, message.trim());
        }

        fragment.setArguments(args);
        return fragment;
    }

    public static CustomDialogFragment newNoWifiDialog() {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, CUSTOM_NO_WIFI_PROGRESS_DIALOG);
        args.putString(KEY_TAG, BaseActivity.DIALOG_NO_WIFI_TAG);

        fragment.setArguments(args);
        return fragment;
    }

    public static CustomDialogFragment newDialog(final String tag,
                                                 final String title, final String message,
                                                 final String positiveButton, final String negativeButton) {
        return newDialog(tag, title, message, positiveButton, negativeButton, false);
    }

    public static CustomDialogFragment newDialog(final String tag,
                                                 final String title, final String message,
                                                 final String positiveButton, final String negativeButton,
                                                 boolean useDefaultDim) {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, CUSTOM_DIALOG);
        args.putString(KEY_TAG, tag);
        args.putBoolean(KEY_DIM, useDefaultDim);

        if (!Utils.isStringEmpty(title)) {
            args.putString(KEY_TITLE, title.trim());
        }
        if (!Utils.isStringEmpty(message)) {
            args.putString(KEY_MESSAGE, message.trim());
        }
        if (!Utils.isStringEmpty(positiveButton)) {
            args.putString(KEY_POSITIVE_BUTTON, positiveButton);
        }
        if (!Utils.isStringEmpty(negativeButton)) {
            args.putString(KEY_NEGATIVE_BUTTON, negativeButton);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public static CustomDialogFragment newFirmwareUpdateDialog(final String tag,
                                                               final String title, final String message,
                                                               final String positiveButton,
                                                               final String negativeButton) {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, CUSTOM_FIRMWARE_UPDATE_DIALOG);
        args.putString(KEY_TAG, tag);
        args.putString(KEY_TITLE, title);
        if (!Utils.isStringEmpty(title)) {
            args.putString(KEY_TITLE, title.trim());
        }
        if (!Utils.isStringEmpty(message)) {
            args.putString(KEY_MESSAGE, message.trim());
        }
        if (!Utils.isStringEmpty(positiveButton)) {
            args.putString(KEY_POSITIVE_BUTTON, positiveButton);
        }
        if (!Utils.isStringEmpty(negativeButton)) {
            args.putString(KEY_NEGATIVE_BUTTON, negativeButton);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public static CustomDialogFragment newSpannableStringDialog(final String tag,
                                                                final String title, final SpannableString message,
                                                                final String positiveButton,
                                                                final String negativeButton) {
        CustomDialogFragment fragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, CUSTOM_SPANNABLE_STRING_DIALOG);
        args.putString(KEY_TAG, tag);
        args.putString(KEY_TITLE, title);
        if (!Utils.isStringEmpty(title)) {
            args.putString(KEY_TITLE, title.trim());
        }
        if (message != null) {
            args.putParcelable(KEY_SPANNABLE_STRING_PARCEL, new SpannableStringParcel(message));
        }
        if (!Utils.isStringEmpty(positiveButton)) {
            args.putString(KEY_POSITIVE_BUTTON, positiveButton);
        }
        if (!Utils.isStringEmpty(negativeButton)) {
            args.putString(KEY_NEGATIVE_BUTTON, negativeButton);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setButtonClickedListener(final OnCustomDialogButtonClickedListener listener) {
        mOnCustomDialogButtonClickedListener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBaseActivity = (BaseActivity) context;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        // Go through all of the arguments and set internal values
        if (getArguments() != null) {
            Iterator<String> it = getArguments().keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (key.equals(KEY_TYPE)) {
                    mTypeDialog = getArguments().getInt(key);
                }
                else if (key.equals(KEY_TITLE)) {
                    mTitle = getArguments().getString(key);
                }
                else if (key.equals(KEY_MESSAGE)) {
                    mMessage = getArguments().getString(key);
                }
                else if (key.equals(KEY_SPANNABLE_STRING_PARCEL)) {
                    mSpannableMessage = getArguments().getParcelable(KEY_SPANNABLE_STRING_PARCEL);
                }
                else if (key.equals(KEY_TAG)) {
                    mTag = getArguments().getString(key);
                }
                else if (key.equals(KEY_POSITIVE_BUTTON)) {
                    mPositiveTitle = getArguments().getString(key);
                }
                else if (key.equals(KEY_NEGATIVE_BUTTON)) {
                    mNegativeTitle = getArguments().getString(key);
                }
                else if (key.equals(KEY_DIM)) {
                    mUseDefaultDim = getArguments().getBoolean(key);
                }
            }
        }

        Dialog dialog = createDialogFromType();

        if (dialog != null) {
            // dialog should use default dim background depending on parameter
            if (!mUseDefaultDim) {
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }

        if (dialog != null && mCreateViewListener != null) {
            mCreateViewListener.onViewCreated(dialog);
        }

        return dialog;
    }

    protected Dialog createDialogFromType() {
        Dialog dialog = null;
        switch (mTypeDialog) {
            case CUSTOM_DIALOG:
            case CUSTOM_SPANNABLE_STRING_DIALOG:
                dialog = createSpannableDialog();
                break;
            case CUSTOM_PROGRESS_DIALOG:
                // Progress dialog
                dialog = createCustomProgressDialog();
                break;
            case CUSTOM_NO_WIFI_PROGRESS_DIALOG:
                // No Wifi dialog have default title and message
                mTitle = getString(R.string.no_wifi_connect_title);
                mMessage = getString(R.string.no_wifi_connect_message);
                dialog = createCustomProgressDialog();
                break;
            case CUSTOM_FIRMWARE_UPDATE_DIALOG:
                dialog = createFirmwareUpdateDialog();
                break;
            case CUSTOM_DONE_DIALOG:
                dialog = createDoneDialog();
                break;
            default:
                break;
        }
        return dialog;
    }

    /**
     * <p>To build a dialog to indicate a successful event. The dialog can have optional title,
     * message and positive button. The dialog displays the image
     * {@link R.drawable#ic_success ic_succes} to indicate the success.</p>
     * <p>This method builds the dialog view with the parameters provided when calling
     * {@link #newDoneDialog(String, String, String, String) newDoneDialog()}.</p>
     *
     * @return the dialog built with the given parameters.
     */
    private Dialog createDoneDialog() {
        // Create a dialog with style
        final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialog);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Use custom layout with done image
        View view = inflater.inflate(R.layout.dialog_custom, null);
        final FrameLayout frameLayout = view.findViewById(R.id.custom_dialog_frame);
        inflater.inflate(R.layout.frame_custom_dialog_done, frameLayout, true);

        // Set the custom view
        dialog.setContentView(view);

        setTitle(view, Gravity.CENTER);
        setMessage(view);

        setButtons(view);

        return dialog;
    }

    /**
     * This function creates a custom progress dialog with internal title and message
     *
     * @return the new custom progress Dialog object
     */
    private Dialog createCustomProgressDialog() {
        // Create a dialog with style
        final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialog);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Use custom progress layout
        View view = inflater.inflate(R.layout.dialog_custom, null);
        final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.custom_dialog_frame);
        inflater.inflate(R.layout.frame_custom_dialog_progress, frameLayout, true);
        final RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.custom_dialog_button_layout);
        relativeLayout.setVisibility(View.GONE);

        // Set the progress view
        dialog.setContentView(view);

        setTitle(view, Gravity.CENTER);
        setMessage(view);

        return dialog;
    }

    private Dialog createDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialog);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = inflater.inflate(R.layout.dialog_custom, null);

        // Set the progress view
        dialog.setContentView(view);

        setTitle(view, Gravity.CENTER | Gravity.LEFT);
        setMessage(view);

        setButtons(view);

        return dialog;
    }

    private Dialog createSpannableDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialog);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = inflater.inflate(R.layout.dialog_custom, null);

        // Set the progress view
        dialog.setContentView(view);

        setTitle(view, Gravity.CENTER | Gravity.START);
        setSpannableMessage(view);

        setButtons(view);

        return dialog;
    }

    public CheckBox getLeftSurroundCheckBox() {
        return mLeftSurroundCheckBox;
    }

    public CheckBox getRightSurroundCheckBox() {
        return mRightSurroundCheckBox;
    }

    private Dialog createFirmwareUpdateDialog() {
        // Create a dialog with style
        final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialog);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Use custom progress layout
        final View view = inflater.inflate(R.layout.dialog_custom, null);
        final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.custom_dialog_frame);
        inflater.inflate(R.layout.frame_custom_dialog_firmware_update, frameLayout, true);

        TextView messageTitle = (TextView) view.findViewById(R.id.custom_dialog_message_text);
        messageTitle.setText(getString(R.string.firmware_message_title));

        TextView updateMessage = (TextView) frameLayout.findViewById(R.id.custom_dialog_firmware_update_message_text);
        updateMessage.setText(getString(R.string.firmware_update_available_surrounds_message));

        mLeftSurroundCheckBox = (CheckBox) frameLayout.findViewById(R.id.custom_dialog_update_left_surround);
        mRightSurroundCheckBox = (CheckBox) frameLayout.findViewById(R.id.custom_dialog_update_right_surround);

        // Set the progress view
        dialog.setContentView(view);

        setTitle(view, Gravity.CENTER);
        setMessage(view);

        setButtons(view);

        return dialog;
    }

    protected void setTitle(final View view, final int gravity) {
        if (view == null) {
            return;
        }
        TextView titleView = (TextView) view.findViewById(R.id.custom_dialog_title_text);
        if ((titleView != null) && !Utils.isStringEmpty(mTitle)) {
            titleView.setText(mTitle);
            titleView.setGravity(gravity);
            view.setContentDescription(getString(R.string.cont_desc_dialog, mTitle));
        }
        else if (titleView != null) {
            titleView.setVisibility(View.GONE);
            view.setContentDescription(getString(R.string.cont_desc_dialog, ""));
        }
    }

    public void setMessage(String message) {
        if (mMessageView != null) {
            mMessageView.setText(message);
        }
    }

    protected void setMessage(final View view) {
        if (view == null) {
            return;
        }
        // Get the message view and set it if there is a message.  If message is empty, we hide the view to save the
        // spacing
        mMessageView = view.findViewById(R.id.custom_dialog_message_text);
        if ((mMessageView != null) && !Utils.isStringEmpty(mMessage)) {
            mMessageView.setText(mMessage);
            mMessageView.setVisibility(View.VISIBLE);
        }
        else if (mMessageView != null) {
            mMessageView.setVisibility(View.GONE);
        }
    }

    private void setSpannableMessage(final View view) {
        if (view == null) {
            return;
        }

        mMessageView = view.findViewById(R.id.custom_dialog_message_text);
        SpannableString content = mSpannableMessage != null ? mSpannableMessage.getSpannableString() : null;

        if ((mMessageView != null) && content != null && !content.toString().trim().isEmpty()) {
            mMessageView.setText(content);
            mMessageView.setVisibility(View.VISIBLE);
        }
        else if (mMessageView != null) {
            mMessageView.setVisibility(View.GONE);
        }
    }

    protected void setButtons(final View view) {
        if (view == null) {
            return;
        }

        Button positiveButton = (Button) view.findViewById(R.id.custom_dialog_positive_button);
        if ((positiveButton != null) && !Utils.isStringEmpty(mPositiveTitle)) {
            positiveButton.setText(mPositiveTitle);
            positiveButton.setContentDescription(getString(R.string.cont_desc_dialog_action, mPositiveTitle));
            positiveButton.setVisibility(View.VISIBLE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBaseActivity.dismissDialog(mTag);
                    if (mOnCustomDialogButtonClickedListener != null) {
                        mOnCustomDialogButtonClickedListener.onPositiveButtonClicked(mTag);
                    }
                }
            });
        }
        else if (positiveButton != null) {
            positiveButton.setVisibility(View.GONE);
            positiveButton.setOnClickListener(null);
        }

        Button negativeButton = (Button) view.findViewById(R.id.custom_dialog_negative_button);
        if ((negativeButton != null) && !Utils.isStringEmpty(mNegativeTitle)) {
            negativeButton.setText(mNegativeTitle);
            negativeButton.setContentDescription(getString(R.string.cont_desc_dialog_action, mNegativeTitle));
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBaseActivity.dismissDialog(mTag);
                    if (mOnCustomDialogButtonClickedListener != null) {
                        mOnCustomDialogButtonClickedListener.onNegativeButtonClicked(mTag);
                    }
                }
            });
        }
        else if (negativeButton != null) {
            negativeButton.setVisibility(View.GONE);
            negativeButton.setOnClickListener(null);
        }
    }

    @Override
    public void blurStarted() {

    }

    @Override
    public void blurFinished() {

    }

    @Override
    public void unblurStarted() {

    }

    @Override
    public void unblurFinished() {

    }

    public void setCreatedViewListener(CreateViewListener listener) {
        mCreateViewListener = listener;
    }

    public interface OnCustomDialogButtonClickedListener extends Serializable {

        void onPositiveButtonClicked(final String tag);

        void onNegativeButtonClicked(final String tag);
    }

    public interface CreateViewListener extends Serializable {

        void onViewCreated(Dialog dialog);
    }
}
