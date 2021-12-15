package com.edgesoft.resulthour;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.edgsoft.resulthour.R;
import com.github.clemp6r.futuroid.Async;
import com.github.clemp6r.futuroid.Future;
import com.github.clemp6r.futuroid.FutureCallback;

import org.jsoup.Jsoup;

import java.util.List;
import java.util.concurrent.Callable;

public class ResultWebViewActivity extends AppCompatActivity {
    private String url;
    private WebView webView;
    private ProgressBar progressBar;
    private String html;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_web_view);



        Intent intent = getIntent();
        url = "https://www.resulthour.com"+intent.getStringExtra("url");
        webView = findViewById(R.id.result_web_view);
        webView.setWebViewClient(new MyWebViewClient());
        progressBar=findViewById(R.id.result_web_view_pbar);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        GetResultTask task = new GetResultTask();
        task.getStringHtml(url).addCallback(new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String formatHtml = "<!DOCTYPE html><html><body>"+result+"</body></html>";
                webView.loadDataWithBaseURL(null,formatHtml,"text/html","utf-8",null);
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_your_result,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_result_print: {
                printResult();
            }
        }
        return true;
    }

    private void printResult() {
        PrintManager printManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();
            String jobName = "Result Document";
            PrintAttributes.Builder builder = new PrintAttributes.Builder();
            builder.setMediaSize(PrintAttributes.MediaSize.ISO_A5);
            PrintJob printJob = printManager.print(jobName, printAdapter, builder.build());

            if(printJob.isCompleted()){
                Toast.makeText(getApplicationContext(), "Print completed", Toast.LENGTH_LONG).show();
            }
            else if(printJob.isFailed()){
                Toast.makeText(getApplicationContext(),"Print failed", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Print is unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    public static class GetResultTask {
        public Future<String> getStringHtml(final String url) {
            return Async.submit(new Callable<String>() {
                @Override
                public String call() {
                    String stringHtml = "";
                    try {
                        org.jsoup.nodes.Document document = Jsoup.connect(url).get();
                        String coreHtml = document.select("div[id=resultdiv]").html().replaceAll("&amp;"," AND ").replaceAll("amp;","");
                        stringHtml = "<html><style>" +"img {display:none;margin-left:0;margin-right:0;width:20%}"+"td {color:black;font-family:verdana;text-transform:uppercase}"
                                +"h1,h4 {color:dodgerblue;font-family:verdana;text-align:center;text-transform:uppercase}" +
                                "" + "td {border:1px solid darkslategray;padding:5px}"+
                                "</style><body>"+coreHtml+"</body></html>";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return stringHtml;
                }
                });
            }
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading (WebView webView, String url){

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            progressBar.setVisibility(View.VISIBLE);
        }

    }

}

