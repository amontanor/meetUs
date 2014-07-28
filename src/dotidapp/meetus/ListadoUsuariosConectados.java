package dotidapp.meetus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import dotidapp.meetus.R;

import com.facebook.FacebookException;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.parse.Parse;
import com.parse.ParsePush;
import com.parse.PushService;

public class ListadoUsuariosConectados extends Activity {

	private String resultJson;
	private String numeroTlf;
	private Facebook facebook;
	private SharedPreferences sp;
	private String access_token;
	private Boolean cancelado = false;

	// Create the Array Adapter to bind the array to the List View
	ArrayAdapter<Usuarios> aa;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_listado_usuarios_conectados);

		// Se guarda que NO hemos estado en el mapa. Sirve para que al
		// volver a
		// premapa nos mande a listaDeAmigos
		Herramientas.setVieneDelMapa(false);

		// Inicializar Parse
		if (!Herramientas.getParseInicializado()) {
			inicializaParse();
		}

		// Eliminamos la notificacion
		if (Herramientas.getAlertaActiva()) {
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			int tamano = Herramientas.getTu().id.length();
			mNotificationManager.cancel(new Integer(Herramientas.getTu().id
					.replace("a", "").substring(tamano - 6, tamano - 1)));
			Herramientas.setAlertaActiva(false);
			mandarNoAcepto();
		}

		// Guardamos el contexto
		Herramientas.setContexto(this);

		ListView myListView = (ListView) findViewById(R.id.myListView);
		// Create the Array List of to do items

		aa = new listadoUsuariosAdapter(this, R.layout.usuariolist_item,
				Herramientas.getListaUsuarios());

		// Bind the Array Adapter to the List View
		myListView.setAdapter(aa);

		ListView lv = (ListView) findViewById(R.id.myListView);
		lv.setVisibility((aa.isEmpty()) ? View.GONE : View.VISIBLE);
		// Item Click Listener the listview
		myListView.setOnItemClickListener(new ListView.OnItemClickListener() {
			private EjecturarEsperaPeticion ejecturarEsperaPeticion = null;

			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				if (((Usuarios) parent.getItemAtPosition(pos)).estado
						.equals("online")) {
					String nombreTu = ((Usuarios) parent.getItemAtPosition(pos)).nombre;
					String idTu = ((Usuarios) parent.getItemAtPosition(pos)).id;

					ParsePush push = new ParsePush();

					// Comprobar si el usuario esta activo

					Herramientas.setTu(new Usuarios(nombreTu, idTu));

					try {

						// JSONObject data = new
						// JSONObject("{\"alert\": \"The Mets scored!\"}");
						JSONObject data = new JSONObject();
						push.setChannel("a" + Herramientas.getTu().id);
						data.put("action", "dotidapp.meetus.UPDATE_STATUS");
						data.put("nombre", Herramientas.getYo().nombre);
						data.put("id", "a" + Herramientas.getYo().id);
						push.setData(data);
						push.sendInBackground();

						// Enviar mi id al otro usuario
						resultJson = Herramientas.jsonLoad("a",
								"http://s425938729.mialojamiento.es/webs/meetUs/wsUsuarios.php?opcion=6&idYo="
										+ Herramientas.getYo().id + "&idTu="
										+ Herramientas.getTu().id);
						// Variables para comprobar si ya nos ha
						// localizado a
						// ambos
						Herramientas.setEncontradoTu(false);
						Herramientas.setEncontradoYo(false);

						Herramientas.setEsperandoUsuario(true);

						ejecturarEsperaPeticion = new EjecturarEsperaPeticion();
						ejecturarEsperaPeticion.execute();

					} catch (Exception e) {
						// TODO
						e.printStackTrace();
					}

				} else if (((Usuarios) parent.getItemAtPosition(pos)).estado
						.equals("offline")) {
					Toast.makeText(ListadoUsuariosConectados.this,
							getResources().getString(R.string.usuarioOffline),
							Toast.LENGTH_SHORT).show();
				}
 			}
		});

		Toast.makeText(ListadoUsuariosConectados.this,
				getResources().getString(R.string.comprobandoEstado),
				Toast.LENGTH_LONG).show();
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

	private void comprobarEstados() {

		for (int i = 0; i < Herramientas.getListaUsuarios().size(); i++) {
			Herramientas.mandarMensajeDeEstadoActual(Herramientas
					.getListaUsuarios().get(i).id);
		}

	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Borrar Posicion y poner elUsuarioEstaActivo a false
		try {
			Herramientas.borrarPosicion();
			Herramientas.setElUsuarioEstaActivo(false);
		} catch (Exception e) {

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_listado_amigos, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.refresh:
			// Pongo estoyRefrescando a true
			Herramientas.setEstasRefrescando(true);
			Herramientas.cambiarEsLaPrimeraVezASi(this);
			Intent i = new Intent(ListadoUsuariosConectados.this, Inicio.class);
			startActivity(i);
			return true;
		case R.id.salir:
			Intent finishapp = new Intent(Intent.ACTION_MAIN);
			finishapp.addCategory(Intent.CATEGORY_HOME);
			finishapp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(finishapp);
			return true;
		case R.id.opciones:
			mostrarVentanaSelectorTiempo();
			return true;
		case R.id.compartirFacebook:
			sendRequestDialog();
			// showHelp();
			return true;
		case R.id.compartirOtros:
			compartirOtros();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void compartirOtros() {
		try {
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					getResources().getString(R.string.invitar));
			startActivity(Intent.createChooser(shareIntent, "MeetUs"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void mostrarVentanaSelectorTiempo() {
		CharSequence colors[] = new CharSequence[] { "3 " + getResources().getString(R.string.segundos),
				"5 " + getResources().getString(R.string.segundos), "10 " + getResources().getString(R.string.segundos) };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.tiempoRefresco));
		builder.setItems(colors, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Herramientas.setOpcionTiempoRespuesta(which);
			}
		});
		builder.show();

	}

	private void lanzarDialogoInvitar() {

		Bundle params = new Bundle();
		params.putString("message", getResources().getString(R.string.invitar));
		WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(this,
				Herramientas.getFacebook().getSession(), params))
				.setOnCompleteListener(new OnCompleteListener() {

					@Override
					public void onComplete(Bundle values,
							FacebookException error) {
						String requestId = null;
						if (values != null) {
							requestId = values.getString("request");
						}
						if (requestId != null) {
							Toast.makeText(
									ListadoUsuariosConectados.this,
									getResources().getString(
											R.string.facebook_peticion_exito),
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(
									ListadoUsuariosConectados.this,
									getResources()
											.getString(
													R.string.facebook_peticion_cancelada),
									Toast.LENGTH_SHORT).show();
						}
					}
				}).build();
		requestsDialog.show();
	}

	private void sendRequestDialog() {
		if (Herramientas.getFacebook() == null || facebook == null
				|| !facebook.isSessionValid()) {
			configurarFacebook();
			facebook.authorize(ListadoUsuariosConectados.this,
					new String[] { "user_friends" }, new DialogListener() {

						@Override
						public void onComplete(Bundle values) {
							Herramientas.setFacebook(facebook);
							Editor editor = sp.edit();
							editor.putString("access_token",
									facebook.getAccessToken());
							editor.putLong("access_expires",
									facebook.getAccessExpires());
							editor.commit();
							access_token = facebook.getAccessToken();
							// btnLoginFB.setText(getResources().getString(R.string.logOut));
							// TODO comprobar si el usuario esta registrado
							Herramientas.setAccess_token(access_token);
							lanzarDialogoInvitar();
						}

						@Override
						public void onFacebookError(FacebookError e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onError(DialogError e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onCancel() {
							// TODO Auto-generated method stub

						}

					});
		} else {
			lanzarDialogoInvitar();
		}

	}

	private void configurarFacebook() {
		facebook = new Facebook("490295961103483");

		sp = getPreferences(MODE_PRIVATE);
		access_token = sp.getString("access_token", null);
		long expires = sp.getLong("access_expires", 0);

		if (access_token != null) {
			facebook.setAccessToken(access_token);
		}

		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

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
			View rootView = inflater.inflate(
					R.layout.fragment_listado_usuarios_conectados, container,
					false);
			return rootView;
		}
	}

	private class EjecturarEsperaPeticion extends AsyncTask<String, Void, Void> {

		private Builder ringProgressDialog;
		private AlertDialog alert = null;

		public EjecturarEsperaPeticion() {
			if (android.os.Build.VERSION.SDK_INT > 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}
			setCancelado(false);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Cargamos el mensaje de espera
			
			ringProgressDialog = new AlertDialog.Builder(
					ListadoUsuariosConectados.this);

			ringProgressDialog.setMessage(getResources().getString(
					R.string.esperandoRespuesta));
			ringProgressDialog.setCancelable(false);

			ringProgressDialog.setNegativeButton(
					getResources().getString(R.string.cancelar),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// Enviar Cancelacion al otro usuario
							mandarHeCancelado(Herramientas.getYo().id);
							dialog.dismiss();
							Herramientas.setEsperandoUsuario(false);
							setCancelado(true);
						}
					});

			alert = ringProgressDialog.create();

			alert.show();
		}

		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */

		protected Void doInBackground(String... valores) {
			while (Herramientas.getEsperandoUsuario()
					&& !Herramientas.getUsuarioNoAceptaInvitacion()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Herramientas.setEsperandoUsuario(true);
			return null;
		}

		protected void onPostExecute(Void result) {
			alert.dismiss();
			if (!cancelado && !Herramientas.getUsuarioNoAceptaInvitacion()) {
				Herramientas.setUsuarioNoAceptaInvitacion(false);
				Herramientas.setEsperandoUsuario(false);
				Intent i = new Intent(ListadoUsuariosConectados.this,
						MainActivity.class);
				startActivity(i);
			}
			Herramientas.setUsuarioNoAceptaInvitacion(false);
		}
		
	}

	private void inicializaParse() {

		if (!Herramientas.getParseInicializado()) {
			Herramientas.setParseInicializado(true);
			Parse.initialize(getApplicationContext(),
					"PuVIKboSUko0q8HRrlVJ0bDl8VYLHyK0ZKt1x2K5",
					"W9qf2khJ8ZwCMOMypxRQU5YnOPuXoF31J7GXF16W");

			// Subscribirse a su propio canal
			if (!PushService.getSubscriptions(getApplicationContext()).isEmpty()) {
				Iterator<String> iter = PushService.getSubscriptions(getApplicationContext()).iterator();
				while (iter.hasNext()) {
					String canal = iter.next();
					PushService.unsubscribe(getApplicationContext(), canal);
				}	
			}
			
			PushService.subscribe(getApplicationContext(), "a"
					+ Herramientas.getYo().id, PreMapa.class);
		}

	}

	public Boolean getCancelado() {
		return cancelado;
	}

	public void setCancelado(Boolean cancelado) {
		this.cancelado = cancelado;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent finishapp = new Intent(Intent.ACTION_MAIN);
			finishapp.addCategory(Intent.CATEGORY_HOME);
			finishapp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(finishapp);
			return true;
		} else {
			return false;
		}

	}

	// Se cancela que el otro usuario acepte la peticion
	static void mandarHeCancelado(String id) {
		JSONObject data = new JSONObject();
		if (!Herramientas.getParseInicializado()) {
			ParsePush push = new ParsePush();
			push.setChannel("a" + Herramientas.getTu().id);
			try {
				data.put("action", "dotidapp.meetus.CANCELO");
				data.put("id", "a" + Herramientas.getYo().id);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			push.setData(data);
			push.sendInBackground();
		}
	}

	// El usuario no acepta la invitacion
	static void mandarNoAcepto() {
		JSONObject data = new JSONObject();

		ParsePush push = new ParsePush();
		push.setChannel(Herramientas.getTu().id);
		try {
			data.put("action", "dotidapp.meetus.NO_ACEPTO");
			data.put("id", Herramientas.getYo().id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		push.setData(data);
		push.sendInBackground();

	}

}
