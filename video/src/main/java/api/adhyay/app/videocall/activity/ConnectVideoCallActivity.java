package api.adhyay.app.videocall.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import api.adhyay.app.libs.AppSocketListener;
import api.adhyay.app.videocall.R;

public class ConnectVideoCallActivity extends AppCompatActivity{

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_video_call);


        AppSocketListener.getInstance().newEventHandler();

      //  startActivity(new Intent(ConnectVideoCallActivity.this, VideoCallActivity.class));
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                startActivity(new Intent(ConnectVideoCallActivity.this, VideoCallActivity.class));
            }
        }, 0);

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppSocketListener.getInstance().setAppConnectedToService(false);
        AppSocketListener.getInstance().disconnect();
     }
    }
