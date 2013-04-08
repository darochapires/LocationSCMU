package mpc.location;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.*;

public class MainActivity extends Activity {
	private DatabaseHelper db;
	WifiManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		db = new DatabaseHelper(this);

		manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		IntentFilter i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		getApplicationContext().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent i) {
				TableLayout table = (TableLayout) findViewById(R.id.table);
				table.removeAllViewsInLayout();

				java.util.List<ScanResult> res = manager.getScanResults();
				java.util.Iterator<ScanResult> it = res.iterator();
				while (it.hasNext()) {
					ScanResult r = it.next();
					TableRow row = new TableRow(table.getContext());

					TextView tf = new TextView(table.getContext());
					tf.setText(r.SSID);
					tf.setPadding(0, 0, 20, 2);
					row.addView(tf);

					tf = new TextView(table.getContext());
					tf.setText(r.BSSID);
					tf.setPadding(0, 0, 20, 2);
					row.addView(tf);

					tf = new TextView(table.getContext());
					tf.setText("" + r.level);
					tf.setPadding(0, 0, 20, 2);
					row.addView(tf);

					tf = new TextView(table.getContext());
					tf.setText("" + r.frequency);
					tf.setPadding(0, 0, 20, 2);
					row.addView(tf);

					table.addView(row);
				}
			}
		}, i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		//return true;
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.action_loc:
	      Toast.makeText(this, "Menu Item 1 selected", Toast.LENGTH_SHORT)
	          .show();
	      break;
	    case R.id.action_point:
	      Toast.makeText(this, "Menu item 2 selected", Toast.LENGTH_SHORT)
	          .show();
	    case R.id.action_about:
		      Toast.makeText(this, "Menu item 3 selected", Toast.LENGTH_SHORT)
		          .show();
	      break;

	    default:
	      break;
	    }

	    return true;
	  } 
	/** Called when the user clicks the Scan Now button */
	public void scan(View view) {
		// Do something in response to button
		// if (manager.isWifiEnabled()) {
		manager.startScan();
		/*
		 * } else { Context context = getApplicationContext(); CharSequence text
		 * = "Please, turn the Wifi on."; int duration = Toast.LENGTH_SHORT;
		 * 
		 * Toast toast = Toast.makeText(context, text, duration); toast.show();
		 * }
		 */
	}

	// MÈtodo para mostrar a caixa de texto
	public void insert(View view) {
		EditText editText = (EditText) findViewById(R.id.editText1);
		Button button = (Button) findViewById(R.id.button3);
		if (editText.getVisibility() == View.VISIBLE) {
			editText.setVisibility(View.GONE);
			button.setVisibility(View.GONE);
		} else {
			editText.setVisibility(View.VISIBLE);
			button.setVisibility(View.VISIBLE);
		}
	}

	// MÈtodo para guardar a localizaÁ„o dada pelo utilizador
	public void save(View view) {
		EditText editText = (EditText) findViewById(R.id.editText1);

		// Código manhoso que encontrei na net para esconder o teclado :P
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

		// Obter o texto inserido
		String point = editText.getText().toString();

		int point_id = db.insert_point(point);

		// Obter os resultados do scan
		manager.startScan();
		List<ScanResult> results = manager.getScanResults();

		// Para cada resultado guardar o AP e respectiva leitura
		Iterator<ScanResult> it = results.iterator();
		while (it.hasNext()) {
			ScanResult next = it.next();
			db.insert_ap(next.BSSID, next.SSID, next.level, point_id);
		}

		// Mensagem de sucesso
		Toast.makeText(getApplicationContext(), point + " successfuly added!",
				Toast.LENGTH_SHORT).show();
	}

	// Distancia Euclideana
	public long euclidean(long x1, long x2, long y1, long y2) {

		long xDiff = x1 - x2;
		long xSqr = (long) Math.pow(xDiff, 2);

		long yDiff = y1 - y2;
		long ySqr = (long) Math.pow(yDiff, 2);

		return (long) Math.sqrt(xSqr + ySqr);
	}

	// MÈtodo para calcular a localizaÁ„o do utilizador
	public void calc(View view) {
		double min_distance = Double.MAX_VALUE;
		double distance = 0;
		int min_point = 0;
		// Obter os APs
		manager.startScan();
		List<ScanResult> results = manager.getScanResults();
		//Obter leitura ordenada por força de sinal
		List<ScanResult> sortedResults = sortList(results);

		ScanResult strongest = sortedResults.get(0);
		
		Map<Integer, List<APEntry>> aps_per_point = db.getAPEntriesGivenMac(strongest.BSSID, results);
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
				if (aps.containsKey(result.BSSID)){
					APEntry ap = aps.get(result.BSSID);
					parcels += Math.pow( result.level - ap.getStrength() , 2);
				}
				else
					parcels += Math.pow( result.level - (-120) , 2);
			}
			
			distance = Math.sqrt(parcels);
			if (distance < min_distance)
			{
				min_distance = distance;
				min_point = point_id;
			}
		}
		PointEntry point = db.get_point(min_point);
		Toast.makeText(getApplicationContext(), "You are at room " + point.getName(),
				Toast.LENGTH_SHORT).show();
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

	// Comparador para ordenar o mapa com as localizaÁ„es por valor (do maior
	// para o mais pequeno)
	/*class ValueComparator implements Comparator<String> {

		Map<String, Integer> base;

		public ValueComparator(Map<String, Integer> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with
		// equals.
		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return 1;
			} else {
				return -1;
			} // returning 0 would merge keys
		}
	}*/

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
