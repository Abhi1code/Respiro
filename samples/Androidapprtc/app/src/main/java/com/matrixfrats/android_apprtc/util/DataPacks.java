package com.matrixfrats.android_apprtc.util;

public class DataPacks {

    public static class LoginDataPack{
        public boolean status = false;
        public String name = "";
    }

    public static class OfferListenerDataPack{
        public String des = "";
    }

    public static class RemoteCandidateDataPack{
        public String candidate = "";
        public int sdpMLineIndex = 0;
        public String sdpMid = "";
    }

    public static class AnswerListenerDataPack{
        public String des = "";
    }

    public static class LocalCandidateDataPack{
        public String candidate = "";
        public int sdpMLineIndex = 0;
        public String sdpMid = "";
    }
}
