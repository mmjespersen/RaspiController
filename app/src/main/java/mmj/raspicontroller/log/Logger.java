package mmj.raspicontroller.log;

public interface Logger {

    void LogMessage(String heading, String message);
    void LogMessage(String message);
}
