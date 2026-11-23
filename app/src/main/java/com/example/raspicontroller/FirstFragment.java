package com.example.raspicontroller;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.raspicontroller.databinding.FragmentFirstBinding;

import java.net.HttpURLConnection;
import java.net.URL;

import mmj.raspicontroller.RP5GameControl;
import mmj.raspicontroller.RP5ControlManage;
import mmj.raspicontroller.RaspiRobot;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    RP5GameControl _eventHandler;
    RP5ControlManage _contollerManager;
    RaspiRobot _robot;

    RobotCom _robotCom = new RobotCom();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView msgView = binding.textviewFirst;
        _robot = new RaspiRobot(RobotSettings.getDriveUrl(requireContext()), msgView);

        _eventHandler = new RP5GameControl(msgView, _robot);
        _contollerManager = new RP5ControlManage(_eventHandler);

        binding.buttonFirst.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );

        binding.btnBlink.setOnClickListener(v-> _robotCom.OnBlink());
    }

    /** Called after the URL was changed in Settings: reconnect to the new address. */
    public void applyRobotUrl(String url) {
        if (binding == null) return;   // view not created; the new URL is used next time
        if (_robot != null) _robot.disconnect();
        _robot = new RaspiRobot(url, binding.textviewFirst);
        _eventHandler = new RP5GameControl(binding.textviewFirst, _robot);
        _contollerManager = new RP5ControlManage(_eventHandler);
        if (getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
            _robot.connect();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (_robot != null) _robot.connect();
    }

    /** Leaving the screen or the app stops the robot and closes the WebSocket. */
    @Override
    public void onStop() {
        if (_robot != null) _robot.disconnect();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return _contollerManager.handleKeyEvent(event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return _contollerManager.handleKeyEvent(event);
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        _contollerManager.GetAllAxis(event);
        return _contollerManager.handleMotionEvent(event);

    }

    boolean is_on = false;
    public void OnBlinkX(){
        String turn_on = "http://192.168.0.118:8000/api/blink";

        String turn_off = "http://192.168.0.118:8000/api/stop-blink";

        if (!is_on){
            new Thread(() -> {
                boolean success = sendPost(turn_on);;  // your POST method
            }).start();
            is_on=true;

        }
        else{
            new Thread(() -> {
                boolean success = sendPost(turn_off);;  // your POST method
            }).start();
            is_on=false;
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
}

