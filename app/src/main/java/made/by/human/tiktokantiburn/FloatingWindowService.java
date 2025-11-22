package made.by.human.tiktokantiburn;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.IBinder;
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
    private boolean WindowsOpened = false;



    @SuppressLint("InflateParams")
    public void CreateElement(int x, int y, int width, int height, long radius, boolean canBeHidden) {
        View floatingView = inflater.inflate(R.layout.blockburn_quad, null);

        boolean useFullScreenAPI =  GetBoolean("FullScreenAPI");

        int displayMode = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (useFullScreenAPI) {
            displayMode = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
        }



        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                displayMode,
                PixelFormat.TRANSLUCENT
        );
        if (useFullScreenAPI) { params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES; }


        params.gravity = Gravity.TOP | Gravity.START;
        params.x = x;
        params.y = y;
        params.width = width;
        params.height = height;

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.BLACK);
        drawable.setCornerRadius(radius);
        floatingView.setBackground(drawable);


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
            CreateElement(blockInfo.x, blockInfo.y, blockInfo.width, blockInfo.height, blockInfo.radius, canBeHidden);
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
        boolean CloseAll = intent != null && "ACTION_CLOSE_WINDOW".equals(intent.getAction()); // Detecting if popups must be closed
        if (CloseAll) { onDestroy(); WindowsOpened = false; return START_NOT_STICKY; } // Remove all popups and set "Multi-Open" defend to non active
        if (WindowsOpened) {return START_NOT_STICKY;} else {WindowsOpened = true;} // Some systems can call event more than one time, its defend to prevent "multi" popups on same places
        final boolean canBeHidden = GetBoolean("Clickable");

        if (!GetBoolean("DisableMainFloatingWindow")) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            final int elementWidth = (size.x) / 5 - 30;
            SharedPreferences sharedPreferences = getSharedPreferences("SeekBarPrefs", MODE_PRIVATE);
            int savedValue = sharedPreferences.getInt("seekBarValue", 40);

            CreateElement((size.x / 2) - (elementWidth / 2), size.y - savedValue, elementWidth, savedValue, 0, canBeHidden);
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
