package made.by.human.tiktokantiburn.settings;

import static made.by.human.tiktokantiburn.utils.tools.setSafeValue;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import made.by.human.tiktokantiburn.R;

public class MainSettingsFragment extends Fragment {
    private ConstraintLayout ConstraintDefaultPanelHeight, HideOnClickOpt;
    private TextInputEditText TriggerPacketName;


    private void HideTextInputFocus(Context context) {
        TriggerPacketName.clearFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(TriggerPacketName.getWindowToken(), 0);
    }





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Context ctx = requireContext();

        TriggerPacketName = view.findViewById(R.id.TriggerPacketName);
        TriggerPacketName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Settings.Service.setString(ctx, "TriggerPacketName", s.toString().replace(" ", ""));
            }
        });
        TriggerPacketName.setText(Settings.Service.getString(ctx, "TriggerPacketName", "com.zhiliaoapp.musically"));
        TriggerPacketName.setOnEditorActionListener((v, actionId, event) -> {
            HideTextInputFocus(ctx);
            return true;
        });


        MaterialSwitch inputMethodsSwitch = view.findViewById(R.id.InputMethodsSwitch);
        inputMethodsSwitch.setChecked(Settings.Service.getBool(ctx, "HideWhenKeyboardIsOpen", false));
        inputMethodsSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Settings.Service.setBool(ctx, "HideWhenKeyboardIsOpen", isChecked);
        }));


        MaterialSwitch hideOnTouch = view.findViewById(R.id.HideOnTouch);
        MaterialSwitch ClickThroughToggle = view.findViewById(R.id.ClickThroughToggle);

        hideOnTouch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Settings.Service.setBool(ctx, "HideOnTouch", isChecked);
            if (isChecked && !ClickThroughToggle.isChecked()) {
                ClickThroughToggle.setEnabled(false);
                ClickThroughToggle.setAlpha(0.75f);
            } else {
                ClickThroughToggle.setEnabled(true);
                ClickThroughToggle.setAlpha(1f);
            }
        }));
        hideOnTouch.setChecked(Settings.Service.getBool(ctx, "HideOnTouch", false));

        ClickThroughToggle.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Settings.Service.setBool(ctx, "touchThroughMode", isChecked);
            HideOnClickOpt.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        }));
        ClickThroughToggle.setChecked(Settings.Service.getBool(ctx, "touchThroughMode", true));


        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

        Slider DefaultPanelHeight = view.findViewById(R.id.DefaultPanelHeight);
        DefaultPanelHeight.addOnChangeListener((slider, progress, fromUser) -> {
            if ((int) progress < DefaultPanelHeight.getValueFrom()) {progress = DefaultPanelHeight.getValueFrom();}
            if ((int) progress > DefaultPanelHeight.getValueTo()) {progress = DefaultPanelHeight.getValueTo();}
            Settings.Service.setInt(ctx, "DefaultElementHeight", (int) progress);
        });

        int screenHeight = ctx.getResources().getDisplayMetrics().heightPixels;
        int savedValue = (int) (screenHeight * 0.09);

        DefaultPanelHeight.setValueFrom((savedValue + 30) / 3f);
        DefaultPanelHeight.setValueTo(savedValue);

        int setValue = Settings.Service.getInt(ctx, "DefaultElementHeight", 100);
        crashlytics.setCustomKey("userViewportHeight", screenHeight);
        crashlytics.setCustomKey("SH-savedValue", savedValue);
        crashlytics.setCustomKey("SH-setValue", savedValue);
        if (setValue < DefaultPanelHeight.getValueFrom()) {setValue = (int) DefaultPanelHeight.getValueFrom();}
        if (setValue > DefaultPanelHeight.getValueTo()) {setValue = (int) DefaultPanelHeight.getValueTo();}
        setSafeValue(DefaultPanelHeight, setValue);



        ConstraintDefaultPanelHeight = view.findViewById(R.id.ConstraintDefaultPanelHeight);
        HideOnClickOpt = view.findViewById(R.id.Main_3);
        MaterialSwitch ShowDefault = view.findViewById(R.id.ShowDefaultElement);
        ShowDefault.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Settings.Service.setBool(ctx, "ShowDefaultElement", isChecked);
            animateInactiveHeight(isChecked);
        }));
        if (Settings.Service.getBool(ctx, "ShowDefaultElement", true)) {
            ShowDefault.setChecked(true);
            animateInactiveHeight(true);
        } else {
            ShowDefault.setChecked(false);
            animateInactiveHeight(false);
        }
    }



    public void animateInactiveHeight(boolean isGloballyEnabled) {
        ConstraintDefaultPanelHeight.animate().alpha(isGloballyEnabled ? 1f : 0.65f).setDuration(200).start();
    }

}