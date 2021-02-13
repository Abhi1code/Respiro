package com.bitbybit.respiro.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bitbybit.respiro.R;
import com.bitbybit.respiro.utils.Data;
import com.bitbybit.respiro.utils.Mysingleton;
import com.bitbybit.respiro.utils.SharedPrefs;
import com.chivorn.smartmaterialspinner.SmartMaterialSpinner;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class OverviewFragment extends Fragment {

    private PowerSpinnerView powerSpinnerView;
    protected Typeface tfRegular;
    protected Typeface tfLight;
    private LineChart chart;
    private ProgressDialog progressDialog;
    private TextView des;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.overview_fragment, container, false);
        //spProvince = view.findViewById(R.id.spinner1);
        tfRegular = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");
        tfLight = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");

        powerSpinnerView = view.findViewById(R.id.spinner);
        des = view.findViewById(R.id.des);
        initSpinner();

        chart = view.findViewById(R.id.chart);

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
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
            @Override
            public String getFormattedValue(float value) {
                //long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date((long) value));
            }
        });


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

        prepare_progressdialog();
        fetch(Data.pefvst);
        return view;
    }

    public void prepare_progressdialog(){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setTitle("Fetching");
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

    private void initSpinner() {

        powerSpinnerView.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                switch (newItem) {
                    case "PEF":
                        fetch(Data.pefvst);
                        break;
                    case "FVC":
                        fetch(Data.fvcvst);
                        break;
                    case "FEV":
                        fetch(Data.fevvst);
                        break;
                    case "LHS":
                        fetch(Data.lhsvst);
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
            set1 = new LineDataSet(values, "DataSet 1");

            set1.setDrawIcons(false);

            // draw dashed line
            //set1.enableDashedLine(10f, 5f, 0f);

            // black lines and points
            set1.setColor(getResources().getColor(R.color.teal_200));
            set1.setCircleColor(getResources().getColor(R.color.purple_500));

            // line thickness and point size
            set1.setLineWidth(2f);
            set1.setCircleRadius(5f);

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
            //set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
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

    private void fetch(String url) {
        progressDialog.show();
        callServer(url, SharedPrefs.getInstance(getActivity()).get_email(), SharedPrefs.getInstance(getActivity()).get_password());
    }

    private void callServer(String url, final String email, final String pass) {

        JSONObject data = new JSONObject();
        try {
            data.put("email", email);
            data.put("password", pass);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,
                data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response;
                    if (jsonObject.getBoolean("success")) {
                        progressDialog.cancel();
                        //setData();
                        switch (url) {
                            case Data.pefvst:
                                JSONArray array = jsonObject.getJSONArray("data");
                                ArrayList<Entry> values = new ArrayList<>();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONArray ja = array.getJSONArray(i);
                                    values.add(new Entry(ja.getLong(1), (float) ja.getDouble(0)));
                                }
                                des.setText("PEF OVER TIME");
                                setData(values);// add one entry per hour
                                chart.invalidate();
                                break;
                            case Data.fevvst:
                                JSONArray array1 = jsonObject.getJSONArray("data");
                                ArrayList<Entry> values1 = new ArrayList<>();
                                for (int i = 0; i < array1.length(); i++) {
                                    JSONArray ja = array1.getJSONArray(i);
                                    values1.add(new Entry(ja.getLong(1), (float) ja.getDouble(0)));
                                }
                                des.setText("FEV1 OVER TIME");
                                setData(values1);// add one entry per hour
                                chart.invalidate();
                                break;
                            case Data.fvcvst:
                                JSONArray array2 = jsonObject.getJSONArray("data");
                                ArrayList<Entry> values2 = new ArrayList<>();
                                for (int i = 0; i < array2.length(); i++) {
                                    JSONArray ja = array2.getJSONArray(i);
                                    values2.add(new Entry(ja.getLong(1), (float) ja.getDouble(0)));
                                }
                                des.setText("FVC OVER TIME");
                                setData(values2);// add one entry per hour
                                chart.invalidate();
                                break;
                            case Data.lhsvst:
                                JSONArray array3 = jsonObject.getJSONArray("data");
                                ArrayList<Entry> values3 = new ArrayList<>();
                                for (int i = 0; i < array3.length(); i++) {
                                    JSONArray ja = array3.getJSONArray(i);
                                    values3.add(new Entry(ja.getLong(1), (float) ja.getDouble(0)));
                                }
                                des.setText("LUNG HEALTH OVER TIME");
                                setData(values3);// add one entry per hour
                                chart.invalidate();
                                break;
                        }

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

        Mysingleton.getInstance(getActivity().getApplicationContext()).addtorequestque(jsonObjectRequest, "overview");
    }
}
