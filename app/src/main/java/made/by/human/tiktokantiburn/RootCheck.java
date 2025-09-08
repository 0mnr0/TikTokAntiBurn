package made.by.human.tiktokantiburn;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;

public class RootCheck {

    public static boolean isDeviceRooted() {
        return checkSuExists() || checkSuInPaths();
    }

    // Попытка выполнить команду "su"
    private static boolean checkSuExists() {
        Process process = null;
        DataOutputStream os = null;
        try {
            // Запускаем su
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());

            os.writeBytes("id\n");
            os.writeBytes("exit\n");
            os.flush();

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false; // su не сработал
        } finally {
            if (os != null) {
                try { os.close(); } catch (Exception ignored) {}
            }
            if (process != null) {
                process.destroy();
            }
        }

    }

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

