package made.by.human.tiktokantiburn.helpers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class ApplicationVersion {
    public static String get(Context ctx) {
        try {
            PackageManager pm = ctx.getApplicationContext().getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(ctx.getApplicationContext().getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}

