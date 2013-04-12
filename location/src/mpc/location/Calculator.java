package mpc.location;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.util.Log;

public class Calculator {
	
	public static final int EUCLIDEAN_ONE = 1;
	public static final int MANHATTAN_ONE = 2;
	public static final int EUCLIDEAN_ALL = 3;
	public static final int MANHATTAN_ALL = 4;

	public static final int EUCLIDEAN = 1;
	public static final int MANHATTAN = 2;

	private DatabaseHelper db;

	public Calculator(DatabaseHelper db) {
		this.db = db;
	}

	public PointEntry strongest_scan_ap(List<ScanResult> results, int algorithm) {
		double min_distance = Double.MAX_VALUE;
		double distance = 0;
		int min_point = 0;
		// Obter os APs

		//Obter leitura ordenada por força de sinal
		List<ScanResult> sortedResults = sortList(results);

		ScanResult strongest = sortedResults.get(0);

		Map<Integer, List<APEntry>> aps_per_point = db.getApPoints(strongest.BSSID, -100);
		Iterator<Integer> point_it = aps_per_point.keySet().iterator();

		while (point_it.hasNext())
		{
			int point_id = point_it.next();

			Map<String, APEntry> aps = sortByMac(aps_per_point.get(point_id));
			Iterator<ScanResult> results_it = results.iterator();

			double parcels = 0;
			while (results_it.hasNext())
			{
				ScanResult result = results_it.next();
				if (aps.containsKey(result.BSSID))
				{
					APEntry ap = aps.get(result.BSSID);
					if (algorithm == EUCLIDEAN)
						parcels += Math.pow( result.level - ap.getStrength() , 2);
					else if (algorithm == MANHATTAN)
						parcels += Math.abs(result.level - ap.getStrength());
				}
				else
				{
					if (algorithm == EUCLIDEAN)
						parcels += Math.pow( result.level - (-120) , 2);
					else if (algorithm == MANHATTAN)
						parcels += Math.abs(result.level - (-120));
				}
			}	


			if (algorithm == EUCLIDEAN)
				distance = Math.sqrt(parcels);
			else if (algorithm == MANHATTAN)
				distance = parcels;
			
			
			
			if (distance < min_distance)
			{
				min_distance = distance;
				min_point = point_id;
			}
		}

		PointEntry point = db.get_point(min_point);
		return point;

	}

	public PointEntry every_scan_ap(List<ScanResult> results, int algorithm) {
		double min_distance = Double.MAX_VALUE;
		double distance = 0;
		int min_point = 0;
		// Obter os APs

		Map<Integer, List<APEntry>> aps_per_point = db.getApsPoints(results);
		Iterator<Integer> point_it = aps_per_point.keySet().iterator();

		while (point_it.hasNext())
		{
			int point_id = point_it.next();

			Map<String, APEntry> aps = sortByMac(aps_per_point.get(point_id));
			Iterator<ScanResult> results_it = results.iterator();

			double parcels = 0;
			while (results_it.hasNext())
			{
				ScanResult result = results_it.next();
				if (aps.containsKey(result.BSSID))
				{
					APEntry ap = aps.get(result.BSSID);
					if (algorithm == EUCLIDEAN)
						parcels += Math.pow( result.level - ap.getStrength() , 2);
					else if (algorithm == MANHATTAN)
						parcels += Math.abs(result.level - ap.getStrength());
				}
				else
				{
					if (algorithm == EUCLIDEAN)
						parcels += Math.pow( result.level - (-120) , 2);
					else if (algorithm == MANHATTAN)
						parcels += Math.abs(result.level - (-120));
				}
			}	


			if (algorithm == EUCLIDEAN)
				distance = Math.sqrt(parcels);
			else if (algorithm == MANHATTAN)
				distance = parcels;

			if (distance < min_distance)
			{
				min_distance = distance;
				min_point = point_id;
			}
		}

		PointEntry point = db.get_point(min_point);
		return point;
	}

	public double accuracy(int choose_algorithm, int distance_algorithm) {
		double count = 0;
		double accuracy = 0;
		Cursor point_cursor = db.getReadableDatabase().rawQuery("select * from points", null);
		if (point_cursor.moveToFirst())
		{
			while (!point_cursor.isAfterLast())
			{
				int point_id = point_cursor.getInt(0);
				List<APEntry> aps = db.getPointAps(point_id);
				point_cursor.moveToNext();
				PointEntry calculated_point = new PointEntry();
				
				Log.d("Calculator", "Starting algorithm " + count);
				if (choose_algorithm == EUCLIDEAN_ONE)
					calculated_point = strongest_point_ap(aps, EUCLIDEAN, point_id);			
				else if (choose_algorithm == MANHATTAN_ONE)
					calculated_point = strongest_point_ap(aps, MANHATTAN, point_id);
				else if (choose_algorithm == EUCLIDEAN_ALL)
					calculated_point = every_point_ap(aps, EUCLIDEAN, point_id);
				else if (choose_algorithm == MANHATTAN_ALL)
					calculated_point = every_point_ap(aps, MANHATTAN, point_id);
				
				PointEntry point = db.get_point(point_id);
				if (calculated_point != null && calculated_point.getName().equals(point.getName()))
					accuracy++;
				count++;
			}
		}


		return accuracy/count;
	}

	private PointEntry strongest_point_ap(List<APEntry> results, int algorithm, int p_id) {
		double min_distance = Double.MAX_VALUE;
		double distance = 0;
		int min_point = 0;
		// Obter os APs

		//Obter leitura ordenada por força de sinal
		List<APEntry> sortedResults = sortPointList(results);

		APEntry strongest = sortedResults.get(0);

		Map<Integer, List<APEntry>> aps_per_point = db.getApPoints(strongest.getMac_address(), p_id);
		Iterator<Integer> point_it = aps_per_point.keySet().iterator();

		while (point_it.hasNext())
		{
			int point_id = point_it.next();

			Map<String, APEntry> aps = sortByMac(aps_per_point.get(point_id));
			Iterator<APEntry> results_it = results.iterator();

			double parcels = 0;
			while (results_it.hasNext())
			{
				Log.d("Calculator", "Calculating parcels");
				APEntry result = results_it.next();
				if (aps.containsKey(result.getMac_address()))
				{
					APEntry ap = aps.get(result.getMac_address());
					if (algorithm == EUCLIDEAN)
						parcels += Math.pow(result.getStrength() - ap.getStrength() , 2);
					else if (algorithm == MANHATTAN)
						parcels += Math.abs(result.getStrength() - ap.getStrength());
				}
				else
				{
					if (algorithm == EUCLIDEAN)
						parcels += Math.pow(result.getStrength() - (-120) , 2);
					else if (algorithm == MANHATTAN)
						parcels += Math.abs(result.getStrength() - (-120));
				}
			}	


			if (algorithm == EUCLIDEAN)
				distance = Math.sqrt(parcels);
			else if (algorithm == MANHATTAN)
				distance = parcels;

			if (distance < min_distance)
			{
				min_distance = distance;
				min_point = point_id;
			}
		}

		PointEntry point = db.get_point(min_point);
		return point;

	}

	private PointEntry every_point_ap(List<APEntry> results, int algorithm, int p_id) {
		double min_distance = Double.MAX_VALUE;
		double distance = 0;
		int min_point = 0;
		// Obter os APs

		Map<Integer, List<APEntry>> aps_per_point = db.getPointsAps(results, p_id);
		Iterator<Integer> point_it = aps_per_point.keySet().iterator();

		while (point_it.hasNext())
		{
			int point_id = point_it.next();

			Map<String, APEntry> aps = sortByMac(aps_per_point.get(point_id));
			Iterator<APEntry> results_it = results.iterator();

			double parcels = 0;
			while (results_it.hasNext())
			{
				APEntry result = results_it.next();
				if (aps.containsKey(result.getMac_address()))
				{
					APEntry ap = aps.get(result.getMac_address());
					if (algorithm == EUCLIDEAN)
						parcels += Math.pow(result.getStrength() - ap.getStrength() , 2);
					else if (algorithm == MANHATTAN)
						parcels += Math.abs(result.getStrength() - ap.getStrength());
				}
				else
				{
					if (algorithm == EUCLIDEAN)
						parcels += Math.pow(result.getStrength() - (-120) , 2);
					else if (algorithm == MANHATTAN)
						parcels += Math.abs(result.getStrength() - (-120));
				}
			}	


			if (algorithm == EUCLIDEAN)
				distance = Math.sqrt(parcels);
			else if (algorithm == MANHATTAN)
				distance = parcels;

			if (distance < min_distance)
			{
				min_distance = distance;
				min_point = point_id;
			}
		}

		PointEntry point = db.get_point(min_point);
		return point;
	}

	// MÈtodo para obter um mapa com os APs ordenados
	private List<ScanResult> sortList(List<ScanResult> results) {
		// Criar o mapa com o comparador que ordena o mapa por forÁa de sinal
		// (do maior para o mais pequeno)
		LevelScanComparator comp = new LevelScanComparator();
		List<ScanResult> list = new LinkedList<ScanResult>();

		Collections.sort(results,comp);

		for( ScanResult result: results){
			list.add(result);
		} 
		return list;
	}

	private List<APEntry> sortPointList(List<APEntry> results) {
		// Criar o mapa com o comparador que ordena o mapa por forÁa de sinal
		// (do maior para o mais pequeno)
		LevelPointComparator comp = new LevelPointComparator();
		List<APEntry> list = new LinkedList<APEntry>();

		Collections.sort(results,comp);

		for( APEntry result: results){
			list.add(result);
		} 
		return list;
	}
	
	private Map<String, APEntry> sortByMac(List<APEntry> results) {
		Map<String, APEntry> map = new HashMap<String, APEntry>();
		Iterator<APEntry> it = results.iterator();
		while (it.hasNext())
		{
			APEntry next = it.next();
			map.put(next.getMac_address(), next);
		}
		return map;
	}

	// Comparador para ordenar o mapa com os APs por forÁa de sinal (do maior
	// para o mais pequeno)
	class LevelScanComparator implements Comparator<ScanResult> {

		public int compare(ScanResult a, ScanResult b) {
			if (a.level >= b.level) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}
	
	// Comparador para ordenar o mapa com os APs por forÁa de sinal (do maior
		// para o mais pequeno)
		class LevelPointComparator implements Comparator<APEntry> {

			public int compare(APEntry a, APEntry b) {
				if (a.getStrength() >= b.getStrength()) {
					return -1;
				} else {
					return 1;
				} // returning 0 would merge keys
			}
		}

}
