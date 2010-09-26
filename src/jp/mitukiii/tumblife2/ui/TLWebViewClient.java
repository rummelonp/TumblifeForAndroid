package jp.mitukiii.tumblife2.ui;

import jp.mitukiii.tumblife2.R;
import jp.mitukiii.tumblife2.model.TLSetting;
import jp.mitukiii.tumblife2.util.TLLog;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Handler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TLWebViewClient extends WebViewClient
{
  protected TLWebViewClientDelegate delegate;
  protected Context                 context;
  protected Handler                 handler;

  protected TLSetting               setting;
  
  public TLWebViewClient(TLWebViewClientDelegate delegate, Context context, Handler handler)
  {
    this.delegate = delegate;
    this.context = context;
    this.handler = handler;
    setting = TLSetting.getSharedInstance(context);
  }
  
  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    TLLog.v("TLMain / WebViewClient / shouldOverrideUrlLoading : url / " + url);
    if (url.matches("^https?://.+?")) {
      switch (setting.getSendTo()) {
        case Confirmation:
          alertSendTo(url);
          break;
        case Browser:
          sendToView(url);
          break;
        case Share:
          sendToShare(url);
          break;
      }
      return true;
    }
    return super.shouldOverrideUrlLoading(view, url);
  }
  
  protected void alertSendTo(final String url)
  {
    TLLog.d("TLWebViewClient / alertSendTo : url / " + url);
    
    new AlertDialog.Builder(context)
    .setTitle(R.string.sendto_title)
    .setItems(context.getResources().getStringArray(R.array.sendto_items), new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        switch (TLSetting.SEND_TO.valueOf(which)) {
          case Browser:
            sendToView(url);
            break;
          case Share:
            sendToShare(url);
            break;
          default:
            sendToView(url);
            break;
        }
      }
    })
    .show();
  }
  
  protected void sendToView(String url)
  {
    TLLog.d("TLWebViewClient / sendToView : url / " + url);
    
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    startActivity(intent);
  }
  
  protected void sendToShare(String url)
  {
    TLLog.d("TLWebViewClient / sendToShare : url / " + url);
    
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_TEXT, url);    
    startActivity(intent);
  }
  
  protected void startActivity(Intent intent)
  {
    try {
      context.startActivity(intent);
    } catch (ActivityNotFoundException e) {
      TLLog.i("TLWebViewClient / startActivity", e);
      handler.post(new Runnable() { public void run() { delegate.startActivityFailure(); } });
    }
  }
}
