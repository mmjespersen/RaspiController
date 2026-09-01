package mmj.raspicontroller;

import android.widget.TextView;

import com.example.raspicontroller.RobotCom;

public class RobotMock implements RobotController{

    private boolean CameraOn = false;
    private final TextView messageTextBox;
    private final RobotCom _robotCom= new RobotCom();

    public RobotMock(TextView messageTextBox){
        this.messageTextBox = messageTextBox;
        this.messageTextBox.setText("Setting text from Mock");
    }

    @Override
    public void Move(int speed) {
        String msg = "Setting speed on robot mock to: " + speed;
        this.messageTextBox.setText(msg);
        _robotCom.OnBlink();

    }

    @Override
    public void Break() {
        String msg = "BREAKING!!!!";
        this.messageTextBox.setText(msg);
    }

    @Override
    public void Turn(int direction) {
        String msg = "Changing direction to something!!!";
        this.messageTextBox.setText(msg);
    }

    @Override
    public void CameraSwitch() {
        if (CameraOn) {
            String msg = "Switching Camera Off!!!";
            this.messageTextBox.setText(msg);
            CameraOn=false;
        }
        else
        {
            String msg = "Switching Camera On!!!";
            this.messageTextBox.setText(msg);
            CameraOn = true;
        }
    }

    @Override
    public void ModeSwitch() {

    }
}
