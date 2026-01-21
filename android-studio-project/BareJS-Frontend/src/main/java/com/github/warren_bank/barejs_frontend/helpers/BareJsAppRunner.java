package com.github.warren_bank.barejs_frontend.helpers;

import com.github.warren_bank.barejs_frontend.data_model.BareJsApp;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public final class BareJsAppRunner {
  private static boolean isReady = false;

  private static String getBareExecutablePath(Context context) {
    return context.getApplicationInfo().nativeLibraryDir + File.separator + "libbare.so";
  }

  private static void initBareExecutable(Context context) throws Exception {
    if (isReady) return;

    File bareExecutable = new File(
      getBareExecutablePath(context)
    );

    if (!bareExecutable.exists())
      throw new Exception("bare binary executable file not found:" + "\n  " + bareExecutable.getAbsolutePath());

    bareExecutable.setExecutable(true);
    isReady = true;
  }

  private static String[] getBareArguments(Context context, BareJsApp app) {
    String[] bare_options = app.getBareOptions();
    String   js_filepath  = app.getJsApplicationFilepath();
    String[] js_options   = app.getJsApplicationOptions();

    ArrayList<String> arguments = new ArrayList<String>();

    arguments.add(
      getBareExecutablePath(context)
    );

    if (bare_options != null) {
      Collections.addAll(arguments, bare_options);
    }

    if ((js_filepath != null) && !js_filepath.isEmpty()) {
      arguments.add(js_filepath);

      if (js_options != null) {
        Collections.addAll(arguments, js_options);
      }
    }

    return (arguments.size() > 1)
      ? arguments.toArray(new String[ arguments.size() ])
      : null;
  }

  public static File getStandardOutputFile(Context context, String id, boolean mustExist) {
    if (id == null)
      id = "exec";

    File dir = context.getExternalFilesDir(null);

    if (dir == null)
      dir = context.getFilesDir();

    if (!dir.exists())
      dir.mkdir();

    File file = new File(dir, "stdout_" + id + ".txt");

    return (mustExist && !file.exists())
      ? null
      : file;
  }

  private static void exec(String[] arguments, String[][] env_vars, String cwd_dirpath, File stdout_filepath) throws Exception {
    if (arguments == null)
      return;

    ProcessBuilder pb = new ProcessBuilder(arguments);

    if (env_vars != null) {
      Map<String, String> env = pb.environment();

      String[] parts;
      for (int i=0; i < env_vars.length; i++) {
        parts = env_vars[i];
        env.put(parts[0], parts[1]);
      }
    }

    if ((cwd_dirpath != null) && !cwd_dirpath.isEmpty()) {
      pb.directory(
        new File(cwd_dirpath)
      );
    }

    if (stdout_filepath != null) {
      pb.redirectError(stdout_filepath);
      pb.redirectOutput(stdout_filepath);
    }

    Process process = pb.start();
    process.waitFor();
  }

  public static void exec(Context context, String id, BareJsApp app) throws Exception {
    String[]   arguments       = getBareArguments(context, app);
    String[][] env_vars        = app.getEnvironmentVariables();
    String     cwd_dirpath     = app.getCurrentWorkingDirectory();
    File       stdout_filepath = getStandardOutputFile(context, id, false);

    initBareExecutable(context);
    exec(arguments, env_vars, cwd_dirpath, stdout_filepath);
  }
}
