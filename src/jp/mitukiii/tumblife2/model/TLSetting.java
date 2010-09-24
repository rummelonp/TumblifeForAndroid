package jp.mitukiii.tumblife2.model;

import jp.mitukiii.tumblife2.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class TLSetting extends TLModel
{
  public static enum PIN_ACTION {
    None,
    Back,
    Next
  };
  
  public static enum DASHBOARD_TYPE {
    Default (null),
    Quote ("quote"),
    Photo ("photo"),
    Text ("text"),
    Link ("link"),
    Chat ("chat"),
    Video ("video"),
    Audio ("audio");
    
    private String type;
    
    private DASHBOARD_TYPE(String type)
    {
      this.type = type;
    }
    
    public String getType()
    {
      return type;
    }
  };
  
  public static enum SEND_TO {
    Confirmation (2),
    Browser (0),
    Share (1);
    
    private int which;
    
    private SEND_TO(int which)
    {
      this.which = which;
    }
    
    public int getWhich()
    {
      return which;
    }
    
    public static SEND_TO valueOf(int which)
    {
      for (SEND_TO item: values()) {
        if (item.getWhich() == which) {
          return item;
        }
      }
      return null;
    }
  };
  
  protected static TLSetting settingManager;

  protected String           email;
  protected String           password;
  protected long             lastPostId;

  protected boolean          useSsl;
  protected boolean          useQuickpost;
  protected boolean          usePin;
  protected PIN_ACTION       pinAction;
  protected DASHBOARD_TYPE   dashboardType;
  protected boolean          useSkipMinePost;
  protected boolean          useSkipPhotos;
  protected boolean          useSavePhotos;
  protected SEND_TO          sendTo;
  protected String           privatePostText;
  protected boolean          useClearCache;
  
  protected TLSetting(Context context)
  {
    loadAccount(context);
    loadSetting(context);
  }
  
  public static TLSetting getSharedInstance(Context context)
  {
    if (settingManager == null) {
      settingManager = new TLSetting(context);
    }
    return settingManager;
  }
  
  public void loadAccount(Context context)
  {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    
    email = preferences.getString(
        context.getString(R.string.setting_email_key),
        "");
    
    password = preferences.getString(
        context.getString(R.string.setting_password_key),
        "");
    
    lastPostId = preferences.getLong(
        context.getString(R.string.setting_lastpostid_key),
        0);
    
    String viewModeString = preferences.getString(
        context.getString(R.string.setting_dashboardtype_key),
        context.getString(R.string.setting_dashboardtype_default));
    dashboardType = DASHBOARD_TYPE.valueOf(viewModeString);
  }
  
  public void loadSetting(Context context)
  {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    
    useSsl = preferences.getBoolean(
        context.getString(R.string.setting_usessl_key),
        Boolean.valueOf(context.getString(R.string.setting_usessl_default)));
    
    useQuickpost = preferences.getBoolean(
        context.getString(R.string.setting_quickpost_key),
        Boolean.valueOf(context.getString(R.string.setting_quickpost_default)));
    
    usePin = preferences.getBoolean(
        context.getString(R.string.setting_usepin_key),
        Boolean.valueOf(context.getString(R.string.setting_usepin_default)));
    
    useSkipMinePost = preferences.getBoolean(
        context.getString(R.string.setting_skipminepost_key),
        Boolean.valueOf(context.getString(R.string.setting_skipminepost_default)));
    
    useSkipPhotos = preferences.getBoolean(
        context.getString(R.string.setting_skipphotos_key),
        Boolean.valueOf(context.getString(R.string.setting_skipphotos_default)));
    
    useSavePhotos = preferences.getBoolean(
        context.getString(R.string.setting_savephotos_key),
        Boolean.valueOf(context.getString(R.string.setting_savephotos_default)));
    
    privatePostText = preferences.getString(
        context.getString(R.string.setting_privateposttext_key),
        context.getString(R.string.setting_privateposttext_default));
    
    useClearCache = preferences.getBoolean(
        context.getString(R.string.setting_clearcache_key),
        Boolean.valueOf(context.getString(R.string.setting_clearcache_default)));
    
    String pinActionString = preferences.getString(
        context.getString(R.string.setting_pinaction_key),
        context.getString(R.string.setting_pinaction_default));
    pinAction = PIN_ACTION.valueOf(pinActionString);
    
    String sendToString = preferences.getString(
        context.getString(R.string.setting_sendto_key),
        context.getString(R.string.setting_sendto_default));
    sendTo = SEND_TO.valueOf(sendToString);
  }

  public String getEmail()
  {
    return email;
  }

  public String getPassword()
  {
    return password;
  }
  
  public long getLastPostId()
  {
    return lastPostId;
  }
  
  public void setLastPostId(long lastPostId)
  {
    this.lastPostId = lastPostId;
  }
  
  public boolean saveLastPostId(Context context, long lastPostId)
  {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    Editor editor = preferences.edit();
    editor.putLong(context.getString(R.string.setting_lastpostid_key), lastPostId);
    return editor.commit();
  }

  public boolean useSsl()
  {
    return useSsl;
  }

  public boolean useQuickpost()
  {
    return useQuickpost;
  }

  public boolean usePin()
  {
    return usePin;
  }

  public PIN_ACTION getPinAction()
  {
    return pinAction;
  }

  public DASHBOARD_TYPE getDashboardType()
  {
    return dashboardType;
  }

  public boolean useSkipMinePost()
  {
    return useSkipMinePost;
  }

  public boolean useSkipPhotos()
  {
    return useSkipPhotos;
  }

  public boolean useSavePhotos()
  {
    return useSavePhotos;
  }
  
  public SEND_TO getSendTo()
  {
    return sendTo;
  }
  
  public String getPrivatePostText()
  {
    return privatePostText;
  }

  public boolean useClearCache()
  {
    return useClearCache;
  }
}
