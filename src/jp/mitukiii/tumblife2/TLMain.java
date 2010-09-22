package jp.mitukiii.tumblife2;

import jp.mitukiii.tumblife2.model.TLPost;
import jp.mitukiii.tumblife2.model.TLSetting;
import jp.mitukiii.tumblife2.util.TLLog;
import jp.mitukiii.tumblife2.util.TLPostFactory;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TLMain extends Activity implements TLDashboardDelegate
{
  public static String  APP_NAME;

  protected Context     context;
  protected Handler     handler;
  protected TLSetting   setting;
  protected TLDashboard dashboard;
  protected TLPost      currentPost;

  protected WebView     webView;
  protected Button      buttonLike;
  protected Button      buttonReblog;
  protected Button      buttonBack;
  protected Button      buttonNext;
  protected Button      buttonPin;

  protected AlertDialog alertNoAccount;
  protected AlertDialog alertNoInternet;
  protected AlertDialog alertNoSDCard;
  protected AlertDialog alertLoginFailure;

  protected AlertDialog alertLike;
  protected AlertDialog alertReblog;
  protected EditText    editTextReblog;
    
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    APP_NAME = getString(R.string.app_name);
    TLLog.i("TLMain / onCreate");
    
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    context = this;
    handler = new Handler();
    setting = TLSetting.getSharedInstance(context);
    dashboard = new TLDashboard(this, this, handler);
    
    webView = (WebView)findViewById(R.id.web_view);
    webView.setWebViewClient(new WebViewClient());
    webView.getSettings().setJavaScriptEnabled(true);
    
    buttonLike = (Button)findViewById(R.id.tumblr_button_like);
    buttonLike.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) { like(); }
    });
    
    buttonReblog = (Button)findViewById(R.id.tumblr_button_reblog);
    buttonReblog.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) { reblog(); }
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
    TLLog.i("TLMain / onResume");
    
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
      movePost(currentPost);
    } else {
      TLPostFactory postFactory = TLPostFactory.getSharedInstance(context);
      webView.loadUrl(postFactory.getDefaultHtmlPath());
      setting.loadAccount(context);
      showToast(getString(R.string.login));
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
    
    dashboard.destroy();
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
        break;
      case R.id.main_menu_reload:
        break;
      case R.id.main_menu_moveto:
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
      .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
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
      .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
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
      .setNegativeButton(R.string.button_negative, new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) { }
      })
      .create();
    }
    alertLoginFailure.show();
  }
  
  public void firstLoading()
  {
    TLLog.v("TLMain / firstLoading");
    
    movePost(dashboard.postCurrent());
    loading();
  }
  
  public void loading()
  {
    TLLog.v("TLMain / loading");
    
    setTitle(dashboard.getTitle());
  }
  
  public void loadSuccess()
  {
    TLLog.d("TLMain / loadSuccess");
    
    showToast(getString(R.string.load_success));
  }
  
  public void loadFailure()
  {
    TLLog.d("TLMain / loadFailure");
    
    showToast(getString(R.string.load_failure));
  }
  
  protected void movePost(TLPost post)
  {
    TLLog.d("TLMain / movePost");
    
    if (post == null) {
      return;
    }
    if (post.getFileUrl().equals(webView.getUrl())) {
      return;
    }
    currentPost = post;
    webView.loadUrl(currentPost.getFileUrl());
    setTitle(dashboard.getTitle());
  }
  
  protected void like()
  {
    TLLog.i("TLMain / like");
    
    if (currentPost == null) {
      return;
    }
    
    if (setting.useQuickpost()) {
      showToast(getString(R.string.like));
      dashboard.like(currentPost);
    } else {
      if (alertLike == null) {
        alertLike = new AlertDialog.Builder(context)
        .setTitle(R.string.like_title)
        .setPositiveButton(R.string.button_positive, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton)
          {
            showToast(getString(R.string.like));
            dashboard.like(currentPost);
          }
        })
        .setNegativeButton(R.string.button_negative, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {}
        })
        .create();
      }
      alertLike.show();
    }
  }
  
  protected void reblog()
  {
    TLLog.i("TLMain / reblog");
    
    if (currentPost == null) {
      return;
    }
    
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
        .setPositiveButton(R.string.button_positive,new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton)
          {
            String comment = editTextReblog.getText().toString();
            showToast(getString(R.string.reblog));
            dashboard.reblog(currentPost, comment);
          }
        })
        .setNegativeButton(R.string.button_negative, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {}
        })
        .create();
      }
      editTextReblog.setText("");
      alertReblog.show();
    }
  }
  
  public void showToast(String string)
  {
    TLLog.d("TLMain / showToast : " + string);
    
    Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
  }
  
  protected void showSetting()
  {
    Intent intent = new Intent(context, TLSettingManager.class);
    startActivity(intent);
  }
}