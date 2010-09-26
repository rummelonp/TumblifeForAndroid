package jp.mitukiii.tumblife2;

import jp.mitukiii.tumblife2.model.TLSetting.DASHBOARD_TYPE;
import jp.mitukiii.tumblife2.util.TLLog;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;

public class Setting extends PreferenceActivity
{ 
  protected Context context;
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.layout.setting);
    
    context = this;
    
    ListPreference viewMode = (ListPreference) findPreference(getString(R.string.setting_dashboardtype_key));
    viewMode.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue)
      {
        TLLog.d("onPreferenceChange");
        ListPreference viewMode = (ListPreference) preference;
        viewMode.setValue((String)newValue);
        togglePreference(preference);
        return true;
      }
    });
  }
  
  @Override
  protected void onResume()
  {
    super.onResume();
    togglePreference(findPreference(getString(R.string.setting_usepin_key)));
    togglePreference(findPreference(getString(R.string.setting_dashboardtype_key)));
  }
  
  @Override
  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
  {
    togglePreference(preference);
    return super.onPreferenceTreeClick(preferenceScreen, preference);
  }
  
  
  protected void togglePreference(Preference preference)
  {
    String key = preference.getKey();
    if (getString(R.string.setting_usepin_key).equals(key)) {
      CheckBoxPreference usePin = (CheckBoxPreference) preference;
      ListPreference pinAction = (ListPreference) findPreference(getString(R.string.setting_pinaction_key));
      if (usePin.isChecked()) {
        pinAction.setEnabled(true);
      } else {
        pinAction.setEnabled(false);
      }
    } else if (getString(R.string.setting_dashboardtype_key).equals(key)) {
      ListPreference viewMode = (ListPreference) preference;
      CheckBoxPreference skipPhotos = (CheckBoxPreference) findPreference(getString(R.string.setting_skipphotos_key));
      if (DASHBOARD_TYPE.Default == DASHBOARD_TYPE.valueOf(viewMode.getValue())) {
        skipPhotos.setEnabled(true);
      } else {
        skipPhotos.setEnabled(false);
      }
    }
  }
}
