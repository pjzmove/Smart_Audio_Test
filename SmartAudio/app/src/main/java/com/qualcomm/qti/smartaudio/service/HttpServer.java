/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.service;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import android.util.Log;
import com.qualcomm.qti.smartaudio.provider.local.LocalProvider;
import com.qualcomm.qti.smartaudio.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {
	private static final String TAG = HttpServer.class.getSimpleName();

	public static final int HTTP_SERVER_PORT = 9998;
	public static final String HTTP_ALBUM_ART_TYPE = "albumart";
	public static final String HTTP_AUDIO_TYPE = "audio";
	public static final String HTTP_TEST_AUDIO = "testaudio";
	public static final String TYPE_TEST_LS_LSRS = "test_LR_LSRS";
	public static final String TYPE_TEST_LS_LFE_LSRS= "test_LR_LFE_LSRS";

	private static final int SCHEDULE_STOP_TIME = 6000; //10 mins
	private static final String HTTP_SCHEME = "http";
	private static final String HTTP_ID = "id";
	private static final String HTTP_TYPE = "type";

	public static final String TEST_AUDIO_NAME = "TestAudio.mp3";
	public static final String TEST_LR_LSRS_NAME = "LR_LSRS_pinkNoise.flac";
	public static final String TEST_LR_LFE_LSRS_NAME = "LR_LFE_LSRS_pinkNoise.flac";
	private static final String MIME_TYPE_MPEG = "audio/mpeg";
	private static final String MIMETYPE_AUDIO_FLAC = "audio/flac";

	private static final String RANGE_HEADER = "range";
	private static final String BYTE_RANGE = "bytes=";
	private static final String ACCEPT_RANGE_HEADER = "Accept-Ranges";
	private static final String BYTES_UNIT = "bytes";
	private static final String CONTENT_TYPE_HEADER = "Content-Type";
	private static final String CONTENT_LENGTH_HEADER = "Content-Length";
	private static final String CONTENT_RANGE_HEADER = "Content-Range";

	private Integer mClientCount = new Integer(0);

	private HttpServerListener mHttpServerListener = null;
	private Context mContext;

	private ScheduledExecutorService mScheduleExecutorService = null;
	private ScheduledFuture<?> mScheduledFuture = null;

	public HttpServer(final Context context, final HttpServerListener httpServerListener) {
		super(HTTP_SERVER_PORT);
		mContext = context;
		mHttpServerListener = httpServerListener;
	}

	@Override
	public void start() throws IOException {
		super.start();
		if ((mScheduleExecutorService == null) || mScheduleExecutorService.isShutdown()) {
			mScheduleExecutorService = Executors.newSingleThreadScheduledExecutor();
		}
		startIdleTimer();
	}

	@Override
	public void start(final int timeout) throws IOException {
		super.start(timeout);
		if ((mScheduleExecutorService == null) || mScheduleExecutorService.isShutdown()) {
			mScheduleExecutorService = Executors.newSingleThreadScheduledExecutor();
		}
		startIdleTimer();
	}

	@Override
	public void stop() {
		stopIdleTimer();
		if ((mScheduleExecutorService != null) && !mScheduleExecutorService.isShutdown()) {
			mScheduleExecutorService.shutdownNow();
		}
		super.stop();
	}

	@Override
	protected ClientHandler createClientHandler(final Socket finalAccept, final InputStream inputStream) {
		stopIdleTimer();
		incrementClient();
		return new HttpClientHandler(inputStream, finalAccept);
	}

	@Override
	public Response serve(IHTTPSession session) {
		Map<String, List<String>> params = session.getParameters();
		List<String> types = params.get(HTTP_TYPE);
		List<String> ids = params.get(HTTP_ID);

		if ((types == null) || types.isEmpty() || Utils.isStringEmpty(types.get(0)) ||
				(ids == null) || ids.isEmpty() || Utils.isStringEmpty(ids.get(0))) {
			return respondNotFound(session.getUri());
		}

		int id = Integer.parseInt(ids.get(0));
		if (id < 0) {
			return respondNotFound(session.getUri());
		}

		String type = types.get(0);
		Uri baseUri = null;
		if (type.equals(HTTP_ALBUM_ART_TYPE)) {
			baseUri = LocalProvider.ALBUM_ART_URI;
		} else if (type.equals(HTTP_AUDIO_TYPE)) {
			baseUri = LocalProvider.SONG_URI;
		}

		Response response;
		try {
			String mimeType = null;
			InputStream inputStream = null;

			if (type.equals(HTTP_ALBUM_ART_TYPE) || (type.equals(HTTP_AUDIO_TYPE))) {
				Uri uri = ContentUris.withAppendedId(baseUri, id);

				ContentResolver contentResolver = mContext.getContentResolver();
				inputStream = contentResolver.openInputStream(uri);
				mimeType = mContext.getContentResolver().getType(uri);

				if (type.equals(HTTP_AUDIO_TYPE)) {
					Cursor c = contentResolver.query(uri, null, null, null, null);
					if (c == null) {
						response = respondNotFound(session.getUri());
					} else {
						c.moveToFirst();
						int sizeIndex = c.getColumnIndexOrThrow(OpenableColumns.SIZE);
						int sizeBytes = c.getInt(sizeIndex);

						long offset = 0;
						long endpoint = -1;

						Map<String, String> headers = session.getHeaders();
						String rangeHeader = headers.get(RANGE_HEADER);

						// was partial content requested?
						if ((rangeHeader != null) && rangeHeader.startsWith(BYTE_RANGE) && !rangeHeader.equals("bytes=0-")) {


							String byteSpec = rangeHeader.substring(BYTE_RANGE.length());
							Log.d(TAG,"partial content request:"+byteSpec);

							int sepPos = byteSpec.indexOf('-');
							// does the byte spec describe a range?
							if (sepPos != -1) {
								// determine offset
								if (sepPos > 0) {
									String s = byteSpec.substring(0, sepPos).trim();
									offset = Integer.parseInt(s);
								}
								// determine endpoint
								if (sepPos != (byteSpec.length() - 1)) {
									String s = byteSpec.substring(sepPos + 1).trim();
									endpoint = Integer.parseInt(s);
								} else {
									endpoint = sizeBytes - 1;
								}
							}
						}

						int contentLength = calculateLength(offset, endpoint, sizeBytes);
						if (contentLength < 0) {
							response = sendRequestRangeNotSatisfiable(offset);
						} else {
							if (offset > 0) {
								inputStream.skip(offset);
							}
							response = newFixedLengthResponse(Response.Status.OK, mimeType, inputStream, sizeBytes);
							response.addHeader(ACCEPT_RANGE_HEADER, BYTES_UNIT);
							response.addHeader(CONTENT_TYPE_HEADER, mimeType);
							response.addHeader(CONTENT_LENGTH_HEADER, "" + contentLength);

							if ((offset != 0) || ((endpoint != sizeBytes) && (endpoint != -1))) {
								//HTTP 206 Partial content
								response.setStatus(Response.Status.PARTIAL_CONTENT);
								response.addHeader(CONTENT_RANGE_HEADER, BYTES_UNIT + " " + offset + "-" +
										(offset + contentLength - 1) + "/" + (offset + contentLength));
							}
						}

						c.close();
					}
				} else {
					response = newChunkedResponse(Response.Status.OK, mimeType, inputStream);
				}
			} else {
				if (type.equals(HTTP_TEST_AUDIO)) {
					mimeType = MIME_TYPE_MPEG;
					inputStream = mContext.getAssets().open(TEST_AUDIO_NAME);
				} else if (type.equals(TYPE_TEST_LS_LSRS)) {
					mimeType = MIMETYPE_AUDIO_FLAC;
					inputStream = mContext.getAssets().open(TEST_LR_LSRS_NAME);
				} else if (type.equals(TYPE_TEST_LS_LFE_LSRS)) {
					mimeType = MIMETYPE_AUDIO_FLAC;
					inputStream = mContext.getAssets().open(TEST_LR_LFE_LSRS_NAME);
				}
				response = newChunkedResponse(Response.Status.OK, mimeType, inputStream);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			response = respondNotFound(session.getUri());
		}
		return response;
	}

	private int calculateLength(final long offset, final long endpoint, final int sizeBytes) {
		if (offset >= sizeBytes) {
			return -1;
		}

		//Check endpoint value
		if (endpoint > 0) {
			if (offset >= endpoint || (endpoint >= sizeBytes)) {
				return -1;
			}
		}

		return (endpoint == -1) ? sizeBytes : (int) ((endpoint - offset) + 1);
	}

	private Response respondNotFound(final String uri) {
		String message = "<html><body><h1>\n";
		message += "File " + ((Utils.isStringEmpty(uri)) ? "" : uri) + " not found\n";
		message += "</h1></body></html>\n";
		return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_HTML, message);
	}

	private Response sendRequestRangeNotSatisfiable(final long offset) {
		String message = "<html><body><h1>\n";
		message += "Requested Range not satisfiable: \n";
		message += Long.toString(offset) + "\n";
		message += "</h1></body></html>\n";
		return newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, MIME_HTML, message);
	}

	private void startIdleTimer() {
		synchronized (this) {
			if (getNumberOfClients() != 0) {
				return;
			}
			if (mScheduledFuture != null) {
				mScheduledFuture.cancel(true);
				mScheduledFuture = null;
			}
			if ((mScheduleExecutorService != null) && !mScheduleExecutorService.isShutdown()) {
				mScheduledFuture = mScheduleExecutorService.schedule(new StopServerRunnable(), SCHEDULE_STOP_TIME, TimeUnit.SECONDS);
			}
		}
	}

	private void stopIdleTimer() {
		synchronized (this) {
			if (mScheduledFuture != null) {
				mScheduledFuture.cancel(true);
				mScheduledFuture = null;
			}
		}
	}

	private void incrementClient() {
		synchronized (mClientCount) {
			mClientCount++;
		}
	}

	private void decrementClient() {
		synchronized (mClientCount) {
			mClientCount--;
		}
	}

	private int getNumberOfClients() {
		synchronized (mClientCount) {
			return mClientCount.intValue();
		}
	}

	public class HttpClientHandler extends ClientHandler {

		public HttpClientHandler(InputStream inputStream, Socket acceptSocket) {
			super(inputStream, acceptSocket);
		}

		@Override
		public void run() {
      super.run();
			decrementClient();
			startIdleTimer();
		}
	}

	private class StopServerRunnable implements Runnable {
		@Override
		public void run() {
			try {
				int clients = getNumberOfClients();
				if ((clients == 0) && (mHttpServerListener != null)) {
					mHttpServerListener.onIdleTimedOut();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public static String buildHttpUrl(final String ipAddress, final String type) {
		return new Uri.Builder().scheme(HTTP_SCHEME).encodedAuthority(ipAddress + ":" + HTTP_SERVER_PORT)
				.appendQueryParameter(HTTP_TYPE, type).build().toString();
	}

	public static String buildHttpUrl(final String ipAddress, final String type, final String id) {
		return new Uri.Builder().scheme(HTTP_SCHEME).encodedAuthority(ipAddress + ":" + HTTP_SERVER_PORT)
				.appendQueryParameter(HTTP_ID, id)
				.appendQueryParameter(HTTP_TYPE, type).build().toString();
	}

	public interface HttpServerListener {
		void onIdleTimedOut();
	}
}
