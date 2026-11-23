package mmj.raspicontroller;
import android.util.Log;
import android.widget.TextView;

public class RP5GameControl implements GameControllerEventListener {

    private final TextView logMessageView;
    private RobotController robot;
    // --- Implement your game responses ---

    public RP5GameControl(TextView logMessageView, RobotController robot){
        this.logMessageView = logMessageView;
        this.robot = robot;
    }
    /** Left stick drives the robot: up/down = forward/reverse, left/right = direction. */
    @Override public void onLeftStickMove(float x, float y) {
        Log.d("Game", "Left Stick: " + x + "," + y);
        // Android Y axis is -1 at the top, so invert it: stick up = drive forward
        this.robot.Move(Math.round(-y * 100));
        this.robot.Turn(Math.round(x * 100));
    }
    @Override public void onRightStickMove(float x, float y) {
        Log.d("Game", "Right Stick: " + x + "," + y);
    }
    @Override public void onLeftStickClick(boolean pressed) {
        Log.d("Game", "Left Click: " + pressed);
    }
    @Override public void onRightStickClick(boolean pressed) {
        Log.d("Game", "Right Click: " + pressed);
    }
    @Override public void onDpadMove(int x, int y) {
        Log.d("Game", "D-Pad: " + x + "," + y);
    }
    @Override public void onL1(boolean pressed) {
        Log.d("Game", "L1: " + pressed);
    }
    @Override public void onR1(boolean pressed) {
        Log.d("Game", "R1: " + pressed);
    }

    @Override
    public void onL2(boolean pressed) {
        if (pressed){
            Log.d("Game", "L2: TRUE");
        }
        else {
            Log.d("Game", "L2: FALSE");
        }
    }

    @Override public void onL2_pressure_change(float pressure) {
        // Driving is done with the left stick (onLeftStickMove)
        Log.d("Game", "L2: " + pressure);
    }

    @Override
    public void onR2(boolean pressed) {
        Log.d("Game", "R2: " + pressed);
    }

    @Override public void onR2_pressure_change(float pressure) {
        Log.d("Game", "R2: " + pressure);
    }
    @Override public void onButtonA(boolean pressed) {
        Log.d("Game", "A: " + pressed);
    }
    @Override public void onButtonB(boolean pressed) {
        Log.d("Game", "B: " + pressed);
    }
    @Override public void onButtonX(boolean pressed) {
        Log.d("Game", "X: " + pressed);
    }
    @Override public void onButtonY(boolean pressed) {
        Log.d("Game", "Y: " + pressed);
    }
    @Override public void onStartButton(boolean pressed) {
        Log.d("Game", "Start: " + pressed);
    }
    @Override public void onSelectButton(boolean pressed) {
        Log.d("Game", "Select: " + pressed);
    }
    
}
