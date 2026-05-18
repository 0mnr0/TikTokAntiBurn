package made.by.human.tiktokantiburn.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;

import java.util.Locale;

public class DefaultSettings {
    private static boolean isEmpty(Context context, String fileName) {
        SharedPreferences prefs = context.getSharedPreferences(fileName, MODE_PRIVATE);
        return prefs.getAll().isEmpty();
    }


    public static void Setup(Context ctx) {
        SetupService(ctx);
        SetupModule(ctx);
        SetupIternal(ctx);
    }


    private static void SetupService(Context ctx){
        if (!Settings.Service.contains(ctx, "TriggerPacketName")) {
            Settings.Service.setString(ctx, "TriggerPacketName", "com.zhiliaoapp.musically");
        }

        if (!Settings.Service.contains(ctx, "HideWhenKeyboardIsOpen")) {
            Settings.Service.setBool(ctx, "HideWhenKeyboardIsOpen", true);
        }

        if (!Settings.Service.contains(ctx, "HideOnTouch")) {
            Settings.Service.setBool(ctx, "HideOnTouch", true);
        }

        if (!Settings.Service.contains(ctx, "ShowDefaultElement")) {
            Settings.Service.setBool(ctx, "ShowDefaultElement", true);
        }

        if (!Settings.Service.contains(ctx, "DefaultElementHeight")) {
            int screenHeight = ctx.getResources().getDisplayMetrics().heightPixels;
            int savedValue = (int) (screenHeight * 0.09);
            savedValue = (savedValue + 40) / 2;
            Settings.Service.setInt(ctx, "DefaultElementHeight", savedValue);
        }



        // -- Additional --


        if (!Settings.Service.contains(ctx, ".enable_logging")) {
            Settings.Service.setBool(ctx, ".enable_logging", true);
        }

        if (!Settings.Service.contains(ctx, "FullScreenAPISwitch")) {
            Settings.Service.setBool(ctx, "FullScreenAPISwitch", true);
        }

        if (!Settings.Service.contains(ctx, "Compatibility_MODE")) {
            Settings.Service.setBool(ctx, "Compatibility_MODE", false);
        }
    }

    private static void SetupModule(Context ctx){
        if (!Settings.Module.contains(ctx, "isModuleEnabled")) {
            Settings.Module.setBool(ctx, "isModuleEnabled", true);
        }
    }

    private static void SetupIternal(Context ctx){
        if (!Settings.Iternal.contains(ctx, "GitVerseAPI")) {
            if (Locale.getDefault().getLanguage().equals("ru")) {
                Settings.Iternal.setBool(ctx, "GitVerseAPI", true);
            }
        }
    }
}
