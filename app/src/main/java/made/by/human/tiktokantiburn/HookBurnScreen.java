package made.by.human.tiktokantiburn;

import android.app.Activity;
import android.content.Context;
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

import com.google.ar.core.Frame;

import java.util.LinkedList;
import java.util.Queue;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookBurnScreen implements IXposedHookLoadPackage {
    final int RandomDirection = View.LAYOUT_DIRECTION_RTL;
    View possibleLinearLayout;

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
        return null; // не нашли
    }

    public static View findChildAtPath(View parent, int... path) {
        View current = parent;
        for (int index : path) {
            if (!(current instanceof ViewGroup)) {
                return null; // дошли не до контейнера
            }
            ViewGroup group = (ViewGroup) current;
            if (index < 0 || index >= group.getChildCount()) {
                return null; // неправильный индекс
            }
            current = group.getChildAt(index);
        }
        return current;
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


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.main.MainActivity", lpparam.classLoader, "onWindowFocusChanged", boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                final Activity activity = (Activity) param.thisObject;



                activity.runOnUiThread(() -> {
                    new android.os.Handler().postDelayed(() -> {

                        View root = activity.getWindow().getDecorView().getRootView();
                        View topPanel = (View) findRootLayout(root).getParent().getParent().getParent();
                        topPanel.setAlpha(0f);




                        View BottomPane = findViewByEnumeration(root);
                        if (BottomPane != null) {
                            BottomPane.setAlpha(0.5f);
                        }



                        Handler closeHandler = new Handler();
                        Runnable hideRunnable = () -> {
                            BottomPane.animate().alpha(0.5f).setDuration(200).start();
                            topPanel.animate().alpha(0f).setDuration(200).start();
                            topPanel.animate().scaleX(0).setDuration(200).start();
                        };

                        ShakeManager shakeManager = new ShakeManager(activity, () -> {
                            closeHandler.removeCallbacks(hideRunnable);

                            topPanel.animate().scaleX(1f).setDuration(200).start();
                            topPanel.animate().alpha(1f).setDuration(200).start();
                            BottomPane.animate().alpha(1f).setDuration(200).start();

                            closeHandler.postDelayed(hideRunnable, 5000);
                        });




                        shakeManager.start();



                        //Test Future Code
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
