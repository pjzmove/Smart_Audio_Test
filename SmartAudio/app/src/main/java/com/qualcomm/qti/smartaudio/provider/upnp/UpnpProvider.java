/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.upnp;

import android.content.Context;
import android.net.Uri;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.model.ContentGroup;
import com.qualcomm.qti.smartaudio.model.ContentList;
import com.qualcomm.qti.smartaudio.model.upnp.UpnpContentList;
import com.qualcomm.qti.smartaudio.model.upnp.UpnpServerContentList;
import com.qualcomm.qti.smartaudio.provider.ContentProviderException;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.model.DataList;
import com.qualcomm.qti.smartaudio.model.MediaContentItem;
import com.qualcomm.qti.smartaudio.provider.Provider;
import com.qualcomm.qti.smartaudio.service.UpnpService;
import com.qualcomm.qti.smartaudio.util.Utils;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;

import java.net.URI;
import java.util.Collection;

public class UpnpProvider implements Provider {
	private static final String TAG = UpnpProvider.class.getSimpleName();

	private static UpnpProvider sInstance = null;

	private AndroidUpnpService mAndroidUpnpService = null;

	public static final String UPNP_CONTENT_DIRECTORY_SCHEME = "upnp://";
	private static final String UPNP_HTTP = "http://";
	public static final String UPNP_CONTENT_DIRECTORY = "directory";
	public static final String UPNP_LIST = "list";
	public static final String UPNP_DIR_ID = "dirId";
	public static final String UPNP_BROWSE = "browse";
	public static final String UPNP_UDN = "udn";
	public static final String UPNP_CONTENT_DIRECTORY_PATH = UPNP_CONTENT_DIRECTORY_SCHEME + UPNP_CONTENT_DIRECTORY +
			"/" + UPNP_LIST;
	public static final String UPNP_BROWSE_CONTENT_DIRECTORY_PATH = UPNP_CONTENT_DIRECTORY_SCHEME + UPNP_CONTENT_DIRECTORY +
			"/" + UPNP_BROWSE + "/";

	public static UpnpProvider getInstance() {
		UpnpProvider upnpProvider = sInstance;
		synchronized (UpnpProvider.class) {
			if (upnpProvider == null) {
				sInstance = upnpProvider = new UpnpProvider();
			}
		}
		return upnpProvider;
	}

	public synchronized void setUpnpService(final AndroidUpnpService upnpService) {
		mAndroidUpnpService = upnpService;
	}

	public synchronized AndroidUpnpService getUpnpService() {
		return mAndroidUpnpService;
	}

	@Override
	public ContentList getContent(Context context, ContentSearchRequest contentSearchRequest,
								  ContentList appendContentList) throws ContentProviderException {
		final AndroidUpnpService androidUpnpService = getUpnpService();
		if (androidUpnpService == null) {
			return null;
		}

		String query = contentSearchRequest.getQuery();
		Uri queryUri = Uri.parse(query);
		String lastPathSegment = queryUri.getLastPathSegment();

		if (appendContentList == null) {
			if (lastPathSegment.equals(UPNP_LIST)) {
				appendContentList = new UpnpServerContentList();
			} else {
				appendContentList = new UpnpContentList();
				appendContentList.setEmptyString(context.getString(R.string.no_music_in_folder));
			}
		}

		final DataList dataList = appendContentList.getDataList();

		if (lastPathSegment.equals(UPNP_LIST)) {
			Collection<Device> devices = androidUpnpService.getRegistry()
					.getDevices(new UDAServiceType(UpnpService.CONTENT_DIRECTORY));
			for (Device device : devices) {
				ContentGroup contentGroup = new ContentGroup();
				final String title = (device.getDetails() != null) ? device.getDetails().getFriendlyName() :
						device.getDisplayString();
				contentGroup.setTitle(title);
				if (device.getIcons().length > 0) {
					Icon icon = device.getIcons()[0];
					URI iconUri = icon.getUri();
					if (iconUri != null) {
						if (iconUri.getHost() != null) {
							contentGroup.setThumbnailUrl(iconUri.toString());
						} else if (device.getIdentity() instanceof RemoteDeviceIdentity) {
							RemoteDeviceIdentity remoteDeviceIdentity = (RemoteDeviceIdentity)device.getIdentity();
							contentGroup.setThumbnailUrl(UPNP_HTTP + remoteDeviceIdentity.getDescriptorURL().getHost() +
									((remoteDeviceIdentity.getDescriptorURL().getPort() > 0) ?
									(":" + remoteDeviceIdentity.getDescriptorURL().getPort()) : "") + "/" +
									iconUri.toString());
						}
					}
				}

				UpnpSearchRequest upnpSearchRequest = new UpnpSearchRequest();
				upnpSearchRequest.setQuery(UPNP_BROWSE_CONTENT_DIRECTORY_PATH + "?" +
						UPNP_UDN + "=" + device.getIdentity().getUdn().getIdentifierString() + "&" +
						UPNP_DIR_ID + "=0");

				contentGroup.setRequest(upnpSearchRequest);
				dataList.addItem(contentGroup);
			}
		} else {
			String udn = queryUri.getQueryParameter(UPNP_UDN);
			String dirId = queryUri.getQueryParameter(UPNP_DIR_ID);
			final Device device = androidUpnpService.getRegistry().getDevice(new UDN(udn), true);
			if (device != null) {
				final Object waitObject = new Object();
				androidUpnpService.getControlPoint().execute(
						new Browse(device.findService(new UDAServiceType("ContentDirectory")),
								dirId, BrowseFlag.DIRECT_CHILDREN) {
					@Override
					public void received(ActionInvocation actionInvocation, DIDLContent didl) {
						for (Container container : didl.getContainers()) {
							ContentGroup contentGroup = new ContentGroup();
							contentGroup.setTitle(container.getTitle());

							UpnpSearchRequest upnpSearchRequest = new UpnpSearchRequest();
							upnpSearchRequest.setQuery(UPNP_BROWSE_CONTENT_DIRECTORY_PATH + "?" +
									UPNP_UDN + "=" + device.getIdentity().getUdn().getIdentifierString() + "&" +
									UPNP_DIR_ID + "=" + container.getId());

							contentGroup.setRequest(upnpSearchRequest);

							dataList.addItem(contentGroup);
						}

						for (Item item : didl.getItems()) {
							if (item instanceof MusicTrack) {
								MusicTrack musicTrack = (MusicTrack) item;
								MediaContentItem mediaContentItem = new MediaContentItem();
								mediaContentItem.setTitle(musicTrack.getTitle());
								String artist = null;
								if (musicTrack.getFirstArtist() != null) {
									artist = musicTrack.getFirstArtist().getName();
									mediaContentItem.setArtist(artist);
								}
								String album = musicTrack.getAlbum();
								mediaContentItem.setAlbum(album);

								String subtitle = artist;
								if (!Utils.isStringEmpty(subtitle) && !Utils.isStringEmpty(album)) {
									subtitle += " - ";
								}
								subtitle += album;
								mediaContentItem.setSubtitle(subtitle);

								for (Res res : item.getResources()) {
									if (res.getValue() != null) {
										mediaContentItem.setStreamUrl(res.getValue());
										long duration = ModelUtil.fromTimeString(res.getDuration()) * 1000;
                    mediaContentItem.setDuration((int)duration);
									}
								}

								dataList.addItem(mediaContentItem);
							}
						}

						synchronized (waitObject) {
							waitObject.notify();
						}
					}

					@Override
					public void updateStatus(Status status) {}

					@Override
					public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
						synchronized (waitObject) {
							waitObject.notify();
						}
					}
				});
				synchronized (waitObject) {
					try {
						waitObject.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return appendContentList;
	}

	@Override
	public ContentList searchContent(Context context, ContentSearchRequest contentSearchRequest,
									 String searchText) throws ContentProviderException {
		return null;
	}
}
