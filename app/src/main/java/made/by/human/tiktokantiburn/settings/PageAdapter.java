package made.by.human.tiktokantiburn.settings;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import made.by.human.tiktokantiburn.RootCheck;

public class PageAdapter extends FragmentStateAdapter {
    boolean haveRoot = false;
    int tabsCount = 3; // with root



    AdditionalSettingsFragment AdditionalSettings;
    LSPosedSettingsFragment LSPosedSettings;
    MainSettingsFragment MainSettings;

    public PageAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    public void init() {
        haveRoot = RootCheck.isDeviceRooted();
        haveRoot = true;
        AdditionalSettings = new AdditionalSettingsFragment();
        LSPosedSettings = new LSPosedSettingsFragment();
        MainSettings = new MainSettingsFragment();
    }

    public void onResumeNotify() {
        LSPosedSettings.onResumeNotify();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1: return AdditionalSettings;
            case 2: return LSPosedSettings;
            default: return MainSettings; // 0
        }
    }

    @Override
    public int getItemCount() {
        return haveRoot ?
                tabsCount :
                tabsCount-1;
    }
}
