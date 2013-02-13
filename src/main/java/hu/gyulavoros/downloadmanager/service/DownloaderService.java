package hu.gyulavoros.downloadmanager.service;

import hu.gyulavoros.downloadmanager.DownloadManagerApplication;
import hu.gyulavoros.downloadmanager.R;
import hu.gyulavoros.downloadmanager.activity.DownloadManagerActivity;
import hu.gyulavoros.downloadmanager.download.DownloadModel;
import hu.gyulavoros.downloadmanager.download.DownloadTask;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public final class DownloaderService extends Service implements Observer {

    public static final String INTENT_ACTION = "hu.gyulavoros.downloadmanager.service.action";
    public static final String INTENT_URL = "hu.gyulavoros.downloadmanager.service.url";

    public static final int ACTION_BIND = 0;
    public static final int ACTION_ADD = 1;

    public final class DownloadBinder extends Binder {

        public DownloaderService getService() {
            return DownloaderService.this;
        }

    }

    protected final static String LOG_TAG = DownloaderService.class.getName();

    protected final List<DownloadTask> downloadTasks = Lists.newArrayList();

    private final DownloadBinder binder = new DownloadBinder();

    private Observer observer;

    public void registerObserver(final Observer newObserver) {
        for (DownloadTask downloadTask : downloadTasks) {
            if (observer != null) {
                downloadTask.deleteObserver(observer);
            }
            downloadTask.addObserver(newObserver);
        }
        this.observer = newObserver;
    }

    protected void startNext() {
        if (DownloadManagerApplication.getInstance().isNetworkAvailable() == false) {
            return;
        }

        DownloadTask nextTask = null;
        for (DownloadTask downloadTask : downloadTasks) {
            if (downloadTask.getState() == DownloadTask.STATE_DOWNLOADING || downloadTask.getState() == DownloadTask.STATE_CONNECTING) {
                return;
            } else if (downloadTask.getState() == DownloadTask.STATE_PENDING) {
                if (nextTask == null) {
                    nextTask = downloadTask;
                }
            }
        }
        if (nextTask != null) {
            nextTask.addObserver(this);
            if (observer != null) {
                nextTask.addObserver(observer);
            }
            nextTask.start();
        }
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.i(LOG_TAG, "onStartCommand: " + intent);
        if (intent.getIntExtra(INTENT_ACTION, 0) == ACTION_BIND) {
            return START_STICKY;
        }
        DownloadManagerApplication.getInstance().getListeningExecutor().execute(new Runnable() {

            @Override
            public void run() {
                final DownloadTask downloadTask = new DownloadTask(intent.getStringExtra(INTENT_URL), DownloadManagerApplication.getInstance().getExternalFilesDir(null).getPath(), 0, null);
                downloadTasks.add(downloadTask);
                final ObjectMapper mapper = new ObjectMapper();
                try {
                    mapper.writeValue(new File(downloadTask.getOutputFolder(), downloadTask.getId() + ".json"), new DownloadModel(downloadTask.getId(), downloadTask.getHttpUrl(), downloadTask.getOutputFolder(), 0));
                } catch (Exception e) {
                    Log.w(LOG_TAG, e);
                }
                startNext();
            }

        });
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "onCreate");

        DownloadManagerApplication.getInstance().getListeningExecutor().execute(new Runnable() {

            @Override
            public void run() {
                final File[] pendingDownloads = getExternalFilesDir(null).listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(final File dir, final String filename) {
                        return filename.endsWith(".json");
                    }

                });

                ObjectMapper mapper = null;
                DownloadModel model = null;
                for (int i = 0; i < pendingDownloads.length; i++) {
                    mapper = new ObjectMapper();
                    try {
                        model = mapper.readValue(pendingDownloads[i], DownloadModel.class);
                        downloadTasks.add(new DownloadTask(model.getHttpUrl(), model.getOutputFolder(), model.getDownloadedBytes(), model.getId()));
                    } catch (final Exception e) {
                        Log.w(LOG_TAG, e);
                    }
                }

                startNext();
            }

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return binder;
    }

    public List<DownloadTask> getDownloadTasks() {
        return downloadTasks;
    }

    @Override
    public void update(final Observable observable, final Object data) {
        final DownloadTask task = (DownloadTask) observable;
        if (task.getState() == DownloadTask.STATE_COMPLETED) {
            final Intent intent = new Intent(DownloadManagerApplication.getInstance(), DownloadManagerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            final NotificationCompat.Builder builder = new Builder(DownloadManagerApplication.getInstance());
            builder.setAutoCancel(true);
            builder.setTicker(getString(R.string.notification_title));
            builder.setContentTitle(getString(R.string.notification_title));
            builder.setContentText(task.getHttpUrl());
            builder.setSmallIcon(R.drawable.av_download_white);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.av_download_white));
            builder.setContentIntent(PendingIntent.getActivity(DownloadManagerApplication.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder.build());
            startNext();
        }
    }

}
