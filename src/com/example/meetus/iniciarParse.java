package com.example.meetus;

import com.parse.Parse;
import com.parse.PushService;

import android.app.Application;

public class iniciarParse extends Application{

	@Override
	public void onCreate()
	{

	    super.onCreate();

	    if(Herramientas.comprobaciones(getApplicationContext()))
	    {
	    	Parse.initialize(getApplicationContext(), "PuVIKboSUko0q8HRrlVJ0bDl8VYLHyK0ZKt1x2K5",
					"W9qf2khJ8ZwCMOMypxRQU5YnOPuXoF31J7GXF16W");

			// Subscribirse a su propio canal
			PushService.unsubscribe(getApplicationContext(),
					"a" + Herramientas.getYo().id);

			PushService.subscribe(getApplicationContext(),
					"a" + Herramientas.getYo().id, PreMapa.class);
	    }

	}

	@Override
	public void onLowMemory()
	{
	    // TODO Auto-generated method stub
	    super.onLowMemory();
	}

	@Override
	public void onTerminate()
	{
	    // TODO Auto-generated method stub
	    super.onTerminate();
	}
	
}
