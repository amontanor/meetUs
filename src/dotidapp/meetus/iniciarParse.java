package dotidapp.meetus;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.PushService;

public class iniciarParse extends Application {

	@Override
	public void onCreate() {

		super.onCreate();
		Boolean entrar = false;

		// inicializa parse
		Parse.initialize(this, "PuVIKboSUko0q8HRrlVJ0bDl8VYLHyK0ZKt1x2K5",
				"W9qf2khJ8ZwCMOMypxRQU5YnOPuXoF31J7GXF16W");

		// Se recupera por si esta recien reiniciado
		//recuperarIdDesdeBBDD();
		Herramientas.setContexto(this);

		if (!Herramientas.getYo().id.equals("")) {
			// Cargo los usuarios
			Herramientas.recuperarUsuariosBaseDeDatos();
			Herramientas.recuperarIdDesdeBBDD();
			entrar = true;
		}

		if (Herramientas.comprobaciones(this)) {
			// Subscribirse a su propio canal
			if (!PushService.getSubscriptions(this).isEmpty()) {
				Iterator<String> iter = PushService.getSubscriptions(this)
						.iterator();
				while (iter.hasNext()) {
					String canal = iter.next();
					PushService.unsubscribe(this, canal);
				}
			}

			if (entrar) {
				// Lo suscribimos y avisamos que no se haga de nuevo en listadodeusuarios
				Herramientas.setParseInicializado(true);
				PushService.subscribe(this, "a" + Herramientas.getYo().id,
						PreMapa.class);
				Herramientas.setReiniciadoMovil(true);
			}
			// Avisamos que estamos conectados
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						comprobarEstados();
					} catch (Exception e) {
					}
				}
			}).start();
		}
	}

	private void comprobarEstados() {

		for (int i = 0; i < Herramientas.getListaUsuarios().size(); i++) {
			Herramientas.mandarMensajeDeEstadoActual(Herramientas
					.getListaUsuarios().get(i).id);
		}

	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

	private void recuperarIdDesdeBBDD() {
		UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(this,
				"baseDeDatos", null, 1);

		SQLiteDatabase db = usdbh.getReadableDatabase();

		// Si hemos abierto correctamente la base de datos
		if (db != null) {
			// Consultamos el valor Id del usuario

			Cursor c = db.rawQuery(
					"SELECT valor from Configuracion where dato='idUsuario'",
					null);
			// Nos aseguramos de que existe al menos un registro
			if (c.moveToFirst()) {
				do {
					Herramientas.getYo().setId(c.getString(0));
				} while (c.moveToNext());
			}

			c = db.rawQuery(
					"SELECT valor from Configuracion where dato='nombre'", null);
			// Nos aseguramos de que existe al menos un registro
			if (c.moveToFirst()) {
				do {
					Herramientas.getYo().setNombre(c.getString(0));
				} while (c.moveToNext());
			}
			db.close();
		}

	}

}
