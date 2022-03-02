package api.adhyay.app.videocall.model;

public class Me extends Info {

  private String mId;
  private String mDisplayName;

  private boolean mCanSendMic;
  private boolean mCanSendCam;
  private boolean mCanChangeCam;

  private boolean mCamInProgress;
  private boolean mShareInProgress;

  private boolean mAudioOnly;
  private boolean mAudioOnlyInProgress;
  private boolean mAudioMuted;

  @Override
  public String getId() {
    return mId;
  }

  public void setId(String id) {
    this.mId = id;
  }

  @Override
  public String getDisplayName() {
    return mDisplayName;
  }

  public void setDisplayName(String displayName) {
    this.mDisplayName = displayName;
  }

  public boolean isCanSendMic() {
    return mCanSendMic;
  }

  public void setCanSendMic(boolean canSendMic) {
    this.mCanSendMic = canSendMic;
  }

  public boolean isCanSendCam() {
    return mCanSendCam;
  }

  public void setCanSendCam(boolean canSendCam) {
    this.mCanSendCam = canSendCam;
  }

  public boolean isCanChangeCam() {
    return mCanChangeCam;
  }

  public void setCanChangeCam(boolean canChangeCam) {
    this.mCanChangeCam = canChangeCam;
  }

  public boolean isCamInProgress() {
    return mCamInProgress;
  }

  public void setCamInProgress(boolean camInProgress) {
    this.mCamInProgress = camInProgress;
  }

  public boolean isShareInProgress() {
    return mShareInProgress;
  }

  public void setShareInProgress(boolean shareInProgress) {
    this.mShareInProgress = shareInProgress;
  }

  public boolean isAudioOnly() {
    return mAudioOnly;
  }

  public void setAudioOnly(boolean audioOnly) {
    this.mAudioOnly = audioOnly;
  }

  public boolean isAudioOnlyInProgress() {
    return mAudioOnlyInProgress;
  }

  public void setAudioOnlyInProgress(boolean audioOnlyInProgress) {
    this.mAudioOnlyInProgress = audioOnlyInProgress;
  }

  public boolean isAudioMuted() {
    return mAudioMuted;
  }

  public void setAudioMuted(boolean audioMuted) {
    this.mAudioMuted = audioMuted;
  }

  public void clear() {
    mCamInProgress = false;
    mShareInProgress = false;
    mAudioOnly = false;
    mAudioOnlyInProgress = false;
    mAudioMuted = false;
  }
}
