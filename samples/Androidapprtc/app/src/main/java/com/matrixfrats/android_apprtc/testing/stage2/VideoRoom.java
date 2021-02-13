package com.matrixfrats.android_apprtc.testing.stage2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.matrixfrats.android_apprtc.R;

import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSink;
import org.webrtc.VideoTrack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class VideoRoom extends Fragment implements View.OnClickListener {

    private SurfaceViewRenderer localVideo, remoteVideo, miniVideo;
    private EglBase rootEglBase;
    private ConstraintLayout sharingBox;
    private FloatingActionButton muteAudio, muteVideo, chaboxToggle, hangup;
    private TextView roomLink;
    private boolean isAudioMuted, isVideoMuted;
    private VideoTrack localVideoTrack, remoteVideoTrack;
    private VideoSink localVideoSink, remoteVideoSink;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stage2_videoroom, container, false);
        localVideo = view.findViewById(R.id.localVideo);
        remoteVideo = view.findViewById(R.id.remoteVideo);
        miniVideo = view.findViewById(R.id.miniVideo);
        sharingBox = view.findViewById(R.id.sharingbox);
        roomLink = view.findViewById(R.id.roomlink);
        muteAudio = view.findViewById(R.id.muteaudio);
        muteVideo = view.findViewById(R.id.mutevideo);
        chaboxToggle = view.findViewById(R.id.chatboxToggle);
        hangup = view.findViewById(R.id.hangup);
        isAudioMuted = false;
        isVideoMuted = false;
        this.setupUi();
        this.initSurfaceRenderer();
        return view;
    }

    private void initSurfaceRenderer() {
        rootEglBase = EglBase.create();
        localVideo.init(rootEglBase.getEglBaseContext(), null);
        remoteVideo.init(rootEglBase.getEglBaseContext(), null);
        miniVideo.init(rootEglBase.getEglBaseContext(), null);
        localVideo.setZOrderMediaOverlay(true);
        remoteVideo.setZOrderMediaOverlay(true);
        miniVideo.setZOrderMediaOverlay(true);
        localVideo.setMirror(true);
        remoteVideo.setMirror(true);
        miniVideo.setMirror(true);
    }

    private void setupUi() {
        localVideo.setVisibility(View.GONE);
        remoteVideo.setVisibility(View.GONE);
        miniVideo.setVisibility(View.GONE);

        sharingBox.setVisibility(View.GONE);
        roomLink.setText("");

        muteVideo.setVisibility(View.GONE);
        muteAudio.setVisibility(View.GONE);
        chaboxToggle.setVisibility(View.GONE);
        hangup.setVisibility(View.GONE);

        muteAudio.setOnClickListener(this);
        muteVideo.setOnClickListener(this);
        chaboxToggle.setOnClickListener(this);
        hangup.setOnClickListener(this);
    }

    public void setUiForLocalVideo(VideoTrack videoTrack) {
        this.localVideoTrack = videoTrack;
        this.localVideoSink = videoFrame -> localVideo.onFrame(videoFrame);
        this.localVideoTrack.addSink(this.localVideoSink);
        this.localVideo.setVisibility(View.VISIBLE);
        this.muteAudio.setVisibility(View.VISIBLE);
        this.muteVideo.setVisibility(View.VISIBLE);
    }

    public void setUiForRemoteVideo(VideoTrack videoTrack) {
        this.remoteVideoTrack = videoTrack;
        this.remoteVideoSink = videoFrame -> remoteVideo.onFrame(videoFrame);
        this.remoteVideoTrack.addSink(this.remoteVideoSink);
        this.localVideoTrack.removeSink(this.localVideoSink);
        this.localVideoSink = videoFrame -> miniVideo.onFrame(videoFrame);
        this.localVideoTrack.addSink(this.localVideoSink);
        this.remoteVideo.setVisibility(View.VISIBLE);
        this.localVideo.setVisibility(View.GONE);
        this.miniVideo.setVisibility(View.VISIBLE);
        this.hangup.setVisibility(View.VISIBLE);
        this.sharingBox.setVisibility(View.GONE);
    }

    public void showSharingBox(String link) {
        this.roomLink.setText(link);
        this.sharingBox.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.muteaudio:
                this.toggleIcon(view);
                break;
            case R.id.mutevideo:
                this.toggleIcon(view);
                break;
            case R.id.chatboxToggle:
                break;
            case R.id.hangup:
                break;
        }
    }

    private void toggleIcon(View v) {
        switch (v.getId()) {
            case R.id.muteaudio:
                if (isAudioMuted) {
                    isAudioMuted = false;
                    muteAudio.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_baseline_mic));
                } else {
                    isAudioMuted = true;
                    muteAudio.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_baseline_mic_off));
                }
                break;
            case R.id.mutevideo:
                if (isVideoMuted) {
                    isVideoMuted = false;
                    muteVideo.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_baseline_videocam));
                } else {
                    isVideoMuted = true;
                    muteVideo.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_baseline_videocam_off));
                }
                break;
        }
    }
}
