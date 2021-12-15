package com.edgesoft.resulthour;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.edgesoft.resulthour.Adapter.ResultListAdapter;
import com.edgesoft.resulthour.Interface.OnBottomReachedListener;
import com.edgesoft.resulthour.Model.ResultData;
import com.edgsoft.resulthour.R;
import com.github.clemp6r.futuroid.Async;
import com.github.clemp6r.futuroid.Future;
import com.github.clemp6r.futuroid.FutureCallback;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ResultListActivity extends AppCompatActivity  {
    private RecyclerView recyclerView;
    private ResultListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Object> dataList = new ArrayList<>();
    private RelativeLayout relativeLayout;
    private String intent_url;
    private ProgressBar pBar;
    private int nextPage = 2;
    private InterstitialAd mInterstitialAd;
    private static final String TAG = SearchResultActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_list);
        loadInterstitialAd();

        // Get Intent URL
        Intent intent = getIntent();
        intent_url = intent.getStringExtra("url");
        pBar = findViewById(R.id.result_list_pbar);
        // Setting Action Bar Title
        setTitle("Results");

        recyclerView = findViewById(R.id.result_list_recycler);
        layoutManager = new LinearLayoutManager(this);
        relativeLayout = findViewById(R.id.result_list_root_layout);
        adapter = new ResultListAdapter(dataList);

        GetResultDataService service = new GetResultDataService();
        pBar.setVisibility(View.VISIBLE);
        service.getResultData(intent_url).addCallback(new FutureCallback<List<Object>>() {
            @Override
            public void onSuccess(List<Object> result) {
                addBannerAds(0);
                pBar.setVisibility(View.GONE);
                loadRecyclerView();
            }
            @Override
            public void onFailure(Throwable t) {
                pBar.setVisibility(View.GONE);
                Toast toast = Toast.makeText(ResultListActivity.this, "Failed to get results.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
        });

        adapter.setOnBottomReachedListener(new OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                service.getResultData(intent_url+nextPage).addCallback(new FutureCallback<List<Object>>() {
                    @Override
                    public void onSuccess(List<Object> result) {
                        adapter.notifyDataSetChanged();
                        nextPage++;
                    }
                    @Override
                    public void onFailure(Throwable t) {
                        pBar.setVisibility(View.GONE);
                        Toast toast = Toast.makeText(ResultListActivity.this, R.string.def_err_msg, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                    }
                });
            }
        });

    }

    public void loadRecyclerView() {
        if(dataList.size() > 0) {
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
        }
    }


    public class GetResultDataService {

        public Future<List<Object>> getResultData(final String url) {
            return Async.submit(new Callable<List<Object>>() {
                @Override
                public List<Object> call() {

                    Document document = null;
                    try {
                        document = Jsoup.connect(url).get();
                        for (org.jsoup.nodes.Element div : document.select("div[class=list-group]")) {
                            for (org.jsoup.nodes.Element row : div.select("a[title]")) {
                                String date = row.children().select("b[class=date]").text();
                                String title = row.ownText();
                                String url = row.attr("href");
                                int lastIndex =  url.lastIndexOf(".");
                                int startIndex = url.lastIndexOf("/");
                                String exam_id = url.substring(startIndex+1,lastIndex);
                                ResultData data = new ResultData(title,date,exam_id,getIntent().getStringExtra("university"));
                                dataList.add(data);
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    return dataList;
                }
            });
        }
    }

    private void addBannerAds(int startIndex) {
        for (int i = startIndex+4;i<dataList.size();i+=4) {
            dataList.add(i,new AdView(ResultListActivity.this));
        }
    }

    @Override
    public void onBackPressed() {
        if(mInterstitialAd != null) {
            mInterstitialAd.show(ResultListActivity.this);
        }
        super.onBackPressed();
    }

    private void loadInterstitialAd() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,getResources().getString(R.string.interstitial_ad_unit), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }
}