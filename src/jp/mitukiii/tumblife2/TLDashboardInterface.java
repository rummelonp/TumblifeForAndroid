package jp.mitukiii.tumblife2;

import jp.mitukiii.tumblife2.model.TLPost;

public interface TLDashboardInterface
{
  public void start();
  
  public TLPost postCurrent();
  
  public TLPost postNext();
  
  public TLPost postBack();
  
  public TLPost postPin(TLPost post);
  
  public String getTitle();
  
  public void like(TLPost post);
  
  public void likeAll();
  
  public void reblog(TLPost post, String comment);
  
  public void reblogAll();
  
  public void stop();
  
  public void restart();
  
  public void destroy();
  
  public boolean isLogined();
  
  public boolean isStoped();
  
  public boolean isDestroyed();
}
