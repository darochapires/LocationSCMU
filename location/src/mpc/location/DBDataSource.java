package mpc.location;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBDataSource {
	// Database fields
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private String[] allColumns = { DatabaseHelper.COLUMN_ID,
			DatabaseHelper.ACCESSPOINT_ADDRESS };

	public DBDataSource(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public LocationEntry createLocation(String location) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.ACCESSPOINT_ADDRESS, location);
		long insertId = database.insert(DatabaseHelper.TABLE_ACCESSPOINT, null,
				values);
		Cursor cursor = database.query(DatabaseHelper.TABLE_ACCESSPOINT,
				allColumns, DatabaseHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		LocationEntry newLocation = cursorToLocation(cursor);
		cursor.close();
		return newLocation;
	}

//	public void deleteComment(Comment comment) {
//		long id = comment.getId();
//		System.out.println("Comment deleted with id: " + id);
//		database.delete(MySQLiteHelper.TABLE_COMMENTS, MySQLiteHelper.COLUMN_ID
//				+ " = " + id, null);
//	}

	public List<LocationEntry> getAllLocations() {
		List<LocationEntry> locations = new ArrayList<LocationEntry>();

		Cursor cursor = database.query(DatabaseHelper.TABLE_ACCESSPOINT,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			LocationEntry location = cursorToLocation(cursor);
			locations.add(location);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return locations;
	}

	private LocationEntry cursorToLocation(Cursor cursor) {
		LocationEntry location = new LocationEntry();
		location.setId(cursor.getLong(0));
		location.setName(cursor.getString(1));
		return location;
	}
}
