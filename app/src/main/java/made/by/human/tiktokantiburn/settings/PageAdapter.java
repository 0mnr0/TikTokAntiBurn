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

    public PageAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    public void rootStatusUpdate() {
        haveRoot = RootCheck.isDeviceRooted();
        Log.d("haveRoot:", String.valueOf(haveRoot));
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1: return new AdditionalSettingsFragment();
            case 2: return new LSPosedSettingsFragment();
            default: return new MainSettingsFragment(); // 0
        }
    }

    @Override
    public int getItemCount() {
        return haveRoot ?
                tabsCount :
                tabsCount-1;
    }
}
