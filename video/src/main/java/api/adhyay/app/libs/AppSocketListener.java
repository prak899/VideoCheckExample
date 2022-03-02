package api.adhyay.app.libs;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import api.adhyay.app.videocall.SocketIOConnection.SocketConstants;
import api.adhyay.app.videocall.SocketIOConnection.SocketListener;
import api.adhyay.app.videocall.activity.IncomingVideoCallActivity;
import api.adhyay.app.videocall.videocallservices.VideoCallServices;
import io.socket.client.Ack;
import io.socket.emitter.Emitter;

public class AppSocketListener implements SocketListener {
 private static AppSocketListener sharedInstance;
 private VideoCallServices socketServiceInterface;
 public SocketListener activeSocketListener;

 public void setActiveSocketListener(SocketListener activeSocketListener) {
  this.activeSocketListener = activeSocketListener;
  if (socketServiceInterface != null && socketServiceInterface.isSocketConnected()){
   onSocketConnected();
  }
 }

 public static AppSocketListener getInstance(){
  if (sharedInstance==null){
   sharedInstance = new AppSocketListener();
  }
  return sharedInstance;
 }

 private ServiceConnection serviceConnection = new ServiceConnection() {
  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
   socketServiceInterface = ((VideoCallServices.LocalBinder)service).getService();
   socketServiceInterface.setServiceBinded(true);
   socketServiceInterface.setSocketListener(sharedInstance);
   if (socketServiceInterface.isSocketConnected()){
    onSocketConnected();
   }

  }

  @Override
  public void onServiceDisconnected(ComponentName name) {

   socketServiceInterface.setServiceBinded(false);
   socketServiceInterface=null;
   onSocketDisconnected();
  }
 };


 public void initialize(){
  Intent intent = new Intent(AppContext.getAppContext(), VideoCallServices.class);
  AppContext.getAppContext().startService(intent);
  AppContext.getAppContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
  LocalBroadcastManager.getInstance(AppContext.getAppContext()).
          registerReceiver(socketConnectionReceiver, new IntentFilter(SocketConstants.
                  socketConnection));
  LocalBroadcastManager.getInstance(AppContext.getAppContext()).
          registerReceiver(connectionFailureReceiver, new IntentFilter(SocketConstants.
                  connectionFailure));
  LocalBroadcastManager.getInstance(AppContext.getAppContext()).
          registerReceiver(incominCall, new IntentFilter(SocketConstants.
                  incomingCall));
 }

 private BroadcastReceiver socketConnectionReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
   boolean connected = intent.getBooleanExtra("connectionStatus",false);
   if (connected){
    Log.i("AppSocketListener","Socket connected");
    onSocketConnected();
   }
   else{
    onSocketDisconnected();
   }
  }
 };

 private BroadcastReceiver connectionFailureReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
   Toast toast = Toast.
           makeText(AppContext.getAppContext(), "Please check your network connection",
                   Toast.LENGTH_SHORT);
   toast.show();
  }
 };

 private BroadcastReceiver incominCall = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {

   String roomId = intent.getStringExtra("roomId");
   String roomPassword = intent.getStringExtra("roomPassword");
   String clientId = intent.getStringExtra("clientId");
   intent.getStringExtra(TelephonyManager.EXTRA_STATE_RINGING);

   SocketConstants.RoomId = roomId;
   SocketConstants.RoomPassword = roomPassword;

   Toast.makeText(AppContext.getAppContext(),"Incoming call"+roomId+" "+roomPassword+ " "+clientId,Toast.LENGTH_LONG).show();

   intent = new Intent(context, IncomingVideoCallActivity.class);
   intent.putExtra("callType", "incoming");
   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
   context.startActivity(intent);

   JSONObject object = new JSONObject();

   try {
    object.put("roomId", roomId);
    object.put("roomPassword", roomPassword);
    object.put("clientId", clientId);

    onIncomingCall(object);
    
   } catch (JSONException e) {
    e.printStackTrace();
   }

  }
 };

 public void destroy(){
  socketServiceInterface.setServiceBinded(false);
  AppContext.getAppContext().unbindService(serviceConnection);
  LocalBroadcastManager.getInstance(AppContext.getAppContext()).
          unregisterReceiver(socketConnectionReceiver);
  LocalBroadcastManager.getInstance(AppContext.getAppContext()).
          unregisterReceiver(incominCall);
 }

 @Override
 public void onSocketConnected() {
  if (activeSocketListener != null) {
   activeSocketListener.onSocketConnected();
  }
 }

 @Override
 public void onSocketDisconnected() {
  if (activeSocketListener != null) {
   activeSocketListener.onSocketDisconnected();
  }
 }

 @Override
 public void onIncomingCall(Object... args) {
  if (activeSocketListener != null) {

   JSONObject object = (JSONObject) args[0];
//  Log.d("VideoCallServices", "incomingCall: " + object);

   JSONObject incomingCall = object.optJSONObject("room");

   JSONObject client = object.optJSONObject("client");

   String roomId = incomingCall.optString("id");
   String roomPassword = incomingCall.optString("password");

   SocketConstants.RoomId = roomId;
   SocketConstants.RoomPassword = roomPassword;

   activeSocketListener.onIncomingCall(incomingCall);
  }
 }


 public void addOnHandler(String event, Emitter.Listener listener){
  socketServiceInterface.addOnHandler(event, listener);
 }
 public void emit(String event, Object[] args, Ack ack){
  socketServiceInterface.emit(event, args, ack);
 }

 public void emit (String event,Object... args){
  socketServiceInterface.emit(event, args);
 }

 void connect(){
  socketServiceInterface.connect();
 }

 public void disconnect(){
  socketServiceInterface.disconnect();
 }
 public void off(String event) {
  if (socketServiceInterface != null) {
   socketServiceInterface.off(event);
  }
 }

 public boolean isSocketConnected(){
  if (socketServiceInterface == null){
   return false;
  }
  return socketServiceInterface.isSocketConnected();
 }

 public void setAppConnectedToService(Boolean status){
  if ( socketServiceInterface != null){
   socketServiceInterface.setAppConnectedToService(status);
  }
 }

 public void restartSocket(){
  if (socketServiceInterface != null){
   socketServiceInterface.restartSocket();
  }
 }
 public void newEventHandler(){
  if (socketServiceInterface != null){
   socketServiceInterface.addNewMessageHandler();
  }
 }
}
