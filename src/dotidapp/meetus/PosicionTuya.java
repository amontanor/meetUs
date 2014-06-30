package dotidapp.meetus;

import android.location.Location;

public class PosicionTuya {

	static Double latitud;
	static Double longitud;
	static Location localizacion;
	static PosicionTuya pos = null;
	


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
	static PosicionTuya obtenerPosicion() {
		if (pos == null) {
			pos = Posicion();
		}
		return pos;
	}

	private static PosicionTuya Posicion() {
		pos = new PosicionTuya();
		return pos;

	}

}
