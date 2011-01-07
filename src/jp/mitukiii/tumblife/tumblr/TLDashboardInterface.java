package jp.mitukiii.tumblife.tumblr;

import java.util.HashMap;
import android.content.Context;
import android.os.Handler;
import jp.mitukiii.tumblife.model.TLPost;

public interface TLDashboardInterface
{
  public void init(TLDashboardDelegate delegate, Context context, Handler handler);
  
  public void start();
  
  public String getTitle();
  
  public TLPost postCurrent();
  
  public TLPost postCurrent(boolean showLastPost);

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
  
  public void writeRegular(String text, String body, HashMap<String, String> options);
  
  public void stop();
  
  public void restart();
  
  public void destroy();
  
  public boolean isLogined();
  
  public boolean isStoped();
  
  public boolean isDestroyed();
}
