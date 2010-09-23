package jp.mitukiii.tumblife2;

import android.os.Handler;
import jp.mitukiii.tumblife2.model.TLPost;

public interface TLDashboardInterface
{
  public void start();
  
  public String getTitle();
  
  public TLPost postCurrent();
  
  public TLPost postNext();
  
  public TLPost postBack();
  
  public TLPost postPin(TLPost post);
  
  public TLPost moveTo(int which);
  
  public int getPinPostsCount();
  
  public boolean hasPinPosts();
  
  public boolean isPinPost(TLPost post);
  
  public void like(TLPost post);
  
  public void likeAll(Handler progressHandler);
  
  public void reblog(TLPost post, String comment);
  
  public void reblogAll(Handler progressHandler);
  
  public void stop();
  
  public void restart();
  
  public void destroy();
  
  public boolean isLogined();
  
  public boolean isStoped();
  
  public boolean isDestroyed();
}
