package ua.org.cofriends.db.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

public class LocalStorage {

	private static final String BUNDLE_REF_KEY = "bundle_ref";
	private static final String NAME = "local_storage";
	
	public static boolean contains(String key, Context context) {
		return context.getSharedPreferences(NAME, Context.MODE_PRIVATE).contains(key);
	}
	
	public static boolean remove(String key, Context context) {
		Editor editor = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
		editor.remove(key);
		return editor.commit();
	}

	public static boolean putString(String key, String value, Context context) {
		Editor editor = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
		editor.putString(key, value == null ? "" : value);
		return editor.commit();
	}

	public static String getString(String key, Context context) {
		return context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getString(key, null);
	}

	public static boolean putFloat(String key, float value, Context context) {
		Editor editor = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
		editor.putFloat(key, value);
		return editor.commit();
	}

	public static float getFloat(String key, Context context) {
		return context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getFloat(key, 0);
	}

	public static boolean putInt(String key, int value, Context context) {
		Editor editor = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
		editor.putInt(key, value);
		return editor.commit();
	}

	public static int getInt(String key, Context context) {
		return context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getInt(key, 0);
	}

	public static boolean putBoolean(String key, boolean value, Context context) {
		Editor editor = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
		editor.putBoolean(key, value);
		return editor.commit();
	}

	public static boolean getBoolean(String key, Context context) {
		return context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getBoolean(key, false);
	}

	public static boolean putBundle(String prefix, Bundle bundle, Context context) {
		Editor editor = context.getSharedPreferences(prefix + NAME, Context.MODE_PRIVATE).edit();
		Iterator<String> iterator = bundle.keySet().iterator();

		while (iterator.hasNext()) {
			String key = iterator.next();
			Object value = bundle.get(key);
			if (value == null) {
				editor.remove(key);
			} else if (value instanceof Integer) {
				editor.putInt(key, (Integer) value);
			} else if (value instanceof Long) {
				editor.putLong(key, (Long) value);
			} else if (value instanceof Boolean) {
				editor.putBoolean(key, (Boolean) value);
			} else if (value instanceof CharSequence) {
				editor.putString(key, ((CharSequence) value).toString());
			} else if (value instanceof Bundle) {
				editor.putString(BUNDLE_REF_KEY, prefix + key);
				putBundle(prefix + key, ((Bundle) value), context);
			}
		}

		return editor.commit();
	}
	
	public static Bundle getBundle(String prefix, Context context) {
		Map<String, ?> all = context.getSharedPreferences(prefix + NAME, Context.MODE_PRIVATE).getAll();
		Bundle bundle = new Bundle();

		for (Entry<String, ?> entry : all.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Integer) {
				bundle.putInt(key, (Integer) value);
			} else if (value instanceof Long) {
				bundle.putLong(key, (Long) value);
			} else if (value instanceof Boolean) {
				bundle.putBoolean(key, (Boolean) value);
			} else if (value instanceof String) {
				bundle.putString(key, (String) value);
				if (BUNDLE_REF_KEY.equals(key)) {
					bundle.putBundle((String) value, getBundle(prefix + value, context));
				}
			}
		}

		return bundle;
	}
}