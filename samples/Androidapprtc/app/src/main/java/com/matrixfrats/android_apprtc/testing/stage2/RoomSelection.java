package com.matrixfrats.android_apprtc.testing.stage2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.matrixfrats.android_apprtc.R;

import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class RoomSelection extends Fragment implements View.OnClickListener {

    private TextView errorText;
    private EditText roomidInput;
    private Button join, random;
    private TextWatcher textWatcher;
    private JoinButtonEvents joinButtonEvents;

    public interface JoinButtonEvents {
        void onJoinClick(@NonNull String id);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stage2_room_selection, container, false);
        errorText = view.findViewById(R.id.errortext);
        roomidInput = view.findViewById(R.id.roomid);
        join = view.findViewById(R.id.join);
        random = view.findViewById(R.id.random);
        errorText.setVisibility(View.GONE);
        this.attachTextWatcher(roomidInput);
        join.setOnClickListener(this);
        random.setOnClickListener(this);
        roomidInput.setText(Utils.generateRandomId());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.validateInputId(roomidInput.getText().toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        roomidInput.removeTextChangedListener(textWatcher);
    }

    private void attachTextWatcher(EditText roomidInput) {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validateInputId(editable.toString());
            }
        };
        roomidInput.addTextChangedListener(textWatcher);
    }

    private void validateInputId(String id) {
        boolean lengthCheck = id.length() > 5;
        String pattern = "^([a-zA-Z0-9-_]+)+$";
        boolean regexCheck = id.matches(pattern);
        if (lengthCheck && regexCheck) {
            join.setEnabled(true);
            join.setClickable(true);
            errorText.setVisibility(View.GONE);
        } else {
            join.setEnabled(false);
            join.setClickable(false);
            errorText.setVisibility(View.VISIBLE);
        }
    }

    public void onJoinClickListener(@NonNull JoinButtonEvents joinButtonEvents) {
        this.joinButtonEvents = joinButtonEvents;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.join:
                if (!this.checkPermission()) return;
                if (joinButtonEvents != null) joinButtonEvents.onJoinClick(roomidInput.getText().toString());
                break;
            case R.id.random:
                roomidInput.setText(Utils.generateRandomId());
                break;
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return true;
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getActivity(), "Please provide camera and mic permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 001);

        } else {
            return true;
        }
        return false;
    }
}
