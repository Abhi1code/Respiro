package com.matrixfrats.android_apprtc.database;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrixfrats.android_apprtc.util.DataPacks;
import com.matrixfrats.android_apprtc.util.IceClass;

import org.greenrobot.eventbus.EventBus;
import org.webrtc.IceCandidate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FireDatabase {

    private static FireDatabase firebaseDatabase;
    private static Context context;

    private FireDatabase(Context c) {
        context = c;
    }

    public static synchronized FireDatabase getInstance(Context context) {
        if (firebaseDatabase == null) {
            firebaseDatabase = new FireDatabase(context);
        }
        return firebaseDatabase;
    }

    public void initUser(String name) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(name);
        databaseReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                databaseReference.child("init").setValue("Initiate", new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        DataPacks.LoginDataPack loginDataPack = new DataPacks.LoginDataPack();
                        loginDataPack.status = true;
                        loginDataPack.name = name;
                        EventBus.getDefault().post(loginDataPack);
                    }
                });
            }
        });
    }

    ValueEventListener offerListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null) return;
            DataPacks.OfferListenerDataPack offerListenerDataPack = new DataPacks.OfferListenerDataPack();
            offerListenerDataPack.des = dataSnapshot.child("des").getValue(String.class);
            EventBus.getDefault().post(offerListenerDataPack);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };

    ValueEventListener remoteCandidateListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null) return;
            DataPacks.RemoteCandidateDataPack remoteCandidateDataPack = new DataPacks.RemoteCandidateDataPack();
            remoteCandidateDataPack.candidate = dataSnapshot.child("candidate").getValue(String.class);
            remoteCandidateDataPack.sdpMid = dataSnapshot.child("sdpMid").getValue(String.class);
            remoteCandidateDataPack.sdpMLineIndex = dataSnapshot.child("sdpMLineIndex").getValue(Integer.class);
            EventBus.getDefault().post(remoteCandidateDataPack);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };

    public void addSelfListener(String name) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(name);
        databaseReference.child("offer").addValueEventListener(offerListener);
        databaseReference.child("remotecandidate").addValueEventListener(remoteCandidateListener);
    }

    public void removeSelfListener(String name) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(name);
        databaseReference.child("offer").removeEventListener(offerListener);
        databaseReference.child("remotecandidate").removeEventListener(remoteCandidateListener);
    }

    ValueEventListener answerListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null) return;
            DataPacks.AnswerListenerDataPack answerListenerDataPack = new DataPacks.AnswerListenerDataPack();
            answerListenerDataPack.des = dataSnapshot.child("des").getValue(String.class);
            EventBus.getDefault().post(answerListenerDataPack);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };

    ValueEventListener localCandidateListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null) return;
            DataPacks.LocalCandidateDataPack localCandidateDataPack = new DataPacks.LocalCandidateDataPack();
            localCandidateDataPack.candidate = dataSnapshot.child("candidate").getValue(String.class);
            localCandidateDataPack.sdpMid = dataSnapshot.child("sdpMid").getValue(String.class);
            localCandidateDataPack.sdpMLineIndex = dataSnapshot.child("sdpMLineIndex").getValue(Integer.class);
            EventBus.getDefault().post(localCandidateDataPack);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };

    public void addRemoteListener(String name) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(name);
        databaseReference.child("answer").addValueEventListener(answerListener);
        databaseReference.child("localcandidate").addValueEventListener(localCandidateListener);
    }

    public void removeRemoteListener(String name) {
        if (name == null) return;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(name);
        databaseReference.child("answer").removeEventListener(answerListener);
        databaseReference.child("localcandidate").removeEventListener(localCandidateListener);
    }

    public void insertOffer(String name, String des) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(name);
        databaseReference.child("offer").child("des").setValue(des);
    }

    public void insertAnswer(String name, String des) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(name);
        databaseReference.child("answer").child("des").setValue(des);
    }

    public void insertLocalCandidate(String name, String candidate, String sdpMid, int sdpMlineIndex) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(name);
        IceClass iceClass = new IceClass(candidate, sdpMid, sdpMlineIndex);
        databaseReference.child("localcandidate").setValue(iceClass);
    }

    public void insertRemoteCandidate(String name, String candidate, String sdpMid, int sdpMlineIndex) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(name);
        IceClass iceClass = new IceClass(candidate, sdpMid, sdpMlineIndex);
        databaseReference.child("remotecandidate").setValue(iceClass);
    }
}
