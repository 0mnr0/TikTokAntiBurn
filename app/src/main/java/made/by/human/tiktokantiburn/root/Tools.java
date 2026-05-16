package made.by.human.tiktokantiburn.root;

import android.content.Context;

import made.by.human.tiktokantiburn.settings.Settings;

public class Tools {
    public static void forceStopTikTok(Context ctx) throws Exception {
        String packageName = Settings.Service.getString(ctx, "TriggerPacketName", "com.zhiliaoapp.musically");


        Process process = Runtime.getRuntime().exec(new String[]{
                "su", "-c", "am force-stop " + packageName
        });
        process.waitFor();
    }
}
