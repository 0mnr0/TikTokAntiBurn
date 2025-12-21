package made.by.human.tiktokantiburn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookBurnScreen implements IXposedHookLoadPackage {
    private View possibleLinearLayout;
    private ShakeManager shakeManager;
    private Handler handler;
    private Runnable hideRunnable;
    private boolean isLogicRunning = false;


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
                    if (i != 2 && child instanceof FrameLayout) possibleWeight += 1;
                    if (i == 2 && child instanceof Button) possibleWeight += 1;
                }
                if (possibleWeight >= 4) return possibleLinearLayout;
            }
        }
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                View result = findViewByEnumeration(child);
                if (result != null) return result;
            }
        }
        return null;
    }

    private void changeAlpha(View view, float alpha) {
        if (view != null) {
            view.animate().alpha(alpha).setDuration(200).start();
        }
    }


    private void cleanup() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        if (shakeManager != null) {
            try {
                shakeManager.stop();
            } catch (Exception e) {
                Log.e("TTBURN", "Error stopping shake manager", e);
            }
            shakeManager = null;
        }

        isLogicRunning = false;
    }

    private void startBurnProtection(Activity activity, boolean allowTop, boolean allowBottom, boolean shake2Show, float topAlpha, float bottomAlpha) {
        cleanup();

        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }

        View root = activity.getWindow().getDecorView().getRootView();
        View topPanel = null;
        try {
            topPanel = allowTop ? (View) findRootLayout(root).getParent().getParent().getParent() : null;
        } catch (Exception ignored) {}

        View bottomPane = allowBottom ? findViewByEnumeration(root) : null;
        final View finalTop = topPanel;
        final View finalBottom = bottomPane;

        hideRunnable = new Runnable() {
            @Override
            public void run() {
                changeAlpha(finalTop, topAlpha);
                changeAlpha(finalBottom, bottomAlpha);
                if (handler != null) {
                    handler.postDelayed(this, 15000);
                }
            }
        };

        handler.postDelayed(hideRunnable, 1000);
        if (shake2Show) {
            shakeManager = new ShakeManager(activity, () -> {
                showTemporarily(finalTop, finalBottom, topAlpha, bottomAlpha);
            });
            shakeManager.start();
        }

        if (finalBottom != null) {
            finalBottom.setOnClickListener(v -> {
                showTemporarily(finalTop, finalBottom, topAlpha, bottomAlpha);
            });
        }

        isLogicRunning = true;
    }

    private void showTemporarily(View top, View bottom, float inactiveTopAlpha, float inactiveBottomAlpha) {
        changeAlpha(top, 1.0f);
        changeAlpha(bottom, 1.0f);

        if (handler != null && hideRunnable != null) {
            handler.removeCallbacks(hideRunnable);

            handler.postDelayed(() -> {
                changeAlpha(top, inactiveTopAlpha);
                changeAlpha(bottom, inactiveBottomAlpha);

                handler.postDelayed(hideRunnable, 15000);
            }, 10000);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.zhiliaoapp.musically"))
            return;

        XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.main.MainActivity", lpparam.classLoader, "onWindowFocusChanged", boolean.class, new XC_MethodHook() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                boolean hasFocus = (boolean) param.args[0];
                final Activity activity = (Activity) param.thisObject;

                if (!hasFocus) {
                    cleanup();
                    return;
                }


                final boolean allowTop = GetBoolean(activity, "XPOSED:AllowTopPaneModifier", false);
                final boolean allowBottom = GetBoolean(activity, "XPOSED:AllowBottomPaneModifier", false);
                final boolean shake2Show = GetBoolean(activity, "XPOSED:Shake2Show", false);

                if (!allowTop && !allowBottom) {
                    cleanup();
                    return;
                }

                final float topOpacity = ((float) GetInt(activity, "XPOSED:TopPaneOpacity", 100)) / 100;
                final float bottomOpacity = ((float) GetInt(activity, "XPOSED:BottomPaneOpacity", 50)) / 100;

                activity.runOnUiThread(() -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        startBurnProtection(activity, allowTop, allowBottom, shake2Show, topOpacity, bottomOpacity);
                    }, 500);
                });
            }
        });
    }
}