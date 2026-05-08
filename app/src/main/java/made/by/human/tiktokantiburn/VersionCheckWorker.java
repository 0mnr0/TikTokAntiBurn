package made.by.human.tiktokantiburn;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class VersionCheckWorker extends Worker {
    public VersionCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public String ParseThisVersion() {
        try {
            PackageManager pm = this.getApplicationContext().getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(this.getApplicationContext().getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }

    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/0mnr0/TikTokAntiBurn/refs/heads/master/app/sampledata/lastversion.inf");
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

            String versionInfo = sb.toString().trim();
            if (versionInfo.equals(ParseThisVersion())) {
                return Result.success();
            }

            sendNotification("We found an update", "A new version of the app has been released: " + versionInfo);
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    private void sendNotification(String title, String text) {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/0mnr0/TikTokAntiBurn/"));
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this.getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        Context context = getApplicationContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "burn_version_check_channel";

        NotificationChannel channel = new NotificationChannel(channelId, "Burn Version Check", NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.update)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        manager.notify(1, builder.build());
    }
}
