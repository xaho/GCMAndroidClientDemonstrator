package com.demonstrator.gcmclient;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	/**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "673775556614";
    //Your GCM sender id, shown on console.developer.google.com -> select Project -> Project Number
    //(Should be suffixed with L in java code to indicate Long int value) 
    
    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Demonstrator";
    
    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    String regid;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
    	mDisplay = (TextView) findViewById(R.id.textview);
    	
        context = getApplicationContext();
        
	    // Check device for Play Services APK.
	    if (checkPlayServices()) {
	        // If this check succeeds, proceed with normal processing.
	        // Otherwise, prompt user to get valid Play Services APK.
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
            else
            {
            	mDisplay.setText("Device registered, registration ID=" + regid);
            }
	    }
	}
	
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
	    new AsyncTask<Void, Void, String>() {
	        @Override
	        protected String doInBackground(Void... params) {
	            String msg = "";
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                regid = gcm.register(SENDER_ID);
	                msg = "Device registered, registration ID=" + regid;

	                // You should send the registration ID to your server over HTTP,
	                // so it can use GCM/HTTP or CCS to send messages to your app.
	                // The request to your server should be authenticated if your app
	                // is using accounts.
	                sendRegistrationIdToBackend();

	                // For this demo: we don't need to send it because the device
	                // will send upstream messages to a server that echo back the
	                // message using the 'from' address in the message.

	                // Persist the regID - no need to register again.
	                storeRegistrationId(context, regid);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	                // If there is an error, don't just keep trying to register.
	                // Require the user to click a button again, or perform
	                // exponential back-off.
	            }
	            return msg;
	        }

	        @Override
	        protected void onPostExecute(String msg) {
	            mDisplay.append(msg + "\n");
	        }
	    }.execute(null, null, null);
	    
	}
	
	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
	 * or CCS to send messages to your app. Not needed for this demo since the
	 * device sends upstream messages to a server that echoes back the message
	 * using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
	    // Your implementation here.
		// Manual for the time being
	}
	
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    int appVersion = getAppVersion(context);
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}
	
	public void onClick(final View view) {
	    if (view == findViewById(R.id.send)) {
	    	mDisplay.setText("onClick()");
	    	System.out.println("Send button pressed");
	        new AsyncTask<Void, Void, String>() {
	            @Override
	            protected String doInBackground(Void... params) {
	                String msg = "";
	                try {
	                    Bundle data = new Bundle();
	                        data.putString("my_message", "Hello World");
	                        data.putString("my_action",
	                                "com.google.android.gcm.demo.app.ECHO_NOW");
	                        String id = Integer.toString(msgId.incrementAndGet());
	                        gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
	                        msg = "Sent message";
	                } catch (IOException ex) {
	                    msg = "Error :" + ex.getMessage();
	                }
	                return msg;
	            }

	            @Override
	            protected void onPostExecute(String msg) {
	                mDisplay.setText(msg + "\n");
	            }
	        }.execute(null, null, null);
	    } else if (view == findViewById(R.id.notify)) 
	    {
	        //show notification with chosen parameters
	    	//get UI input
	    	CheckBox cbsetAutoCancel = (CheckBox)findViewById(R.id.cbsetAutoCancel);
	    	CheckBox cbsetContentInfo = (CheckBox)findViewById(R.id.cbsetContentInfo);
	    	CheckBox cbsetContentIntent = (CheckBox)findViewById(R.id.cbsetContentIntent);
	    	CheckBox cbsetContentText = (CheckBox)findViewById(R.id.cbsetContentText);
	    	CheckBox cbsetContentTitle = (CheckBox)findViewById(R.id.cbsetContentTitle);
	    	CheckBox cbsetNumber = (CheckBox)findViewById(R.id.cbsetNumber);
	    	CheckBox cbsetOngoing = (CheckBox)findViewById(R.id.cbsetOngoing);
	    	CheckBox cbsetAction = (CheckBox)findViewById(R.id.cbsetAction);
	    	CheckBox cbsetStyle = (CheckBox)findViewById(R.id.cbsetStyle);
	    	CheckBox cbsetSubtext = (CheckBox)findViewById(R.id.cbsetSubtext);
	    	CheckBox cbsetTicker = (CheckBox)findViewById(R.id.cbsetTicker);
	    	RadioButton rbsmallIcon = (RadioButton)findViewById(R.id.rbsetSmallIcon);
	    	RadioButton rblargeIcon = (RadioButton)findViewById(R.id.rbsetLargeIcon);
	    	

	        NotificationManager mNotificationManager = (NotificationManager)
	                this.getSystemService(Context.NOTIFICATION_SERVICE);
	    	
	    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                new Intent(this, MainActivity.class), 0);
	    	
	    	NotificationCompat.Builder mBuilder =
	                new NotificationCompat.Builder(this);
	    	Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
	    	
	        if (cbsetAction.isChecked())
	        {
		    	mBuilder.addAction(R.drawable.ic_launcher , "<Action 1>", contentIntent);
		    	mBuilder.addAction(R.drawable.ic_launcher , "<Action 2>", contentIntent);
		    	mBuilder.addAction(R.drawable.ic_launcher , "<Action 3>", contentIntent);
	        }
	    	if (cbsetAutoCancel.isChecked())
	    		mBuilder.setAutoCancel(true);
	    	if (cbsetContentInfo.isChecked())
	    		mBuilder.setContentInfo("<ContentInfo>");
	    	if (cbsetContentIntent.isChecked())
	    		mBuilder.setContentIntent(contentIntent);
	        if (cbsetContentText.isChecked())
	        	mBuilder.setContentText("<ContextText>");
	        if (cbsetContentTitle.isChecked())
	        	mBuilder.setContentTitle("<ContentTitle>");
	        if (cbsetNumber.isChecked())
	        	mBuilder.setNumber(42);
	    	if (cbsetOngoing.isChecked())
	    		mBuilder.setOngoing(true);
	        if (cbsetStyle.isChecked())
	        	mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("<BigText>"));
	        if (cbsetSubtext.isChecked())
	        	mBuilder.setSubText("<SubText>");
	        if (cbsetTicker.isChecked())
	        	mBuilder.setTicker("<Ticker>");
	        
	        if (rbsmallIcon.isChecked())
	    		mBuilder.setSmallIcon(R.drawable.ic_launcher);
	    	else
	    		mBuilder.setLargeIcon(bm);
	    	mNotificationManager.notify(42, mBuilder.build());
	    }
	}
	
	// You need to do the Play Services APK check here too.
	@Override
	protected void onResume() {
	    super.onResume();
	    checkPlayServices();
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device is not supported.");
	            finish();
	        }
	        return false;
	    }
	    Log.i(TAG, "This device is supported.");
	    return true;
	}
	
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    // Depends on implementation... not sure how to handle this, has downside: 
	    // update of application bricks connection between server and client via GCM even if it might still work...
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    System.out.println(registrationId);
	    return registrationId;
	}
	
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return getSharedPreferences(MainActivity.class.getSimpleName(),
	            Context.MODE_PRIVATE);
	}
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
