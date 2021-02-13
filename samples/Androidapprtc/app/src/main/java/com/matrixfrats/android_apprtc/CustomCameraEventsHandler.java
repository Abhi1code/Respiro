package com.matrixfrats.android_apprtc;

import android.util.Log;

import org.webrtc.CameraVideoCapturer;

public class CustomCameraEventsHandler implements CameraVideoCapturer.CameraEventsHandler {

    private String logTag = "ABHI";


    @Override
    public void onCameraError(String s) {
        Log.d(logTag, "onCameraError() called with: s = [" + s + "]");
    }

    @Override
    public void onCameraFreezed(String s) {
        Log.d(logTag, "onCameraFreezed() called with: s = [" + s + "]");
    }

    @Override
    public void onCameraOpening(String i) {
        Log.d(logTag, "onCameraOpening() called with: i = [" + i + "]");
    }

    @Override
    public void onFirstFrameAvailable() {
        Log.d(logTag, "onFirstFrameAvailable() called");
    }

    @Override
    public void onCameraClosed() {
        Log.d(logTag, "onCameraClosed() called");
    }

    @Override
    public void onCameraDisconnected() {

    }
}
