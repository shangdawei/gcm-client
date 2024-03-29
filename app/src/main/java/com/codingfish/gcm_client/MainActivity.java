package com.codingfish.gcm_client;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    static final String TAG = "GCM-Client";

    GoogleCloudMessaging gcm;

    Context context;
    String regid;

    /**
     * This is the project number you got from the API Console."
     */
    String SENDER_ID = "598439751400";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        // Check device for Play Services APK.
        if ( checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance( this);
            regid = getRegistrationId( context);
            if ( regid.isEmpty()) {
                registerInBackground();
            }
        }
        else {
            Log.i( TAG, "No valid Google Play Services APK found.");
        }
    }


    // You need to do the Play Services APK check here, too.
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if ( GooglePlayServicesUtil.isUserRecoverableError( resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i( "Google Play Services: ", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences( Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }


    /**
     * Gets the current registration ID for application on GCM service.
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing registration ID.
     */
    private String getRegistrationId( Context context) {
        final SharedPreferences prefs = getGCMPreferences( context);
        String registrationId = prefs.getString( PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i( TAG, "Registration Id not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt( PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i( TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }


    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion( Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo( context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch ( PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException( "Could not get package name: " + e);
        }
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */

    private void registerInBackground() {
        new AsyncTask<Void,Void,String>() {
            @Override
            protected String doInBackground( Void... params) {
                String msg = "";
                try {
                    if ( gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance( context);
                    }
                    regid = gcm.register( SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    // Send the registration ID to your server over HTTP.
                    sendRegistrationIdToBackend();
                    // Persist the regID - no need to register again.
                    storeRegistrationId( context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {
                Log.i( TAG, "msg: " + msg);
            }
        }.execute();
    }


    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId( Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences( context);
        int appVersion = getAppVersion( context);
        Log.i( TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString( PROPERTY_REG_ID, regId);
        editor.putInt( PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }


    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */

    private void sendRegistrationIdToBackend() {

        // TODO
        // Your implementation here.

    }


}
