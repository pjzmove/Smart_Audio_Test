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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.BuildConfig;
import com.qualcomm.qti.smartaudio.R;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AboutFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "AboutFragment";

    private OnAboutClickedListener mOnAboutClickedListener;

    @IntDef(value = {Items.LEGAL_NOTICES, Items.TERMS_OF_SERVICE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Items {
        int LEGAL_NOTICES = 0;
        int TERMS_OF_SERVICE = 1;
    }

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOnAboutClickedListener = (OnAboutClickedListener) context;
    }

    @Override
    public View onCreateView(@NotNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        ListView listView = view.findViewById(R.id.about_list);

        String[] aboutItems = getResources().getStringArray((R.array.about_items_array));

        listView.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.list_item_settings_child,
                                                     R.id.settings_child_text_view, aboutItems) {
            @NotNull
            @Override
            public View getView(int position, View convertView, @NotNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setContentDescription(getItemContentDescription(position));
                return view;
            }

        });


        listView.setOnItemClickListener(this);

        TextView listFooterTextView = (TextView) inflater.inflate(R.layout.list_footer, listView, false);
        listFooterTextView.setText(getString(R.string.about_info, getString(R.string.app_name),
                                             BuildConfig.VERSION_NAME, String.valueOf(BuildConfig.Revision),
                                             BuildConfig.iotivityVerion));
        listView.setFooterDividersEnabled(false);
        listView.addFooterView(listFooterTextView);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case Items.LEGAL_NOTICES:
                mOnAboutClickedListener.onLegalClicked();
                break;
            case Items.TERMS_OF_SERVICE:
                mOnAboutClickedListener.onToSClicked();
                break;
            default:
                break;
        }
    }

    private String getItemContentDescription(int position) {
        switch (position) {
            case Items.LEGAL_NOTICES:
                return getString(R.string.cont_desc_legal_notices);
            case Items.TERMS_OF_SERVICE:
                return getString(R.string.cont_desc_terms_of_service);
            default:
                return getString(R.string.cont_desc_unknown);
        }
    }

    public interface OnAboutClickedListener {

        void onLegalClicked();

        void onToSClicked();
    }
}
