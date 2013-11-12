package hu.gyulavoros.downloadmanager.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import hu.gyulavoros.downloadmanager.R;
import hu.gyulavoros.downloadmanager.adapter.BandwidthLimitAdapter;
import hu.gyulavoros.downloadmanager.download.DownloadTask;

public final class DownloadOptionsDialog extends DialogFragment {

    protected DownloadTask downloadTask;

    protected Spinner spinnerWifi;
    protected Spinner spinnerMobile;

    private Button btnOk;
    private Button btnCancel;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloadTask = (DownloadTask) (getArguments() == null ? null : getArguments().getSerializable(DownloadTask.class.getName()));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        getDialog().setTitle(R.string.dialog_options_title);

        final View view = inflater.inflate(R.layout.dialog_download_options, container, false);

        spinnerWifi = (Spinner) view.findViewById(R.id.s_download_options_wifi);
        spinnerMobile = (Spinner) view.findViewById(R.id.s_download_options_mobile);
        btnOk = (Button) view.findViewById(R.id.b_download_options_ok);
        btnCancel = (Button) view.findViewById(R.id.b_download_options_cancel);

        spinnerWifi.setAdapter(new BandwidthLimitAdapter());
        spinnerMobile.setAdapter(new BandwidthLimitAdapter());

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                getDialog().dismiss();
            }

        });

        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                downloadTask.setBandwidthLimitWifi((Integer) spinnerWifi.getSelectedItem());
                downloadTask.setBandwidthLimitMobile((Integer) spinnerMobile.getSelectedItem());
                getDialog().dismiss();
            }

        });

        for (int i = 0; i < BandwidthLimitAdapter.LIMITS.length; i++) {
            if (BandwidthLimitAdapter.LIMITS[i] == downloadTask.getBandwidthLimitWifi()) {
                spinnerWifi.setSelection(i);
            }
            if (BandwidthLimitAdapter.LIMITS[i] == downloadTask.getBandwidthLimitMobile()) {
                spinnerMobile.setSelection(i);
            }
        }

        return view;
    }

}
