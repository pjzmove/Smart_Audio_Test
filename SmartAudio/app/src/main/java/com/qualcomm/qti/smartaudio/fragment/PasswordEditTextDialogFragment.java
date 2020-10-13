/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;


import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.util.Utils;

public class PasswordEditTextDialogFragment extends EditTextDialogFragment {

    private static final String TAG = PasswordEditTextDialogFragment.class.getSimpleName();
    protected static final String KEY_PASSWORD_LENGTH_MIN = "keyPasswordLengthMin";

    public static PasswordEditTextDialogFragment newEditTextDialog(final String tag, final String title,
                                                                   final String hint,
                                                                   final String positiveButton,
                                                                   final String negativeButton,
                                                                   int passwordLengthMin) {
        PasswordEditTextDialogFragment fragment = new PasswordEditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, CUSTOM_EDIT_TEXT_DIALOG);
        args.putString(KEY_TAG, tag);
        if (!Utils.isStringEmpty(title)) {
            args.putString(KEY_TITLE, title.trim());
        }
        if (!Utils.isStringEmpty(hint)) {
            args.putString(KEY_HINT, hint);
        }
        if (!Utils.isStringEmpty(positiveButton)) {
            args.putString(KEY_POSITIVE_BUTTON, positiveButton);
        }
        if (!Utils.isStringEmpty(negativeButton)) {
            args.putString(KEY_NEGATIVE_BUTTON, negativeButton);
        }
        args.putInt(KEY_PASSWORD_LENGTH_MIN, passwordLengthMin);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected Dialog createDialogFromType() {
        Dialog dialog = super.createDialogFromType();

        if (mEditText != null) {
            mEditText.setContentDescription(getString(R.string.cont_desc_enter_password));
        }

        return dialog;
    }

    protected void updateUI() {
        super.updateUI();
        CheckBox checkBox = (CheckBox) mView.findViewById(R.id.custom_dialog_show_password_checkbox);
        if (!Utils.isStringEmpty(mHint)) {
            mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            checkBox.setVisibility(View.VISIBLE);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (mEditText != null) {
                        mEditText.setTransformationMethod((!isChecked) ? PasswordTransformationMethod.getInstance() :
                                                                  HideReturnsTransformationMethod.getInstance());
                        mEditText.setSelection(mEditText.length());
                    }
                }
            });
        }
        else {
            checkBox.setVisibility(View.GONE);
        }
    }

    protected void setupEditTextChangedListener(final Button button) {
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setButtonProperty(button, (mEditText.getText().length() >= getArguments().getInt(KEY_PASSWORD_LENGTH_MIN)));
            }
        });
    }
}
