package com.matrixfrats.android_apprtc.testing.stage2;

import android.util.Log;

import java.util.Random;

public class Utils {

    public static String generateRandomId() {
        return generateRandomId(9);
    }

    public static String generateRandomId(int length) {
        if (length <= 0) generateRandomId(9);
        Random rand = new Random();
        String id = "";
        while (length-- > 0) id += String.valueOf(rand.nextInt(9));
        return id;
    }

    public static void trace(String msg) {
        Log.d("ABHI", msg);
    }
}
