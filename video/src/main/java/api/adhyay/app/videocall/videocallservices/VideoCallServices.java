package api.adhyay.app.videocall.videocallservices;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import api.adhyay.app.libs.AppContext;
import api.adhyay.app.videocall.R;
import api.adhyay.app.videocall.SocketIOConnection.SocketConstants;
import api.adhyay.app.videocall.SocketIOConnection.SocketListener;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static api.adhyay.app.videocall.api.CreateRoomConstants.corpId;
import static api.adhyay.app.videocall.api.CreateRoomConstants.serviceKey;
import static api.adhyay.app.videocall.api.CreateRoomConstants.userKey;

public class VideoCallServices extends Service {

    public static final String TAG = VideoCallServices.class.getName();

    private Boolean appConnectedToService;
    private Socket mSocket;
    private SocketListener socketListener;
    private boolean verify=false;
    private boolean serviceBinded = false;
    private boolean alreadyStart = false;

    private final LocalBinder mBinder = new LocalBinder();


    public void setAppConnectedToService(Boolean appConnectedToService) {
        this.appConnectedToService = appConnectedToService;
    }

    public void setSocketListener(SocketListener socketListener) {
        this.socketListener = socketListener;
    }



    public class LocalBinder extends Binder {
        public VideoCallServices getService() {
            return VideoCallServices.this;
        }
    }

    public void setServiceBinded(boolean serviceBinded) {
        this.serviceBinded = serviceBinded;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(!alreadyStart) {

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    initializeSocket();
                    addSocketHandlers();
                }
            });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags,startId);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
//        closeSocketSession();
//        stopForeground(true);
//        stopSelf();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return serviceBinded;
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }


    private void initializeSocket() {
        try{
            IO.Options options = new IO.Options();
            options.forceNew = true;
            mSocket = IO.socket(SocketConstants.SOCKET_URI);
        }
        catch (Exception e){
            Log.e("Error", "Exception in socket creation");
            throw new RuntimeException(e);
        }
    }

    private void closeSocketSession(){
        mSocket.disconnect();
        mSocket.off();
    }

    private void addSocketHandlers(){

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent("socket.connect");
                intent.putExtra("connectionStatus", true);
                broadcastEvent(intent);
            }
        });

        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent("socket.connection");
                intent.putExtra("connectionStatus", false);
                broadcastEvent(intent);
            }
        });


        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent = new Intent("failedConnect");
                broadcastEvent(intent);
            }
        });


        mSocket.connect();

        connectApi();
        addNewMessageHandler();

        alreadyStart = true;
    }

    public void connectApi(){
        mSocket.emit("connect-api","connectApi");
        credentialsCheck();
    }

    private void credentialsCheck() {

        JSONObject object = new JSONObject();
        try {
            object.put("id","1");
            object.put("key", userKey);
            object.put("name", "Abcd");
            object.put("dp", " Unavailable");
            object.put("corpId", corpId);
            object.put("serviceKey", serviceKey);
            mSocket.emit("credentials-check", object);
            verify = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void addNewMessageHandler(){
        mSocket.off("incoming-call");
        mSocket.on("incoming-call", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                JSONObject object = (JSONObject) args[0];

                JSONObject incomingCall = object.optJSONObject("room");

                JSONObject client = object.optJSONObject("client");

                String clientId = client.optString("id");

                String roomId = incomingCall.optString("id");
                String roomPassword = incomingCall.optString("password");

                //api.adhyay.app.videocall
                if (isForeground("api.adhyay.app.videocall")) {

                    Intent intent = new Intent(SocketConstants.incomingCall);
                    intent.putExtra("clientId", clientId);
                    intent.putExtra("roomId", roomId);
                    intent.putExtra("roomPassword", roomPassword);
                    intent.putExtra(TelephonyManager.EXTRA_STATE_RINGING, "Ankush");


                    broadcastEvent(intent);

                } else {
                    showNotificaitons(clientId, roomId, roomPassword);
                }
            }
        });
    }

    public void removeMessageHandler() {
        mSocket.off("credentials-check");
    }

    public void emit(String event, Object[] args, Ack ack){
        mSocket.emit(event, args, ack);
    }
    public void emit (String event,Object... args) {
        try {
            mSocket.emit(event, args,null);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addOnHandler(String event, Emitter.Listener listener){
        mSocket.on(event, listener);
    }

    public void connect(){
        mSocket.connect();
    }

    public void disconnect(){
        mSocket.disconnect();
    }

    public void restartSocket(){
        mSocket.off();
        mSocket.disconnect();
        addSocketHandlers();
    }

    public void off(String event){
        mSocket.off(event);
    }

    private void broadcastEvent(Intent intent){
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public boolean isSocketConnected(){
        if (mSocket == null){
            return false;
        }
        return mSocket.connected();
    }

    public void showNotificaitons(String clientId, String roomId, String roomPassword){
        Intent toLaunch = new Intent(getApplicationContext(), VideoCallServices.class);
        toLaunch.putExtra("clientId", clientId);
        toLaunch.putExtra("roomId", roomId);
        toLaunch.putExtra("roomPassword", roomPassword);
        toLaunch.setAction("android.intent.action.MAIN");
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0,toLaunch,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n  = new NotificationCompat.Builder(getApplicationContext(), AppContext.CHANNEL_ID)
                .setContentTitle("You have pending new messages")
                .setContentText("New Message")
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        notificationManager.notify(0, n);
    }

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }
}
