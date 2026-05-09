package made.by.human.tiktokantiburn.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import made.by.human.tiktokantiburn.R;

public class MainSettingsFragment extends Fragment {

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
        hideOnTouch.setChecked(Settings.Service.getBool(ctx, "HideOnTouch", false));
        hideOnTouch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Settings.Service.setBool(ctx, "HideOnTouch", isChecked);
        }));








    }






}