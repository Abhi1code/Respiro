package com.bitbybit.respiro.utils;

public class Data {

    private static Data data;
    public static final String url = "http://db705aeb71a8.ngrok.io/app/";
    public static final String authcheck_url = url + "signin";
    public static final String signup_url = url + "signup";
    public static final String pefvst = url + "pefvst";
    public static final String fvcvst = url + "fvcvst";
    public static final String fevvst = url + "fevvst";
    public static final String lhsvst = url + "lhsvst";
    public static final String getHistory = url + "gettime";
    public static final String getData = url + "getdata";
    public static final String upload_url = url + "upload";

    private long time;
    private int code;
    private boolean flag = true;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    private Data() {
    }

    public static synchronized Data getInstance() {

        if (data == null) {
            data = new Data();
        }
        return data;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
