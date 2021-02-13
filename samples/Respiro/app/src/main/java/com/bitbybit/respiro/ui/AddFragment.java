package com.bitbybit.respiro.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bitbybit.respiro.R;
import com.bitbybit.respiro.calibration.Calibration;
import com.bitbybit.respiro.utils.Data;
import com.bitbybit.respiro.utils.SharedPrefs;
import com.bitbybit.respiro.utils.SoundMeter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddFragment extends Fragment {

    boolean rflag = false, eflag = true;
    int rcode = 0;
    private Handler handler, handler1;
    SoundMeter soundMeter;
    ImageView ball1, ball2, ball3;
    CardView card1, card2, card3;
    TextView info;
    FloatingActionButton record, next;
    ProgressDialog progressDialog;

    ObjectAnimator anim11, anim12, anim21, anim22, anim31, anim32;
    float pos1 = 1000000, pos2 = 1000000, pos3 = 1000000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_fragment, container, false);
        ball1 = view.findViewById(R.id.ball1);
        ball2 = view.findViewById(R.id.ball2);
        ball3 = view.findViewById(R.id.ball3);
        card1 = view.findViewById(R.id.card1);
        card2 = view.findViewById(R.id.card2);
        card3 = view.findViewById(R.id.card3);

        info = view.findViewById(R.id.info);
        record = view.findViewById(R.id.record);
        next = view.findViewById(R.id.next);

        soundMeter = new SoundMeter(getActivity());
        handler = new Handler();
        handler1 = new Handler();
        prepare_progressdialog();

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermission()) {
                    requestPermission();
                } else {
                    if(!rflag) {
                        soundMeter.start();
                        runnable.run();
                        info.setText("Recording...");
                        rcode = 1;
                        rflag = true;
                        if (eflag) {
                            eflag = false;
                            handler1.postDelayed(runnable1, 1000);
                        }
                    } else {
                        soundMeter.stop();
                        handler.removeCallbacks(runnable);
                        info.setText("Record your audio");
                        rcode = 2;
                        rflag = false;
                    }
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rcode == 2) {
                    uploadFile();
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            soundMeter.stop();
            handler.removeCallbacks(runnable);
            info.setText("Record your audio");
            rflag = false;
            alertdialog("Recording error! Don't move your phone while recording.");
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //Log.e(TAG, "" + soundMeter.getAmplitude() + " " + soundMeter.getAmplitudeEMA());
            double amp = soundMeter.getAmplitude();
            if (amp > 0.8) {
                process(amp, 1);
                process(amp, 2);
                process(amp-0.8, 3);
            }

            if (amp >= 0.4 && amp <= 0.8) {
                process(amp, 1);
                process(amp-0.4, 2);
            }

            if (amp < 0.4) {
                process(amp, 1);
            }

            handler.postDelayed(this, 500);
        }
    };

    private void process(double amp, int flag) {
        float temp = (float) ((-card1.getMeasuredHeight()/(0.4*1.0))*amp);
        int time = (int) ((1000/card1.getMeasuredHeight())*temp*-1);
        if ((temp + ball1.getHeight() + 20) < -5) {
            if (temp < (-card1.getMeasuredHeight() + ball1.getHeight() + 20)) {
                temp = -card1.getMeasuredHeight();
                time = 1000;
            }

            switch (flag) {
                case 1:
                    if (anim11 != null && anim12 != null) {
                        anim11.cancel();
                        anim12.cancel();
                    }
                    setFirstAnimator(temp, time, time);
                    break;
                case 2:
                    if (anim21 != null && anim22 != null) {
                        anim21.cancel();
                        anim22.cancel();
                    }
                    setSecondAnimator(temp, time, time);
                    break;
                case 3:
                    if (anim31 != null && anim32 != null) {
                        anim31.cancel();
                        anim32.cancel();
                    }
                    setThirdAnimator(temp, time, time);
                    break;
            }
        }
    }

    private void setFirstAnimator(float pos, int utime, int dtime) {
        pos1 = 1;
        anim11 = ObjectAnimator.ofFloat(ball1, "translationY",  pos + ball1.getHeight() + 20);
        anim11.setDuration(utime);
        anim11.setInterpolator(new DecelerateInterpolator());
        anim11.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                pos1 = -1;
                anim12 = ObjectAnimator.ofFloat(ball1, "translationY", 0);
                anim12.setDuration(dtime);
                anim12.setInterpolator(new AccelerateInterpolator());
                anim12.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        //Log.d(TAG, valueAnimator.getAnimatedValue("translationY") + "");
                        pos1 = (Float) valueAnimator.getAnimatedValue("translationY");
                    }
                });
                anim12.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        anim11.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                pos1 = (Float) valueAnimator.getAnimatedValue("translationY");
            }
        });
        anim11.start();
    }

    private void setSecondAnimator(float pos, int utime, int dtime) {
        pos2 = 1;
        anim21 = ObjectAnimator.ofFloat(ball2, "translationY",  pos + ball2.getHeight() + 20);
        anim21.setDuration(utime);
        anim21.setInterpolator(new DecelerateInterpolator());
        anim21.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                pos2 = -1;
                anim22 = ObjectAnimator.ofFloat(ball2, "translationY", 0);
                anim22.setDuration(dtime);
                anim22.setInterpolator(new AccelerateInterpolator());
                anim22.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        //Log.d(TAG, valueAnimator.getAnimatedValue("translationY") + "");
                        pos2 = (Float) valueAnimator.getAnimatedValue("translationY");
                    }
                });
                anim22.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        anim21.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                pos2 = (Float) valueAnimator.getAnimatedValue("translationY");
            }
        });
        anim21.start();
    }

    private void setThirdAnimator(float pos, int utime, int dtime) {
        pos3 = 1;
        anim31 = ObjectAnimator.ofFloat(ball3, "translationY",  pos + ball3.getHeight() + 20);
        anim31.setDuration(utime);
        anim31.setInterpolator(new DecelerateInterpolator());
        anim31.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                pos3 = -1;
                anim32 = ObjectAnimator.ofFloat(ball3, "translationY", 0);
                anim32.setDuration(dtime);
                anim32.setInterpolator(new AccelerateInterpolator());
                anim32.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        //Log.d(TAG, valueAnimator.getAnimatedValue("translationY") + "");
                        pos3 = (Float) valueAnimator.getAnimatedValue("translationY");
                    }
                });
                anim32.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        anim31.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                pos3 = (Float) valueAnimator.getAnimatedValue("translationY");
            }
        });
        anim31.start();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) !=
                        PackageManager.PERMISSION_GRANTED) {
        } else {
            return true;
        }
        return false;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.RECORD_AUDIO}, 001);
    }

    public void prepare_progressdialog(){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        //progressDialog.setTitle("Fetching");
        progressDialog.setMessage("please wait...");
    }

    private void alertdialog(String s){
        progressDialog.cancel();
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(getActivity());
        alertdialog.setMessage(s).setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.cancel();
                        dialogInterface.dismiss();
                    }

                });

        AlertDialog showalertdialog = alertdialog.create();
        //showalertdialog.setTitle("Pairing Request");
        showalertdialog.show();
    }

    private void uploadFile() {
        progressDialog.show();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(getActivity().getFilesDir().getAbsolutePath() + "temp.mp3");

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            progressDialog.cancel();
            e.printStackTrace();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            while (fis.available() > 0) {
                bos.write(fis.read());
            }
        } catch (IOException e1) {
            progressDialog.cancel();
            e1.printStackTrace();
        }

        byte[] bytes = bos.toByteArray();
        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("audio", "androidFlask.mp3", RequestBody.create(MediaType.parse("image/*mp3"), bytes))
                .addFormDataPart("email", SharedPrefs.getInstance(getActivity()).get_email())
                .addFormDataPart("password", SharedPrefs.getInstance(getActivity()).get_password())
                .addFormDataPart("time", System.currentTimeMillis() + "")
                .build();
        postRequest(Data.upload_url, postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertdialog("Failed to Connect to Server");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    Data.getInstance().setTime(jsonObject.getLong("message"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                            Log.d("ABHI", Data.getInstance().getTime() + "");
                            getActivity().startActivity(new Intent(getActivity(), Results.class));
                            getActivity().finish();
                            //alertdialog(response.body().string() );
                            //alertdialog(response.body().string() + " ");
                    }
                });
            }
        });
    }
}
