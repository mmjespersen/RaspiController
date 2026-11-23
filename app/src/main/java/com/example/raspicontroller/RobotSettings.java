package com.example.raspicontroller;

import android.content.Context;
import android.content.SharedPreferences;

/** Persists the robot's drive WebSocket URL on the device. */
public final class RobotSettings {

    public static final String DEFAULT_DRIVE_URL = "ws://robotpi:8000/ws/drive";

    private static final String PREFS = "robot_settings";
    private static final String KEY_DRIVE_URL = "drive_url";

    private RobotSettings() {}

    public static String getDriveUrl(Context context) {
        return prefs(context).getString(KEY_DRIVE_URL, DEFAULT_DRIVE_URL);
    }

    public static void setDriveUrl(Context context, String url) {
        prefs(context).edit().putString(KEY_DRIVE_URL, url.trim()).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
