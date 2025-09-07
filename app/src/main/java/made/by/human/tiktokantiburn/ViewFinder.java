package made.by.human.tiktokantiburn;

import android.view.View;
import android.view.ViewGroup;

public class ViewFinder {
    public static View getChildByClassName(View parent, String simpleClassName, int position) {
        if (!(parent instanceof ViewGroup)) return null;

        ViewGroup group = (ViewGroup) parent;
        int count = 0;

        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.getClass().getSimpleName().equals(simpleClassName)) {
                if (count == position) {
                    return child;
                }
                count++;
            }
        }

        return null; // Если не нашли
    }











    public static View getNthChildByClassName(View parent, String simpleClassName, int position) {
        if (parent == null) return null;

        // Создаём контейнер для найденных совпадений
        Holder holder = new Holder();
        holder.targetIndex = position;

        return searchRecursive(parent, simpleClassName, holder);
    }

    // Вспомогательный класс для хранения состояния поиска
    private static class Holder {
        int count = 0;       // сколько уже найдено
        int targetIndex = 0; // нужный индекс
    }

    private static View searchRecursive(View view, String simpleClassName, Holder holder) {
        if (view.getClass().getSimpleName().equals(simpleClassName)) {
            if (holder.count == holder.targetIndex) {
                return view;
            }
            holder.count++;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = searchRecursive(group.getChildAt(i), simpleClassName, holder);
                if (found != null) return found;
            }
        }

        return null;
    }

}

