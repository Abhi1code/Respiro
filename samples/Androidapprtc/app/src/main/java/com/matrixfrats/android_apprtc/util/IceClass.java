package com.matrixfrats.android_apprtc.util;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class IceClass {

    public String candidate;
    public String sdpMid;
    public int sdpMLineIndex;

    public IceClass() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public IceClass(String candidate, String sdpMid, int sdpMLineIndex) {
        this.candidate = candidate;
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
    }

}
