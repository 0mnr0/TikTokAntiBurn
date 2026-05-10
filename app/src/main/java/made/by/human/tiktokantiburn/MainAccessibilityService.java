package made.by.human.tiktokantiburn;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import made.by.human.tiktokantiburn.settings.Settings;



@SuppressLint("AccessibilityPolicy")
public class MainAccessibilityService extends android.accessibilityservice.AccessibilityService {
    LogSystem logger;
    boolean CompatibilityMode = false;



    public boolean GetBoolean(String settingName, boolean defValue) {
        return Settings.Service.getBool(this, settingName, defValue);
    }

    public String GetString(String settingName, String defValue) {
        return Settings.Service.getString(this, settingName, defValue);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final boolean isWindowsChanged = event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED;

        Log.d("accessibilityService", "isWChaned:" + isWindowsChanged);
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

                boolean TikTokOpened = activePackages.contains(GetString("TriggerPacketName", "com.zhiliaoapp.musically"));

                if (GetBoolean("InputMethodSkip", false)) {
                    final String PCKGS = activePackages.toString();
                    if (PCKGS.contains("com.google.android.inputmethod")
                            || PCKGS.contains("com.simejikeyboard")
                            || PCKGS.contains("ru.yandex.androidkeyboard")
                            || PCKGS.contains("com.touchtype.swiftkey")
                    ) {
                        TikTokOpened = false;
                    }
                }

                //Log.d("TikTokOpened: ", activePackages.toString());
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
