package api.adhyay.app.videocall.model;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;

public class Peer extends Info {

    private final String mId;
 //   private final String mDisplayName;
    private boolean camStatus;
    private boolean micStatus;

    private Set<String> consumers;

    public Peer(@NonNull JSONObject info) {
        mId = info.optString("id");
   //     mDisplayName = info.optString("clientName");
        consumers = new HashSet<>();
    }

    @Override
    public String getId() {
        return mId;
    }

//    @Override
//    public String getDisplayName() {
//        return mDisplayName;
//    }

    @Override
    public boolean camStatus() {
        return camStatus;
    }

    @Override
    public boolean micStatus() {
        return micStatus;
    }


    public void setCamStatus(boolean status) {
        camStatus = status;
    }

    public void setMicStatus(boolean status) {
        micStatus = status;
    }

    public Set<String> getConsumers() {
        return consumers;
    }

}
