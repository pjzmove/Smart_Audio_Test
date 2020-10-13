/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.model.ContentGroup;
import com.qualcomm.qti.smartaudio.model.ContentItem;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.input.InputSearchRequest;
import com.qualcomm.qti.smartaudio.provider.local.LocalProvider;
import com.qualcomm.qti.smartaudio.provider.local.LocalSearchRequest;
import com.qualcomm.qti.smartaudio.provider.online.OnlineSearchRequest;
import com.qualcomm.qti.smartaudio.provider.playlist.PlaylistProvider;
import com.qualcomm.qti.smartaudio.provider.playlist.PlaylistSearchRequest;
import com.qualcomm.qti.smartaudio.provider.upnp.UpnpProvider;
import com.qualcomm.qti.smartaudio.provider.upnp.UpnpSearchRequest;
import com.qualcomm.qti.smartaudio.service.UpnpService;

import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.UDAServiceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BrowseMainFragment extends BrowseFragment {

    public static final String TAG = "BrowseMainFragment";

    private BrowseMainAdapter mAdapter = null;

    public static BrowseMainFragment newInstance() {
        return new BrowseMainFragment();
    }

    public BrowseMainFragment() {
        mFragmentID = R.layout.fragment_browse_main;
    }

    @Override
    protected BrowseAdapter getBrowseAdapter() {
        mAdapter = new BrowseMainAdapter();
        return mAdapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateInUiThread();
    }

    @Override
    public boolean isSearchable() {
        return true;
    }


    @Override
    protected void updateState() {
        mAdapter.updateItems(createBrowseMainCategories());
    }

    @Override
    public BrowseFragmentType getBrowseFragmentType() {
        return BrowseFragmentType.MAIN;
    }

    private enum BrowseMainCategoryType {
        ARTISTS,
        ALBUMS,
        GENRES,
        PLAYLISTS,
        SONGS,
        SECTION,
        INPUTS,
        //OUTPUTS,
        UPNP,
        ONLINE
    }

    @SuppressLint("ParcelCreator")
    class BrowseMainCategoryItem extends ContentItem {

        private BrowseMainCategoryType mBrowseMainCategoryType;
        private String mTitle;


        public BrowseMainCategoryItem(final BrowseMainCategoryType type, final String title) {
            setBrowseMainCategoryType(type);
            setTitle(title);
        }

        @Override
        public String getTitle() {
            return mTitle;
        }

        @Override
        public void setTitle(String title) {
            mTitle = title;
        }

        @Override
        public String getSubtitle() {
            return null;
        }

        @Override
        public void setSubtitle(String subtitle) {
        }

        @Override
        public String getThumbnailUrl() {
            return null;
        }

        @Override
        public void setThumbnailUrl(String thumbnailUrl) {
        }

        public BrowseMainCategoryType getBrowseMainCategoryType() {
            return mBrowseMainCategoryType;
        }

        public void setBrowseMainCategoryType(BrowseMainCategoryType type) {
            mBrowseMainCategoryType = type;
        }

        @Override
        public ContentType getContentType() {
            return null;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }
    }

    private List<ContentItem> createBrowseMainCategories() {

        List<ContentItem> browseMainCategoryItems = new ArrayList<>();

        BrowseMainCategoryItem sectionOnThisDevice = new BrowseMainCategoryItem(BrowseMainCategoryType.SECTION,
                                                                                getString(R.string.on_this_device));
        browseMainCategoryItems.add(sectionOnThisDevice);

        BrowseMainCategoryItem artistsCategory = new BrowseMainCategoryItem(BrowseMainCategoryType.ARTISTS,
                                                                            getString(R.string.artists));
        browseMainCategoryItems.add(artistsCategory);

        BrowseMainCategoryItem albumsCategory = new BrowseMainCategoryItem(BrowseMainCategoryType.ALBUMS,
                                                                           getString(R.string.albums));
        browseMainCategoryItems.add(albumsCategory);

        BrowseMainCategoryItem songsCategory = new BrowseMainCategoryItem(BrowseMainCategoryType.SONGS,
                                                                          getString(R.string.songs));
        browseMainCategoryItems.add(songsCategory);

        BrowseMainCategoryItem genresCategory = new BrowseMainCategoryItem(BrowseMainCategoryType.GENRES,
                                                                           getString(R.string.genres));
        browseMainCategoryItems.add(genresCategory);

        BrowseMainCategoryItem playlistsCategory = new BrowseMainCategoryItem(BrowseMainCategoryType.PLAYLISTS,
                                                                              getString(R.string.playlists));
        browseMainCategoryItems.add(playlistsCategory);


        UpnpProvider upnpProvider = UpnpProvider.getInstance();
        final AndroidUpnpService androidUpnpService = upnpProvider.getUpnpService();

        final IoTGroup currentZone = mAllPlayManager.getCurrentGroup();
        if ((currentZone != null && currentZone.isInputSelectorModeSupported()) || androidUpnpService != null) {
            BrowseMainCategoryItem sectionCategory = new BrowseMainCategoryItem(BrowseMainCategoryType.SECTION,
                                                                                getString(R.string.other_audio_sources));
            browseMainCategoryItems.add(sectionCategory);

            if (currentZone.isInputSelectorModeSupported()) {
                BrowseMainCategoryItem inputCategory = new BrowseMainCategoryItem(BrowseMainCategoryType.INPUTS,
                                                                                  getString(R.string.aux_line_in));
                browseMainCategoryItems.add(inputCategory);
            }

            if (androidUpnpService != null) {
                Collection<Device> devices = androidUpnpService.getRegistry()
                        .getDevices(new UDAServiceType(UpnpService.CONTENT_DIRECTORY));
                if (!devices.isEmpty()) {
                    BrowseMainCategoryItem upnpCategory = new BrowseMainCategoryItem(BrowseMainCategoryType.UPNP,
                                                                                     getString(R.string.local_media_server));
                    browseMainCategoryItems.add(upnpCategory);
                }
            }
        }

        BrowseMainCategoryItem onLineMusicCategory = new BrowseMainCategoryItem(BrowseMainCategoryType.ONLINE,
                                                                                getString(R.string.online_media_server));
        browseMainCategoryItems.add(onLineMusicCategory);

        return browseMainCategoryItems;
    }

    private class BrowseMainAdapter extends BrowseAdapter {

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final BrowseMainCategoryItem browseMainCategoryItem = (BrowseMainCategoryItem) getItem(position);
            String title = browseMainCategoryItem.getTitle();

            switch (browseMainCategoryItem.getBrowseMainCategoryType()) {
                case ARTISTS:
                case ALBUMS:
                case GENRES:
                case PLAYLISTS:
                case SONGS:
                case UPNP:
                case INPUTS:
                case ONLINE: {
                    if ((convertView == null) ||
                            !convertView.getTag().equals(browseMainCategoryItem.getBrowseMainCategoryType().toString())) {
                        if (browseMainCategoryItem.getBrowseMainCategoryType().equals(BrowseMainCategoryType.UPNP) ||
                                browseMainCategoryItem.getBrowseMainCategoryType().equals(BrowseMainCategoryType.INPUTS) ||
                                browseMainCategoryItem.getBrowseMainCategoryType().equals(BrowseMainCategoryType.ONLINE)) {
                            convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_browse_other,
                                                                                     viewGroup, false);
                        }
                        else {
                            convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_browse_main,
                                                                                     viewGroup, false);
                        }
                    }

                    TextView titleText = null;
                    if (!browseMainCategoryItem.getBrowseMainCategoryType().equals(BrowseMainCategoryType.UPNP) &&
                            !browseMainCategoryItem.getBrowseMainCategoryType().equals(BrowseMainCategoryType.INPUTS) &&
                            !browseMainCategoryItem.getBrowseMainCategoryType().equals(BrowseMainCategoryType.ONLINE)) {
                        ImageView imageView = (ImageView) convertView.findViewById(R.id.browse_main_category_icon);
                        int categoryDrawableID = -1;
                        switch (browseMainCategoryItem.getBrowseMainCategoryType()) {
                            case ARTISTS:
                                categoryDrawableID = R.drawable.ic_categories_artists;
                                break;
                            case ALBUMS:
                                categoryDrawableID = R.drawable.ic_categories_albums;
                                break;
                            case GENRES:
                                categoryDrawableID = R.drawable.ic_categories_genres;
                                break;
                            case PLAYLISTS:
                                categoryDrawableID = R.drawable.ic_categories_playlists;
                                break;
                            case SONGS:
                                categoryDrawableID = R.drawable.ic_categories_songs;
                                break;
                        }
                        imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), categoryDrawableID,
                                                                               null));

                        titleText = convertView.findViewById(R.id.browse_main_category_name);
                    }
                    else {
                        titleText = convertView.findViewById(R.id.browse_other_category_name);
                    }
                    titleText.setText(title);

                    convertView.setOnClickListener(view -> {
                        String query = null;
                        ContentSearchRequest contentSearchRequest = null;
                        switch (browseMainCategoryItem.getBrowseMainCategoryType()) {
                            case ARTISTS:
                                query = LocalProvider.ARTIST_PATH;
                                break;
                            case ALBUMS:
                                query = LocalProvider.ALBUM_PATH;
                                break;
                            case SONGS:
                                query = LocalProvider.SONG_PATH;
                                break;
                            case GENRES:
                                query = LocalProvider.GENRE_PATH;
                                break;
                            case PLAYLISTS:
                                query = PlaylistProvider.PLAYLIST_PATH;
                                contentSearchRequest = new PlaylistSearchRequest();
                                break;
                            case UPNP:
                                query = UpnpProvider.UPNP_CONTENT_DIRECTORY_PATH;
                                contentSearchRequest = new UpnpSearchRequest();
                                break;
                            case INPUTS:
                                contentSearchRequest = new InputSearchRequest();
                                break;
              /*case OUTPUTS:
                contentSearchRequest = new OutputSourceSearchRequest();
                break;*/
                            case ONLINE:
                                contentSearchRequest = new OnlineSearchRequest();
                                break;
                        }

                        ContentGroup contentGroup = new ContentGroup();
                        if (contentSearchRequest == null) {
                            contentSearchRequest = new LocalSearchRequest();
                        }
                        contentSearchRequest.setQuery(query);
                        contentGroup.setRequest(contentSearchRequest);
                        contentGroup.setTitle(title);
                        mOnBrowseControl.get().clickBrowseListGroup(contentGroup);
                    });

                    convertView.setContentDescription(getString(R.string.cont_desc_browse_category, title));
                    break;
                }
                case SECTION: {
                    if ((convertView == null) || !convertView.getTag().equals(title)) {
                        LayoutInflater inflater = (LayoutInflater) getActivity().
                                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = inflater.inflate(R.layout.list_item_browse_section, viewGroup, false);
                    }

                    TextView titleView = (TextView) convertView.findViewById(R.id.browse_section_title);
                    titleView.setText(title);
                    convertView.setContentDescription(getString(R.string.cont_desc_browse_section, title));
                }
                break;
            }

            convertView.setTag(browseMainCategoryItem.getBrowseMainCategoryType().toString());

            return convertView;
        }
    }
}
