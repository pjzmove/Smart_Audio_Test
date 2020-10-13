package com.qualcomm.qti.smartaudio.interfaces;

public interface BottomDialogResultListener {

    void onSortingBottomDialogResult(int bottomSheetId, int selectionId);
    void onZoneBottomDialogResult(int bottomSheetId, int selectionId, String playerId, String playerHostname);
}


