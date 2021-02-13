package com.bitbybit.respiro.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private ProgressDialog progressDialog;
    private TextInputEditText username, password;
    private MaterialButton sign_in_button, googleSignIn, facebookSignIn;
    private TextView signupText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findingview();
        prepare_progressdialog();
        closesoftkeyboard();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(SharedPrefs.getInstance(this).get_session_token()){
            Intent intent = new Intent(Login.this, BaseActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void closesoftkeyboard(){
        View v = this.getCurrentFocus();
        if(v != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(),0);
        }
    }

    private void findingview() {
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        sign_in_button = findViewById(R.id.signInButton);
        googleSignIn = findViewById(R.id.googlesignInButton);
        facebookSignIn = findViewById(R.id.facebooksignInButton);
        signupText = findViewById(R.id.createAccount);
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Signup.class));
                finish();
            }
        });
        sign_in_button.setOnClickListener(this);
        googleSignIn.setOnClickListener(this);
        facebookSignIn.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Respiro");
    }

    public void prepare_progressdialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setTitle("Logging In");
        progressDialog.setMessage("please wait...");
    }

    private void alertdialog(String s){
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signInButton :
                login();
                break;
            case R.id.googlesignInButton:

                break;
            case R.id.facebooksignInButton:

                break;
        }
    }

    private void login() {
        String user_name = username.getText().toString().trim();
        String pass_word = password.getText().toString().trim();

        if(!TextUtils.isEmpty(user_name) && !TextUtils.isEmpty(pass_word)){
            progressDialog.show();
            authenticate_user(user_name, pass_word);
        } else {
            alertdialog("Please enter username and password...");
        }
    }

    private void authenticate_user(final String email, final String pass) {
        JSONObject data = new JSONObject();
        try {
            data.put("email", email);
            data.put("password", pass);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Data.authcheck_url,
                data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response;
                    if (jsonObject.getBoolean("success")) {
                        progressDialog.cancel();
                        SharedPrefs.getInstance(Login.this).set_session(jsonObject.getString("name"), email, pass);
                        startActivity(new Intent(Login.this, BaseActivity.class));
                        finish();
                    } else {
                        alertdialog("Invalid username or password");
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

        Mysingleton.getInstance(this.getApplicationContext()).addtorequestque(jsonObjectRequest, "auth_check");
    }

}