package api.adhyay.app.videocall.videocallservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class IncomingVideoCallBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serviceIntent = new Intent(VideoCallServices.class.getName());
        context.startService(serviceIntent);
    }
}
