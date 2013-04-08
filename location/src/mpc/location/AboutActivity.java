package mpc.location;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		TextView text = (TextView) findViewById(R.id.text_about);
		text.setText(Html.fromHtml(getString(R.string.about)));
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
