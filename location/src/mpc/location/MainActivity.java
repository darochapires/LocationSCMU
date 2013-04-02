package mpc.location;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.*;

public class MainActivity extends Activity
{
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
        getApplicationContext().registerReceiver( new BroadcastReceiver(){
			@Override
			public void onReceive(Context c, Intent i) {
				TableLayout table = (TableLayout) findViewById(R.id.table);
				table.removeAllViewsInLayout();
				
				java.util.List<ScanResult> res = manager.getScanResults();
				java.util.Iterator<ScanResult> it = res.iterator();
				while (it.hasNext()) {
					ScanResult r = it.next();
					TableRow row = new TableRow(table.getContext());

					TextView tf = new TextView( table.getContext());
					tf.setText(r.SSID);
					tf.setPadding(0, 0, 20, 2);
					row.addView(tf);
					
					tf = new TextView( table.getContext());
					tf.setText(r.BSSID);
					tf.setPadding(0, 0, 20, 2);
					row.addView(tf);

					tf = new TextView( table.getContext());
					tf.setText(""+r.level);
					tf.setPadding(0, 0, 20, 2);
					row.addView(tf);

					tf = new TextView( table.getContext());
					tf.setText(""+r.frequency);
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
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/** Called when the user clicks the Scan Now button */
	public void scan(View view) {
	    // Do something in response to button
		//if (manager.isWifiEnabled()) {
			manager.startScan();
		/*}
		else {
			Context context = getApplicationContext();
			CharSequence text = "Please, turn the Wifi on.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}*/
	}
	
	public void insert(View view) {	
		EditText editText = (EditText) findViewById(R.id.editText1);
		Button button = (Button) findViewById(R.id.button3);
		if (editText.getVisibility() == View.VISIBLE) {
			editText.setVisibility(View.GONE);
			button.setVisibility(View.GONE);
		}
		else {
			editText.setVisibility(View.VISIBLE);
			button.setVisibility(View.VISIBLE);
		}
	}
	
	public void save(View view) {
		EditText editText = (EditText) findViewById(R.id.editText1);
		
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
			
		
		String location = editText.getText().toString();
		List<ScanResult> results = manager.getScanResults();
		Iterator<ScanResult> it = results.iterator();
		Context context = getApplicationContext();
		while (it.hasNext())
		{
			ScanResult next = it.next();
			Toast.makeText(getApplicationContext(), "" + next.level, Toast.LENGTH_SHORT).show();
			db.insert_ap(context, next.BSSID, location, next.SSID, next.level);
		}
		
		Toast.makeText(getApplicationContext(), location + " successfuly added!", Toast.LENGTH_SHORT).show();
		
		/*ScanResult result = it.next();
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		APEntry ap = db.getResults(result.BSSID);
		Toast toast = Toast.makeText(context, ap.getMacAddress(), duration);
		toast.show();*/
	}
	
	@SuppressWarnings("unchecked")
	public void calc(View view) {
		List<ScanResult> results = manager.getScanResults();
		
		Map<Integer, ScanResult> aps = new TreeMap<Integer, ScanResult>();
		aps = sortList(results);
		Map<String, Integer> locations = new HashMap<String,Integer>();
        ValueComparator bvc =  new ValueComparator(locations);
		Map<String, Integer> orderedLocations = new TreeMap<String, Integer>(bvc);
		
		Iterator<ScanResult> it = aps.values().iterator();
		
		for (int i = 0; i < 5; i++)
		{
			ScanResult next = it.next();
			ReadingEntry reading = db.getResults(next.BSSID, next.level);
			String loc = reading.getLocation();
			if (locations.containsKey(loc))
			{
				Integer count = (Integer) locations.get(loc);
				count++;
				locations.put(loc, count);
			}
			else
				locations.put(loc, 1);
		}
		
		orderedLocations.putAll(locations);
		String location = orderedLocations.keySet().iterator().next();
		
		if (location == null)
			Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(getApplicationContext(), "You are at room " + location, Toast.LENGTH_SHORT).show();
	}
	
	private Map<Integer, ScanResult> sortList(List<ScanResult> results) {
		LevelComparator comp = new LevelComparator();
		Map<Integer, ScanResult> map = new TreeMap<Integer, ScanResult>(comp);
		Iterator<ScanResult> it = results.iterator();
		while (it.hasNext())
		{
			ScanResult next = it.next();
			map.put(next.level, next);
		}
		return map;
	}
	
	class ValueComparator implements Comparator<String> {

	    Map<String, Integer> base;
	    public ValueComparator(Map<String, Integer> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return 1;
	        } else {
	            return -1;
	        } // returning 0 would merge keys
	    }
	}
	
	class LevelComparator implements Comparator<Integer> {

		/*Map<Integer, ScanResult> base;
	    public LevelComparator(Map<Integer, ScanResult> base) {
	        this.base = base;
	    }
*/

	    	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(Integer a, Integer b) {
	        if (a >= b) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}


}
