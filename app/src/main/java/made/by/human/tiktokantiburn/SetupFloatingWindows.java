package made.by.human.tiktokantiburn;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SetupFloatingWindows extends Service {
    boolean dragging = false;

    ConstraintLayout blockBurnSettings;
    final int bgColor = Color.parseColor("#272727");

    View lastBlockBurnElement = null;
    WindowManager windowManager;
    View floatingMenu;
    List<View> blockburnList = new ArrayList<>();
    List<Long> blockburnRadiusesList = new ArrayList<>();
    List<Point> savedPositions = new ArrayList<>();

    TextView elementWidth, elementHeight, elementRadius;
    SeekBar widthBar, heightBar, radiusBar;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    public void RemoveElement() {
        if (lastBlockBurnElement != null) {
            windowManager.removeView(lastBlockBurnElement);
            blockburnRadiusesList.remove(blockburnList.indexOf(lastBlockBurnElement));
            blockburnList.remove(lastBlockBurnElement);
            lastBlockBurnElement = null;
            CloseBurnSettings();
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Context themedContext = new ContextThemeWrapper(getApplicationContext(), R.style.Theme_TikTokAntiBurn);
        LayoutInflater inflater = LayoutInflater.from(themedContext);
        floatingMenu = inflater.inflate(R.layout.floating_menu, null);


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
        elementWidth = floatingMenu.findViewById(R.id.elementWidth);
        elementHeight = floatingMenu.findViewById(R.id.elementHeight);
        elementRadius = floatingMenu.findViewById(R.id.elementRadius);
        widthBar = floatingMenu.findViewById(R.id.widthBar);
        heightBar = floatingMenu.findViewById(R.id.heightBar);
        radiusBar = floatingMenu.findViewById(R.id.radiusBar);
        Button RemoveElement = floatingMenu.findViewById(R.id.removeElement);
        Button btnSave = floatingMenu.findViewById(R.id.btnSave);
        RemoveElement.setOnClickListener(v -> RemoveElement());
        btnSave.setOnClickListener(v -> SaveSettings());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        widthBar.setMin(70);
        widthBar.setMax(width+(width/4));
        heightBar.setMin(70);
        heightBar.setMax(height/2);
        radiusBar.setMax(Math.max(widthBar.getProgress(), heightBar.getProgress()) /2);
        widthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) lastBlockBurnElement.getLayoutParams();
                layoutParams.width = i;
                windowManager.updateViewLayout(lastBlockBurnElement, layoutParams);
                RefreshSettings(false);
                radiusBar.setMax(Math.max(widthBar.getProgress(), heightBar.getProgress()) / 2);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        heightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) lastBlockBurnElement.getLayoutParams();
                layoutParams.height = i;
                windowManager.updateViewLayout(lastBlockBurnElement, layoutParams);
                RefreshSettings(false);
                radiusBar.setMax(Math.max(widthBar.getProgress(), heightBar.getProgress()) / 2);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (dragging) {return;}
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setColor(bgColor);
                drawable.setStroke(1, Color.RED);
                drawable.setCornerRadius(radiusBar.getProgress());
                lastBlockBurnElement.setBackground(drawable);
                blockburnRadiusesList.set(blockburnList.indexOf(lastBlockBurnElement), (long) radiusBar.getProgress());
                RefreshSettings(false);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });


        //Add on click event
        constraintLayout.setOnTouchListener((v, event) -> {
            int[] location = new int[2];
            blockBurnSettings.getLocationOnScreen(location);
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            int left = location[0];
            int top = location[1];
            int right = left + blockBurnSettings.getWidth();
            int bottom = top + blockBurnSettings.getHeight();

            return x < left || x > right || y < top || y > bottom;
        });

        LoadSettings();
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
        blockburnRadiusesList.add(0L);
        CloseBurnSettings();
    }




    @SuppressLint("SetTextI18n")
    public void RefreshSettings(Boolean force){
        int BurnWidth = lastBlockBurnElement.getWidth();
        int BurnHeight = lastBlockBurnElement.getHeight();
        if (force) {
            widthBar.setProgress(BurnWidth);
            heightBar.setProgress(BurnHeight);
            radiusBar.setProgress(Math.toIntExact(blockburnRadiusesList.get(blockburnList.indexOf(lastBlockBurnElement))));
        }

        elementWidth.setText(getString(R.string.ExtendedSetting_Width) + BurnWidth + " px");
        elementHeight.setText(getString(R.string.ExtendedSetting_Height) + BurnHeight + " px");
        elementRadius.setText(getString(R.string.borderRadiusSetting) + (blockburnRadiusesList.get(blockburnList.indexOf(lastBlockBurnElement))) + " px");
    }

    private void makeViewDraggable(View view, WindowManager.LayoutParams params) {
        view.setOnTouchListener(new View.OnTouchListener() {
            float initialTouchX, initialTouchY;
            int initialX, initialY;

            View draggingObject = null;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean NeedCloseSettings = (v != lastBlockBurnElement);
                if (v != lastBlockBurnElement && dragging) {
                    return true;
                }
                Log.d("blockburnRadiusesList", blockburnRadiusesList.toString());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (dragging) return true;
                        if (NeedCloseSettings) CloseBurnSettings();
                        lastBlockBurnElement = v;
                        dragging = true;
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        GradientDrawable drawable = new GradientDrawable();
                        drawable.setShape(GradientDrawable.RECTANGLE);
                        drawable.setColor(bgColor);
                        drawable.setCornerRadius(blockburnRadiusesList.get(blockburnList.indexOf(lastBlockBurnElement)));
                        drawable.setStroke(dpToPx(1), Color.RED);
                        lastBlockBurnElement.setBackground(drawable);
                        blockBurnSettings.setVisibility(View.VISIBLE);
                        draggingObject = v;
                        RefreshSettings(true);
                        windowManager.updateViewLayout(view, params);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (draggingObject != v) return true;
                        params.x = initialX + (int)(event.getRawX() - initialTouchX);
                        params.y = initialY + (int)(event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(view, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        dragging = false;
                        draggingObject = null;
                        return true;
                }
                return false;
            }
        });
    }

    private void CloseBurnSettings() {
        if (dragging) return;
        blockBurnSettings.setVisibility(View.GONE);

        for (View view : blockburnList) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(bgColor);
            drawable.setStroke(dpToPx(1), Color.TRANSPARENT);
            drawable.setCornerRadius(blockburnRadiusesList.get(blockburnList.indexOf(view)));
            view.setBackground(drawable);
        }
    }

    private void closeWindow() {
        savedPositions.clear();
        for (View view : blockburnList) {
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
            savedPositions.add(new Point(lp.x, lp.y));
            windowManager.removeView(view);
        }

        blockburnList.clear();
        blockburnRadiusesList.clear();
        windowManager.removeView(floatingMenu);

        stopSelf(); // Остановить сервис
    }

    private int dpToPx(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }


    public void SaveSettings(){
        List<BlockInfo> blockList = new ArrayList<>();
        for (View view : blockburnList) {
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
            blockList.add(new BlockInfo(lp.width, lp.height, lp.x, lp.y, blockburnRadiusesList.get(blockburnList.indexOf(view))));
        }

        SharedPreferences prefs = getSharedPreferences("blockPos", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(blockList);

        editor.putString("block_list", json);
        editor.apply();
        closeWindow();
    }


    public void LoadSettings(){
        SharedPreferences prefs = getSharedPreferences("blockPos", MODE_PRIVATE);
        String json = prefs.getString("block_list", null);

        Gson gson = new Gson();
        Type type = new TypeToken<List<BlockInfo>>(){}.getType();
        List<BlockInfo> blockList = gson.fromJson(json, type);

        if (blockList == null) {
            return;
        }
        for (BlockInfo blockInfo : blockList) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(Color.BLACK);
            drawable.setCornerRadius(blockInfo.radius);

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
            blockburn.setBackground(drawable);

            windowManager.addView(blockburn, params);
            blockburnList.add(blockburn);
            blockburnRadiusesList.add(blockInfo.radius);
            makeViewDraggable(blockburn, params);
        }
        CloseBurnSettings();

    }
}

