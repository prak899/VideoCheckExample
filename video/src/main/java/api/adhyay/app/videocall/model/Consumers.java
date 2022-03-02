package api.adhyay.app.videocall.model;

import org.mediasoup.droid.Consumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Consumers {

//    public static class ConsumerWrapper {
//
//        private String mType;
//        private Consumer mConsumer;

//        ConsumerWrapper(String type, Consumer consumer) {
//            mType = type;
//            mConsumer = consumer;
//        }
//
//        public String getType() {
//            return mType;
//        }
//
//        public Consumer getConsumer() {
//            return mConsumer;
//        }
//
//    }

    private final Map<String, Consumer> consumers;

    public Consumers() {
        consumers = new ConcurrentHashMap<>();
    }

    public void addConsumer(String clientID, Consumer consumer) {

        consumers.put(clientID, consumer);
    }

    public void removeConsumer(String clientID) {
        consumers.remove(clientID);
    }

    public void setConsumerPaused(String consumerId, String originator) {
        Consumer wrapper = consumers.get(consumerId);
        if (wrapper == null) {
            return;
        }
        wrapper.pause();
    }

    public void setConsumerResumed(String consumerId, String originator) {
        Consumer wrapper = consumers.get(consumerId);
        if (wrapper == null) {
            return;
        }
        wrapper.pause();
    }

    public Consumer getConsumer(String clientID) {
        return consumers.get(clientID);
    }

    public void clear() {
        consumers.clear();
    }

}
