package hu.gyulavoros.downloadmanager.activity;

import hu.gyulavoros.downloadmanager.ApplicationEvent;
import hu.gyulavoros.downloadmanager.Constants;
import hu.gyulavoros.downloadmanager.DownloadManagerApplication;
import hu.gyulavoros.downloadmanager.R;
import hu.gyulavoros.downloadmanager.adapter.DownloadersAdapter;
import hu.gyulavoros.downloadmanager.dialog.DownloadOptionsDialog;
import hu.gyulavoros.downloadmanager.dialog.NewDownloadDialog;
import hu.gyulavoros.downloadmanager.download.DownloadTask;
import hu.gyulavoros.downloadmanager.service.DownloaderService;
import hu.gyulavoros.downloadmanager.service.DownloaderService.DownloadBinder;

import java.util.Observable;
import java.util.Observer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.common.eventbus.Subscribe;

public final class DownloadManagerActivity extends SherlockFragmentActivity implements Observer {

    private static final String LOG_TAG = DownloadManagerActivity.class.getName();

    protected boolean serviceConnected;
    protected DownloaderService downloaderService;
    protected ServiceConnection serviceConnection;

    protected Menu optionsMenu;
    protected DownloadersAdapter downloadersAdapter;

    protected ListView listDownloads;

    private TextView textConnectivity;

    private void setConnectivity() {
        if (DownloadManagerApplication.getInstance().isNetworkAvailable()) {
            if (DownloadManagerApplication.getInstance().isMobileNetworkAvailableOnly()) {
                textConnectivity.setText(R.string.main_connectivity_mobile);
                ((LevelListDrawable) textConnectivity.getCompoundDrawables()[0]).setLevel(1);
            } else {
                textConnectivity.setText(R.string.main_connectivity_wifi);
                ((LevelListDrawable) textConnectivity.getCompoundDrawables()[0]).setLevel(2);
            }
        } else {
            textConnectivity.setText(R.string.main_connectivity_none);
            ((LevelListDrawable) textConnectivity.getCompoundDrawables()[0]).setLevel(0);
        }
        onPrepareOptionsMenu(optionsMenu);
    }

    @Subscribe
    public void onEvent(final ApplicationEvent event) {
        if (event == ApplicationEvent.ConnectivityChanged) {
            setConnectivity();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "onCreate " + savedInstanceState);

        setContentView(R.layout.activity_main);
        textConnectivity = (TextView) findViewById(R.id.tv_main_connectivity);
        listDownloads = (ListView) findViewById(R.id.lv_main_downloads);

        listDownloads.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                final DownloadTask task = (DownloadTask) parent.getItemAtPosition(position);
                if (task.getState() == DownloadTask.STATE_DOWNLOADING) {
                    task.pause();
                } else if (task.getState() == DownloadTask.STATE_PAUSED) {
                    task.start();
                }
            }

        });

        listDownloads.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                final DownloadTask task = (DownloadTask) parent.getItemAtPosition(position);
                final Bundle args = new Bundle();
                args.putSerializable(DownloadTask.class.getName(), task);
                final DownloadOptionsDialog optionsDialog = new DownloadOptionsDialog();
                optionsDialog.setArguments(args);
                optionsDialog.show(getSupportFragmentManager(), Constants.FRAGMENT_TAG_DIALOG);
                return true;
            }

        });

        serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(final ComponentName name) {
                serviceConnected = false;
                onPrepareOptionsMenu(optionsMenu);
            }

            @Override
            public void onServiceConnected(final ComponentName name, final IBinder service) {
                serviceConnected = true;
                downloaderService = ((DownloadBinder) service).getService();
                downloaderService.registerObserver(DownloadManagerActivity.this);
                final DownloadersAdapter adapter = new DownloadersAdapter(downloaderService.getDownloadTasks());
                listDownloads.setAdapter(adapter);
                onPrepareOptionsMenu(optionsMenu);
            }

        };
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        optionsMenu = menu;
        menu.add(0, 0, 0, R.string.menu_add).setIcon(R.drawable.content_new).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (menu == null) {
            return false;
        }
        menu.getItem(0).setEnabled(serviceConnected && DownloadManagerApplication.getInstance().isNetworkAvailable());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (serviceConnected == false) {
            return false;
        }
        if (item.getItemId() == 0) {
            final DialogFragment addDownloadDialog = new NewDownloadDialog();
            addDownloadDialog.show(getSupportFragmentManager(), Constants.FRAGMENT_TAG_DIALOG);
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        DownloadManagerApplication.getInstance().getEventBus().register(this);
        bindService(new Intent(DownloadManagerApplication.getInstance(), DownloaderService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        DownloadManagerApplication.getInstance().getEventBus().unregister(this);
        unbindService(serviceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setConnectivity();
    }

    @Override
    public void update(final Observable observable, final Object data) {
        DownloadManagerApplication.getInstance().getHandler().post(new Runnable() {

            @Override
            public void run() {
                ((BaseAdapter) listDownloads.getAdapter()).notifyDataSetChanged();
            }

        });
    }

}
