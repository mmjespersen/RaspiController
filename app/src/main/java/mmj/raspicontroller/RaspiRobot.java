package mmj.raspicontroller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Drives the real robot over the robot-api WebSocket (/ws/drive).
 *
 * Protocol (see robot-api app/routes/ws.py):
 *   send {"drive": d, "direction": r}   d, r in -100..100 (up = forward, right = positive direction)
 *   send {"stop": true}                 emergency stop
 *   recv {"motor1": m1, "motor2": m2} / {"stopped": true} / {"error": "..."}
 *
 * The robot has a 1 s watchdog, so the current drive/direction is re-sent every
 * SEND_INTERVAL_MS while connected, even when the stick is held still.
 * If the connection drops, the robot ramps to a stop by itself; we reconnect automatically.
 */
public class RaspiRobot implements RobotController {

    private static final String TAG = "RaspiRobot";
    private static final long SEND_INTERVAL_MS = 100;
    private static final long RECONNECT_DELAY_MS = 2000;

    private final String url;
    private final TextView statusView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final OkHttpClient client = new OkHttpClient.Builder()
            .dns(new MdnsDns())   // lets "robotpi" resolve on Android (see MdnsDns)
            .connectTimeout(3, TimeUnit.SECONDS)
            .pingInterval(5, TimeUnit.SECONDS)
            .build();

    private volatile int drive = 0;
    private volatile int direction = 0;

    private ScheduledExecutorService scheduler;
    private volatile WebSocket webSocket;
    private volatile boolean connected = false;
    private volatile boolean running = false;
    private String lastStatus = "";

    /** @param url e.g. "ws://robotpi:8000/ws/drive" (must be valid, see {@link #isValidUrl}) */
    public RaspiRobot(String url, TextView statusView) {
        this.url = url;
        this.statusView = statusView;
    }

    /** True if {@code url} is a usable ws:// or wss:// URL. */
    public static boolean isValidUrl(String url) {
        if (url == null) return false;
        String u = url.trim().toLowerCase(java.util.Locale.ROOT);
        String httpUrl;
        if (u.startsWith("ws://")) httpUrl = "http://" + url.trim().substring(5);
        else if (u.startsWith("wss://")) httpUrl = "https://" + url.trim().substring(6);
        else return false;
        return okhttp3.HttpUrl.parse(httpUrl) != null;
    }

    public String getUrl() {
        return url;
    }

    /** Open the connection and start sending. Call from onStart/onResume. */
    public synchronized void connect() {
        if (running) return;
        running = true;
        drive = 0;
        direction = 0;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(this::sendCurrent, SEND_INTERVAL_MS, SEND_INTERVAL_MS, TimeUnit.MILLISECONDS);
        openSocket();
    }

    /** Stop the robot and close the connection. Call from onStop/onPause. */
    public synchronized void disconnect() {
        if (!running) return;
        running = false;
        WebSocket ws = webSocket;
        if (ws != null) {
            ws.send("{\"stop\": true}");
            ws.close(1000, "app closed");
        }
        webSocket = null;
        connected = false;
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        showStatus("Disconnected");
    }

    private void openSocket() {
        if (!running) return;
        showStatus("Connecting to " + url + " ...");
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                connected = true;
                showStatus("Connected to " + url);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                // e.g. {"motor1": 70, "motor2": 90}
                showStatus("Robot: " + text);
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                handleLost(ws, "closed (" + code + ")");
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.w(TAG, "WebSocket failure for " + url, t);
                handleLost(ws, "to " + url + " failed: " + t.getMessage());
            }
        });
    }

    private void handleLost(WebSocket ws, String why) {
        if (ws != webSocket) return;   // an old socket we already replaced/closed
        connected = false;
        webSocket = null;
        if (!running) return;
        showStatus("Connection " + why + " - retrying...");
        ScheduledExecutorService s = scheduler;
        if (s != null && !s.isShutdown()) {
            s.schedule(this::openSocket, RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void sendCurrent() {
        WebSocket ws = webSocket;
        if (ws == null || !connected) return;
        ws.send("{\"drive\": " + drive + ", \"direction\": " + direction + "}");
    }

    private void showStatus(String text) {
        mainHandler.post(() -> {
            if (!text.equals(lastStatus)) {
                lastStatus = text;
                statusView.setText(text);
            }
        });
    }

    private static int clamp(int v) {
        return Math.max(-100, Math.min(100, v));
    }

    // --- RobotController ---

    /** Forward (+) / reverse (-) speed, -100..100. */
    @Override
    public void Move(int speed) {
        drive = clamp(speed);
    }

    /** Emergency stop: immediate, no ramp. */
    @Override
    public void Break() {
        drive = 0;
        direction = 0;
        WebSocket ws = webSocket;
        if (ws != null) ws.send("{\"stop\": true}");
    }

    /** Left (-) / right (+) direction, -100..100. */
    @Override
    public void Turn(int direction) {
        this.direction = clamp(direction);
    }

    @Override
    public void CameraSwitch() {
    }

    @Override
    public void ModeSwitch() {
    }
}
