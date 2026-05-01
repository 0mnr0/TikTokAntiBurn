package made.by.human.tiktokantiburn;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.view.View;

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

public class __ElementsModifier implements IXposedHookLoadPackage {

    private Activity getActivityFromContext(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Debouncer debouncer = new Debouncer(300);

        XposedHelpers.findAndHookMethod(
                View.class,
                "onAttachedToWindow",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        View view = (View) param.thisObject;
                        Activity activity = getActivityFromContext(view.getContext());
                        View rootWindow = view.getRootView();

                        debouncer.call(() -> hideUI(activity, rootWindow));
                    }
                }
        );
    }




    private void hideUI(Activity activity, View rootWindow) {
        if (activity == null || rootWindow == null) {
            return;
        }

        SharedPreferences prefs = activity.getSharedPreferences("LSPrefs", Context.MODE_PRIVATE);
        final boolean showHidden = prefs.getBoolean("XPOSED:RunBinder", false);

        Set<String> Settings = prefs.getStringSet("ElementsModifiers", new HashSet<>());

        XposedBridge.log("[Search]: " + Settings.size());
        Settings.forEach(line -> {
            String[] splitData = line.split(";");
            float Alpha = Float.parseFloat(splitData[0]);
            boolean IsVisible = Objects.equals(splitData[1], "1");
            boolean keepOnEveryVideo = Objects.equals(splitData[2], "1");
            String ViewPath = splitData[3];

            List<View> SearchResultList = PathFinder.search(rootWindow, ViewPath, keepOnEveryVideo);
            SearchResultList.forEach(SearchResult -> {
                XposedBridge.log("[Search:Result] " + SearchResult);
                if (SearchResult != null) {
                    SearchResult.setAlpha(Alpha);
                    SearchResult.setVisibility((IsVisible || showHidden) ? View.VISIBLE : View.GONE);
                }
            });
        });
    }
}