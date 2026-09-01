package mmj.raspicontroller.log;

public class DummyLog implements Logger{
    @Override
    public void LogMessage(String heading, String message) {
        String x = heading;
        String y = message;

    }

    @Override
    public void LogMessage(String message) {
        LogMessage("LogMessage", message);
    }
}
