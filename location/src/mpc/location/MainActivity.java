package mpc.location;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
	
	//Método para mostrar a caixa de texto
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
	
	//Método para guardar a localização dada pelo utilizador
	public void save(View view) {
		EditText editText = (EditText) findViewById(R.id.editText1);
		
		//Código manhoso que encontrei na net para esconder o teclado :P
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
			
		//Obter o texto inserido
		String location = editText.getText().toString();
		//Obter os resultados do scan
		manager.startScan();
		List<ScanResult> results = manager.getScanResults();
		
		//Para cada resultado guardar o AP e respectiva leitura
		Iterator<ScanResult> it = results.iterator();
		while (it.hasNext())
		{
			ScanResult next = it.next();
			db.insert_ap(next.BSSID, location, next.SSID, next.level);
		}
		
		//Mensagem de sucesso
		Toast.makeText(getApplicationContext(), location + " successfuly added!", Toast.LENGTH_SHORT).show();
	}
	
	//Método para calcular a localização do utilizador
	public void calc(View view) {
		//Obter os APs
		manager.startScan();
		List<ScanResult> results = manager.getScanResults();
		
		//Mapa para ordenar os APs pela força de sinal (do maior para o mais pequeno)
		Map<Integer, ScanResult> aps = new TreeMap<Integer, ScanResult>();
		aps = sortList(results);
		
		//Mapa para guardar o nº de ocorrência das localizações, ordenado por valor (do maior para o mais pequeno)
		Map<String, Integer> locations = new HashMap<String,Integer>();
        ValueComparator bvc =  new ValueComparator(locations);
		Map<String, Integer> orderedLocations = new TreeMap<String, Integer>(bvc);
		
		//Percorrer os APs
		Iterator<ScanResult> it = aps.values().iterator();
		for (int i = 0; i < aps.size(); i++)
		{
			ScanResult next = it.next();
			//Obter a leitura mais próxima para o AP "next"
			ReadingEntry reading = db.getResults(next.BSSID, next.level);
			//Obter a localização correspondente à leitura
			String loc = reading.getLocation();
			//Se estiver no mapa locations, incrementar o valor
			if (loc != null && locations.containsKey(loc))
			{
				Integer count = (Integer) locations.get(loc);
				count++;
				locations.put(loc, count);
			}
			//Se não estiver no mapa locations, inserir com valor 1
			else
				locations.put(loc, 1);
		}
		
		//Criar o mapa orderedLocations com os valores do mapa locations, para obtermos o mapa ordenado por valor
		orderedLocations.putAll(locations);
		//Obter a localização com maior nº de ocorrências
		String location = orderedLocations.keySet().iterator().next();
		
		//Se não encontrar uma localização dar mensagem de erro
		if (location == null)
			Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
		//Se encontrar, retornar a localização
		else
			Toast.makeText(getApplicationContext(), "You are at room " + location, Toast.LENGTH_SHORT).show();
		
	}
	
	//Método para obter um mapa com os APs ordenados
	private Map<Integer, ScanResult> sortList(List<ScanResult> results) {
		//Criar o mapa com o comparador que ordena o mapa por força de sinal (do maior para o mais pequeno)
		LevelComparator comp = new LevelComparator();
		Map<Integer, ScanResult> map = new TreeMap<Integer, ScanResult>(comp);
		
		//Percorrer os APs da lista
		Iterator<ScanResult> it = results.iterator();
		while (it.hasNext())
		{
			//Para cada AP inserir no mapa
			ScanResult next = it.next();
			map.put(next.level, next);
		}
		return map;
	}
	
	//Comparador para ordenar o mapa com as localizações por valor (do maior para o mais pequeno)
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
	
	//Comparador para ordenar o mapa com os APs por força de sinal (do maior para o mais pequeno)
	class LevelComparator implements Comparator<Integer> {

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
