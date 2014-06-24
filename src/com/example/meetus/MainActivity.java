package com.example.meetus;

import java.util.Timer;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
	
	
}