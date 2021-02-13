package com.matrixfrats.android_apprtc.testing.stage2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.widget.Toast;

import com.matrixfrats.android_apprtc.R;

public class AppController extends AppCompatActivity implements Call.CallListeners {

    private Call call;
    private VideoRoom videoRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stage2_app_controller);
        this.showRoomSelection();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private void showRoomSelection() {
        RoomSelection roomSelection = new RoomSelection();
        getSupportFragmentManager().beginTransaction().replace(R.id.baseframe, roomSelection).commit();
        roomSelection.onJoinClickListener(id -> {
            getSupportFragmentManager().beginTransaction().replace(R.id.baseframe, new Loader()).commit();
            createCall(id, true);
        });
    }

    private void createCall(String roomId, boolean isInitiator) {
        if (this.call != null) return;
        this.call = new Call(getApplicationContext());
        this.call.registerCallListener(this);
        this.call.initiateCall(roomId, isInitiator);
    }

    @Override
    public void onUserMediaSuccess() {
        Utils.trace("Media success");
        videoRoom = new VideoRoom();
        getSupportFragmentManager().beginTransaction().replace(R.id.baseframe, videoRoom).commitNow();
        videoRoom.setUiForLocalVideo(this.call.getVideoTrack());
    }

    @Override
    public void onUserMediaFailed(String err) {
        Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        this.call = null;
        this.showRoomSelection();
    }

    @Override
    public void showSharingBox(String link) {
        if (this.videoRoom == null) return;
        this.videoRoom.showSharingBox(link);
    }
}