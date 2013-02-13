package hu.gyulavoros.downloadmanager.adapter;

import hu.gyulavoros.downloadmanager.DownloadManagerApplication;
import hu.gyulavoros.downloadmanager.R;
import hu.gyulavoros.downloadmanager.Utils;
import hu.gyulavoros.downloadmanager.download.DownloadTask;

import java.util.List;

import android.content.res.Resources;
import android.graphics.drawable.LevelListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public final class DownloadersAdapter extends BaseAdapter {

    protected final class ViewHolder {

        protected TextView textName;
        protected TextView textSpeed;
        protected TextView textDownloaded;
        protected TextView textRemaining;
        protected ImageView imageStatus;
        protected ProgressBar progress;

    }

    private final LayoutInflater inflater;
    private final Resources resources;
    private final List<DownloadTask> downloadTasks;

    public DownloadersAdapter(final List<DownloadTask> downloadTasks) {
        this.inflater = LayoutInflater.from(DownloadManagerApplication.getInstance());
        this.resources = DownloadManagerApplication.getInstance().getResources();
        this.downloadTasks = downloadTasks;
    }

    @Override
    public int getCount() {
        return downloadTasks.size();
    }

    @Override
    public DownloadTask getItem(final int position) {
        return downloadTasks.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return 0;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;

        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.item_download, parent, false);
            view.setTag(holder);
            holder.textName = (TextView) view.findViewById(R.id.tv_item_download_name);
            holder.textSpeed = (TextView) view.findViewById(R.id.tv_item_download_speed);
            holder.textDownloaded = (TextView) view.findViewById(R.id.tv_item_download_downloaded);
            holder.textRemaining = (TextView) view.findViewById(R.id.tv_item_download_remaining);
            holder.progress = (ProgressBar) view.findViewById(R.id.pb_item_download_progress);
            holder.imageStatus = (ImageView) view.findViewById(R.id.iv_item_download_status);
            holder.progress.setMax(100);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final DownloadTask task = getItem(position);
        holder.textName.setText(task.getHttpUrl());
        holder.progress.setProgress(task.getProgress());
        holder.textSpeed.setText(Utils.formatDownloadSpeed(task.getDownloadSpeed()));
        holder.textDownloaded.setText(Utils.formatDownloadSize(task.getDownloadedBytes() / 1024.0));
        holder.textRemaining.setText("- " + Utils.formatDownloadSize(task.getRemainingBytes() / 1024.0));

        setStatsVisibility(holder, View.VISIBLE);
        view.setBackgroundColor(resources.getColor(R.color.transparent));

        final int state = task.getState();
        int level = 0;
        switch (state) {
            case DownloadTask.STATE_PENDING:
                level = 0;
                setStatsVisibility(holder, View.INVISIBLE);
                break;
            case DownloadTask.STATE_CONNECTING:
                level = 1;
                setStatsVisibility(holder, View.INVISIBLE);
                break;
            case DownloadTask.STATE_DOWNLOADING:
                level = 2;
                break;
            case DownloadTask.STATE_PAUSED:
                level = 3;
                holder.textSpeed.setVisibility(View.INVISIBLE);
                break;
            case DownloadTask.STATE_COMPLETED:
                level = 4;
                holder.textSpeed.setVisibility(View.INVISIBLE);
                holder.textRemaining.setVisibility(View.INVISIBLE);
                break;
            case DownloadTask.STATE_ERROR:
                level = 6;
                holder.textSpeed.setVisibility(View.INVISIBLE);
                view.setBackgroundColor(resources.getColor(R.color.red));
                break;
            default:
                break;
        }
        ((LevelListDrawable) holder.imageStatus.getDrawable()).setLevel(level);

        return view;
    }

    private static void setStatsVisibility(final ViewHolder holder, final int visibility) {
        holder.textDownloaded.setVisibility(visibility);
        holder.textRemaining.setVisibility(visibility);
        holder.textSpeed.setVisibility(visibility);
    }

}
