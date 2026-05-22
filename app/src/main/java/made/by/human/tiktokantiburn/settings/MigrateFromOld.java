package made.by.human.tiktokantiburn.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import made.by.human.tiktokantiburn.BlockInfo;
import made.by.human.tiktokantiburn.R;

public class MigrateFromOld {
    public static void start(Context ctx) {
        if (Settings.Iternal.getBool(ctx, "MigrationTo_1.5.0_Done", false)) {return;}
        Settings.Iternal.setBool(ctx, "MigrationTo_1.5.0_Done", true);


        boolean somethingWentWrong = false;

        try {
            SharedPreferences Old_Preferences = Settings.getFile(ctx, "Preferences");
            String triggerPkg = Old_Preferences.getString("TriggerPacketName", "com.zhiliaoapp.musically");
            // boolean EnablePreview = Old_Preferences.getBoolean("EnablePreview", false);                                   NOT NEEDED ANYMORE
            boolean CompatibilityMode = Old_Preferences.getBoolean("CompatibilityMode", false);
            boolean ClickableViews = Old_Preferences.getBoolean("Clickable", false);
            boolean InputMethodSkip = Old_Preferences.getBoolean("InputMethodSkip", false);
            boolean FullScreenAPI = Old_Preferences.getBoolean("FullScreenAPI", false);
            boolean DisableMainFloatingWindow = Old_Preferences.getBoolean("DisableMainFloatingWindow", false);


            Settings.Service.setString(ctx, "TriggerPacketName", triggerPkg);
            Settings.Service.setBool(ctx, "HideWhenKeyboardIsOpen", InputMethodSkip);
            Settings.Service.setBool(ctx, "HideOnTouch", ClickableViews);
            Settings.Service.setBool(ctx, "ShowDefaultElement", !DisableMainFloatingWindow);
            Settings.Service.setBool(ctx, "FullScreenAPISwitch", FullScreenAPI);
            Settings.Service.setBool(ctx, "Compatibility_MODE", CompatibilityMode);
        } catch (Exception ignored) {somethingWentWrong = true;}



        try {
            SharedPreferences Old_Preferences = Settings.getFile(ctx, "blockPos");
            String strBlockList = Old_Preferences.getString("block_list", null);
            Log.d("blockInfo: ", "start");
            if (strBlockList == null) {return;}

            List<BlockInfo> blockList = new Gson().fromJson(strBlockList, new TypeToken<List<BlockInfo>>(){}.getType());
            if (blockList == null || blockList.isEmpty()) {
                return;
            }

            List<BlockInfo> newBlockList = new ArrayList<>(blockList);
            Settings.Service.setString(ctx, "block_list", new Gson().toJson(newBlockList));


        } catch (Exception ignored) {somethingWentWrong = true;}



        Settings.deleteFile(ctx, "Preferences");
        Settings.deleteFile(ctx, "blockPos");
        Settings.deleteFile(ctx, "SeekBarPrefs");


        if (somethingWentWrong) {
            Toast.makeText(ctx, ctx.getString(R.string.SETTINGS_MIGRATION_FAILED), Toast.LENGTH_SHORT).show();
        }
    }
}
