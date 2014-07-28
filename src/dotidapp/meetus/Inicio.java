package dotidapp.meetus;

import dotidapp.meetus.R;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

public class Inicio extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_inicio);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		Boolean seguir = Herramientas.comprobaciones(this);
		
		if (seguir)
		{
		Boolean esLaPrimeraVez = comprobarSiEsLaPrimeraVez();
		if (esLaPrimeraVez) {
			Herramientas.cambiarEsLaPrimeraVezANo(Inicio.this);
			Intent i = new Intent(this, Login.class);
			esperarDosSegundos(i);
		} else {
			Herramientas.recuperarUsuariosBaseDeDatos();
			Herramientas.recuperarIdDesdeBBDD();
			Intent i = new Intent(this,
					ListadoUsuariosConectados.class);
			esperarDosSegundos(i);
		}
		}
		else
		{
			esperarCuatroSegundosSalir();
		}
	}

	

	@Override
	protected void onRestart() {
		super.onRestart();
		Intent i = new Intent(this,
				ListadoUsuariosConectados.class);
	}

	

	//Tiempo que se muestra al inicio el logo
	private void esperarDosSegundos(final Intent i) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
					startActivity(i);
				} catch (Exception e) {

				}
			}
		}).start();
		
	}
	
	//Tiempo que se muestra al inicio el logo
		private void esperarCuatroSegundosSalir() {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(4000);
						Intent finishapp = new Intent(Intent.ACTION_MAIN);
						finishapp.addCategory(Intent.CATEGORY_HOME);
						finishapp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(finishapp);
					} catch (Exception e) {

					}
				}
			}).start();
			
		}


	private Boolean comprobarSiEsLaPrimeraVez() {

		// Abrimos la base de datos en modo lectura
		UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(this,
				"baseDeDatos", null, 1);

		SQLiteDatabase db = usdbh.getReadableDatabase();

		String valorEsLaPrimeraVez = "n";

		// Si hemos abierto correctamente la base de datos
		if (db != null) {
			// Consultamos el valor esLaPrimeraVez
			Cursor c = db
					.rawQuery(
							"SELECT valor from Configuracion where dato = \"esLaPrimeraVez\"",
							null);
			// Nos aseguramos de que existe al menos un registro
			if (c.moveToFirst()) {
				valorEsLaPrimeraVez = c.getString(0);
			}
			db.close();
		}

		return valorEsLaPrimeraVez.equals("s");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.inicio, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_inicio,
					container, false);
			return rootView;
		}
	}

}
