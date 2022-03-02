package api.adhyay.app.videocall.model;

import org.json.JSONObject;
import org.mediasoup.droid.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class Peers {

    private static final String TAG = "Peers";

    private Map<String, Peer> mPeersInfo;

    public Peers() {
        mPeersInfo = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    public void addPeer(String clientID, @NonNull JSONObject peerInfo) {
        mPeersInfo.put(clientID, new Peer(peerInfo));
    }

    public void removePeer(String clientID) {
        mPeersInfo.remove(clientID);
    }

    public void addConsumer(String clientID) {
        Peer peer = getPeer(clientID);
        if (peer == null) {
            Logger.e(TAG, "no Peer found for new Consumer");
            return;
        }
        peer.getConsumers().add(clientID);
    }

    public void removeConsumer(String clientID) {
        Peer peer = getPeer(clientID);
        if (peer == null) {
            return;
        }
        peer.getConsumers().remove(clientID);
    }

    public Peer getPeer(String clientID) {
        return mPeersInfo.get(clientID);
    }

    public List<Peer> getAllPeers() {
        List<Peer> peers = new ArrayList<>();
        for (Map.Entry<String, Peer> info : mPeersInfo.entrySet()) {
            peers.add(info.getValue());
        }
     //   Collections.reverse(peers);
        return peers;
    }

    public void clear() {
        mPeersInfo.clear();
    }

}
