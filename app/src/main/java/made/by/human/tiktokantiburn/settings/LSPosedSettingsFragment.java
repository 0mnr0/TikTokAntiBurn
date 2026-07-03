package made.by.human.tiktokantiburn.settings;

import static made.by.human.tiktokantiburn.utils.tools.setSafeValue;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import made.by.human.tiktokantiburn.BlockableLinearLayout;
import made.by.human.tiktokantiburn.R;
import made.by.human.tiktokantiburn.root.Tools;
import made.by.human.tiktokantiburn.utils.tools;

public class LSPosedSettingsFragment extends Fragment {
    Context ctx;
    TextView TopModifierDisabled, BottomModifierDisabled;
    Slider TopPaneModifierValue, BottomPaneModifierValue;

    BlockableLinearLayout SettingsRequiresNonBinderMode;
    Button ActivateBinderAction;
    boolean activeBindMode = false;




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
        ctx = requireContext();




        activeBindMode = Settings.Module.getBool(ctx, "StartWithBinder", false);
        SettingsRequiresNonBinderMode = view.findViewById(R.id.RequiresNonBinder);
        ActivateBinderAction = view.findViewById(R.id.ActivateBinderMode);
        ActivateBinderAction.setOnClickListener((v) -> {
            activeBindMode = !activeBindMode;
            UpdateBindInfo();

            if (activeBindMode) {
                try {
                    Tools.forceStopTikTok(ctx);
                } catch (Exception e) {
                    Toast.makeText(ctx, getString(R.string.__Binder_Saved2), Toast.LENGTH_LONG).show();
                }
            }
        });
        if (activeBindMode) {
            UpdateBindInfo();
        }

        TextView removeAllModified = view.findViewById(R.id.removeAllModified);
        removeAllModified.setOnClickListener((v) -> askForModifiedReset());


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
        setSafeValue(TopPaneModifierValue, Settings.Module.getFloat(ctx, "TopPaneAlpha", 100f));



        BottomModifierDisabled = view.findViewById(R.id.bottomPaneModificatorDisabled);
        ImageView bottomPaneImage = view.findViewById(R.id.bottomPaneImage);
        BottomPaneModifierValue = view.findViewById(R.id.BottomPaneModifierValue_);
        BottomPaneModifierValue.addOnChangeListener((slider, progress, fromUser) -> {
            Settings.Module.setFloat(ctx, "BottomPaneAlpha", progress);
            bottomPaneImage.setAlpha(progress/100f);
            updateInactiveBottom();
        });
        setSafeValue(BottomPaneModifierValue, Settings.Module.getFloat(ctx, "BottomPaneAlpha", 100f));
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


    public void askForModifiedReset() {
        String descText = getString(R.string.Settings_LSP_RemoveModified_Desc);

        new MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.Settings_LSP_RemoveModified)
                .setMessage(descText)
                .setNegativeButton(getString(R.string.Cancel), (dialog, which) -> {
                    dialog.cancel();
                })
                .setPositiveButton(getString(R.string.Erase), (dialog, which) -> {
                    dialog.cancel();
                    runClearer();
                })
                .show();
    }

    public void runClearer() {
        Settings.Module.remove(ctx, "ElementsModifiers");
        try {
            Tools.forceStopTikTok(ctx);
            Toast.makeText(ctx, getString(R.string.Done), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(ctx, getString(R.string.__Binder_Saved2), Toast.LENGTH_LONG).show();
        }
    }


    public void UpdateBindInfo() {
        final String whenBindModeEnabled = getString(R.string.Settings_LSP_BinderTitle_OFF);
        final String whenBindModeDisabled = getString(R.string.Settings_LSP_BinderTitle_ON);

        Settings.Module.setBool(ctx, "StartWithBinder", activeBindMode);
        ActivateBinderAction.setText(activeBindMode ? whenBindModeEnabled : whenBindModeDisabled);
        SettingsRequiresNonBinderMode.setAlpha(
                activeBindMode ? 0.35f : 1f
        );
        SettingsRequiresNonBinderMode.setBlockTouches(activeBindMode);
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



    @Override
    public void onResume(){
        super.onResume();
        onResumeNotify();
    }

    public void onResumeNotify() {
        if (ctx == null) {return;}
        if (activeBindMode != Settings.Module.getBool(ctx, "StartWithBinder", false)) {
            ActivateBinderAction.performClick();
        }
    }
}