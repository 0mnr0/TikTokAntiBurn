package made.by.human.tiktokantiburn;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

//public class AppHook implements IXposedHookZygoteInit, IXposedHookLoadPackage {
public class AppHook implements IXposedHookLoadPackage {


    // private String modulePath = null;
    // private final String TAG = "TikTokAntiBurn";




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


    public void RealUpdate(boolean statement, View targetButton) {
        if (statement) {
            targetButton.setAlpha(0f);
        } else {
            targetButton.setVisibility(View.GONE);
        }
    }


    public void UpdateViewByRules(Context context, View targetButton) {
        SharedPreferences prefs = context.getSharedPreferences("LSPrefs", Context.MODE_PRIVATE);
        boolean someSetting = prefs.getBoolean("XPOSED:MakeInvisibleInstead", false);
        RealUpdate(someSetting, targetButton);
    }



    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.zhiliaoapp.musically"))
            return;


        XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.main.MainActivity", lpparam.classLoader, "onWindowFocusChanged", boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                final Activity activity = (Activity) param.thisObject;


                activity.runOnUiThread(() -> {
                    new android.os.Handler().postDelayed(() -> {
                        View root = activity.getWindow().getDecorView().getRootView();
                        View targetButton = findViewByContentDescription(root, ResourceHelper.getString(R.string.LSPosedHookButtonByText));


                        if (targetButton != null) {
                            UpdateViewByRules(activity, targetButton);
                            targetButton.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                                    UpdateViewByRules(activity, targetButton));
                        }
                    }, 500);
                });
            }
        });
    }





    //@Override
    //public void initZygote(StartupParam startupParam) {
    //    XposedBridge.log(TAG + ": initZygote triggered!");
    //    modulePath = startupParam.modulePath;
    //}



}