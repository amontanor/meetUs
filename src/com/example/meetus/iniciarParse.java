package com.example.meetus;

import com.parse.Parse;
import com.parse.PushService;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class iniciarParse extends Application {

	@Override
	public void onCreate() {

		super.onCreate();

		// Se recupera por si esta recien reiniciado
		recuperarIdDesdeBBDD();

		if (Herramientas.comprobaciones(getApplicationContext())
				&& !Herramientas.getYo().id.equals("")) {

			Herramientas.setParseInicializado(true);
			Parse.initialize(getApplicationContext(),
					"PuVIKboSUko0q8HRrlVJ0bDl8VYLHyK0ZKt1x2K5",
					"W9qf2khJ8ZwCMOMypxRQU5YnOPuXoF31J7GXF16W");

			// Subscribirse a su propio canal
			if (PushService.getSubscriptions(getApplicationContext()).isEmpty()) {
				PushService.subscribe(getApplicationContext(), "a"
						+ Herramientas.getYo().id, PreMapa.class);
			}

			Herramientas.setReiniciadoMovil(true);
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
