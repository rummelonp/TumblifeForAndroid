package jp.mitukiii.tumblife2;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import android.os.Handler;
import jp.mitukiii.tumblife2.exeption.TLAuthenticationFailureException;
import jp.mitukiii.tumblife2.exeption.TLParserException;
import jp.mitukiii.tumblife2.exeption.TLSDCardNotFoundException;
import jp.mitukiii.tumblife2.model.TLPost;
import jp.mitukiii.tumblife2.model.TLSetting;
import jp.mitukiii.tumblife2.model.TLTumblelog;
import jp.mitukiii.tumblife2.model.TLUser;
import jp.mitukiii.tumblife2.model.TLSetting.VIEW_MODE_TYPE;
import jp.mitukiii.tumblife2.parser.TLPostParser;
import jp.mitukiii.tumblife2.parser.TLUserParser;
import jp.mitukiii.tumblife2.util.TLConnection;
import jp.mitukiii.tumblife2.util.TLLog;
import jp.mitukiii.tumblife2.util.TLPostFactory;

public class TLDashboard implements TLDashboardInterface
{
  protected static final String      HTTP_SCHEME   = "http://";
  protected static final String      HTTPS_SCHEME  = "https://";
  protected static final String      TUMBLR_URL    = "www.tumblr.com";
  protected static final String      AUTH_URL      = "/api/authenticate";
  protected static final String      DASHBOARD_URL = "/api/dashboard";
  protected static final String      LIKE_URL      = "/api/like";
  protected static final String      REBLOG_URL    = "/api/reblog";
  protected static final String      WRITE_URL     = "/api/write";

  protected static final int         SLEEP_TIME    = 2 * 1000;
  protected static final int         DURATION_TIME = 10 * 1000;
  protected static final int         START_MAX     = 250;
  protected static final int         LOAD_NUM      = 50;
  protected static final int         CALLBACK_NUM  = 5;

  protected TLDashboardDelegate      delegate;
  protected Context                  context;
  protected Handler                  handler;

  protected List<TLPost>             posts         = new ArrayList<TLPost>(300);
  protected int                      postIndex     = 0;
  protected int                      containsPostCount = 0;
  protected HashMap<Long, TLPost>    pinPosts      = new HashMap<Long, TLPost>();
  protected TLPostFactory            postFactory;
  protected TLSetting                setting;
  protected TLUser                   user;
  protected TLTumblelog              tumblelog;

  protected boolean                  isLogined;
  protected boolean                  isStoped;
  protected boolean                  isDestroyed;

  public TLDashboard(TLDashboardDelegate delegate, Context context, Handler handler)
  {
    this.delegate = delegate;
    this.context  = context;
    this.handler  = handler;
    postFactory   = TLPostFactory.getSharedInstance(context);
    setting       = TLSetting.getSharedInstance(context);
  }
  
  public void start()
  {
    TLLog.i("TLDashboard / start");
    
    new Thread() {
      public void run() {
        if (setting.getEmail().length() == 0 ||
            setting.getPassword().length() == 0)
        {
          TLLog.d("TLDashboard / start : No account.");
          handler.post(new Runnable() { public void run() { delegate.noAccount(); } });
          return;
        }
        if (!login()) {
          TLLog.d("TLDashboard / start : Login failed.");
          return;
        }
        Long beforeTime = null;
        while (true) {
          if (isDestroyed) {
            TLLog.d("TLDashboard / start : destroyed.");
            return;
          } else if (isStoped) {
            TLLog.d("TLDashboard / start : stoped.");
          } else if (posts.size() > START_MAX) {
            TLLog.d("TLDashboard / start : All posts loaded.");
            return;
          } else if (beforeTime == null || System.currentTimeMillis() - beforeTime > DURATION_TIME) {
            beforeTime = System.currentTimeMillis();
            try {
              load();
            } catch (TLSDCardNotFoundException e) {
              TLLog.e("TLDashboard / start", e);
              handler.post(new Runnable() { public void run() { delegate.noSDCard(); } });
              return;
            }
          }
          try {
            Thread.sleep(SLEEP_TIME);
          } catch (InterruptedException e) {
            TLLog.i("TLDashboard / start", e);
          }
        }
      }
    }.start();
  }
  
  protected boolean login()
  {
    TLLog.i("TLDashboard / login");
    
    isLogined = false;
    HttpURLConnection con = null;
    try {
      con = TLConnection.post(getTumblrUrl(AUTH_URL), getAccountParameters());
      if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new TLAuthenticationFailureException("Authentication failed.");
      }
      user = new TLUserParser(con.getInputStream()).parse();
      tumblelog = user.getPrimaryTumblelog();
      isLogined = true;
      handler.post(new Runnable() { public void run(){ delegate.loginSuccess(); } });
    } catch (SocketException e) {
      TLLog.i("TLDashboard / login", e);
      handler.post(new Runnable() { public void run(){ delegate.noInternet(); } });
    } catch (UnknownHostException e) {
      TLLog.i("TLDashboard / login", e);
      handler.post(new Runnable() { public void run(){ delegate.noInternet(); } });
    } catch (XmlPullParserException e) {
      TLLog.e("TLDashboard / login", e);
      handler.post(new Runnable() { public void run(){ delegate.loginFailure(); } });
    } catch (IOException e) {
      TLLog.e("TLDashboard / login", e);
      handler.post(new Runnable() { public void run(){ delegate.loginFailure(); } });
    } finally {
      if (con != null) {
        con.disconnect();
      }
    }
    return isLogined;
  }
  
  protected void load()
    throws TLSDCardNotFoundException
  {
    TLLog.i("TLDashboard / load");
    
    HttpURLConnection con = null;
    try {
      HashMap<String, String> parameters = getAccountParameters();
      parameters.put("start", String.valueOf(posts.size() + containsPostCount)); // Todo Check
      parameters.put("num", String.valueOf(LOAD_NUM));
      String type = setting.getViewMode().getType();
      if (type != null) {
        parameters.put("type", type);
      }
      con = TLConnection.get(getTumblrUrl(DASHBOARD_URL), parameters);
      if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new TLAuthenticationFailureException("Authentication failed.");
      }
      List<TLPost> _posts = new TLPostParser(con.getInputStream()).parse();
      if (_posts.size() == 0) {
        throw new TLParserException("Parsing failed.");
      }
      for (final TLPost post: _posts) {
        if (isDestroyed) {
          return;
        }
        if (posts.contains(post)) {
          containsPostCount += 1;
        } else {
          postFactory.addQueue(post);
          postFactory.makeHtmlFile(post);
          posts.add(post);
          if (posts.size() % CALLBACK_NUM == 0) {
            if (posts.size() == CALLBACK_NUM) {
              handler.post(new Runnable() { public void run(){ delegate.firstLoading(); } });
            } else {
              handler.post(new Runnable() { public void run(){ delegate.loading(); } });
            }
          }
        }
      }
      handler.post(new Runnable() { public void run(){ delegate.loadSuccess(); } });
    } catch (TLSDCardNotFoundException e) {
      throw e;
    } catch (NumberFormatException e) {
      TLLog.e("TLDashboard / load", e);
      handler.post(new Runnable() { public void run(){ delegate.loadFailure(); } });
    } catch (XmlPullParserException e) {
      TLLog.e("TLDashboard / load", e);
      handler.post(new Runnable() { public void run(){ delegate.loadFailure(); } });
    } catch (IOException e) {
      TLLog.e("TLDashboard / load", e);
      handler.post(new Runnable() { public void run(){ delegate.loadFailure(); } });
    } finally {
      if (con != null) {
        con.disconnect();
      }
    }
  }
  
  public TLPost postCurrent()
  {
    TLLog.v("TLDashboard / postCurrent");
    
    TLPost post = null;
    int _postIndex = postIndex;
    while (posts.size() > postIndex) {
      post = posts.get(postIndex);
      if (!isSkipPost(post)) {
        if (posts.size() > postIndex + 1) {
          postFactory.addQueueToFirst(posts.get(postIndex + 1));
        }
        return post;
      }
      postIndex += 1;
    }
    postIndex = _postIndex;
    return null;
  }
  
  public TLPost postNext()
  {
    TLLog.v("TLDashboard / postNext");
    
    TLPost post = null;
    int _postIndex = postIndex;
    postIndex += 1;
    while (posts.size() > postIndex) {
      post = posts.get(postIndex);
      if (!isSkipPost(post)) {
        if (posts.size() > postIndex + 1) {
          postFactory.addQueueToFirst(posts.get(postIndex + 1));
        }
        return post;
      }
      postIndex += 1;
    }
    postIndex = _postIndex;
    return null;
  }
  
  public TLPost postBack()
  {
    TLLog.v("TLDashboard / postBack");
    
    TLPost post = null;
    int _postIndex = postIndex;
    postIndex -= 1;
    while (posts.size() > 0 && postIndex >= 0) {
      post = posts.get(postIndex);
      if (!isSkipPost(post)) {
        if (postIndex - 1 >= 0) {
          postFactory.addQueueToFirst(posts.get(postIndex - 1));
        }
        return post;
      }
      postIndex -= 1;
    }
    postIndex = _postIndex;
    return null;
  }
  
  public TLPost postPin(TLPost post)
  {
    TLLog.v("TLDashboard / pinPost");
    
    if (pinPosts.containsKey(post.getId())) {
      pinPosts.remove(post.getId());
    } else {
      pinPosts.put(post.getId(), post);
    }
    
    switch (setting.getPinAction()) {
      case Back:
        return postBack();
      case Next:
        return postNext();
      case None:
        return postCurrent();
      default:
        return postCurrent();
    }
  }
  
  protected boolean isSkipPost(TLPost post)
  {
    if (setting.useSkipMinePost()) {
      if (tumblelog.getName().equals(post.getTumblelogName())) {
        return true;
      }
    }
    if (setting.useSkipPhotos()) {
      if (setting.getViewMode() == VIEW_MODE_TYPE.Default) {
        if (TLPost.TYPE_PHOTO.equals(post.getType())) {
          return true;
        }
      }
    }
    return false;
  }
  
  public String getTitle()
  {
    return TLMain.APP_NAME + ": " + String.valueOf(postIndex + 1) + "/" + String.valueOf(posts.size());
  }
  
  protected boolean like(TLPost post, boolean retry)
  {
    boolean result;
    HttpURLConnection con = null;
    try {
      HashMap<String, String> parameters = getAccountParameters();
      parameters.put("post-id", String.valueOf(post.getId()));
      parameters.put("reblog-key", post.getReblogKey());
      con = TLConnection.get(getTumblrUrl(LIKE_URL), parameters);
      if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new TLAuthenticationFailureException("Authentication failed.");
      }
      result = true;
    } catch (IOException e) {
      TLLog.e("TLDashboard / login", e);
      result = false;
    } finally {
      con.disconnect();
    }
    if (result) {
      return true;
    } else if (retry) {
      return like(post, false);
    } else {
      return false;
    }
  }
  
  public void like(final TLPost post)
  {
    TLLog.d("TLDashboard / like");
    
    new Thread() {
      public void run() {
        final String string;
        if (like(post, true)) {
          string = context.getString(R.string.like_success);
        } else {
          string = context.getString(R.string.like_failure);
        }
        handler.post(new Runnable() { public void run() { delegate.showToast(string); } });
      }
    }.start();
  }
  
  public void likeAll()
  {
    
  }
  
  protected boolean reblog(TLPost post, String comment, boolean retry)
  {
    boolean result;
    HttpURLConnection con = null;
    try {
      HashMap<String, String> parameters = getAccountParameters();
      parameters.put("post-id", String.valueOf(post.getId()));
      parameters.put("reblog-key", post.getReblogKey());
      if (comment != null && comment.length() > 0) {
        parameters.put("comment", comment);
      }
      con = TLConnection.post(getTumblrUrl(REBLOG_URL), parameters);
      if (con.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
        throw new TLAuthenticationFailureException("Authentication failed.");
      }
      result = true;
    } catch (IOException e) {
      TLLog.e("TLDashboard / login", e);
      result = false;
    } finally {
      con.disconnect();
    }
    if (result) {
      return true;
    } else if (retry) {
      return reblog(post, comment, false);
    } else {
      return false;
    }
  }
  
  public void reblog(final TLPost post, final String comment)
  {
    TLLog.d("TLDashboard / reblog");
    
    new Thread() {
      public void run() {
        final String string;
        if (reblog(post, comment, true)) {
          string = context.getString(R.string.reblog_success);
        } else {
          string = context.getString(R.string.reblog_failure);
        }
        handler.post(new Runnable() { public void run() { delegate.showToast(string); } });
      }
    }.start();
  }
  
  public void reblogAll()
  {
    
  }
  
  public void restart()
  {
    TLLog.i("TLDashboard / restart");
    
    isStoped = false;
  }
  
  public void stop()
  {
    TLLog.i("TLDashboard / stop");
    
    isStoped = true;
  }
  
  public void destroy()
  {
    TLLog.i("TLDashboard / destroy");
    
    postFactory.destroy();
    isDestroyed = true;
    Thread.interrupted();
  }
  
  public boolean isLogined()
  {
    return isLogined;
  }
  
  public boolean isStoped()
  {
    return isStoped;
  }
  
  public boolean isDestroyed()
  {
    return isDestroyed;
  }
  
  protected String getTumblrUrl(String url)
  {
    String scheme = setting.useSsl()? HTTPS_SCHEME: HTTP_SCHEME;
    return scheme + TUMBLR_URL + url;
  }
  
  protected HashMap<String, String> getAccountParameters()
  {
    HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put("email", setting.getEmail());
    parameters.put("password", setting.getPassword());
    return parameters;
  }
}
