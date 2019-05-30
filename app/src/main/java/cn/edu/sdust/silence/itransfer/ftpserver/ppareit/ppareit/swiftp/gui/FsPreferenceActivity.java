/*******************************************************************************
 * Copyright (c) 2012-2013 Pieter Pareit.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Contributors:
 * Pieter Pareit - initial API and implementation
 ******************************************************************************/

package cn.edu.sdust.silence.itransfer.ftpserver.ppareit.ppareit.swiftp.gui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.net.InetAddress;
import java.util.List;

import cn.edu.sdust.silence.itransfer.R;
import cn.edu.sdust.silence.itransfer.ftpserver.ppareit.ppareit.android.DynamicMultiSelectListPreference;
import cn.edu.sdust.silence.itransfer.application.ITransferApp;
import cn.edu.sdust.silence.itransfer.ftpserver.ppareit.ppareit.swiftp.FsService;
import cn.edu.sdust.silence.itransfer.ftpserver.ppareit.ppareit.swiftp.FsSettings;


/**
 * This is the main activity for swiftp, it enables the user to start the server service
 * and allows the users to change the settings.
 */
public class FsPreferenceActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private static String TAG = FsPreferenceActivity.class.getSimpleName();

    private EditTextPreference mPassWordPref;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "created");
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Resources resources = getResources();

        TwoStatePreference runningPref = findPref("running_switch");
        updateRunningState();
        runningPref.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                startServer();
            } else {
                stopServer();
            }
            return true;
        });

        PreferenceScreen prefScreen = findPref("preference_screen");
        Preference marketVersionPref = findPref("market_version");
        marketVersionPref.setOnPreferenceClickListener(preference -> {
            // start the market at our application
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=be.ppareit.swiftp"));
            try {
                // this can fail if there is no market installed
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch the market.");
                e.printStackTrace();
            }
            return false;
        });
        if (ITransferApp.isFreeVersion() == false) {
            prefScreen.removePreference(marketVersionPref);
        }

        updateLoginInfo();

        EditTextPreference usernamePref = findPref("username");
        usernamePref.setOnPreferenceChangeListener((preference, newValue) -> {
            String newUsername = (String) newValue;
            if (preference.getSummary().equals(newUsername))
                return false;
            if (!newUsername.matches("[a-zA-Z0-9]+")) {
                Toast.makeText(FsPreferenceActivity.this,
                        R.string.username_validation_error, Toast.LENGTH_LONG).show();
                return false;
            }
            stopServer();
            return true;
        });

        mPassWordPref = findPref("password");
        mPassWordPref.setOnPreferenceChangeListener((preference, newValue) -> {
            stopServer();
            return true;
        });

        DynamicMultiSelectListPreference autoconnectListPref = findPref("autoconnect_preference");
        autoconnectListPref.setOnPopulateListener(
                pref -> {
//                    Cat.d("autoconnect populate listener");

                    WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
                    List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
                    if (configs == null) {
//                        Cat.e("Unable to receive wifi configurations, bark at user and bail");
                        Toast.makeText(this,
                                R.string.autoconnect_error_enable_wifi_for_access_points,
                                Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    CharSequence[] ssids = new CharSequence[configs.size()];
                    CharSequence[] niceSsids = new CharSequence[configs.size()];
                    for (int i = 0; i < configs.size(); ++i) {
                        ssids[i] = configs.get(i).SSID;
                        String ssid = configs.get(i).SSID;
                        if (ssid.length() > 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                            ssid = ssid.substring(1, ssid.length() - 1);
                        }
                        niceSsids[i] = ssid;
                    }
                    pref.setEntries(niceSsids);
                    pref.setEntryValues(ssids);
                });

        EditTextPreference portnum_pref = findPref("portNum");
        portnum_pref.setSummary(sp.getString("portNum",
                resources.getString(R.string.portnumber_default)));
        portnum_pref.setOnPreferenceChangeListener((preference, newValue) -> {
            String newPortnumString = (String) newValue;
            if (preference.getSummary().equals(newPortnumString))
                return false;
            int portnum = 0;
            try {
                portnum = Integer.parseInt(newPortnumString);
            } catch (Exception e) {
            }
            if (portnum <= 0 || 65535 < portnum) {
                Toast.makeText(FsPreferenceActivity.this,
                        R.string.port_validation_error, Toast.LENGTH_LONG).show();
                return false;
            }
            preference.setSummary(newPortnumString);
            stopServer();
            return true;
        });

        Preference chroot_pref = findPref("chrootDir");
        chroot_pref.setSummary(FsSettings.getChrootDirAsString());
        chroot_pref.setOnPreferenceClickListener(preference -> {
            AlertDialog folderPicker = new FolderPickerDialogBuilder(this, FsSettings.getChrootDir())
                    .setSelectedButton(R.string.select, path -> {
                        if (preference.getSummary().equals(path))
                            return;
                        if (!FsSettings.setChrootDir(path))
                            return;
                        // TODO: this is a hotfix, create correct resources, improve UI/UX
                        final File root = new File(path);
                        if (!root.canRead()) {
                            Toast.makeText(this,
                                    "Notice that we can't read/write in this folder.",
                                    Toast.LENGTH_LONG).show();
                        } else if (!root.canWrite()) {
                            Toast.makeText(this,
                                    "Notice that we can't write in this folder, reading will work. Writing in subfolders might work.",
                                    Toast.LENGTH_LONG).show();
                        }

                        preference.setSummary(path);
                        stopServer();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create();
            folderPicker.show();
            return true;
        });

        final CheckBoxPreference wakelock_pref = findPref("stayAwake");
        wakelock_pref.setOnPreferenceChangeListener((preference, newValue) -> {
            stopServer();
            return true;
        });

        Preference help = findPref("help");
        help.setOnPreferenceClickListener(preference -> {
            Log.v(TAG, "On preference help clicked");
            Context context = FsPreferenceActivity.this;
            AlertDialog ad = new AlertDialog.Builder(context)
                    .setTitle(R.string.help_dlg_title)
                    .setMessage(R.string.help_dlg_message)
                    .setPositiveButton(R.string.ok, null)
                    .create();
            ad.show();
            Linkify.addLinks((TextView) ad.findViewById(android.R.id.message),
                    Linkify.ALL);
            return true;
        });

        Preference about = findPref("about");
        about.setOnPreferenceClickListener(preference -> {
            AlertDialog ad = new AlertDialog.Builder(FsPreferenceActivity.this)
                    .setTitle(R.string.about_dlg_title)
                    .setMessage(R.string.about_dlg_message)
                    .setPositiveButton(getText(R.string.ok), null)
                    .create();
            ad.show();
            Linkify.addLinks((TextView) ad.findViewById(android.R.id.message),
                    Linkify.ALL);
            return true;
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_feedback) {
            String to = "pieter.pareit@gmail.com";
            String subject = "FTP Server feedback";
            String message = "Device: " + Build.MODEL + "\nAndroid version: "
                    + Build.VERSION.RELEASE + "\nFeedback: \n";

            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
            email.putExtra(Intent.EXTRA_SUBJECT, subject);
            email.putExtra(Intent.EXTRA_TEXT, message);
            email.setType("message/rfc822");

            startActivity(email);
        } else if (item.getItemId() == R.id.action_about) {
            AlertDialog ad = new AlertDialog.Builder(this)
                    .setTitle(R.string.about_dlg_title)
                    .setMessage(R.string.about_dlg_message)
                    .setPositiveButton(getText(android.R.string.ok), null)
                    .create();
            ad.show();
            Linkify.addLinks((TextView) ad.findViewById(android.R.id.message), Linkify.ALL);
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateRunningState();

        Log.d(TAG, "onResume: Register the preference change listner");
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        sp.registerOnSharedPreferenceChangeListener(this);

        Log.d(TAG, "onResume: Registering the FTP server actions");
        IntentFilter filter = new IntentFilter();
        filter.addAction(FsService.ACTION_STARTED);
        filter.addAction(FsService.ACTION_STOPPED);
        filter.addAction(FsService.ACTION_FAILEDTOSTART);
        registerReceiver(mFsActionsReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v(TAG, "onPause: Unregistering the FTPServer actions");
        unregisterReceiver(mFsActionsReceiver);

        Log.d(TAG, "onPause: Unregistering the preference change listner");
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        updateLoginInfo();
    }

    private void startServer() {
        sendBroadcast(new Intent(FsService.ACTION_START_FTPSERVER));
    }

    private void stopServer() {
        sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
    }

    private void updateLoginInfo() {

        String username = FsSettings.getUserName();
        String password = FsSettings.getPassWord();

        Log.v(TAG, "Updating login summary");
        PreferenceScreen loginPreference = findPref("login");
        loginPreference.setSummary(username + " : " + transformPassword(password));
        ((BaseAdapter) loginPreference.getRootAdapter()).notifyDataSetChanged();

        EditTextPreference usernamePref = findPref("username");
        usernamePref.setSummary(username);

        EditTextPreference passWordPref = findPref("password");
        passWordPref.setSummary(transformPassword(password));
    }

    private void updateRunningState() {
        Resources res = getResources();
        TwoStatePreference runningPref = findPref("running_switch");
        if (FsService.isRunning()) {
            runningPref.setChecked(true);
            // Fill in the FTP server address
            InetAddress address = FsService.getLocalInetAddress();
            if (address == null) {
                Log.v(TAG, "Unable to retrieve wifi ip address");
                runningPref.setSummary(R.string.cant_get_url);
                return;
            }
            String iptext = "ftp://" + address.getHostAddress() + ":"
                    + FsSettings.getPortNumber() + "/";
            String summary = res.getString(R.string.running_summary_started, iptext);
            runningPref.setSummary(summary);
        } else {
            runningPref.setChecked(false);
            runningPref.setSummary(R.string.running_summary_stopped);
        }
    }

    /**
     * This receiver will check FTPServer.ACTION* messages and will update the button,
     * running_state, if the server is running and will also display at what url the
     * server is running.
     */
    BroadcastReceiver mFsActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "action received: " + intent.getAction());
            // remove all pending callbacks
            mHandler.removeCallbacksAndMessages(null);
            // action will be ACTION_STARTED or ACTION_STOPPED
            updateRunningState();
            // or it might be ACTION_FAILEDTOSTART
            final TwoStatePreference runningPref = findPref("running_switch");
            if (intent.getAction().equals(FsService.ACTION_FAILEDTOSTART)) {
                runningPref.setChecked(false);
                mHandler.postDelayed(
                        () -> runningPref.setSummary(R.string.running_summary_failed),
                        100);
                mHandler.postDelayed(
                        () -> runningPref.setSummary(R.string.running_summary_stopped),
                        5000);
            }
        }
    };

    static private String transformPassword(String password) {
        Context context = ITransferApp.getAppContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Resources res = context.getResources();
        String showPasswordString = res.getString(R.string.show_password_default);
        boolean showPassword = showPasswordString.equals("true");
        showPassword = sp.getBoolean("show_password", showPassword);
        if (showPassword)
            return password;
        else {
            StringBuilder sb = new StringBuilder(password.length());
            for (int i = 0; i < password.length(); ++i)
                sb.append('*');
            return sb.toString();
        }
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    protected <T extends Preference> T findPref(CharSequence key) {
        return (T) this.findPreference(key);
    }

}
