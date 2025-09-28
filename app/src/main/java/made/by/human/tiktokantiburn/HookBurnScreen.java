package made.by.human.tiktokantiburn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ObjectInputStream;
import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookBurnScreen implements IXposedHookLoadPackage {
    View possibleLinearLayout;
    ShakeManager shakeManager;

    private LinearLayout findRootLayout(View root) {
        if (root instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) root;
            if (layout.getChildCount() == 3) {
                boolean isValid = true;
                for (int i = 0; i < 3; i++) {
                    View child = layout.getChildAt(i);
                    if (!(child instanceof ViewGroup) ||
                            !(getFirstDescendant(child) instanceof TextView)) {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) return layout;
            }
        }

        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                LinearLayout result = findRootLayout(group.getChildAt(i));
                if (result != null) return result;
            }
        }
        return null;
    }

    public boolean GetBoolean(Context context, String keyName, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences("LSPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean(keyName, defaultValue);
    }
    public int GetInt(Context context, String keyName, int def) {
        SharedPreferences prefs = context.getSharedPreferences("LSPrefs", Context.MODE_PRIVATE);
        return prefs.getInt(keyName, def);
    }

    private View getFirstDescendant(View view) {
        if (!(view instanceof ViewGroup) || ((ViewGroup) view).getChildCount() == 0) {
            return view;
        }
        return getFirstDescendant(((ViewGroup) view).getChildAt(0));
    }

    public static View findNthChild(View parent, int targetIndex) {
        if (!(parent instanceof ViewGroup)) return null;
        return findNthRecursive((ViewGroup) parent, targetIndex, new int[]{0});
    }

    private static View findNthRecursive(ViewGroup parent, int targetIndex, int[] counter) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            if (child instanceof ViewStub) {
                continue; // пропускаем
            }

            if (counter[0] == targetIndex) {
                return child;
            }
            counter[0]++;

            if (child instanceof ViewGroup) {
                View result = findNthRecursive((ViewGroup) child, targetIndex, counter);
                if (result != null) return result;
            }
        }
        return null;
    }




    public static FrameLayout findTikTokRootView(View root) {
        Context context = root.getContext();
        int resId = context.getResources().getIdentifier(
                "view_rootview", // имя ресурса
                "id",            // тип ресурса
                "com.zhiliaoapp.musically" // пакет
        );

        if (resId != 0) {
            View v = root.findViewById(resId);
            if (v instanceof FrameLayout) {
                return (FrameLayout) v;
            }
        }
        return null;
    }


    private View findViewByEnumeration(View root) {
        if (possibleLinearLayout != null) {
            return possibleLinearLayout;
        }

        if (root instanceof LinearLayout) {
            LinearLayout possibleLinearLayout = (LinearLayout) root;
            int possibleLinearChildren = possibleLinearLayout.getChildCount();

            if (possibleLinearChildren == 5) {
                int possibleWeight = 0;

                for (int i = 0; i < possibleLinearChildren; i++) {
                    View child = possibleLinearLayout.getChildAt(i);

                    if (i != 2 && child instanceof FrameLayout) {
                        possibleWeight += 1;
                    }

                    if (i == 2 && child instanceof Button) {
                        possibleWeight += 1;
                    }
                }

                if (possibleWeight >= 4) {
                    return possibleLinearLayout;
                }
            }
        }


        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                View result = findViewByEnumeration(child);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }



    private void SHOW_TOP_PANEL(View TopPane, float topPaneAlpha) {
        if (TopPane != null) {
            TopPane.animate().alpha(topPaneAlpha).setDuration(200).start();
        }
    }

    private void SHOW_BOTTOM_PANEL(View BottomPane, float bottomPaneAlpha) {
        if (BottomPane != null) {
            BottomPane.animate().alpha(bottomPaneAlpha).setDuration(200).start();
        }
    }

    private void HIDE_TOP_PANEL(View TopPane, float topPaneAlpha) {
        if (TopPane != null) {
            TopPane.animate().alpha(topPaneAlpha).setDuration(200).start();
        }
    }

    private void HIDE_BOTTOM_PANEL(View BottomPane, float bottomPaneAlpha) {
        if (BottomPane != null) {
            BottomPane.animate().alpha(bottomPaneAlpha).setDuration(200).start();
        }
    }


    private Handler handler;
    private Runnable hideRunnable;

    private void setupPanelHider(Activity activity, View topPanel, View bottomPane, float topPaneInactiveAlpha, float bottomPaneInactiveAlpha, boolean Shake2Show) {
        hideRunnable = () -> {
            HIDE_TOP_PANEL(topPanel, topPaneInactiveAlpha);
            HIDE_BOTTOM_PANEL(bottomPane, bottomPaneInactiveAlpha);

            handler.postDelayed(hideRunnable, 15000);
        };
        handler.postDelayed(hideRunnable, 1000);

        if (Shake2Show) {
            shakeManager = new ShakeManager(activity, () -> {
                showTemporarily(topPanel, bottomPane, topPaneInactiveAlpha, bottomPaneInactiveAlpha);
            });
            shakeManager.start();
        }

        if (bottomPane != null) {
            bottomPane.setOnClickListener(v -> {
                showTemporarily(topPanel, bottomPane, topPaneInactiveAlpha, bottomPaneInactiveAlpha);
            });
        }
    }

    private void showTemporarily(View topPanel, View bottomPane,
                                 float topPaneInactiveAlpha, float bottomPaneInactiveAlpha) {
        SHOW_TOP_PANEL(topPanel, 1.0f);
        SHOW_BOTTOM_PANEL(bottomPane, 1.0f);

        handler.removeCallbacks(hideRunnable);

        handler.postDelayed(() -> {
            HIDE_TOP_PANEL(topPanel, topPaneInactiveAlpha);
            HIDE_BOTTOM_PANEL(bottomPane, bottomPaneInactiveAlpha);

            handler.postDelayed(hideRunnable, 15000);
        }, 10000);
    }



    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.zhiliaoapp.musically"))
            return;

        XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.main.MainActivity", lpparam.classLoader, "onWindowFocusChanged", boolean.class, new XC_MethodHook() {
            @SuppressLint("ClickableViewAccessibility") // yes, this is bad, but this is to avoid BREAKING TIKTOK UI LOGIC
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                final Activity activity = (Activity) param.thisObject;
                final boolean AllowTopPaneModificator = GetBoolean(activity, "XPOSED:AllowTopPaneModifier", false);
                final boolean AllowBottomPaneModificator = GetBoolean(activity, "XPOSED:AllowBottomPaneModifier", false);
                final boolean Shake2Show = GetBoolean(activity, "XPOSED:Shake2Show", false);
                if (!AllowTopPaneModificator && !AllowBottomPaneModificator) {
                    if (shakeManager != null) {
                        try{ shakeManager.stop(); }
                        catch (Exception ignored) {}
                    }
                    return;
                }

                final float topPaneInactiveAlpha = ((float) GetInt(activity, "XPOSED:TopPaneOpacity", 100)) / 100;
                final float bottomPaneInactiveAlpha = ((float) GetInt(activity, "XPOSED:BottomPaneOpacity", 50)) / 100;
                handler = new Handler(Looper.getMainLooper());

                activity.runOnUiThread(() -> {
                    new android.os.Handler().postDelayed(() -> {


                        View root = activity.getWindow().getDecorView().getRootView();
                        View topPanel = AllowTopPaneModificator ? (View) findRootLayout(root).getParent().getParent().getParent() : null;
                        View bottomPane = AllowBottomPaneModificator ? findViewByEnumeration(root) : null;

                        if (shakeManager != null) {
                            shakeManager.stop();
                        }

                        Log.d("TTBURN", "bottomPane:"+bottomPane);
                        setupPanelHider(activity, topPanel, bottomPane, topPaneInactiveAlpha, bottomPaneInactiveAlpha, Shake2Show);


                        //Testing Future Code
                        if (false) {
                            int interval = 1000;
                            Handler handler = new Handler(Looper.getMainLooper());

                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    FrameLayout VideoLayout = findTikTokRootView(root);
                                    View child;
                                    if (VideoLayout != null) {
                                        child = ViewFinder.getChildByClassName(VideoLayout, "InteractAreaRootLayout", 0);
                                        child = ViewFinder.getChildByClassName(child, "InteractCheckDrawRelativeLayout", 0);
                                        child = ViewFinder.getChildByClassName(child, "InteractFrameLayout", 0);
                                        child = ViewFinder.getNthChildByClassName(child, "InteractConstraintLayout", 0);
                                        ViewUtils.printChildren(child);
                                        child.setAlpha(0.5f);
                                    }

                                    handler.postDelayed(this, interval);
                                }
                            };

                            handler.post(runnable);

                            Log.d("TikTokPaneSearcher [F]", "findTarget: " + topPanel);
                        }
                    }, 500);
                });
            }
        });
    }


}

