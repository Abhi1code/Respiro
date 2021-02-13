package com.matrixfrats.android_apprtc.testing.stage2;

import com.google.firebase.database.IgnoreExtraProperties;

import androidx.annotation.NonNull;

@IgnoreExtraProperties
public class HangupClass {

    private String type;
    private boolean status;

    private HangupClass() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public HangupClass(@NonNull String type, @NonNull boolean status) {
        this.type = type;
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public boolean isStatus() {
        return status;
    }

}
