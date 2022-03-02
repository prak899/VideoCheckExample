package api.adhyay.app.videocall.model;

import org.mediasoup.droid.Producer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;

public class Producers {

  private static final String TAG = Producers.class.getName();

  public static class ProducersWrapper {

    public static final String TYPE_CAM = "cam";
    public static final String TYPE_SHARE = "share";

    private final Producer mProducer;
    private String mType;

    ProducersWrapper(Producer producer) {
      this.mProducer = producer;
    }

    public Producer getProducer() {
      return mProducer;
    }

    public String getType() {
      return mType;
    }

  }

  private final Map<String, ProducersWrapper> mProducers;

  public Producers() {
    mProducers = new ConcurrentHashMap<>();
  }

  public void addProducer(Producer producer) {
    mProducers.put(producer.getId(), new ProducersWrapper(producer));

  //  Log.d(TAG, "addProducer: "+mProducers.get(producer.getId()));
  }

  public void removeProducer(String producerId) {
    mProducers.remove(producerId);
  }

  public void setProducerPaused(String producerId) {
    ProducersWrapper wrapper = mProducers.get(producerId);
    if (wrapper == null) {
      return;
    }
    wrapper.mProducer.pause();
  }

  public void setProducerResumed(String producerId) {
    ProducersWrapper wrapper = mProducers.get(producerId);
    if (wrapper == null) {
      return;
    }
    wrapper.mProducer.resume();
  }

  public ProducersWrapper filter(@NonNull String kind) {
    for (ProducersWrapper wrapper : mProducers.values()) {
      if (wrapper.mProducer == null) {
        continue;
      }
      if (wrapper.mProducer.getTrack() == null) {
        continue;
      }
      if (kind.equals(wrapper.mProducer.getTrack().kind())) {
        return wrapper;
      }
    }

    return null;
  }

  public void clear() {
    mProducers.clear();
  }
}
