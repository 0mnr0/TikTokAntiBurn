package made.by.human.tiktokantiburn.helpers;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.util.Map;
import java.util.WeakHashMap;

public class HIERARCHY {
    private static final Map<View, Drawable> originalBackgrounds = new WeakHashMap<>();

    public static void SetBorder(View view) {
        originalBackgrounds.put(view, view.getBackground());
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.TRANSPARENT);
        border.setStroke(5, Color.RED);
        view.setBackground(border);
    }

    public static void ClearBorder(View view) {
        if (view == null) return;
        Drawable original = originalBackgrounds.remove(view);
        view.setBackground(original);
    }


    public static View getParent(View currentView) {
        ViewParent parent = currentView.getParent();
        return (parent instanceof View) ? (View) parent : null;
    }

    public static View getPrevView(View currentView) {
        ViewParent p = currentView.getParent();
        if (!(p instanceof ViewGroup)) return null;
        ViewGroup parent = (ViewGroup) p;
        int index = parent.indexOfChild(currentView);
        return index > 0 ? parent.getChildAt(index - 1) : null;
    }

    public static boolean haveParent(View currentView) {
        return currentView.getParent() != null;
    }

    public static View getChild(View currentView) {
        View firstChild = null;

        if (currentView instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) currentView;

            if (parent.getChildCount() > 0) {
                firstChild = parent.getChildAt(0);
            }
        }
        return firstChild;
    }



    public static View getNextView(View currentView) {
        ViewParent p = currentView.getParent();
        if (!(p instanceof ViewGroup)) return null;
        ViewGroup parent = (ViewGroup) p;
        int index = parent.indexOfChild(currentView);
        return (index >= 0 && index < parent.getChildCount() - 1)
                ? parent.getChildAt(index + 1) : null;
    }

    public static void UpdatePointers(View currentView, ImageButton GoLeft, ImageButton GoRight, ImageButton GoUP, ImageButton GoDOWN) {
        boolean havePrevView = getPrevView(currentView) != null;
        boolean haveNextView = getNextView(currentView) != null;
        boolean haveChildren = getChild(currentView) != null;
        boolean haveParent = haveParent(currentView);

        GoLeft.setAlpha(havePrevView ? 1f : 0.5f);
        GoLeft.setEnabled(havePrevView);

        GoRight.setAlpha(haveNextView ? 1f : 0.5f);
        GoRight.setEnabled(haveNextView);

        GoUP.setAlpha(haveParent ? 1f : 0.5f);
        GoUP.setEnabled(haveParent);

        GoDOWN.setAlpha(haveChildren ? 1f : 0.5f);
        GoDOWN.setEnabled(haveChildren);
    }

    public static void UpdateViewAttr(View currentView, CheckBox VisibilityCheckbox, SeekBar AlphaSlider) {
        VisibilityCheckbox.setChecked(
                currentView.getVisibility() != View.GONE
        );

        AlphaSlider.setProgress(
                (int) (currentView.getAlpha() * 100)
        );
    }









    public static class Type {
        final public static String UP = "UP";
        final public static String DOWN = "DOWN";
        final public static String LEFT = "LEFT";
        final public static String NEXT = "NEXT";
    }
}
