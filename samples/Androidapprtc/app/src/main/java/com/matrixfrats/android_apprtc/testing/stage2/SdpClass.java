package com.matrixfrats.android_apprtc.testing.stage2;

import com.google.firebase.database.IgnoreExtraProperties;

import androidx.annotation.NonNull;

@IgnoreExtraProperties
public class SdpClass {

    private String type;
    private String sdp;

    private SdpClass() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public SdpClass(@NonNull String type, @NonNull String sdp) {
        this.type = type;
        this.sdp = sdp;
    }

    public String getType() {
        return type;
    }

    public String getSdp() {
        return sdp;
    }
}
