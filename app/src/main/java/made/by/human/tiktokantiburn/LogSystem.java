package made.by.human.tiktokantiburn;

import java.io.File;
import java.io.IOException;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.FileWriter;
import android.app.Application;
import android.util.Log;

public class LogSystem {
    public static final String LoggerVersion = "1.2.1";
    private static final long MAX_FILE_SIZE = 35L * 1024 * 1024; // 35 MB
    private static final String LOG_FILE_NAME = "logs.txt";
    private static LogSystem instance;
    private final File logFile;
    private final Handler backgroundHandler;

    // Получаем доступ к внутреннему хранилищу приложения через Application
    public static LogSystem getInstanceOrNull() {
        return instance;
    }


    LogSystem(Application app) {
        logFile = new File(app.getFilesDir(), LOG_FILE_NAME);

        HandlerThread thread = new HandlerThread("LogWriterThread");
        thread.start();
        backgroundHandler = new Handler(thread.getLooper());
    }

    public static void init(Application app) {
        if (instance == null) {
            instance = new LogSystem(app);
        }
    }

    public static LogSystem getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LogSystem is not initialized. Call LogSystem.init(application) first.");
        }
        return instance;
    }

    public void Save(String key, Object data, boolean DivideTop, boolean DivideBottom) {
        String content = "[(" + LoggerVersion + ") " + key + "] - " + data.toString() + "\n";
        if (DivideTop) {content = "\n" + content;}
        if (DivideBottom) {content = content + "\n";}

        String finalContent = content;
        backgroundHandler.post(() -> {
            try {
                manageFileSize(); // Проверяем размер до записи
                FileWriter writer = new FileWriter(logFile, true);
                writer.write(finalContent);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void manageFileSize() {
        if (logFile.exists() && logFile.length() > MAX_FILE_SIZE) {
            logFile.delete();
        }
    }
}
