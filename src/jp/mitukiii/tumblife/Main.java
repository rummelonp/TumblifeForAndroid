package jp.mitukiii.tumblife;

import java.util.HashMap;
import jp.mitukiii.tumblife.R;
import jp.mitukiii.tumblife.model.TLPost;
import jp.mitukiii.tumblife.model.TLSetting;
import jp.mitukiii.tumblife.tumblr.TLDashboard;
import jp.mitukiii.tumblife.tumblr.TLDashboardDelegate;
import jp.mitukiii.tumblife.tumblr.TLDashboardInterface;
import jp.mitukiii.tumblife.ui.TLWebViewClient;
import jp.mitukiii.tumblife.ui.TLWebViewClientDelegate;
import jp.mitukiii.tumblife.util.TLLog;
import jp.mitukiii.tumblife.util.TLPostFactory;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements TLDashboardDelegate, TLWebViewClientDelegate
{
  public static String                  APP_NAME;

  protected static TLDashboardInterface dashboard;

  protected TLDashboardDelegate         delegate;
  protected Context                     context;
  protected Handler                     handler;
  protected TLSetting                   setting;
  protected TLPostFactory               postFactory;
  protected TLPost                      currentPost;
  protected TLWebViewClient             webViewClient;

  protected WebView                     webView;
  protected LinearLayout                buttonBar;
  protected Button                      buttonLike;
  protected Button                      buttonReblog;
  protected Button                      buttonBack;
  protected Button                      buttonNext;
  protected Button                      buttonPin;

  protected ProgressDialog              progressLike;
  protected ProgressDialog              progressReblog;

  protected boolean                     isFinished;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    APP_NAME = getString(R.string.app_name);
    TLLog.i("Main / onCreate");

    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_RIGHT_ICON);
    setContentView(R.layout.main);

    delegate = this;
    context  = this;
    handler  = new Handler();
    setting  = TLSetting.getSharedInstance(context);
    postFactory = TLPostFactory.getSharedInstance(context);

    if (dashboard == null) {
      dashboard = new TLDashboard(this, context, handler);
    } else {
      dashboard.init(delegate, context, handler);
      dashboard.restart();
    }

    webViewClient = new TLWebViewClient(this, context, handler);
    webView = (WebView)findViewById(R.id.web_view);
    webView.setWebViewClient(webViewClient);
    webView.getSettings().setJavaScriptEnabled(true);

    buttonBar = (LinearLayout)findViewById(R.id.tumblr_buttons);

    buttonLike = (Button)findViewById(R.id.tumblr_button_like);
    buttonLike.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (dashboard.hasPinPosts()) {
          likeAll(true);
        } else {
          like();
        }
      }
    });

    buttonReblog = (Button)findViewById(R.id.tumblr_button_reblog);
    buttonReblog.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (dashboard.hasPinPosts()) {
          reblogAll(true);
        } else {
          reblog();
        }
      }
    });

    buttonBack = (Button)findViewById(R.id.tumblr_button_back);
    buttonBack.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) { movePost(dashboard.postBack()); }
    });

    buttonNext = (Button)findViewById(R.id.tumblr_button_next);
    buttonNext.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) { movePost(dashboard.postNext()); }
    });

    buttonPin = (Button)findViewById(R.id.tumblr_button_pin);
    buttonPin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        TLPost post = dashboard.postPin(currentPost);
        if (post == null) {
          post = currentPost;
        }
        movePost(post);
      }
    });
  }

  @Override
  protected void onResume()
  {
    super.onResume();

    setting.loadSetting(context);

    if (setting.usePin()) {
      buttonPin.setVisibility(View.VISIBLE);
      buttonPin.setEnabled(true);
    } else {
      buttonPin.setVisibility(View.GONE);
      buttonPin.setEnabled(false);
    }

    if (setting.hideButtonBar()) {
      buttonBar.setVisibility(View.GONE);
    } else {
      buttonBar.setVisibility(View.VISIBLE);
    }

    if (dashboard.isLogined()) {
      TLLog.i("Main / onResume : Logined.");
      TLPost post = dashboard.postCurrent();
      if (post == null) {
        setEnabledButtons(false);
        webView.loadUrl(postFactory.getDefaultHtmlUrl());
      } else {
        setEnabledButtons(true);
        movePost(post);
      }
    } else {
      TLLog.i("Main / onResume : start.");
      setEnabledButtons(false);
      webView.loadUrl(postFactory.getDefaultHtmlUrl());
      setTitle(dashboard.getTitle());
      setting.loadAccount(context);
      start();
    };
  }

  @Override
  protected void onStop()
  {
    TLLog.i("Main / onStop");

    super.onStop();
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();

    dashboard.stop();

    if (isFinished) {
      TLLog.i("Main / onDestroy : Finished.");
      dashboard.destroy();
      postFactory.stop();
      postFactory.deleteFiles();
      postFactory.destroy();
      System.exit(0);
    } else {
      TLLog.i("Main / onDestroy : Maintain dashboard.");
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    TLLog.d("Main / onCreateOptionsMenu");

    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.layout.main_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuOpened(int featureId, Menu menu)
  {
    TLLog.d("Main / onMenuOpened");

    return super.onMenuOpened(featureId, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    TLLog.d("Main / onOptionsItemSelected");

    switch (item.getItemId()) {
      case R.id.main_menu_setting:
        showSetting();
        break;
      case R.id.main_menu_about:
        showAbout();
        break;
      case R.id.main_menu_exit:
        isFinished = true;
        finish();
        break;
      case R.id.main_menu_reload:
        reload();
        break;
      case R.id.main_menu_moveto:
        moveTo();
        break;
      case R.id.main_menu_privatepost:
        privatePost();
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event)
  {
    int keyCode = event.getKeyCode();
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      if (keyCode == setting.getKeyCodeBackButton()) {
        movePost(dashboard.postBack());
        return true;
      } else if (keyCode == setting.getKeyCodeNextButton()) {
        movePost(dashboard.postNext());
        return true;
      } else if (keyCode == setting.getKeyCodePinButton()) {
        TLPost post = dashboard.postPin(currentPost);
        if (post == null) {
          post = currentPost;
        }
        movePost(post);
        return true;
      } else if (keyCode == setting.getKeyCodeLikeButton()) {
        if (dashboard.hasPinPosts()) {
          likeAll(true);
        } else {
          like();
        }
        return true;
      } else if (keyCode == setting.getKeyCodeReblogButton()) {
        if (dashboard.hasPinPosts()) {
          reblogAll(true);
        } else {
          reblog();
        }
        return true;
      }
    } else {
      if (keyCode == setting.getKeyCodeBackButton()) {
        return true;
      } else if (keyCode == setting.getKeyCodeNextButton()) {
        return true;
      } else if (keyCode == setting.getKeyCodePinButton()) {
        return true;
      } else if (keyCode == setting.getKeyCodeLikeButton()) {
        return true;
      } else if (keyCode == setting.getKeyCodeReblogButton()) {
        return true;
      }
    }

    return super.dispatchKeyEvent(event);
  }

  public void noAccount()
  {
    TLLog.i("Main / noAccount");

    new AlertDialog.Builder(context)
    .setTitle(R.string.login_no_account_title)
    .setMessage(R.string.login_no_account_message)
    .setPositiveButton(R.string.button_positive, new OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        showSetting();
      }
    })
    .setNegativeButton(R.string.button_negative, new OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) { }
    })
    .show();
  }

  public void noInternet()
  {
    TLLog.i("Main / noInternet");

    new AlertDialog.Builder(context)
    .setTitle(R.string.no_internet_title)
    .setMessage(R.string.no_internet_message)
    .setPositiveButton(R.string.button_ok, new OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {}
    })
    .show();
  }

  public void noSDCard()
  {
    TLLog.i("Main / noSDCard");

    new AlertDialog.Builder(context)
    .setTitle(R.string.no_sdcard_title)
    .setMessage(R.string.no_sdcard_message)
    .setPositiveButton(R.string.button_ok, new OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {}
    })
    .show();
  }

  public void loginSuccess()
  {
    TLLog.d("Main / loginSuccess");

    showToast(R.string.login_success);
    showToast(R.string.load);
    setTitle(dashboard.getTitle());
  }

  public void loginFailure()
  {
    TLLog.d("Main / loginFailure");

    new AlertDialog.Builder(context)
    .setTitle(R.string.login_failure_title)
    .setMessage(R.string.login_failure_message)
    .setPositiveButton(R.string.button_positive, new OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        showSetting();
      }
    })
    .setNeutralButton(R.string.button_retry, new OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        reload();
      }
    })
    .setNegativeButton(R.string.button_negative, new OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) { }
    })
    .show();
  }

  public void loading()
  {
    TLLog.v("Main / loading");

    movePost(dashboard.postCurrent(false));
  }

  public void loadSuccess()
  {
    TLLog.d("Main / loadSuccess");

    showToast(R.string.load_success);
  }

  public void loadAllSuccess()
  {
    TLLog.d("Main / loadAllSuccess");

    showToast(R.string.loadall_success);
  }

  public void loadFailure()
  {
    TLLog.d("Main / loadFailure");

    showToast(R.string.load_failure);
  }

  public void loadError()
  {
    TLLog.d("Main / loadError");

    new AlertDialog.Builder(context)
    .setTitle(R.string.load_error_title)
    .setMessage(R.string.load_error_message)
    .setPositiveButton(R.string.button_positive, new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dashboard.start();
      }
    })
    .setNegativeButton(R.string.button_negative, new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
      }
    })
    .show();
  }

  public void likeSuccess()
  {
    TLLog.d("Main / likeSuccess");

    showToast(R.string.like_success);
  }

  public void likeFailure()
  {
    TLLog.d("Main / likeFailure");

    showToast(R.string.like_failure);
  }

  public void likeMinePost()
  {
    TLLog.d("Main / likeMinePost");

    showToast(R.string.like_minepost);
  }

  public void likeAllSuccess()
  {
    TLLog.d("Main / likeAllSuccess");

    setTitle(dashboard.getTitle());
    if (progressLike != null) {
      progressLike.dismiss();
    }
    showToast(R.string.likeall_success);
  }

  public void likeAllFailure()
  {
    TLLog.d("Main / likeAllFailure");

    setTitle(dashboard.getTitle());
    if (progressLike != null) {
      progressLike.dismiss();
    }

    new AlertDialog.Builder(context)
    .setTitle(R.string.likeall_failure_title)
    .setMessage(String.format(getString(R.string.likeall_failure_message), dashboard.getPinPostsCount()))
    .setPositiveButton(R.string.button_positive, new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        likeAll(false);
      }
    })
    .setNegativeButton(R.string.button_negative, new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
      }
    })
    .show();
  }

  public void reblogSuccess()
  {
    TLLog.d("Main / reblogSuccess");

    showToast(R.string.reblog_success);
  }

  public void reblogFailure()
  {
    TLLog.d("Main / reblogFailure");

    showToast(R.string.reblog_failure);
  }

  public void reblogAllSuccess()
  {
    TLLog.d("Main / reblogAllSuccess");

    setTitle(dashboard.getTitle());
    if (progressReblog != null) {
      progressReblog.dismiss();
    }
    showToast(R.string.reblogall_success);
  }

  public void reblogAllFailure()
  {
    TLLog.d("Main / reblogAllFailure");

    setTitle(dashboard.getTitle());
    if (progressReblog != null) {
      progressReblog.dismiss();
    }

    new AlertDialog.Builder(context)
    .setTitle(R.string.reblogall_failure_title)
    .setMessage(String.format(getString(R.string.reblogall_failure_message), dashboard.getPinPostsCount()))
    .setPositiveButton(R.string.button_positive, new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        reblogAll(false);
      }
    })
    .setNegativeButton(R.string.button_negative, new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
      }
    })
    .show();
  }

  public void writeSuccess()
  {
    TLLog.d("Main / writeSuccess");

    showToast(R.string.write_success);
  }

  public void writeFailure()
  {
    TLLog.d("Main / writeFailure");

    showToast(R.string.write_failure);
  }

  public void startActivityFailure()
  {
    TLLog.d("Main / startActivityFailure");

    showToast(R.string.startactivity_failure);
  }

  public void showNewPosts(String text)
  {
    TLLog.d("Main / showNewPosts");

    showToast(String.format(getString(R.string.new_posts), text));
  }

  public void showLastPost(TLPost post)
  {
    TLLog.d("Main / showLastPost");

    String text = getString(R.string.last_post);
    text = text.replace(":index", String.valueOf(post.getIndex() + 1));
    text = text.replace(":id", String.valueOf(post.getId()));
    showToast(text);
  }

  protected void setEnabledButtons(boolean enabled)
  {
    buttonLike.setEnabled(enabled);
    buttonReblog.setEnabled(enabled);
    buttonBack.setEnabled(enabled);
    buttonNext.setEnabled(enabled);
    buttonPin.setEnabled(enabled);
  }

  protected void reload()
  {
    TLLog.d("Main / reload");

    setEnabledButtons(false);
    setting.loadAccount(context);
    setting.loadSetting(context);
    postFactory.stop();
    postFactory.destroy();
    postFactory = TLPostFactory.getSharedInstance(context);
    dashboard.stop();
    dashboard.destroy();
    dashboard = new TLDashboard(this, context, handler);
    webView.loadUrl(postFactory.getDefaultHtmlUrl());
    setTitle(dashboard.getTitle());
    start();
  }

  protected void start()
  {
    if (setting.getEmail().length() == 0 ||
        setting.getPassword().length() == 0)
    {
      TLLog.i("Main / start : No account.");
      noAccount();
    } else {
      TLLog.i("Main / start : Login.");
      showToast(R.string.login);
      dashboard.start();
    }
  }

  protected void showToast(int resid)
  {
    showToast(getString(resid));
  }

  protected void showToast(String text)
  {
    TLLog.d("Main / showToast : " + text);

    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
  }

  protected void showSetting()
  {
    TLLog.d("Main / showSetting");

    Intent intent = new Intent(context, Setting.class);
    startActivity(intent);
  }

  protected void showAbout()
  {
    TLLog.d("Main / showAbout");

    TextView textViewAbout = new TextView(context);
    textViewAbout.setTextSize(15);
    textViewAbout.setAutoLinkMask(Linkify.ALL);
    textViewAbout.setText(R.string.about_message);

    new AlertDialog.Builder(context)
    .setTitle(R.string.about_title)
    .setView(textViewAbout)
    .setPositiveButton(R.string.button_ok, new OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {}
    })
    .show();
  }

  protected void movePost(TLPost post)
  {
    setTitle(dashboard.getTitle());

    if (post == null) {
      return;
    }

    setEnabledButtons(true);

    if (dashboard.isPinPost(post)) {
      getWindow().setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, R.drawable.pin);
    } else {
      getWindow().setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, R.drawable.none);
    }

    if (post == currentPost ||
        post.getFileUrl().equals(webView.getUrl()))
    {
      return;
    }

    TLLog.d("Main / movePost : index / " + post.getIndex());

    webView.loadUrl(post.getFileUrl());

    currentPost = post;
  }

  protected void moveTo()
  {
    TLLog.d("Main / moveTo");

    new AlertDialog.Builder(context)
    .setTitle(R.string.moveto_title)
    .setItems(getResources().getStringArray(R.array.moveto_items), new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        TLPost post = dashboard.moveTo(which);
        if (post == null) {
          showToast(R.string.moveto_failure);
        } else {
          movePost(post);
        }
      }
    })
    .show();
  }

  protected void like()
  {
    if (currentPost == null) {
      return;
    }

    if (currentPost.getTumblelogName() != null &&
        currentPost.getTumblelogName().equals(dashboard.getTumblelog().getName()))
    {
      likeMinePost();
      return;
    }

    TLLog.d("Main / like : index / " + currentPost.getIndex());

    if (setting.useQuickpost()) {
      showToast(R.string.like);
      dashboard.like(currentPost);
    } else {
      new AlertDialog.Builder(context)
      .setTitle(R.string.like_title)
      .setPositiveButton(R.string.button_positive, new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton)
        {
          showToast(R.string.like);
          dashboard.like(currentPost);
        }
      })
      .setNegativeButton(R.string.button_negative, new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {}
      })
      .show();
    }
  }

  protected void likeAll(boolean showAlert)
  {
    if (!dashboard.hasPinPosts()) {
      return;
    }

    TLLog.d("Main / likeAll");

    progressLike = new ProgressDialog(context);
    progressLike.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressLike.setTitle(R.string.likeall_progress_title);
    progressLike.setCancelable(false);
    progressLike.incrementProgressBy(0);
    progressLike.setSecondaryProgress(0);
    progressLike.setMax(dashboard.getPinPostsCount());

    final Handler handlerLike = new Handler() {
      public void handleMessage(Message message) {
        progressLike.setSecondaryProgress(1);
        if ((Boolean) message.obj) {
          progressLike.incrementProgressBy(1);
        }
      }
    };

    if (showAlert) {
      new AlertDialog.Builder(context)
      .setTitle(R.string.likeall_title)
      .setMessage(String.format(getString(R.string.likeall_message), dashboard.getPinPostsCount()))
      .setPositiveButton(R.string.button_positive, new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          progressLike.show();
          dashboard.likeAll(handlerLike);
        }
      })
      .setNeutralButton(R.string.button_likeone, new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          like();
        }
      })
      .setNegativeButton(R.string.button_cancel, new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {}
      })
      .show();
    } else {
      progressLike.show();
      dashboard.likeAll(handlerLike);
    }
  }

  protected void reblog()
  {
    if (currentPost == null) {
      return;
    }

    TLLog.d("Main / reblog : index / " + currentPost.getIndex());

    if (setting.useQuickpost()) {
      showToast(R.string.reblog);
      dashboard.reblog(currentPost, null);
    } else {
      final EditText editTextReblog = new EditText(context);
      new AlertDialog.Builder(context)
      .setTitle(R.string.reblog_title)
      .setView(editTextReblog)
      .setPositiveButton(R.string.button_positive,new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton)
        {
          String comment = editTextReblog.getText().toString();
          showToast(R.string.reblog);
          dashboard.reblog(currentPost, comment);
        }
      })
      .setNegativeButton(R.string.button_negative, new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {}
      })
      .show();
    }
  }

  protected void reblogAll(boolean showAlert)
  {
    if (!dashboard.hasPinPosts()) {
      return;
    }

    TLLog.d("Main / reblogAll");

    progressReblog = new ProgressDialog(context);
    progressReblog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressReblog.setTitle(R.string.reblogall_progress_title);
    progressReblog.setCancelable(false);
    progressReblog.incrementProgressBy(0);
    progressReblog.setSecondaryProgress(0);
    progressReblog.setMax(dashboard.getPinPostsCount());

    final Handler handlerReblog = new Handler() {
      public void handleMessage(Message message) {
        progressReblog.setSecondaryProgress(1);
        if ((Boolean) message.obj) {
          progressReblog.incrementProgressBy(1);
        }
      }
    };

    if (showAlert) {
      new AlertDialog.Builder(context)
      .setTitle(R.string.reblogall_title)
      .setMessage(String.format(getString(R.string.reblogall_message), dashboard.getPinPostsCount()))
      .setPositiveButton(R.string.button_positive, new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          progressReblog.show();
          dashboard.reblogAll(handlerReblog);
        }
      })
      .setNeutralButton(R.string.button_reblogone, new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          reblog();
        }
      })
      .setNegativeButton(R.string.button_cancel, new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {}
      })
      .show();
    } else {
      progressReblog.show();
      dashboard.reblogAll(handlerReblog);
    }
  }

  protected void privatePost()
  {
    if (!dashboard.isLogined()) {
      return;
    }

    TLLog.d("Main / privatePost");

    String text = setting.getPrivatePostText();
    if (text == null || text.length() == 0) {
      text = getString(R.string.setting_privateposttext_default);
    }
    HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put("private", "1");
    showToast(R.string.write);
    dashboard.writeRegular(text, null, parameters);
  }
}