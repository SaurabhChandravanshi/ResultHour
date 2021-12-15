package com.edgesoft.resulthour.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.edgesoft.resulthour.PDFActivity;
import com.edgsoft.resulthour.R;
import com.github.clemp6r.futuroid.Async;
import com.github.clemp6r.futuroid.Future;
import com.github.clemp6r.futuroid.FutureCallback;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.concurrent.Callable;

public class DownloadFragment extends BottomSheetDialogFragment {

    private int downloadId;
    private TextView downloadTitle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button cancelBtn = view.findViewById(R.id.download_cancel);
        downloadTitle = view.findViewById(R.id.download_title);
        if(getArguments() != null)
            openFileFromUrl();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PRDownloader.cancel(downloadId);
                dismiss();
            }
        });
    }

    private void openFileFromUrl() {
        if(getArguments().getString("content").equals("qp")) {
            GetQPUrl qpUrl = new GetQPUrl();
            qpUrl.getFileUrl(getArguments().getString("url"))
                    .addCallback(new FutureCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            downloadFile(result);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Toast.makeText(getActivity(), R.string.def_err_msg, Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    });
        } else if (getArguments().getString("content").equals("directUrl")) {
            downloadFile(getArguments().getString("url"));
        }
    }

    private void downloadFile(String url) {
        String filename = url.substring(url.lastIndexOf("/")+1);

        downloadId = PRDownloader.download(url,getActivity().getFilesDir().getPath(),filename)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        dismiss();
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long percent = (progress.currentBytes*100/progress.totalBytes);
                        if(percent <= 100 && percent >= 0) {
                            downloadTitle.setText("Opening file "+percent+"% complete.");
                        } else {
                            downloadTitle.setText("Opening file.");
                        }
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        // open file in PDF Viewer.
                        Intent pdfIntent  = new Intent(getContext(), PDFActivity.class);
                        pdfIntent.putExtra("filePath",filename);
                        getActivity().startActivity(pdfIntent);
                        dismiss();
                    }

                    @Override
                    public void onError(Error error) {
                        Toast.makeText(getActivity(), "Failed to download file, Please retry or contact us if needed.", Toast.LENGTH_LONG).show();
                        dismiss();
                    }

                });
    }

    static class GetQPUrl {
        public Future<String> getFileUrl(String url) {
            return Async.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    Document document = Jsoup.connect(url).get();
                    Element object = document.select("object[type=application/pdf]").first();
                    return object.attr("data");
                }
            });
        }
    }
}
