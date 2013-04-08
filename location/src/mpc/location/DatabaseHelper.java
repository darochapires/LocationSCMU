package mpc.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.ScanResult;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String COLUMN_ID = "_id";

	public static final String TABLE_ACCESSPOINT = "accesspoints";
	public static final String ACCESSPOINT_MACADDRESS = "macaddress";
	public static final String NETWORK_NAME = "network_name";
	public static final String POINT_ID = "point_id";
	public static final String ACCESSPOINT_STRENGTH = "strength";

	public static final String TABLE_POINT = "points";
	public static final String POINT_NAME = "point_name";

	private static final String DATABASE_NAME = "accesspoints.db";
	private static final int DATABASE_VERSION = 1;

	private static final String CREATE_ACCESSPOINT = "create table "
			+ TABLE_ACCESSPOINT + " ( " + COLUMN_ID
			+ " integer primary key autoincrement, " + ACCESSPOINT_MACADDRESS
			+ " text not null, " + NETWORK_NAME + " text not null, " + POINT_ID
			+ " integer, " + ACCESSPOINT_STRENGTH + " integer);";

	private static final String CREATE_POINT = "create table " + TABLE_POINT
			+ " (" + COLUMN_ID + " integer primary key autoincrement, "
			+ POINT_NAME + " text not null);";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_ACCESSPOINT);
		db.execSQL(CREATE_POINT);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCESSPOINT);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_POINT);
		onCreate(db);
	}

	public long insert_point(String point) {
		// obtem os pontos com o nome "point"
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();

		// Se nao existe, guardar em BD
		values.put(POINT_NAME, point);
		long id_to_return = db.insertOrThrow(TABLE_POINT, null, values);

		return id_to_return;
	}

	public PointEntry get_point(int id) {
		Cursor cursor = getReadableDatabase().rawQuery("select * from points where _id = ?", new String[] { id + "" });
		PointEntry point = new PointEntry();
		if (cursor.moveToFirst())
		{	
			point.setId(cursor.getInt(0));
			point.setName(cursor.getString(1));
		}
		else
			point = null;
		return point;
	}

	// Método para inserir o AP e a respectiva Leitura
	public void insert_ap(String mac_address, String network_name,
			int strength, long point_id) {

		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put(ACCESSPOINT_MACADDRESS, mac_address);
		values.put(NETWORK_NAME, network_name);
		values.put(POINT_ID, point_id);
		values.put(ACCESSPOINT_STRENGTH, strength);

		// Inserir um novo tuplo para a leitura
		db.insertOrThrow(TABLE_ACCESSPOINT, null, values);
	}

	public Map<Integer, List<APEntry>> getApPoints(String mac, int p_id) {

		Map<Integer, List<APEntry>> result = new HashMap<Integer, List<APEntry>>();

		Cursor ap_cursor = getReadableDatabase().rawQuery(
				"select * from accesspoints where macaddress = ? ;",
				new String[] { mac });

		// esta list vai ter todos os APs que estao na BD com o mesmo
		// macaddress que os APs do scan, caso constem na BD
		// se se obtiveram resultados...
		if (ap_cursor.moveToFirst()) {
			while (!ap_cursor.isAfterLast())
			{
				int point_id = ap_cursor.getInt(3);
				if (p_id == -1 || point_id != p_id) 
				{
					ap_cursor.moveToNext();

					Cursor aps_in_point_cursor = getReadableDatabase().rawQuery(
							"select * from accesspoints where point_id = ? ;", new String[] { point_id + "" });

					if (aps_in_point_cursor.moveToFirst()) {
						List<APEntry> aps_in_point_list = new ArrayList<APEntry>();
						while (!aps_in_point_cursor.isAfterLast())
						{
							APEntry ap_in_point_entry = new APEntry(
									aps_in_point_cursor.getInt(0),
									aps_in_point_cursor.getString(1),
									aps_in_point_cursor.getString(2),
									aps_in_point_cursor.getInt(3),
									aps_in_point_cursor.getInt(4));
							aps_in_point_list.add(ap_in_point_entry);
							aps_in_point_cursor.moveToNext();
						}
						result.put(point_id, aps_in_point_list);
					}
				}
			}
		}
		return result;
	}

	public Map<Integer, List<APEntry>> getApsPoints(List<ScanResult> results) {

		Map<Integer, List<APEntry>> result = new HashMap<Integer, List<APEntry>>();

		for (int i = 0; i < results.size(); i++)
		{
			Cursor ap_cursor = getReadableDatabase().rawQuery(
					"select * from accesspoints where macaddress = ? ;", new String[] { results.get(i).BSSID });

			// esta list vai ter todos os APs que estao na BD com o mesmo
			// macaddress que os APs do scan, caso constem na BD
			// se se obtiveram resultados...
			if (ap_cursor.moveToFirst()) {
				while (!ap_cursor.isAfterLast())
				{
					int point_id = ap_cursor.getInt(3);
					ap_cursor.moveToNext();
					Cursor aps_in_point_cursor = getReadableDatabase().rawQuery(
							"select * from accesspoints where point_id = ?", new String[] { point_id + "" });

					if (aps_in_point_cursor.moveToFirst()) {
						List<APEntry> aps_in_point_list = new ArrayList<APEntry>();
						while (!aps_in_point_cursor.isAfterLast())
						{
							APEntry ap_in_point_entry = new APEntry(
									aps_in_point_cursor.getInt(0),
									aps_in_point_cursor.getString(1),
									aps_in_point_cursor.getString(2),
									aps_in_point_cursor.getInt(3),
									aps_in_point_cursor.getInt(4));
							aps_in_point_list.add(ap_in_point_entry);
							aps_in_point_cursor.moveToNext();
						}
						result.put(point_id, aps_in_point_list);
					}
				}
			}
		}
		return result;
	}

	public List<APEntry> getPointAps(int point_id) {

		List<APEntry> result = new LinkedList<APEntry>();

		Cursor aps_in_point_cursor = getReadableDatabase().rawQuery(
				"select * from accesspoints where point_id = ?", new String[] { point_id + "" });

		if (aps_in_point_cursor.moveToFirst()) {
			List<APEntry> aps_in_point_list = new ArrayList<APEntry>();
			while (!aps_in_point_cursor.isAfterLast())
			{
				APEntry ap_in_point_entry = new APEntry(
						aps_in_point_cursor.getInt(0),
						aps_in_point_cursor.getString(1),
						aps_in_point_cursor.getString(2),
						aps_in_point_cursor.getInt(3),
						aps_in_point_cursor.getInt(4));
				aps_in_point_list.add(ap_in_point_entry);
				aps_in_point_cursor.moveToNext();
				result.add(ap_in_point_entry);
			}
		}
		return result;
	}

	public Map<Integer, List<APEntry>> getPointsAps(List<APEntry> aps, int p_id)
	{
		Map<Integer, List<APEntry>> result = new HashMap<Integer, List<APEntry>>();

		for (int i = 0; i < aps.size(); i++)
		{
			Cursor ap_cursor = getReadableDatabase().rawQuery(
					"select * from accesspoints where macaddress = ? ;", new String[] { aps.get(i).getMac_address() });

			// esta list vai ter todos os APs que estao na BD com o mesmo
			// macaddress que os APs do scan, caso constem na BD
			// se se obtiveram resultados...
			if (ap_cursor.moveToFirst()) {
				while (!ap_cursor.isAfterLast())
				{
					int point_id = ap_cursor.getInt(3);
					if (point_id != p_id)
					{
						ap_cursor.moveToNext();
						Cursor aps_in_point_cursor = getReadableDatabase().rawQuery(
								"select * from accesspoints where point_id = ?", new String[] { point_id + "" });

						if (aps_in_point_cursor.moveToFirst()) {
							List<APEntry> aps_in_point_list = new ArrayList<APEntry>();
							while (!aps_in_point_cursor.isAfterLast())
							{
								APEntry ap_in_point_entry = new APEntry(
										aps_in_point_cursor.getInt(0),
										aps_in_point_cursor.getString(1),
										aps_in_point_cursor.getString(2),
										aps_in_point_cursor.getInt(3),
										aps_in_point_cursor.getInt(4));
								aps_in_point_list.add(ap_in_point_entry);
								aps_in_point_cursor.moveToNext();
							}
							result.put(point_id, aps_in_point_list);
						}
					}
				}
			}
		}
		return result;
	}


}