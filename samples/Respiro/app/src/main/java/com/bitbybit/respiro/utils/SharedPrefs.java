package com.bitbybit.respiro.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {

    private static volatile SharedPrefs shared_prefs;
    private Context context;
    SharedPreferences login_creds, email_list;
    SharedPreferences.Editor creds_editor, email_editor;

    private SharedPrefs(Context context){
        this.context = context;
        prepare_sharedprefs();
    }

    public static synchronized SharedPrefs getInstance(Context context){
        if(shared_prefs == null){
            shared_prefs = new SharedPrefs(context);
        }
        return shared_prefs;
    }

    private void prepare_sharedprefs(){
        login_creds = context.getSharedPreferences("LOGIN_CRED_FILENAME", Context.MODE_PRIVATE);
        email_list = context.getSharedPreferences("EMAIL_STORAGE_FILENAME", Context.MODE_PRIVATE);
        creds_editor = login_creds.edit();
        email_editor = email_list.edit();
    }

    public void set_session(String username, String email, String password){
        clear_session();
        set_username(username);
        set_email(email);
        set_password(password);
        set_session_token(true);
        set_cal(false);
    }

    public void clear_session(){
        set_username(null);
        set_password(null);
        set_email(null);
        set_session_token(false);
        set_cal(false);
        email_editor.clear().commit();
    }

    private void set_username(String username){
        creds_editor.putString("USERNAME_KEYWORD", username).commit();
    }

    public String get_username(){
        return login_creds.getString("USERNAME_KEYWORD", "username");
    }

    private void set_email(String email){
        creds_editor.putString("EMAIL_KEYWORD", email).commit();
    }

    public String get_email(){
        return login_creds.getString("EMAIL_KEYWORD", "email");
    }

    private void set_password(String password){
        creds_editor.putString("PASSWORD_KEYWORD", password).commit();
    }

    public String get_password(){
        return login_creds.getString("PASSWORD_KEYWORD", "password");
    }

    public void set_cal(boolean cal){
        creds_editor.putBoolean("CAL_KEYWORD", cal).commit();
    }

    public boolean get_cal(){
        return login_creds.getBoolean("CAL_KEYWORD", false);
    }

    private void set_session_token(boolean flag){
        creds_editor.putBoolean("SESSION_TOKEN", flag).commit();
    }

    public boolean get_session_token(){
        return login_creds.getBoolean("SESSION_TOKEN", false);
    }
}
