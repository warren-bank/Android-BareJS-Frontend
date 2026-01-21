package com.github.warren_bank.barejs_frontend.ui.activity_main.tabs;

import com.github.warren_bank.barejs_frontend.R;
import com.github.warren_bank.barejs_frontend.data_model.BareJsApp;
import com.github.warren_bank.barejs_frontend.helpers.BareJsAppRunner;
import com.github.warren_bank.barejs_frontend.helpers.UI;
import com.github.warren_bank.barejs_frontend.services.exec.IRemoteService;
import com.github.warren_bank.barejs_frontend.services.exec.IRemoteServiceCallback;
import com.github.warren_bank.barejs_frontend.services.exec.RemoteService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class ExecTabFragment extends AbstractTabFragment {

  private Activity mActivity;
  private MenuItem mMenuItem_kill_app;

  private void showSnackbar(String message) {
    if (mActivity != null) {
      View view = mActivity.getWindow().findViewById(android.R.id.content);
      UI.getSnackbar(view, message, /* maxLines= */ 6).show();
    }
  }

  protected IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
    public void onBareComplete(String appTitle) {
      // update View components on UI thread
      if (mActivity != null) {
        mActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            handleBareComplete(appTitle);
          }
        });
      }
    }
  };

  protected void handleBareComplete(String appTitle) {
    // kill the background service, clear state, update UI
    unbindRemoteService(true);

    // show a snackbar after execution of app completes
    showExecutionCompleteNotification(appTitle);
  }

  private void showExecutionCompleteNotification(String appTitle) {
    String message = mActivity.getString(R.string.app_exec_complete, appTitle);
    showSnackbar(message);
  }

  protected IRemoteService mService;
  protected boolean        mIsBound;
  protected BareJsApp      mPendingBareJsApp;
  protected String         mPreviousBareJsAppTitle;

  private ServiceConnection mConnection = new ServiceConnection() {

    public void onServiceConnected(ComponentName className, IBinder service) {
      mService = IRemoteService.Stub.asInterface(service);
      mIsBound = true;

      try {
        if (mPendingBareJsApp == null) {
          mService.die();
        }
        else {
          mService.registerCallback(mCallback);

          String title               = mPreviousBareJsAppTitle;
          String serializedBareJsApp = BareJsApp.serialize(mPendingBareJsApp);

          mService.startBareWithArguments(title, serializedBareJsApp);
        }        
      }
      catch(RemoteException e) {
        // the service has crashed; disconnect will occur next..
      }
      catch(Exception e) {}

      updateMenuItem_kill_app();
    }

    public void onServiceDisconnected(ComponentName className) {
      mService = null;
      mIsBound = false;

      if (mPendingBareJsApp != null) {
        handleBareComplete(mPreviousBareJsAppTitle);
      }
    }

  };

  private boolean isServiceConnected() {
    return (mIsBound && (mService != null));
  }

  private void bindRemoteService(BareJsApp app) {
    if (app == null) return;

    if (mIsBound) {
      if (mActivity != null) {
        String message = mActivity.getString(R.string.notification_process_busy, app.toString(), mPreviousBareJsAppTitle);
        showSnackbar(message);
      }
      return;
    }

    mIsBound                = true;
    mPendingBareJsApp       = app;
    mPreviousBareJsAppTitle = app.toString();

    Context context = getContext();
    Intent  intent  = new Intent(context, RemoteService.class);

    context.bindService(intent, mConnection, (Context.BIND_AUTO_CREATE | Context.BIND_NOT_FOREGROUND));
  }

  private void unbindRemoteService(boolean updateUI) {
    if (mPendingBareJsApp == null) return;

    BareJsApp app = updateUI ? mPendingBareJsApp : null;

    mIsBound          = false;
    mPendingBareJsApp = null;

    Context context = getContext();

    context.unbindService(mConnection);

    if (updateUI) {
      // update toolbar menuitems
      updateMenuItem_kill_app();

      // clear row background color
      updateRowBackgroundColorClear(app);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Constructor:
  // ---------------------------------------------------------------------------------------------

  public ExecTabFragment() {
    super(false);
  }

  // ---------------------------------------------------------------------------------------------
  // Lifecycle Events:
  // ---------------------------------------------------------------------------------------------

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof Activity)
      this.mActivity = (Activity) context;
  }

  @Override
  public void onDetach() {
    super.onDetach();

    this.mActivity = null;

    unbindRemoteService(false);
  }

  // ---------------------------------------------------------------------------------------------
  // Menu:
  // ---------------------------------------------------------------------------------------------

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);

    inflater.inflate(R.menu.activity_main_tabs_fragment_exec, menu);

    mMenuItem_kill_app = menu.findItem(R.id.menu_kill_app);
    updateMenuItem_kill_app();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    switch(menuItem.getItemId()) {
      case R.id.menu_read_stdout: {
        readStandardOutput(mPreviousBareJsAppTitle, null);
        return true;
      }
      case R.id.menu_kill_app: {
        unbindRemoteService(true);
        return true;
      }
    }
    return super.onOptionsItemSelected(menuItem);
  }

  protected void updateMenuItem_kill_app() {
    if (getUserVisibleHint() && (mActivity != null) && (mMenuItem_kill_app != null)) {
      boolean isAppRunning = isServiceConnected();

      String title = isAppRunning
        ? mActivity.getString(R.string.activity_main_tabs_fragment_exec_menu_kill_app_title, mPreviousBareJsAppTitle)
        : mActivity.getString(R.string.activity_main_tabs_fragment_exec_menu_kill_app)
      ;

      mMenuItem_kill_app.setEnabled(isAppRunning);
      mMenuItem_kill_app.setTitle(title);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Row Colors:
  // ---------------------------------------------------------------------------------------------

  @Override
  public int getColorOnRowClear(BareJsApp listItem) {
    int resId = ((mPendingBareJsApp != null) && (mPendingBareJsApp == listItem))
      ? R.color.fragmentListItemOnDaemonRunning
      : R.color.fragmentListItemOnRowClear
    ;

    return getResources().getColor(resId);
  }

  // ---------------------------------------------------------------------------------------------
  // Abstract Methods:
  // ---------------------------------------------------------------------------------------------

  protected void startService(BareJsApp listItem) {
  }

  protected void stopService(BareJsApp listItem) {
  }

  protected void runApplication(BareJsApp listItem) {
    bindRemoteService(listItem);
    updateRowBackgroundColorClear(listItem);
  }
}
