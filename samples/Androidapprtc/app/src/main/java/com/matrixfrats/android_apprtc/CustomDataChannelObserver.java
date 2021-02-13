package com.matrixfrats.android_apprtc;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.PeerConnection;

public class CustomDataChannelObserver  implements DataChannel.Observer {

    private String logTag = "ABHI";

    @Override
    public void onBufferedAmountChange(long l) {
        Log.d(logTag, "onBufferedAmountChange() called with: s = [" + l + "]");
    }

    @Override
    public void onStateChange() {
        Log.d(logTag, "onStateChange() called");
    }

    @Override
    public void onMessage(DataChannel.Buffer buffer) {
        Log.d(logTag, "onMessage() called");
    }
}
