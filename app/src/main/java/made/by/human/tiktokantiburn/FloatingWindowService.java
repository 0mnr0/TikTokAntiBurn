package made.by.human.tiktokantiburn;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


public class FloatingWindowService extends Service {

    private WindowManager windowManager;
    private View floatingView;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "ACTION_CLOSE_WINDOW".equals(intent.getAction())) {
            try {
                if (floatingView != null) {
                    windowManager.removeView(floatingView);
                    floatingView = null;
                }
                stopSelf();
                return START_NOT_STICKY;
            } catch (Exception ignored) {}
        }

        if (floatingView != null) {
            return START_STICKY;
        }

        floatingView = LayoutInflater.from(this).inflate(R.layout.blockburn, null);

        WindowManager.LayoutParams params = null;
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // Получаем ширину экрана и настраиваем параметры окна
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;

        params.width = screenWidth / 5 - 20;
        params.height = 120;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER;


        windowManager.addView(floatingView, params);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);

        new Handler(Looper.getMainLooper()).post(() -> floatingView.startAnimation(fadeInAnimation));



        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
            floatingView = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
