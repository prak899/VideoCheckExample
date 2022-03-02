package api.adhyay.app.libs;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import org.mediasoup.droid.Logger;
import org.mediasoup.droid.MediasoupClient;

public class AppContext extends Application {

    private static Context context;
    public static final String CHANNEL_ID = "autoStartServiceChannel";
    public static final String CHANNEL_NAME = "Auto Start Service Channel";

    public void onCreate(){
        super.onCreate();
        AppContext.context = getApplicationContext();
        createNotificationChannel();
        Logger.setLogLevel(Logger.LogLevel.LOG_DEBUG);
        Logger.setDefaultHandler();
        initializeSocket();
        MediasoupClient.initialize(this);
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public static Context getAppContext() {
        return AppContext.context;
    }


    public void initializeSocket(){
        AppSocketListener.getInstance().initialize();
    }

    public void destroySocketListener(){
        AppSocketListener.getInstance().destroy();
    }


}
