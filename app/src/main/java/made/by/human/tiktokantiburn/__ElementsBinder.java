package made.by.human.tiktokantiburn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.XModuleResources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import made.by.human.tiktokantiburn.helpers.HIERARCHY;
import made.by.human.tiktokantiburn.helpers.PathFinder;
import made.by.human.tiktokantiburn.helpers.ViewState;

public class __ElementsBinder implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private final String myPkgName = "made.by.human.tiktokantiburn";
    private boolean enableBinder = false;
    private Context AppContext = null;


    private View RootWindow, GlobalSettingsPanel;
    private final Map<View, ViewState> ModifiedViews = new IdentityHashMap<>();



    private LinearLayout Modifiers, KeepEveryVideo;
    private TextView classNameInfo;
    private TextView AllowModifyText;
    private ImageButton left;
    private ImageButton right;
    private ImageButton up;
    private ImageButton down;
    private CheckBox checkBoxGone, AllowModify, EveryVideoBox;
    private SeekBar seekBarAlpha;



    private final Set<View> panelViews = new HashSet<>();
    private boolean notXposedTriggers(View view) {
        return !panelViews.contains(view);
    }


    @Override
    public void initZygote(StartupParam startupParam) {
        XModuleResources moduleRes = XModuleResources.createInstance(startupParam.modulePath, null);
    }


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(
                "android.view.View",
                lpparam.classLoader,
                "performClick",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        View view = (View) param.thisObject;
                        if (notXposedTriggers(view) && enableBinder) {
                            RootWindow = view.getRootView();
                            param.setResult(true);
                            onClick(view);
                        }
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                "android.view.View",
                lpparam.classLoader,
                "setOnClickListener",
                "android.view.View$OnClickListener",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        final View.OnClickListener originalListener = (View.OnClickListener) param.args[0];
                        param.args[0] = (View.OnClickListener) v -> {
                            if (notXposedTriggers(v) && enableBinder) {
                                onClick(v);
                            } else {
                                originalListener.onClick(v);
                            }
                        };
                    }
                }
        );

        XposedHelpers.findAndHookMethod("com.ss.android.ugc.aweme.main.MainActivity", lpparam.classLoader, "onWindowFocusChanged", boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                XposedBridge.log("[ContextSearch] Running...");
                AppContext = (Context) param.thisObject;
                if (AppContext == null) {
                    XposedBridge.log("[ContextSearch] found nothing"); return;
                }

                XposedBridge.log("[ContextSearch] Found");
                SharedPreferences prefs = AppContext.getSharedPreferences("LSPrefs", Context.MODE_PRIVATE);
                if (prefs.getBoolean("XPOSED:RunBinder", false)) {
                    enableBinder = true;
                    loadList(prefs);
                    injectLayout((Activity) param.thisObject);
                    XposedBridge.log("[ContextSearch] Should be injected");
                } else {
                    enableBinder = false;
                    XposedBridge.log("[ContextSearch] Skipped (XPOSED:RunBinder) -> false");
                }
                XposedBridge.log("[enableBinder]: "+enableBinder);
            }
        });
    }



    public void loadList(SharedPreferences prefs){
        Set<String> Settings = prefs.getStringSet("ElementsModifiers", new HashSet<>());

        Settings.forEach(line -> {
            XposedBridge.log("[Loading BY Search]: "+line);
            String[] splitData = line.split(";");
            float Alpha = Float.parseFloat(splitData[0]); if (Alpha < 0.3) {Alpha = 0.3f;}
            final boolean keepOnEvery = splitData[2].equals("1");
            final String ViewPath = splitData[3];


            float finalAlpha = Alpha;
            List<View> SearchResultList = PathFinder.search(RootWindow, ViewPath, false);
            SearchResultList.forEach(view -> {
                XposedBridge.log("[Loading BY Result]: "+view);
                if (view != null) {
                    ModifiedViews.put(view, new ViewState(finalAlpha, View.VISIBLE, keepOnEvery));
                }
            });

        });
    }










    View activeView;

    @SuppressLint("SetTextI18n")
    private void onClick(View view) {
        if (!enableBinder || AppContext == null) {
            XposedBridge.log("[Click] APP Context is none. View Hook is not loaded (binder: " + enableBinder + " | ctx: " + AppContext + ")");
            return;
        } else {
            XposedBridge.log("[Click] APP Context is OK. Canceling...");
        }

        AllowModify.setEnabled(view != null);
        AllowModify.setAlpha(view == null ? 0.5f : 1f);
        AllowModifyText.setAlpha(view == null ? 0.5f : 1f);
        if (view == null) { return; }
        if (activeView != null) {
            HIERARCHY.ClearBorder(activeView);
        }

        activeView = view;
        try {
            classNameInfo.setText(view.getClass().getName());
        } catch (Exception ignored) {
            classNameInfo.setText("Class Not Defined :(");
        }

        String viewPath = PathFinder.getPath(view);
        XposedBridge.log("[Click] Path: "+viewPath);

        KeepEveryVideo.setVisibility(viewPath.contains("/VerticalViewPager/") ? View.VISIBLE : View.GONE);
        HIERARCHY.SetBorder(view);

        HIERARCHY.UpdatePointers(view, left, right, up, down);
        HIERARCHY.UpdateViewAttr(view, checkBoxGone, seekBarAlpha);
        if (ModifiedViews.containsKey(view)) { AllowModify.setChecked(true); }
    }

    private void runAction(String action) {
        final View previousView = activeView;
        switch (action) {
            case HIERARCHY.Type.LEFT:  activeView = HIERARCHY.getPrevView(activeView); break;
            case HIERARCHY.Type.NEXT:  activeView = HIERARCHY.getNextView(activeView); break;
            case HIERARCHY.Type.UP:    activeView = HIERARCHY.getParent(activeView);   break;
            case HIERARCHY.Type.DOWN:  activeView = HIERARCHY.getChild(activeView);    break;
        }
        if (activeView == null) { return; }
        HIERARCHY.ClearBorder(previousView);
        onClick(activeView);
    }


    public void SaveRules() {
        if (AllowModify.isChecked()) {
            if (!ModifiedViews.containsKey(activeView)) {
                ModifiedViews.put(activeView, new ViewState(1f, View.VISIBLE, EveryVideoBox.isChecked()));
            }

            ViewState state = ModifiedViews.get(activeView);
            if (state != null) {
                state.alpha = seekBarAlpha.getProgress()/100f;
                state.visibility = checkBoxGone.isChecked() ? 1 : 0;
            }
        } else ModifiedViews.remove(activeView);
        XposedBridge.log("[ModifiedViews]: "+ModifiedViews);
    }


    public void setVisibility(boolean state) {
        if (activeView == null) {return;}
        activeView.setVisibility(state ? View.VISIBLE : View.GONE);
        SaveRules();
    }

    public void setAlpha(int value) {
        if (activeView == null) {return;}
        value = Math.max(0, Math.min(100, value));
        activeView.setAlpha(value / 100f);
        SaveRules();
    }




    public void Save() {
        SharedPreferences.Editor Prefs = AppContext.getSharedPreferences("LSPrefs", Context.MODE_PRIVATE).edit();
        Set<String> set = new HashSet<>();


        ModifiedViews.forEach((key, value) -> {
            final String ElementPath = PathFinder.getPath(key);
            final String ApplyRules = value.alpha+";"+value.visibility+";"+value.keepOnEveryVideo+";";
            set.add(ApplyRules+ElementPath);
        });

        Prefs.putStringSet(
                "ElementsModifiers",
                set
        ).apply();
        Prefs.putBoolean("XPOSED:RunBinder", false).apply();
        if (GlobalSettingsPanel.getParent() != null) {
            ((ViewGroup) GlobalSettingsPanel.getParent()).removeView(GlobalSettingsPanel);
        }
        enableBinder = false;
        Toast.makeText(AppContext, ResourceHelper.getString(R.string.__Binder_Saved), Toast.LENGTH_LONG).show();
        Toast.makeText(AppContext, ResourceHelper.getString(R.string.__Binder_Saved2), Toast.LENGTH_LONG).show();



    }


    public void JustClose() {
        SharedPreferences.Editor Prefs = AppContext.getSharedPreferences("LSPrefs", Context.MODE_PRIVATE).edit();
        Prefs.putBoolean("XPOSED:RunBinder", false).apply();
        if (GlobalSettingsPanel.getParent() != null) {
            ((ViewGroup) GlobalSettingsPanel.getParent()).removeView(GlobalSettingsPanel);
        }
        enableBinder = false;
    }

    public void correctCurrentView() {
        if (activeView == null) {return;}
        ModifiedViews.remove(activeView);
        activeView.setVisibility(View.VISIBLE);
        activeView.setAlpha(1f);
    }




    @SuppressLint("DiscouragedApi") // only variant in xposed module to fix warning
    private void injectLayout(Activity activity) {
        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        if (decor.findViewWithTag("xposed_panel") != null) return;

        XposedBridge.log("[LayoutInjector] Init");
        Context ctx;
        try {
            ctx = activity.createPackageContext(myPkgName, Context.CONTEXT_IGNORE_SECURITY);
            XposedBridge.log("[LayoutInjector] Got");
        } catch (PackageManager.NameNotFoundException e) {
            XposedBridge.log("[LayoutInjector] Failed");
            throw new RuntimeException(e);
        }

        View panel;
        LayoutInflater inflater = LayoutInflater.from(ctx).cloneInContext(ctx);
        int layoutId = ctx.getResources().getIdentifier("xposed_panel", "layout", myPkgName);
        try {
            panel = inflater.inflate(layoutId, null, false);
            XposedBridge.log("[LayoutInjector] ID Parsed!");
        } catch (Throwable t) {
            XposedBridge.log("[LayoutInjector] CRASH: " + t.getMessage());
            for (StackTraceElement el : t.getStackTrace()) { XposedBridge.log("  at " + el.toString()); }
            return;
        }
        panel.setTag("xposed_panel");



        classNameInfo = panel.findViewById(getId(ctx, "panel_text"));
        left          = panel.findViewById(getId(ctx, "Left"));
        up            = panel.findViewById(getId(ctx, "Up"));
        down          = panel.findViewById(getId(ctx, "Down"));
        right         = panel.findViewById(getId(ctx, "Right"));
        checkBoxGone  = panel.findViewById(getId(ctx, "VisibilityGONE"));
        seekBarAlpha  = panel.findViewById(getId(ctx, "ViewALPHA"));
        AllowModify   = panel.findViewById(getId(ctx, "AllowModify"));
        Modifiers      = panel.findViewById(getId(ctx, "Modifiers"));
        AllowModifyText = panel.findViewById(getId(ctx, "AllowModifyText"));
        KeepEveryVideo  = panel.findViewById(getId(ctx, "KeepEveryVideo"));
        EveryVideoBox   = panel.findViewById(getId(ctx, "KeepEveryVideoBox"));

        Button applyButton = panel.findViewById(getId(ctx, "apply_button"));
        ImageButton closeButton = panel.findViewById(getId(ctx, "close"));
        TextView everyVideoText = panel.findViewById(getId(ctx, "KeepEveryVideoText"));



        panelViews.addAll(Arrays.asList(
                classNameInfo, applyButton, closeButton,
                left, right, up, down,
                AllowModify, AllowModifyText, Modifiers, KeepEveryVideo,
                EveryVideoBox, everyVideoText,
                checkBoxGone, seekBarAlpha, panel
        ));



        applyButton.setOnClickListener(v -> Save());
        closeButton.setOnClickListener(v -> JustClose());
        AllowModifyText.setOnClickListener(v -> {if (activeView!=null) { AllowModify.performClick(); }});
        everyVideoText.setOnClickListener(v -> {if (activeView!=null) { EveryVideoBox.performClick(); }});
        left.setOnClickListener(v -> runAction(HIERARCHY.Type.LEFT));
        right.setOnClickListener(v -> runAction(HIERARCHY.Type.NEXT));
        up.setOnClickListener(v -> runAction(HIERARCHY.Type.UP));
        down.setOnClickListener(v -> runAction(HIERARCHY.Type.DOWN));

        AllowModify.setOnClickListener(v -> {
            if (activeView == null) {v.setEnabled(AllowModify.isChecked()); return;}
            Modifiers.setVisibility(AllowModify.isChecked() ? View.VISIBLE : View.GONE);
            if (!AllowModify.isChecked()) {correctCurrentView();}
        });
        checkBoxGone.setOnClickListener(v -> setVisibility(checkBoxGone.isChecked()));

        seekBarAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setAlpha(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.topMargin = 350;

        activity.runOnUiThread(() -> {
            GlobalSettingsPanel = panel;
            decor.addView(panel, params);
            setVisibility(checkBoxGone.isChecked());
        });
        XposedBridge.log("[LayoutInjector] Done");
    }


    @SuppressLint("DiscouragedApi")
    private int getId(Context moduleContext, String name) {
        return moduleContext.getResources().getIdentifier(name, "id", myPkgName);
    }
}