package com.matrixfrats.android_apprtc.testing.stage2;

import com.google.firebase.database.IgnoreExtraProperties;

import androidx.annotation.NonNull;

@IgnoreExtraProperties
public class IceClass {

    private String candidate;
    private String sdpMid;
    private int sdpMLineIndex;

    private IceClass() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public IceClass(@NonNull String candidate, @NonNull String sdpMid, @NonNull int sdpMLineIndex) {
        this.candidate = candidate;
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
    }

    public String getCandidate() {
        return candidate;
    }

    public String getSdpMid() {
        return sdpMid;
    }

    public int getSdpMLineIndex() {
        return sdpMLineIndex;
    }
}
