package hu.gyulavoros.downloadmanager.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import hu.gyulavoros.downloadmanager.Constants;
import hu.gyulavoros.downloadmanager.DownloadManagerApplication;
import hu.gyulavoros.downloadmanager.R;
import hu.gyulavoros.downloadmanager.service.DownloaderService;

public final class NewDownloadDialog extends DialogFragment {

    protected EditText editDownloadUrl;

    private Button btnOk;
    private Button btnCancel;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        getDialog().setTitle(R.string.dialog_add_title);

        final View view = inflater.inflate(R.layout.dialog_new_download, container, false);

        editDownloadUrl = (EditText) view.findViewById(R.id.et_add_download);
        btnOk = (Button) view.findViewById(R.id.b_add_download_ok);
        btnCancel = (Button) view.findViewById(R.id.b_add_download_cancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                getDialog().dismiss();
            }

        });

        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                final Intent intent = new Intent(DownloadManagerApplication.getInstance(), DownloaderService.class);
                intent.putExtra(DownloaderService.INTENT_ACTION, DownloaderService.ACTION_ADD);
                intent.putExtra(DownloaderService.INTENT_URL, editDownloadUrl.getText().toString());
                getActivity().startService(intent);
                getDialog().dismiss();
                DownloadManagerApplication.NEXT++;
            }

        });

        editDownloadUrl.setText(Constants.DOWNLOADS[DownloadManagerApplication.NEXT % 4]);

        return view;
    }

}
