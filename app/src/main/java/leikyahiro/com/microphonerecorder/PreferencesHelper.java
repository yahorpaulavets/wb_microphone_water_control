package leikyahiro.com.microphonerecorder;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Yahor on 20.03.2016.
 * (C) All rights reserved.
 */
public class PreferencesHelper {
    private static final String SERVER_IP_KEY = "SERVER_IP_KEY";

    public static String getIp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                "leikyahiro.com.microphonerecorder", Context.MODE_PRIVATE);

        return prefs.getString(SERVER_IP_KEY, null);
    }

    public static void setServerIpKey(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(
                "leikyahiro.com.microphonerecorder", Context.MODE_PRIVATE);

        prefs.edit().putString(SERVER_IP_KEY, key).commit();
    }
}
