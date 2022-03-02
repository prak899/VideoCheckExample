package api.adhyay.app.libs;


public class RoomOptions {

  // Whether we want to produce audio/video.
  private boolean mCamProduce = true;

  private boolean mMicProduce = true;
  // Whether we should consume.
  private boolean mConsume = true;

  public RoomOptions setCamProduce(boolean camProduce) {
    mCamProduce = camProduce;
    return this;
  }

  public RoomOptions setMicProducer(boolean micProducer)
  {
    mMicProduce = micProducer;
    return this;
  }

  public RoomOptions setConsume(boolean consume) {
    this.mConsume = consume;
    return this;
  }

  public boolean ismMicProduce() {
    return mMicProduce;
  }

  public boolean isCamProduce() {
    return mCamProduce;
  }

  public boolean isConsume() {
    return mConsume;
  }

}
