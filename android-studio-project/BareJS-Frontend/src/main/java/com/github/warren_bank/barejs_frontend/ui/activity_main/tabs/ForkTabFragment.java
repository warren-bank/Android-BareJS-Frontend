package com.github.warren_bank.barejs_frontend.ui.activity_main.tabs;

import com.github.warren_bank.barejs_frontend.R;
import com.github.warren_bank.barejs_frontend.data_model.BareJsApp;
import com.github.warren_bank.barejs_frontend.helpers.ProcessMgr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class ForkTabFragment extends AbstractTabFragment {

  // ---------------------------------------------------------------------------------------------
  // Constructor:
  // ---------------------------------------------------------------------------------------------

  public ForkTabFragment() {
    super(true);
  }

  // ---------------------------------------------------------------------------------------------
  // Lifecycle Events:
  // ---------------------------------------------------------------------------------------------

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    getRunningServiceIds();
  }

  // ---------------------------------------------------------------------------------------------
  // Manage list of running daemons:
  // ---------------------------------------------------------------------------------------------

  private ArrayList<String> runningServiceIds;

  private void getRunningServiceIds() {
    runningServiceIds = ProcessMgr.getRunningServiceIds(getContext());
  }

  private void addRunningServiceId(String id) {
    if (!isServiceRunning(id))
      runningServiceIds.add(id);
  }

  private void addRunningServiceId(BareJsApp listItem) {
    addRunningServiceId(listItem.getId());
  }

  private void removeRunningServiceId(String id) {
    int index;
    while ((index = runningServiceIds.indexOf(id)) >= 0) {
      runningServiceIds.remove(index);
    }
  }

  private void removeRunningServiceId(BareJsApp listItem) {
    removeRunningServiceId(listItem.getId());
  }

  private boolean isServiceRunning(String id) {
    return runningServiceIds.contains(id);
  }

  private boolean isServiceRunning(BareJsApp listItem) {
    return isServiceRunning(listItem.getId());
  }

  // ---------------------------------------------------------------------------------------------
  // Row Colors:
  // ---------------------------------------------------------------------------------------------

  @Override
  public int getColorOnRowClear(BareJsApp listItem) {
    int resId = isServiceRunning(listItem)
      ? R.color.fragmentListItemOnDaemonRunning
      : R.color.fragmentListItemOnRowClear
    ;

    return getResources().getColor(resId);
  }

  // ---------------------------------------------------------------------------------------------
  // Abstract Methods:
  // ---------------------------------------------------------------------------------------------

  protected void startService(BareJsApp listItem) {
    Intent intent = getServiceIntent(listItem);

    if (intent == null)
      return;

    getContext().startService(intent);

    addRunningServiceId(listItem);
    updateRowBackgroundColorClear(listItem);
  }

  protected void stopService(BareJsApp listItem) {
    Intent intent = getServiceIntent(listItem);

    if (intent == null)
      return;

    getContext().stopService(intent);

    removeRunningServiceId(listItem);
    updateRowBackgroundColorClear(listItem);
  }

  protected void runApplication(BareJsApp listItem) {
  }

  private Class getServiceClass(BareJsApp listItem) {
    try {
      String id        = listItem.getId();
      String className = getResources().getString(R.string.fork_service_package_prefix) + id;

      return Class.forName(className);
    }
    catch(Exception e) {
      return null;
    }
  }

  private Intent getServiceIntent(BareJsApp listItem) {
    Class serviceClass = getServiceClass(listItem);

    return (serviceClass == null)
      ? null
      : new Intent(getContext(), serviceClass);
  }
}
