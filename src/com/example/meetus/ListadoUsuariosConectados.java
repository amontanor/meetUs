package com.example.meetus;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.PushService;

public class ListadoUsuariosConectados extends Activity {

	private String resultJson;
	private String numeroTlf;
	private Facebook facebook;
	private SharedPreferences sp;
	private String access_token;

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

		// Se guarda que NO hemos estado en el mapa. Sirve para que al volver a
		// premapa nos mande a listaDeAmigos
		Herramientas.setVieneDelMapa(false);

		// Inicializar Parse
		inicializaParse();

		// Guardamos el contexto
		Herramientas.setContexto(this);

		ListView myListView = (ListView) findViewById(R.id.myListView);
		// Create the Array List of to do items

		// Create the Array Adapter to bind the array to the List View
		final ArrayAdapter<Usuarios> aa;
		aa = new listadoUsuariosAdapter(this, R.layout.usuariolist_item,
				Herramientas.getListaUsuarios());

		// Bind the Array Adapter to the List View
		myListView.setAdapter(aa);
	
		ListView lv = (ListView)findViewById(R.id.myListView);
	    lv.setVisibility((aa.isEmpty())?View.GONE:View.VISIBLE); 
		// Item Click Listener the listview
		myListView.setOnItemClickListener(new ListView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				JSONObject obj = new JSONObject();

				String nombreTu = ((Usuarios) parent.getItemAtPosition(pos)).nombre;
				String idTu = ((Usuarios) parent.getItemAtPosition(pos)).id;

				ParsePush push = new ParsePush();
				
				// Comprobar si el usuario esta activo

				Herramientas.setTu(new Usuarios(nombreTu, idTu));
				push.setChannel("a" + Herramientas.getTu().id);
				//push.setData(obj);
				
				push.setMessage(Herramientas.getYo().nombre
						+ " quiere ver por donde andas!");
				push.setExpirationTime(15000);
				try {
					// Enviar mi id al otro usuario
					resultJson = Herramientas.jsonLoad("a",
							"http://s425938729.mialojamiento.es/webs/meetUs/wsUsuarios.php?opcion=6&idYo="
									+ Herramientas.getYo().id + "&idTu="
									+ Herramientas.getTu().id);
					obj.put("id", Herramientas.getYo().id);
					//push.setData(obj);
					push.send();

					// Variables para comprobar si ya nos ha localizado a ambos
					Herramientas.setEncontradoTu(false);
					Herramientas.setEncontradoYo(false);
					Intent i = new Intent(ListadoUsuariosConectados.this,
							MainActivity.class);
					startActivity(i);
				} catch (ParseException e) {
					// TODO
					e.printStackTrace();
				} catch (Exception e) {
					// TODO
					e.printStackTrace();
				}
			}
		});

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
		try{
			Herramientas.borrarPosicion();
			Herramientas.setElUsuarioEstaActivo(false);
		}
		catch(Exception e){
			
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
		case R.id.compartir:
			sendRequestDialog();
			// showHelp();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void mostrarVentanaSelectorTiempo() {
		CharSequence colors[] = new CharSequence[] { "3 segundos",
				"5 segundos", "10 segundos" };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Tiempo de refresco del Mapa");
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
		params.putString("meetUs",
				getResources().getString(R.string.invitar));
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
							Toast.makeText(ListadoUsuariosConectados.this,
									getResources().getString(R.string.facebook_peticion_exito), Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(ListadoUsuariosConectados.this,
									getResources().getString(R.string.facebook_peticion_cancelada), Toast.LENGTH_SHORT)
									.show();
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

	private class ejecutarJson extends AsyncTask<String, Void, Void> {

		public ejecutarJson() {

		}

		protected void onPreExecute() {
			super.onPreExecute();
		}

		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */

		protected Void doInBackground(String... valores) {
			List listaUsuariosCompatibles = new ArrayList<Usuarios>();
			String cadenaUsuarios = "";

			valores[0] = valores[0].replace("(", "");
			valores[0] = valores[0].replace(")", "");
			valores[0] = valores[0].replace(" ", "");
			valores[0] = valores[0].replace("+", "");
			valores[0] = valores[0].replace("-", "");
			valores[0] = valores[0].replace("#", "");
			valores[0] = valores[0].replace("*", "");
			resultJson = Herramientas.jsonLoad("a",
					"http://s425938729.mialojamiento.es/webs/meetUs/wsUsuarios.php?opcion=4&tlf="
							+ valores[0]);
			listaUsuariosCompatibles.add(Herramientas
					.parsearUsuarios(resultJson));

			Boolean exito = Herramientas.parsearPosicionTuya(resultJson);

			return null;
		}

		protected void onPostExecute(Void result) {
		}

	}

	private void inicializaParse() {

		Parse.initialize(this, "PuVIKboSUko0q8HRrlVJ0bDl8VYLHyK0ZKt1x2K5",
				"W9qf2khJ8ZwCMOMypxRQU5YnOPuXoF31J7GXF16W");

		// Subscribirse a su propio canal
		PushService.unsubscribe(getApplicationContext(),
				"a" + Herramientas.getYo().id);

		PushService.subscribe(getApplicationContext(),
				"a" + Herramientas.getYo().id, PreMapa.class);

	}

}
