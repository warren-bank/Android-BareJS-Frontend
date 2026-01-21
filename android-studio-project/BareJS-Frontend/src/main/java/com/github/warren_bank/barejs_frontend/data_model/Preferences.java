package com.github.warren_bank.barejs_frontend.data_model;

import com.github.warren_bank.barejs_frontend.R;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public final class Preferences {
  private static final String PREFS_FILENAME = "PREFS";
  private static final String PREF_APPS_EXEC = "APPS_EXEC";
  private static final String PREF_APPS_FORK = "APPS_FORK";

  private static String getAppsPrefKey(boolean isDaemon) {
    return isDaemon ? PREF_APPS_FORK : PREF_APPS_EXEC;
  }

  private static SharedPreferences getSharedPreferences(Context context) {
    return context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
  }

  private static ArrayList<BareJsApp> initializeBareJsApps(Context context, boolean isDaemon) {
    ArrayList<BareJsApp> listItems = new ArrayList<BareJsApp>();

    if (isDaemon) {
      int max_barejs_daemons = context.getResources().getInteger( R.integer.max_barejs_daemons );
      String formatter = "%0" + String.format("%d", max_barejs_daemons).length() + "d";
      String id;
      for (int i=1; i <= max_barejs_daemons; i++) {
        id = String.format(formatter, i);
        listItems.add( new BareJsApp(id) );
      }
    }

    setBareJsApps(context, isDaemon, listItems);
    return listItems;
  }

  public static ArrayList<BareJsApp> getBareJsApps(Context context, boolean isDaemon) {
    SharedPreferences sharedPreferences = getSharedPreferences(context);
    String PREF_KEY                     = getAppsPrefKey(isDaemon);
    String JSON                         = sharedPreferences.getString(PREF_KEY, null);

    return (JSON == null)
      ? initializeBareJsApps(context, isDaemon)
      : BareJsApp.fromJson(JSON)
    ;
  }

  public static void setBareJsApps(Context context, boolean isDaemon, ArrayList<BareJsApp> listItems) {
    SharedPreferences sharedPreferences   = getSharedPreferences(context);
    String PREF_KEY                       = getAppsPrefKey(isDaemon);
    String JSON                           = BareJsApp.toJson(listItems);
    SharedPreferences.Editor prefs_editor = sharedPreferences.edit();
    prefs_editor.putString(PREF_KEY, JSON);
    prefs_editor.commit();
  }

  public static void resetBareJsApps(Context context) {
    initializeBareJsApps(context, true);
    initializeBareJsApps(context, false);
  }
}
