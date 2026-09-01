package mmj.raspicontroller;
public interface GameControllerEventListener {

    // Analog sticks
    void onLeftStickMove(float x, float y);
    void onRightStickMove(float x, float y);

    // Stick clicks
    void onLeftStickClick(boolean pressed);
    void onRightStickClick(boolean pressed);

    // D-Pad
    void onDpadMove(int directionX, int directionY);

    // Triggers and shoulders
    void onL1(boolean pressed);
    void onR1(boolean pressed);
    void onL2(boolean pressed);
    void onL2_pressure_change(float pressure);
    void onR2(boolean pressed);
    void onR2_pressure_change(float pressure);

    // Face buttons
    void onButtonA(boolean pressed);
    void onButtonB(boolean pressed);
    void onButtonX(boolean pressed);
    void onButtonY(boolean pressed);

    // System buttons
    void onStartButton(boolean pressed);
    void onSelectButton(boolean pressed);
}
