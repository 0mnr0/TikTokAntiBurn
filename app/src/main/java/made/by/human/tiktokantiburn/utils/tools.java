package made.by.human.tiktokantiburn.utils;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.slider.Slider;

public class tools {
    public static void setSafeValue(Slider slider, float value) {
        float from = slider.getValueFrom();
        float to = slider.getValueTo();
        float step = slider.getStepSize();

        if (value < from) value = from;
        if (value > to) value = to;

        if (step > 0.0f) {
            int steps = Math.round((value - from) / step);
            value = from + (steps * step);
        }

        slider.setValue(value);
    }
}
