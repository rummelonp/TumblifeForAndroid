package jp.mitukiii.tumblife;

import jp.mitukiii.tumblife.R;
import jp.mitukiii.tumblife.util.TLLog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

public class HardkeySetting extends PreferenceActivity
{
  protected Context context;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.layout.hardkey_setting);

    context = this;

    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

    int[] keys = {R.string.hardkey_setting_likebutton_key,
                  R.string.hardkey_setting_reblogbutton_key,
                  R.string.hardkey_setting_backbutton_key,
                  R.string.hardkey_setting_nextbutton_key,
                  R.string.hardkey_setting_pinbutton_key};

    for (int key_id : keys) {
      String key = getString(key_id);
      Preference preference = findPreference(key);
      int value = preferences.getInt(key, KeyEvent.KEYCODE_UNKNOWN);
      KeyCodeMap keyCodeMap = KeyCodeMap.valueOf(value);
      String summary = keyCodeMap.getName();
      preference.setSummary(summary);
    }
  }

  @Override
  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, final Preference preference)
  {
    if (getString(R.string.hardkey_setting_hidebuttonbar_key).equals(preference.getKey())) {
      return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    final Editor editor = preferences.edit();

    final int keyCode = preferences.getInt(preference.getKey(), KeyEvent.KEYCODE_UNKNOWN);

    final TextView textView = new TextView(context);
    textView.setText(KeyCodeMap.valueOf(keyCode).getName());
    textView.setTextSize(15);
    textView.setPadding(20, 5, 20, 5);

    AlertDialog dialog = new AlertDialog(context) {
      public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_MENU) {
          showToast(R.string.hardkey_setting_menu);
        } else {
          editor.putInt(preference.getKey(), keyCode);
          textView.setText(KeyCodeMap.valueOf(keyCode).getName());
        }
        return true;
      }
    };
    dialog.setTitle(preference.getTitle());
    dialog.setView(textView);
    dialog.setButton(getString(R.string.button_ok), new OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        editor.commit();
        int keyCode = preferences.getInt(preference.getKey(), KeyEvent.KEYCODE_UNKNOWN);
        preference.setSummary(KeyCodeMap.valueOf(keyCode).getName());
      }
    });
    dialog.setButton2(getString(R.string.button_cancel), new OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        editor.putInt(preference.getKey(), keyCode);
        editor.commit();
      }
    });
    dialog.setButton3(getString(R.string.button_clear), new OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        editor.putInt(preference.getKey(), KeyEvent.KEYCODE_UNKNOWN);
        editor.commit();
        preference.setSummary(KeyCodeMap.KEYCODE_UNKNOWN.getName());
      }
    });
    dialog.show();

    return super.onPreferenceTreeClick(preferenceScreen, preference);
  }

  protected void showToast(int resid)
  {
    showToast(getString(resid));
  }

  protected void showToast(String text)
  {
    TLLog.d("HardkeySetting / showToast : " + text);

    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
  }
}
