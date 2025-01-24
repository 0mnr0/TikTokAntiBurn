package made.by.human.tiktokantiburn;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
        ComponentName expectedComponentName = new ComponentName(context, accessibilityService);

        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),  Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null)
            return false;

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(expectedComponentName))
                return true;
        }

        return false;
    }

    private void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, 1234);
        }
    }

    public void CreateFloatingWindow(View view){
        if (Settings.canDrawOverlays(this)) {
            Intent serviceIntent = new Intent(this, FloatingWindowService.class);
            startService(serviceIntent);
        } else {
            Log.d("FloatingWindowService", "Overlay permission not granted");
        }


    }

    public void openRequestTopWindow(View view){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    public void UseDataRequest(View view){
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    public void openSpecificAccessibilityServiceSettings(View view) {
        try {
            Intent openSettings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            openSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(openSettings, 1000);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Невозможно открыть настройки доступности", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkOverlayPermission();

        boolean isServiceEnabled = isAccessibilityServiceEnabled(this, MyAccessibilityService.class);

        Log.d("isServiceEnabled:", String.valueOf(isServiceEnabled));
        if (!isServiceEnabled) {
            openSpecificAccessibilityServiceSettings(null);
        }
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());

        if (mode != AppOpsManager.MODE_ALLOWED) {
            UseDataRequest(null);
        }


        Intent intent = new Intent(MainActivity.this, FloatingWindowService.class);
        startService(intent);

    }

}