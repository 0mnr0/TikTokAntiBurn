package made.by.human.tiktokantiburn;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {

    // Рекурсивная функция с уровнем вложенности
    private static void printChildren(View view, int level) {
        if (view == null) return;

        // Формируем отступы для наглядной структуры
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < level; i++) {
            prefix.append("--");
        }

        // Выводим текущий View
        Log.d("TTViewHierarchy", prefix + view.getClass().getSimpleName());

        // Если это ViewGroup, рекурсивно обходим детей
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                printChildren(group.getChildAt(i), level + 1);
            }
        }
    }
}

