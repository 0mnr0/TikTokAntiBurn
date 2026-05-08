package made.by.human.tiktokantiburn.helpers;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;

public class PathFinder {
    private static final boolean showDebugPath=false;


    public static String getPath(View view) {
        StringBuilder debugSearch = new StringBuilder();
        LinkedList<String> parts = new LinkedList<>();
        View current = view;

        while (current != null) {
            String simpleName = current.getClass().getSimpleName();
            if (simpleName.isEmpty()) {
                simpleName = current.getClass().getName();
            }

            ViewParent parent = current.getParent();

            if (parent instanceof ViewGroup) {
                ViewGroup parentGroup = (ViewGroup) parent;
                int totalSiblings = parentGroup.getChildCount();


                int sameTypeCount = 0;
                int myIndex = -1;

                for (int i = 0; i < totalSiblings; i++) {
                    View child = parentGroup.getChildAt(i);
                    if (child.getClass().equals(current.getClass())) {
                        sameTypeCount++;
                        if (child == current) {
                            myIndex = sameTypeCount;
                        }
                    }
                }

                if (sameTypeCount > 1 && myIndex!=1) {
                    parts.addFirst(simpleName + "=" + myIndex);
                    if (showDebugPath) {
                        debugSearch.append(simpleName).append("=").append(myIndex).append("(").append(sameTypeCount).append(")/");
                    }
                } else {
                    parts.addFirst(simpleName);
                    if (showDebugPath) {
                        debugSearch.append(simpleName).append("/");
                    }
                }

                current = parentGroup;
            } else {
                parts.addFirst("root");
                current = null;
            }
        }


        // XposedBridge.log("[Click Search]: "+String.join("/", parts)); // <-- Also Shows number of self-typed view's
        if (showDebugPath) {
            XposedBridge.log("[Click Search [D]]: "+debugSearch);
        }

        return String.join("/", parts);
    }





    //   PARSER



    public static class PathSegment {
        public final String className;
        public final int index;

        public PathSegment(String className, int index) {
            this.className = className;
            this.index = index;
        }

        @Override
        public String toString() {
            return index == -1 ? className : className + "=" + index;
        }
    }

    public static List<PathSegment> parsePath(String path) {
        List<PathSegment> segments = new ArrayList<>();
        String[] parts = path.split("/");

        for (String part : parts) {
            if (part.equals("root")) {
                segments.add(new PathSegment("root", -1));
                continue;
            }

            int eqIdx = part.indexOf('=');
            if (eqIdx != -1) {
                String className = part.substring(0, eqIdx);
                int index = Integer.parseInt(part.substring(eqIdx + 1));
                segments.add(new PathSegment(className, index));
            } else {
                segments.add(new PathSegment(part, -1));
            }
        }
        return segments;
    }

    public static List<View> search(View root, String path, boolean includeVideoType) {
        List<View> results = new ArrayList<>();
        List<PathSegment> segments = parsePath(path);

        if (segments.isEmpty() || !segments.get(0).className.equals("root")) {
            return results;
        }

        // Ищем индекс сегмента VideoViewCellRootView в пути
        int videoViewSegmentIndex = -1;
        for (int i = 0; i < segments.size(); i++) {
            if (segments.get(i).className.equals("VideoViewCellRootView")) {
                videoViewSegmentIndex = i;
                break;
            }
        }

        // Если VideoViewCellRootView не найден в пути, работаем как раньше
        if (videoViewSegmentIndex == -1) {
            View result = searchSingle(root, segments);
            if (result != null) {
                results.add(result);
            }
            return results;
        }

        // Ищем все VideoViewCellRootView или только первый
        List<View> videoViews = findAllVideoViewCellRootViews(root, segments, videoViewSegmentIndex, includeVideoType);

        // Для каждого найденного VideoViewCellRootView продолжаем поиск
        for (View videoView : videoViews) {
            View result = continueSearchFrom(videoView, segments, videoViewSegmentIndex + 1);
            if (result != null) {
                results.add(result);
                if (!includeVideoType) {
                    break; // Если includeVideoType = false, берём только первый результат
                }
            }
        }

        return results;
    }

    private static List<View> findAllVideoViewCellRootViews(View root, List<PathSegment> segments, int videoViewSegmentIndex, boolean includeAll) {
        List<View> videoViews = new ArrayList<>();
        View current = root;

        // Проходим путь до VideoViewCellRootView
        for (int i = 1; i < videoViewSegmentIndex; i++) {
            PathSegment segment = segments.get(i);

            if (!(current instanceof ViewGroup)) {
                return videoViews;
            }

            View found = findChildBySegment((ViewGroup) current, segment);
            if (found == null) {
                return videoViews;
            }

            current = found;
        }

        // Теперь ищем VideoViewCellRootView
        if (!(current instanceof ViewGroup)) {
            return videoViews;
        }

        ViewGroup group = (ViewGroup) current;
        PathSegment videoSegment = segments.get(videoViewSegmentIndex);

        for (int j = 0; j < group.getChildCount(); j++) {
            View child = group.getChildAt(j);
            String simpleName = child.getClass().getSimpleName();

            if (simpleName.isEmpty()) {
                simpleName = child.getClass().getName();
            }

            if (simpleName.equals("VideoViewCellRootView")) {
                videoViews.add(child);
                if (!includeAll) {
                    break; // Если нужен только первый, прерываем цикл
                }
            }
        }

        return videoViews;
    }

    private static View continueSearchFrom(View startView, List<PathSegment> segments, int startIndex) {
        View current = startView;

        for (int i = startIndex; i < segments.size(); i++) {
            PathSegment segment = segments.get(i);

            if (!(current instanceof ViewGroup)) {
                return null;
            }

            View found = findChildBySegment((ViewGroup) current, segment);
            if (found == null) {
                return null;
            }

            current = found;
        }

        return current;
    }

    private static View findChildBySegment(ViewGroup group, PathSegment segment) {
        int sameTypeCounter = 0;
        int targetIndex = segment.index == -1 ? 1 : segment.index;

        for (int j = 0; j < group.getChildCount(); j++) {
            View child = group.getChildAt(j);
            String simpleName = child.getClass().getSimpleName();
            if (simpleName.isEmpty()) simpleName = child.getClass().getName();

            if (simpleName.equals(segment.className)) {
                sameTypeCounter++;
                if (sameTypeCounter == targetIndex) return child;
            } else if (isTransparentWrapper(child, segment.className)) {
                // Прозрачная обёртка — ищем нужный класс внутри неё
                View inner = findChildBySegment((ViewGroup) child, segment);
                if (inner != null) return inner;
            }
        }
        return null;
    }

    private static boolean isTransparentWrapper(View view, String lookingFor) {
        if (!(view instanceof ViewGroup)) return false;
        String name = view.getClass().getSimpleName();
        // Однобуквенные обфусцированные имена — потенциальные обёртки
        if (name.length() > 2) return false;
        ViewGroup vg = (ViewGroup) view;
        if (vg.getChildCount() == 0) return false;
        // Проверяем, есть ли нужный класс среди детей
        for (int i = 0; i < vg.getChildCount(); i++) {
            String childName = vg.getChildAt(i).getClass().getSimpleName();
            if (childName.equals(lookingFor)) return true;
        }
        return false;
    }

    private static View searchSingle(View root, List<PathSegment> segments) {
        View current = root;

        for (int i = 1; i < segments.size(); i++) {
            PathSegment segment = segments.get(i);

            if (!(current instanceof ViewGroup)) {
                return null;
            }

            View found = findChildBySegment((ViewGroup) current, segment);
            if (found == null) {
                return null;
            }

            current = found;
        }

        return current;
    }
}
