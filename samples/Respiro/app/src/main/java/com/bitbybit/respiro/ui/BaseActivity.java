package com.bitbybit.respiro.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.bitbybit.respiro.R;
import com.bitbybit.respiro.calibration.Calibration;
import com.bitbybit.respiro.utils.Data;
import com.bitbybit.respiro.utils.SharedPrefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Overview");
        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!SharedPrefs.getInstance(this).get_session_token()){
            startActivity(new Intent(this, Login.class));
            finish();
        }

        if (!SharedPrefs.getInstance(this).get_cal()) {
            startActivity(new Intent(this, Calibration.class));
            finish();
        } else {
            switch (Data.getInstance().getCode()) {
                case 0:
                    getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, new OverviewFragment()).commit();
                    break;
                case 1:
                    getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, new AddFragment()).commit();
                    break;
                case 2:
                    getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, new HistoryFragment()).commit();
                    break;
                default:
                    getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, new OverviewFragment()).commit();
                    break;
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.overview:
                    fragment = new OverviewFragment();
                    setTitle("Overview");
                    break;
                case R.id.add:
                    fragment = new AddFragment();
                    setTitle("Add");
                    break;
                case R.id.history:
                    fragment = new HistoryFragment();
                    setTitle("History");
                    break;
                case R.id.account:
                    fragment = new AccountFragment();
                    setTitle("Account");
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, fragment).commit();
            return true;
        }
    };
}