package mpc.location;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private int ap_id;

	/*
		public static final String TABLE_LOCATION = "locations";
		public static final String LOCATION_NAME = "name";
	 */

	public static final String COLUMN_ID = "_id";

	public static final String TABLE_ACCESSPOINT = "accesspoints";
	public static final String ACCESSPOINT_ADDRESS = "macaddress";

	public static final String TABLE_READING = "reading";
	public static final String READING_LOCATION = "location";
	public static final String READING_STRENGTH = "strength";
	public static final String READING_NAME = "name";
	public static final String READING_AP_ID = "accesspoint_id";

	private static final String DATABASE_NAME = "accesspoints.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	/*private static final String CREATE_LOCATION = "create table "
			+ TABLE_LOCATION + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + LOCATION_NAME
			+ " text not null);";
	 */

	private static final String CREATE_ACCESSPOINT = "create table "
			+ TABLE_ACCESSPOINT + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + ACCESSPOINT_ADDRESS
			+ " text not null);";

	private static final String CREATE_READING = "create table "
			+ TABLE_READING + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + READING_NAME
			+ " text not null, " + READING_LOCATION
			+ " text not null, " + READING_STRENGTH
			+ " integer, " + READING_AP_ID
			+ " integer);";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		ap_id = 0;
		//database.execSQL(CREATE_LOCATION);
		database.execSQL(CREATE_ACCESSPOINT);
		database.execSQL(CREATE_READING);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCESSPOINT);
		onCreate(db);
	}

	public void insert_ap(Context context, String mac_address, String location, String name, int strength) {
		Cursor cursor = getReadableDatabase().rawQuery("select * from accesspoints where macaddress = ?",
				new String[] { mac_address });

		int found = cursor.getCount(); 
		long ap_to_insert = 0;

		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.clear();

		if (found == 0)
		{
			values.put(ACCESSPOINT_ADDRESS, mac_address);

			db.insertOrThrow(TABLE_ACCESSPOINT, null, values);
			ap_id++;
			ap_to_insert = ap_id;
		}
		else
		{
			cursor.moveToFirst();
			ap_to_insert = cursor.getLong(0);
		}

		values.clear();

		values.put(READING_AP_ID, ap_to_insert);
		values.put(READING_LOCATION, location);
		values.put(READING_NAME, name);
		values.put(READING_STRENGTH, strength);

		db.insertOrThrow(TABLE_READING, null, values);
	}

	public ReadingEntry getResults(String mac, int strength) {
		Cursor ap_cursor = getReadableDatabase().rawQuery("select * from accesspoints where macaddress = ?", new String[] { mac });
		double min_distance = Integer.MAX_VALUE;
		double distance = 0;
		int pos = 0;
		ReadingEntry reading = new ReadingEntry();
		if (ap_cursor.moveToFirst())
		{
			String id = String.valueOf(ap_cursor.getLong(0));

			Cursor read_cursor = getReadableDatabase().
					rawQuery("select * from reading where accesspoint_id = ? order by strength desc",
							new String[] { id });

			if (read_cursor.moveToFirst())
			{
				pos = read_cursor.getPosition();
				while (!read_cursor.isAfterLast() && min_distance > distance)
				{
					distance = Math.sqrt( Math.pow(read_cursor.getInt(3) - strength, 2));
					if (distance < min_distance)
					{
						min_distance = distance;
						pos = read_cursor.getPosition();
					}
					read_cursor.moveToNext();
				}

				read_cursor.moveToPosition(pos);

				reading.setName(read_cursor.getString(read_cursor.getColumnIndex(READING_NAME)));
				reading.setLocation(read_cursor.getString(read_cursor.getColumnIndex(READING_LOCATION)));
				reading.setStrength(read_cursor.getInt(read_cursor.getColumnIndex(READING_STRENGTH)));
				reading.setAp_id(read_cursor.getInt(read_cursor.getColumnIndex(READING_AP_ID)));
			}
		}
		return reading;
	}
}