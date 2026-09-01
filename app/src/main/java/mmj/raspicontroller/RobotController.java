package mmj.raspicontroller;

public interface RobotController {
    void Move(int speed);
    void Break();
    void Turn(int direction);

    void CameraSwitch();

    void ModeSwitch();

}
