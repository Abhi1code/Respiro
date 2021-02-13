package com.matrixfrats.android_apprtc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.matrixfrats.android_apprtc.database.FireDatabase;
import com.matrixfrats.android_apprtc.util.DataPacks;
import com.matrixfrats.android_apprtc.util.Login_session;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
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

import java.util.ArrayList;
import java.util.List;

public class VideoRoom extends AppCompatActivity implements View.OnClickListener {

    private SurfaceViewRenderer remote_video;
    private Surface local_video;
    private Button call, hangup;
    private EditText name_to_call;
    private Login_session login_session;
    private static boolean conn_status = false;

    private VideoRenderer localRenderer;
    private VideoRenderer remoteRenderer;
    private PeerConnectionFactory peerConnectionFactory;
    private MediaConstraints audioConstraints, videoConstraints, sdpConstraints;
    private VideoSource videoSource;
    private VideoTrack localVideoTrack;
    private AudioSource audioSource;
    private AudioTrack localAudioTrack;
    private PeerConnection localPeer;
    private String conn_user = null;
    private EglBase rootEglBase;

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private final int MY_PERMISSIONS_REQUEST = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_room);
        local_video = findViewById(R.id.local_camera);
        remote_video = findViewById(R.id.remote_video);
        call = findViewById(R.id.call);
        hangup = findViewById(R.id.hangup);
        name_to_call = findViewById(R.id.name_to_connect);
        login_session = new Login_session(this);
        askForPermissions();
        initVideos();
        start();
        call.setOnClickListener(this);
        hangup.setOnClickListener(this);
    }

    public void askForPermissions() {
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
        local_video.init(rootEglBase.getEglBaseContext(), null);
        remote_video.init(rootEglBase.getEglBaseContext(), null);
        local_video.setZOrderMediaOverlay(true);
        remote_video.setZOrderMediaOverlay(true);
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        videoCapturer = createCameraCapturer(new Camera1Enumerator(false));
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

    public void start() {
        //hangup.setEnabled(false);
        //Initialize PeerConnectionFactory globals.
        //Params are context, initAudio,initVideo and videoCodecHwAcceleration
        //PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .setEnableVideoHwAcceleration(true)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        //Create a new PeerConnectionFactory instance.
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(),  /* enableIntelVp8Encoder */true,  /* enableH264HighProfile */true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());
        ///peerConnectionFactory = new PeerConnectionFactory(options, defaultVideoEncoderFactory, defaultVideoDecoderFactory);
        peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options)
                .createPeerConnectionFactory();

        //Now create a VideoCapturer instance. Callback methods are there if you want to do something! Duh!
        //VideoCapturer videoCapturerAndroid = getVideoCapturer(new CustomCameraEventsHandlers());
        VideoCapturer videoCapturerAndroid = createVideoCapturer();


        //Create MediaConstraints - Will be useful for specifying video and audio constraints.
        audioConstraints = new MediaConstraints();
        videoConstraints = new MediaConstraints();

        //Create a VideoSource instance
        if (videoCapturerAndroid != null) {
            videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid);
        }
        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);


        //create an AudioSource instance
        audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);


        if (videoCapturerAndroid != null) {
            videoCapturerAndroid.startCapture(1024, 720, 30);
        }
        //create a videoRenderer based on SurfaceViewRenderer instance
        //localRenderer = new VideoRenderer(local_video);
        // And finally, with our VideoRenderer ready, we
        // can add our renderer to the VideoTrack.
        //localVideoTrack.addRenderer(localRenderer);

        //////////////////////////////////////////////////////////////////////
//        local_video.addFrameListener(new EglRenderer.FrameListener() {
//            @Override
//            public void onFrame(Bitmap bitmap) {
//                Log.d("ABHI", "frame");
//            }
//        },2);
        VideoSink videoSink = new VideoSink() {
            @Override
            public void onFrame(VideoFrame videoFrame) {
                //Log.d("ABHI", "frames cap");
                local_video.onFrame(videoFrame);
                //BitmapFactory bitmapFactory = cre
                //Canvas canvas = new Canvas()

            }
        };
        localVideoTrack.addSink(videoSink);
        //////////////////////////////////////////////////////////////////////
        local_video.setMirror(true);
        remote_video.setMirror(true);

        //creating peer connection

        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        //PeerConnection.IceServer peerIceServer = PeerConnection.IceServer.builder(iceServer.url).createIceServer();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun2.1.google.com:19302").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("turn:35.225.250.3:3478").setUsername("Abhi")
                .setPassword("Abhi").createIceServer());

//        PeerConnection.IceServer peerIceServer = PeerConnection.IceServer.builder(iceServer.url)
//                                .setUsername(iceServer.username)
//                                .setPassword(iceServer.credential)
//                                .createIceServer();
//        iceServers.add(PeerConnection.IceServer.builder("turn:numb.viagenie.ca")
//                .setUsername("webrtc@live.com")
//                .setPassword("muazkh")
//                .createIceServer());
        //iceServers.add(new org.webrtc.PeerConnection.IceServer("stun:stun2.1.google.com:19302"));
        //iceServers.add(new org.webrtc.PeerConnection.IceServer("turn:numb.viagenie.ca", "webrtc@live.com", "muazkh"));

        //create sdpConstraints
        sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveVideo", "true"));

        //creating localPeer

        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        localPeer = peerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver("localPeerCreation") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                FireDatabase.getInstance(VideoRoom.this).insertLocalCandidate(login_session.getName(), iceCandidate.sdp,
                        iceCandidate.sdpMid, iceCandidate.sdpMLineIndex);
                if (conn_user != null) {
                    FireDatabase.getInstance(VideoRoom.this).insertRemoteCandidate(conn_user, iceCandidate.sdp,
                            iceCandidate.sdpMid, iceCandidate.sdpMLineIndex);
                }
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                //showToast("Received Remote stream");
                super.onAddStream(mediaStream);
                addstream(mediaStream);
            }
        });


//        localPeer = peerConnectionFactory.createPeerConnection(iceServers, sdpConstraints, new CustomPeerConnectionObserver("localPeerCreation") {
//            @Override
//            public void onIceCandidate(IceCandidate iceCandidate) {
//                super.onIceCandidate(iceCandidate);
//                sendicecandidate(iceCandidate);
//            }
//
//            @Override
//            public void onAddStream(MediaStream mediaStream) {
//                super.onAddStream(mediaStream);
//                addstream(mediaStream);
//            }
//
//            @Override
//            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
//                super.onIceGatheringChange(iceGatheringState);
//
//            }
//        });

        //creating local mediastream
        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
        //stream.addTrack(localAudioTrack);
        stream.addTrack(localVideoTrack);
        localPeer.addStream(stream);

    }

    private void addstream(MediaStream mediaStream){
        final VideoTrack videoTrack = mediaStream.videoTracks.get(0);
        AudioTrack audioTrack = mediaStream.audioTracks.get(0);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    remoteRenderer = new VideoRenderer(remote_video);
                    videoTrack.addRenderer(remoteRenderer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.call:
                if (TextUtils.isEmpty(name_to_call.getText().toString().trim())){
                    return;
                }
                call(name_to_call.getText().toString().trim());
                hangup.setEnabled(true);
                call.setEnabled(false);
                break;
            case R.id.hangup:
                close();
                finish();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        FireDatabase.getInstance(this).addSelfListener(login_session.getName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        close();
        finish();
    }

    public void close(){
        FireDatabase.getInstance(this).removeSelfListener(login_session.getName());
        FireDatabase.getInstance(this).removeRemoteListener(conn_user);
        if(peerConnectionFactory == null){
            return;
        }
        if (localPeer == null) return;
        localPeer.close();
        localPeer = null;
        rootEglBase.release();
    }

    private void call(String name_to_call){
        conn_user = name_to_call;
        FireDatabase.getInstance(this).addRemoteListener(name_to_call);
        localPeer.createOffer(new CustomSdpObserver("localCreateOffer"){

            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                FireDatabase.getInstance(VideoRoom.this).insertOffer(name_to_call, sessionDescription.description);
                localPeer.setLocalDescription(new CustomSdpObserver("localSetLocalDesc"), sessionDescription);
            }
        }, sdpConstraints);
    }

    private void handleicecandidate(String candidate, String sdpmid, int sdpmlineIndex){
        Log.e("ABHI", "Handle ice candidates");
        localPeer.addIceCandidate(new IceCandidate(sdpmid, sdpmlineIndex, candidate));
    }

    private void handleoffer(String sessionDescription){
        localPeer.setRemoteDescription(new CustomSdpObserver("localSetRemoteDesc"), new SessionDescription(SessionDescription.Type.OFFER, sessionDescription));

        localPeer.createAnswer(new CustomSdpObserver("remoteCreateOffer") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                //remote answer generated. Now set it as local desc for remote peer and remote desc for local peer.
                super.onCreateSuccess(sessionDescription);
                FireDatabase.getInstance(VideoRoom.this).insertAnswer(login_session.getName(), sessionDescription.description);
                localPeer.setLocalDescription(new CustomSdpObserver("remoteSetLocalDesc"), sessionDescription);

            }
        },new MediaConstraints());
    }

    private void handleanswer(String sessionDescription){
        localPeer.setRemoteDescription(new CustomSdpObserver("localSetRemoteDesc"), new SessionDescription(SessionDescription.Type.ANSWER, sessionDescription));
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataPacks.OfferListenerDataPack offerListenerDataPack) {
        handleoffer(offerListenerDataPack.des);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataPacks.RemoteCandidateDataPack remoteCandidateDataPack) {
        handleicecandidate(remoteCandidateDataPack.candidate, remoteCandidateDataPack.sdpMid,
                remoteCandidateDataPack.sdpMLineIndex);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataPacks.AnswerListenerDataPack answerListenerDataPack) {
        handleanswer(answerListenerDataPack.des);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataPacks.LocalCandidateDataPack localCandidateDataPack) {
        handleicecandidate(localCandidateDataPack.candidate, localCandidateDataPack.sdpMid,
                localCandidateDataPack.sdpMLineIndex);
    }
}
