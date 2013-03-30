package mpc.location;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String TABLE_LOCATION = "locations";
	public static final String COLUMN_ID = "_id";
	public static final String LOCATION_NAME = "name";

	public static final String TABLE_ACCESSPOINT = "accesspoints";
	public static final String ACCESSPOINT_ID = "_accesspoint_id";
	public static final String ACCESSPOINT_NAME = "name";
	public static final String ACCESSPOINT_LOCATION_ID = "location_id";
	public static final String ACCESSPOINT_STRENGTH = "strength";
	public static final String ACCESSPOINT_ADDRESS = "macaddress";

	private static final String DATABASE_NAME = "location.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String CREATE_TABLE_LOCATION = "create table "
			+ TABLE_LOCATION + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + LOCATION_NAME
			+ " text not null);";

	private static final String CREATE_TABLE_ACCESSPOIMT = "create table "
			+ TABLE_ACCESSPOINT + " (" + COLUMN_ID
			+ " integer primary key autoincrement, " + ACCESSPOINT_NAME
			+ " text not null, " + ACCESSPOINT_ADDRESS + " text not null, "
			+ ACCESSPOINT_STRENGTH + " integer, " + ACCESSPOINT_LOCATION_ID
			+ " integer);";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_LOCATION);
		database.execSQL(CREATE_TABLE_ACCESSPOIMT);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
		onCreate(db);
	}

	// public void insert_location(String location_name, String ap_name, int
	// strength, String address) {
	//
	// }
}