package mpc.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.util.Pair;

public class DatabaseHelper extends SQLiteOpenHelper {

	private int current_point_id;

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
		current_point_id = 0;
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

	// Método para inserir o AP e a respectiva Leitura
	public void insert_ap(String mac_address, String network_name,
			int strength, int point_id) {

		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put(ACCESSPOINT_MACADDRESS, mac_address);
		values.put(NETWORK_NAME, network_name);
		values.put(POINT_ID, point_id);
		values.put(ACCESSPOINT_STRENGTH, strength);

		// Inserir um novo tuplo para a leitura
		db.insertOrThrow(TABLE_ACCESSPOINT, null, values);
	}

	/**
	 * Esparguete ALERT! 
	 * 1º obter todos os APs da bd que foram obtidos no scan;
	 * 2º obter os pontos onde estes APs contam; 
	 * 3º obter todos os APs destes pontos.
	 */
	public Map<Integer, List<APEntry>> getAPEntriesGivenMac(String mac,
			List<ScanResult> results) {

		Map<Integer, List<APEntry>> result = new HashMap<Integer, List<APEntry>>();

		Cursor ap_cursor = getReadableDatabase().rawQuery(
				"select * from accesspoints where macaddress = ?",
				new String[] { mac });

		// esta list vai ter todos os APs que estao na BD com o mesmo
		// macaddress que os APs do scan, caso constem na BD
		// se se obtiveram resultados...
		if (ap_cursor.moveToFirst()) {
			List<Integer> points = new ArrayList<Integer>();
			for (int i = 0; i < ap_cursor.getCount(); i++) {
				points.add(ap_cursor.getInt(3));
			}

			// 2º obter os pontos onde estes APs constam
			int point_id;
			//String point_constraints = null;
			for (int i = 0; i < points.size(); i++) {
				//ap = itApEntries.next();
				point_id = points.get(i);
				Cursor aps_in_point_cursor = getReadableDatabase()
						.rawQuery(
								"select * from accesspoints where point_id = ?",
								new String[] { point_id + "" });

				if (aps_in_point_cursor.moveToFirst()) {
					List<APEntry> aps_in_point_list = new ArrayList<APEntry>();
					for (int j = 0; j < aps_in_point_cursor.getCount(); j++) {
						APEntry ap_in_point_entry = new APEntry(
								aps_in_point_cursor.getInt(0),
								aps_in_point_cursor.getString(1),
								aps_in_point_cursor.getString(2),
								aps_in_point_cursor.getInt(3),
								aps_in_point_cursor.getInt(4));
						aps_in_point_list.add(ap_in_point_entry);
					}
					result.put(point_id, aps_in_point_list);
				}
			}
		}
		return result;
	}

	public int insert_point(String point) {
		// obtem os pontos com o nome "point"
		Cursor cursor = getReadableDatabase().rawQuery(
				"select * from points where point_name = ?",
				new String[] { point });

		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.clear();

		int id_to_return;
		// if existe...
		if (cursor.moveToFirst()) {
			id_to_return = cursor.getInt(0);
		} else {
			// Se nao existe, guardar em BD
			values.put(POINT_NAME, point);
			db.insertOrThrow(TABLE_POINT, null, values);
			// actualizar o contador do point_id
			current_point_id++;
			id_to_return = current_point_id;
		}
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
}
