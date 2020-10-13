/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.requests;

import android.support.annotation.Nullable;
import android.util.Log;

import com.qualcomm.qti.smartaudio.manager.onboarding.models.Capabilities;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.RequestResult;
import com.qualcomm.qti.smartaudio.model.WifiNetwork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NetworksListRequest extends HttpRequest {

    private static final String TAG = "NetworksListRequest";

    private static final int TIME_OUT_MS = 15000;
    private static final RequestType REQUEST_TYPE = RequestType.REFRESH_SCAN_LIST;

    private class JSONKeys {
        private static final String LIST_KEY = "scanlist";

        private class ScanItem {
            private static final String SSID_KEY = "ssid";
            private static final String BSSID_KEY = "bssid";
            private static final String SIGNAL_KEY = "signal";
            private static final String ENCRYPTION_KEY = "encryption";
        }
    }

    NetworksListRequest(String url, @Nullable RequestListener listener) {
        super(REQUEST_TYPE, url, Method.GET, listener, TIME_OUT_MS);
    }

    @Override
    public void onResponse(String response) {
        JSONObject jsonObject;
        try {
            jsonObject = parseJSON(response);
        }
        catch (Exception exception) {
            Log.w(TAG,"[onResponse] Exception occurred when parsing response.");
            exception.printStackTrace();
            onError(RequestResult.READING_RESPONSE_FAILED);
            return;
        }

        List<WifiNetwork> networks;

        try {
            networks = processResponse(jsonObject);
        }
        catch (Exception exception) {
            Log.w(TAG, "[onResponse] Exception occurred when reading JSON.");
            exception.printStackTrace();
            onError(RequestResult.READING_RESPONSE_FAILED);
            return;
        }

        onComplete(networks);
    }



    private JSONObject parseJSON(String response) throws Exception {
        return new JSONObject(response);
    }

    private static List<WifiNetwork> processResponse(JSONObject response) throws Exception {
        JSONArray array = getJSONNetworkList(response);
        List<WifiNetwork> resultList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            WifiNetwork network = parseWifiNetwork(item);
            resultList.add(network);
        }
        return resultList;
    }

    private static JSONArray getJSONNetworkList(JSONObject response) throws Exception {
        Object list = response.get(JSONKeys.LIST_KEY);
        if (list instanceof JSONArray) {
            return (JSONArray) list;
        }
        else if (list instanceof String && !((String) list).isEmpty()) {
            // response.get(JSONKeys.LIST_KEY) returns an empty String when there is no list.
            return new JSONArray((String) list);
        }
        else {
            // unreadable
            return new JSONArray();
        }
    }

    /**
     * <p>To build a new instance of wifiNetwork from a JSON Object.</p>
     *
     * @param item contains the information about the network.
     *
     * @return The WifiNetwork object built from the content of the given JSONObject.
     *
     * @throws JSONException throws an exception if an error occurs when getting information from the given
     * JSONObject item.
     */
    private static WifiNetwork parseWifiNetwork(JSONObject item) throws JSONException {
        // getting name
        String ssid = item.has(JSONKeys.ScanItem.SSID_KEY) ? item.getString(JSONKeys.ScanItem.SSID_KEY) :
                item.has(JSONKeys.ScanItem.BSSID_KEY) ? item.getString(JSONKeys.ScanItem.BSSID_KEY) : "";

        // getting the level
        int level = item.has(JSONKeys.ScanItem.SIGNAL_KEY) ? item.getInt(JSONKeys.ScanItem.SIGNAL_KEY) : 0;

        // getting the security type
        JSONObject capabilities = item.has(JSONKeys.ScanItem.ENCRYPTION_KEY) ?
                item.getJSONObject(JSONKeys.ScanItem.ENCRYPTION_KEY) : null;
        boolean isOpen = checkIsOpen(capabilities);

        return new WifiNetwork(ssid, level, isOpen);
    }

    /**
     */
    private static boolean checkIsOpen(JSONObject capabilities) {
        if (capabilities == null) {
            return true;
        }

        return !capabilities.has(Capabilities.WEP.toLowerCase())
                && !capabilities.has(Capabilities.WPA.toLowerCase());
    }

}
