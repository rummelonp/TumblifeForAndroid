package jp.mitukiii.tumblife2.tumblr;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import jp.mitukiii.tumblife2.Main;
import jp.mitukiii.tumblife2.R;
import jp.mitukiii.tumblife2.exeption.TLAuthenticationFailureException;
import jp.mitukiii.tumblife2.exeption.TLParserException;
import jp.mitukiii.tumblife2.exeption.TLSDCardNotFoundException;
import jp.mitukiii.tumblife2.model.TLPost;
import jp.mitukiii.tumblife2.model.TLSetting;
import jp.mitukiii.tumblife2.model.TLTumblelog;
import jp.mitukiii.tumblife2.model.TLUser;
import jp.mitukiii.tumblife2.model.TLSetting.DASHBOARD_TYPE;
import jp.mitukiii.tumblife2.parser.TLPostParser;
import jp.mitukiii.tumblife2.parser.TLUserParser;
import jp.mitukiii.tumblife2.util.TLConnection;
import jp.mitukiii.tumblife2.util.TLLog;
import jp.mitukiii.tumblife2.util.TLPostFactory;

public class TLDashboard implements TLDashboardInterface
{
  public static enum MOVE_TO {
    FirstPost (0),
    LastPost (1),
    LastSession (2),
    NextMyPost (3);
    
    private int which;
    
    private MOVE_TO(int which)
    {
      this.which = which;
    }
    
    public int getWhich()
    {
      return which;
    }
    
    public static MOVE_TO valueOf(int which)
    {
      for (MOVE_TO item: values()) {
        if (item.getWhich() == which) {
          return item;
        }
      }
      return null;
    }
  }
  
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

  protected boolean                  isLastPostLoaded;
  protected int                      lastPostIndex;
  
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
            handler.post(new Runnable() { public void run() { delegate.loadAllSuccess(); } });
            if (!isLastPostLoaded) {
              handler.post(new Runnable() { public void run() { delegate.showNewPosts(posts.size() + "+"); } });
            }
            return;
          } else if (beforeTime == null || System.currentTimeMillis() - beforeTime > DURATION_TIME) {
            beforeTime = System.currentTimeMillis();
            try {
              load();
            } catch (TLSDCardNotFoundException e) {
              TLLog.i("TLDashboard / start", e);
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
      parameters.put("start", String.valueOf(posts.size() + containsPostCount));
      parameters.put("num", String.valueOf(LOAD_NUM));
      String type = setting.getDashboardType().getType();
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
      if (posts.size() == 0) {
        setting.saveLastPostId(context, _posts.get(0).getId());
      }
      for (final TLPost post: _posts) {
        if (isDestroyed) {
          return;
        }
        if (posts.contains(post)) {
          containsPostCount += 1;
        } else {
          post.setIndex(posts.size());
          postFactory.addQueue(post);
          postFactory.makeHtmlFile(post);
          posts.add(post);
          if (posts.size() % CALLBACK_NUM == 0) {
            handler.post(new Runnable() { public void run(){ delegate.loading(); } });
          }
          if (!isLastPostLoaded && post.getId() <= setting.getLastPostId()) {
            setting.setLastPostId(post.getId());
            handler.post(new Runnable() {
              public void run() {
                delegate.showNewPosts((post.getIndex() == 0)? "No": String.valueOf(post.getIndex()));
              }
            });
            lastPostIndex = post.getIndex();
            isLastPostLoaded = true;
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
  
  public String getTitle()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(Main.APP_NAME + ": ");
    if (setting.getDashboardType() != DASHBOARD_TYPE.Default) {
      sb.append(setting.getDashboardType().getType() + ": ");
    }
    if (isLogined) {
      if (posts.size() > 0) {
        sb.append((postIndex + 1) + "/" + posts.size());
        if (pinPosts.size() > 0) {
          sb.append(" (" + pinPosts.size() + " pin)");
        }
      } else {
        sb.append(context.getString(R.string.load));
      }
    } else {
      sb.append(context.getString(R.string.login));
    }
    return sb.toString();
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
    if (post == null) {
      return null;
    }
    
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
  
  public TLPost moveTo(int which)
  {
    switch (MOVE_TO.valueOf(which)) {
      case FirstPost:
        postIndex = 0;
        return postCurrent();
      case LastPost:
        postIndex = posts.size();
        return postBack();
      case LastSession:
        if (isLastPostLoaded) {
          postIndex = lastPostIndex;
          return postCurrent();
        }
        return null;
      case NextMyPost:
        TLPost post = null;
        int _postIndex = postIndex;
        _postIndex += 1;
        while (posts.size() > _postIndex) {
          post = posts.get(_postIndex);
          if (post.getTumblelogName().equals(tumblelog.getName())) {
            postIndex = _postIndex;
            return postCurrent();
          }
          _postIndex += 1;
        }
        return null;
    }
    return null;
  }
  
  public int getPinPostsCount()
  {
    TLLog.v("TLDashboard / getPinPostsCount");
    
    return pinPosts.size();
  }
  
  public boolean hasPinPosts()
  {
    TLLog.v("TLDashboard / hasPinPosts");
    
    return !pinPosts.isEmpty();
  }
  
  public boolean isPinPost(TLPost post)
  {
    TLLog.v("TLDashboard / isPinPost");
    
    return pinPosts.containsKey(post.getId());
  }
  
  protected boolean isSkipPost(TLPost post)
  {
    if (setting.useSkipMinePost()) {
      if (tumblelog.getName().equals(post.getTumblelogName())) {
        return true;
      }
    }
    if (setting.useSkipPhotos()) {
      if (setting.getDashboardType() == DASHBOARD_TYPE.Default) {
        if (TLPost.TYPE_PHOTO.equals(post.getType())) {
          return true;
        }
      }
    }
    return false;
  }
  
  protected boolean like(TLPost post, boolean retry)
  {
    TLLog.d("TLDashboard / like : index / " + post.getIndex());
    
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
      TLLog.e("TLDashboard / like", e);
      result = false;
    } finally {
      if (con != null) {
        con.disconnect();
      }
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
    new Thread() {
      public void run() {
        if (like(post, true)) {
          handler.post(new Runnable() { public void run() { delegate.likeSuccess(); } });
        } else {
          handler.post(new Runnable() { public void run() { delegate.likeFailure(); } });
        }
        
      }
    }.start();
  }
  
  public void likeAll(final Handler progressHandler)
  {
    new Thread()
    {
      public void run()
      {
        Iterator<Long> iterator = pinPosts.keySet().iterator();
        while (iterator.hasNext()) {
          long key = (long) iterator.next();
          TLPost post = pinPosts.get(key);
          Message message = new Message();
          if (like(post, false)) {
            message.obj = true;
            iterator.remove();
          } else {
            message.obj = false;
          }
          progressHandler.sendMessage(message);
        }
        if (pinPosts.isEmpty()) {      
          handler.post(new Runnable() { public void run() { delegate.likeAllSuccess(); } });
        } else {
          handler.post(new Runnable() { public void run() { delegate.likeFailure(); } });
        }
      }
    }.start();
  }
  
  protected boolean reblog(TLPost post, String comment, boolean retry)
  {
    TLLog.d("TLDashboard / reblog : index / " + post.getIndex() + " : comment / " + comment);
    
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
      TLLog.e("TLDashboard / reblog", e);
      result = false;
    } finally {
      if (con != null) {
        con.disconnect();
      }
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
    new Thread() {
      public void run() {
        if (reblog(post, comment, true)) {
          handler.post(new Runnable() { public void run() { delegate.reblogSuccess(); } });
        } else {
          handler.post(new Runnable() { public void run() { delegate.reblogFailure(); } });
        }
      }
    }.start();
  }
  
  public void reblogAll(final Handler progressHandler)
  {
    new Thread()
    {
      public void run()
      {
        Iterator<Long> iterator = pinPosts.keySet().iterator();
        while (iterator.hasNext()) {
          long key = (long) iterator.next();
          TLPost post = pinPosts.get(key);
          Message message = new Message();
          if (reblog(post, null, false)) {
            message.obj = true;
            iterator.remove();
          } else {
            message.obj = false;
          }
          progressHandler.sendMessage(message);
        }
        if (pinPosts.isEmpty()) {      
          handler.post(new Runnable() { public void run() { delegate.reblogAllSuccess(); } });
        } else {
          handler.post(new Runnable() { public void run() { delegate.reblogAllFailure(); } });
        }
      }
    }.start();
  }
  
  public void writeRegular(final String title, final String body, final HashMap<String, String> options)
  {
    if ((title == null || title.length() == 0) &&
        (body == null || body.length() == 0))
    {
      return;
    }
    
    TLLog.d("TLDashboard / write : title / " + title + " : body / " + body);
    
    new Thread() {
      public void run() {
        HttpURLConnection con = null;
        try {
          HashMap<String, String> parameters = getAccountParameters();
          parameters.putAll(options);
          parameters.put("type", "regular");
          if (title != null) {
            parameters.put("title", title);
          }
          if (body != null) {
            parameters.put("bodt", body);
          }
          con = TLConnection.get(getTumblrUrl(WRITE_URL), parameters);
          if (con.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
            throw new TLAuthenticationFailureException("Authentication failed.");
          }
          handler.post(new Runnable() { public void run() { delegate.writeSuccess(); }});
        } catch (IOException e) {
          TLLog.e("TLDashboard / write", e);
          handler.post(new Runnable() { public void run() { delegate.writeFailure(); }});
        } finally {
          if (con != null) {
            con.disconnect();
          }
        }
      }
    }.start();
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
    
    isDestroyed = true;
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
