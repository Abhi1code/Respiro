package com.matrixfrats.android_apprtc.testing.stage2;

import android.content.Context;

import com.matrixfrats.android_apprtc.CustomCameraEventsHandler;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

public class PeerConnectionClient {

    private Context context;

    private PeerConnectionFactory peerConnectionFactory;
    private VideoCapturer videoCapturer;
    private VideoSource videoSource;
    private VideoTrack localVideoTrack;

    private AudioSource audioSource;
    private AudioTrack localAudioTrack;

    public PeerConnectionClient(Context context) {
        this.context = context;
        this.initiateFactory();
    }

    private void initiateFactory() {
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(this.context)
                        .setEnableVideoHwAcceleration(true)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer = this.createCameraCapturer(new Camera1Enumerator(false));
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // Trying to find a front facing camera!
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, new CustomCameraEventsHandler());
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // We were not able to find a front cam. Look for other cameras
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName,  new CustomCameraEventsHandler());
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    public boolean getUserMedia() {
        videoCapturer = this.createVideoCapturer();
        if (videoCapturer != null) {
            videoSource = peerConnectionFactory.createVideoSource(videoCapturer);
            localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
            videoCapturer.startCapture(1024, 720, 30);

            audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
            localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);

            return true;
        }
        return false;
    }

    public VideoTrack getLocalVideoTrack() {
        return this.localVideoTrack;
    }

    public AudioTrack getLocalAudioTrack() {
        return this.localAudioTrack;
    }

    public void onMessage(MsgClass msgClass) {

    }
}
