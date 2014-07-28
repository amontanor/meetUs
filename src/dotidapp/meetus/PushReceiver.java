package dotidapp.meetus;

import org.json.JSONObject;

import dotidapp.meetus.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

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
				String id = json.getString("id");
				generarNotificacion(context, nombre, id);
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
			} else if (accion.equals("dotidapp.meetus.CANCELO")) {
				String id = json.getString("id");
				Herramientas.setElOtroUsuarioCancela(true);
			} else if (accion.equals("dotidapp.meetus.SALGO")) {
				String id = json.getString("id");
				Herramientas.setElUsuarioEstaActivo(false);
			} else if (accion.equals("dotidapp.meetus.NO_ACEPTO")) {
				Herramientas.setUsuarioNoAceptaInvitacion(true);
				Toast.makeText(
						Herramientas.getContexto(),
						Herramientas.getContexto().getResources()
								.getString(R.string.usuarioNoAcepta),
						Toast.LENGTH_LONG).show();
			} else if (accion.equals("dotidapp.meetus.CANCELO_LOCALIZANDO")) {
				Herramientas.setUsuarioCancelaLocalizando(true);
				Toast.makeText(
						Herramientas.getContexto(),
						Herramientas.getContexto().getResources()
								.getString(R.string.usuarioCancela),
						Toast.LENGTH_LONG).show();
			}

		} catch (Exception e) {
			int a = 0;
		}
	}

	private void generarNotificacion(Context context, String nombre, String id) {

		Intent intent = new Intent(context, PreMapa.class);
		PendingIntent pIntent = PendingIntent
				.getActivity(context, 0, intent, 0);

		Herramientas.getTu().setId(id);

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
				.setPriority(Notification.PRIORITY_MAX)
				.addAction(R.drawable.ic_action_accept,
						context.getResources().getString(R.string.si), pIntent)
				.addAction(R.drawable.ic_action_cancel,
						context.getResources().getString(R.string.no),
						pIntentLista).setTicker("meetUs!");
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		int tamano = Herramientas.getTu().id.length();
		mNotificationManager.notify(new Integer(Herramientas.getTu().id
				.replace("a", "").substring(tamano - 6, tamano - 1)), mBuilder
				.build());
		
		// Get instance of Vibrator from current Context
		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

		// Start without a delay
		// Each element then alternates between vibrate, sleep, vibrate, sleep...
		long[] pattern = {0, 800, 1200, 800};

		// The '-1' here means to vibrate once
		// '0' would make the pattern vibrate indefinitely
		v.vibrate(pattern, -1);

		// Flag alerta esta activa
		Herramientas.setAlertaActiva(true);

		Herramientas.setElOtroUsuarioCancela(false);

	}
}
