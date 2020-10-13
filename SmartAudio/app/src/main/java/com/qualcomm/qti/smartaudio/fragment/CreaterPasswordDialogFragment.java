/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.util.Utils;

public class CreaterPasswordDialogFragment extends CustomDialogFragment {

    private static final String TAG = CreaterPasswordDialogFragment.class.getSimpleName();

    protected static final int CUSTOM_CREATE_PASSWORD_DIALOG = 6;

    private OnCreatePasswordKeyClickedListener mOnEditTextKeyClickedListener = null;

    private EditText mPasswordEditText = null;
    private EditText mRetypePasswordEditText = null;
    private TextView mErrorMessage = null;
    private View mView;
    private CheckBox mCheckBox;
    public static final String DEFAULT_PASSWORD = "000000";


    public static CreaterPasswordDialogFragment newCreatePasswordDialog(final String tag, final String title,
                                                                        final String positiveButton,
                                                                        final String negativeButton) {
        CreaterPasswordDialogFragment fragment = new CreaterPasswordDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, CUSTOM_CREATE_PASSWORD_DIALOG);
        args.putString(KEY_TAG, tag);
        if (!Utils.isStringEmpty(title)) {
            args.putString(KEY_TITLE, title.trim());
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

    public void setCreatePasswordKeyClickedListener(final OnCreatePasswordKeyClickedListener listener) {
        mOnEditTextKeyClickedListener = listener;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    protected Dialog createDialogFromType() {

        Dialog dialog = null;

        switch (mTypeDialog) {
            case CUSTOM_CREATE_PASSWORD_DIALOG:
                dialog = createEditTextDialog(R.layout.frame_create_password_dialog);
                break;
        }
        return dialog;
    }

    protected Dialog createEditTextDialog(int layout) {

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mView = inflater.inflate(R.layout.dialog_custom, null);
        final FrameLayout frameLayout = (FrameLayout) mView.findViewById(R.id.custom_dialog_frame);
        inflater.inflate(layout, frameLayout, true);

        final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialog);
        // Set the progress view
        dialog.setContentView(mView);

        setTitle(mView, Gravity.CENTER | Gravity.LEFT);
        setMessage(mView);

        setButtons(mView);

        mCheckBox = (CheckBox) mView.findViewById(R.id.create_password_show_password_checkbox);
        mPasswordEditText = (EditText) mView.findViewById(R.id.custom_dialog_create_password);
        mRetypePasswordEditText = (EditText) mView.findViewById(R.id.custom_dialog_retype_password);
        mErrorMessage = (TextView) mView.findViewById(R.id.dialog_password_error_text);

        mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mRetypePasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mPasswordEditText != null) {
                    mPasswordEditText.setTransformationMethod((!isChecked) ?
                                                                      PasswordTransformationMethod.getInstance() :
                                                                      HideReturnsTransformationMethod.getInstance());
                    mPasswordEditText.setSelection(mPasswordEditText.length());
                    mRetypePasswordEditText.setTransformationMethod((!isChecked) ?
                                                                            PasswordTransformationMethod.getInstance() : HideReturnsTransformationMethod.getInstance());
                    mRetypePasswordEditText.setSelection(mRetypePasswordEditText.length());
                }
            }
        });

        mRetypePasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (mOnEditTextKeyClickedListener != null && checkPasswords()) {
                        mOnEditTextKeyClickedListener.onDoneClicked(mTag, mPasswordEditText);
                        return true;
                    }
                }
                return false;
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                InputMethodManager imm =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromInputMethod(mPasswordEditText.getWindowToken(), 0);
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mPasswordEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        return dialog;
    }

    // Check passwords and display error message if needed
    private boolean checkPasswords() {
        Log.d(TAG, "checkPasswords()");
        mErrorMessage.setVisibility(View.VISIBLE);
        String passwordEntered = mPasswordEditText.getText().toString();
        if (passwordEntered.isEmpty() || mRetypePasswordEditText.getText().toString().isEmpty()) {
            mErrorMessage.setText(R.string.password_dialog_error_empty_password);
            return false;
        }
        if (!passwordEntered.equals(mRetypePasswordEditText.getText().toString())) {
            mErrorMessage.setText(R.string.password_dialog_error_mismatch_password);
            return false;
        }
        if (passwordEntered.equals(DEFAULT_PASSWORD)) {
            mErrorMessage.setText(R.string.password_dialog_error_invalid_password);
            return false;
        }
        mErrorMessage.setVisibility(View.INVISIBLE);
        return true;
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
                    if (mOnEditTextKeyClickedListener != null && checkPasswords()) {
                        Log.d(TAG, "dismiss dialog");
                        mBaseActivity.dismissDialog(mTag);
                        mOnEditTextKeyClickedListener.onDoneClicked(mTag, mPasswordEditText);
                    }
                }
            });
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
                }
            });
        }
    }

    public interface OnCreatePasswordKeyClickedListener {

        void onDoneClicked(final String tag, final EditText editText);
    }
}
