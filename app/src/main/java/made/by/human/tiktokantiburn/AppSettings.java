package made.by.human.tiktokantiburn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppSettings extends AppCompatActivity {
    private View view;
    private MaterialSwitch HideForACoupleSeconds, CompatibilityMode, MainFloatingWindowEnabled, InputMethodsSwitch,
            UseOldDetectionMethod, MakeInvisibleInstead, TopPaneModifier;
    private ConstraintLayout SomeSetting;
    private Slider seekBar;
    private SharedPreferences sharedPreferences;
    private TextView progressText, topPaneModificatorDescription;
    private TextInputEditText TriggerPacketName;
    boolean LSPosed_INVISIBLE, LSPosed_OLD_METHOD;
    private LoadingIndicator loadingIndicator;
    Slider TopPaneModifierValue;
    int TopPaneOpacity = 0;

    @SuppressLint({"SetWorldReadable", "ApplySharedPref"})
    public void SaveSettings(String settingName, Object value) {
        SharedPreferences prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
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

        editor.commit();
    }





    public boolean GetBoolean(String settingName, boolean defaultValue) {
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getBoolean(settingName, defaultValue);
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

    public void CheckSomeSettings(){
        SomeSetting.setVisibility(GetBoolean("DisableMainFloatingWindow", false) ? View.GONE : View.VISIBLE);
    }

    public boolean isSettingKeyExists(String CollectionName, String settingName) {
        SharedPreferences prefs = getSharedPreferences(CollectionName, MODE_PRIVATE);
        return prefs.contains(settingName);
    }

    public String GetString(String settingName, String defValue) {
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getString(settingName, defValue);
    }

    public boolean SaveLSPosed() {
        final String packageName = "com.zhiliaoapp.musically";
        String prefsPath = "/data/data/" + packageName + "/shared_prefs/LSPrefs.xml";

        String xmlContent = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n" +
                "<map>\n" +
                "    <boolean name=\"" + "XPOSED:MakeInvisibleInstead" + "\" value=\"" + LSPosed_INVISIBLE + "\" />\n" +
                "    <boolean name=\"" + "XPOSED:OldHookMethod" + "\" value=\"" + LSPosed_OLD_METHOD + "\" />\n" +
                "    <boolean name=\"" + "XPOSED:AllowTopPaneModifier" + "\" value=\"" + TopPaneModifier.isChecked() + "\" />\n" +
                "    <int name=\"" + "XPOSED:TopPaneOpacity" + "\" value=\"" + TopPaneOpacity + "\" />\n" +
                "</map>\n";

        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(packageName, 0);
            int uid = appInfo.uid;

            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(su.getOutputStream());

            os.writeBytes("mkdir -p /data/data/" + packageName + "/shared_prefs\n");

            os.writeBytes("cat > " + prefsPath + " << EOF\n");
            os.writeBytes(xmlContent);
            os.writeBytes("EOF\n");

            os.writeBytes("chown " + uid + ":" + uid + " " + prefsPath + "\n");
            os.writeBytes("chmod 660 " + prefsPath + "\n");
            os.writeBytes("am force-stop com.zhiliaoapp.musically\n");

            os.writeBytes("exit\n");
            os.flush();

            int exitCode = su.waitFor();
            return exitCode == 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean ReadLSPosedSetting(String key, Boolean defaultValue) {
        final String prefsPath = "/data/data/com.zhiliaoapp.musically/shared_prefs/LSPrefs.xml";

        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(su.getOutputStream());
            os.writeBytes("cat " + prefsPath + "\n");
            os.writeBytes("exit\n");
            os.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(su.getInputStream()));
            StringBuilder xmlContent = new StringBuilder(); String line;

            while ((line = reader.readLine()) != null) { xmlContent.append(line).append("\n"); } su.waitFor();

            Pattern pattern = Pattern.compile("<boolean name=\"" + Pattern.quote(key) + "\" value=\"(true|false)\"\\s*/>");
            Matcher matcher = pattern.matcher(xmlContent.toString());

            return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : defaultValue;

        } catch (Exception e) {
            return defaultValue;
        }
    }
    public int ReadLSPosedSetting(String key, int defaultValue) {
        final String prefsPath = "/data/data/com.zhiliaoapp.musically/shared_prefs/LSPrefs.xml";

        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(su.getOutputStream());
            os.writeBytes("cat " + prefsPath + "\n");
            os.writeBytes("exit\n");
            os.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(su.getInputStream()));
            StringBuilder xmlContent = new StringBuilder(); String line;

            while ((line = reader.readLine()) != null) { xmlContent.append(line).append("\n"); } su.waitFor();

            Pattern pattern = Pattern.compile("<int name=\"" + Pattern.quote(key) + "\" value=\"(\\d+)\"\\s*/>");
            Matcher matcher = pattern.matcher(xmlContent.toString());

            return matcher.find() ? Integer.parseInt(Objects.requireNonNull(matcher.group(1))) : defaultValue;

        } catch (Exception e) {
            return defaultValue;
        }
    }


    public boolean CheckLSPosedAvaiable() {
        try {
            String[] paths = {
                    "/system/app/Superuser.apk",
                    "/sbin/su",
                    "/system/bin/su",
                    "/system/xbin/su",
                    "/data/local/xbin/su",
                    "/data/local/bin/su",
                    "/system/sd/xbin/su",
                    "/system/bin/failsafe/su",
                    "/data/local/su"
            };
            for (String path : paths) {
                File file = new File(path);
                if (file.exists()) {
                    Log.d("LSPosed", "LSPosed is installed!");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e("LSPosed", "Error checking LSPosed availability", e);
            return true;
        }
    }


    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appsettings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.MainAppLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TopPaneModifier = findViewById(R.id.TopPaneModifier);
        TopPaneModifierValue = findViewById(R.id.TopPaneModifierValue);
        SomeSetting = findViewById(R.id.SomeSetting);

        // Hide elements for a couple seconds
        HideForACoupleSeconds = findViewById(R.id.TheSwitchingTool);
        HideForACoupleSeconds.setChecked(GetClickableStatus());
        HideForACoupleSeconds.setOnCheckedChangeListener((buttonView, isChecked) -> SetClickableStatus(isChecked));

        // Compatibility mode
        CompatibilityMode = findViewById(R.id.OptimalSwitcher);
        CompatibilityMode.setChecked(GetBoolean("CompatibilityMode", false));
        CompatibilityMode.setOnCheckedChangeListener((buttonView, isChecked) -> SaveSettings("CompatibilityMode", isChecked));

        // Main Floating Window Disabled
        MainFloatingWindowEnabled = findViewById(R.id.MinifiedVersion);
        MainFloatingWindowEnabled.setChecked(GetBoolean("DisableMainFloatingWindow", false));
        CheckSomeSettings();
        MainFloatingWindowEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SaveSettings("DisableMainFloatingWindow", isChecked);
            CheckSomeSettings();
        });

        // Hide all when GBoard is working
        InputMethodsSwitch = findViewById(R.id.InputMethodsSwitch);
        InputMethodsSwitch.setChecked(GetBoolean("InputMethodSkip", false));
        CheckSomeSettings();
        InputMethodsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SaveSettings("InputMethodSkip", isChecked);
        });

        // Main Element Height Text
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int max = (int) (screenHeight * 0.09);
        sharedPreferences = getSharedPreferences("SeekBarPrefs", MODE_PRIVATE);
        int savedValue = sharedPreferences.getInt("seekBarValue", 40);
        if (!isSettingKeyExists("SeekBarPrefs", "seekBarValue")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            savedValue = (max + 40) / 2;
            editor.putInt("seekBarValue", savedValue);
            editor.apply();
        }
        topPaneModificatorDescription = findViewById(R.id.topPaneModificatorDescription);
        progressText = findViewById(R.id.textView);
        progressText.setText(getString(R.string.fastSettingsMainFlowtingWindow) + savedValue + " px");

        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Main Element Height
        seekBar = findViewById(R.id.slider);
        seekBar.setValueTo(max);
        seekBar.setValueFrom(40);



        seekBar.setValue(savedValue);
        seekBar.addOnChangeListener((slider, progress, fromUser) -> {
            int value = (int) Math.max(progress, 40);
            progressText.setText(getString(R.string.fastSettingsMainFlowtingWindow) + value + " px");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("seekBarValue", value);
            editor.apply();
        });
        seekBar.setLabelFormatter(label -> ((int) Math.max(seekBar.getValue(), 40)) + " px");


        TriggerPacketName = findViewById(R.id.TriggerPacketName);
        TriggerPacketName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                SaveSettings("TriggerPacketName", s.toString());
            }
        });
        TriggerPacketName.setText(GetString("TriggerPacketName", "com.zhiliaoapp.musically"));
        TriggerPacketName.setOnEditorActionListener((v, actionId, event) -> {
            HideTextInputFocus();
            return true;
        });


        loadingIndicator.setVisibility(View.VISIBLE);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean AllowTopPaneModifier = ReadLSPosedSetting("XPOSED:AllowTopPaneModifier", false);
            boolean invisible = ReadLSPosedSetting("XPOSED:MakeInvisibleInstead", false);
            boolean oldMethod = ReadLSPosedSetting("XPOSED:OldHookMethod", false);
            TopPaneOpacity = ReadLSPosedSetting("XPOSED:TopPaneOpacity", 100);
            boolean available = CheckLSPosedAvaiable();

            handler.post(() -> {
                MakeInvisibleInstead = findViewById(R.id.MakeInvisibleInstead);
                MakeInvisibleInstead.setChecked(invisible);
                MakeInvisibleInstead.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    LSPosed_INVISIBLE = isChecked;

                    executor.execute(() -> {
                        boolean saved = SaveLSPosed();
                        if (!saved) {
                            handler.post(() -> Toast.makeText(this, "Failed to save preferences to TikTok", Toast.LENGTH_SHORT).show());
                        }
                    });
                });


                TopPaneModifier.setChecked(AllowTopPaneModifier);
                TopPaneModifier.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                    executor.execute(() -> {
                        boolean saved = SaveLSPosed();
                        if (!saved) {
                            handler.post(() -> Toast.makeText(this, "Failed to save preferences to TikTok", Toast.LENGTH_SHORT).show());
                        }
                    });
                }));

                UseOldDetectionMethod = findViewById(R.id.UseOldDetectionMethod);
                UseOldDetectionMethod.setChecked(oldMethod);
                UseOldDetectionMethod.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    LSPosed_OLD_METHOD = isChecked;

                    executor.execute(() -> {
                        boolean saved = SaveLSPosed();
                        if (!saved) {
                            handler.post(() -> Toast.makeText(this, "Failed to save preferences to TikTok", Toast.LENGTH_SHORT).show());
                        }
                    });
                });

                topPaneModificatorDescription.setText(getString(R.string.IdleBrightness)  + " " + TopPaneOpacity + "%");
                TopPaneModifierValue.setValue(TopPaneOpacity);
                TopPaneModifierValue.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                    @Override
                    public void onStartTrackingTouch(@NonNull Slider slider) {}

                    @Override
                    public void onStopTrackingTouch(@NonNull Slider slider) {
                        TopPaneOpacity = (int) slider.getValue();
                        topPaneModificatorDescription.setText(getString(R.string.IdleBrightness) + " " + TopPaneOpacity + "%");

                        executor.execute(() -> {
                            boolean saved = SaveLSPosed();
                            if (!saved) {
                                handler.post(() -> Toast.makeText(AppSettings.this, "Failed to save preferences to TikTok", Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                });


                if (!available) {
                    ConstraintLayout LSPosedSettings = findViewById(R.id.LSPosedSettings);
                    LSPosedSettings.setVisibility(View.GONE);
                }
                loadingIndicator.setVisibility(View.GONE);
            });
        });



    }


    private void HideTextInputFocus() {
        TriggerPacketName.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(TriggerPacketName.getWindowToken(), 0);
    }
}