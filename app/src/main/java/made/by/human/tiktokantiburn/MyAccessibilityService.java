package made.by.human.tiktokantiburn;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MyAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        boolean ClosePopups;
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
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
                if (activePackages.contains("com.android.launcher")
                        || activePackages.contains("com.google.android.apps.nexuslauncher")
                        || activePackages.contains("com.miui.home") // MIUI
                        || activePackages.contains("com.huawei.android.launcher") // Huawei
                        || activePackages.contains("com.samsung.android.launcher")) {

                    ClosePopups = true;
                } else {
                    ClosePopups = !activePackages.contains("com.zhiliaoapp.musically");
                }

                if (ClosePopups) {
                    serviceIntent.setAction("ACTION_CLOSE_WINDOW");
                    startService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            } catch (Exception e) {
                Log.w("Exception caught:", e);
                serviceIntent.setAction("ACTION_CLOSE_WINDOW");
                startService(serviceIntent);
            }

        }

    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d("MyAccessibilityService", "Accessibility Service connected");
    }

    @Override
    public void onInterrupt() {
        Log.d("MyAccessibilityService", "Accessibility Service interrupted");
    }
}
