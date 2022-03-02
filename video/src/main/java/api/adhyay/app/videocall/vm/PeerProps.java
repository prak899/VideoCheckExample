package api.adhyay.app.videocall.vm;

import android.app.Application;

import org.mediasoup.droid.Consumer;
import org.webrtc.AudioTrack;
import org.webrtc.VideoTrack;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Observable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import api.adhyay.app.libs.RoomStore;
import api.adhyay.app.videocall.model.Consumers;
import api.adhyay.app.videocall.model.Peer;
import api.adhyay.app.videocall.model.Peers;


public class PeerProps extends PeerViewProps {

  private final StateComposer mStateComposer;

  public PeerProps(@NonNull Application application, @NonNull RoomStore roomStore) {
    super(application, roomStore);
    setMe(false);
    mStateComposer = new StateComposer();
    mStateComposer.addOnPropertyChangedCallback(
            new Observable.OnPropertyChangedCallback() {
              @Override
              public void onPropertyChanged(Observable sender, int propertyId) {

                Consumer audioConsumer = mStateComposer.getConsumer("audio");
                Consumer videoCW = mStateComposer.getConsumer("video");
        //        Consumer audioConsumer =audioConsumer != null ?audioConsumer.getConsumer() : null;
        //        Consumer videoConsumer = videoCW != null ? videoCW.getConsumer() : null;

                mPeer.set(mStateComposer.mPeer);

                mAudioProducerId.set(audioConsumer != null ?audioConsumer.getId() : null);
                mVideoProducerId.set(videoCW != null ? videoCW.getId() : null);

                mAudioRtpParameters.set(
                       audioConsumer != null ?audioConsumer.getRtpParameters() : null);
                mVideoRtpParameters.set(
                        videoCW != null ? videoCW.getRtpParameters() : null);

                mAudioTrack.set(audioConsumer!= null ? (AudioTrack)audioConsumer.getTrack() : null);

                mVideoTrack.set(videoCW != null ? (VideoTrack) videoCW.getTrack():null);

              }
            });
  }

  public void connect(LifecycleOwner owner, @NonNull String clientID) {
    mStateComposer.connect(owner, getRoomStore(), clientID);
  }

  @Override
  public void connect(LifecycleOwner lifecycleOwner) {
    throw new IllegalAccessError("use connect with clientID");
  }

  public static class StateComposer extends BaseObservable {

    private String clientID;
    private Peer mPeer;
    private Consumers mConsumers;

    private Observer<Peers> mPeersObservable =
            peers -> {
              mPeer = peers.getPeer(clientID);
              notifyChange();
            };

    private Observer<Consumers> mConsumersObserver =
            consumers -> {
              mConsumers = consumers;
              notifyChange();
            };

    void connect(@NonNull LifecycleOwner owner, RoomStore store, String mClientID) {
      clientID = mClientID;
      store.getPeers().removeObserver(mPeersObservable);
      store.getPeers().observe(owner, mPeersObservable);

      store.getConsumers().removeObserver(mConsumersObserver);
      store.getConsumers().observe(owner, mConsumersObserver);
    }

    Consumer getConsumer(String kind) {
      if (mPeer == null || mConsumers == null) {
        return null;
      }

      Set<String> clientIDS = mPeer.getConsumers();

      for (String clientID : clientIDS) {
        Consumer wp = mConsumers.getConsumer(clientID);
        if (wp == null || wp.getTrack() == null) {
          continue;
        }
        if (kind.equals(wp.getKind())) {
          return wp;
        }
      }
      return null;
    }
  }
}
