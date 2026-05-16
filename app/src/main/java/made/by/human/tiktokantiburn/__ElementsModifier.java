package made.by.human.tiktokantiburn;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import made.by.human.tiktokantiburn.helpers.Debouncer;
import made.by.human.tiktokantiburn.helpers.PathFinder;
import made.by.human.tiktokantiburn.helpers.ShouldRun;
import made.by.human.tiktokantiburn.settings.__SettingsGetter;

public class __ElementsModifier implements IXposedHookLoadPackage {
    Debouncer debouncer;
    Activity activity;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {

            XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.main.MainActivity",
                    lpparam.classLoader, "onWindowFocusChanged", boolean.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            boolean hasFocus = (boolean) param.args[0];
                            if (!hasFocus || !ShouldRun.check(param.thisObject)) {
                                returnToBasics();
                                return;
                            }
                            activity = (Activity) param.thisObject;
                            if (__SettingsGetter.getBoolean(activity, "StartWithBinder", false)) {return;}


                            if (debouncer == null) {
                                debouncer = new Debouncer(300);
                            }

                            View root = activity.getWindow().getDecorView().getRootView();
                            ViewTreeObserver vto = root.getViewTreeObserver();


                            vto.removeOnGlobalLayoutListener(globalLayoutListener);
                            vto.addOnGlobalLayoutListener(globalLayoutListener);
                        }
                    }
            );
        } catch (Exception ignored) {
            XposedBridge.log("BindModule: java.lang.ClassNotFoundException");
        } //
    }

    private final ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = () -> {
        if (activity == null) return;
        View root = activity.getWindow().getDecorView().getRootView();
        debouncer.call(() -> hideUI(activity, root));
    };




    List<View> SearchResultList;
    private void returnToBasics() {
        if (SearchResultList == null) {return;}
        SearchResultList.forEach(SearchResult -> {
            if (SearchResult != null) {
                SearchResult.setAlpha(1f);
                SearchResult.setVisibility(View.VISIBLE);
            }
        });
    }



    private void hideUI(Activity ctx, View rootWindow) {
        if (ctx == null || rootWindow == null) {
            XposedBridge.log("[Search]: " + "ctx: "+ctx+" | window:"+rootWindow);
            return;
        }
        XposedBridge.log("[Search]: ctx is fine");

        final boolean showHidden = __SettingsGetter.getBoolean(ctx, "StartWithBinder", false);
        Set<String> Settings = __SettingsGetter.getStringSet(ctx, "ElementsModifiers", new HashSet<>());

        XposedBridge.log("[Search] L: " + Settings.size());
        Settings.forEach(line -> {
            String[] splitData = line.split(";");
            float Alpha = Float.parseFloat(splitData[0]);
            boolean IsVisible = Objects.equals(splitData[1], "1");
            boolean keepOnEveryVideo = Objects.equals(splitData[2], "1");
            String ViewPath = splitData[3];

            XposedBridge.log("[Search] Path: "+ViewPath);
            SearchResultList = PathFinder.search(rootWindow, ViewPath, keepOnEveryVideo);
            SearchResultList.forEach(SearchResult -> {
                XposedBridge.log("[Search:Result] " + PathFinder.getPath(SearchResult));
                if (SearchResult != null) {
                    SearchResult.setAlpha(Alpha);
                    SearchResult.setVisibility((IsVisible || showHidden) ? View.VISIBLE : View.GONE);
                }
            });
        });
    }
}