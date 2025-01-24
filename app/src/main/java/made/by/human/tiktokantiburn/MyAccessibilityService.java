package made.by.human.tiktokantiburn;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MyAccessibilityService extends AccessibilityService {
    boolean Launched = false;

    public boolean isKeyboardActive() {
        List<AccessibilityWindowInfo> windows = getWindows();
        if (windows == null) return false;

        for (AccessibilityWindowInfo window : windows) {
            if (window.getType() == AccessibilityWindowInfo.TYPE_INPUT_METHOD) {
                return true; // Если окно типа INPUT_METHOD активно, значит клавиатура открыта
            }
        }
        return false;
    }


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
            Toast.makeText(this, "isKeyboardActive: "+isKeyboardActive(), Toast.LENGTH_SHORT).show();
            if (!activePackages.contains("com.zhiliaoapp.musically") || isKeyboardActive()){
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
