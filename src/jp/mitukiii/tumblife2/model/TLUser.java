package jp.mitukiii.tumblife2.model;

import java.util.List;

public class TLUser extends TLModel
{
  protected List<TLTumblelog> tumblelogs;

  protected String            defaultPostFormat;
  protected boolean           canUploadAudio;
  protected boolean           canUploadAiff;
  protected boolean           canAskQuestion;
  protected boolean           canUploadVideo;
  protected int               maxVideoBytesUploaded;
  protected int               likedPostCount;

  public TLTumblelog getPrimaryTumblelog()
  {
    for (TLTumblelog tumblelog: tumblelogs) {
      if (tumblelog.isPrimary()) {
        return tumblelog;
      }
    }
    return null;
  }
  
  public List<TLTumblelog> getTumblelogs()
  {
    return tumblelogs;
  }

  public void setTumblelogs(List<TLTumblelog> tumblelogs)
  {
    this.tumblelogs = tumblelogs;
  }
  
  public String getDefaultPostFormat()
  {
    return defaultPostFormat;
  }

  public void setDefaultPostFormat(String defaultPostFormat)
  {
    this.defaultPostFormat = defaultPostFormat;
  }

  public boolean canUploadAudio()
  {
    return canUploadAudio;
  }

  public void setCanUploadAudio(boolean canUploadAudio)
  {
    this.canUploadAudio = canUploadAudio;
  }

  public boolean canUploadAiff()
  {
    return canUploadAiff;
  }

  public void setCanUploadAiff(boolean canUploadAiff)
  {
    this.canUploadAiff = canUploadAiff;
  }

  public boolean canAskQuestion()
  {
    return canAskQuestion;
  }

  public void setCanAskQuestion(boolean canAskQuestion)
  {
    this.canAskQuestion = canAskQuestion;
  }

  public boolean canUploadVideo()
  {
    return canUploadVideo;
  }

  public void setCanUploadVideo(boolean canUploadVideo)
  {
    this.canUploadVideo = canUploadVideo;
  }

  public int getMaxVideoBytesUploaded()
  {
    return maxVideoBytesUploaded;
  }

  public void setMaxVideoBytesUploaded(int maxVideoBytesUploaded)
  {
    this.maxVideoBytesUploaded = maxVideoBytesUploaded;
  }

  public int getLikedPostCount()
  {
    return likedPostCount;
  }

  public void setLikedPostCount(int likedPostCount)
  {
    this.likedPostCount = likedPostCount;
  }
}
