package com.matrixfrats.android_apprtc.testing.stage2;

import android.content.Context;

import org.webrtc.AudioTrack;
import org.webrtc.VideoTrack;

import androidx.annotation.NonNull;

public class Call implements SignallingChannel.MessageCallback {

    private String roomId;
    private boolean isInitiator;
    private Context context;
    private SignallingChannel signallingChannel;
    private PeerConnectionClient peerConnectionClient;

    private CallListeners callListeners;

    public interface CallListeners {
        void onUserMediaSuccess();
        void onUserMediaFailed(String err);
        void showSharingBox(String link);
    }

    public void registerCallListener(@NonNull CallListeners callListeners) {
        this.callListeners = callListeners;
    }

    public void unregisterCallListener() {
        this.callListeners = null;
    }

    public Call(@NonNull Context context) {
        this.peerConnectionClient = null;
        this.context = context;
    }

    public void initiateCall(@NonNull String id, @NonNull boolean isInitiator) {
        this.roomId = id;
        this.isInitiator = isInitiator;
        this.signallingChannel = new SignallingChannel(id, isInitiator);
        this.connectToRoom();
    }

    private void connectToRoom() {
        this.signallingChannel.initiate(new SignallingChannel.InitiateCallback() {
            @Override
            public void onSuccess() {
                createPcClient();
                if (maybeGetMedia()) {
                    startSignalling();
                } else {
                    // TODO handle when media fails
                }
            }

            @Override
            public void onFailed() {
                // TODO when room is full
            }
        });
    }

    private void createPcClient() {
        if (this.peerConnectionClient != null) {
            return;
        }
        this.peerConnectionClient = new PeerConnectionClient(this.context);
        this.signallingChannel.setOnMessageListener(this);
    }

    private boolean maybeGetMedia() {
        if (this.peerConnectionClient.getUserMedia()) {
            if (this.callListeners != null) this.callListeners.onUserMediaSuccess();
            return true;
        }

        if (this.callListeners != null) this.callListeners.onUserMediaFailed("No camera found");
        return false;
    }

    private void startSignalling() {
        if (this.isInitiator && this.callListeners != null)
            this.callListeners.showSharingBox(Constants.ROOM_SERVER + this.roomId);


    }

    public VideoTrack getVideoTrack() {
        if (this.peerConnectionClient == null) return null;
        return peerConnectionClient.getLocalVideoTrack();
    }

    public AudioTrack getAudioTrack() {
        if (this.peerConnectionClient == null) return null;
        return peerConnectionClient.getLocalAudioTrack();
    }

    @Override
    public void onMessage(MsgClass msgClass) {
        if (this.peerConnectionClient == null) return;
        this.peerConnectionClient.onMessage(msgClass);
    }
}
