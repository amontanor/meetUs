package dotidapp.meetus;

import android.app.Activity;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class PreMapa extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Herramientas.preMapa();
		Intent i;
		if (!Herramientas.getVieneDelMapa()) {
			// Eliminamos la notificacion
			if (Herramientas.getAlertaActiva())
			{
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				//Aqui no tiene la a, se añade
				Herramientas.getTu().setId("a" + Herramientas.getTu().getId());
				int tamano = Herramientas.getTu().id.length();
				mNotificationManager.cancel(new Integer(Herramientas
						.getTu().id.replace("a", "").substring(tamano - 6,
						tamano -1)));
				Herramientas.setAlertaActiva(false);
			}
			if (Herramientas.getElOtroUsuarioCancela()) {
				Herramientas.setElOtroUsuarioCancela(false);
				Toast.makeText(this,
						getResources().getString(R.string.usuarioCancela),
						Toast.LENGTH_LONG).show();
				i = new Intent(PreMapa.this, ListadoUsuariosConectados.class);
			} else {
				i = new Intent(PreMapa.this, MainActivity.class);
				Herramientas.mandarUsuarioAcepta();
			}
		} else {
			i = new Intent(PreMapa.this, ListadoUsuariosConectados.class);
		}
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pre_mapa, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_pre_mapa,
					container, false);
			return rootView;
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Intent i;
		if (!Herramientas.getVieneDelMapa()) {
			i = new Intent(PreMapa.this, MainActivity.class);
		} else {
			i = new Intent(PreMapa.this, ListadoUsuariosConectados.class);
		}
		startActivity(i);
	}

}
