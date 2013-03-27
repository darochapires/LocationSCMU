package mpc.location;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.*;

public class MainActivity extends Activity
{
	WifiManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
		if (manager.isWifiEnabled()) {
			manager.startScan();
		}
		else {
			Context context = getApplicationContext();
			CharSequence text = "Please, turn the Wifi on.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
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
		String location = editText.getText().toString();
		List<ScanResult> results = manager.getScanResults();
		
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, location, duration);
		toast.show();
	}

}
