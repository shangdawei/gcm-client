package com.codingfish.gcm_client;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;


/**
 * Created by achim on 22.08.15.
 */

public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    //private NotificationManager mNotificationManager;

    static final String TAG = "GCM-Client";
    static final String APP_NAME = "GCM-Client";
    static final int MESSAGE_NOTIFICATION_TEXT     = 100;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        if ( !extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String mMessageAction = extras.getString( "message_action");
                if ( mMessageAction != null) {
                    Integer integer = Integer.valueOf(mMessageAction);
                    int action = integer.intValue();
                    switch (action) {
                        case MESSAGE_NOTIFICATION_TEXT: {
                            Log.i( TAG, extras.getString( "message_text"));
                            sendNotification( extras.getString( "message_text"));
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


    private void sendNotification( String msg) {

        NotificationManager mNotificationManager = ( NotificationManager) this.getSystemService( Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity( this, 0, new Intent( this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder( this)
                                                                                .setAutoCancel( true)
                                                                                .setSmallIcon( R.drawable.gcm_client_small)
                                                                                .setLargeIcon( BitmapFactory.decodeResource(getResources(), R.drawable.gcm_client))
                                                                                .setContentTitle( APP_NAME)
                                                                                .setStyle( new NotificationCompat.BigTextStyle().bigText(msg))
                                                                                .setContentText( msg)
                                                                                .setSubText( "Tap notification to start app and read the article.");

        mBuilder.setContentIntent( contentIntent);
        mNotificationManager.notify( NOTIFICATION_ID, mBuilder.build());

    }

}