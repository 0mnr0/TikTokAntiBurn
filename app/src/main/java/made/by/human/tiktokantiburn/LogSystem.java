package made.by.human.tiktokantiburn;

import java.io.File;
import java.io.IOException;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.FileWriter;
import java.util.Date;

import android.app.Application;

public class LogSystem {
    public static final String LoggerVersion = "1.4.0";
    private static final long MAX_FILE_SIZE = 15L * 1024 * 1024; // 15 MB
    private static final String LOG_FILE_NAME = "logs.txt";
    private static LogSystem instance;
    private final File logFile;
    private final Handler backgroundHandler;
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
        String content = "(" + LoggerVersion + ") [" + key + "] - " + data.toString() + "\n";
        Date date = new Date();
        content =  date.toLocaleString() + "  " + content;
        if (DivideTop) {content = "\n" + content;}
        if (DivideBottom) {content = content + "\n";}

        String finalContent = content;
        SaveOperation(finalContent);

    }

    private void SaveOperation(String text){
        backgroundHandler.post(() -> {
            try {
                manageFileSize(); // Проверяем размер до записи
                FileWriter writer = new FileWriter(logFile, true);
                writer.write(text);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void Append(String content) {
        SaveOperation(content);
    }

    private void manageFileSize() {
        if (logFile.exists() && logFile.length() > MAX_FILE_SIZE) {
            logFile.delete();
        }
    }
    public void clear() {
        if (logFile.exists()) { logFile.delete();}
    }
}
