package com.bitbybit.respiro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bitbybit.respiro.R;
import com.bitbybit.respiro.utils.SharedPrefs;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AccountFragment extends Fragment {

    TextView name, email;
    MaterialButton signout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        signout = view.findViewById(R.id.signout);
        name.setText(SharedPrefs.getInstance(getActivity()).get_username());
        email.setText(SharedPrefs.getInstance(getActivity()).get_email());
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPrefs.getInstance(getActivity()).clear_session();
                startActivity(new Intent(getActivity(), Login.class));
                getActivity().finish();
            }
        });
        return view;
    }
}
