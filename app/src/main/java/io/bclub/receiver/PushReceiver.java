package io.bclub.receiver;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.v4.app.NotificationCompat;


import com.backendless.messaging.PublishOptions;
import com.backendless.push.BackendlessBroadcastReceiver;

import io.bclub.R;

public class PushReceiver extends BackendlessBroadcastReceiver
{
    @Override
    public boolean onMessage( Context context, Intent intent )
    {
        CharSequence tickerText = intent.getStringExtra( PublishOptions.ANDROID_TICKER_TEXT_TAG );
        CharSequence contentTitle = intent.getStringExtra( PublishOptions.ANDROID_CONTENT_TITLE_TAG );
        CharSequence contentText = intent.getStringExtra( PublishOptions.ANDROID_CONTENT_TEXT_TAG );
        String subtopic = intent.getStringExtra( "message" );

        if( tickerText != null && tickerText.length() > 0 ){
            int appIcon = context.getApplicationInfo().icon;
            if( appIcon == 0 )
                appIcon = R.mipmap.ic_launcher;

//            Intent notificationIntent = new Intent( context, AcceptChatActivity.class );
//            notificationIntent.putExtra( "subtopic", subtopic );
//            PendingIntent contentIntent = PendingIntent.getActivity( context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT );

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder( context );
            notificationBuilder.setSmallIcon( appIcon );
            notificationBuilder.setTicker( tickerText );
            notificationBuilder.setWhen( System.currentTimeMillis() );
            notificationBuilder.setContentTitle( contentTitle );
            //notificationBuilder.setContentText( contentText );
            notificationBuilder.setContentText( subtopic );
            notificationBuilder.setAutoCancel( true );
//            notificationBuilder.setContentIntent( contentIntent );

            Notification notification = notificationBuilder.build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );
            notificationManager.notify( 0, notification );
        }
        return false;
    }

    @Override
    public void onError( Context context, String messageError )
    {
    }
}