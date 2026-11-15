package made.by.human.tiktokantiburn;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import made.by.human.tiktokantiburn.settings.Settings;



@SuppressLint("AccessibilityPolicy")
public class MainAccessibilityService extends android.accessibilityservice.AccessibilityService {
    private static MainAccessibilityService instance;

    public static MainAccessibilityService getInstance() {
        return instance;
    }

    LogSystem logger;
    boolean CompatibilityMode = false;


    public boolean GetBoolean(String settingName, boolean defValue) {
        return Settings.Service.getBool(this, settingName, defValue);
    }

    public String GetString(String settingName, String defValue) {
        return Settings.Service.getString(this, settingName, defValue);
    }


    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable lockRunnable = this::lockThePhone;


    private void removeLockTimer() {handler.removeCallbacks(lockRunnable);}
    private void resetTimer(AccessibilityEvent event, boolean TikTokOpened) {
        int eventType = event.getEventType();
        if ((
                eventType != AccessibilityEvent.TYPE_VIEW_CLICKED &&
                eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
            ) || Build.VERSION.SDK_INT < Build.VERSION_CODES.P
        ) {return;}


        removeLockTimer();
        if (!TikTokOpened) {return;}

        if (GetBoolean("LockAfterAFKTime", false)) {
            int waitMinutes = Settings.Service.getInt(this, "LockTimer", 8);
            handler.postDelayed(lockRunnable, waitMinutes * (1000 * 60));
        }
    }

    private void lockThePhone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final boolean isClicked = event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED;
        final boolean isWindowsChanged = event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED;
        if (!isWindowsChanged && !isClicked) { return; }

        Intent serviceIntent = new Intent(this, FloatingWindowService.class);
        logger.Save("[MyAccessibilityService] - onAccessibilityEvent received", "Is WindowsChanged: "+isWindowsChanged, true, false);
        boolean ClosePopups;
        CompatibilityMode = GetBoolean("Compatibility_MODE", false);


        try {
            List<AccessibilityWindowInfo> windows = getWindows();

            activePackages.clear();
            for (AccessibilityWindowInfo window : windows) {
                if (window.getRoot() != null) {
                    if (window.getRoot().getPackageName() != null) {
                        String packageName = window.getRoot().getPackageName().toString();
                        activePackages.add(packageName);
                    }
                }
            }

            boolean TikTokOpened = activePackages.contains(GetString("TriggerPacketName", "com.zhiliaoapp.musically"));
            resetTimer(event, TikTokOpened);
            if (isClicked) return;

            if (GetBoolean("HideWhenKeyboardIsOpen", false) && hasKeyboardPackage()) {
                if (activePackages.stream().anyMatch(p ->
                        p.startsWith("com.google.android.inputmethod") ||
                                p.startsWith("com.simejikeyboard") ||
                                p.startsWith("ru.yandex.androidkeyboard") ||
                                p.startsWith("com.touchtype.swiftkey") ||
                                p.startsWith("com.gamelounge.chroomakeyboard") ||
                                p.startsWith("com.facemoji.lite")))
                {
                    TikTokOpened = false;
                }
            }

            if (activePackages.contains("com.android.launcher")
                    || activePackages.contains("com.google.android.apps.nexuslauncher")
                    || activePackages.contains("com.miui.home") // MIUI
                    || activePackages.contains("com.huawei.android.launcher") // Huawei
            ) {
                ClosePopups = true;
            } else {
                ClosePopups = !TikTokOpened;
            }

            if (CompatibilityMode) {
                ClosePopups = !TikTokOpened;
                logger.Save("CompatibilityMode", "ClosePopup now = "+ClosePopups, false, false);
            }

            if (Settings.Iternal.getBool(this, "viewsSetup", false)) {
                ClosePopups = true;
                logger.Save("viewsSetup", "viewsSetup -> true ", false, false);
            }

            logger.Save("TikTok Opened", TikTokOpened, false, false);

            if (!android.provider.Settings.canDrawOverlays(this)) {
                logger.Save("OverlayError", "Overlay permission is not granted", false, false);
                Toast.makeText(this, getString(R.string.ServiceOverlayPermissionNotGranted), Toast.LENGTH_LONG).show();
                serviceIntent.setAction("ACTION_CLOSE_WINDOW");
                startService(serviceIntent);
                disableSelf();
                return;
            }


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

    private boolean hasKeyboardPackage() {
        for (String p : activePackages) {
            if (p.startsWith("com.google.android.inputmethod")
                    || p.startsWith("com.simejikeyboard")
                    || p.startsWith("ru.yandex.androidkeyboard")
                    || p.startsWith("com.touchtype.swiftkey")
                    || p.startsWith("com.gamelounge.chroomakeyboard")
                    || p.startsWith("com.facemoji.lite")) {
                return true;
            }
        }
        return false;
    }


    private final Set<String> activePackages = new HashSet<>();
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d("MyAccessibilityService", "Accessibility Service connected");
        if (LogSystem.getInstanceOrNull() == null) {
            LogSystem.init((Application) getApplicationContext());
        }
        logger = LogSystem.getInstance();
        logger.Save("MyAccessibilityService", "Service connected", true, true);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        instance = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Override
    public void onInterrupt() {
        Log.d("MyAccessibilityService", "Accessibility Service interrupted");
        logger.Save("MyAccessibilityService", "Service interrupted", true, true);
    }
}
