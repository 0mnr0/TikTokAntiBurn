package made.by.human.tiktokantiburn.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import made.by.human.tiktokantiburn.R;
import made.by.human.tiktokantiburn.settings.Settings;

public class FloatTestEnv {
    public interface OnLimitCalculatedListener {
        void onCalculated(int maxWindows);
    }

    public static int run(Context context, WindowManager windowManager) {
        int count = 0;
        List<View> testViews = new ArrayList<>();
        try {
            for (int i = 0; i < 40; i++) {
                View v = new View(context);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        10, 10, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                windowManager.addView(v, params);
                testViews.add(v);
                count++;
            }
        } catch (WindowManager.BadTokenException e) {
            Log.d("LimitTest", "max overlays: " + count);
        } finally {
            for (View v : testViews) {
                try {
                    windowManager.removeView(v);
                } catch (Exception ignored) {}
            }
        }
        return count;
    }

    public static void get(Context context, WindowManager windowManager, OnLimitCalculatedListener listener) {
        int max = Settings.Service.getInt(context, "MaxFloatWindows", 20);

        if (Settings.Service.contains(context, "MaxFloatWindows")) {
            listener.onCalculated(max);
            return;
        }

        Context themedContext = new ContextThemeWrapper(context, R.style.Theme_TikTokAntiBurn);

        AlertDialog dialog = new MaterialAlertDialogBuilder(themedContext)
                .setTitle(R.string.FloatingWindowsLimitTitle)
                .setMessage(R.string.FloatingWindowsLimitDesc)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }
        dialog.show();

        HandlerThread handlerThread = new HandlerThread("FloatWindowTestThread");
        handlerThread.start();
        Handler backgroundHandler = new Handler(handlerThread.getLooper());

        backgroundHandler.post(() -> {
            final int calculatedMax = run(context, windowManager);
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Settings.Service.setInt(context, "MaxFloatWindows", calculatedMax);
                } catch (Exception ignored) {}

                handlerThread.quitSafely();

                listener.onCalculated(calculatedMax);
            });
        });
    }
}