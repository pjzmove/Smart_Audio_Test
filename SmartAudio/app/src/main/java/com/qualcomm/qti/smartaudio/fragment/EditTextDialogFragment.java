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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.util.Utils;

public class EditTextDialogFragment extends CustomDialogFragment {

    private static final String TAG = "EditTextDialogFragment";

    private static final String KEY_TEXT = "keyText";
    protected static final String KEY_HINT = "keyHint";

    protected static final int CUSTOM_EDIT_TEXT_DIALOG = 3;

    private OnEditTextKeyClickedListener mOnEditTextKeyClickedListener = null;

    protected String mHint = null;

    private String mText = null;

    protected EditText mEditText = null;
    protected View mView;
    protected Button mPositiveButton = null;


    public static EditTextDialogFragment newEditTextDialog(final String tag, final String title, final String text,
                                                           final String hint, final String positiveButton,
                                                           final String negativeButton) {
        EditTextDialogFragment fragment = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, CUSTOM_EDIT_TEXT_DIALOG);
        args.putString(KEY_TAG, tag);
        if (!Utils.isStringEmpty(title)) {
            args.putString(KEY_TITLE, title.trim());
        }
        if (!Utils.isStringEmpty(text)) {
            args.putString(KEY_TEXT, text);
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
        fragment.setArguments(args);
        return fragment;
    }

    public void setEditTextKeyClickedListener(final OnEditTextKeyClickedListener listener) {
        mOnEditTextKeyClickedListener = listener;
    }

    public EditText getEditText() {
        return mEditText;
    }

    /**
     * To get the text typed in the edit text from this dialog.
     *
     * @return The current text from the edit text.
     */
    public String getText() {
        return mEditText == null ? "" : mEditText.getText().toString();
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        if (getArguments() != null) {
            mText = getArguments().getString(KEY_TEXT);
            mHint = getArguments().getString(KEY_HINT);
        }
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    protected Dialog createDialogFromType() {

        Dialog dialog = null;

        switch (mTypeDialog) {
            case CUSTOM_EDIT_TEXT_DIALOG:
                dialog = createEditTextDialog(R.layout.frame_edit_text_dialog);
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

        mEditText = mView.findViewById(R.id.custom_dialog_edit_text);
        String title = Utils.isStringEmpty(mTitle) ? "" : mTitle;
        mEditText.setContentDescription(getString(R.string.cont_desc_dialog_edit_text, title));

        updateUI();

        mPositiveButton = (Button) mView.findViewById(R.id.custom_dialog_positive_button);
        setButtonProperty(mPositiveButton, false);

        setupEditTextChangedListener(mPositiveButton);

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (mOnEditTextKeyClickedListener != null) {
                        mOnEditTextKeyClickedListener.onDoneClicked(mTag, mEditText);
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
                imm.hideSoftInputFromInputMethod(mEditText.getWindowToken(), 0);
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });


        return dialog;
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
                setButtonProperty(button, s.length() > 0);
            }
        });
    }

    protected void updateUI() {
        if (!Utils.isStringEmpty(mText)) {
            mEditText.setText(mText);
        }

        if (!Utils.isStringEmpty(mHint)) {
            mEditText.setHint(mHint);
        }
    }

    protected void setButtonProperty(Button button, boolean enable) {
        button.setEnabled(enable);
        button.setAlpha(enable ? 1f : 0.2f);
    }

    public interface OnEditTextKeyClickedListener {

        void onDoneClicked(final String tag, final EditText editText);
    }
}
