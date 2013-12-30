package ua.org.cofriends.db.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns {

	private static final String COLUMN_NAMES = "_column_names";

	private static final String VERSION = "_version";

	private static final String DATABASE_NAME = "mydb.db";

	private static final String DATABASE_TABLE = "mytable";

	private static final String DATABASE_CREATE_FORM = "create table " + DATABASE_TABLE + " (" + BaseColumns._ID
			+ " integer primary key autoincrement%s);";

	private final Context mContext;

	private static volatile SQLiteDatabase mDB;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private DatabaseHelper(Context context, int version, DatabaseErrorHandler errorHandler) {
		super(context, DATABASE_NAME, null, version, errorHandler);

		mContext = context;
		mDB = getWritableDatabase();
	}

	private DatabaseHelper(Context context, int version) {
		super(context, DATABASE_NAME, null, version);

		mContext = context;
		mDB = getWritableDatabase();
	}

	/**
	 * @param context
	 *            to use
	 * @return actual instance of db helper
	 */
	public static DatabaseHelper newInstance(Context context) {
		int version = LocalStorage.getInt(VERSION, context);
		if (version < 1) {
			version = 1;
		}

		return new DatabaseHelper(context, version);
	}

	@Override
	public synchronized void close() {
		super.close();

		mDB.close();
	}

	/**
	 * Updates table structure adding column.
	 * 
	 * @param context
	 * @param columnName
	 */
	public static void addNewColumn(Context context, String columnName) {
		int version = LocalStorage.getInt(VERSION, context);
		Bundle bundle = LocalStorage.getBundle(COLUMN_NAMES, context);
		bundle.putString(columnName, ", " + columnName + " text");
		LocalStorage.remove(COLUMN_NAMES, context);
		LocalStorage.putBundle(COLUMN_NAMES, bundle, context);
		LocalStorage.putInt(VERSION, version + 1, context);
	}

	/**
	 * Updates table structure removing column.
	 * 
	 * @param context
	 * @param columnName
	 */
	public static void removeColumn(Context context, String columnName) {
		int version = LocalStorage.getInt(VERSION, context);
		Bundle bundle = LocalStorage.getBundle(COLUMN_NAMES, context);
		bundle.putString(columnName, null);
		LocalStorage.remove(COLUMN_NAMES, context);
		LocalStorage.putBundle(COLUMN_NAMES, bundle, context);
		LocalStorage.putInt(VERSION, version + 1, context);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Bundle bundle = LocalStorage.getBundle(COLUMN_NAMES, mContext);
		StringBuilder columns = new StringBuilder();
		for (String column : bundle.keySet()) {
			columns.append(bundle.getString(column));
		}
		db.execSQL(String.format(DATABASE_CREATE_FORM, columns));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("SQLite", "Updating " + oldVersion + " to " + newVersion);

		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		onCreate(db);
	}

	/**
	 * Queries DB by example.
	 * 
	 * @param example
	 * @return result list
	 */
	public List<String> query(Map<String, String> example) {
		StringBuilder selection = new StringBuilder();
		for (String column : example.keySet()) {
			if (example.get(column) != null && !"".equals(example.get(column))) {
				selection.append("(" + column + " = '" + example.get(column) + "')and");
			}
		}

		Cursor cursor = mDB.query(DATABASE_TABLE, null,
				selection.length() > 3 ? selection.subSequence(0, selection.length() - 3).toString() : null, null,
				null, null, null);
		List<String> result = new ArrayList<String>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			StringBuilder row = new StringBuilder();
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				row.append(cursor.getString(i) + " - ");
			}
			result.add(row.subSequence(0, row.length() - 3).toString());
			cursor.moveToNext();
		}
		cursor.close();

		return result;
	}

	/**
	 * Insert example into DB.
	 * 
	 * @param example
	 */
	public void insert(Map<String, String> example) {
		ContentValues values = new ContentValues();
		for (String column : example.keySet()) {
			values.put(column, example.get(column));
		}

		mDB.insert(DATABASE_TABLE, null, values);
	}

	/**
	 * @return set of present database columns
	 */
	public List<String> getColumns() {
		return new ArrayList<String>(LocalStorage.getBundle(COLUMN_NAMES, mContext).keySet());
	}

	/**
	 * @param example
	 * @param id
	 * @return result set without deleted row
	 */
	public void remove(String id) {
		mDB.delete(DATABASE_TABLE, BaseColumns._ID + " = ?", new String[] { id });
	}
}
