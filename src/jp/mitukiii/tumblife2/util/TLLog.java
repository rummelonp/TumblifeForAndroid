package jp.mitukiii.tumblife2.util;

import jp.mitukiii.tumblife2.TLMain;
import android.util.Log;

public class TLLog
{ 
  public static final boolean IS_DEBUG  = true;
  
  public static int e(String msg)
  {
    return Log.e(TLMain.APP_NAME, msg);
  }
  
  public static int e(String msg, Throwable tr)
  {
    return Log.e(TLMain.APP_NAME, msg, tr);
  }
  
  public static int w(String msg)
  {
    return Log.w(TLMain.APP_NAME, msg);
  }
  
  public static int w(String msg, Throwable tr)
  {
    return Log.w(TLMain.APP_NAME, msg, tr);
  }
  
  public static int i(String msg)
  {
    return Log.i(TLMain.APP_NAME, msg);
  }
  
  public static int i(String msg, Throwable tr)
  {
    return Log.i(TLMain.APP_NAME, msg, tr);
  }
  
  public static int d(String msg)
  {
    if (IS_DEBUG) {
      return Log.d(TLMain.APP_NAME, msg);
    } else {
      return Log.DEBUG;
    }
  }
  
  public static int d(String msg, Throwable tr)
  {
    if (IS_DEBUG) {
      return Log.d(TLMain.APP_NAME, msg, tr);
    } else {
      return Log.DEBUG;
    }
  }
  
  public static int v(String msg)
  {
    if (IS_DEBUG) {
      return Log.v(TLMain.APP_NAME, msg);
    } else {
      return Log.VERBOSE;
    }
  }
  
  public static int v(String msg, Throwable tr)
  {
    if (IS_DEBUG) {
      return Log.v(TLMain.APP_NAME, msg, tr);
    } else {
      return Log.VERBOSE;
    }
  }
}
