package made.by.human.tiktokantiburn;

import android.accessibilityservice.AccessibilityService;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MyAccessibilityService extends AccessibilityService {
    LogSystem logger;
    boolean CompatibilityMode = false;
    // в любой точке приложения




    public String GetApplicationName(String packageName) {
        try {
            PackageManager packageManager = this.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "?";

    }

    public boolean GetBoolean(String settingName, boolean defaultValue) {
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getBoolean(settingName, defaultValue);
    }

    public String GetString(String settingName, String defValue) {
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getString(settingName, defValue);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final boolean isWindowsChanged = event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED;
        logger.Save("[MyAccessibilityService] - onAccessibilityEvent received", "Is WindowsChanged: "+isWindowsChanged, true, false);
        boolean ClosePopups;
        CompatibilityMode = GetBoolean("CompatibilityMode", false);
        if (isWindowsChanged) {
            Intent serviceIntent = new Intent(this, FloatingWindowService.class);
            try {
                List<AccessibilityWindowInfo> windows = getWindows();

                Set<String> activePackages = new HashSet<>();
                for (AccessibilityWindowInfo window : windows) {
                    if (window.getRoot() != null) {
                        if (window.getRoot().getPackageName() != null) {
                            String packageName = window.getRoot().getPackageName().toString();
                            activePackages.add(packageName);
                        }
                    }
                }

                logger.Save("MyAccessibilityService - [Active Packages]", "ActivePackages: "+activePackages + " (CompatibilityMode: "+CompatibilityMode+")", false, false);
                boolean TikTokOpened = activePackages.contains(GetString("TriggerPacketName", "com.zhiliaoapp.musically"));

                Log.d("TikTokOpened: ", activePackages.toString());
                if (activePackages.contains("com.android.launcher")
                        || activePackages.contains("com.google.android.apps.nexuslauncher")
                        || activePackages.contains("com.miui.home") // MIUI
                        || activePackages.contains("com.huawei.android.launcher") // Huawei
                        || activePackages.contains("com.samsung.android.launcher")) {

                    ClosePopups = true;
                } else {
                    ClosePopups = !TikTokOpened;
                }

                if (CompatibilityMode) {
                    ClosePopups = !TikTokOpened;
                    logger.Save("CompatibilityMode", "ClosePopup now = "+ClosePopups, false, false);
                }

                logger.Save("TikTok Opened", TikTokOpened, false, false);

                if (ClosePopups) {
                    serviceIntent.setAction("ACTION_CLOSE_WINDOW");
                    startService(serviceIntent);
                    logger.Save("ClosePopups Result", "true, Closing Popup Window!", false, false);
                } else {
                    startService(serviceIntent);
                    logger.Save("ClosePopups Result", "false, Opening Popup Window!", false, false);
                }
            } catch (Exception e) {
                Log.w("Exception caught:", e);
                logger.Save(" | MyAccessibilityService Error | ", "Exception caught: " + e, true, true);
                serviceIntent.setAction("ACTION_CLOSE_WINDOW");
                startService(serviceIntent);
            }

        }

    }



    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d("MyAccessibilityService", "Accessibility Service connected");
        if (LogSystem.getInstanceOrNull() == null) {
            LogSystem.init((Application) getApplicationContext());
        }
        logger = LogSystem.getInstance();
        logger.Save("MyAccessibilityService", "Service connected", true, true);
    }

    @Override
    public void onInterrupt() {
        Log.d("MyAccessibilityService", "Accessibility Service interrupted");
        logger.Save("MyAccessibilityService", "Service interrupted", true, true);
    }
}
