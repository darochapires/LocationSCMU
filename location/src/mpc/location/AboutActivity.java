package mpc.location;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		//return true;
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
			intent = new Intent(this, TrackActivity.class);
		    startActivity(intent);
		    break;
		}
		case R.id.action_about:
			Toast.makeText(this, "Item j‡ seleccionado", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}

		return true;
	} 
}
