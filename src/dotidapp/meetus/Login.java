package dotidapp.meetus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import dotidapp.meetus.R;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

//FaceBook

public class Login extends Activity {

	@SuppressWarnings("deprecation")
	
	private Button btnLoginFB;
	private SharedPreferences sp;
	protected String access_token;

	Facebook facebook;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);

		facebook = new Facebook("490295961103483");
		Herramientas.setFacebook(facebook);

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
		
		Button button = (Button) findViewById(R.id.btnlogin);
		button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	mostrarLogin();
		    }
		});

		mostrarLogin();

	}

	private void mostrarLogin() {
		if (facebook.isSessionValid() && !Herramientas.getEstasRefrescando()) {

		} else {
			facebook.authorize(Login.this, new String[] { "user_friends" },
					new DialogListener() {

						@Override
						public void onFacebookError(FacebookError e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onError(DialogError e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onComplete(Bundle values) {
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
							Herramientas.setContexto(Login.this);
							Herramientas
									.arrancarProgressDialogLogin(Login.this);
							//Poner estasRefrescando como falso
							if (Herramientas.getEstasRefrescando()) {
								try {
									//facebook.logout(Login.this);
									Herramientas.setEstasRefrescando(false);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} 
							}
						}

						@Override
						public void onCancel() {
						}
					});
		}

	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		Intent i = new Intent(this,
				ListadoUsuariosConectados.class);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}
}