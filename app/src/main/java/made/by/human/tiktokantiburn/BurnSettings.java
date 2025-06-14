package made.by.human.tiktokantiburn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

public class BurnSettings extends AppCompatActivity {
    MaterialSwitch HideForACoupleSeconds, CompatibilityMode, MainFloatingWindowEnabled;
    private ConstraintLayout SomeSetting;
    private Slider seekBar;
    private SharedPreferences sharedPreferences;
    private TextView progressText;
    private static final String PREF_VALUE = "";

    TextInputEditText TriggerPacketName;



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
        if (GetBoolean("DisableMainFloatingWindow", false)) { SomeSetting.setVisibility(View.GONE); } else { SomeSetting.setVisibility(View.VISIBLE); }
    }

    public boolean isSettingKeyExists(String CollectionName, String settingName) {
        SharedPreferences prefs = getSharedPreferences(CollectionName, MODE_PRIVATE);
        return prefs.contains(settingName);
    }

    public String GetString(String settingName, String defValue) {
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getString(settingName, defValue);
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_burn_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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


        // Main Element Height Text
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int max = (int) (screenHeight * 0.1);
        sharedPreferences = getSharedPreferences("SeekBarPrefs", MODE_PRIVATE);
        int savedValue = sharedPreferences.getInt("seekBarValue", 40);
        if (!isSettingKeyExists("SeekBarPrefs", "seekBarValue")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            savedValue = (max + 40) / 2;
            editor.putInt("seekBarValue", savedValue);
            editor.apply();
        }
        progressText = findViewById(R.id.textView);
        progressText.setText(getString(R.string.fastSettingsMainFlowtingWindow) + savedValue + " px");

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

    }


    public void HideTextInputFocus() {
        TriggerPacketName.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(TriggerPacketName.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        if (TriggerPacketName.isFocused()) {
            HideTextInputFocus();
        } else {
            super.onBackPressed();
        }
    }
}