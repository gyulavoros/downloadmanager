package hu.gyulavoros.downloadmanager.adapter;

import hu.gyulavoros.downloadmanager.DownloadManagerApplication;
import hu.gyulavoros.downloadmanager.R;
import hu.gyulavoros.downloadmanager.Utils;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public final class BandwidthLimitAdapter implements SpinnerAdapter {

    public static final int[] LIMITS = new int[] { 100, 200, 500, 1024, 1536, 2048, 3072, 5120 };

    private final LayoutInflater inflater;

    public BandwidthLimitAdapter() {
        this.inflater = LayoutInflater.from(DownloadManagerApplication.getInstance());
    }

    @Override
    public int getCount() {
        return LIMITS.length;
    }

    @Override
    public Integer getItem(final int position) {
        return LIMITS[position];
    }

    @Override
    public long getItemId(final int position) {
        return 0;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        TextView item = (TextView) convertView;

        if (item == null) {
            item = (TextView) inflater.inflate(R.layout.item_bandwidth_limit, parent, false);
        }
        item.setText(Utils.formatDownloadSpeed(getItem(position)));

        return item;
    }

    @Override
    public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public int getItemViewType(final int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        // not used
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // not used
    }

}
