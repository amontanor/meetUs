package com.example.meetus;

import com.google.android.gms.internal.in;

public class Constantes {

	//Tiempos
	static int[] tiempos = {3000,5000,10000};
	
	public static int getTiempoRefresco(int valor)
	{
		return tiempos[valor];
	}
	
}
