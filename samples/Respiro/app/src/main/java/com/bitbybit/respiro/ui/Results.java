package com.bitbybit.respiro.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bitbybit.respiro.R;
import com.bitbybit.respiro.utils.Data;
import com.bitbybit.respiro.utils.Mysingleton;
import com.bitbybit.respiro.utils.SharedPrefs;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Results extends AppCompatActivity {

    protected Typeface tfRegular;
    protected Typeface tfLight;
    private LineChart chart;
    private PowerSpinnerView powerSpinnerView;
    private ProgressDialog progressDialog;
    private TextView des;
    JSONObject temp;
    CircleProgressBar cp1, cp2, cp3;
    TextView val1, val2, val3, val4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        cp1 = findViewById(R.id.cprogress1);
        cp2 = findViewById(R.id.cprogress2);
        cp3 = findViewById(R.id.cprogress3);
        val1 = findViewById(R.id.pvalue1);
        val2 = findViewById(R.id.pvalue2);
        val3 = findViewById(R.id.pvalue3);
        val4 = findViewById(R.id.pvalue4);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(ShowFullEmail.this, "Back button", Toast.LENGTH_SHORT).show();
                Data.getInstance().setCode(0);
                startActivity(new Intent(Results.this, BaseActivity.class));
                finish();
            }
        });

        tfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        tfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");

        chart = findViewById(R.id.chart);
        des = findViewById(R.id.des);

        // background color
        chart.setBackgroundColor(Color.WHITE);

        // disable description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // set listeners
        //chart.setOnChartValueSelectedListener(this);
        chart.setDrawGridBackground(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        XAxis xAxis = chart.getXAxis();
        // vertical grid lines
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
//        xAxis.setValueFormatter(new ValueFormatter() {
//            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
//
//            @Override
//            public String getFormattedValue(float value) {
//                long millis = TimeUnit.HOURS.toMillis((long) value);
//                return mFormat.format(new Date(millis));
//            }
//        });


        YAxis yAxis;
        // // Y-Axis Style // //
        yAxis = chart.getAxisLeft();
        // disable dual axis (only use LEFT axis)
        chart.getAxisRight().setEnabled(false);

        // horizontal grid lines
        yAxis.enableGridDashedLine(10f, 10f, 0f);
        // axis range
        //yAxis.setAxisMaximum(200f);
        //yAxis.setAxisMinimum(-50f);
        //setData(10, 50);
        // draw points over time
        //chart.animateX(1500);
        // get the legend (only possible after setting data)
        //Legend l = chart.getLegend();
        // draw legend entries as lines
        //l.setForm(Legend.LegendForm.LINE);

        powerSpinnerView = findViewById(R.id.spinner);
        initSpinner();
        prepare_progressdialog();
        fetch();
    }

    public void prepare_progressdialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        //progressDialog.setTitle("Fetching");
        progressDialog.setMessage("please wait...");
    }

    private void alertdialog(String s) {
        progressDialog.cancel();
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);
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

    private void initSpinner() {

        powerSpinnerView.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                switch (newItem) {
                    case "F|T":
                        JSONArray array = null;
                        try {
                            array = temp.getJSONObject("data").getJSONArray("fvst");
                            ArrayList<Entry> values = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONArray ja = array.getJSONArray(i);
                                values.add(new Entry((float) ja.getDouble(0), (float) ja.getDouble(1)));
                            }
                            des.setText("FLOW VS TIME");
                            setData(values);// add one entry per hour
                            chart.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "F|V":
                        JSONArray array1 = null;
                        try {
                            array1 = temp.getJSONObject("data").getJSONArray("fvsv");
                            ArrayList<Entry> values = new ArrayList<>();
                            for (int i = 0; i < array1.length(); i++) {
                                JSONArray ja = array1.getJSONArray(i);
                                values.add(new Entry((float) ja.getDouble(0), (float) ja.getDouble(1)));
                            }
                            des.setText("FLOW VS VOLUME");
                            setData(values);// add one entry per hour
                            chart.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "V|T":
                        JSONArray array2 = null;
                        try {
                            array2 = temp.getJSONObject("data").getJSONArray("vvst");
                            ArrayList<Entry> values = new ArrayList<>();
                            for (int i = 0; i < array2.length(); i++) {
                                JSONArray ja = array2.getJSONArray(i);
                                values.add(new Entry((float) ja.getDouble(0), (float) ja.getDouble(1)));
                            }
                            des.setText("VOLUME VS TIME");
                            setData(values);// add one entry per hour
                            chart.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });

    }

    private void setData(ArrayList<Entry> values) {

        // now in hours
//        long now = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());
//
//        ArrayList<Entry> values = new ArrayList<>();
//
//        // count = hours
//        float to = now + count;
//
//        // increment by 1 hour
//        for (float x = now; x < to; x++) {
//
//            float y = (float) (Math.random() * range) + 50;
//            values.add(new Entry(x, y)); // add one entry per hour
//        }

        LineDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.notifyDataSetChanged();
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet");

            set1.setDrawIcons(false);

            // draw dashed line
            //set1.enableDashedLine(10f, 5f, 0f);

            // black lines and points
            set1.setColor(getResources().getColor(R.color.purple_500));
            set1.setCircleColor(getResources().getColor(R.color.purple_500));
            set1.setDrawCircles(false);

            // line thickness and point size
            set1.setLineWidth(4f);
            set1.setCircleRadius(0f);

            // draw points as solid circles
            set1.setDrawCircleHole(false);

            // customize legend entry
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            // text size of values
            set1.setValueTextSize(9f);

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f);

            // set the filled area
            //set1.setDrawFilled(true);
//            set1.setFillFormatter(new IFillFormatter() {
//                @Override
//                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
//                    return chart.getAxisLeft().getAxisMinimum();
//                }
//            });

//            // set color of filled area
//            if (Utils.getSDKInt() >= 18) {
//                // drawables only supported on api level 18 and above
//                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.fade_red);
//                set1.setFillDrawable(drawable);
//            } else {
//                set1.setFillColor(Color.BLACK);
//            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets
            set1.setDrawValues(false);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            chart.setData(data);

            chart.animateX(1500);
            // get the legend (only possible after setting data)
            Legend l = chart.getLegend();
            // draw legend entries as lines
            l.setForm(Legend.LegendForm.LINE);
        }
    }

    private void fetch() {
        progressDialog.show();
        callServer(SharedPrefs.getInstance(this).get_email(), SharedPrefs.getInstance(this).get_password());
    }

    private void callServer(final String email, final String pass) {

        JSONObject data = new JSONObject();
        try {
            data.put("email", email);
            data.put("password", pass);
            data.put("time", Data.getInstance().getTime());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Data.getData,
                data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response;
                    temp = response;
                    if (jsonObject.getBoolean("success")) {
                        progressDialog.cancel();
                        //setData();
                        //Log.d("ABHI", jsonObject.getJSONObject("data").toString());
                        //chart.invalidate();
                        double fvc = jsonObject.getJSONObject("data").getDouble("fvc");
                        double fev1 = jsonObject.getJSONObject("data").getDouble("fev");
                        double hs = jsonObject.getJSONObject("data").getDouble("lhs");
                        double pef = jsonObject.getJSONObject("data").getDouble("pef");
                        int fvc_h = 4, hs_h = 5, fev1_h = 3;
                        cp1.setProgress(((int) ((100/fvc_h*1.0)*fvc)));
                        val1.setText("(" + round(fvc, 2) + ")");
                        cp2.setProgress(((int) ((100/fev1_h*1.0)*fev1)));
                        val2.setText("(" + round(fev1, 2) + ")");
                        cp3.setProgress(((int) ((100/hs_h*1.0)*hs)));
                        val3.setText("(" + round(hs, 2) + ")");
                        val4.setText("PEF " + "(" + round(pef, 2) + ")");

                        JSONArray array = jsonObject.getJSONObject("data").getJSONArray("fvst");
                        ArrayList<Entry> values = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONArray ja = array.getJSONArray(i);
                            values.add(new Entry((float) ja.getDouble(0), (float) ja.getDouble(1)));
                        }
                        des.setText("FLOW VS TIME");
                        setData(values);// add one entry per hour
                        chart.invalidate();
                    } else {
                        alertdialog("Something went wrong");
                    }

                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                alertdialog("Please check your network connection...");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        Mysingleton.getInstance(this.getApplicationContext()).addtorequestque(jsonObjectRequest, "result");
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}