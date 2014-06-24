package com.example.meetus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PushReceiver extends BroadcastReceiver {
	
	public static final String ACTION = "custom-action-name";

  @SuppressWarnings("unused")
@Override
  public void onReceive(Context context, Intent intent) {
    try {
    	int a = 0;
      String action = intent.getAction();
      String channel = intent.getExtras().getString("id");

    } catch (Exception e) {
      
    }
  }
}
