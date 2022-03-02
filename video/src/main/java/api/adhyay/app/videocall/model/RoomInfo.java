package api.adhyay.app.videocall.model;


import api.adhyay.app.libs.RoomClient;
import  static  api.adhyay.app.libs.RoomClient.ConnectionState.NEW;

public class RoomInfo {

  private String mUrl;
  private int mRoomId;
  private String mRoomPassword;
  private RoomClient.ConnectionState mConnectionState = NEW;
  private String mActiveSpeakerId;
  private String mStatsPeerId;


  public void setRoomPassword(String mRoomPassword)
  {
    this.mRoomPassword = mRoomPassword;
  }

  public void setRoomId(int roomId) {
    this.mRoomId = roomId;
  }

  public RoomClient.ConnectionState getConnectionState() {
    return mConnectionState;
  }

  public void setConnectionState(RoomClient.ConnectionState connectionState) {
    this.mConnectionState = connectionState;
  }

  public String getActiveSpeakerId() {
    return mActiveSpeakerId;
  }

  public void setActiveSpeakerId(String activeSpeakerId) {
    this.mActiveSpeakerId = activeSpeakerId;
  }

  public String getStatsPeerId() {
    return mStatsPeerId;
  }

  public void setStatsPeerId(String statsPeerId) {
    this.mStatsPeerId = statsPeerId;
  }

}
