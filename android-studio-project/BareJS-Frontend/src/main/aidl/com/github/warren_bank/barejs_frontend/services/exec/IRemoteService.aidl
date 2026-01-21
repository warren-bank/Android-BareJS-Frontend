package com.github.warren_bank.barejs_frontend.services.exec;

import com.github.warren_bank.barejs_frontend.services.exec.IRemoteServiceCallback;

oneway interface IRemoteService {
  void registerCallback(in IRemoteServiceCallback cb);

//void unregisterCallback(in IRemoteServiceCallback cb);

  void startBareWithArguments(in String title, in String serializedBareJsApp);

  void die();
}
