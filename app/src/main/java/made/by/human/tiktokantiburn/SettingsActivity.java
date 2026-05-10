package made.by.human.tiktokantiburn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import made.by.human.tiktokantiburn.settings.DefaultSettings;
import made.by.human.tiktokantiburn.settings.PageAdapter;
import made.by.human.tiktokantiburn.settings.Settings;

public class SettingsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private PageAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });


        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.inflateMenu(R.menu.top_app_bar);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_more) {
                View anchor = toolbar.findViewById(R.id.action_more);
                showSearchDropdown(anchor);
                return true;

            }
            return false;
        });

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });



        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        adapter = new PageAdapter(this);
        adapter.init();
        viewPager.setAdapter(adapter);


        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Главные"); break;
                case 1: tab.setText("Дополнительно"); break;
                case 2: tab.setText("LSPosed"); break;
            }
        }).attach();
    }


    @Override
    public void onResume() {
        super.onResume();
        adapter.onResumeNotify();
    }


    private void showSearchDropdown(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.settings_dropdown, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.settings_reset) {
                askForSettingsReset();
                return true;
            }
            return false;
        });

        popup.show();
    }

    public void askForSettingsReset() {
        boolean isServiceSettings = tabLayout.getSelectedTabPosition()+1 != adapter.tabsCount;
        String descText = isServiceSettings
                ? getString(R.string.Settings_MENU_ResetService)
                : getString(R.string.Settings_MENU_ResetModule);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.Settings_MENU_ResetTitle)
                .setMessage(descText)
                .setNegativeButton(getString(R.string.Cancel), (dialog, which) -> {
                    dialog.cancel();
                })
                .setPositiveButton(getString(R.string.Erase), (dialog, which) -> {
                    dialog.cancel();
                    runClearer(isServiceSettings);
                })
                .show();
    }

    public void runClearer(boolean isServiceSettings) {
        if (isServiceSettings) {
            Settings.Service.clear(this);
            Settings.Iternal.clear(this);
        } else {
            Settings.Module.clear(this);
        }
        Toast.makeText(this, getString(R.string.Settings_MENU_WasErased), Toast.LENGTH_LONG).show();

        DefaultSettings.Setup(this);
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }



    private <T extends Fragment> T getFragment(int index, Class<T> clazz) {
        return clazz.cast(getSupportFragmentManager()
                .findFragmentByTag("f" + index));
    }
}
