package com.edgesoft.resulthour;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.edgsoft.resulthour.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.File;

import static androidx.core.content.FileProvider.getUriForFile;

public class PDFActivity extends AppCompatActivity {

    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        PDFView pdfView = findViewById(R.id.pdf_view);

        String fileName = getIntent().getStringExtra("filePath");
        file = new File(PDFActivity.this.getFilesDir(),fileName);
        pdfView.fromFile(file).load();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pdf,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pdf_share:
                shareFile(PDFActivity.this,file);
                break;
            case R.id.menu_pdf_report:
                startActivity(new Intent(PDFActivity.this,ReportActivity.class));
        }
        return true;
    }

    private void shareFile(Context context, File file) {
        Uri contentUri = getUriForFile(context, context.getPackageName(), file);
        Intent shareContentIntent = new Intent(Intent.ACTION_SEND, contentUri);
        shareContentIntent.setDataAndType(contentUri, "application/pdf");
        shareContentIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareContentIntent.putExtra(Intent.EXTRA_SUBJECT,"File from Result Hour app");
        shareContentIntent.putExtra(Intent.EXTRA_TEXT,"Download Result Hour App for more such contents\n"+
                "https://play.google.com/store/apps/details?id="+getApplicationContext().getPackageName());
        shareContentIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareContentIntent, "Share with"));
    }


}