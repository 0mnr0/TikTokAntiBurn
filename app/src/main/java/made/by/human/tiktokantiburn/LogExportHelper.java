package made.by.human.tiktokantiburn;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LogExportHelper {

    public static void exportLogs(Context context) {
        File logFile = new File(context.getFilesDir(), "logs.txt");

        if (!logFile.exists()) {
            Toast.makeText(context, "Log file does not exist", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29) и выше — Scoped Storage
            exportToScopedStorage(context, logFile);
        } else {
            // Android 8.0 - 9.0 (API 26-28) — доступ к внешнему хранилищу
            exportToExternalStorage(context, logFile);
        }
    }

    private static void exportToScopedStorage(Context context, File logFile) {
        // Android 10 (API 29) и выше — запись через MediaStore
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "logs.txt");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        }

        if (uri != null) {
            try (InputStream inputStream = new FileInputStream(logFile);
                 OutputStream outputStream = contentResolver.openOutputStream(uri)) {

                if (outputStream != null) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    Toast.makeText(context, "Logs are exported to Downloads folder", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Failed to export logs", Toast.LENGTH_SHORT).show();
            }
        } else {
            exportToExternalStorage(context, logFile);
        }
    }

    private static void exportToExternalStorage(Context context, File logFile) {
        // Android 8.0 - 9.0 (API 26 - 28) — запись в внешний каталог Downloads
        File externalStorage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "logs.txt");

        try (InputStream inputStream = new FileInputStream(logFile);
             OutputStream outputStream = new FileOutputStream(externalStorage)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            Toast.makeText(context, "Logs are exported to Downloads folder", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to export logs", Toast.LENGTH_SHORT).show();
        }
    }
}
