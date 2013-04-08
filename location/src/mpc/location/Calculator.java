package mpc.location;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.net.wifi.ScanResult;

public class Calculator {

	public static final int EUCLIDEAN = 1;
	public static final int MANHATTAN = 2;

	private DatabaseHelper db;

	public Calculator(DatabaseHelper db) {
		this.db = db;
	}

	public PointEntry strongest_ap(List<ScanResult> results, int algorithm) {
		double min_distance = Double.MAX_VALUE;
		double distance = 0;
		int min_point = 0;
		// Obter os APs

		//Obter leitura ordenada por força de sinal
		List<ScanResult> sortedResults = sortList(results);

		ScanResult strongest = sortedResults.get(0);

		Map<Integer, List<APEntry>> aps_per_point = db.getApPoints(strongest.BSSID);
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

	public PointEntry every_ap(List<ScanResult> results, int algorithm) {
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

	// MÈtodo para obter um mapa com os APs ordenados
	private List<ScanResult> sortList(List<ScanResult> results) {
		// Criar o mapa com o comparador que ordena o mapa por forÁa de sinal
		// (do maior para o mais pequeno)
		LevelComparator comp = new LevelComparator();
		List<ScanResult> list = new LinkedList<ScanResult>();

		Collections.sort(results,comp);

		for( ScanResult result: results){
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
	class LevelComparator implements Comparator<ScanResult> {

		public int compare(ScanResult a, ScanResult b) {
			if (a.level >= b.level) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}

}
