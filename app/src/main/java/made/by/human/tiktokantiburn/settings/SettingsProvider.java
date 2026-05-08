package made.by.human.tiktokantiburn.settings;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

public class SettingsProvider extends ContentProvider {

    public static final String AUTHORITY = "made.by.human.tiktokantiburn.settings";
    public static final String COL_VALUE = "value";

    private static final String PREFS_NAME = "ModuleSettings";
    private static final String TAG = "[SettingsProvider]";

    private SharedPreferences prefs;

    @Override
    public boolean onCreate() {
        prefs = getContext().getSharedPreferences(PREFS_NAME, 0);
        Log.w(TAG, "SettingsProvider created");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String key = uri.getLastPathSegment();
        if (key == null || !prefs.contains(key)) {
            Log.w(TAG, "Key not found: " + key);
            return null;
        }

        MatrixCursor cursor = new MatrixCursor(new String[]{COL_VALUE});
        Object value = prefs.getAll().get(key);
        String serialized;
        if (value instanceof Set) {
            serialized = new JSONArray((Set<?>) value).toString();
        } else {
            serialized = String.valueOf(value);
        }
        cursor.addRow(new Object[]{serialized});
        return cursor;
    }

    // Остальные методы ContentProvider — не нужны, но обязательны
    @Nullable @Override
    public String getType(@NonNull Uri uri) { return null; }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values == null) return null;
        String key = uri.getLastPathSegment();
        String type = values.getAsString("type");
        String value = values.getAsString("value");
        if (key == null || value == null) return null;

        SharedPreferences.Editor editor = prefs.edit();
        if ("stringset".equals(type)) {
            try {
                JSONArray arr = new JSONArray(value);
                Set<String> set = new HashSet<>();
                for (int i = 0; i < arr.length(); i++) set.add(arr.getString(i));
                editor.putStringSet(key, set);
            } catch (JSONException e) {
                Log.w(TAG, "Failed to parse StringSet for key: " + key, e);
                return null;
            }
        } else if ("boolean".equals(type)) {
            editor.putBoolean(key, Boolean.parseBoolean(value));
        }
        editor.apply();
        return uri;
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) { return 0; }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues v, @Nullable String s, @Nullable String[] a) { return 0; }
}