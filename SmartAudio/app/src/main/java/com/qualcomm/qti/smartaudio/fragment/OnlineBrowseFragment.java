/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                     *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 *  ************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.model.ContentItem.ContentType;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;

import java.util.ArrayList;
import java.util.List;

public class OnlineBrowseFragment extends BrowseFragment implements AdapterView.OnItemClickListener {

    private class MusicService {

        public String name;
        public String packageName;
        public String icon;
    }

    private List<MusicService> mMusicServiceList = new ArrayList<>();
    private String mTitle;

    public static OnlineBrowseFragment newInstance(final String title,
												   final ContentSearchRequest contentSearchRequest) {
        OnlineBrowseFragment fragment = new OnlineBrowseFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, title);
        bundle.putSerializable(KEY_REQUEST, contentSearchRequest);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] musicServiceNames = getResources().getStringArray((R.array.music_services_array));
        String[] musicServiceIcons = getResources().getStringArray((R.array.music_service_icons_array));
        String[] musicServicePackages = getResources().getStringArray((R.array.music_service_packages_array));

        for (int i = 0; i < musicServiceNames.length; i++) {
            MusicService musicService = new MusicService();
            musicService.name = musicServiceNames[i];
            musicService.icon = musicServiceIcons[i];
            musicService.packageName = musicServicePackages[i];
            mMusicServiceList.add(musicService);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online_music_service, container, false);
        ListView listView = view.findViewById(R.id.music_services_list);
        listView.setAdapter(new MusicServiceAdapter());
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public BrowseFragmentType getBrowseFragmentType() {
        return BrowseFragmentType.ONLINE;
    }

    protected BrowseAdapter getBrowseAdapter() {
        if (mAdapter == null) {
            mAdapter = new MusicServiceAdapter();
        }
        return mAdapter;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MusicService musicService = mMusicServiceList.get(position);

        Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(musicService.packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + musicService.packageName)));
        }
    }

    private class MusicServiceAdapter extends BrowseAdapter {

        @Override
        public int getCount() {
            return mMusicServiceList.size();
        }

        @Override
        public Object getItem(int position) {
            return mMusicServiceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            if (convertView == null) {
                LayoutInflater inflater =
						(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_music_service, viewGroup, false);
            }

            MusicService musicService = (MusicService) getItem(position);

            ImageView imageView = convertView.findViewById(R.id.music_service_icon);
            TextView textView = convertView.findViewById(R.id.music_service_text);

            int iconResId = getResources().getIdentifier(musicService.icon, "drawable", getContext().getPackageName());
            if (iconResId != 0) {
                imageView.setImageResource(iconResId);
            }
            textView.setText(musicService.name);

            convertView.setTag(ContentType.ONLINE.toString());

            convertView.setContentDescription(getString(R.string.cont_desc_online_service, musicService.name));

            return convertView;
        }
    }


}
