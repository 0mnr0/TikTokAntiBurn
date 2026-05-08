package made.by.human.tiktokantiburn;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class BlockableLinearLayout extends LinearLayout {

    private boolean blockTouches = false;

    public BlockableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBlockTouches(boolean block) {
        this.blockTouches = block;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return blockTouches || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Поглощаем событие, чтобы оно не ушло дальше по иерархии
        return blockTouches || super.onTouchEvent(ev);
    }
}
