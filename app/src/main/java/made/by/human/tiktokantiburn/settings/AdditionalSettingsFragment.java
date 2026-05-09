package made.by.human.tiktokantiburn.settings;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;

import made.by.human.tiktokantiburn.LogSystem;
import made.by.human.tiktokantiburn.R;

public class AdditionalSettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_additional, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Context ctx = requireContext();


        MaterialSwitch Compatibility_MODE = view.findViewById(R.id.Compatibility_MODE);
        Compatibility_MODE.setChecked(Settings.Service.getBool(ctx, "Compatibility_MODE", false));
        Compatibility_MODE.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Settings.Service.setBool(ctx, "Compatibility_MODE", isChecked);
        }));


        MaterialSwitch FullScreenAPISwitch = view.findViewById(R.id.FullScreenAPISwitch);
        FullScreenAPISwitch.setChecked(Settings.Service.getBool(ctx, "FullScreenAPISwitch", false));
        FullScreenAPISwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Settings.Service.setBool(ctx, "FullScreenAPISwitch", isChecked);
        }));

        MaterialSwitch EnableLogging = view.findViewById(R.id.EnableLogging);
        EnableLogging.setChecked(Settings.Service.getBool(ctx, ".enable_logging", false));
        EnableLogging.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Settings.Service.setBool(ctx, ".enable_logging", isChecked);
            if (!isChecked) {
                LogSystem.getInstance().clear();
            }
        }));




        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)) {
            FullScreenAPISwitch.setChecked(false);
            Settings.Service.setBool(ctx, "FullScreenAPISwitch", false);
            FullScreenAPISwitch.setEnabled(false);
            FullScreenAPISwitch.setAlpha(0.5f);

            TextView FullScreenDesc = view.findViewById(R.id.FullScreenAPIText);
            FullScreenDesc.setText(getString(R.string.Settings_ADD_FULLSCREEN_API_NotSupported));
        }


    }
}