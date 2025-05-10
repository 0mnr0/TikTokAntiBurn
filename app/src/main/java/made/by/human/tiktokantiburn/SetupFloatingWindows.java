package made.by.human.tiktokantiburn;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

public class SetupFloatingWindows extends Service {

    Boolean dragging = false;

    ConstraintLayout blockBurnSettings;
    final int bgColor = Color.parseColor("#272727");

    View lastBlockBurnElement = null;
    WindowManager windowManager;
    View floatingMenu;
    List<View> blockburnList = new ArrayList<>();
    List<Point> savedPositions = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) { return null; }



    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Создаём главное окно с кнопками
        floatingMenu = LayoutInflater.from(this).inflate(R.layout.floating_menu, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        windowManager.addView(floatingMenu, params);
        blockBurnSettings = floatingMenu.findViewById(R.id.BlockBurnSettings);

        floatingMenu.findViewById(R.id.btnAdd).setOnClickListener(v -> addBlockburn());
        floatingMenu.findViewById(R.id.btnClose).setOnClickListener(v -> closeWindow());

        ConstraintLayout constraintLayout = floatingMenu.findViewById(R.id.linearLayout);

        //Add on click event
        constraintLayout.setOnClickListener(v -> CloseBurnSettings());
    }

    private void addBlockburn() {
        if (dragging) {return;}
        View blockburn = LayoutInflater.from(this).inflate(R.layout.blockburn, null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                dpToPx(100), dpToPx(100),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.x = 100;
        params.y = 100;
        params.gravity = Gravity.TOP | Gravity.START;
        blockburn.setBackgroundColor(bgColor);

        makeViewDraggable(blockburn, params);
        windowManager.addView(blockburn, params);
        blockburnList.add(blockburn);
        CloseBurnSettings();
    }

    private void makeViewDraggable(View view, WindowManager.LayoutParams params) {
        view.setOnTouchListener(new View.OnTouchListener() {
            float initialTouchX, initialTouchY;
            int initialX, initialY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dragging = true;
                lastBlockBurnElement = v;
                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(bgColor);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        drawable.setStroke(dpToPx(1), Color.RED);
                        v.setBackground(drawable);
                        blockBurnSettings.setVisibility(View.VISIBLE);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int)(event.getRawX() - initialTouchX);
                        params.y = initialY + (int)(event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(view, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        dragging = false;
                        return true;
                }
                return false;
            }
        });
    }

    private void CloseBurnSettings() {
        if (dragging) return;
        blockBurnSettings.setVisibility(View.GONE);
        if (lastBlockBurnElement == null) return;
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(bgColor);
        drawable.setStroke(0, Color.TRANSPARENT);
        lastBlockBurnElement.setBackground(drawable);
    }

    private void closeWindow() {
        savedPositions.clear();
        for (View view : blockburnList) {
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
            savedPositions.add(new Point(lp.x, lp.y));
            windowManager.removeView(view);
        }

        blockburnList.clear();
        windowManager.removeView(floatingMenu);

        stopSelf(); // Остановить сервис
    }

    private int dpToPx(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }
}

