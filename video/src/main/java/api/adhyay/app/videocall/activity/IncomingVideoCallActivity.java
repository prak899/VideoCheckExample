package api.adhyay.app.videocall.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import api.adhyay.app.videocall.R;

public class IncomingVideoCallActivity extends AppCompatActivity {

    private ImageButton acceptCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_video_call);

        acceptCall = findViewById(R.id.accept_call);

        acceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IncomingVideoCallActivity.this, VideoCallActivity.class);
                intent.putExtra("callType","incoming");
                startActivity(intent);
            }
        });

      }
    }