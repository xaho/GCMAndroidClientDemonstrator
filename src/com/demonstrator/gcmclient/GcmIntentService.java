package com.demonstrator.gcmclient;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GcmIntentService extends IntentService {

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GIS";
    
	static int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    
    //private static Map<String,Integer> LUT = new HashMap<String,Integer>();//Lookup table for finding NotificationID belong to a MessageID.

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {//This should happen when a message was received from GCM and is being handled 
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
            	showNotification("Send error: ", extras);
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
            	showNotification("Deleted messages on server: ", extras);
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
            	String type = intent.getStringExtra("Type");
            	if (type != null)
            	{
            		//TODO: change to ENUM
	            	switch (type)
	            	{
	            	case "Regular":
	                    showNotification("Received regular: ", extras);
	            		break;
	            	case "Collapsible":
	            		showNotification("Received collapsible: ", extras);
	            		break;
	            	case "NotificationKey":
	            		showNotification("Received NotificationKey: ", extras);
	            		break;
	        		default:
	        			showNotification("Received default: ", extras);
	                    break;
	            	}
            	}
            	else
            		showNotification("Received: ", extras);

                // Post notification of received message.
                Log.i(TAG, "Received extras: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    
    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void showNotification(String prefix, Bundle extras) {
    	String msg = prefix + extras.toString();
    	
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("ContentTitle: GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle().bigText("bigText: " + msg))
        //TODO:if (this message is of a type that should disappear when clicked on directly (not a button in the notification))
        	.setAutoCancel(true)
        .setContentText("contextText: " + msg);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        NOTIFICATION_ID++;
    } 
}
