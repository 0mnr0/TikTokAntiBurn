package made.by.human.tiktokantiburn.helpers;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.pm.PackageManager;

import de.robv.android.xposed.XposedBridge;

public class ResourceHelper {

    private static final String MODULE_PACKAGE = "made.by.human.tiktokantiburn"; // замените на свой пакет

    public static String getString(Object id) {
        try {
            Context appContext = AndroidAppHelper.currentApplication();
            if (appContext == null) {
                XposedBridge.log("ResourceHelper: currentApplication() вернул null");
                return null;
            }

            Context moduleContext = null;
            try {
                moduleContext = appContext.createPackageContext(MODULE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY);
            } catch (PackageManager.NameNotFoundException e) {
                XposedBridge.log("ResourceHelper: createPackageContext failed: " + e);
                return null;
            }

            if (id instanceof Integer) {
                // id — это int ресурсный идентификатор
                return moduleContext.getString((Integer) id);
            } else if (id instanceof String) {
                // id — это имя ресурса, например "app_name"
                String resName = (String) id;
                int resId = moduleContext.getResources().getIdentifier(resName, "string", MODULE_PACKAGE);
                if (resId == 0) {
                    XposedBridge.log("ResourceHelper: resource string '" + resName + "' не найден");
                    return null;
                }
                return moduleContext.getString(resId);
            } else {
                XposedBridge.log("ResourceHelper: неизвестный тип id: " + id.getClass().getName());
                return null;
            }
        } catch (Throwable t) {
            XposedBridge.log("ResourceHelper: Ошибка при получении строки: " + t);
            return null;
        }
    }
}

