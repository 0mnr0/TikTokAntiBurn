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

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AppHook implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private String modulePath = null;
    private final String TAG = "TikTokAntiBurn";




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



    public boolean GetBoolean(String settingName, boolean defaultValue) throws PackageManager.NameNotFoundException {
        Context context = AndroidAppHelper.currentApplication().createPackageContext(
                "made.by.human.tiktokantiburn.AppHook",
                Context.CONTEXT_IGNORE_SECURITY
        );

        SharedPreferences prefs = context.getSharedPreferences("LSPosedSettings", Context.MODE_PRIVATE);
        return prefs.getBoolean(settingName, defaultValue);
    }


    public void UpdateViewByRules(View targetButton) {
        try {
            if (GetBoolean("ROOT:Disable:GONE", false)) {
                targetButton.setVisibility(View.GONE);
            } else if (GetBoolean("ROOT:Disable:INVISIBLE", false)) {
                targetButton.setAlpha(0f);
            }
        } catch (PackageManager.NameNotFoundException e) {
            targetButton.setVisibility(View.GONE);
        }
    }


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.zhiliaoapp.musically"))
            return;

        if (!GetBoolean("ROOT:DisablePlusStatus", false)) {return;}

        XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.main.MainActivity", lpparam.classLoader, "onWindowFocusChanged", boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                final Activity activity = (Activity) param.thisObject;

                activity.runOnUiThread(() -> {
                    new android.os.Handler().postDelayed(() -> {
                        View root = activity.getWindow().getDecorView().getRootView();
                        View targetButton = findViewByContentDescription(root, ResourceHelper.getString(R.string.LSPosedHookButtonByText));



                        if (targetButton != null) {
                            UpdateViewByRules(targetButton);
                            targetButton.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> UpdateViewByRules(targetButton));
                        }
                    }, 500);
                });
            }
        });
    }



    @Override
    public void initZygote(StartupParam startupParam) {
        XposedBridge.log(TAG + ": initZygote triggered!");
        modulePath = startupParam.modulePath;
    }

}