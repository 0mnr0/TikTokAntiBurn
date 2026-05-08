package made.by.human.tiktokantiburn.helpers;

import android.app.Activity;
import android.content.Context;

import made.by.human.tiktokantiburn.settings.__SettingsGetter;

public class ShouldRun {
    public static boolean check(Activity activity) {
        if (activity == null) {return false;}
        return
                __SettingsGetter.getBoolean(activity, "isModuleEnabled", false)
                && !__SettingsGetter.getBoolean(activity, "StartWithBinder", false);
    }
    public static boolean check(Context ctx) {
        if (ctx == null) {return false;}
        return __SettingsGetter.getBoolean(ctx, "isModuleEnabled", false)
                && !__SettingsGetter.getBoolean(ctx, "StartWithBinder", false);
    }
    public static boolean check(Object activity) {
        if (activity == null) {return false;}
        Activity newActivity = (Activity) activity;
        return __SettingsGetter.getBoolean(newActivity, "isModuleEnabled", false)
                && !__SettingsGetter.getBoolean(newActivity, "StartWithBinder", false);
    }


    public static boolean check(Object activity, boolean ignoreBinder) {
        if (activity == null) {return false;}
        Activity newActivity = (Activity) activity;
        return __SettingsGetter.getBoolean(newActivity, "isModuleEnabled", false);
    }
}
