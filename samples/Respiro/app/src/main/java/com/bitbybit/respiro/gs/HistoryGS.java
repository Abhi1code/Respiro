package com.bitbybit.respiro.gs;

public class HistoryGS {

    private long time;
    private double pef, fvc;

    public HistoryGS(long time, double pef, double fvc) {
        this.time = time;
        this.pef = pef;
        this.fvc = fvc;
    }

    public long getTime() {
        return time;
    }

    public double getPef() {
        return pef;
    }

    public double getFvc() {
        return fvc;
    }
}
