package made.by.human.tiktokantiburn.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import made.by.human.tiktokantiburn.BlockableLinearLayout;
import made.by.human.tiktokantiburn.R;

public class LSPosedSettingsFragment extends Fragment {
    TextView TopModifierDisabled, BottomModifierDisabled;
    Slider TopPaneModifierValue, BottomPaneModifierValue;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_lsposed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Context ctx = requireContext();




        final boolean[] bindMode = {Settings.Module.getBool(ctx, "StartWithBindMode", false)};
        final String whenBindModeEnabled = "Stop Bind Mode";
        final String whenBindModeDisabled = "Enable Bind Mode";

        BlockableLinearLayout SettingsRequiresNonBinderMode = view.findViewById(R.id.RequiresNonBinder);
        Button ActivateBinderAction = view.findViewById(R.id.ActivateBinderMode);
        ActivateBinderAction.setOnClickListener((v) -> {
            bindMode[0] = !bindMode[0];
            Settings.Module.setBool(ctx, "StartWithBinder", bindMode[0]);
            ActivateBinderAction.setText(bindMode[0] ? whenBindModeEnabled : whenBindModeDisabled);
            SettingsRequiresNonBinderMode.setAlpha(
                    bindMode[0] ? 0.35f : 1f
            );
            SettingsRequiresNonBinderMode.setBlockTouches(bindMode[0]);
        });


        ImageView CenterPlusButton = view.findViewById(R.id.CenterPlusButton);
        MaterialSwitch HidePlusButton = view.findViewById(R.id.HidePlusButton);
        HidePlusButton.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Settings.Module.setBool(ctx, "HidePlusButton", isChecked);
            CenterPlusButton.setScaleY(isChecked ? 0f : 1f); // setAlpha is useless
        }));
        HidePlusButton.setChecked(Settings.Module.getBool(ctx, "HidePlusButton", false));


        MaterialSwitch RemoveFromPanel = view.findViewById(R.id.RemoveFromPanel);
        RemoveFromPanel.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Settings.Module.setBool(ctx, "RemoveFromPanel", isChecked);
            CenterPlusButton.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        }));
        RemoveFromPanel.setChecked(Settings.Module.getBool(ctx, "RemoveFromPanel", false));

        MaterialSwitch OldDetectionMethod = view.findViewById(R.id.OldDetectionMethod);
        OldDetectionMethod.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Settings.Module.setBool(ctx, "OldDetectionMethod", isChecked);
        }));
        OldDetectionMethod.setChecked(Settings.Module.getBool(ctx, "OldDetectionMethod", false));

        MaterialSwitch Shake2Show_API = view.findViewById(R.id.Shake2Show_API);
        Shake2Show_API.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Settings.Module.setBool(ctx, "Shake2Show", isChecked);
        }));
        Shake2Show_API.setChecked(Settings.Module.getBool(ctx, "Shake2Show", false));






        // Slider's
        TopModifierDisabled = view.findViewById(R.id.topPaneModificatorDisabled);
        ImageView topPaneImage = view.findViewById(R.id.topPaneImage);
        TopPaneModifierValue = view.findViewById(R.id.TopPaneModifierValue);
        TopPaneModifierValue.addOnChangeListener((slider, progress, fromUser) -> {
            Settings.Module.setFloat(ctx, "TopPaneAlpha", progress);
            topPaneImage.setAlpha(progress/100f);
            updateInactiveTop();
        });
        TopPaneModifierValue.setValue(Settings.Module.getFloat(ctx, "TopPaneAlpha", 100f));



        BottomModifierDisabled = view.findViewById(R.id.bottomPaneModificatorDisabled);
        ImageView bottomPaneImage = view.findViewById(R.id.bottomPaneImage);
        BottomPaneModifierValue = view.findViewById(R.id.BottomPaneModifierValue_);
        BottomPaneModifierValue.addOnChangeListener((slider, progress, fromUser) -> {
            Settings.Module.setFloat(ctx, "BottomPaneAlpha", progress);
            bottomPaneImage.setAlpha(progress/100f);
            updateInactiveBottom();
        });
        BottomPaneModifierValue.setValue(Settings.Module.getFloat(ctx, "BottomPaneAlpha", 100f));
        updateInActiveText();


        // final switch
        LinearLayout WholeModuleLayout = view.findViewById(R.id.ModuleLayout);
        MaterialSwitch EnableModule = view.findViewById(R.id.EnableModule);
        EnableModule.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Settings.Module.setBool(ctx, "isModuleEnabled", isChecked);
            WholeModuleLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        EnableModule.setChecked(Settings.Module.getBool(ctx, "isModuleEnabled", false));
        WholeModuleLayout.setVisibility(Settings.Module.getBool(ctx, "isModuleEnabled", false) ? View.VISIBLE : View.GONE);


    }


    private void updateInActiveText(){
        updateInactiveTop();
        updateInactiveBottom();
    }
    private void updateInactiveTop(){
        TopModifierDisabled.setVisibility(TopPaneModifierValue.getValue() == 100f ? View.VISIBLE : View.GONE);
    }
    private void updateInactiveBottom(){
        BottomModifierDisabled.setVisibility(BottomPaneModifierValue.getValue() == 100f ? View.VISIBLE : View.GONE);
    }
}