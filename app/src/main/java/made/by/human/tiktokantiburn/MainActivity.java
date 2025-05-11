package made.by.human.tiktokantiburn;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Slider seekBar;
    private TextView progressText;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SeekBarPrefs";
    private static final String PREF_VALUE = "seekBarValue";



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
        try {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(intent, 1234);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to request Overlay permission. Request it manually", Toast.LENGTH_SHORT).show();
        }
    }

    public void openRequestTopWindow(View view){
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to request Overlay permission. Request it manually", Toast.LENGTH_SHORT).show();
        }
    }

    public void OpenGithub(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/0mnr0/TikTokAntiBurn"));
        startActivity(browserIntent);
    }

    public void UseDataRequest(View view){
        try {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Request permission of USAGE ACCESS manually, please", Toast.LENGTH_SHORT).show();
        }
    }

    public void OpenExtendedSetting(View view){
        try{
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "com.zhiliaoapp.musically",
                    "com.ss.android.ugc.aweme.splash.SplashActivity"
            ));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Приложение TikTok не найдено", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(this, SetupFloatingWindows.class);
        startService(intent);
        Log.d("MyAccessibilityService", "Accessibility Service started");




    }

    public void openSpecificAccessibilityServiceSettings(View view) {
        try {
            Intent openSettings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            openSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(openSettings, 1000);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Cant reach accessibility setting. Request permission manually", Toast.LENGTH_SHORT).show();
        }
    }
    public String CalculateTrackInfo() {
        float sliderValue = seekBar.getValue();
        return String.valueOf(sliderValue);
    }

    @SuppressLint("SetTextI18n")
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

        seekBar = findViewById(R.id.slider);
        progressText = findViewById(R.id.textView);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int max = (int) (screenHeight * 0.1);
        seekBar.setValueTo(max);
        seekBar.setValueFrom(40);
        int savedValue = sharedPreferences.getInt(PREF_VALUE, 40);
        seekBar.setValue(savedValue);
        progressText.setText("Popup height: " + savedValue + " px");

        seekBar.addOnChangeListener((slider, progress, fromUser) -> {
            int value = (int) Math.max(progress, 40);
            progressText.setText("Popup height: " + value + " px");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_VALUE, value);
            editor.apply();
        });

        seekBar.setLabelFormatter(label -> ((int) Math.max(seekBar.getValue(), 40)) + " px");


    }

}