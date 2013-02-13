package hu.gyulavoros.downloadmanager.download;

import hu.gyulavoros.downloadmanager.DownloadManagerApplication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;
import java.util.UUID;
import java.util.concurrent.Callable;

import android.os.SystemClock;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFutureTask;

public final class DownloadTask extends Observable implements Callable<Boolean>, FutureCallback<Boolean>, Serializable {

    private static final long serialVersionUID = 1L;

    protected static final String LOG_TAG = DownloadTask.class.getName();

    public static final int STATE_PENDING = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_DOWNLOADING = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_COMPLETED = 4;
    public static final int STATE_CANCELLED = 5;
    public static final int STATE_ERROR = 6;

    protected static final int BUFFER_SIZE = 16384;

    protected long downloadedBytes;
    protected String httpUrl;
    protected String outputFolder;

    private String id;
    private int state;
    private int fileSize;
    private int downloadSpeed;
    private int sleep;
    private int bandwidthLimitWifi;
    private int bandwidthLimitMobile;
    private boolean onMobileNetwork;

    private ListenableFutureTask<Boolean> task;

    public DownloadTask(final String httpUrl, final String outputFolder, final long downloadedBytes, final String id) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.httpUrl = httpUrl;
        this.outputFolder = outputFolder;
        this.downloadedBytes = downloadedBytes;
        this.fileSize = -1;
        this.bandwidthLimitMobile = Integer.MAX_VALUE;
        this.bandwidthLimitWifi = Integer.MAX_VALUE;
    }

    @Override
    public void onFailure(final Throwable throwable) {
        Log.e(LOG_TAG, "", throwable);
    }

    @Override
    public void onSuccess(final Boolean result) {
        if (result) {
            update();
            return;
        }
        setState(STATE_ERROR);
    }

    @Override
    public Boolean call() throws Exception {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        HttpURLConnection connection = null;
        try {
            final URL url = new URL(httpUrl);

            connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(15000);
            connection.connect();

            if (connection.getResponseCode() / 100 != 2) {
                setState(STATE_ERROR);
                return Boolean.FALSE;
            }

            final int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                setState(STATE_ERROR);
                return Boolean.FALSE;
            }

            if (fileSize == -1) {
                fileSize = contentLength;
                update();
            }

            connection.disconnect();

            connection = (HttpURLConnection) url.openConnection();
            final String byteRange = downloadedBytes + "-" + contentLength;
            connection.setRequestProperty("Range", "bytes=" + byteRange);
            connection.connect();

            setState(STATE_DOWNLOADING);

            inputStream = new BufferedInputStream(connection.getInputStream());

            final String fileURL = url.getFile();
            randomAccessFile = new RandomAccessFile(new File(outputFolder, fileURL.substring(fileURL.lastIndexOf('/') + 1)), "rw");
            randomAccessFile.seek(downloadedBytes);

            long currentMillis = SystemClock.uptimeMillis();
            long timeStamp = currentMillis;
            long deltaTime = 0;
            long deltaFeedback = 0;
            long deltaPersist = 0;
            long deltaBytes = 0;

            final byte data[] = new byte[BUFFER_SIZE];
            int numRead = 0;
            while ((state == STATE_DOWNLOADING) && ((numRead = inputStream.read(data, 0, BUFFER_SIZE)) != -1)) {
                if (sleep > 0) {
                    SystemClock.sleep(sleep);
                }
                randomAccessFile.write(data, 0, numRead);
                downloadedBytes += numRead;
                deltaBytes += numRead;
                currentMillis = SystemClock.uptimeMillis();
                deltaTime += currentMillis - timeStamp;
                deltaFeedback += currentMillis - timeStamp;
                deltaPersist += currentMillis - timeStamp;
                timeStamp = currentMillis;
                if (deltaTime > 1000) {
                    timeStamp = currentMillis;
                    downloadSpeed = (int) (deltaBytes / deltaTime);
                    deltaBytes = 0;
                    deltaTime = 0;
                    update();
                }
                if (deltaFeedback > 1000) {
                    final int difference = Math.abs(onMobileNetwork ? downloadSpeed - bandwidthLimitMobile : downloadSpeed - bandwidthLimitWifi);
                    if ((onMobileNetwork && downloadSpeed > bandwidthLimitMobile) || (onMobileNetwork == false && downloadSpeed > bandwidthLimitWifi)) {
                        if (difference > 500) {
                            sleep += 5;
                        } else if (downloadSpeed - bandwidthLimitWifi > 300) {
                            sleep += 3;
                        } else {
                            sleep++;
                        }
                    } else {
                        if (difference > 500) {
                            sleep -= 5;
                        } else if (difference > 300) {
                            sleep -= 3;
                        } else {
                            sleep--;
                        }
                        if (sleep < 0) {
                            sleep = 0;
                        }
                    }
                    deltaFeedback = 0;
                }
                if (deltaPersist > 5000) {
                    final ObjectMapper mapper = new ObjectMapper();
                    try {
                        mapper.writeValue(new File(outputFolder, id + ".json"), new DownloadModel(id, httpUrl, outputFolder, downloadedBytes));
                    } catch (Exception e) {
                        Log.w(LOG_TAG, e);
                    }
                    deltaPersist = 0;
                }
            }

            if (state == STATE_DOWNLOADING) {
                state = STATE_COMPLETED;
                downloadedBytes = fileSize;
                new File(outputFolder, id + ".json").delete();
            }

            return Boolean.TRUE;
        } catch (final Exception e) {
            Log.e(LOG_TAG, null, e);
            onFailure(e);
            return Boolean.FALSE;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, null, e);
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, null, e);
                }
            }
        }
    }

    public void update() {
        setChanged();
        notifyObservers();
    }

    public void start() {
        setState(STATE_CONNECTING);
        task = ListenableFutureTask.create(this);
        Futures.addCallback(task, this);
        DownloadManagerApplication.getInstance().getListeningExecutor().submit(task);
    }

    public void pause() {
        downloadSpeed = 0;
        setState(STATE_PAUSED);
    }

    public void cancel() {
        setState(STATE_CANCELLED);
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getState() {
        return state;
    }

    public int getProgress() {
        return (int) (((float) downloadedBytes / fileSize) * 100);
    }

    public String getId() {
        return id;
    }

    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public double getDownloadSpeed() {
        return downloadSpeed;
    }

    public long getRemainingBytes() {
        return fileSize - downloadedBytes;
    }

    public int getBandwidthLimitWifi() {
        return bandwidthLimitWifi;
    }

    public void setBandwidthLimitWifi(final int bandwidthLimitWifi) {
        this.bandwidthLimitWifi = bandwidthLimitWifi;
    }

    public int getBandwidthLimitMobile() {
        return bandwidthLimitMobile;
    }

    public void setBandwidthLimitMobile(final int bandwidthLimitMobile) {
        this.bandwidthLimitMobile = bandwidthLimitMobile;
    }

    private void setState(final int newState) {
        state = newState;
        update();
    }

}
