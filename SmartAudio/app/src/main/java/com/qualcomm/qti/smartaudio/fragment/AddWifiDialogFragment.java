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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.manager.onboarding.models.AuthType;
import com.qualcomm.qti.smartaudio.util.Utils;

public class AddWifiDialogFragment extends PasswordEditTextDialogFragment {

    private static final int CUSTOM_ADD_WIFI_NETWORK_DIALOG = 4;
    private static final String KEY_PASSWORD_LENGTH = "keyPasswordLength";

    private EditText mSsidEditText = null;
    private Spinner mSelectSecuritySpinner;
    private TextView mPasswordHeaderTextView;
    private String mDefaultSsid;

    public static AddWifiDialogFragment newAddWifiNetworkDialog(final String tag, final String title,
                                                                final String positiveButton,
                                                                final String negativeButton, String ssid,
                                                                int passwordLengthMin) {
        Bundle args = new Bundle();

        AddWifiDialogFragment fragment = new AddWifiDialogFragment();

        args.putInt(KEY_TYPE, CUSTOM_ADD_WIFI_NETWORK_DIALOG);
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
        args.putInt(KEY_PASSWORD_LENGTH, passwordLengthMin);


        args.putBoolean(KEY_DIM, true);

        fragment.setArguments(args);
        fragment.mDefaultSsid = ssid;
        return fragment;
    }

    public String getSsid() {
        return mSsidEditText.getText().toString();
    }

    public String getPassword() {
        return mEditText.getText().toString();
    }

    public AuthType getAuthType() {
        return (AuthType) mSelectSecuritySpinner.getSelectedItem();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Context context = getContext();
        if (context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSsidEditText.getWindowToken(), 0);
        }
    }

    @Override
    protected Dialog createDialogFromType() {
        Dialog dialog = null;

        if (mTypeDialog == CUSTOM_ADD_WIFI_NETWORK_DIALOG) {
            dialog = createAddWifiNetworkDialog();
        }
        return dialog;
    }

    private void updateDropdownList() {
        AuthType selection = getAuthType();

        if (selection == AuthType.WPS || selection == AuthType.OPEN) {
            mHint = "";
            updateUI();
            mPasswordHeaderTextView.setVisibility(View.GONE);
            mEditText.setVisibility(View.GONE);
            setButtonProperty(mPositiveButton, (mSsidEditText.getText().length() >= 1));
        }
        else {
            mEditText.getText().clear();
            mHint = getString(R.string.password_hint);
            updateUI();
            mPasswordHeaderTextView.setVisibility(View.VISIBLE);
            mEditText.setVisibility(View.VISIBLE);
        }
    }

    private Dialog createAddWifiNetworkDialog() {
        final Dialog dialog = super.createEditTextDialog(R.layout.frame_add_wifi_network_dialog);
        final View addWifiView = mView;

        mSelectSecuritySpinner = addWifiView.findViewById(R.id.credential_ssid_security_spinner);
        SecurityAdapter mAdapter = new SecurityAdapter();
        mSelectSecuritySpinner.setAdapter(mAdapter);

        mPasswordHeaderTextView = addWifiView.findViewById(R.id.addwifi_password_header_text);

        mSsidEditText = addWifiView.findViewById(R.id.addwifi_ssid_edittext);
        if (mDefaultSsid != null && !mDefaultSsid.isEmpty()) {
            mSsidEditText.setText(mDefaultSsid);
            mSsidEditText.setEnabled(false);
            addWifiView.findViewById(R.id.addwifi_ssid_security_header_text).setVisibility(View.GONE);

            mHint = getString(R.string.password_hint);
            updateUI();
            mPasswordHeaderTextView.setVisibility(View.VISIBLE);
            mEditText.setVisibility(View.VISIBLE);
        }

        mSelectSecuritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDropdownList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mSelectSecuritySpinner.setOnTouchListener((v, event) -> false);

        Bundle args = getArguments();
        int passwordLengthMin = args == null ? 0 : args.getInt(KEY_PASSWORD_LENGTH);
        mSsidEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                AuthType selectedSecurityItem = getAuthType();
                setButtonProperty(mPositiveButton, ((mSsidEditText.getText().length() > 0)
                        && ((mEditText.getText().length() >= passwordLengthMin)
                        || (AuthType.OPEN.equals(selectedSecurityItem)))));
            }
        });

        // set content descriptions
        mEditText.setContentDescription(getString(R.string.cont_desc_enter_password));
        mSsidEditText.setContentDescription(getString(R.string.cont_desc_wifi_ssid));
        mSelectSecuritySpinner.setContentDescription(getString(R.string.cont_desc_select_security));

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
                setButtonProperty(mPositiveButton,
                                  (mSsidEditText.getText().length() >= 1) && (mEditText.getText().length() >= 2));
            }
        });
    }

    private class SecurityAdapter extends BaseAdapter {

        private AuthType[] mSecurityList = {AuthType.OPEN, AuthType.SECURE, AuthType.WPS};

        @Override
        public int getCount() {
            return mSecurityList.length;
        }

        @Override
        public AuthType getItem(int position) {
            return mSecurityList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final AuthType security = mSecurityList[position];

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.list_item_security, parent, false);
            }

            TextView textView = convertView.findViewById(R.id.security_type);
            if (textView != null) {
                textView.setText(getSecurityLabel(security));
            }

            return convertView;
        }

        private int getSecurityLabel(AuthType security) {
            switch (security) {
                case SECURE:
                    return R.string.security_password;
                case WPS:
                    return R.string.security_wps;
                case OPEN:
                default:
                    return R.string.security_open;
            }
        }

    }
}
