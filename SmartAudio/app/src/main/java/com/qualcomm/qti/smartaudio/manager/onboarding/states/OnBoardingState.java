/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.states;

import com.qualcomm.qti.smartaudio.manager.onboarding.requests.RequestType;

public enum OnBoardingState {
    SENDING_CREDENTIALS,
    CONNECTING,
    CHECK_CONNECTION_STATUS,
    JOIN,
    DISCONNECTED;

    public String getRequestCommand() {
        switch (this) {
            case SENDING_CREDENTIALS:
                return RequestType.VERIFY_CONNECT.getCommand();
            case CONNECTING:
                return RequestType.VERIFY_CONNECTING.getCommand();
            case CHECK_CONNECTION_STATUS:
                return RequestType.CHECK_CONNECT_STATUS.getCommand();
            case JOIN:
                return RequestType.JOIN.getCommand();
            case DISCONNECTED:
                return "{no request, is wifi disconnection}";
            default:
                return "{no corresponding request}";
        }
    }
}
