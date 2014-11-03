package com.demonstrator.gcmclient;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GcmIntentService extends IntentService {

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Demonstrator";
    
	static int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
            	String type = intent.getStringExtra("Type");
            	if (type != null)
            	{
	            	switch (type)
	            	{
	            	case "Regular":
	                    sendNotification("Received regular: " + extras.toString());
	            		break;
	            	case "Collapsible":
	                    sendNotification("Received collapsible: " + extras.toString());
	            		break;
	            	case "NotificationKey":
	            		sendNotification("Received NotificationKey: " + extras.toString());
	            		break;
	        		default:
	                    sendNotification("Received: " + extras.toString());
	                    break;
	            	}
            	}
            	else
            		sendNotification("Received: "+ extras.toString());
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        //PendingIntent silentIntent = 

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("ContentTitle: GCM Notification")//("GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle().bigText("bigText: " + msg))
        .setAutoCancel(true)
        .setOngoing(true)
        .setContentText("contextText: " + msg);
        if (msg.contains("Regular"))
        {
        	mBuilder.addAction(R.drawable.ic_launcher , "Btn dismiss 1", contentIntent);
        	mBuilder.addAction(R.drawable.ic_launcher , "Btn dismiss all", contentIntent);
        }

        mBuilder.setContentIntent(contentIntent);//What to do when notification itself is clicked
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        Log.i(TAG, "NotificationID: " + Integer.toString(NOTIFICATION_ID));
        NOTIFICATION_ID++;
    }
    
    

}
