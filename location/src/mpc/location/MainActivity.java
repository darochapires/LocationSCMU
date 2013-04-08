package mpc.location;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final int EUCLIDEAN_ONE = 1;
	public static final int MANHATTAN_ONE = 2;
	public static final int EUCLIDEAN_ALL = 3;
	public static final int MANHATTAN_ALL = 4;
	
	public static final int EUCLIDEAN = 1;
	public static final int MANHATTAN = 2;

	private int selected_algorithm;
	private DatabaseHelper db;
	private Calculator calc;
	
	WifiManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		db = new DatabaseHelper(this);
		calc = new Calculator(db);

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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_loc:
			Toast.makeText(this, "Item já seleccionado", Toast.LENGTH_SHORT).show();
			break;
		case R.id.action_point:
		{
			Intent intent = new Intent(this, TrackActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.action_about:
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}

		return true;
	} 

	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch(view.getId()) {
		case R.id.euclidean1:
			if (checked)
				selected_algorithm = EUCLIDEAN_ONE;
			break;
		case R.id.manhattan1:
			if (checked)
				selected_algorithm = MANHATTAN_ONE;
			break;
		case R.id.euclidean_all:
			if (checked)
				selected_algorithm = EUCLIDEAN_ALL;
			break;
		case R.id.manhattan_all:
			if (checked)
			selected_algorithm = MANHATTAN_ALL;
			break;

		}
	}

	/** Called when the user clicks the Scan Now button */
	public void scan(View view) {
		// Do something in response to button
		if (manager.isWifiEnabled()) {
			manager.startScan();

		} else { 
			Context context = getApplicationContext();
			CharSequence text = "Please, turn the Wifi on."; int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration); toast.show();
		}

	}

	// MÈtodo para calcular a localizaÁ„o do utilizador
	public void calc(View view) {
		if (!manager.isWifiEnabled())
		{
			Toast.makeText(getApplicationContext(), "Liga o WiFi!",	Toast.LENGTH_SHORT).show();
			return;
		}
		else if (manager.getScanResults().isEmpty())
		{			
			Toast.makeText(getApplicationContext(), "Faz scan primeiro", Toast.LENGTH_SHORT).show();
			return;
		}

		List<ScanResult> results = manager.getScanResults();		
		PointEntry point = new PointEntry();
		
		if (selected_algorithm == EUCLIDEAN_ONE)
			point = calc.strongest_ap(results, EUCLIDEAN);			
		else if (selected_algorithm == MANHATTAN_ONE)
			point = calc.strongest_ap(results, MANHATTAN);
		else if (selected_algorithm == EUCLIDEAN_ALL)
			point = calc.every_ap(results, EUCLIDEAN);
		else if (selected_algorithm == MANHATTAN_ALL)
			point = calc.every_ap(results, MANHATTAN);


		
		if (point == null)
			Toast.makeText(getApplicationContext(), "Não foram detectadas localizações", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(getApplicationContext(), "You are at room " + point.getName(), Toast.LENGTH_SHORT).show();
	}
}