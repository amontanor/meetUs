package com.example.meetus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.support.v4.app.NotificationCompat;

public class PushReceiver extends BroadcastReceiver {

	public static final String ACTION = "custom-action-name";
	MediaPlayer mp = null;// Here 

	@SuppressWarnings("unused")
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			int a = 0;
			String action = intent.getAction();
			String channel = intent.getExtras().getString("id");
			
	            mp = MediaPlayer.create(context, R.raw.alertsound);//Onreceive gives you context
	            mp.start();
	            
	            generarNotificacion(context);
	            
		} catch (Exception e) {
			int a = 0;
		}
	}

	private void generarNotificacion(Context context) {
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(context)
			        .setSmallIcon(R.drawable.ic_launcher)
			        .setLargeIcon((((BitmapDrawable)context.getResources()
			            .getDrawable(R.drawable.ic_launcher)).getBitmap()))
			        .setContentTitle("Mensaje de Alerta")
			        .setContentText("Ejemplo de notificación.")
			        .setContentInfo("4")
			        .setTicker("Alerta!");
		NotificationManager mNotificationManager =
			    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			 
			mNotificationManager.notify(100, mBuilder.build());
		
	}
}
