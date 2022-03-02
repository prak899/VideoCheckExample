package api.adhyay.app.libs;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import api.adhyay.app.videocall.model.RoomInfo;
import api.adhyay.app.videocall.vm.EdiasProps;


public class RoomProps extends EdiasProps {


  private ObservableField<RoomClient.ConnectionState> mConnectionState;
  private ObservableField<Boolean> mAudioOnly;
  private ObservableField<Boolean> mAudioOnlyInProgress;
  private ObservableField<Boolean> mAudioMuted;

  public RoomProps(@NonNull Application application, @NonNull RoomStore roomStore) {
    super(application, roomStore);
    mConnectionState = new ObservableField<>();
    mAudioOnly = new ObservableField<>();
    mAudioOnlyInProgress = new ObservableField<>();
    mAudioMuted = new ObservableField<>();
  }

  public ObservableField<RoomClient.ConnectionState> getConnectionState() {
    return mConnectionState;
  }


  public ObservableField<Boolean> getAudioMuted() {
    return mAudioMuted;
  }


  private void receiveState(RoomInfo roomInfo) {
    mConnectionState.set(roomInfo.getConnectionState());

  }

  @Override
  public void connect(LifecycleOwner owner) {
    RoomStore roomStore = getRoomStore();
    roomStore.getRoomInfo().observe(owner, this::receiveState);
    roomStore
        .getMe()
        .observe(
            owner,
            me -> {
              mAudioOnly.set(me.isAudioOnly());
              mAudioOnlyInProgress.set(me.isAudioOnlyInProgress());
              mAudioMuted.set(me.isAudioMuted());
            });
  }
}
