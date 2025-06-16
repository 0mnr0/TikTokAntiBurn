package made.by.human.tiktokantiburn;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.Map;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AppHook implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private String modulePath = null;
    Context context;
    private final String TAG = "TikTokAntiBurn";


    private boolean EnablePlusStatus = false;




    private View findViewByContentDescription(View root, String targetDesc) {
        if (root == null) return null;

        CharSequence desc = root.getContentDescription();
        if (desc != null && targetDesc.contentEquals(desc)) {
            return root;
        }

        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                View result = findViewByContentDescription(group.getChildAt(i), targetDesc);
                if (result != null) return result;
            }
        }

        return null;
    }


    public boolean GetXposedSettings(String settingName, Boolean value) {
        XSharedPreferences prefs = new XSharedPreferences("made.by.human.tiktokantiburn", "Preferences");
        prefs.makeWorldReadable();
        prefs.reload();
        XposedBridge.log("prefs.getXposedBoolean ("+settingName+") -> " + (prefs.getBoolean(settingName, value)));
        return prefs.getBoolean(settingName, value);
    }


    public boolean GetBoolean( String settingName, boolean defaultValue) {
        Context hostAppContext;
        try {
            Context appContext = AndroidAppHelper.currentApplication().createPackageContext(
                    "made.by.human.tiktokantiburn",
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE
            );

// Теперь получаем SharedPreferences
            SharedPreferences prefs = appContext.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            File prefsFile = new File("/data/data/made.by.human.tiktokantiburn/shared_prefs/Preferences.xml");
            boolean res = prefs.getBoolean("ROOT:EnablePlusStatus", false);
            XposedBridge.log(settingName+" -> "+ res);
            XposedBridge.log("prefsFile -> "+ prefsFile.exists());

            return res;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void DumpPreferences() {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        Map<String, ?> allPrefs = prefs.getAll();

        XposedBridge.log(TAG + ": DumpPreferences start — total " + allPrefs.size() + " entries");
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            XposedBridge.log(TAG + ": key=" + entry.getKey() + ", value=" + entry.getValue() + ", type=" + (entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null"));
        }
        XposedBridge.log(TAG + ": DumpPreferences end");
    }





    public void UpdateViewByRules(View targetButton) {
        //if (GetXposedSettings("ROOT:Disable:GONE", false)) {
        //    targetButton.setVisibility(View.GONE);
        //} else if (GetXposedSettings("ROOT:Disable:INVISIBLE", false)) {
        //    targetButton.setAlpha(0f);
        //}
        targetButton.setVisibility(View.GONE);
    }




    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.zhiliaoapp.musically"))
            return;


        XposedHelpers.findAndHookMethod(
                "android.app.Application",
                lpparam.classLoader,
                "onCreate",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        context = (Context) param.thisObject;

                        XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.main.MainActivity", lpparam.classLoader, "onWindowFocusChanged", boolean.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                final Activity activity = (Activity) param.thisObject;
                                //if (!GetXposedSettings("ROOT:EnablePlusModule", false)) {return;}


                                activity.runOnUiThread(() -> {
                                    new android.os.Handler().postDelayed(() -> {
                                        View root = activity.getWindow().getDecorView().getRootView();
                                        View targetButton = findViewByContentDescription(root, ResourceHelper.getString(R.string.LSPosedHookButtonByText));



                                        if (targetButton != null) {
                                            UpdateViewByRules(targetButton);
                                            targetButton.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                                                    UpdateViewByRules(targetButton));
                                        }
                                    }, 500);
                                });
                            }
                        });

                    }
                }
        );


    }





    @Override
    public void initZygote(StartupParam startupParam) {
        XposedBridge.log(TAG + ": initZygote triggered!");
        modulePath = startupParam.modulePath;
    }

}