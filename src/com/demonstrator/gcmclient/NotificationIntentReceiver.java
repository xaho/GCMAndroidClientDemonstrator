package com.demonstrator.gcmclient;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationIntentReceiver extends BroadcastReceiver {

	private static MainActivity ma = null;
    private NotificationManager mNotificationManager;
	public static void setMainActivityHandler(MainActivity ma)
	{
		NotificationIntentReceiver.ma = ma;
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		// This should happen when user has pressed on button in notification 
		String nKey = intent.getStringExtra("NotificationKey");
		int nID = intent.getIntExtra("NotificationID",-1);
		String MessageID = intent.getStringExtra("MessageID");
		Log.v("NIR", "intent: " + intent.toString());
		Log.v("NIR", "nKey? " + nKey.substring(0,8));
		Log.v("NIR", "nID? " + nID);
		Log.v("NIR", "MessageID? " + MessageID);

		String action = intent.getAction();
		if (action.equals("COM.DEMONSTRATOR.GCMCLIENT.DISMISS_ALL"))
		{
			Log.v("NIR", "dismiss all");
			//dismiss local notification
	    	mNotificationManager = (NotificationManager)
	                ma.getSystemService(Context.NOTIFICATION_SERVICE);
	    	mNotificationManager.cancel(nID);
			Log.v("NIR", "dismiss NotificationID: " + nID);
			//dismiss external notification
			//TODO: Shouldn't this refer to class variable?
			ma.dismissNotification(nKey, MessageID);
		}
		else if (action.equals("COM.DEMONSTRATOR.GCMCLIENT.DISMISS"))
		{
			//dismiss local notification
	    	mNotificationManager = (NotificationManager)
	                ma.getSystemService(Context.NOTIFICATION_SERVICE);
	    	mNotificationManager.cancel(nID);
			Log.v("NIR", "dismiss NotificationID: " + nID);
		}
		else if (action.equals("COM.DEMONSTRATOR.GCMCLIENT.CLOSED"))
		{
			MainActivity.LUT.remove(MessageID);
			Log.v("NIR", "TODO: Send msg to server to log closure of notifciation.");
		}
	}
}