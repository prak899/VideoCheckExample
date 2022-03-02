package api.adhyay.app.libs;


import android.text.TextUtils;

import org.json.JSONObject;
import org.mediasoup.droid.Consumer;
import org.mediasoup.droid.Producer;

import api.adhyay.app.videocall.model.Consumers;
import api.adhyay.app.videocall.model.Me;
import api.adhyay.app.videocall.model.Peers;
import api.adhyay.app.videocall.model.Producers;
import api.adhyay.app.videocall.model.RoomInfo;


public class RoomStore {


 private static final String TAG = "RoomStore";

// // room
 private SupplierMutableLiveData<RoomInfo> roomInfo = new SupplierMutableLiveData<>(RoomInfo::new);

 // producers
 private SupplierMutableLiveData<Producers> producers =
         new SupplierMutableLiveData<>(Producers::new);
//
 // me
 private SupplierMutableLiveData<Me> me = new SupplierMutableLiveData<>(Me::new);


 private SupplierMutableLiveData<Peers> peers = new SupplierMutableLiveData<>(Peers::new);

// // consumers
 private SupplierMutableLiveData<Consumers> consumers =
         new SupplierMutableLiveData<>(Consumers::new);


 public void setRoomState(RoomClient.ConnectionState state) {
  roomInfo.postValue(roomInfo -> roomInfo.setConnectionState(state));

  if (RoomClient.ConnectionState.CLOSED.equals(state)) {
   peers.postValue(Peers::clear);
   me.postValue(Me::clear);
   producers.postValue(Producers::clear);
   consumers.postValue(Consumers::clear);
  }
 }

 public void setMe(String userID, String displayName) {
  me.postValue(
          me -> {
           me.setId(userID);
           me.setDisplayName(displayName);
          });
 }

 public void setMediaCapabilities(boolean canSendMic, boolean canSendCam) {
  me.postValue(
          me -> {
           me.setCanSendMic(canSendMic);
           me.setCanSendCam(canSendCam);
          });
 }

 public void setAudioOnlyState(boolean enabled) {
  me.postValue(me -> me.setAudioOnly(enabled));
 }

 public void setAudioOnlyInProgress(boolean enabled) {
  me.postValue(me -> me.setAudioOnlyInProgress(enabled));
 }

 public void setAudioMutedState(boolean enabled) {
  me.postValue(me -> me.setAudioMuted(enabled));
 }

 public void setCamInProgress(boolean inProgress) {
  me.postValue(me -> me.setCamInProgress(inProgress));
 }

 public void addProducer(Producer producer) {
  producers.postValue(producers -> producers.addProducer(producer));
 }

 public void setProducerPaused(String producerId) {
  producers.postValue(producers -> producers.setProducerPaused(producerId));
 }

 public void setProducerResumed(String producerId) {
  producers.postValue(producers -> producers.setProducerResumed(producerId));
 }

 public void removeProducer(String producerId) {
  producers.postValue(producers -> producers.removeProducer(producerId));
 }

 public void addPeer(String clientID, JSONObject peerInfo) {
  peers.postValue(peersInfo -> peersInfo.addPeer(clientID, peerInfo));
 }

 public void removePeer(String clientID) {
  roomInfo.postValue(
          roomInfo -> {
           if (!TextUtils.isEmpty(clientID) && clientID.equals(roomInfo.getActiveSpeakerId())) {
            roomInfo.setActiveSpeakerId(null);
           }
           if (!TextUtils.isEmpty(clientID) && clientID.equals(roomInfo.getStatsPeerId())) {
            roomInfo.setStatsPeerId(null);
           }
          });
  peers.postValue(peersInfo -> peersInfo.removePeer(clientID));
 }

 public void addConsumer(String clientID, Consumer consumer) {
  consumers.postValue(consumers -> consumers.addConsumer(clientID, consumer));
  peers.postValue(peers -> peers.addConsumer(clientID));
 }

 public void removeConsumer(String clientID) {
  consumers.postValue(consumers -> consumers.removeConsumer(clientID));
  peers.postValue(peers -> peers.removeConsumer(clientID));
 }

 public void setConsumerPaused(String consumerId, String originator) {
  consumers.postValue(consumers -> consumers.setConsumerPaused(consumerId, originator));
 }

 public void setConsumerResumed(String consumerId, String originator) {
  consumers.postValue(consumers -> consumers.setConsumerResumed(consumerId, originator));
 }

 public SupplierMutableLiveData<RoomInfo> getRoomInfo() {
  return roomInfo;
 }

 public SupplierMutableLiveData<Me> getMe() {
  return me;
 }

 public SupplierMutableLiveData<Peers> getPeers() {
  return peers;
 }


 public SupplierMutableLiveData<Consumers> getConsumers() {
  return consumers;
 }

 public SupplierMutableLiveData<Producers> getProducers() {
  return producers;
 }

}
