package com.bitbybit.respiro.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bitbybit.respiro.R;
import com.bitbybit.respiro.adaptors.HistoryAdaptor;
import com.bitbybit.respiro.gs.HistoryGS;
import com.bitbybit.respiro.utils.Data;
import com.bitbybit.respiro.utils.Mysingleton;
import com.bitbybit.respiro.utils.SharedPrefs;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryFragment extends Fragment {

    private RecyclerView rchistory;
    private ProgressDialog progressDialog;
    public ArrayList<HistoryGS> historyGS;
    private HistoryAdaptor historyAdaptor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment, container, false);
        rchistory = view.findViewById(R.id.rchistory);

        prepare_progressdialog();
        setrecycleview();
        fetchData();
        return view;
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

    private void setrecycleview() {
        historyGS = new ArrayList<>();
        rchistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        rchistory.setHasFixedSize(true);
        historyAdaptor = new HistoryAdaptor(historyGS, getActivity());
        rchistory.setAdapter(historyAdaptor);
    }

    private void fetchData() {
        progressDialog.show();
        JSONObject data = new JSONObject();
        try {
            data.put("email", SharedPrefs.getInstance(getActivity()).get_email());
            data.put("password", SharedPrefs.getInstance(getActivity()).get_password());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Data.getHistory
                , data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        progressDialog.cancel();
                        JSONArray time = response.getJSONArray("time");
                        JSONArray fev = response.getJSONArray("fev");
                        JSONArray fvc = response.getJSONArray("fvc");
                        for (int i = 0; i < time.length(); i++) {
                            historyGS.add(new HistoryGS(time.getLong(i), round(fev.getDouble(i), 2), round(fvc.getDouble(i), 2)));
                        }
                        historyAdaptor.notifyDataSetChanged();
                    } else {
                        alertdialog("something went wrong");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.cancel();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.cancel();
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

        Mysingleton.getInstance(getActivity().getApplicationContext()).addtorequestque(jsonObjectRequest, "history");
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
