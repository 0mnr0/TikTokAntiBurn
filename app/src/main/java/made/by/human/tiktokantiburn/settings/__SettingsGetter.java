package made.by.human.tiktokantiburn.settings;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;


public class __SettingsGetter {
    private static final String AUTHORITY = "made.by.human.tiktokantiburn.settings";
    private static final String TAG = "[SettingsGetter]";

    private static String getRaw(Context context, String key) {
        if (context == null) {return null;}
        Uri uri = new Uri.Builder()
                .scheme("content")
                .authority(AUTHORITY)
                .appendPath(key)
                .build();
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int col = cursor.getColumnIndex("value");
                if (col >= 0) return cursor.getString(col);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get key: " + key, e);
        }
        return null;
    }

    public static String getString(Context context, String key, String defaultValue) {
        String v = getRaw(context, key);
        return v != null ? v : defaultValue;
    }

    public static int getInt(Context context, String key, int defaultValue) {
        String v = getRaw(context, key);
        if (v == null) return defaultValue;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return defaultValue; }
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        String v = getRaw(context, key);
        if (v == null) return defaultValue;
        return Boolean.parseBoolean(v);
    }

    public static float getFloat(Context context, String key, float defaultValue) {
        String v = getRaw(context, key);
        if (v == null) return defaultValue;
        try { return Float.parseFloat(v); } catch (NumberFormatException e) { return defaultValue; }
    }

    public static long getLong(Context context, String key, long defaultValue) {
        String v = getRaw(context, key);
        if (v == null) return defaultValue;
        try { return Long.parseLong(v); } catch (NumberFormatException e) { return defaultValue; }
    }







    public static void setBoolean(Context context, String key, boolean value) {
        Uri uri = new Uri.Builder()
                .scheme("content")
                .authority(AUTHORITY)
                .appendPath(key)
                .build();
        try {
            ContentValues cv = new ContentValues();
            cv.put("type", "boolean");
            cv.put("value", String.valueOf(value));
            context.getContentResolver().insert(uri, cv);
        } catch (Exception e) {
            Log.w(TAG, "Failed to set boolean for key: " + key, e);
        }
    }



    // SET's
    public static Set<String> getStringSet(Context context, String key, Set<String> defaultValue) {
        String raw = getRaw(context, key);
        if (raw == null) return defaultValue;
        try {
            JSONArray arr = new JSONArray(raw);
            Set<String> result = new HashSet<>();
            for (int i = 0; i < arr.length(); i++) result.add(arr.getString(i));
            return result;
        } catch (JSONException e) {
            Log.w(TAG, "Failed to parse StringSet for key: " + key, e);
            return defaultValue;
        }
    }

    public static boolean putStringSet(Context context, String key, Set<String> value) {
        Uri uri = new Uri.Builder()
                .scheme("content")
                .authority(AUTHORITY)
                .appendPath(key)
                .build();
        try {
            JSONArray arr = new JSONArray(value);
            ContentValues cv = new ContentValues();
            cv.put("type", "stringset");
            cv.put("value", arr.toString());
            return context.getContentResolver().insert(uri, cv) != null;
        } catch (Exception e) {
            Log.w(TAG, "Failed to set StringSet for key: " + key, e);
            return false;
        }
    }



}