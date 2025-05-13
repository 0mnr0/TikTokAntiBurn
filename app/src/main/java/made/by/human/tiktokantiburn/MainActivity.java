package made.by.human.tiktokantiburn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;


public class MainActivity extends AppCompatActivity {

    private Slider seekBar;
    private TextView progressText;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SeekBarPrefs";
    private static final String PREF_VALUE = "seekBarValue";

    public ConstraintLayout SomeSetting;

    MaterialSwitch TheSwitch;




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
            Toast.makeText(this, getString(R.string.NoOverlayPermission), Toast.LENGTH_SHORT).show();
        }
    }

    public void openRequestTopWindow(View view){
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.NoOverlayPermission), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, getString(R.string.UsageStatsPermessionIsNotGranted), Toast.LENGTH_SHORT).show();
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
            //startActivity(intent);
        } catch (Exception e) {
            //Toast.makeText(this, getString(R.string.TikTokNotFound), Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(this, SetupFloatingWindows.class);
        startService(intent);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent serviceIntent = new Intent(this, FloatingWindowService.class);
            serviceIntent.setAction("ACTION_CLOSE_WINDOW");
            startService(serviceIntent);
        }, 500);
    }

    public void openSpecificAccessibilityServiceSettings(View view) {
        try {
            Intent openSettings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            openSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(openSettings, 1000);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.AcessabilitySettingNotReacheable), Toast.LENGTH_SHORT).show();
        }
    }


    public boolean GetClickableStatus(){
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getBoolean("Clickable", false);
    }

    public void SetClickableStatus(boolean status){
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("Clickable", status);
        editor.apply();
    }



    public void CleanLogs(View view) {
        if (LogSystem.getInstanceOrNull() == null) {
            LogSystem.init((Application) getApplicationContext());
        }
        LogSystem logger = LogSystem.getInstance();
        logger.clear();
        Toast.makeText(this, "Cleared!", Toast.LENGTH_SHORT).show();
    }
    public void ExportLogs(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            LogExportHelper.exportLogs(this);
        } else {
            // Android 8.0 — 9.0 (API 26 - 28)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        123);
            } else {
                LogExportHelper.exportLogs(this);
            }
        }



    }

    public void SaveSettings(String settingName, Object value) {
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (value instanceof String) {
            editor.putString(settingName, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(settingName, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(settingName, (Boolean) value);
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
        }
        editor.apply();
    }

    public boolean GetBoolean(String settingName) {
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getBoolean(settingName, false);
    }

    public int GetInt(String settingName) {
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getInt(settingName, 0);
    }

    public String GetString(String settingName) {
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getString(settingName, "");
    }

    public boolean isSettingKeyExists(String CollectionName, String settingName) {
        SharedPreferences prefs = getSharedPreferences(CollectionName, MODE_PRIVATE);
        return prefs.contains(settingName);
    }
    public void CheckSomeSettings(){
        if (GetBoolean("DisableMainFloatingWindow")) { SomeSetting.setVisibility(View.GONE); } else { SomeSetting.setVisibility(View.VISIBLE); }
    }



    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
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
        SomeSetting = findViewById(R.id.SomeSetting);
        TextView VersionCode = findViewById(R.id.VersionCode);
        VersionCode.setText(LogSystem.LoggerVersion);
        TheSwitch = findViewById(R.id.TheSwitchingTool);
        TheSwitch.setChecked(GetClickableStatus());
        TheSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> SetClickableStatus(isChecked));


        boolean isServiceEnabled = isAccessibilityServiceEnabled(this, MyAccessibilityService.class);

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
        MaterialSwitch MainFloatingWindowEnabled = findViewById(R.id.MinifiedVersion);

        if (mode != AppOpsManager.MODE_ALLOWED) {
            UseDataRequest(null);
        }

        MainFloatingWindowEnabled.setChecked(GetBoolean("DisableMainFloatingWindow")); CheckSomeSettings();
        MainFloatingWindowEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {SaveSettings("DisableMainFloatingWindow", isChecked); CheckSomeSettings();});



        seekBar = findViewById(R.id.slider);
        progressText = findViewById(R.id.textView);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int max = (int) (screenHeight * 0.1);
        seekBar.setValueTo(max);
        seekBar.setValueFrom(40);
        int savedValue = sharedPreferences.getInt(PREF_VALUE, 40);
        if (!isSettingKeyExists(PREFS_NAME, PREF_VALUE)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            savedValue = (max+40)/2;
            editor.putInt(PREF_VALUE, savedValue);
            editor.apply();
        }
        seekBar.setValue(savedValue);
        progressText.setText(getString(R.string.fastSettingsMainFlowtingWindow) + savedValue + " px");

        seekBar.addOnChangeListener((slider, progress, fromUser) -> {
            int value = (int) Math.max(progress, 40);
            progressText.setText(getString(R.string.fastSettingsMainFlowtingWindow) + value + " px");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_VALUE, value);
            editor.apply();
        });

        seekBar.setLabelFormatter(label -> ((int) Math.max(seekBar.getValue(), 40)) + " px");
        refreshPermissionStatuses();

    }


    public void refreshPermissionStatuses() {
        Button AboveAllWindows, UsagePermission, SpecialAbilities;
        AboveAllWindows = findViewById(R.id.AboveAllWindows);
        UsagePermission = findViewById(R.id.UsagePermission);
        SpecialAbilities = findViewById(R.id.SpecialAbilities);
        Drawable done = ContextCompat.getDrawable(this, R.drawable.check_circle);
        Drawable none = ContextCompat.getDrawable(this, R.drawable.x_circle);
        Drawable unknown = ContextCompat.getDrawable(this, R.drawable.patch_question);

        try {
            AboveAllWindows.setCompoundDrawablesWithIntrinsicBounds(Settings.canDrawOverlays(this) ? done : none, null, null, null);
            new Handler(Looper.getMainLooper()).postDelayed(() -> AboveAllWindows.setCompoundDrawablesWithIntrinsicBounds(Settings.canDrawOverlays(this) ? done : none, null, null, null), 500);
        } catch (Exception ignored) {
            AboveAllWindows.setCompoundDrawablesWithIntrinsicBounds(unknown, null, null, null);
        }

        try {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
            boolean UsagePermissionAllowed = mode == AppOpsManager.MODE_ALLOWED;
            UsagePermission.setCompoundDrawablesWithIntrinsicBounds(UsagePermissionAllowed ? done : none, null, null, null);
        } catch (Exception ignored) {
            UsagePermission.setCompoundDrawablesWithIntrinsicBounds(unknown, null, null, null);
        }

        try {
            boolean isServiceEnabled = isAccessibilityServiceEnabled(this, MyAccessibilityService.class);
            SpecialAbilities.setCompoundDrawablesWithIntrinsicBounds(isServiceEnabled ? done : none, null, null, null);
        } catch (Exception ignored) {
            SpecialAbilities.setCompoundDrawablesWithIntrinsicBounds(unknown, null, null, null);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPermissionStatuses();
    }


}