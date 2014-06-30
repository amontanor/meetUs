package dotidapp.meetus;

import dotidapp.meetus.R;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class PosicionMia {

	static Double latitud;
	static Double longitud;
	static Location localizacion;
	static PosicionMia pos = null;
	private static LocationManager locationManager;
	private static LocationListener locListener;

	public Double getLatitud() {
		return latitud;
	}

	public void setLatitud(Double latitud) {
		this.latitud = latitud;
	}

	public Double getLongitud() {
		return longitud;
	}

	public void setLongitud(Double longitud) {
		this.longitud = longitud;
	}

	// Metodo que crea el objeto Posicion, y guarda la longitud y latitud
	static void inicializarMia() {
		// Creamos el locListener y sus eventos
		longitud = null;
		latitud = null;
		locListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				longitud = location.getLongitude();
				latitud = location.getLatitude();
				Herramientas.setEncontradoYo(true);
				Herramientas.dibujarPersonaEnMapa();
			}
			@Override
			public void onProviderDisabled(String provider) {
				Toast toast1 = Toast.makeText(
						Herramientas.getContexto(),
						Herramientas.getContexto().getResources()
								.getText(R.string.conx_perdida),
						Toast.LENGTH_SHORT);

				toast1.show();
			}
			@Override
			public void onProviderEnabled(String provider) {
				/*Toast toast1 = Toast.makeText(
						Herramientas.getContexto(),
						Herramientas.getContexto().getResources()
								.getText(R.string.gpsProveedorOn),
						Toast.LENGTH_SHORT);

				toast1.show();*/
			}
			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				/*Toast toast1 = Toast.makeText(
						Herramientas.getContexto(),
						Herramientas.getContexto().getResources()
								.getText(R.string.gpsCambioStatus)
								+ String.valueOf(status), Toast.LENGTH_SHORT);

				toast1.show();*/
			}
		};

		// Crear Manejador de localizacion
		locationManager = (LocationManager) Herramientas.getContexto()
				.getSystemService(Context.LOCATION_SERVICE);
		
		//Comprobamos si esta conectado el GPS
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast toast1 = Toast.makeText(
					Herramientas.getContexto(),
					Herramientas.getContexto().getResources()
							.getText(R.string.gpsPosNoEncontrada),
					Toast.LENGTH_SHORT);

			toast1.show();
		}
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
	}

	public static void pararGps(){
		if (!locationManager.getAllProviders().isEmpty())
		{
			locationManager.removeUpdates(locListener);
		}
	}
	
	public static void arrancarGps(){
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
	}
	
	private static PosicionMia Posicion() {
		pos = new PosicionMia();

		// TODO

		return pos;

	}

}
