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
    boolean Launched = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            List<AccessibilityWindowInfo> windows = getWindows();

            Set<String> activePackages = new HashSet<>();
            for (AccessibilityWindowInfo window : windows) {
                if (window.getRoot() != null && window.getRoot().getPackageName() != null) {
                    String packageName = window.getRoot().getPackageName().toString();
                    activePackages.add(packageName);
                }
            }




            Intent serviceIntent = new Intent(this, FloatingWindowService.class);
            if (!activePackages.contains("com.zhiliaoapp.musically")){
                serviceIntent.setAction("ACTION_CLOSE_WINDOW");
                startService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            try{
                Log.wtf("Services list: ", String.valueOf(activePackages.contains("com.zhiliaoapp.musically")));
                Log.wtf("Service start with launching: ", String.valueOf(Launched));
            } catch (Exception e) {
                Log.w("Service Start exception", e);
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
