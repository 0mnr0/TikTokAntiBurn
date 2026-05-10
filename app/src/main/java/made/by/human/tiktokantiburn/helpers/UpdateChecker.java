package made.by.human.tiktokantiburn.helpers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.work.ListenableWorker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import made.by.human.tiktokantiburn.settings.Settings;

public class UpdateChecker {
    public static class ParseResult {
        public boolean isSuccessParse = false;
        public boolean haveNewUpdate = false;
        public String ProjectURL = null;
        public String versionName = null;
        public boolean wasGitSuccess = false;
    }

    public static String ParseThisVersion(Context ctx) {
        try {
            PackageManager pm = ctx.getApplicationContext().getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(ctx.getApplicationContext().getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public interface UpdateCallback {
        void onResult(ParseResult result);
    }

    public static void runAsync(Context ctx, UpdateCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            ParseResult result = run(ctx);
            mainHandler.post(() -> callback.onResult(result));
        });
    }




    public static ParseResult run(Context ctx) {
        ParseResult result = new ParseResult();
        Log.d("updateStatusRUN:", "PRE: "+ParseThisVersion(ctx));
        try {
            URL url = new URL("https://raw.githubusercontent.com/0mnr0/TikTokAntiBurn/refs/heads/master/app/sampledata/lastversion.inf");

            Log.d("updateStatusRUN:", "GitHub ... ?");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            Log.d("updateStatusRUN:", "GitHub ... 2?");

            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            Log.d("updateStatusRUN:", "GitHub ... 3?");

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            Log.d("updateStatusRUN:", "GitHub -> Sucsessful connection");
            String versionInfo = sb.toString().trim();
            result.isSuccessParse = true;
            result.wasGitSuccess = true;

            result.ProjectURL = "https://github.com/0mnr0/TikTokAntiBurn/releases";
            if (versionInfo.equals(ParseThisVersion(ctx))) {
                return result;
            }
            result.versionName = versionInfo;
            result.haveNewUpdate = true;

            return result;
        } catch (Exception e) {
            Log.d("updateStatusRUN:", "GitHub -> ! Failed connection !"+e);
            result = tryGitVerse(ctx);
            return result;
        }
    }



    public static ParseResult tryGitVerse(Context ctx) {
        ParseResult result = new ParseResult();
        if (!Settings.Iternal.getBool(ctx, "GitVerseAPI", false)) {
            return result;
        }


        try {
            URL url = new URL("https://gitverse.ru/api/repos/dsvl/TikTokAntiBurn/raw/branch/master/app%2Fsampledata%2Flastversion.inf");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            Log.d("updateStatusRUN:", "GitVerse -> Successful connection");
            String versionInfo = sb.toString().trim();
            result.isSuccessParse = true;
            result.wasGitSuccess = false;
            result.ProjectURL = "https://gitverse.ru/dsvl/TikTokAntiBurn/releases";
            if (versionInfo.equals(ParseThisVersion(ctx))) {
                return result;
            }
            result.versionName = versionInfo;
            result.haveNewUpdate = true;

            return result;
        } catch (Exception e) {
            Log.d("updateStatusRUN:", "GitVerse -> Failed connection: "+e);
            result.isSuccessParse = false;
            result.wasGitSuccess = false;
            result.ProjectURL = null;
            result.haveNewUpdate = false;
            return result;
        }
    }
}
