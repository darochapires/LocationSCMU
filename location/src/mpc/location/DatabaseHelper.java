package mpc.location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

	// Metodo que retorna o AP mais pr—ximo dado um MAC Address
	public ReadingEntry getResults(String mac, int strength) {
		// Procurar pelo AP com o MAC Address "mac"
		Cursor ap_cursor = getReadableDatabase().rawQuery(
				"select * from accesspoints where macaddress = ?",
				new String[] { mac });
		// variaveis para o calculo da distância minima
		double min_distance = Integer.MAX_VALUE;
		double distance = 0;
		// Posicao do AP mais pr—ximo encontrado
		int pos = 0;
		ReadingEntry reading = new ReadingEntry();

		// TODO DUVIDA! Caso o AP nao exista, cria-se o AP e a respectiva
		// leitura?

		// Verificar se o MAC Address existe
		if (ap_cursor.moveToFirst()) {
			// ID do AP encontrado
			String id = String.valueOf(ap_cursor.getInt(0));

			// Procurar pelas leituras que correspondam ao AP encontrado
			Cursor read_cursor = getReadableDatabase()
					.rawQuery(
							"select * from reading where accesspoint_id = ? order by strength desc",
							new String[] { id });

			// Verificar se ha leituras
			if (read_cursor.moveToFirst()) {
				// Obter posicao da leitura
				pos = read_cursor.getPosition();
				while (!read_cursor.isAfterLast()) {
					// Calcular a distancia com a euclediana
					distance = Math.sqrt(Math.pow(read_cursor.getInt(3)
							- strength, 2));
					// Verificar se encontramos uma distancia minima
					if (distance < min_distance) {
						// Em caso positivo, guardar o valor como minimo e
						// respectiva posicao
						min_distance = distance;
						pos = read_cursor.getPosition();
					}
					read_cursor.moveToNext();
				}

				// Mover o cursor para a posicao da Leitura minima
				read_cursor.moveToPosition(pos);

				// reading.setName(read_cursor.getString(read_cursor.getColumnIndex(READING_NAME)));
				// reading.setLocation(read_cursor.getString(read_cursor
				// .getColumnIndex(READING_LOCATION)));este
				// reading.setStrength(read_cursor.getInt(read_cursor.getColumnIndex(READING_STRENGTH)));
				// reading.setAp_id(read_cursor.getInt(read_cursor.getColumnIndex(READING_AP_ID)));
			}
		}
		return reading;
	}

	/**
	 * Esparguete ALERT! 
	 * 1º obter todos os APs da bd que foram obtidos no scan;
	 * 2º obter os pontos onde estes APs contam; 
	 * 3º obter todos os APs destes pontos.
	 */
	public List<Pair<PointEntry, List<APEntry>>> getAPEntriesGivenMac(
			List<ScanResult> results) {

		List<Pair<PointEntry, List<APEntry>>> result = new ArrayList<Pair<PointEntry, List<APEntry>>>();

		String constraints = "";
		Iterator<ScanResult> it = results.iterator();

		// se tiver um record...
		if (it.hasNext()) {
			ScanResult s = it.next();
			constraints.concat(ACCESSPOINT_MACADDRESS + " = '" + s.BSSID + "'");

			// se tiver mais records...
			while (it.hasNext()) {
				constraints.concat(" or ");
				s = it.next();
				constraints.concat(ACCESSPOINT_MACADDRESS + " = '" + s.BSSID
						+ "'");
			}
			constraints.concat(";");

			// 1º obter todos os APs da bd que foram obtidos no scan
			Cursor ap_cursor = getReadableDatabase().rawQuery(
					"select * from accesspoints where ?",
					new String[] { constraints });

			// esta list vai ter todos os APs que estao na BD com o mesmo
			// macaddress que os APs do scan, caso constem na BD
			// se se obtiveram resultados...
			if (ap_cursor.moveToFirst()) {
				List<APEntry> apEntries = new ArrayList<APEntry>();
				for (int i = 0; i < ap_cursor.getCount(); i++) {
					APEntry ap = new APEntry(ap_cursor.getInt(0),
							ap_cursor.getString(1), ap_cursor.getString(2),
							ap_cursor.getInt(3), ap_cursor.getInt(4));
					apEntries.add(ap);
				}

				// 2º obter os pontos onde estes APs constam
				Iterator<APEntry> itApEntries = apEntries.iterator();
				// List<PointEntry> points = new ArrayList<PointEntry>();
				APEntry ap;
				int point_id;
				String point_constraints = null;
				if (itApEntries.hasNext()) {
					ap = itApEntries.next();
					point_id = ap.getPointId();
					point_constraints = POINT_ID + " = " + point_id;

					while (itApEntries.hasNext()) {
						ap = itApEntries.next();
						point_id = ap.getPointId();
						point_constraints.concat(" or " + POINT_ID + " = "
								+ point_id);
					}
					point_constraints.concat(";");
					Cursor points_cursor = getReadableDatabase().rawQuery(
							"select * from points where ?",
							new String[] { point_constraints });
					// se se obtiveram resultados...
					if (points_cursor.moveToFirst()) {

						for (int i = 0; i < points_cursor.getCount(); i++) {
							PointEntry p = new PointEntry(
									points_cursor.getInt(0),
									points_cursor.getString(1));

							// 3º obter todos os APs destes pontos
							Cursor aps_in_point_cursor = getReadableDatabase()
									.rawQuery(
											"select * from accesspoints where point_id = ?",
											new String[] { p.getId() + "" });

							if (aps_in_point_cursor.moveToFirst()) {
								List<APEntry> aps_in_point_list = new ArrayList<APEntry>();
								for (int j = 0; j < aps_in_point_cursor
										.getCount(); j++) {
									APEntry ap_in_point_entry = new APEntry(
											aps_in_point_cursor.getInt(0),
											aps_in_point_cursor.getString(1),
											aps_in_point_cursor.getString(2),
											aps_in_point_cursor.getInt(3),
											aps_in_point_cursor.getInt(4));
									aps_in_point_list.add(ap_in_point_entry);
								}
								// par (point,APs desse point)
								Pair<PointEntry, List<APEntry>> pair = new Pair<PointEntry, List<APEntry>>(
										p, aps_in_point_list);
								result.add(pair);
							}
						}
					}
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
}
