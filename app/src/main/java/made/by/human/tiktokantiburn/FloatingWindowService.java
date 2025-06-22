package made.by.human.tiktokantiburn;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FloatingWindowService extends Service {
    final int AnimationLength = 250;
    final int HiddenActionLength = 5000;
    private WindowManager windowManager;
    private LayoutInflater inflater;
    private final List<View> floatingViews = new ArrayList<>();



    public void CreateElement(int x, int y, int width, int height, boolean rounded, boolean canBeHidden) {
        View floatingView = inflater.inflate(rounded ? R.layout.blockburn : R.layout.blockburn_quad, null);


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = x;
        params.y = y;
        params.width = width;
        params.height = height;

        floatingView.setAlpha(0f);
        floatingView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        windowManager.addView(floatingView, params);
        floatingViews.add(floatingView);
        floatingView.animate().alpha(1f).setDuration(AnimationLength).start();

        if (canBeHidden) {
            floatingView.setOnClickListener(v -> {
                floatingView.animate().alpha(0f).setDuration(AnimationLength).start();
                new Handler().postDelayed(() ->
                                floatingView.setVisibility(View.GONE),
                AnimationLength);

                new Handler().postDelayed(() -> {
                    if (floatingView != null && floatingView.getParent() != null) {
                        floatingView.setVisibility(View.VISIBLE);
                        floatingView.animate().alpha(1f).setDuration(AnimationLength).start();
                    }
                }, HiddenActionLength);
            });
        }
    }


    public void DestroyAll(){
        for (View view : floatingViews) {
            if (view != null) {
                view.animate().alpha(0f).setDuration(AnimationLength).start();
                new Handler().postDelayed(() -> windowManager.removeView(view), AnimationLength);
            }
        }
        floatingViews.clear();
    }

    public boolean GetBoolean(String settingName) {
        SharedPreferences prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        return prefs.getBoolean(settingName, false);
    }


    public void LoadCustomBurns(boolean canBeHidden) {
        SharedPreferences prefs = getSharedPreferences("blockPos", MODE_PRIVATE);
        String json = prefs.getString("block_list", null);

        Gson gson = new Gson();
        Type type = new TypeToken<List<BlockInfo>>(){}.getType();
        List<BlockInfo> blockList = gson.fromJson(json, type);

        if (blockList == null || blockList.isEmpty()) {
            return;
        }
        for (int i = 0; i < blockList.size(); i++) {
            BlockInfo blockInfo = blockList.get(i);
            CreateElement(blockInfo.x, blockInfo.y, blockInfo.width, blockInfo.height, false, canBeHidden);
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        inflater = LayoutInflater.from(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean isClosed = intent != null && "ACTION_CLOSE_WINDOW".equals(intent.getAction());
        if (isClosed) { onDestroy(); return START_NOT_STICKY; }
        final boolean canBeHidden = GetBoolean("Clickable");

        if (!GetBoolean("DisableMainFloatingWindow")) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            final int elementWidth = (size.x) / 5 - 60;
            SharedPreferences sharedPreferences = getSharedPreferences("SeekBarPrefs", MODE_PRIVATE);
            int savedValue = sharedPreferences.getInt("seekBarValue", 40);

            CreateElement((size.x / 2) - (elementWidth / 2), size.y - savedValue, elementWidth, savedValue, true, canBeHidden);
        }
        LoadCustomBurns(canBeHidden);




        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        DestroyAll();
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
