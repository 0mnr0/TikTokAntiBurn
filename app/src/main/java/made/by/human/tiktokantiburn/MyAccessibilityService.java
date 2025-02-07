package made.by.human.tiktokantiburn;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MyAccessibilityService extends AccessibilityService {





    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
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

            Intent serviceIntent = new Intent(this, FloatingWindowService.class);
            Log.w("TikTok is NOT opened: ", String.valueOf(!activePackages.contains("com.zhiliaoapp.musically")));
            if (!activePackages.contains("com.zhiliaoapp.musically")){
                serviceIntent.setAction("ACTION_CLOSE_WINDOW");
                startService(serviceIntent);
            } else {
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
