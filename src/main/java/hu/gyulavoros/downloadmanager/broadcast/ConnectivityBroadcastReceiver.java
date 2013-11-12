package hu.gyulavoros.downloadmanager.broadcast;

import hu.gyulavoros.downloadmanager.ApplicationEvent;
import hu.gyulavoros.downloadmanager.DownloadManagerApplication;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.net.ConnectivityManagerCompat;
import android.util.Log;

public final class ConnectivityBroadcastReceiver extends BroadcastReceiver {

    private final static String LOG_TAG = ConnectivityBroadcastReceiver.class.getName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = ConnectivityManagerCompat.getNetworkInfoFromBroadcast(connectivityManager, intent);
        final DownloadManagerApplication application = DownloadManagerApplication.getInstance();

        if (null == info) {
            application.setConnectionAvailable(false);
            application.getEventBus().post(ApplicationEvent.ConnectionLost);
        } else {
            boolean hasConnection = false;
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.i(LOG_TAG, "connection changed - wifi: " + info.isConnected());
                hasConnection = info.isConnected();
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                Log.i(LOG_TAG, "connection changed - mobile: " + info.isConnected());
                if (hasConnection == false) {
                    hasConnection = info.isConnected();
                }
            }

            if (application.isConnectionAvailable() == false) {
                application.getEventBus().post(ApplicationEvent.ConnectionLost);
            }

            application.getEventBus().post(ApplicationEvent.ConnectivityChanged);
        }
    }

}