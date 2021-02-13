package com.bitbybit.qrscanner;

import android.content.Context;

class Urls {

    private static Urls urls;
    public String url = "https://" + getCode() + ".ngrok.io/app/";
    //public String url = "http://127.0.0.1:5000/app/";
    public String qr_url = url + "qrscan";
    private Context context;

    private Urls(Context context) {
        this.context = context;
    }

    public static synchronized Urls getInstance(Context context) {

        if (urls == null) {
            urls = new Urls(context);
        }
        return urls;
    }

    private String getCode() {
        return Shared_prefs.getInstance(context).get_ip();
    }
}
