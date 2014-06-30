package com.example.meetus;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.android.Facebook;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParsePush;

public class Herramientas {

	private final static String urlWebService = "";
	private static GoogleMap mapa;
	private static Context contexto;
	private static List<Usuarios> listaUsuarios = new ArrayList<>();
	private static Usuarios yo = new Usuarios("", "");
	private static Usuarios tu = new Usuarios("", "");
	private static ProgressDialog ringProgressDialog;
	private static Builder ringProgressDialogMapa;
	private static Long tiempoInicio;
	private static Boolean mensajeActivo = false;
	private static Boolean elUsuarioEstaActivo = false;
	private static Boolean vieneDelMapa = false;
	private static Boolean estasRefrescando = false;
	private static Facebook facebook;
	private static int opcionTiempoRespuesta = 2;
	private static Boolean esperandoUsuario = false;
	private static Boolean parseInicializado = false;
	private static Boolean reiniciadoMovil = false;
	
	public static Boolean getEsperandoUsuario() {
		return esperandoUsuario;
	}

	public static void setEsperandoUsuario(Boolean esperandoUsuario) {
		Herramientas.esperandoUsuario = esperandoUsuario;
	}

	private static String access_token;
	private static boolean encontradoYo, encontradoTu;
	private static ejecutarSnipperMapa ejecutarSnipperMapa;
	private static Thread hiloActualizaPos;
	private static Timer timer;

	public static Facebook getFacebook() {
		return facebook;
	}

	public static void setFacebook(Facebook facebook) {
		Herramientas.facebook = facebook;
	}

	public static Boolean getElUsuarioEstaActivo() {
		return elUsuarioEstaActivo;
	}

	public static void setElUsuarioEstaActivo(Boolean elUsuarioEstaActivo) {
		Herramientas.elUsuarioEstaActivo = elUsuarioEstaActivo;
	}

	public static Long getTiempoInicio() {
		return tiempoInicio;
	}

	public static void setTiempoInicio(Long tiempoInicio) {
		Herramientas.tiempoInicio = tiempoInicio;
	}

	public static boolean isEncontradoYo() {
		return encontradoYo;
	}

	public static void cambiarEsLaPrimeraVezANo(Context contexto) {
		// Actualizar un registro

		// Abrimos la base de datos en modo lectura
		UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(contexto,
				"baseDeDatos", null, 1);

		SQLiteDatabase db = usdbh.getReadableDatabase();
		db.execSQL("UPDATE Configuracion SET valor='n' WHERE dato='esLaPrimeraVez'");
		db.close();

	}

	public static void cambiarEsLaPrimeraVezASi(Context contexto) {
		// Actualizar un registro

		// Abrimos la base de datos en modo lectura
		UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(contexto,
				"baseDeDatos", null, 1);

		SQLiteDatabase db = usdbh.getReadableDatabase();
		db.execSQL("UPDATE Configuracion SET valor='s' WHERE dato='esLaPrimeraVez'");
		db.close();

	}

	public static void setEncontradoYo(boolean encontradoYo) {
		Herramientas.encontradoYo = encontradoYo;
	}

	public static boolean isEncontradoTu() {
		return encontradoTu;
	}

	public static void setEncontradoTu(boolean encontradoTu) {
		Herramientas.encontradoTu = encontradoTu;
	}

	public static String getAccess_token() {
		return access_token;
	}

	public static void setAccess_token(String access_token) {
		Herramientas.access_token = access_token;
	}

	public static void arrancarProgressDialogLogin(final Context contextoDialogo) {
		ringProgressDialog = ProgressDialog.show(contextoDialogo, "MeetUs",
				"Buscando amigos de Facebook! ...", true);

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					listarAmigos();
				} catch (Exception e) {

				}
				ringProgressDialog.dismiss();
				Intent i = new Intent(contextoDialogo,
						ListadoUsuariosConectados.class);
				contextoDialogo.startActivity(i);
			}
		}).start();
	}

	public static void pararProgressDialogMapa(final Context contextoDialogo) {
		if (!hiloActualizaPos.isAlive()) {
			hiloActualizaPos.interrupt();
		}
	}

	public static void arrancarProgressDialogMapa(final Context contextoDialogo) {

		ejecutarSnipperMapa = new ejecutarSnipperMapa();
		ejecutarSnipperMapa.execute();

	}

	static void mandarMensajeDeEstadoActual(String id) {
		JSONObject data = new JSONObject();
		ParsePush push = new ParsePush();
		push.setChannel("a" + id);
		try {
			data.put("action", "com.example.meetus.PREGUNTA_ESTADO");
			data.put("id", Herramientas.getYo().id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		push.setData(data);
		push.sendInBackground();

	}

	static void mandarRespuestaDeEstadoActual(String id) {
		JSONObject data = new JSONObject();
		ParsePush push = new ParsePush();
		push.setChannel("a" + id);
		try {
			data.put("action", "com.example.meetus.RESPUESTA_ESTADO");
			data.put("id", Herramientas.getYo().id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		push.setData(data);
		push.sendInBackground();

		for (int i = 0; i < Herramientas.getListaUsuarios().size(); i++) {
			if (Herramientas.getListaUsuarios().get(i).id.equals(id)) {
				Herramientas.getListaUsuarios().get(i).setEstado("online");
				ListView myListView = (ListView) ((ListadoUsuariosConectados) contexto)
						.findViewById(R.id.myListView);
				((ListadoUsuariosConectados) contexto).aa
						.notifyDataSetChanged();

			}
		}

	}

	static void mandarMensajeDesconectado(String id) {
		JSONObject data = new JSONObject();
		ParsePush push = new ParsePush();

		for (int i = 0; i < Herramientas.getListaUsuarios().size(); i++) {
			push.setChannel("a" + Herramientas.getListaUsuarios().get(i).id);
			try {
				data.put("action", "com.example.meetus.DESCONECTADO");
				data.put("id", Herramientas.getYo().id);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			push.setData(data);
			push.sendInBackground();
		}

	}

	static void mandarUsuarioAcepta() {
		JSONObject data = new JSONObject();
		ParsePush push = new ParsePush();
		push.setChannel("a" + Herramientas.getTu().id);
		try {
			data.put("action", "com.example.meetus.ACEPTA");
			data.put("id", Herramientas.getYo().id);
			push.setData(data);
			push.sendInBackground();
		} catch (JSONException e) {
			e.printStackTrace();
			
		}

	}

	public static Usuarios getTu() {
		return tu;
	}

	public static void setTu(Usuarios tu) {
		Herramientas.tu = tu;
	}

	private static Boolean semaforoParse = true;

	public static Boolean getSemaforoParse() {
		return semaforoParse;
	}

	public static void setSemaforoParse(Boolean semaforoParse) {
		Herramientas.semaforoParse = semaforoParse;
	}

	public static Usuarios getYo() {
		return yo;
	}

	public static void setYo(Usuarios yo) {
		Herramientas.yo = yo;
	}

	public static List<Usuarios> getListaUsuarios() {
		return listaUsuarios;
	}

	public void setListaUsuarios(List<Usuarios> listaUsuarios) {
		this.listaUsuarios = listaUsuarios;
	}

	public static void addUsuario(Usuarios usuario) {
		listaUsuarios.add(usuario);
	}

	// Json
	static InputStream is = null;
	static String json = "";
	private static JSONArray jArray;
	private static String resultJson;

	public static Context getContexto() {
		return contexto;
	}

	public static void setContexto(Context contexto) {
		Herramientas.contexto = contexto;
	}

	public static GoogleMap getMapa() {
		return mapa;
	}

	public static void setMapa(GoogleMap mapa) {
		Herramientas.mapa = mapa;
	}

	public static String getUrlWebService() {
		return urlWebService;
	}

	public static boolean probarConWebService() {
		// TODO
		// Este metodo devuelve si es posible conectar con el webService
		boolean funciona = false;

		return funciona;
	}

	public static boolean enviarPos() {
		// TODO
		// Enviar pos, retornaFalso si no se consigue
		boolean funciona = false;

		return funciona;
	}

	public static void dibujarPersonaEnMapa() {
		try {
			mapa.clear();
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			LatLng Latlng1 = null;
			LatLng Latlng2 = null;
			if (encontradoYo) {

				final Charset utf8 = Charset.forName("UTF-8");
				String nombre = new String(
						Herramientas.getYo().nombre.getBytes(utf8), utf8);

				Latlng1 = new LatLng(PosicionMia.latitud, PosicionMia.longitud);

				mapa.addMarker(
						new MarkerOptions().position(Latlng1).title(nombre))
						.showInfoWindow();
			}
			if (encontradoTu) {

				final Charset utf8 = Charset.forName("UTF-8");
				String nombre = new String(
						Herramientas.getTu().nombre.getBytes(utf8), utf8);

				Latlng2 = new LatLng(PosicionTuya.latitud,
						PosicionTuya.longitud);

				mapa.addMarker(
						new MarkerOptions().position(Latlng2).title(nombre))
						.showInfoWindow();
			}

			if (Latlng1 != null && Latlng2 != null) {
				builder.include(Latlng1);
				builder.include(Latlng2);
				LatLngBounds bounds = builder.build();
				mapa.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,
						50));
			}
		} catch (Exception e) {

		}

	}

	public static void inicializarMia() {
		PosicionMia.inicializarMia();
	}

	public static String jsonLoad(String dato, String url) {

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		try {
			// defaultHttpClient

			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		return json;
	}

	public static Boolean parsearPosicionTuya(String cadena) {
		try {
			JSONObject jsonObject = new JSONObject(cadena);
			JSONArray jResult = jsonObject.getJSONArray("Posicion");

			if (jResult.length() > 0) {

				PosicionTuya.latitud = Double.valueOf(jResult.getJSONObject(0)
						.getString("latitud"));
				PosicionTuya.longitud = Double.valueOf(jResult.getJSONObject(0)
						.getString("longitud"));
				if (!PosicionTuya.longitud.toString().equals("0.0")) {
					Herramientas.setEncontradoTu(true);
					elUsuarioEstaActivo = true;
				} else {
					// Comprobar si el usuario se ha desconectado
					if (PosicionTuya.longitud.toString().equals("0.0")
							&& elUsuarioEstaActivo) {
						((MainActivity) contexto).runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(
										contexto,
										contexto.getResources().getString(
												R.string.desconectado),
										Toast.LENGTH_LONG).show();
								Intent i = new Intent(contexto,
										ListadoUsuariosConectados.class);
								contexto.startActivity(i);
							}
						});
					}
					if (PosicionMia.latitud != null
							&& System.currentTimeMillis() > getTiempoInicio() + 10000
							&& !mensajeActivo) {
						mensajeActivo = true;
						((MainActivity) contexto).runOnUiThread(new Runnable() {
							public void run() {

								AlertDialog.Builder builder = new AlertDialog.Builder(
										contexto);
								builder.setMessage(contexto.getResources()
										.getString(R.string.usuario_ausente));
								builder.setCancelable(false);
								builder.setPositiveButton(contexto
										.getResources().getString(R.string.si),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
												// Reiniciar TiempoInicio
												setTiempoInicio(System
														.currentTimeMillis());
												mensajeActivo = false;
											}
										});

								builder.setNegativeButton(contexto
										.getResources().getString(R.string.no),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
												Intent i = new Intent(
														contexto,
														ListadoUsuariosConectados.class);
												contexto.startActivity(i);
											}
										});
								builder.show();

							}
						});
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public static Object parsearUsuarios(String cadena) {
		try {
			JSONObject jsonObject = new JSONObject(cadena);
			JSONArray jResult = jsonObject.getJSONArray("Tlf");

			if (jResult.length() > 0) {
				PosicionTuya.latitud = Double.valueOf(jResult.getJSONObject(0)
						.getString("tlf"));
				PosicionTuya.longitud = Double.valueOf(jResult.getJSONObject(0)
						.getString("nombre"));
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public static void preMapa() {
		resultJson = Herramientas.jsonLoad("a",
				"http://s425938729.mialojamiento.es/webs/meetUs/wsUsuarios.php?opcion=7&id="
						+ getYo().getId());
		parsearPreMapa(resultJson);

	}

	public static void parsearPreMapa(String cadena) {
		try {
			JSONObject jsonObject = new JSONObject(cadena);
			JSONArray jResult = jsonObject.getJSONArray("id");

			if (jResult.length() > 0) {
				getTu().setId(
						jResult.getJSONObject(0).getString("idUltimoUsuario"));
			} else {
			}
		} catch (Exception e) {

		}
	}

	public static void borrarPosicion() {
		String cadena = "http://s425938729.mialojamiento.es/webs/meetUs/wsUsuarios.php?opcion=3&idUsuario="
				+ Herramientas.getYo().id + "&longitud=0" + "&latitud=" + "0";

		resultJson = Herramientas.jsonLoad(Herramientas.getYo().id, cadena);
	}

	@SuppressWarnings({ "deprecation", "unused" })
	public static void listarAmigos() {

		HttpClient httpclient = new DefaultHttpClient();
		String url = "https://graph.facebook.com/me/friends?fields=name,id,picture&access_token="
				+ URLEncoder.encode(access_token);

		String url2 = "";
		HttpGet httppost = new HttpGet(url);
		try {
			HttpResponse response = httpclient.execute(httppost);

			// to get the response string
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			// used to construct the string
			String res = "";
			for (String line = null; (line = reader.readLine()) != null;) {
				res += line + "\n";

			}
			// here we will parse the response
			JSONObject obj = new JSONObject(new JSONTokener(res));
			JSONArray data = obj.getJSONArray("data");

			int len = data.length();
			Herramientas.getListaUsuarios().clear();
			for (int i = 0; i < len; i++) {
				JSONObject currentResult = data.getJSONObject(i);
				// String name = currentResult.getString("name");
				String id = currentResult.getString("id");
				String nombre = currentResult.getString("name");
				String urlPicture = currentResult.getJSONObject("picture")
						.getJSONObject("data").getString("url");

				// Download Picture
				InputStream is = (InputStream) new URL(urlPicture).getContent();
				Bitmap d = BitmapFactory.decodeStream(is);
				is.close();
				Usuarios usuario = new Usuarios(nombre, id, d);
				Herramientas.addUsuario(usuario);
			}

			if (len > 0) {
				vaciarTablaAmigos();
				insertarAmigosEnBaseDeDatos();
			}

			buscarYo();
			registrarUsuario();

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void vaciarTablaAmigos() {
		// Abrimos la base de datos en modo lectura
		UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(contexto,
				"baseDeDatos", null, 1);

		SQLiteDatabase db = usdbh.getReadableDatabase();
		db.execSQL("delete from Amigos where 1");
		db.close();

	}

	private static void insertarAmigosEnBaseDeDatos() {
		try {
			// Abrimos la base de datos en modo lectura
			UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(contexto,
					"baseDeDatos", null, 1);

			SQLiteDatabase db = usdbh.getReadableDatabase();

			// Insertamos los usuarios
			for (int i = 0; i < Herramientas.getListaUsuarios().size(); i++) {
				// Variables para guardar images

				Bitmap image = Herramientas.getListaUsuarios().get(i).foto;

				ByteArrayOutputStream out = new ByteArrayOutputStream();

				image.compress(Bitmap.CompressFormat.PNG, 100, out);

				byte[] buffer = out.toByteArray();

				ContentValues cv = new ContentValues();
				cv.put("foto", buffer); // CHUNK blob type field of your table

				// Creamos el registro a insertar como objeto ContentValues
				ContentValues nuevoRegistro = new ContentValues();
				nuevoRegistro.put("id",
						Herramientas.getListaUsuarios().get(i).id);
				nuevoRegistro.put("nombre", Herramientas.getListaUsuarios()
						.get(i).nombre);
				nuevoRegistro.put("foto", buffer);

				// Insertamos el registro en la base de datos
				db.insert("Amigos", null, nuevoRegistro);
			}
			db.close();
		} catch (Exception e) {
			int a = 0;
		}
	}

	private static void registrarUsuario() {
		Herramientas.jsonLoad(
				"",
				"http://s425938729.mialojamiento.es/webs/meetUs/wsUsuarios.php?opcion=5&id="
						+ Herramientas.getYo().id + "&nombre="
						+ URLEncoder.encode(Herramientas.getYo().nombre));
		Herramientas
		// Añadir el usuario a la tabla posiciones
				.jsonLoad("",
						"http://s425938729.mialojamiento.es/webs/meetUs/wsUsuarios.php?opcion=8&id="
								+ Herramientas.getYo().id);
	}

	private static void buscarYo() {
		HttpClient httpclient = new DefaultHttpClient();
		@SuppressWarnings("deprecation")
		String url = "https://graph.facebook.com/me?fields=id,name&access_token="
				+ URLEncoder.encode(access_token);
		HttpGet httppost = new HttpGet(url);
		try {
			HttpResponse response = httpclient.execute(httppost);

			// to get the response string
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			// used to construct the string
			String res = "";
			for (String line = null; (line = reader.readLine()) != null;) {
				res += line + "\n";

			}

			Pattern patronNombre = Pattern.compile("name\":\"(.*)\"");
			Matcher matcherNombre = patronNombre.matcher(res);

			Pattern patronId = Pattern.compile("id\":\"(.*)\",");
			Matcher matcherId = patronId.matcher(res);

			// Hace que Matcher busque los trozos.
			matcherNombre.find();
			matcherId.find();

			Herramientas.setYo(new Usuarios(matcherNombre.group(1), matcherId
					.group(1)));

			UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(contexto,
					"baseDeDatos", null, 1);

			SQLiteDatabase db = usdbh.getReadableDatabase();

			db.execSQL("INSERT INTO Configuracion VALUES ('idUsuario','"
					+ matcherId.group(1) + "')");

			db.execSQL("INSERT INTO Configuracion VALUES ('nombre','"
					+ matcherNombre.group(1) + "')");

			db.close();

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void arrancarHilosMapa(Context contexto) {
		// Hilo Subir mi pos y bajar la del otro usuario
		hiloActualizaPos = new Thread(new Runnable() {
			public void run() {

				while (true) {
					try {
						Thread.sleep(Constantes.getTiempoRefresco(Herramientas
								.getOpcionTiempoRespuesta()));

						// Enviar posicion
						if (PosicionMia.latitud != null
								&& PosicionMia.longitud != null) {
							String cadena = "http://s425938729.mialojamiento.es/webs/meetUs/wsUsuarios.php?opcion=3&idUsuario="
									+ Herramientas.getYo().id
									+ "&longitud="
									+ String.valueOf(PosicionMia.longitud)
									+ "&latitud="
									+ String.valueOf(PosicionMia.latitud);

							resultJson = Herramientas.jsonLoad(
									Herramientas.getYo().id, cadena);
						}
						// Recibir posicion otro usuario
						resultJson = Herramientas
								.jsonLoad(
										"",
										"http://s425938729.mialojamiento.es/webs/meetUs/wsUsuarios.php?opcion=2&idUsuario="
												+ Herramientas.getTu().id);
						Boolean exito = Herramientas
								.parsearPosicionTuya(resultJson);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		});
		hiloActualizaPos.start();
	}

	public static Boolean getVieneDelMapa() {
		return vieneDelMapa;
	}

	public static void setVieneDelMapa(Boolean vieneDelMapa) {
		Herramientas.vieneDelMapa = vieneDelMapa;
	}

	public static Boolean getEstasRefrescando() {
		return estasRefrescando;
	}

	public static void setEstasRefrescando(Boolean estasRefrescando) {
		Herramientas.estasRefrescando = estasRefrescando;
	}

	public static int getOpcionTiempoRespuesta() {
		return opcionTiempoRespuesta;
	}

	public static void setOpcionTiempoRespuesta(int opcionTiempoRespuesta) {
		Herramientas.opcionTiempoRespuesta = opcionTiempoRespuesta;
	}

	private static class ejecutarSnipperMapa extends
			AsyncTask<Void, Void, Void> {

		private AlertDialog alert;

		public ejecutarSnipperMapa() {
			if (android.os.Build.VERSION.SDK_INT > 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			ringProgressDialogMapa = new AlertDialog.Builder(contexto);

			ringProgressDialogMapa.setMessage(contexto.getResources()
					.getString(R.string.Localizando));
			ringProgressDialogMapa.setCancelable(false);

			ringProgressDialogMapa.setNegativeButton(contexto.getResources()
					.getString(R.string.cancelar),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
							Intent i = new Intent(contexto,
									ListadoUsuariosConectados.class);
							contexto.startActivity(i);
						}
					});

			alert = ringProgressDialogMapa.create();

			alert.show();

		}

		@Override
		protected Void doInBackground(Void... accion) {
			while (!encontradoTu || !encontradoYo) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int a = 0;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			alert.cancel();
		}

	}

	public static Boolean comprobaciones(Context contexto) {

		// Comprobar Internet
		ConnectivityManager cm = (ConnectivityManager) contexto
				.getSystemService(contexto.CONNECTIVITY_SERVICE);

		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}

		Toast.makeText(contexto,
				contexto.getResources().getString(R.string.no_internet),
				Toast.LENGTH_LONG).show();
		return false;
	}

	public static void RecibirRespuestaDeEstadoActual(String id, String estado) {
		for (int i = 0; i < Herramientas.getListaUsuarios().size(); i++) {
			if (Herramientas.getListaUsuarios().get(i).id.equals(id)) {
				Herramientas.getListaUsuarios().get(i).setEstado(estado);
				ListView myListView = (ListView) ((ListadoUsuariosConectados) contexto)
						.findViewById(R.id.myListView);
				((ListadoUsuariosConectados) contexto).aa
						.notifyDataSetChanged();

			}
		}

	}

	public static Boolean getParseInicializado() {
		return parseInicializado;
	}

	public static void setParseInicializado(Boolean parseInicializado) {
		Herramientas.parseInicializado = parseInicializado;
	}

	public static Boolean getReiniciadoMovil() {
		return reiniciadoMovil;
	}

	public static void setReiniciadoMovil(Boolean reiniciadoMovil) {
		Herramientas.reiniciadoMovil = reiniciadoMovil;
	}
}
