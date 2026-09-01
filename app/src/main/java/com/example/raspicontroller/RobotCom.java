package com.example.raspicontroller;

import java.net.HttpURLConnection;
import java.net.URL;

public class RobotCom {
    private boolean is_on = false;
    public void OnBlink(){
        String turn_on = "http://192.168.0.118:8000/api/blink";

        String turn_off = "http://192.168.0.118:8000/api/stop-blink";

        if (!Is_on()){
            new Thread(() -> {
                boolean success = sendPost(turn_on);;  // your POST method
            }).start();
            Is_on(true);

        }
        else{
            new Thread(() -> {
                boolean success = sendPost(turn_off);;  // your POST method
            }).start();
            Is_on(false);
        }

    }

    public static boolean sendPost(String urlString) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Optional: if your API expects JSON body
            // String jsonInputString = "{}";
            // try(OutputStream os = conn.getOutputStream()) {
            //     byte[] input = jsonInputString.getBytes("utf-8");
            //     os.write(input, 0, input.length);
            // }

            int responseCode = conn.getResponseCode();
            return (responseCode >= 200 && responseCode < 300);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public boolean Is_on() {
        return is_on;
    }

    public void Is_on(boolean is_on) {
        this.is_on = is_on;
    }
}
