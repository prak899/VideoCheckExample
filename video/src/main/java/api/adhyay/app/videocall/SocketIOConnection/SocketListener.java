package api.adhyay.app.videocall.SocketIOConnection;

public interface SocketListener {

    void onSocketConnected();
    void onSocketDisconnected();
    void onIncomingCall(Object... args);
}
