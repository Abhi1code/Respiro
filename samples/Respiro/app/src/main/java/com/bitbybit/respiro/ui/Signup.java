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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private TextInputEditText name, email, password, confirmpassword;
    private MaterialButton signupbutton;
    private TextView signInText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        findingview();
        prepare_progressdialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(SharedPrefs.getInstance(this).get_session_token()){
            Intent intent = new Intent(this, BaseActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void findingview() {
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.enterpassword);
        confirmpassword = findViewById(R.id.enterconfirmpassword);
        signInText = findViewById(R.id.alreadyAccount);
        signupbutton = findViewById(R.id.signUpButton);
        signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Signup.this, Login.class));
                finish();
            }
        });

        signupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Respiro");
        closesoftkeyboard();
    }

    public void prepare_progressdialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setTitle("Logging In");
        progressDialog.setMessage("please wait...");
    }

    private void closesoftkeyboard(){
        View v = this.getCurrentFocus();
        if(v != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(),0);
        }
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

    private void signUp() {
        String name1 = name.getText().toString().trim();
        String email1 = email.getText().toString().trim();
        String password1 = password.getText().toString().trim();
        String cpassword = confirmpassword.getText().toString().trim();

        if (!password1.equals(cpassword)) {
            Toast.makeText(this, "Passwords must match", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!TextUtils.isEmpty(name1) && !TextUtils.isEmpty(email1) && !TextUtils.isEmpty(password1)){
            progressDialog.show();
            register_user(email1, password1, name1);
        } else {
            alertdialog("Please enter all fields...");
        }
    }

    private void register_user(final String email, final String pass, final String name) {

        JSONObject data = new JSONObject();
        try {
            data.put("email", email);
            data.put("password", pass);
            data.put("name", name);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Data.signup_url,
                data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response;
                    if (jsonObject.getBoolean("success")) {
                        progressDialog.cancel();
                        SharedPrefs.getInstance(Signup.this).set_session(name, "mehariaabhishek@gmail.com", "abhishek");
                        startActivity(new Intent(Signup.this, BaseActivity.class));
                        finish();
                    } else {
                        alertdialog("Email already registered");
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

        Mysingleton.getInstance(this.getApplicationContext()).addtorequestque(jsonObjectRequest, "signup");
    }
}