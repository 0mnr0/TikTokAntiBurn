package made.by.human.tiktokantiburn;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.gson.Gson;


public class FloatingWindowService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private static final String PREFS_NAME = "SeekBarPrefs";
    private static final String PREF_VALUE = "seekBarValue";

    private ArrayList<View> blockburnList = new ArrayList<>();

    public void HideAndUnHide(View view) {
        try {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                view.setVisibility(View.GONE);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    view.setVisibility(View.VISIBLE);
                }, 5000 - 150);

            }, 150);
        } catch (Exception ignored) {}
    }

    public void LoadSettings(boolean canBeHidden) {
        SharedPreferences prefs = getSharedPreferences("blockPos", MODE_PRIVATE);
        String json = prefs.getString("block_list", null);

        Gson gson = new Gson();
        Type type = new TypeToken<List<BlockInfo>>(){}.getType();
        List<BlockInfo> blockList = gson.fromJson(json, type);

        if (blockList == null) {
            return;
        }
        for (BlockInfo blockInfo : blockList) {

            View blockburn = LayoutInflater.from(this).inflate(R.layout.blockburn, null);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    blockInfo.width, blockInfo.height,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT);
            params.x = blockInfo.x;
            params.y = blockInfo.y;
            params.gravity = Gravity.TOP | Gravity.START;
            blockburn.setBackgroundResource(R.drawable.block_drawable_quad);
            blockburn.setAlpha(0f);
            windowManager.addView(blockburn, params);
            blockburnList.add(blockburn);

            if (canBeHidden) {
                blockburn.setOnClickListener(v -> {
                    HideAndUnHide(blockburn);
                    blockburn.animate().alpha(0f).setDuration(100).start();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> blockburn.animate().alpha(1f).setDuration(100).start(), 5000);
                });
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                blockburn.animate()
                        .alpha(1f)
                        .setDuration(200) // или сколько нужно
                        .start();
            });
        }

    }


    public boolean GetBoolean(String settingName) {
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getBoolean(settingName, false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    public boolean GetClickableStatus(){
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getBoolean("Clickable", false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean isClosed = intent != null && "ACTION_CLOSE_WINDOW".equals(intent.getAction());
        final boolean canBeHidden = GetClickableStatus();

        if (isClosed) {
            try {
                floatingView.animate() .alpha(0f) .setDuration(200) .start();
                if (blockburnList != null) {
                    for (View view : blockburnList) { view.animate().alpha(0f).setDuration(200).start(); }
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try{
                        if (floatingView != null) {
                             windowManager.removeView(floatingView);
                            floatingView = null;
                        }
                        if (blockburnList != null) {
                            for (View view : blockburnList) {
                                windowManager.removeView(view);
                            }
                            blockburnList.clear();
                        }
                    } catch (Exception ignored) {}
                    stopSelf();

                }, 300);


                return START_NOT_STICKY;
            } catch (Exception ignored) {}
            return START_NOT_STICKY;
        }

        if (floatingView != null) {
            return START_STICKY;
        }

        floatingView = LayoutInflater.from(this).inflate(R.layout.blockburn, null);

        WindowManager.LayoutParams params;
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedValue = sharedPreferences.getInt(PREF_VALUE, 40); // 40 - значение по умолчанию


        params.width = screenWidth / 5 - 20;
        params.height = savedValue;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        if (canBeHidden) {
            floatingView.setOnClickListener(v -> {
                HideAndUnHide(v);
                floatingView.animate().alpha(0f).setDuration(100).start();
                new Handler(Looper.getMainLooper()).postDelayed(() -> floatingView.animate().alpha(1f).setDuration(100).start(), 5000);
            });
        }

        try {
            LoadSettings(canBeHidden);

            if (!GetBoolean("DisableMainFloatingWindow")) {
                floatingView.setAlpha(0f);
                windowManager.addView(floatingView, params);
                new Handler(Looper.getMainLooper()).post(() -> {
                    floatingView.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .start();
                });
            }


        } catch (Exception e) {
            Log.w("Exception catched:", e);
            return Service.START_STICKY;
        }



        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            try{ windowManager.removeView(floatingView); } catch (Exception ignored) {}
            floatingView = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
