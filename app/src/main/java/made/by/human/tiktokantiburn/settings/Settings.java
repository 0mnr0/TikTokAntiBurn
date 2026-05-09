package made.by.human.tiktokantiburn.settings;

import android.content.SharedPreferences;
import android.content.Context;
import java.util.HashSet;
import java.util.Set;




public final class Settings {

    private Settings() {
    }

    public static final class Service {
        private static final String PREF_NAME = "ServiceSettings";

        private Service() {}

        private static SharedPreferences prefs(Context context) {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }

        public static int getInt(Context context, String key, int defValue) {
            return prefs(context).getInt(key, defValue);
        }

        public static String getString(Context context, String key, String defValue) {
            return prefs(context).getString(key, defValue);
        }

        public static boolean getBool(Context context, String key, boolean defValue) {
            return prefs(context).getBoolean(key, defValue);
        }

        public static long getLong(Context context, String key, long defValue) {
            return prefs(context).getLong(key, defValue);
        }

        public static float getFloat(Context context, String key, float defValue) {
            return prefs(context).getFloat(key, defValue);
        }

        public static Set<String> getStringSet(Context context, String key, Set<String> defValue) {
            Set<String> result = prefs(context).getStringSet(key, defValue);
            return result != null ? new HashSet<>(result) : defValue;
        }

        public static void setInt(Context context, String key, int value) {
            prefs(context).edit().putInt(key, value).apply();
        }

        public static void setString(Context context, String key, String value) {
            prefs(context).edit().putString(key, value).apply();
        }

        public static void setBool(Context context, String key, boolean value) {
            prefs(context).edit().putBoolean(key, value).apply();
        }

        public static void setLong(Context context, String key, long value) {
            prefs(context).edit().putLong(key, value).apply();
        }

        public static void setFloat(Context context, String key, float value) {
            prefs(context).edit().putFloat(key, value).apply();
        }

        public static void setStringSet(Context context, String key, Set<String> value) {
            prefs(context).edit().putStringSet(key, new HashSet<>(value)).apply();
        }

        public static void remove(Context context, String key) {
            prefs(context).edit().remove(key).apply();
        }

        public static void clear(Context context) {
            prefs(context).edit().clear().apply();
        }

        public static boolean contains(Context context, String key) {
            return prefs(context).contains(key);
        }
    }




    public static final class Module {
        private static final String PREF_NAME = "ModuleSettings";

        private Module() {
        }

        private static SharedPreferences prefs(Context context) {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }

        public static int getInt(Context context, String key, int defValue) {
            return prefs(context).getInt(key, defValue);
        }

        public static String getString(Context context, String key, String defValue) {
            return prefs(context).getString(key, defValue);
        }

        public static boolean getBool(Context context, String key, boolean defValue) {
            return prefs(context).getBoolean(key, defValue);
        }

        public static long getLong(Context context, String key, long defValue) {
            return prefs(context).getLong(key, defValue);
        }

        public static float getFloat(Context context, String key, float defValue) {
            return prefs(context).getFloat(key, defValue);
        }

        public static Set<String> getStringSet(Context context, String key, Set<String> defValue) {
            Set<String> result = prefs(context).getStringSet(key, defValue);
            return result != null ? new HashSet<>(result) : defValue;
        }

        public static void setInt(Context context, String key, int value) {
            prefs(context).edit().putInt(key, value).apply();
        }

        public static void setString(Context context, String key, String value) {
            prefs(context).edit().putString(key, value).apply();
        }

        public static void setBool(Context context, String key, boolean value) {
            prefs(context).edit().putBoolean(key, value).apply();
        }

        public static void setLong(Context context, String key, long value) {
            prefs(context).edit().putLong(key, value).apply();
        }

        public static void setFloat(Context context, String key, float value) {
            prefs(context).edit().putFloat(key, value).apply();
        }

        public static void setStringSet(Context context, String key, Set<String> value) {
            prefs(context).edit().putStringSet(key, new HashSet<>(value)).apply();
        }

        public static void remove(Context context, String key) {
            prefs(context).edit().remove(key).apply();
        }

        public static void clear(Context context) {
            prefs(context).edit().clear().apply();
        }

        public static boolean contains(Context context, String key) {
            return prefs(context).contains(key);
        }

    }


    public static final class Iternal {
        private static final String PREF_NAME = "InAppSettings";

        private Iternal() {}

        private static SharedPreferences prefs(Context context) {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }

        public static int getInt(Context context, String key, int defValue) {
            return prefs(context).getInt(key, defValue);
        }

        public static String getString(Context context, String key, String defValue) {
            return prefs(context).getString(key, defValue);
        }

        public static boolean getBool(Context context, String key, boolean defValue) {
            return prefs(context).getBoolean(key, defValue);
        }

        public static long getLong(Context context, String key, long defValue) {
            return prefs(context).getLong(key, defValue);
        }

        public static float getFloat(Context context, String key, float defValue) {
            return prefs(context).getFloat(key, defValue);
        }

        public static Set<String> getStringSet(Context context, String key, Set<String> defValue) {
            Set<String> result = prefs(context).getStringSet(key, defValue);
            return result != null ? new HashSet<>(result) : defValue;
        }

        public static void setInt(Context context, String key, int value) {
            prefs(context).edit().putInt(key, value).apply();
        }

        public static void setString(Context context, String key, String value) {
            prefs(context).edit().putString(key, value).apply();
        }

        public static void setBool(Context context, String key, boolean value) {
            prefs(context).edit().putBoolean(key, value).apply();
        }

        public static void setLong(Context context, String key, long value) {
            prefs(context).edit().putLong(key, value).apply();
        }

        public static void setFloat(Context context, String key, float value) {
            prefs(context).edit().putFloat(key, value).apply();
        }

        public static void setStringSet(Context context, String key, Set<String> value) {
            prefs(context).edit().putStringSet(key, new HashSet<>(value)).apply();
        }

        public static void remove(Context context, String key) {
            prefs(context).edit().remove(key).apply();
        }

        public static void clear(Context context) {
            prefs(context).edit().clear().apply();
        }

        public static boolean contains(Context context, String key) {
            return prefs(context).contains(key);
        }
    }
}