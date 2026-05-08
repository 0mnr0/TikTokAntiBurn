package made.by.human.tiktokantiburn;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import made.by.human.tiktokantiburn.settings.PageAdapter;

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
        LinearLayout AppTitleLayout = findViewById(R.id.AppTitleLayout);
        ViewCompat.setOnApplyWindowInsetsListener(AppTitleLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });


        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        adapter = new PageAdapter(this);
        adapter.rootStatusUpdate();
        viewPager.setAdapter(adapter);


        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Главные"); break;
                case 1: tab.setText("Дополнительно"); break;
                case 2: tab.setText("LSPosed"); break;
            }
        }).attach();
    }




    private <T extends Fragment> T getFragment(int index, Class<T> clazz) {
        return clazz.cast(getSupportFragmentManager()
                .findFragmentByTag("f" + index));
    }
}
