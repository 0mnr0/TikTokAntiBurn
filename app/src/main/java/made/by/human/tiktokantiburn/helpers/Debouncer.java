package made.by.human.tiktokantiburn.helpers;

import android.os.Handler;
import android.os.Looper;

public class Debouncer {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long delayMs;
    private Runnable pending;

    public Debouncer(long delayMs) {
        this.delayMs = delayMs;
    }

    public void call(Runnable action) {
        if (pending != null) handler.removeCallbacks(pending);
        pending = action;
        handler.postDelayed(pending, delayMs);
    }

    public void cancel() {
        if (pending != null) handler.removeCallbacks(pending);
        pending = null;
    }
}