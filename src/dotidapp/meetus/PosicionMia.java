package dotidapp.meetus;

import dotidapp.meetus.R;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.Toast;

public class PosicionMia {

	static Double latitud;
	static Double longitud;
	static Location localizacion;
	static PosicionMia pos = null;
	private static LocationManager locationManager;
	private static LocationListener gpsLocationListener,
			networkLocationListener;

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
		
		// Network listener

		final LocationListener networkLocationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				longitud = location.getLongitude();
				latitud = location.getLatitude();
				Herramientas.setEncontradoYo(true);
				Herramientas.dibujarPersonaEnMapa();

			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}

		};
		
		gpsLocationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				if (locationManager.getAllProviders().contains(
						networkLocationListener)) {
					locationManager.removeUpdates(networkLocationListener);
				}
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

				// Activamos conx. por network
				try{
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 0,
						networkLocationListener);
				}
				catch(Exception e)
				{
					int a = 0;
				}
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		};

		// Crear Manejador de localizacion
		locationManager = (LocationManager) Herramientas.getContexto()
				.getSystemService(Context.LOCATION_SERVICE);

		// Comprobamos si esta conectado el GPS
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast toast1 = Toast.makeText(
					Herramientas.getContexto(),
					Herramientas.getContexto().getResources()
							.getText(R.string.gpsPosNoEncontrada),
					Toast.LENGTH_SHORT);

			toast1.show();
		}

		// Iniciar conex

		// Probar si tiene conex Network
		try {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0,
					networkLocationListener);

			// Inicio rapido
			Location location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location != null) {
				longitud = location.getLongitude();
				latitud = location.getLatitude();
				Herramientas.setEncontradoYo(true);
			} 
		} catch (Exception e) {

		}
		// Probar si tiene conex GPS
		try {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 3000, 0, gpsLocationListener);
		} catch (Exception e) {

		}

	}

	public static void pararGps() {
		try {
			locationManager.removeUpdates(gpsLocationListener);
			locationManager.removeUpdates(networkLocationListener);
		} catch (Exception e) {

		}

	}

	public static void arrancarGps() {
		// Iniciar conex

				// Probar si tiene conex Network
				try {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER, 0, 0,
							networkLocationListener);

					// Inicio rapido
					Location location = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null) {
						longitud = location.getLongitude();
						latitud = location.getLatitude();
						Herramientas.setEncontradoYo(true);
					} 
				} catch (Exception e) {

				}
				// Probar si tiene conex GPS
				try {
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, 3000, 0, gpsLocationListener);
				} catch (Exception e) {

				}
	}

	private static PosicionMia Posicion() {
		pos = new PosicionMia();

		// TODO

		return pos;

	}

}
