package dotidapp.meetus;

import org.json.JSONObject;

import dotidapp.meetus.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
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

			JSONObject json = new JSONObject(intent.getExtras().getString(
					"com.parse.Data"));
			String accion = json.getString("action");

			if (accion.equals("dotidapp.meetus.UPDATE_STATUS")) {
				mp = MediaPlayer.create(context, R.raw.alertsound);// Onreceive
																	// gives you
																	// context
				mp.start();
				String nombre = json.getString("nombre");
				generarNotificacion(context, nombre);
			} else if (accion.equals("dotidapp.meetus.PREGUNTA_ESTADO")) {
				String id = json.getString("id");
				Herramientas.mandarRespuestaDeEstadoActual(id);
			} else if (accion.equals("dotidapp.meetus.RESPUESTA_ESTADO")) {
				String id = json.getString("id");
				Herramientas.RecibirRespuestaDeEstadoActual(id, "online");
			} else if (accion.equals("dotidapp.meetus.DESCONECTADO")) {
				String id = json.getString("id");
				Herramientas.RecibirRespuestaDeEstadoActual(id, "offline");
			} else if (accion.equals("dotidapp.meetus.ACEPTA")) {
				String id = json.getString("id");
				Herramientas.setEsperandoUsuario(false);
			}

		} catch (Exception e) {
			int a = 0;
		}
	}

	private void generarNotificacion(Context context, String nombre) {

		Intent intent = new Intent(context, PreMapa.class);
		PendingIntent pIntent = PendingIntent
				.getActivity(context, 0, intent, 0);

		Intent intentLista = new Intent(context,
				ListadoUsuariosConectados.class);
		PendingIntent pIntentLista = PendingIntent.getActivity(context, 0,
				intentLista, 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(
						(((BitmapDrawable) context.getResources().getDrawable(
								R.drawable.ic_launcher)).getBitmap()))
				.setContentTitle("meetUs")
				.setContentText(
						context.getResources().getString(
								R.string.aceptarSolicitud)
								+ " " + nombre)
				.setContentInfo("1")
				.addAction(R.drawable.ic_action_accept,
						context.getResources().getString(R.string.si), pIntent)
				.addAction(R.drawable.ic_action_cancel,
						context.getResources().getString(R.string.no),
						pIntentLista).setTicker("meetUs!");
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.notify(100, mBuilder.build());

	}
}
