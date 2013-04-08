package mpc.location;

import java.util.Iterator;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class TrackActivity extends Activity {

	WifiManager manager;
	DatabaseHelper db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track);
		manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		db = new DatabaseHelper(this);

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

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.action_loc:
			intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			break;
		case R.id.action_point:
		{
			Toast.makeText(this, "Item já seleccionado", Toast.LENGTH_SHORT).show();
			break;
		}
		case R.id.action_about:
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}

		return true;
	}

	public void scan(View view) {
		// Do something in response to button
		if (manager.isWifiEnabled()) {
			manager.startScan();

		} else { 
			Context context = getApplicationContext();
			CharSequence text = "Liga o WiFi!"; int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration); toast.show();
		}

	}

	public void save(View view) {
		if (!manager.isWifiEnabled())
		{
			Toast.makeText(getApplicationContext(), "Liga o WiFi!", Toast.LENGTH_SHORT).show();
			return;
		}
		else if (manager.getScanResults().isEmpty())
		{	
			Toast.makeText(getApplicationContext(), "Faz scan primeiro", Toast.LENGTH_SHORT).show();
			return;
		}

		EditText editText = (EditText) findViewById(R.id.editText1);

		if (editText.getText().length() == 0)
		{
			Toast.makeText(getApplicationContext(), "Insere uma localização", Toast.LENGTH_SHORT).show();
			return;
		}
		// Código manhoso que encontrei na net para esconder o teclado :P
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

		// Obter o texto inserido
		String point = editText.getText().toString();

		long point_id = db.insert_point(point);

		// Obter os resultados do scan
		List<ScanResult> results = manager.getScanResults();

		// Para cada resultado guardar o AP e respectiva leitura
		Iterator<ScanResult> it = results.iterator();
		while (it.hasNext()) {
			ScanResult next = it.next();
			db.insert_ap(next.BSSID, next.SSID, next.level, point_id);
		}

		editText.setText("");

		// Mensagem de sucesso
		Toast.makeText(getApplicationContext(), point + " successfuly added! " + point_id,
				Toast.LENGTH_SHORT).show();
	}

}
