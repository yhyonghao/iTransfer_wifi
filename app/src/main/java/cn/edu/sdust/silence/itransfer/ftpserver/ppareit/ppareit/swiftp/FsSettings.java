/*
Copyright 2011-2013 Pieter Pareit
Copyright 2009 David Revell

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

package cn.edu.sdust.silence.itransfer.ftpserver.ppareit.ppareit.swiftp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import cn.edu.sdust.silence.itransfer.application.ITransferApp;

public class FsSettings {

    private final static String TAG = FsSettings.class.getSimpleName();

    public static String getUserName() {
//        final SharedPreferences sp = getSharedPreferences();
//        return sp.getString("username", "ftp");
        return ITransferApp.username;
    }

    public static String getPassWord() {
//        final SharedPreferences sp = getSharedPreferences();
//        return sp.getString("password", "ftp");
        return ITransferApp.password;
    }

    public static boolean allowAnoymous() {
//        final SharedPreferences sp = getSharedPreferences();
//        return sp.getBoolean("allow_anonymous", true);
        return ITransferApp.allow_anonymous;
    }

    public static File getChrootDir() {
//        final SharedPreferences sp = getSharedPreferences();
//        String dirName = sp.getString("chrootDir", "");
//        File chrootDir = new File(dirName);
//        if (dirName.equals("")) {
//            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                chrootDir = Environment.getExternalStorageDirectory();
//            } else {
//                chrootDir = new File("/");
//            }
//        }
//        if (!chrootDir.isDirectory()) {
//            Log.e(TAG, "getChrootDir: not a directory");
//            return null;
//        }
        File chrootDir = new File(ITransferApp.chrootDir);
        return chrootDir;
    }

    public static String getChrootDirAsString() {
        File dirFile = getChrootDir();
        return dirFile != null ? dirFile.getAbsolutePath() : "";
    }

    public static boolean setChrootDir(String dir) {
//        File chrootTest = new File(dir);
//        if (!chrootTest.isDirectory() || !chrootTest.canRead())
//                return false;
//        final SharedPreferences sp = getSharedPreferences();
//        sp.edit().putString("chrootDir", dir).apply();
//        return true;
        ITransferApp.chrootDir = dir;
        return true;
    }

    public static int getPortNumber() {
//        final SharedPreferences sp = getSharedPreferences();
//        // TODO: port is always an number, so store this accordenly
//        String portString = sp.getString("portNum", "2121");
//        int port = Integer.valueOf(portString);
//        Log.v(TAG, "Using port: " + port);
//        return port;
//        return 2121;
        return ITransferApp.portNum;
    }

    public static boolean shouldTakeFullWakeLock() {
//        final SharedPreferences sp = getSharedPreferences();
//        return sp.getBoolean("stayAwake", false);
//        return true;
        return ITransferApp.shouldTakeFullWakeLock;
    }

    public static Set<String> getAutoConnectList() {
//        SharedPreferences sp = getSharedPreferences();
//        return sp.getStringSet("autoconnect_preference", new TreeSet<>());
        return null;
    }

    /**
     * @return the SharedPreferences for this application
     */
//    private static SharedPreferences getSharedPreferences() {
//        final Context context = ITransferApp.getAppContext();
//        return PreferenceManager.getDefaultSharedPreferences(context);
//    }

    // cleaning up after his
    protected static int inputBufferSize = 256;
    protected static boolean allowOverwrite = false;
    protected static int dataChunkSize = 8192; // do file I/O in 8k chunks
    protected static int sessionMonitorScrollBack = 10;
    protected static int serverLogScrollBack = 10;

    public static int getInputBufferSize() {
        return inputBufferSize;
    }

    public static void setInputBufferSize(int inputBufferSize) {
        FsSettings.inputBufferSize = inputBufferSize;
    }

    public static boolean isAllowOverwrite() {
        return allowOverwrite;
    }

    public static void setAllowOverwrite(boolean allowOverwrite) {
        FsSettings.allowOverwrite = allowOverwrite;
    }

    public static int getDataChunkSize() {
        return dataChunkSize;
    }

    public static void setDataChunkSize(int dataChunkSize) {
        FsSettings.dataChunkSize = dataChunkSize;
    }

    public static int getSessionMonitorScrollBack() {
        return sessionMonitorScrollBack;
    }

    public static void setSessionMonitorScrollBack(int sessionMonitorScrollBack) {
        FsSettings.sessionMonitorScrollBack = sessionMonitorScrollBack;
    }

    public static int getServerLogScrollBack() {
        return serverLogScrollBack;
    }

    public static void setLogScrollBack(int serverLogScrollBack) {
        FsSettings.serverLogScrollBack = serverLogScrollBack;
    }

}
