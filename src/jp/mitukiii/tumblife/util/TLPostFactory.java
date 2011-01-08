package jp.mitukiii.tumblife.util;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import android.content.Context;
import android.content.res.AssetManager;
import jp.mitukiii.tumblife.exeption.TLSDCardNotFoundException;
import jp.mitukiii.tumblife.model.TLPost;
import jp.mitukiii.tumblife.model.TLSetting;

public class TLPostFactory
{ 
  protected static final int     SLEEP_TIME       = 1 * 1000;
  protected static final int     MAX_THREAD_COUNT = 3;

  protected static TLPostFactory postFactory;

  protected List<TLPost>         posts            = new LinkedList<TLPost>();
  protected TLSetting            setting;
  protected Context              context;

  protected String               postHeaders;

  protected String               defaultHtmlName = "default.html";
  protected String               defaultHtmlUrl;
  protected String               defaultHtml;

  protected int                  threadCount;

  protected boolean              isStoped;
  protected boolean              isDestroyed;

  protected TLPostFactory(Context context)
  {
    this.context = context;
    setting = TLSetting.getSharedInstance(context);
    copyAssetHeaderFiles();
    start();
  }

  public static TLPostFactory getSharedInstance(Context context)
  {
    if (postFactory == null) {
      postFactory = new TLPostFactory(context);
    }
    return postFactory;
  }

  public String getDefaultHtmlName()
  {
    TLLog.v("TLPostFactory / getDefaultHtmlName");

    return defaultHtmlName;
  }

  public String getDefaultHtml()
  {
    TLLog.v("TLPostFactory / getDefaultHtml");

    if (defaultHtml != null) {
      return defaultHtml;
    }
    defaultHtml = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                postHeaders +
                "</head>\n" +
                "<body>\n" +
                "</body>\n" +
                "</html>";
    return defaultHtml;
  }

  public String getDefaultHtmlUrl()
  {
    TLLog.v("TLPostFactory / getDefaultHtmlUrl");

    return defaultHtmlUrl;
  }

  public void makeHtmlFile(TLPost post)
    throws TLSDCardNotFoundException, IOException
  {
    if (post == null) {
      return;
    }

    TLLog.v("TLPostFactory / makeHtmlFile");

    String fileUrl = TLExplorer.makeHtmlFile(post.getFileName(), post.getHtml(postHeaders), post.isPhoto());
    post.setFileUrl(fileUrl);
  }

  public void makeImageFile(TLPost post)
    throws TLSDCardNotFoundException, IOException
  {
    if (post == null || !post.isPhoto()) {
      return;
    }

    TLLog.v("TLPostFactory / makeImageFile");

    String photoUrl = post.getPhotoUrlMaxWidth400();
    String imageFileName;
    String imageFileUrl;
    if (photoUrl.endsWith(TLExplorer.IMAGE_GIF_EXTENSION)) {
      imageFileName = TLExplorer.getPreffix(photoUrl) + "." + TLExplorer.IMAGE_GIF_EXTENSION;
      imageFileUrl = TLExplorer.makeGifImageFile(photoUrl, imageFileName);
    } else {
      imageFileName = TLExplorer.getPreffix(photoUrl) + "." + TLExplorer.IMAGE_PNG_EXTENSION;
      imageFileUrl = TLExplorer.makePngImageFile(photoUrl, imageFileName);
    }
    post.setImageFileUrl(imageFileUrl);
  }

  public void addQueue(TLPost post)
  {
    addQueueToLast(post);
  }

  public void addQueueToFirst(TLPost post)
  {
    if (!setting.useSavePhotos() ||
        post == null ||
        !post.isPhoto() ||
        post.getImageFileUrl() != null)
    {
      return;
    }

    TLLog.v("TLPostFactory / addQueueToFirst");

    if (posts.contains(post)) {
      posts.remove(post);
    }
    posts.add(0, post);
  }

  public void addQueueToLast(TLPost post)
  {
    if (!setting.useSavePhotos() ||
        post == null ||
        !post.isPhoto() ||
        post.getImageFileUrl() != null)
    {
      return;
    }

    TLLog.v("TLPostFactory / addQueueToLast");

    if (posts.contains(post)) {
      posts.remove(post);
    }
    posts.add(post);
  }

  public void addQueues(List<TLPost> _posts)
  {
    if (!setting.useSavePhotos() ||
        _posts == null) {
      return;
    }

    TLLog.v("TLPostFactory / addQueues");

    for (TLPost post: _posts) {
      if (post.isPhoto() &&
          post.getImageFileUrl() == null)
      {
        if (!posts.contains(post)) {
          posts.add(post);
        }
      }
    }
  }

  public void start()
  {
    TLLog.d("TLPostFactory / start");

    new Thread() {
      public void run() {
        while (true) {
          if (isDestroyed) {
            TLLog.d("TLPostFactory / start : destroyed.");
            return;
          } else if (isStoped) {
            TLLog.d("TLPostFactory / start : stoped.");
          } else {
            int index = 0;
            TLLog.v("TLPostFactory / start : running. : Thread count / " + threadCount + " : Queue count / " + posts.size());
            while (posts.size() > index && MAX_THREAD_COUNT > threadCount) {
              TLPost post = posts.get(index);
              makePostFiles(post);
              threadCount += 1;
              index += 1;
            }
          }
          try {
            Thread.sleep(SLEEP_TIME);
          } catch (InterruptedException e) {
            TLLog.i("TLPostFactory / start : interrupted.", e);
            return;
          }
        }
      }
    }.start();
  }

  public void stop()
  {
    TLLog.d("TLPostFactory / stop");

    isStoped = true;
  }

  public void destroy()
  {
    TLLog.d("TLPostFactory / destroy");

    isDestroyed = true;
    postFactory = null;
  }

  public void deleteFiles()
  { 
    if (!setting.useClearCache()) {
      return;
    }

    TLLog.d("TLPostFactory / deleteFiles");

    TLExplorer.deleteFiles(new File(TLExplorer.HTML_DIR));
    TLExplorer.deleteFiles(new File(TLExplorer.IMAGE_DIR));
  }

  protected void copyAssetHeaderFiles()
  {
    TLLog.d("TLPostFactory / copyAssetHeaderFiles");

    AssetManager manager = context.getAssets();
    String fileUrl;
    StringBuffer sb = new StringBuffer();
    try {
      fileUrl = TLExplorer.makeFile(TLExplorer.CSS_DIR, "default.css", manager.open("default.css"), true);
      sb.append("<link rel=\"stylesheet\" href=\"" + fileUrl + "\" type=\"text/css\">\n");
    } catch (IOException e) {
      TLLog.w("TLPostFactory / copyAssetHeaderFiles", e);
    }
    try {
      fileUrl = TLExplorer.makeFile(TLExplorer.CSS_DIR, "color.css", manager.open("color.css"), false);
      sb.append("<link rel=\"stylesheet\" href=\"" + fileUrl + "\" type=\"text/css\">\n");
    } catch (IOException e) {
      TLLog.w("TLPostFactory / copyAssetHeaderFiles", e);
    }
    try {
      fileUrl = TLExplorer.makeFile(TLExplorer.JS_DIR, "application.js", manager.open("application.js"), true);
      sb.append("<script src=\"" + fileUrl + "\" type=\"text/javascript\"></script>\n");
    } catch (IOException e) {
      TLLog.w("TLPostFactory / copyAssetHeaderFiles", e);
    }
    postHeaders = sb.toString();

    try {
      defaultHtmlUrl = TLExplorer.makeHtmlFile(getDefaultHtmlName(), getDefaultHtml(), true);
    } catch (IOException e) {
      TLLog.w("TLPostFactory / copyAssetHeaderFiles", e);
    }
  }

  protected void makePostFiles(final TLPost post)
  {
    TLLog.v("TLPostFactory / makePostFiles : Thread count / " + threadCount);

    new Thread() {
      public void run() {
        try {
          makeImageFile(post);
          makeHtmlFile(post);
        } catch (TLSDCardNotFoundException e) {
          TLLog.w("TLPostFactory / makePostFiles", e);
          stop();
        } catch (IOException e) {
          TLLog.w("TLPostFactory / makePostFiles", e);
        }
        if (posts.contains(post)) {
          posts.remove(post);
        }
        threadCount -= 1;
      }
    }.start();
  }
}
