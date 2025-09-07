package made.by.human.tiktokantiburn;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class RootCheck {

    public static boolean isDeviceRooted() {
        return checkSuExists() || checkSuInPaths();
    }

    // Попытка выполнить команду "su"
    private static boolean checkSuExists() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = in.readLine();
            return result != null; // если нашли путь к su → root доступен
        } catch (Exception e) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    // Проверка стандартных путей, где обычно лежит su
    private static boolean checkSuInPaths() {
        String[] paths = {
                "/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/",
                "/data/local/"
        };
        for (String path : paths) {
            File suFile = new File(path + "su");
            if (suFile.exists()) {
                return true;
            }
        }
        return false;
    }
}

