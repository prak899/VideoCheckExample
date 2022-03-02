package api.adhyay.app.videocall.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import api.adhyay.app.libs.PeerConnectionUtils;
import api.adhyay.app.libs.RoomClient;
import api.adhyay.app.libs.RoomOptions;
import api.adhyay.app.libs.RoomProps;
import api.adhyay.app.libs.RoomStore;
import api.adhyay.app.videocall.R;
import api.adhyay.app.videocall.RetrofitClient.RetrofitClient;
import api.adhyay.app.videocall.adapter.PeerVideoAdapter;
import api.adhyay.app.videocall.api.CreateRoomConstants;
import api.adhyay.app.videocall.databinding.ActivityVideoCallBinding;
import api.adhyay.app.videocall.model.CreateRoomResponse;
import api.adhyay.app.videocall.model.Me;
import api.adhyay.app.videocall.model.Peer;
import api.adhyay.app.videocall.videocallservices.VideoCallServices;
import api.adhyay.app.videocall.vm.EdiasProps;
import api.adhyay.app.videocall.vm.MeProps;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static api.adhyay.app.videocall.SocketIOConnection.SocketConstants.RoomId;
import static api.adhyay.app.videocall.SocketIOConnection.SocketConstants.RoomPassword;


import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;


public class VideoCallActivity extends AppCompatActivity {

    private static final String TAG = VideoCallActivity.class.getName();

    private RoomClient roomClient;

    private PeerVideoAdapter mPeerAdapter;
    private RoomStore mRoomStore;

    private ActivityVideoCallBinding mBinding;
    private SharedPreferences preferences;

    public static final int RequestPermissionCode = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_video_call);

        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.setSpeakerphoneOn(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink_anim);
//        mBinding.connectingAnim.startAnimation(animation);

        roomConfig();

        if (CheckingPermissionIsEnabledOrNot()) {
            if (getIntent().getStringExtra("callType") != null && getIntent().getStringExtra("callType").equals("incoming")) {
                roomClient = new RoomClient(VideoCallActivity.this, mRoomStore);
                roomClient.connectCall();
                initialize();
            } else {
                createRoom();
            }

        } else {
            //Calling method to enable permission.
            RequestMultiplePermission();

        }
    }

    private void roomConfig() {

        mRoomStore = new RoomStore();
        getViewModelStore().clear();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        RoomOptions mOptions = new RoomOptions();

        mOptions.setCamProduce(true);

        mOptions.setMicProducer(true);

        mOptions.setConsume(preferences.getBoolean("consume", true));

        String camera = preferences.getString("camera", "front");
        PeerConnectionUtils.setPreferCameraFace(camera);

    }


    private void initialize() {

        EdiasProps.Factory factory = new EdiasProps.Factory(getApplication(), mRoomStore);

        // Room.
        RoomProps roomProps = ViewModelProviders.of(this, factory).get(RoomProps.class);
        roomProps.connect(this);

        MeProps meProps = ViewModelProviders.of(this, factory).get(MeProps.class);
        meProps.connect(this);
        mBinding.me.setProps(meProps);
        mBinding.setMeProps(meProps);

        mBinding.micMute.setOnClickListener(
                view -> {
                    if (MeProps.DeviceState.ON.equals(meProps.getMicState().get())) {
                        roomClient.muteMic();
                    } else {
                        roomClient.unmuteMic();
                    }
                });

        mBinding.muteAudio.setOnClickListener(
                v -> {
                    Me me = meProps.getMe().get();
                    if (me != null) {
                        if (me.isAudioMuted()) {
                            roomClient.unmuteAudio();
                        } else {
                            roomClient.muteAudio();
                        }
                    }
                });

        mBinding.videoOn.setOnClickListener(
                view -> {
                    if (MeProps.DeviceState.ON.equals(meProps.getCamState().get())) {
                        roomClient.disableCam();
                    } else {
                        roomClient.enableCam();
                    }
                });

        mBinding.leaveCall.setOnClickListener(view -> {
            roomClient.leaveCall();
            startActivity(new Intent(VideoCallActivity.this, ConnectVideoCallActivity.class));
            finish();
        });

        mBinding.cameraFlipIB.setOnClickListener(
                view -> roomClient.changeCam());

        mPeerAdapter = new PeerVideoAdapter(mRoomStore, this);

        mBinding.remotePeers.setAdapter(mPeerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

        mRoomStore
                .getPeers()
                .observe(
                        this,
                        peers -> {
                            List<Peer> peersList = peers.getAllPeers();
                            int userSize = peersList.size();
                            if (peersList.isEmpty()) {
                                mBinding.remotePeers.setVisibility(View.GONE);
                            } else {
                                mBinding.remotePeers.setVisibility(View.VISIBLE);

                                if(userSize<4) {
                                    mBinding.remotePeers.setLayoutManager(layoutManager);
                                }
                                else{
                                    mBinding.remotePeers.setLayoutManager(gridLayoutManager);
                                }

                            }
                            mPeerAdapter.replacePeers(peersList);
                        });
    }


    private void createRoom() {

        Call<CreateRoomResponse> call = RetrofitClient.getInstance().getApi().
                getCreateRoomResponse(CreateRoomConstants.corpId, CreateRoomConstants.serviceKey,
                        "1", CreateRoomConstants.userKey, CreateRoomConstants.type);

        call.enqueue(new Callback<CreateRoomResponse>() {
            @Override
            public void onResponse(Call<CreateRoomResponse> call, Response<CreateRoomResponse> response) {

                if (response.isSuccessful()) {

                    RoomId = response.body().getId();
                    RoomPassword = response.body().getPassword();
                    roomClient = new RoomClient(getApplicationContext(), mRoomStore);
                    roomClient.call();
                    initialize();
                }
            }

            @Override
            public void onFailure(Call<CreateRoomResponse> call, Throwable t) {
                Toast.makeText(VideoCallActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void RequestMultiplePermission() {

        // Creating String Array with Permissions.
        ActivityCompat.requestPermissions(VideoCallActivity.this, new String[]
                {
                        CAMERA,
                        RECORD_AUDIO,
                }, RequestPermissionCode);

    }

    public boolean CheckingPermissionIsEnabledOrNot() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {

                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordAudioPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (CameraPermission && RecordAudioPermission) {

                        if (getIntent().getStringExtra("callType") != null && getIntent().getStringExtra("callType").equals("incoming")) {
                            roomClient = new RoomClient(VideoCallActivity.this, mRoomStore);
                            roomClient.connectCall();
                            initialize();
                        } else {
                            createRoom();
                        }

                    } else {
                        Toast.makeText(VideoCallActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        roomClient.onDestroy();
            stopService(new Intent(this.getApplicationContext(), VideoCallServices.class));
    }

}