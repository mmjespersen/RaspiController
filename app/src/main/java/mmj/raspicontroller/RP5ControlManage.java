package mmj.raspicontroller;

import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * Handles all Android controller input and dispatches clean, high-level events to GameControllerListener.
 * Supports analog sticks, triggers, D-pad, face buttons, and system buttons.
 */
public class RP5ControlManage {

    private static final String TAG = "GameController";
    private static final float STICK_DEADZONE = 0.25f;
    private static final float TRIGGER_DEADZONE = 0.05f;

    private final GameControllerEventListener listener;

    public RP5ControlManage(GameControllerEventListener listener) {
        this.listener = listener;
    }

    // --------------------------------------------------------------
    // Handle Button and Key Events
    // --------------------------------------------------------------
    public boolean handleKeyEvent(KeyEvent event) {
        if (listener == null) return false;

        boolean pressed = event.getAction() == KeyEvent.ACTION_DOWN;
        int code = event.getKeyCode();

        switch (code) {
            // Stick clicks
            case KeyEvent.KEYCODE_BUTTON_THUMBL:
                listener.onLeftStickClick(pressed); return true;
            case KeyEvent.KEYCODE_BUTTON_THUMBR:
                listener.onRightStickClick(pressed); return true;

            // Shoulder buttons (digital)
            case KeyEvent.KEYCODE_BUTTON_L1:
                listener.onL1(pressed); return true;
            case KeyEvent.KEYCODE_BUTTON_R1:
                listener.onR1(pressed); return true;
            case KeyEvent.KEYCODE_BUTTON_L2:
                listener.onL2(pressed); return true;
            case KeyEvent.KEYCODE_BUTTON_R2:
                listener.onR2(pressed); return true;
            // Face buttons
            case KeyEvent.KEYCODE_BUTTON_A:
                listener.onButtonA(pressed); return true;
            case KeyEvent.KEYCODE_BUTTON_B:
                listener.onButtonB(pressed); return true;
            case KeyEvent.KEYCODE_BUTTON_X:
                listener.onButtonX(pressed); return true;
            case KeyEvent.KEYCODE_BUTTON_Y:
                listener.onButtonY(pressed); return true;

            // System / meta buttons
            case KeyEvent.KEYCODE_BUTTON_START:
            case KeyEvent.KEYCODE_BUTTON_MODE:
                listener.onStartButton(pressed); return true;
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                listener.onSelectButton(pressed); return true;

            default:
                Log.d(TAG, "Unhandled key: " + KeyEvent.keyCodeToString(code));
                return false;
        }
    }

    float _oldL2 =0;
    float _oldR2 =0;
    // --------------------------------------------------------------
    // Handle Analog Motion Events (sticks, triggers, D-pad)
    // --------------------------------------------------------------
    public boolean handleMotionEvent(MotionEvent event) {
        if (listener == null) return false;

        // Left stick
        float lx = event.getAxisValue(MotionEvent.AXIS_X);
        float ly = event.getAxisValue(MotionEvent.AXIS_Y);
        if (Math.abs(lx) < STICK_DEADZONE) lx = 0;
        if (Math.abs(ly) < STICK_DEADZONE) ly = 0;
        if (lx != 0 || ly != 0)
            listener.onLeftStickMove(lx, ly);

        // Right stick
        float rx = event.getAxisValue(MotionEvent.AXIS_Z);
        float ry = event.getAxisValue(MotionEvent.AXIS_RZ);
        if (Math.abs(rx) < STICK_DEADZONE) rx = 0;
        if (Math.abs(ry) < STICK_DEADZONE) ry = 0;
        if (rx != 0 || ry != 0)
            listener.onRightStickMove(rx, ry);

        // D-pad (hat switch)
        float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
        float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
        if (hatX != 0 || hatY != 0)
            listener.onDpadMove((int) hatX, (int) hatY);

        // Triggers (analog)
        float l2 = event.getAxisValue(MotionEvent.AXIS_BRAKE);
        float r2 = event.getAxisValue(MotionEvent.AXIS_GAS);

        boolean l2Pressed = l2 > TRIGGER_DEADZONE;
        boolean r2Pressed = r2 > TRIGGER_DEADZONE;

        if (l2 != _oldL2) {
            listener.onL2_pressure_change(l2);
            _oldL2=l2;
        }
        if (r2 != _oldR2) {
            listener.onR2_pressure_change(r2);
            _oldR2=r2;
        }
        return true;
    }

    // --------------------------------------------------------------
    // Optional: helper for debugging controller capabilities
    // --------------------------------------------------------------
    public static void logControllerAxes(InputDevice device) {
        Log.d(TAG, "Device: " + device.getName());
        for (InputDevice.MotionRange range : device.getMotionRanges()) {
            Log.d(TAG, "Axis: " + MotionEvent.axisToString(range.getAxis()) +
                    " | Range: " + range.getMin() + "–" + range.getMax());
        }
    }

    public void GetAllAxis(MotionEvent event){
        InputDevice device = event.getDevice();

        if (device == null) return ;
        ArrayList<String> axisList = new ArrayList<String>();

        for (InputDevice.MotionRange range : device.getMotionRanges()) {
            float value = event.getAxisValue(range.getAxis());
            if (Math.abs(value) > 0.01f) {
                String x = MotionEvent.axisToString(range.getAxis()) + " = " + value;
                Log.d("AxesDebug", x);
                axisList.add(x);
            }
        }

        Integer c = axisList.size();
    }

}