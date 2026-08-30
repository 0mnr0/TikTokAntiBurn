package made.by.human.tiktokantiburn;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import made.by.human.tiktokantiburn.settings.Settings;
import made.by.human.tiktokantiburn.utils.FloatTestEnv;

public class SetupFloatingWindows extends Service {
    final String PrefsFileName = "BlockData";
    boolean dragging = false;

    boolean serviceStarted = false;
    ConstraintLayout blockBurnSettings;
    final int bgColor = Color.parseColor("#272727");
    int maxWindows = 20;

    View lastBlockBurnElement = null;
    WindowManager windowManager;
    View floatingMenu;
    List<View> blockburnList = new ArrayList<>();
    List<Long> blockburnRadiusesList = new ArrayList<>();
    List<Point> savedPositions = new ArrayList<>();

    TextView elementWidth, elementHeight, elementRadius, elementAlpha;
    SeekBar widthBar, heightBar, radiusBar, alphaBar;
    Button RemoveElementBtn, btnSave, btnAdd;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @SuppressLint("SetTextI18n")
    public void RemoveElement() {
        if (lastBlockBurnElement != null) {
            lastBlockBurnElement.setOnTouchListener(null);
            windowManager.removeView(lastBlockBurnElement);
            blockburnRadiusesList.remove(blockburnList.indexOf(lastBlockBurnElement));
            blockburnList.remove(lastBlockBurnElement);
            lastBlockBurnElement = null;
            CloseBurnSettings();
            btnAdd.setText(
                    getString(R.string.ExtendedSetting_Create, blockburnList.size(), maxWindows)
            );
        }
    }


    @SuppressLint({"SetWorldReadable", "ApplySharedPref"})
    public void SaveSettings(String settingName, Object value) {
        if (value instanceof String) {
            Settings.Service.setString(this, settingName, (String) value);
        } else if (value instanceof Integer) {
            Settings.Service.setInt(this, settingName, (int) value);
        } else if (value instanceof Boolean) {
            Settings.Service.setBool(this, settingName, (boolean) value);
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        SaveSettings("isSetupping", true);
        FloatTestEnv.get(this, windowManager, maxWindowCount -> {
            maxWindows = maxWindowCount;
            runService();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void runService() {
        if (serviceStarted) {
            return;
        }
        serviceStarted = true;
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

        try {
            windowManager.addView(floatingMenu, params);
        } catch (WindowManager.BadTokenException e) {
            Log.e("AntiBurn", "Не удалось добавить floatingMenu: " + e.getMessage());
            stopSelf();
            return;
        }
        blockBurnSettings = floatingMenu.findViewById(R.id.BlockBurnSettings);

        btnAdd = floatingMenu.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> addBlockburn());
        btnAdd.setText(
                getString(R.string.ExtendedSetting_Create, blockburnList.size(), maxWindows)
        );



        floatingMenu.findViewById(R.id.btnClose).setOnClickListener(v -> closeWindow());

        ConstraintLayout constraintLayout = floatingMenu.findViewById(R.id.linearLayout);
        elementWidth = floatingMenu.findViewById(R.id.elementWidth);
        elementHeight = floatingMenu.findViewById(R.id.elementHeight);
        elementRadius = floatingMenu.findViewById(R.id.elementRadius);
        elementAlpha = floatingMenu.findViewById(R.id.alphaTextView);
        widthBar = floatingMenu.findViewById(R.id.widthBar);
        heightBar = floatingMenu.findViewById(R.id.heightBar);
        radiusBar = floatingMenu.findViewById(R.id.radiusBar);
        alphaBar = floatingMenu.findViewById(R.id.alphaBar);
        RemoveElementBtn = floatingMenu.findViewById(R.id.removeElement);
        btnSave = floatingMenu.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> SaveSettings());
        ImageView launchTikTok = floatingMenu.findViewById(R.id.launchTikTok);
        launchTikTok.setOnClickListener(v -> launchTikTok());
        RemoveElementBtn.setOnClickListener(v -> RemoveElement());


        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        widthBar.setMin(70);
        widthBar.setMax(width+(width/4));
        heightBar.setMin(70);
        heightBar.setMax(height/2);
        radiusBar.setMax(Math.min(widthBar.getProgress(), heightBar.getProgress()) /2);
        alphaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                lastBlockBurnElement.setAlpha(alphaBar.getProgress() / 100f);
                RefreshSettings(false);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        widthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) lastBlockBurnElement.getLayoutParams();
                layoutParams.width = i;
                windowManager.updateViewLayout(lastBlockBurnElement, layoutParams);
                RefreshSettings(false);
                radiusBar.setMax(Math.min(widthBar.getProgress(), heightBar.getProgress()) / 2);
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
                radiusBar.setMax(Math.min(widthBar.getProgress(), heightBar.getProgress()) / 2);
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

    private long getRadiusFor(View view) {
        int idx = blockburnList.indexOf(view);
        return idx >= 0 ? blockburnRadiusesList.get(idx) : 0L;
    }

    @SuppressLint("SetTextI18n")
    private void addBlockburn() {
        if (dragging) {return;}
        Log.d("blockburnList:", blockburnList.size() + "|" + maxWindows);
        if (blockburnList.size() >= maxWindows) {
            Toast.makeText(this, R.string.FloatingWindowsLimitError, Toast.LENGTH_LONG).show();
            return;
        }
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
        try {
            windowManager.addView(blockburn, params);
        } catch (WindowManager.BadTokenException e) {
            Log.e("AntiBurn", "Не удалось добавить плашку: " + e.getMessage());
            Toast.makeText(this, R.string.FloatingWindowsLimitError, Toast.LENGTH_LONG).show();
            return;
        }
        blockburnList.add(blockburn);
        blockburnRadiusesList.add(0L);
        CloseBurnSettings();
        btnAdd.setText(
                getString(R.string.ExtendedSetting_Create, blockburnList.size(), maxWindows)
        );
    }




    @SuppressLint("SetTextI18n")
    public void RefreshSettings(Boolean force){
        int BurnWidth = lastBlockBurnElement.getWidth();
        int BurnHeight = lastBlockBurnElement.getHeight();
        if (force) {
            widthBar.setProgress(BurnWidth);
            heightBar.setProgress(BurnHeight);
            radiusBar.setProgress(Math.toIntExact(getRadiusFor(lastBlockBurnElement)));
            alphaBar.setProgress((int) (lastBlockBurnElement.getAlpha() * 100f));
        }

        elementWidth.setText(getString(R.string.ExtendedSetting_Width) + BurnWidth + " px");
        elementHeight.setText(getString(R.string.ExtendedSetting_Height) + BurnHeight + " px");
        elementRadius.setText(getString(R.string.borderRadiusSetting) + (getRadiusFor(lastBlockBurnElement)) + " px");
        elementAlpha.setText(getString(R.string.Alpha) + ((int) (lastBlockBurnElement.getAlpha() * 100)) + "%");
    }

    private void makeViewDraggable(View view, WindowManager.LayoutParams params) {
        view.setOnTouchListener(new View.OnTouchListener() {
            float initialTouchX, initialTouchY;
            int initialX, initialY;

            View draggingObject = null;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!blockburnList.contains(v)) {
                    return false;
                }

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
                        drawable.setCornerRadius(getRadiusFor(lastBlockBurnElement));
                        drawable.setStroke(
                                dpToPx(1), Color.RED
                        );
                        lastBlockBurnElement.setBackground(drawable);
                        blockBurnSettings.setVisibility(View.VISIBLE);
                        RemoveElementBtn.setVisibility(View.VISIBLE);
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
        RemoveElementBtn.setVisibility(View.GONE);

        for (View view : blockburnList) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(bgColor);
            drawable.setStroke(dpToPx(1), Color.TRANSPARENT);
            drawable.setCornerRadius(blockburnRadiusesList.get(blockburnList.indexOf(view)));
            view.setBackground(drawable);
        }
        btnAdd.setText(
                getString(R.string.ExtendedSetting_Create, blockburnList.size(), maxWindows)
        );
    }

    private void closeWindow() {
        savedPositions.clear();
        for (View view : blockburnList) {
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
            savedPositions.add(new Point(lp.x, lp.y));
        }

        clearAllViews();
        Settings.Iternal.setBool(this, "viewsSetup", false);

        stopSelf();
    }

    private int dpToPx(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }

    public String GetString(String settingName, String defaultValue) {
        return Settings.Service.getString(this, settingName, defaultValue);
    }

    public void SaveSettings(){
        List<BlockInfo> blockList = new ArrayList<>();
        for (View view : blockburnList) {
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
            blockList.add(
                    new BlockInfo(
                            lp.width,
                            lp.height,
                            lp.x,
                            lp.y,
                            view.getAlpha(),
                            blockburnRadiusesList.get(blockburnList.indexOf(view))
                    )
            );
        }

        String json = new Gson().toJson(blockList);
        Settings.Service.setString(this, "block_list", json);
        closeWindow();
    }

    public void launchTikTok() {
        String pkg = Settings.Service.getString(
                this,
                "TriggerPacketName",
                "com.zhiliaoapp.musically"
        );

        PackageManager pm = getApplicationContext().getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(pkg);

        if (launchIntent != null) {
            Settings.Iternal.setBool(getApplicationContext(), "viewsSetup", true);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchIntent);
        } else {
            Toast.makeText(this, getString(R.string.TargetPackageAppNotFound), Toast.LENGTH_LONG).show();
        }
    }

    public boolean GetBoolean(String settingName) {
        return Settings.Service.getBool(this, settingName, false);
    }

    public void LoadSettings(){
        String json = Settings.Service.getString(this, "block_list", null);

        Gson gson = new Gson();
        Type type = new TypeToken<List<BlockInfo>>(){}.getType();
        List<BlockInfo> blockList = gson.fromJson(json, type);

        if (blockList == null) {
            return;
        }

        int windowCount = 0;
        for (BlockInfo blockInfo : blockList) {
            if (windowCount >= maxWindows) {
                break;
            }

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setColor(Color.BLACK);
            drawable.setCornerRadius(blockInfo.radius);

            View blockburn = LayoutInflater.from(this).inflate(R.layout.blockburn, null);


            boolean useFullScreenAPI =  GetBoolean("FullScreenAPISwitch");
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
                    blockInfo.width, blockInfo.height,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    displayMode,
                    PixelFormat.TRANSLUCENT);

            if (useFullScreenAPI && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
            params.x = blockInfo.x;
            params.y = blockInfo.y;
            params.gravity = Gravity.TOP | Gravity.START;
            blockburn.setBackground(drawable);

            try {
                windowManager.addView(blockburn, params);
            } catch (WindowManager.BadTokenException e) {
                Log.e("AntiBurn", "Не удалось добавить плашку: " + e.getMessage());
                break;
            }
            blockburnList.add(blockburn);
            blockburnRadiusesList.add(blockInfo.radius);
            blockburn.setAlpha(
                    blockInfo.alpha
            );
            makeViewDraggable(blockburn, params);
            windowCount++;
        }
        CloseBurnSettings();
    }

    private void clearAllViews() {
        if (windowManager == null) return;

        for (View view : new ArrayList<>(blockburnList)) {
            try {
                if (view != null) {
                    windowManager.removeView(view);
                }
            } catch (Exception e) {
                Log.e("AntiBurn", "Ошибка удаления плашки: " + e.getMessage());
            }
        }
        blockburnList.clear();
        blockburnRadiusesList.clear();

        try {
            if (floatingMenu != null) {
                windowManager.removeView(floatingMenu);
            }
        } catch (Exception e) {
            Log.e("AntiBurn", "Ошибка удаления меню: " + e.getMessage());
        }
    }




    @Override
    public void onDestroy() {
        clearAllViews();
        SaveSettings("isSetupping", false);
        super.onDestroy();
    }

}