package api.adhyay.app.libs;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mediasoup.droid.Consumer;
import org.mediasoup.droid.Device;
import org.mediasoup.droid.Logger;
import org.mediasoup.droid.MediasoupException;
import org.mediasoup.droid.Producer;
import org.mediasoup.droid.RecvTransport;
import org.mediasoup.droid.SendTransport;
import org.mediasoup.droid.Transport;
import org.webrtc.AudioTrack;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.VideoTrack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;

import api.adhyay.app.videocall.SocketIOConnection.SocketConstants;
import api.adhyay.app.videocall.SocketIOConnection.SocketListener;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static api.adhyay.app.videocall.SocketIOConnection.SocketConstants.RoomId;
import static api.adhyay.app.videocall.SocketIOConnection.SocketConstants.RoomPassword;

public class RoomClient implements SocketListener {

    private static final String TAG = RoomClient.class.getName();

    public enum ConnectionState {
        // initial state.
        NEW,
        // connecting or reconnecting.
        CONNECTING,
        // connected.
        CONNECTED,
        // mClosed.
        CLOSED,
    }

    private final Context context;

    private final Handler workerThread;

    private VideoTrack localVideoTrack;
    private AudioTrack mLocalAudioTrack;

    // Local mic mediasoup Producer.
    private String roomRtpCapability;

    // Closed flag.
    private volatile boolean mClosed;
    //producerId

    private String recvTransportId;

    private PeerConnectionUtils peerConnectionUtils;

    private JSONArray client;

    private String audioProducerId;
    private String videoProducerId;

    // mediasoup-client Device instance.
    private Device mMediasoupDevice;
    // mediasoup Transport for sending.
    private SendTransport mSendTransport;
    // mediasoup Transport for receiving.
    private RecvTransport mRecvTransport;
    // Local cam mediasoup Producer.
    private Producer mCamProducer;
    // Local mic mediasoup Producer.
    private Producer mMicProducer;

    private final Handler mainThread;

    private JSONArray array;

    private final RoomStore mRoomStore;

    @NonNull
    final Map<String, ConsumerHolder> mConsumers;

    public RoomClient(Context context, RoomStore roomStore) {

        //  super(roomStore);
        this.context = context.getApplicationContext();
        this.mClosed = false;
        mRoomStore = roomStore;
        mConsumers = new ConcurrentHashMap<>();
        this.mRoomStore.setMe(SocketConstants.UserId, SocketConstants.UserName);
        HandlerThread handlerThread = new HandlerThread("WorkerThread");
        handlerThread.start();
        workerThread = new Handler(handlerThread.getLooper());
        mainThread = new Handler(Looper.getMainLooper());
        AppSocketListener.getInstance().setActiveSocketListener(this);
        workerThread.post(() -> peerConnectionUtils = new PeerConnectionUtils());

        client = new JSONArray();
        array = new JSONArray();
        // Restart Socket.io to avoid weird stuff ;-)
        //     AppSocketListener.getInstance().restartSocket();
    }

    static class ConsumerHolder {
        @NonNull
        final String peerId;
        @NonNull
        final Consumer mConsumer;

        ConsumerHolder(@NonNull String peerId, @NonNull Consumer consumer) {
            this.peerId = peerId;
            mConsumer = consumer;
        }
    }

    @Async
    public void call() {
        mRoomStore.setRoomState(ConnectionState.CONNECTING);
        workerThread.post(this::startCall);
    }

    @WorkerThread
    private void startCall() {

        JSONObject object = new JSONObject();
        JSONObject roomObject = new JSONObject();
        try {
            object.put("id", RoomId);
            object.put("password", RoomPassword);
            object.put("type", "video");

            roomObject.put("room", object);
            roomObject.put("clientId", SocketConstants.ClientId);

            AppSocketListener.getInstance().emit("start-call", roomObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void connectCall() {

        workerThread.post(() -> {
            JSONObject object = new JSONObject();
            JSONObject roomObject = new JSONObject();
            try {
                object.put("id", RoomId);
                object.put("password", RoomPassword);
                object.put("type", "video");

                roomObject.put("room", object);
                AppSocketListener.getInstance().emit("connect-call", roomObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

    }


    @Async
    public void enableMic() {
        //      Logger.d(TAG, "enableMic()");
        workerThread.post(this::openMic);
    }

    @Async
    public void enableCam() {
        //     Logger.d(TAG, "enableMic()");
        workerThread.post(this::openCam);
    }

    @Async
    public void muteAudio() {
        Logger.d(TAG, "muteAudio()");
        mRoomStore.setAudioMutedState(true);
        workerThread.post(
                () -> {
                    for (ConsumerHolder holder : mConsumers.values()) {
                        if (!"audio".equals(holder.mConsumer.getKind())) {
                            continue;
                        }
                        pauseConsumer(holder.mConsumer);
                    }
                });
    }

    @Async
    public void unmuteAudio() {
        Logger.d(TAG, "unmuteAudio()");
        mRoomStore.setAudioMutedState(false);
        workerThread.post(
                () -> {
                    for (ConsumerHolder holder : mConsumers.values()) {
                        if (!"audio".equals(holder.mConsumer.getKind())) {
                            continue;
                        }
                        resumeConsumer(holder.mConsumer);
                    }
                });
    }

    @WorkerThread
    private void createSendTransport() {

        JSONObject object = new JSONObject();
        JSONObject roomObject = new JSONObject();
        try {
            roomObject.put("id", RoomId);
            roomObject.put("password", RoomPassword);
            roomObject.put("type", "video");

            object.put("room", roomObject);
            object.put("transportType", "send");

            AppSocketListener.getInstance().emit("create-transport", object);

            Log.d(TAG, "createSendTransport: " + object);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @WorkerThread
    private void createRecvTransport() {

        JSONObject object = new JSONObject();
        JSONObject roomObject = new JSONObject();
        try {
            roomObject.put("id", RoomId);
            roomObject.put("password", RoomPassword);
            roomObject.put("type", "video");

            object.put("room", roomObject);
            object.put("transportType", "recv");

            AppSocketListener.getInstance().emit("create-transport", roomObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @WorkerThread
    private void openCam() {

        try {

            if (mCamProducer != null) {
                return;
            }
            if (!mMediasoupDevice.isLoaded()) {
                Logger.w(TAG, "enableCam() | not loaded");
                return;
            }
            if (!mMediasoupDevice.canProduce("video")) {
                Logger.w(TAG, "enableCam() | cannot produce video");
                return;
            }
            if (mSendTransport == null) {
                Logger.w(TAG, "enableCam() | mSendTransport doesn't ready");
                return;
            }

            if (localVideoTrack != null) {
                localVideoTrack = null;
            }

            localVideoTrack = peerConnectionUtils.createVideoTrack(context, "cam");
            localVideoTrack.setEnabled(true);

            mCamProducer =
                    mSendTransport.produce(
                            producer -> {
                                Logger.e(TAG, "onTransportClose(), camProducer");
                                if (mCamProducer != null) {
                                    //          mStore.removeProducer(mCamProducer.getId());
                                    mCamProducer = null;
                                }
                            },
                            localVideoTrack,
                            null,
                            null);

            mRoomStore.addProducer(mCamProducer);

            Log.d(TAG, "openCam: "+mCamProducer.getId());

        } catch (MediasoupException e) {
            e.printStackTrace();
        }

    }

    private String getProducerId(){
       Observable<String> observable = Observable.create(emitter -> {

            AppSocketListener.getInstance().addOnHandler("create-producer", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject object = (JSONObject) args[0];
                    if(!emitter.isDisposed()){
                        emitter.onNext(object.optString("id"));
                    }
                }
            });
        });
        return observable.blockingFirst();
    }


    @WorkerThread
    private void openMic() {
        try {

            if (mMicProducer != null) {
                return;
            }
            if (!mMediasoupDevice.isLoaded()) {
                Logger.w(TAG, "enableMic() | not loaded");
                return;
            }
            if (!mMediasoupDevice.canProduce("audio")) {
                Logger.w(TAG, "enableMic() | cannot produce audio");
                return;
            }
            if (mSendTransport == null) {
                Logger.w(TAG, "enableMic() | mSendTransport doesn't ready");
                return;
            }

            if (mLocalAudioTrack == null) {
                mLocalAudioTrack = peerConnectionUtils.createAudioTrack(context, "ARDAMSa0");
                mLocalAudioTrack.setEnabled(true);
            }

            mMicProducer =
                    mSendTransport.produce(
                            producer -> {
                                Logger.e(TAG, "onTransportClose(), micProducer");
                                if (mMicProducer != null) {
                                    mMicProducer = null;
                                }
                            },
                            mLocalAudioTrack,
                            null,
                            null);

            mRoomStore.addProducer(mMicProducer);

        } catch (MediasoupException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "openMic: "+mMicProducer.getId());

    }

    @Async
    public void changeCam() {
        Logger.d(TAG, "changeCam()");
        mRoomStore.setCamInProgress(true);
        workerThread.post(
                () ->
                        peerConnectionUtils.switchCam(
                                new CameraVideoCapturer.CameraSwitchHandler() {
                                    @Override
                                    public void onCameraSwitchDone(boolean b) {
                                        //   mStore.setCamInProgress(false);
                                    }

                                    @Override
                                    public void onCameraSwitchError(String s) {
                                        Logger.w(TAG, "changeCam() | failed: " + s);
                                        //      mStore.addNotify("error", "Could not change cam: " + s);
                                        mRoomStore.setCamInProgress(false);
                                    }
                                }));
    }

    @Async
    public void disableCam() {
        //      Logger.d(TAG, "disableCam()");
        workerThread.post(this::disableCamImpl);
    }

    @Async
    public void muteMic() {
        //     Logger.d(TAG, "muteMic()");
        //   mRoomStore.setAudioMutedState(true);
        workerThread.post(this::muteMicImpl);
    }

    @Async
    public void unmuteMic() {
        Logger.d(TAG, "unMuteMic()");
        workerThread.post(this::unMuteMicImpl);
    }

    @WorkerThread
    private void disableCamImpl() {
        Logger.d(TAG, "disableCamImpl()");

        if (mCamProducer == null) {
            return;
        }

        mCamProducer.close();
        mRoomStore.removeProducer(mCamProducer.getId());
        mCamProducer = null;

    }

    @WorkerThread
    private void muteMicImpl() {
        Logger.d(TAG, "MuteMicImpl()");
        if (mMicProducer != null) {
            mMicProducer.pause();
            mRoomStore.setProducerPaused(mMicProducer.getId());
        }

    }

    @WorkerThread
    private void unMuteMicImpl() {

        if (mMicProducer != null) {

            mMicProducer.resume();
            mRoomStore.setProducerResumed(mMicProducer.getId());
        }

    }


    private final SendTransport.Listener sendTransportListener = new SendTransport.Listener() {

        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public String onProduce(Transport transport, String kind, String rtpParameters, String appData) {
            if (mClosed) {
                return "";
            }

            String producerId;
            JSONObject object = new JSONObject();
            JSONObject roomObject = new JSONObject();

            try {
                JSONObject rtp = new JSONObject(rtpParameters);
                object.put("transportId", transport.getId());
                object.put("rtpParameters", rtp);
                object.put("kind", kind);
                object.put("callbackId", kind);
                roomObject.put("id", RoomId);
                roomObject.put("password", RoomPassword);
                roomObject.put("type", "video");
                object.put("room", roomObject);

                AppSocketListener.getInstance().emit("create-producer", object);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            producerId  = getProducerId();

            return producerId;
        }

        @Override
        public void onConnect(Transport transport, String dtlsParameters) {

            //      Log.d(TAG, "onConnect: " + transport.getId());

            JSONObject object = new JSONObject();
            JSONObject roomObject = new JSONObject();

            try {
                JSONObject dtls = new JSONObject(dtlsParameters);
                object.put("transportId", transport.getId());
                object.put("dtlsParameters", dtls);
                object.put("transportType", "send");
                roomObject.put("id", RoomId);
                roomObject.put("password", RoomPassword);
                roomObject.put("type", "video");
                roomObject.put("connected", "true");
                object.put("room", roomObject);

                AppSocketListener.getInstance().emit("connect-transport", object);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onConnectionStateChange(Transport transport, String connectionState) {

            //    Log.d(TAG, "onConnectionStateChange: " + connectionState);

        }
    };

    private final RecvTransport.Listener recvTransportListener =
            new RecvTransport.Listener() {
                @Override
                public void onConnect(Transport transport, String dtlsParameters) {

                    JSONObject object = new JSONObject();
                    JSONObject roomObject = new JSONObject();
                    try {

                        object.put("transportId", transport.getId());
                        object.put("dtlsParameters", new JSONObject(dtlsParameters));
                        object.put("transportType", "recv");
                        object.put("rtpParameters", new JSONObject(roomRtpCapability));
                        roomObject.put("id", RoomId);
                        roomObject.put("password", RoomPassword);
                        roomObject.put("type", "video");
                        object.put("room", roomObject);
                        AppSocketListener.getInstance().emit("connect-transport", object);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConnectionStateChange(Transport transport, String connectionState) {
                    //     Log.d(TAG, "onConnectionStateChange: " + connectionState);
                }
            };

    @WorkerThread
    private void consumeRoomUsers() {
        try {

            if (array != null && array.length() != 0) {

                for (int i = 0; i < array.length(); i++) {
                    String clientId = client.getString(i);

                    JSONObject object1 = new JSONObject();
                    object1.put("id", clientId);

                    mRoomStore.addPeer(clientId, object1);

                    for (int j = 0; j < array.getJSONArray(i).length(); j++) {

                        String producerIds = array.getJSONArray(i).getString(j);

                        try {
                            JSONObject roomObject = new JSONObject();
                            roomObject.put("clientId", clientId);
                            roomObject.put("producerId", producerIds);
                            roomObject.put("transportId", recvTransportId);
                            roomObject.put("name", "Bhokal");
                            roomObject.put("rtpCapabilities", new JSONObject(roomRtpCapability));

                            AppSocketListener.getInstance().emit("start-consume", roomObject);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @WorkerThread
    private void pauseConsumer(Consumer consumer) {
        Logger.d(TAG, "pauseConsumer() " + consumer.getId());
        if (consumer.isPaused()) {
            return;
        }

            consumer.pause();
            mRoomStore.setConsumerPaused(consumer.getId(), "local");

    }

    @WorkerThread
    private void resumeConsumer(Consumer consumer) {
        Logger.d(TAG, "resumeConsumer() " + consumer.getId());
        if (!consumer.isPaused()) {
            return;
        }

        try {
         //   mProtoo.syncRequest("resumeConsumer", req -> jsonPut(req, "consumerId", consumer.getId()));
            consumer.resume();
            mRoomStore.setConsumerResumed(consumer.getId(), "local");
        } catch (Exception e) {
            e.printStackTrace();
         //   logError("resumeConsumer() | failed:", e);
         //   mStore.addNotify("error", "Error resuming Consumer: " + e.getMessage());
        }
    }

    @Async
    public void leaveCall(){

        JSONObject object = new JSONObject();
        JSONObject roomObject = new JSONObject();
        try {
            object.put("id", RoomId);
            object.put("password", RoomPassword);
            object.put("type", "video");

            roomObject.put("room", object);
            AppSocketListener.getInstance().emit("leave-call", roomObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSocketConnected() {

        //       Log.d(TAG, "onSocketConnected: "+AppSocketListener.getInstance().isSocketConnected());

        AppSocketListener.getInstance().addOnHandler(Socket.EVENT_CONNECT_ERROR, onConnectError);
        AppSocketListener.getInstance().addOnHandler("connect-api", connectApi);
        AppSocketListener.getInstance().addOnHandler("rtp-capabilities", rtpCapabilities);
        AppSocketListener.getInstance().addOnHandler("start-call", startCall);
        AppSocketListener.getInstance().addOnHandler("connect-call", connectCall);
        AppSocketListener.getInstance().addOnHandler("create-transport", createTransport);
        AppSocketListener.getInstance().addOnHandler("new-producer", newProducer);
        AppSocketListener.getInstance().addOnHandler("start-consume", startConsume);
        AppSocketListener.getInstance().addOnHandler("incoming-call", incomingCall);
        AppSocketListener.getInstance().addOnHandler("leave-call", leaveCall);
        AppSocketListener.getInstance().addOnHandler("disconnect", onDisconnect);
        AppSocketListener.getInstance().addOnHandler("network-lost", networkLost);
        AppSocketListener.getInstance().addOnHandler("decline-call", declineCall);
        AppSocketListener.getInstance().addOnHandler("call-logs", callLogs);

    }

    @Override
    public void onSocketDisconnected() {

    }

    @Override
    public void onIncomingCall(Object... args) {

    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            ////    Log.i("Failed","Failed to connect");

        }
    };

    private static Emitter.Listener connectApi = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            //       Log.d(TAG, "call: "+args[0]);

        }
    };

    private static Emitter.Listener incomingCall = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
        //    Log.d(TAG, "call: " + args[0]);
            JSONObject object = (JSONObject) args[0];
            //      Log.d("Test Room", "incomingCall: " + object);

            JSONObject incomingCall = object.optJSONObject("room");

            JSONObject client = object.optJSONObject("client");

            String clientId = client.optString("id");

            String roomId = incomingCall.optString("id");
            String roomPassword = incomingCall.optString("password");

            SocketConstants.RoomId = roomId;
            SocketConstants.RoomPassword = roomPassword;

        }
    };

    private Emitter.Listener rtpCapabilities = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            JSONObject object = (JSONObject) args[0];
                 Log.d(TAG, "rtp-capabilities: " + object);

            mMediasoupDevice = new Device();

            try {
                roomRtpCapability = object.getString("routerRtpCapabilities");
                mMediasoupDevice.load(roomRtpCapability);

      //          Log.d(TAG, "call: " + roomRtpCapability);

                mRoomStore.setRoomState(ConnectionState.CONNECTED);

                createSendTransport();
                createRecvTransport();
            } catch (JSONException | MediasoupException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener connectCall = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            JSONObject object = (JSONObject) args[0];

      //      Log.d(TAG, "connectCall: " + object);

            try {
                JSONObject clientDetails = object.getJSONObject("client");

                if (clientDetails.getJSONArray("producers") != null) {

                    JSONArray clientArray = clientDetails.getJSONArray("producers");
                    String id = clientDetails.getString("id");

                    client.put(id);
                    array.put(clientArray);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private final Emitter.Listener createTransport = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            try {
                JSONObject object = (JSONObject) args[0];

                //       Log.d(TAG, "create transport: " + object.optString("transportType"));

                JSONObject data = object.getJSONObject("transportData");
                String id = data.optString("id");
                String iceParameters = data.optString("iceParameters");
                String iceCandidates = data.optString("iceCandidates");
                String dtlsParameters = data.optString("dtlsParameters");
                String transportType = object.optString("transportType");

                //      Log.d(TAG, "createTransport: " + object);

                if (transportType.equals("send")) {

                    mSendTransport =
                            mMediasoupDevice.createSendTransport(
                                    sendTransportListener, id, iceParameters, iceCandidates, dtlsParameters);

                    boolean canSendMic = mMediasoupDevice.canProduce("audio");
                    boolean canSendCam = mMediasoupDevice.canProduce("video");
                    mRoomStore.setMediaCapabilities(canSendMic, canSendCam);

                    mainThread.post(RoomClient.this::enableMic);
                    mainThread.post(RoomClient.this::enableCam);


                } else {

                    JSONObject object1 = object.getJSONObject("transportData");

                    recvTransportId = object1.optString("id");

                    mRecvTransport =
                            mMediasoupDevice.createRecvTransport(
                                    recvTransportListener, id, iceParameters, iceCandidates, dtlsParameters, null);

                    consumeRoomUsers();

                }

            } catch (MediasoupException | JSONException e) {
                e.printStackTrace();
            }

        }
    };


    private Emitter.Listener startCall = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            //     Log.d(TAG, "call: " + args[0]);
        }
    };


    private Emitter.Listener newProducer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject object = (JSONObject) args[0];

            if (mRoomStore.getPeers().getValue().getAllPeers().size() > 4) {
                return;
            }
            String clientId = object.optString("clientId");
            String producerID = object.optString("producerId");

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", clientId);
                mRoomStore.addPeer(clientId, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject roomObject = new JSONObject();
            try {

                roomObject.put("clientId", clientId);
                roomObject.put("producerId", producerID);
                roomObject.put("transportId", recvTransportId);
                roomObject.put("name", "Bhokal");
                roomObject.put("rtpCapabilities", new JSONObject(roomRtpCapability));
                AppSocketListener.getInstance().emit("start-consume", roomObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener startConsume = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            JSONObject object = (JSONObject) args[0];

            String consumerID = object.optString("consumerId");
            String producerID = object.optString("producerId");
            String kind = object.optString("kind");
            String type = object.optString("type");
            String rtpParameters = object.optString("rtpParameters");
            String clientID = object.optString("clientId");

            Consumer consumer = null;
            try {
                consumer = mRecvTransport.consume(
                        c -> {
                            mConsumers.remove(c.getId());
                            Logger.w(TAG, "onTransportClose for consume");
                        },
                        consumerID,
                        producerID,
                        kind,
                        rtpParameters, null);

            } catch (MediasoupException e) {
                e.printStackTrace();
            }


            mConsumers.put(consumer.getId(), new ConsumerHolder(clientID, consumer));
            mRoomStore.addConsumer(clientID, consumer);
            Log.d(TAG, "call: "+consumer.getTrack());
        }
    };

    private Emitter.Listener leaveCall = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            JSONObject object = (JSONObject) args[0];
            Log.d(TAG, "on disconnect: " + object);

            String clientId = object.optString("clientId");
            mRoomStore.removePeer(clientId);
            mRoomStore.removeConsumer(clientId);

        }
    };


    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            JSONObject object = (JSONObject) args[0];
            Log.d(TAG, "on disconnect: " + object);

            String clientId = object.optString("clientId");
            mRoomStore.removePeer(clientId);
            mRoomStore.removeConsumer(clientId);

        }
    };


    private Emitter.Listener networkLost = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            JSONObject object = (JSONObject) args[0];
            Log.d(TAG, "on disconnect: " + object);

            String clientId = object.optString("clientId");
            mRoomStore.removePeer(clientId);
            mRoomStore.removeConsumer(clientId);

        }
    };


    private Emitter.Listener declineCall = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //      Log.d(TAG, "call: $args");


        }
    };

    private static Emitter.Listener callLogs = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //      Log.d(TAG, "call: $args");

        }
    };

    public void onDestroy() {
        AppSocketListener.getInstance().off(Socket.EVENT_CONNECT_ERROR);
        AppSocketListener.getInstance().off("connect-api");
        AppSocketListener.getInstance().off("rtp-capabilities");
        AppSocketListener.getInstance().off("start-call");
        AppSocketListener.getInstance().off("connect-call");
        AppSocketListener.getInstance().off("create-transport");
        AppSocketListener.getInstance().off("start-consume");
        AppSocketListener.getInstance().off("incoming-call");
        AppSocketListener.getInstance().off("leave-call");
        AppSocketListener.getInstance().off("disconnect");
        AppSocketListener.getInstance().off("network-lost");
        AppSocketListener.getInstance().off("decline-call");
        AppSocketListener.getInstance().off("call-logs");
        disconnectSocket();
        //    stopService(new Intent(context.getApplicationContext(), VideoCallServices.class));
    }

    public void disconnectSocket() {
        AppSocketListener.getInstance().disconnect();
        AppSocketListener.getInstance().destroy();
        mRoomStore.setRoomState(ConnectionState.CLOSED);
           disposeTransportDevice();
    }

    @WorkerThread
    private void disposeTransportDevice() {
        Logger.d(TAG, "disposeTransportDevice()");

        if (mMediasoupDevice != null) {
            mMediasoupDevice.dispose();
            mMediasoupDevice = null;
        }

        if (mSendTransport != null) {
            mSendTransport.close();
            mSendTransport.dispose();
            mSendTransport = null;
        }

        if (mRecvTransport != null) {
            mRecvTransport.close();
            mRecvTransport.dispose();
            mRecvTransport = null;
        }

        // dispose device.

        workerThread.post(
                () -> {
                    if (mLocalAudioTrack != null) {
                        mLocalAudioTrack.setEnabled(false);
                        mLocalAudioTrack.dispose();
                        mLocalAudioTrack = null;
                    }

                    // dispose video track.
                    if (localVideoTrack != null) {
                        localVideoTrack.setEnabled(false);
                        localVideoTrack.dispose();
                        localVideoTrack = null;
                    }

                    // dispose peerConnection.
                    peerConnectionUtils.dispose();

                    // quit worker handler thread.
                    workerThread.getLooper().quit();
                });

    }
}
