package com.example.meetus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Window;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;


public class MainActivity extends FragmentActivity {

	private ProgressDialog dialog;
	private String resultJson;
	private GoogleMap mapa;
	private SupportMapFragment mapaView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fragment_main);
		
		//Se guarda que hemos estado en el mapa.Sirve para que al volver a premapa nos mande a listaDeAmigos
		Herramientas.setVieneDelMapa(true);

		Herramientas.setContexto(this);

		Herramientas.inicializarMia();

		//Inicializamos el timer para ver si el usuario ha estado 15 segundos sin contestar
		
		Herramientas.setTiempoInicio(System.currentTimeMillis());

		Herramientas.arrancarProgressDialogMapa(MainActivity.this);
		
		mapaView = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapa = (mapaView).getMap();
		
		Herramientas.arrancarHilosMapa(MainActivity.this);

		Herramientas.setMapa(mapa);

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		PosicionMia.arrancarGps();
		Herramientas.arrancarHilosMapa(MainActivity.this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		salir();
	}

	@Override
	protected void onPause() {
		super.onPause();
		salir();
		
	}

	@Override
	protected void onStop() {
		super.onStop();
		salir();
		
	}
	
	protected void salir(){
		PosicionMia.pararGps();
		Herramientas.pararProgressDialogMapa(MainActivity.this);
		Herramientas.borrarPosicion();
		Herramientas.setElUsuarioEstaActivo(false);

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK && Herramientas.getReiniciadoMovil()) {
	    	Herramientas.setReiniciadoMovil(false);
	    	Intent tabi=new Intent(getApplicationContext(),Inicio.class);
	    	tabi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivity(tabi);
	        finish();
	        super.onKeyDown(keyCode, event);
	        return true;
	    }
	    else{
	    	Intent tabi=new Intent(getApplicationContext(),ListadoUsuariosConectados.class);
	    	tabi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivity(tabi);
	        finish();
	        super.onKeyDown(keyCode, event);
	        return true;
	    }
	}
	
}