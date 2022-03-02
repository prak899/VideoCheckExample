package api.adhyay.app.videocall.vm;

import android.app.Application;

import org.webrtc.AudioTrack;
import org.webrtc.VideoTrack;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import api.adhyay.app.libs.RoomStore;
import api.adhyay.app.videocall.model.Info;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class PeerViewProps extends EdiasProps {
  boolean mIsMe;
  ObservableField<Info> mPeer;
  ObservableField<String> mAudioProducerId;
  ObservableField<String> mVideoProducerId;
  ObservableField<String> mAudioConsumerId;
  ObservableField<String> mVideoConsumerId;
  ObservableField<String> mAudioRtpParameters;
  ObservableField<String> mVideoRtpParameters;
  ObservableField<AudioTrack> mAudioTrack;
  ObservableField<VideoTrack> mVideoTrack;
  ObservableField<Boolean> mAudioMuted;
  ObservableField<String> mAudioCodec;
  ObservableField<String> mVideoCodec;

  public PeerViewProps(@NonNull Application application, @NonNull RoomStore roomStore) {
    super(application, roomStore);
    // Add default value to avoid null check in layout.
    mPeer = new ObservableField<>(new Info());
    mAudioProducerId = new ObservableField<>();
    mVideoProducerId = new ObservableField<>();
    mAudioConsumerId = new ObservableField<>();
    mVideoConsumerId = new ObservableField<>();
    mAudioRtpParameters = new ObservableField<>();
    mVideoRtpParameters = new ObservableField<>();
    mAudioTrack = new ObservableField<>();
    mVideoTrack = new ObservableField<>();
    mAudioMuted = new ObservableField<>();
    mAudioCodec = new ObservableField<>();
    mVideoCodec = new ObservableField<>();
  }

  public void setMe(boolean me) {
    mIsMe = me;
  }

  public boolean isMe() {
    return mIsMe;
  }

  public ObservableField<Info> getPeer() {
    return mPeer;
  }

  public ObservableField<String> getAudioProducerId() {
    return mAudioProducerId;
  }

  public ObservableField<String> getVideoProducerId() {
    return mVideoProducerId;
  }

  public ObservableField<String> getAudioConsumerId() {
    return mAudioConsumerId;
  }

  public ObservableField<String> getVideoConsumerId() {
    return mVideoConsumerId;
  }

  public ObservableField<String> getAudioRtpParameters() {
    return mAudioRtpParameters;
  }

  public ObservableField<String> getVideoRtpParameters() {
    return mVideoRtpParameters;
  }

  public ObservableField<AudioTrack> getAudioTrack() {
    return mAudioTrack;
  }

  public ObservableField<VideoTrack> getVideoTrack() {
    return mVideoTrack;
  }

  public ObservableField<String> getAudioCodec() {
    return mAudioCodec;
  }

  public ObservableField<String> getVideoCodec() {
    return mVideoCodec;
  }

}
