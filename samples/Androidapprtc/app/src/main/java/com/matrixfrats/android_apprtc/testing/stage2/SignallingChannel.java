package com.matrixfrats.android_apprtc.testing.stage2;

import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SignallingChannel {

    private String roomId;
    private boolean isInitiator;
    private boolean isRegistered;
    private DatabaseReference conn;
    private ValueEventListener offerListener, answerListener, localCandidateListener, remoteCandidateListener, hangupListener;
    private MessageCallback messageCallback;

    public interface InitiateCallback {
        void onSuccess();
        void onFailed();
    }

    public interface CloseCallback {
        void onClose();
    }

    public interface MessageCallback {
        void onMessage(MsgClass msgClass);
    }

    public SignallingChannel(@NotNull String roomId, @NonNull boolean isInitiator) {
        this.roomId = roomId;
        this.isInitiator = isInitiator;
        this.isRegistered = false;
        this.conn = null;
    }

    public void initiate(@NonNull InitiateCallback initiateCallback) {
        if (this.conn != null) return;
        if (this.isRegistered) return;
        if (TextUtils.isEmpty(roomId)) return;

        Utils.trace("Connecting to firebase");
        this.conn = FirebaseDatabase.getInstance().getReference();

        this.conn.child(this.roomId).child("session").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If the user is initiator
                if (snapshot.getValue() == null && isInitiator) {
                    conn.child(roomId).removeValue();
                    conn.child(roomId).child("session").child("code").setValue(1, (error, ref) -> {
                        registerListener();
                        initiateCallback.onSuccess();
                    });
                    // If the user is not initiator
                } else if (snapshot.getValue() != null
                        && snapshot.child("code").getValue(Integer.class) == 1
                        && !isInitiator) {
                    conn.child(roomId).child("session").child("code").setValue(2, (error, ref) -> {
                        registerListener();
                        initiateCallback.onSuccess();
                    });
                } else {
                    initiateCallback.onFailed();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                initiateCallback.onFailed();
            }
        });
    }

    private void registerListener() {
        if (this.isRegistered) return;
        Utils.trace("Registering listeners");

        offerListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null)
                    handleIncomingMessage("offer", new SdpClass("offer", snapshot.child("sdp").getValue(String.class)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        answerListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null)
                    handleIncomingMessage("answer", new SdpClass("answer", snapshot.child("sdp").getValue(String.class)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        remoteCandidateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    handleIncomingMessage("remotecandidate", new IceClass(snapshot.child("candidate").getValue(String.class),
                            snapshot.child("sdpMid").getValue(String.class), snapshot.child("sdpMLineIndex").getValue(Integer.class)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        localCandidateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    handleIncomingMessage("localcandidate", new IceClass(snapshot.child("candidate").getValue(String.class),
                            snapshot.child("sdpMid").getValue(String.class), snapshot.child("sdpMLineIndex").getValue(Integer.class)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        hangupListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null)
                    handleIncomingMessage("hangup", new HangupClass("hangup", snapshot.child("hangup").getValue(Boolean.class)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        if (this.isInitiator) {
            this.conn.child(this.roomId).child("offer").addValueEventListener(offerListener);
            this.conn.child(this.roomId).child("remotecandidate").addValueEventListener(remoteCandidateListener);
            this.conn.child(this.roomId).child("hangup").addValueEventListener(hangupListener);
        } else {
            this.conn.child(this.roomId).child("answer").addValueEventListener(answerListener);
            this.conn.child(this.roomId).child("localcandidate").addValueEventListener(localCandidateListener);
            this.conn.child(this.roomId).child("hangup").addValueEventListener(hangupListener);
        }

        this.isRegistered = true;
    }

    private void removeListener() {
        if (!this.isRegistered) return;
        this.conn.child(this.roomId).child("offer").removeEventListener(offerListener);
        this.conn.child(this.roomId).child("answer").removeEventListener(answerListener);
        this.conn.child(this.roomId).child("localcandidate").removeEventListener(localCandidateListener);
        this.conn.child(this.roomId).child("remotecandidate").removeEventListener(remoteCandidateListener);
        this.conn.child(this.roomId).child("hangup").removeEventListener(hangupListener);
        this.isRegistered = false;
    }

    public void close(@NonNull CloseCallback closeCallback) {
        if (this.isRegistered) this.removeListener();
        if (this.roomId == null || this.conn == null) return;
        Utils.trace("Closing connection");

        this.conn.child(this.roomId).child("hangup").child("hangup").setValue(true, (error, ref) -> {
            this.conn.child(this.roomId).child("session").removeValue((error1, ref1) -> {
                roomId = null;
                conn = null;
                messageCallback = null;
                closeCallback.onClose();
            });
        });
    }

    public void setOnMessageListener(@NonNull MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    private void handleIncomingMessage(String type, Object object) {
        MsgClass msgClass = new MsgClass(type, object);
        if (messageCallback != null) messageCallback.onMessage(msgClass);
    }

    public void sendMessage(@NonNull MsgClass msgClass) {
        if (this.roomId == null || this.conn == null) return;
        this.conn.child(this.roomId).child(msgClass.getType()).setValue(msgClass.getObject());
    }
}
