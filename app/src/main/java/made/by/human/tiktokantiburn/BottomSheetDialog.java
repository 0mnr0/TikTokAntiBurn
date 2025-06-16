package made.by.human.tiktokantiburn;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XSharedPreferences;

public class BottomSheetDialog extends BottomSheetDialogFragment {
    private View view;
    private Context context;
    private MaterialSwitch HideForACoupleSeconds, CompatibilityMode, MainFloatingWindowEnabled, AllowModuleSwitch;
    private ConstraintLayout SomeSetting;
    private Slider seekBar;
    private SharedPreferences sharedPreferences;
    private TextView progressText;
    private TextInputEditText TriggerPacketName;

    boolean isLSPosedInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo("org.lsposed.manager", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    @SuppressLint({"SetWorldReadable", "ApplySharedPref"})
    public void SaveSettings(String settingName, Object value) {
        SharedPreferences prefs = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
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
        File prefsDir = new File(context.getApplicationInfo().dataDir + "/shared_prefs");
        File prefsFile = new File(prefsDir, "Preferences.xml");
        prefsDir.setReadable(true, false);
        prefsFile.setReadable(true, false);
        File dataDir = new File(context.getApplicationInfo().dataDir);
        dataDir.setExecutable(true, false);
        dataDir.setReadable(true, false);

    }





    public boolean GetBoolean(String settingName, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getBoolean(settingName, defaultValue);
    }


    public boolean GetClickableStatus(){
        SharedPreferences prefs = context.getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getBoolean("Clickable", false);
    }

    public void SetClickableStatus(boolean status){
        SharedPreferences prefs = context.getSharedPreferences("Preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("Clickable", status);
        editor.apply();
    }

    public void CheckSomeSettings(){
        SomeSetting.setVisibility(GetBoolean("DisableMainFloatingWindow", false) ? View.GONE : View.VISIBLE);
    }

    public boolean isSettingKeyExists(String CollectionName, String settingName) {
        SharedPreferences prefs = context.getSharedPreferences(CollectionName, MODE_PRIVATE);
        return prefs.contains(settingName);
    }

    public String GetString(String settingName, String defValue) {
        SharedPreferences prefs = context.getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getString(settingName, defValue);
    }

    public void pushContext(Context context) {
        this.context = context;
    }
    public void setPreferenceWithRoot(String packageName, String prefsFile, String key, String value) {
        String dirPath = "/data/data/" + packageName + "/shared_prefs/";
        String fullPath = dirPath + prefsFile + ".xml";

        // Полноценная XML-строка
        String newEntry = String.format("<string name=\"%s\">%s</string>", key, value);

        String command = String.format(
                "mkdir -p '%s'; " +
                        "if [ ! -f '%s' ]; then " +
                        "echo '<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<map>\n%s\n</map>' > '%s'; " +
                        "else " +
                        "if grep -q '<string name=\"%s\">' '%s'; then " +
                        "sed -i 's|<string name=\"%s\">.*</string>|%s|' '%s'; " +
                        "else " +
                        "sed -i '/<map>/a\\    %s' '%s'; " +
                        "fi; " +
                        "fi; " +
                        "chmod 660 '%s'; " +
                        "chown %s.%s '%s';",
                dirPath,
                fullPath,
                newEntry,
                fullPath,
                key, fullPath,
                key, newEntry, fullPath,
                newEntry, fullPath,
                fullPath,
                packageName, packageName, fullPath
        );

        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(su.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            su.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getPreferenceWithRoot(String packageName, String prefsFile, String key, Object defaultValue) {
        String fullPath = "/data/data/" + packageName + "/shared_prefs/" + prefsFile + ".xml";
        String grepCommand = "grep '<string name=\"" + key + "\">' " + fullPath;

        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(su.getOutputStream());
            DataInputStream is = new DataInputStream(su.getInputStream());

            os.writeBytes(grepCommand + "\n");
            os.writeBytes("exit\n");
            os.flush();

            su.waitFor();

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            // Пример строки: <string name="ROOT:EnablePlusStatus">true</string>
            Pattern pattern = Pattern.compile(">(.*?)<");
            Matcher matcher = pattern.matcher(output.toString());
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_burn_settings, container, false);

        SomeSetting = view.findViewById(R.id.SomeSetting);

        // Hide elements for a couple seconds
        HideForACoupleSeconds = view.findViewById(R.id.TheSwitchingTool);
        HideForACoupleSeconds.setChecked(GetClickableStatus());
        HideForACoupleSeconds.setOnCheckedChangeListener((buttonView, isChecked) -> SetClickableStatus(isChecked));

        // Compatibility mode
        CompatibilityMode = view.findViewById(R.id.OptimalSwitcher);
        CompatibilityMode.setChecked(GetBoolean("CompatibilityMode", false));
        CompatibilityMode.setOnCheckedChangeListener((buttonView, isChecked) -> SaveSettings("CompatibilityMode", isChecked));

        // Main Floating Window Disabled
        MainFloatingWindowEnabled = view.findViewById(R.id.MinifiedVersion);
        MainFloatingWindowEnabled.setChecked(GetBoolean("DisableMainFloatingWindow", false));
        CheckSomeSettings();
        MainFloatingWindowEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SaveSettings("DisableMainFloatingWindow", isChecked);
            CheckSomeSettings();
        });


        // Main Element Height Text
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int max = (int) (screenHeight * 0.1);
        sharedPreferences = context.getSharedPreferences("SeekBarPrefs", MODE_PRIVATE);
        int savedValue = sharedPreferences.getInt("seekBarValue", 40);
        if (!isSettingKeyExists("SeekBarPrefs", "seekBarValue")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            savedValue = (max + 40) / 2;
            editor.putInt("seekBarValue", savedValue);
            editor.apply();
        }
        progressText = view.findViewById(R.id.textView);
        progressText.setText(getString(R.string.fastSettingsMainFlowtingWindow) + savedValue + " px");

        // Main Element Height
        seekBar = view.findViewById(R.id.slider);
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


        TriggerPacketName = view.findViewById(R.id.TriggerPacketName);
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


        AllowModuleSwitch = view.findViewById(R.id.AllowModuleSwitch);
        AllowModuleSwitch.setChecked(GetBoolean("ROOT:EnablePlusModule", false));
        AllowModuleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SaveSettings("ROOT:EnablePlusModule", isChecked);
        });

        AutoCompleteTextView dropdown = view.findViewById(R.id.exposed_dropdown);
        String[] items = new String[] {"Убрать из меню", "Сделать невидимым"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                items
        );
        dropdown.setAdapter(adapter);
        dropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SaveSettings("ExposedDropdown", i);
            }
        });
        dropdown.setText(GetString("ExposedDropdown", "0"));

        return view;
    }

    private void HideTextInputFocus() {
        if (TriggerPacketName != null) {
            TriggerPacketName.clearFocus();

            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(TriggerPacketName.getWindowToken(), 0);
            }
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (TriggerPacketName != null && TriggerPacketName.isFocused()) {
                            HideTextInputFocus();
                        } else {
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                });
    }

}

