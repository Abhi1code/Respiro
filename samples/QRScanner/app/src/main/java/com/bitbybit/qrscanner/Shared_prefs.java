package com.bitbybit.qrscanner;

import android.content.Context;
import android.content.SharedPreferences;

public class Shared_prefs {

    private static volatile Shared_prefs shared_prefs;
    private Context context;
    SharedPreferences login_creds, email_list;
    SharedPreferences.Editor creds_editor, email_editor;

    private Shared_prefs(Context context){
        this.context = context;
        prepare_sharedprefs();
    }

    public static synchronized Shared_prefs getInstance(Context context){
        if(shared_prefs == null){
            shared_prefs = new Shared_prefs(context);
        }
        return shared_prefs;
    }

    private void prepare_sharedprefs(){
        login_creds = context.getSharedPreferences("LOGIN_CRED_FILENAME", Context.MODE_PRIVATE);
        email_list = context.getSharedPreferences("EMAIL_STORAGE_FILENAME", Context.MODE_PRIVATE);
        creds_editor = login_creds.edit();
        email_editor = email_list.edit();
    }

    public void set_session(String username, String password){
        clear_session();
        set_username(username);
        set_password(password);
        set_session_token(true);
    }

    public void clear_session(){
        set_username(null);
        set_password(null);
        set_session_token(false);
        set_ip("");
        email_editor.clear().commit();
    }

    private void set_username(String username){
        creds_editor.putString("USERNAME_KEYWORD", username).commit();
    }

    public String get_username(){
        return login_creds.getString("USERNAME_KEYWORD", "username");
    }

    private void set_password(String password){
        creds_editor.putString("PASSWORD_KEYWORD", password).commit();
    }

    public String get_password(){
        return login_creds.getString("PASSWORD_KEYWORD", "password");
    }

    private void set_session_token(boolean flag) {
        creds_editor.putBoolean("SESSION_TOKEN", flag).commit();
    }

    public boolean get_session_token() {
        return login_creds.getBoolean("SESSION_TOKEN", false);
    }

    public String get_ip() {
        return login_creds.getString("IP", "78fb51ee11c1");
    }

    public void set_ip(String ip) {
        creds_editor.putString("IP", ip).commit();
    }
}
