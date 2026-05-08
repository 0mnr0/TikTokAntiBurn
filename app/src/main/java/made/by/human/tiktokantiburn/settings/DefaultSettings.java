package made.by.human.tiktokantiburn.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class DefaultSettings {

    private static boolean isEmpty(Context context, String fileName) {
        SharedPreferences prefs = context.getSharedPreferences(fileName, MODE_PRIVATE);
        return prefs.getAll().isEmpty();
    }


    public static void Setup(Context context) {
        boolean SetupService = isEmpty(context, "ServiceSettings");
        boolean ModuleService = isEmpty(context, "ModuleSettings");
        if (SetupService) {SetupService(context);}
        if (ModuleService) {SetupModule(context);}
    }


    private static void SetupService(Context ctx){
        SharedPreferences.Editor prefs = ctx.getSharedPreferences("ServiceSettings", MODE_PRIVATE).edit();

        prefs.putString("TriggerPacketName", "com.zhiliaoapp.musically").apply();
        prefs.putBoolean("HideWhenKeyboardIsOpen", true).apply();
        prefs.putBoolean("HideOnTouch", true).apply();

        // -- Additional --
        prefs.putBoolean(".enable_logging", true).apply();
        prefs.putBoolean("FullScreenAPISwitch", true).apply();
        prefs.putBoolean("Compatibility_MODE", false).apply();
    }

    private static void SetupModule(Context ctx){
        SharedPreferences.Editor prefs = ctx.getSharedPreferences("ModuleSettings", MODE_PRIVATE).edit();
        prefs.putBoolean("EnableModule", true).apply();
    }
}
