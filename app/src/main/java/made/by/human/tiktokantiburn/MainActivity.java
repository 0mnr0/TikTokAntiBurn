package made.by.human.tiktokantiburn;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import made.by.human.tiktokantiburn.helpers.ApplicationVersion;
import made.by.human.tiktokantiburn.helpers.UpdateChecker;
import made.by.human.tiktokantiburn.settings.DefaultSettings;
import made.by.human.tiktokantiburn.settings.MigrateFromOld;
import made.by.human.tiktokantiburn.utils.FloatTestEnv;


public class MainActivity extends AppCompatActivity {
    LogSystem logger;
    String projectURL = "https://github.com/0mnr0/TikTokAntiBurn";



    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
        AccessibilityManager am = (AccessibilityManager)
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        if (am == null) return false;

        List<AccessibilityServiceInfo> enabledServices =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        String expectedPackage = context.getPackageName();
        String expectedClass = accessibilityService.getName();

        for (AccessibilityServiceInfo info : enabledServices) {
            ServiceInfo serviceInfo = info.getResolveInfo().serviceInfo;
            if (expectedPackage.equals(serviceInfo.packageName) &&
                    expectedClass.equals(serviceInfo.name)) {
                return true;
            }
        }
        return false;
    }

    private static void onCalculated(int maxWindows) {

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

    public void requestNotification(View view){
        if (!areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Показать rationale, если пользователь уже отклонил однажды
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.POST_NOTIFICATIONS)) {
                        new AlertDialog.Builder(this)
                                .setTitle("Нужны уведомления")
                                .setMessage("Разреши уведомления, чтобы не пропустить важное.")
                                .setPositiveButton("Разрешить", (d, w) ->
                                        ActivityCompat.requestPermissions(this,
                                                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                                1001))
                                .setNegativeButton("Отмена", null)
                                .show();
                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                1001);
                    }
                }
            }
        }
    }

    public boolean areNotificationsEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return NotificationManagerCompat.from(this).areNotificationsEnabled();
        }
    }

    public void OpenGithub(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(projectURL));
        startActivity(browserIntent);
    }

    public void OpenGithubForAStar(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/0mnr0/TikTokAntiBurn"));
        startActivity(browserIntent);
        made.by.human.tiktokantiburn.settings.Settings.Iternal.setBool(this, "HidePleaseStar", true);
        findViewById(R.id.PleaseGiveAStar).setVisibility(View.GONE);
    }



    public void OpenExtendedSetting(View view) {
        if (!PermissionOverlayGranted()) {
            Toast.makeText(this, R.string.PleaseRequestOverlay, Toast.LENGTH_LONG).show();
            return;
        }

        Intent serviceIntent = new Intent(this, FloatingWindowService.class);
        serviceIntent.setAction("ACTION_CLOSE_WINDOW_IMMEDIATE");
        startService(serviceIntent);

        Intent intent = new Intent(this, SetupFloatingWindows.class);
        startService(intent);

    }

    public void openSpecificAccessibilityServiceSettings(View view) {
        if (!PermissionOverlayGranted()) {
            Toast.makeText(this, getString(R.string.GrandOverlayFirst), Toast.LENGTH_SHORT).show();
            openRequestTopWindow(null);
            return;
        }

        try {
            Intent openSettings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            openSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(openSettings, 1000);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.AcessabilitySettingNotReacheable), Toast.LENGTH_SHORT).show();
        }
    }

    public void openAppSettings(View view) {
        Intent serviceIntent = new Intent(this, SettingsActivity.class);
        startActivity(serviceIntent);
    }


    public void CleanLogs(View view) {
        logger.clear();
        Log.d("SomeRandomAction", "Clean Logs Detected");
        Toast.makeText(this, "Cleared!", Toast.LENGTH_SHORT).show();
    }
    public void ExportLogs(View view) {
        logger.Append("\n\n\n[ Permission OverlayGranted ] - " + PermissionOverlayGranted());
        logger.Append("\n[ Permission PermissionUsageGranted ] - " + PermissionUsageGranted());
        logger.Append("\n[ Permission SpecialAbilitiesGranted ] - " + PermissionSpecialAbilitiesGranted()+"\n");
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





    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LogSystem.getInstanceOrNull() == null) {
            LogSystem.init((Application) getApplicationContext());
        }
        logger = LogSystem.getInstance();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.MainAppLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView VersionCode = findViewById(R.id.VersionCode);
        made.by.human.tiktokantiburn.settings.Settings.Iternal.setBool(this, "viewsSetup", false);
        VersionCode.setText(ApplicationVersion.get(this));
        MigrateFromOld.start(this);
        DefaultSettings.Setup(this);
        if (!areNotificationsEnabled()) { requestNotification(null); }
        refreshPermissionStatuses();


        PeriodicWorkRequest checkRequest =
                new PeriodicWorkRequest.Builder(VersionCheckWorker.class, 2, TimeUnit.DAYS)
                        .setConstraints(
                                new Constraints.Builder()
                                        .setRequiredNetworkType(NetworkType.CONNECTED)
                                        .setRequiresBatteryNotLow(true)
                                        .build()
                        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_version_check",
                ExistingPeriodicWorkPolicy.KEEP,
                checkRequest
        );
        UpdateLogsVisibility();
        CheckUpdates();


        if (made.by.human.tiktokantiburn.settings.Settings.Iternal.getBool(this, "HidePleaseStar", false)) {
            findViewById(R.id.PleaseGiveAStar).setVisibility(View.GONE);
        }
    }

    public void UpdateLogsVisibility() {
        ConstraintLayout LogsPanel = findViewById(R.id.LogsPanel);
        LogsPanel.setVisibility(
                made.by.human.tiktokantiburn.settings.Settings.Service.getBool(this, ".enable_logging", true)
                ? View.VISIBLE
                        : View.GONE
        );
    }

    public boolean PermissionOverlayGranted(){
        return Settings.canDrawOverlays(this);
    }

    public boolean PermissionUsageGranted(){
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public boolean PermissionSpecialAbilitiesGranted(){
        return isAccessibilityServiceEnabled(this, MainAccessibilityService.class);
    }

    public void refreshPermissionStatuses() {
        Button AboveAllWindows, SpecialAbilities, requestNotification, ExtendedSetting;
        AboveAllWindows = findViewById(R.id.AboveAllWindows);
        SpecialAbilities = findViewById(R.id.SpecialAbilities);
        requestNotification = findViewById(R.id.requestNotification);
        ExtendedSetting = findViewById(R.id.ExtendedSetting);

        Drawable done = ContextCompat.getDrawable(this, R.drawable.check_circle);
        Drawable none = ContextCompat.getDrawable(this, R.drawable.x_circle);
        Drawable unknown = ContextCompat.getDrawable(this, R.drawable.patch_question);

        try {
            final boolean isGranted = PermissionOverlayGranted();

            ExtendedSetting.setAlpha(isGranted ? 1f : 0.75f);
            SpecialAbilities.setAlpha(isGranted ? 1f : 0.75f);

            AboveAllWindows.setCompoundDrawablesWithIntrinsicBounds(isGranted ? done : none, null, null, null);
            new Handler(Looper.getMainLooper()).postDelayed(() -> AboveAllWindows.setCompoundDrawablesWithIntrinsicBounds(PermissionOverlayGranted() ? done : none, null, null, null), 500);
        } catch (Exception ignored) {
            AboveAllWindows.setCompoundDrawablesWithIntrinsicBounds(unknown, null, null, null);
        }

        try {
            SpecialAbilities.setCompoundDrawablesWithIntrinsicBounds(PermissionSpecialAbilitiesGranted() ? done : none, null, null, null);
        } catch (Exception ignored) {
            SpecialAbilities.setCompoundDrawablesWithIntrinsicBounds(unknown, null, null, null);
        }

        requestNotification.setVisibility(areNotificationsEnabled() ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPermissionStatuses();
        UpdateLogsVisibility();
        made.by.human.tiktokantiburn.settings.Settings.Iternal.setBool(this, "viewsSetup", false);
    }

    public void CheckUpdates() {
        ConstraintLayout NewUpdateFound = findViewById(R.id.TheresNewUpdate);
        UpdateChecker.runAsync(this, (result) -> {
            if (result.isSuccessParse && result.haveNewUpdate) {
                if (result.ProjectURL != null) {projectURL = result.ProjectURL;}
                NewUpdateFound.setVisibility(View.VISIBLE);
            }
        });
    }

}