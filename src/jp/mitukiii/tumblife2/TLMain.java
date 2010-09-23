package jp.mitukiii.tumblife2;

import jp.mitukiii.tumblife2.model.TLPost;
import jp.mitukiii.tumblife2.model.TLSetting;
import jp.mitukiii.tumblife2.util.TLLog;
import jp.mitukiii.tumblife2.util.TLPostFactory;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TLMain extends Activity implements TLDashboardDelegate, TLWebViewClientDelegate
{
  public static String      APP_NAME;
  
  protected static TLDashboard     dashboard;

  protected Context         context;
  protected Handler         handler;
  protected TLSetting       setting;
  protected TLPostFactory   postFactory;
  protected TLPost          currentPost;
  protected TLWebViewClient webViewClient;

  protected WebView         webView;
  protected Button          buttonLike;
  protected Button          buttonReblog;
  protected Button          buttonBack;
  protected Button          buttonNext;
  protected Button          buttonPin;

  protected AlertDialog     alertNoAccount;
  protected AlertDialog     alertNoInternet;
  protected AlertDialog     alertNoSDCard;
  protected AlertDialog     alertLoginFailure;
  protected AlertDialog     alertMoveTo;
  protected AlertDialog     alertLike;
  protected AlertDialog     alertLikeAll;
  protected AlertDialog     alertLikeAllFailure;
  protected AlertDialog     alertReblog;
  protected AlertDialog     alertReblogAll;
  protected AlertDialog     alertReblogAllFailure;

  protected ProgressDialog  progressLike;
  protected ProgressDialog  progressReblog;
  
  protected Handler         handlerLike;
  protected Handler         handlerReblog;
  
  protected EditText        editTextReblog;
  
  protected boolean         isFinished;
    
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    APP_NAME = getString(R.string.app_name);
    TLLog.i("TLMain / onCreate");
    
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_RIGHT_ICON);
    setContentView(R.layout.main);
    
    context = this;
    handler = new Handler();
    setting = TLSetting.getSharedInstance(context);
    postFactory = TLPostFactory.getSharedInstance(context);
    
    if (dashboard == null) {
      dashboard = new TLDashboard(this, context, handler);
    }
    
    webViewClient = new TLWebViewClient(this, context, handler);
    webView = (WebView)findViewById(R.id.web_view);
    webView.setWebViewClient(webViewClient);
    webView.getSettings().setJavaScriptEnabled(true);
    
    buttonLike = (Button)findViewById(R.id.tumblr_button_like);
    buttonLike.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (dashboard.hasPinPosts()) {
          likeAll();
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
          reblogAll();
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
      public void onClick(View view) { movePost(dashboard.postPin(currentPost)); }
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
    
    if (dashboard.isLogined()) {
      TLLog.i("TLMain / onResume : Logined.");
      setEnabledButtons(true);
      movePost(dashboard.postCurrent());
    } else {
      TLLog.i("TLMain / onResume : login.");
      setEnabledButtons(false);
      webView.loadUrl(postFactory.getDefaultHtmlUrl());
      setTitle(dashboard.getTitle());
      showToast(getString(R.string.login));
      setting.loadAccount(context);
      dashboard.start();
    };
  }
  
  @Override
  protected void onStop()
  {
    TLLog.i("TLMain / onStop");
    
    super.onStop();
  }
  
  @Override
  protected void onDestroy()
  {
    TLLog.i("TLMain / onDestroy");
    
    super.onDestroy();
    
    if (isFinished) {
      dashboard.stop();
      dashboard.destroy();
      postFactory.stop();
      postFactory.deleteFiles();
      postFactory.destroy();
      System.exit(0);
    }
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    TLLog.d("TLMain / onCreateOptionsMenu");
    
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.layout.main_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }
  
  @Override
  public boolean onMenuOpened(int featureId, Menu menu)
  {
    TLLog.d("TLMain / onMenuOpened");
    
    return super.onMenuOpened(featureId, menu);
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    TLLog.d("TLMain / onOptionsItemSelected");
    
    switch (item.getItemId()) {
      case R.id.main_menu_setting:
        showSetting();
        break;
      case R.id.main_menu_about:
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
      case R.id.main_menu_post:
        break;
    }
    
    return super.onOptionsItemSelected(item);
  }
  
  public void noAccount()
  {
    TLLog.i("TLMain / noAccount");
    
    if (alertNoAccount == null) {
      alertNoAccount = new AlertDialog.Builder(context)
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
      .create();
    }
    alertNoAccount.show();
  }
  
  public void noInternet()
  {
    TLLog.i("TLMain / noInternet");
    
    if (alertNoInternet == null) {
      alertNoInternet = new AlertDialog.Builder(context)
      .setTitle(R.string.no_internet_title)
      .setMessage(R.string.no_internet_message)
      .setPositiveButton(R.string.button_ok, new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {}
      })
      .create();
    }
    alertNoInternet.show();
  }
  
  public void noSDCard()
  {
    TLLog.i("TLMain / noSDCard");
    
    if (alertNoSDCard == null) {
      alertNoSDCard = new AlertDialog.Builder(context)
      .setTitle(R.string.no_sdcard_title)
      .setMessage(R.string.no_sdcard_message)
      .setPositiveButton(R.string.button_ok, new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {}
      })
      .create();
    }
    alertNoSDCard.show();
  }
  
  public void loginSuccess()
  {
    TLLog.d("TLMain / loginSuccess");
    
    showToast(getString(R.string.login_success));
    showToast(getString(R.string.load));
    setTitle(dashboard.getTitle());
  }
  
  public void loginFailure()
  {
    TLLog.d("TLMain / loginFailure");
    
    if (alertLoginFailure == null) {
      alertLoginFailure = new AlertDialog.Builder(context)
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
      .create();
    }
    alertLoginFailure.show();
  }
  
  public void loading()
  {
    TLLog.v("TLMain / loading");
    
    movePost(dashboard.postCurrent());
  }
  
  public void loadSuccess()
  {
    TLLog.d("TLMain / loadSuccess");
    
    showToast(getString(R.string.load_success));
  }
  
  public void loadAllSuccess()
  {
    TLLog.d("TLMain / loadAllSuccess");
    
    showToast(getString(R.string.loadall_success));
  }
  
  public void loadFailure()
  {
    TLLog.d("TLMain / loadFailure");
    
    showToast(getString(R.string.load_failure));
  }
  
  public void likeSuccess()
  {
    TLLog.d("TLMain / likeSuccess");
    
    showToast(getString(R.string.like_success));
  }
  
  public void likeFailure()
  {
    TLLog.d("TLMain / likeFailure");
    
    showToast(getString(R.string.like_failure));
  }
  
  public void likeAllSuccess()
  {
    TLLog.d("TLMain / likeAllSuccess");
    
    setTitle(dashboard.getTitle());
    if (progressLike != null) {
      progressLike.dismiss();
    }
    showToast(getString(R.string.likeall_success));
  }
  
  public void likeAllFailure()
  {
    TLLog.d("TLMain / likeAllFailure");
    
    setTitle(dashboard.getTitle());
    if (progressLike != null) {
      progressLike.dismiss();
    }
    
    if (alertLikeAllFailure == null) {
      alertLikeAllFailure = new AlertDialog.Builder(context)
      .setTitle(R.string.likeall_failure_title)
      .setPositiveButton(R.string.button_positive, new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          likeAll();
        }
      })
      .setNegativeButton(R.string.button_negative, new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
        }
      })
      .create();
    }
    alertLikeAllFailure.setMessage(String.format(getString(R.string.likeall_failure_message), dashboard.getPinPostsCount()));
    alertLikeAllFailure.show();
  }
  
  public void reblogSuccess()
  {
    TLLog.d("TLMain / reblogSuccess");
    
    showToast(getString(R.string.reblog_success));
  }
  
  public void reblogFailure()
  {
    TLLog.d("TLMain / reblogFailure");
    
    showToast(getString(R.string.reblog_failure));
  }
  
  public void reblogAllSuccess()
  {
    TLLog.d("TLMain / reblogAllSuccess");
    
    setTitle(dashboard.getTitle());
    if (progressReblog != null) {
      progressReblog.dismiss();
    }
    showToast(getString(R.string.reblogall_success));
  }
  
  public void reblogAllFailure()
  {
    TLLog.d("TLMain / reblogAllFailure");
    
    setTitle(dashboard.getTitle());
    if (progressReblog != null) {
      progressReblog.dismiss();
    }
    
    if (alertReblogAllFailure == null) {
      alertReblogAllFailure = new AlertDialog.Builder(context)
      .setTitle(R.string.reblogall_failure_title)
      .setPositiveButton(R.string.button_positive, new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          reblogAll();
        }
      })
      .setNegativeButton(R.string.button_negative, new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
        }
      })
      .create();
    }
    alertReblogAllFailure.setMessage(String.format(getString(R.string.reblogall_failure_message), dashboard.getPinPostsCount()));
    alertReblogAllFailure.show();
  }
  
  public void startActivityFailure()
  {
    TLLog.d("TLMain / startActivityFailure");
    
    showToast(getString(R.string.startactivity_failure));
  }
  
  public void showNewPosts(String text)
  {
    TLLog.d("TLMain / showNewPosts");
    
    showToast(String.format(getString(R.string.new_posts), text));
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
    TLLog.d("TLMain / reload");
    
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
    showToast(getString(R.string.login));
    dashboard.start();
  }
  
  protected void showToast(String text)
  {
    TLLog.d("TLMain / showToast : " + text);
    
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
  }
  
  protected void showSetting()
  {
    TLLog.d("TLMain / showSetting");
    
    Intent intent = new Intent(context, TLSettingManager.class);
    startActivity(intent);
  }
  
  protected void movePost(TLPost post)
  {
    setTitle(dashboard.getTitle());
    
    if (post == null ||
        post == currentPost ||
        post.getFileUrl().equals(webView.getUrl()))
    {
      return;
    }
    
    TLLog.d("TLMain / movePost : index / " + post.getIndex());
    
    setEnabledButtons(true);
    webView.loadUrl(post.getFileUrl());
    
    if (dashboard.isPinPost(post)) {
      getWindow().setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, R.drawable.pin);
    } else {
      getWindow().setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, R.drawable.none);
    }
    
    if (post.getId() == setting.getLastPostId()) {
      showToast(String.format(getString(R.string.last_post), post.getIndex() + 1, post.getId()));
    }
    
    currentPost = post;
  }
  
  protected void moveTo()
  {
    TLLog.d("TLMain / moveTo");
    
    if (alertMoveTo == null) {
      alertMoveTo = new AlertDialog.Builder(context)
      .setTitle(R.string.moveto_title)
      .setItems(getResources().getStringArray(R.array.moveto_items), new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          TLPost post = dashboard.moveTo(which);
          if (post == null) {
            showToast(getString(R.string.moveto_failure));
          } else {
            movePost(post);
          }
        }
      })
      .create(); 
    }
    alertMoveTo.show();
  }
  
  protected void like()
  {
    if (currentPost == null) {
      return;
    }
    
    TLLog.d("TLMain / like : index / " + currentPost.getIndex());
    
    if (setting.useQuickpost()) {
      showToast(getString(R.string.like));
      dashboard.like(currentPost);
    } else {
      if (alertLike == null) {
        alertLike = new AlertDialog.Builder(context)
        .setTitle(R.string.like_title)
        .setPositiveButton(R.string.button_positive, new OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton)
          {
            showToast(getString(R.string.like));
            dashboard.like(currentPost);
          }
        })
        .setNegativeButton(R.string.button_negative, new OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {}
        })
        .create();
      }
      alertLike.show();
    }
  }
  
  protected void likeAll()
  {
    if (!dashboard.hasPinPosts()) {
      return;
    }
    
    TLLog.d("TLMain / likeAll");
    
    if (progressLike == null) {
      progressLike = new ProgressDialog(context);
      progressLike.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      progressLike.setTitle(R.string.likeall_progress_title);
      progressLike.incrementProgressBy(0);
      progressLike.setSecondaryProgress(0);
      progressLike.setMax(dashboard.getPinPostsCount());
      progressLike.setCancelable(false);
    }
    
    if (handlerLike == null) {
      handlerLike = new Handler() {
        public void handleMessage(Message message) {
          progressLike.setSecondaryProgress(1);
          if ((Boolean) message.obj) {
            progressLike.incrementProgressBy(1);
          }
        }
      };
    }
    
    if (alertLikeAll == null) {
      alertLikeAll = new AlertDialog.Builder(context)
      .setTitle(R.string.likeall_title)
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
      .create();
    }
    alertLikeAll.setMessage(String.format(getString(R.string.likeall_message), dashboard.getPinPostsCount()));
    alertLikeAll.show();
  }
  
  protected void reblog()
  {
    if (currentPost == null) {
      return;
    }
    
    TLLog.d("TLMain / reblog : index / " + currentPost.getIndex());
    
    if (setting.useQuickpost()) {
      showToast(getString(R.string.reblog));
      dashboard.reblog(currentPost, null);
    } else {
      if (editTextReblog == null) {
        editTextReblog = new EditText(context);
      }
      if (alertReblog == null) {
        alertReblog = new AlertDialog.Builder(context)
        .setTitle(R.string.reblog_title)
        .setView(editTextReblog)
        .setPositiveButton(R.string.button_positive,new OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton)
          {
            String comment = editTextReblog.getText().toString();
            showToast(getString(R.string.reblog));
            dashboard.reblog(currentPost, comment);
          }
        })
        .setNegativeButton(R.string.button_negative, new OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {}
        })
        .create();
      }
      editTextReblog.setText("");
      alertReblog.show();
    }
  }
  
  protected void reblogAll()
  {
    if (!dashboard.hasPinPosts()) {
      return;
    }
    
    TLLog.d("TLMain / reblogAll");
    
    if (progressReblog == null) {
      progressReblog = new ProgressDialog(context);
      progressReblog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      progressReblog.setTitle(R.string.reblogall_progress_title);
      progressReblog.incrementProgressBy(0);
      progressReblog.setSecondaryProgress(0);
      progressReblog.setMax(dashboard.getPinPostsCount());
      progressReblog.setCancelable(false);
    }
    
    if (handlerReblog == null) {
      handlerReblog = new Handler() {
        public void handleMessage(Message message) {
          progressReblog.setSecondaryProgress(1);
          if ((Boolean) message.obj) {
            progressReblog.incrementProgressBy(1);
          }
        }
      };
    }
    
    if (alertReblogAll == null) {
      alertReblogAll = new AlertDialog.Builder(context)
      .setTitle(R.string.reblogall_title)
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
      .create();
    }
    alertReblogAll.setMessage(String.format(getString(R.string.reblogall_message), dashboard.getPinPostsCount()));
    alertReblogAll.show();
  }
}