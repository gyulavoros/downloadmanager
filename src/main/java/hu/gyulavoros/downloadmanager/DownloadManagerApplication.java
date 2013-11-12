package hu.gyulavoros.downloadmanager;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Process;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import hu.gyulavoros.downloadmanager.broadcast.ConnectivityBroadcastReceiver;

import java.util.concurrent.Executors;

public final class DownloadManagerApplication extends Application {

    public static int NEXT = 0;

    private static DownloadManagerApplication instance;

    private boolean connectionAvailable;
    private Handler handler;
    private EventBus eventBus;
    private ConnectivityManager connectivityManager;
    private ListeningScheduledExecutorService listeningExecutor;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        handler = new Handler();
        eventBus = new EventBus();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        registerReceiver(new ConnectivityBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        final ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        builder.setNameFormat("download-worker-%d");
        listeningExecutor = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(3, builder.build()));
    }

    public static DownloadManagerApplication getInstance() {
        return instance;
    }

    public boolean isNetworkAvailable() {
        boolean mobileNetworkAvailable = false;
        boolean wifiNetworkAvailable = false;

        final NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
        for (int i = 0; i < infos.length; i++) {
            final NetworkInfo info = infos[i];
            if (info.getType() == ConnectivityManager.TYPE_MOBILE && info.isConnected()) {
                mobileNetworkAvailable = true;
            }
            if (info.getType() == ConnectivityManager.TYPE_WIFI && info.isConnected()) {
                wifiNetworkAvailable = true;
            }
        }

        return mobileNetworkAvailable || wifiNetworkAvailable;
    }

    public boolean isMobileNetworkAvailableOnly() {
        boolean mobileNetworkAvailable = true;
        boolean wifiNetworkAvailable = true;

        final NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
        for (int i = 0; i < infos.length; i++) {
            final NetworkInfo info = infos[i];
            if (info.getType() == ConnectivityManager.TYPE_MOBILE && !info.isConnected()) {
                mobileNetworkAvailable = false;
            }
            if (info.getType() == ConnectivityManager.TYPE_WIFI && !info.isConnected()) {
                wifiNetworkAvailable = false;
            }
        }

        return mobileNetworkAvailable && !wifiNetworkAvailable;
    }

    public ListeningScheduledExecutorService getListeningExecutor() {
        return listeningExecutor;
    }

    public Handler getHandler() {
        return handler;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public boolean isConnectionAvailable() {
        return connectionAvailable;
    }

    public void setConnectionAvailable(final boolean connectionAvailable) {
        this.connectionAvailable = connectionAvailable;
    }

}
