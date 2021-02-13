package com.matrixfrats.android_apprtc.testing.stage1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.matrixfrats.android_apprtc.CustomCameraEventsHandler;
import com.matrixfrats.android_apprtc.CustomDataChannelObserver;
import com.matrixfrats.android_apprtc.CustomPeerConnectionObserver;
import com.matrixfrats.android_apprtc.CustomSdpObserver;
import com.matrixfrats.android_apprtc.R;
import com.matrixfrats.android_apprtc.VideoRoom;
import com.matrixfrats.android_apprtc.database.FireDatabase;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LocalVideoRoom extends AppCompatActivity {

    private SurfaceViewRenderer localVideo, remoteVideo;
    private Button join, close, lsend, rsend;
    private TextView lmsg, rmsg;
    private EditText linput, rinput;

    private EglBase rootEglBase;
    private PeerConnectionFactory peerConnectionFactory;
    private MediaConstraints sdpConstraints;
    private VideoCapturer videoCapturer;
    private VideoRenderer videoRenderer;
    private VideoSource videoSource;
    private VideoTrack localVideoTrack;
    private AudioSource audioSource;
    private AudioTrack localAudioTrack;
    private PeerConnection localpeerConnection, remotepeerConnection;
    private DataChannel localChannel, remoteChannel;

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private final int MY_PERMISSIONS_REQUEST = 102;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stage1_videoroom);
        localVideo = findViewById(R.id.local_camera);
        remoteVideo = findViewById(R.id.remote_video);
        join = findViewById(R.id.join);
        close = findViewById(R.id.close);
        lsend = findViewById(R.id.lsend);
        rsend = findViewById(R.id.rsend);
        lmsg = findViewById(R.id.lmsg);
        rmsg = findViewById(R.id.rmsg);
        linput = findViewById(R.id.linput);
        rinput = findViewById(R.id.rinput);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localpeerConnection.createOffer(new CustomSdpObserver("localCreateOffer"){
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        super.onCreateSuccess(sessionDescription);
                        localpeerConnection.setLocalDescription(new CustomSdpObserver("localSetLocalDesc"), sessionDescription);
                        remotepeerConnection.setRemoteDescription(new CustomSdpObserver("remoteSetRemoteDesc"), sessionDescription);
                        remotepeerConnection.createAnswer(new CustomSdpObserver("remoteCreateAnswer") {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {
                                super.onCreateSuccess(sessionDescription);
                                remotepeerConnection.setLocalDescription(new CustomSdpObserver("localSetRemoteDesc"), sessionDescription);
                                localpeerConnection.setRemoteDescription(new CustomSdpObserver("localSetRemoteDesc"), sessionDescription);
                            }
                        }, new MediaConstraints());
                    }

                    @Override
                    public void onCreateFailure(String s) {
                        super.onCreateFailure(s);
                    }
                }, new MediaConstraints());
            }
        });
        lsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (localChannel.state() == DataChannel.State.OPEN) {
                    ByteBuffer buffer = ByteBuffer.wrap(linput.getText().toString().trim().getBytes());
                    linput.setText("");
                    localChannel.send(new DataChannel.Buffer(buffer, false));
                }
            }
        });
        rsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (remoteChannel.state() == DataChannel.State.OPEN) {
                    ByteBuffer buffer = ByteBuffer.wrap(rinput.getText().toString().trim().getBytes());
                    rinput.setText("");
                    remoteChannel.send(new DataChannel.Buffer(buffer, false));
                }
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoCapturer != null) {
                    try {
                        videoCapturer.stopCapture();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    videoCapturer.dispose();
                    videoSource.dispose();
                    localVideoTrack.dispose();
                    localVideo.release();
                }
                if (audioSource != null) {
                    audioSource.dispose();
                    localAudioTrack.dispose();
                }
                remoteVideo.release();

                localpeerConnection.close();
                remotepeerConnection.close();
                rootEglBase.release();
            }
        });

        this.askForPermissions();
        this.initiateFactory();
        this.initVideos();
        this.createPeerConnection();
        this.createDataChannel();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        videoRenderer = new VideoRenderer(localVideo);
//        localVideoTrack.addRenderer(videoRenderer);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        localVideoTrack.removeRenderer(videoRenderer);
    }

    private void askForPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST);
        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    private void initVideos() {
        rootEglBase = EglBase.create();
        localVideo.init(rootEglBase.getEglBaseContext(), null);
        remoteVideo.init(rootEglBase.getEglBaseContext(), null);
        localVideo.setZOrderMediaOverlay(true);
        remoteVideo.setZOrderMediaOverlay(true);
        localVideo.setMirror(true);
        remoteVideo.setMirror(true);

        videoCapturer = this.createVideoCapturer();
        if (videoCapturer != null) {
            videoSource = peerConnectionFactory.createVideoSource(videoCapturer);
            localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
            videoCapturer.startCapture(1024, 720, 30);
            videoRenderer = new VideoRenderer(localVideo);
            localVideoTrack.addRenderer(videoRenderer);
//            VideoSink videoSink = videoFrame -> localVideo.onFrame(videoFrame);
//            localVideoTrack.addSink(videoSink);
        }

        audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
        this.setSpeakerphoneOn(false);
    }

    private void initiateFactory() {
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .setEnableVideoHwAcceleration(true)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();
    }

    private void createPeerConnection() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun2.1.google.com:19302").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("turn:35.225.250.3:3478").setUsername("Abhi")
                .setPassword("Abhi").createIceServer());

        sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveVideo", "true"));

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
//        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
//        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
//        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
//        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
//        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;

        localpeerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver("localPeerCreation") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                remotepeerConnection.addIceCandidate(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
            }
        });

        remotepeerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver("localPeerCreation") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                localpeerConnection.addIceCandidate(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                VideoTrack videoTrack = mediaStream.videoTracks.get(0);
                VideoSink videoSink = videoFrame -> remoteVideo.onFrame(videoFrame);
                videoTrack.addSink(videoSink);
                //mediaStream.audioTracks.get(0).setEnabled(false);
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                super.onDataChannel(dataChannel);
                remoteChannel = dataChannel;
                remoteChannel.registerObserver(new CustomDataChannelObserver(){
                    @Override
                    public void onMessage(DataChannel.Buffer buffer) {
                        super.onMessage(buffer);
                        ByteBuffer data = buffer.data;
                        byte[] bytes = new byte[data.remaining()];
                        data.get(bytes);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                rmsg.setText(new String(bytes));
                            }
                        });
                    }

                    @Override
                    public void onStateChange() {
                        super.onStateChange();
                        if (remoteChannel.state() == DataChannel.State.OPEN) {}
                        if (remoteChannel.state() == DataChannel.State.CLOSED) {}
                    }
                });
            }
        });

        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
        stream.addTrack(localAudioTrack);
        stream.addTrack(localVideoTrack);
        localpeerConnection.addStream(stream);
    }

    private void createDataChannel() {
        DataChannel.Init dcInit = new DataChannel.Init();
        localChannel = localpeerConnection.createDataChannel("105", dcInit);
        localChannel.registerObserver(new CustomDataChannelObserver(){
            @Override
            public void onMessage(DataChannel.Buffer buffer) {
                super.onMessage(buffer);
                ByteBuffer data = buffer.data;
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lmsg.setText(new String(bytes));
                    }
                });
            }

            @Override
            public void onStateChange() {
                super.onStateChange();
                if (localChannel.state() == DataChannel.State.OPEN) {}
                if (localChannel.state() == DataChannel.State.CLOSED) {}
            }
        });
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

    private void setSpeakerphoneOn(boolean on) {
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(on);
    }
}
