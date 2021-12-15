package com.edgesoft.resulthour;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.edgesoft.resulthour.Fragment.DownloadFragment;
import com.edgesoft.resulthour.Model.QuestionPaper;
import com.edgsoft.resulthour.R;
import com.github.clemp6r.futuroid.Async;
import com.github.clemp6r.futuroid.Future;
import com.github.clemp6r.futuroid.FutureCallback;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class QPActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;
    private RecyclerView.LayoutManager layoutManager;
    private final List<Object> dataList = new ArrayList<>();
    private ProgressBar pBar;
    private InterstitialAd mInterstitialAd;
    private static final String TAG = QPActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qp);
        loadInterstitialAds();
        initial();
        getData();
    }

    @Override
    public void onBackPressed() {
        if(mInterstitialAd != null) {
            mInterstitialAd.show(QPActivity.this);
        }
        super.onBackPressed();
    }

    private void initial() {
        recyclerView = findViewById(R.id.qp_recycler);
        pBar = findViewById(R.id.qp_pBar);
        adapter = new MyAdapter(dataList, getSupportFragmentManager());
        layoutManager = new LinearLayoutManager(this);
    }

    private void getData() {
        GetDataService service = new GetDataService();
        service.getData(getIntent().getStringExtra("url"))
                .addCallback(new FutureCallback<List<Object>>() {
                    @Override
                    public void onSuccess(List<Object> result) {
                        pBar.setVisibility(View.GONE);
                        if(result != null)
                            loadRecyclerView();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        pBar.setVisibility(View.GONE);
                        Toast.makeText(QPActivity.this, R.string.def_err_msg,Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadRecyclerView() {
        if (dataList.size() > 0) {
            addAdVies();
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(QPActivity.this, R.string.def_err_msg, Toast.LENGTH_LONG).show();
        }
    }

    private void addAdVies() {
        for (int i=8;i<dataList.size();i+=8) {
            dataList.add(i,new AdView(this));
        }
    }


    public class GetDataService {

        public Future<List<Object>> getData(final String url) {
            return Async.submit(new Callable<List<Object>>() {
                @Override
                public List<Object> call() {

                    Document document = null;
                    try {
                        document = Jsoup.connect(url).get();
                        Element div  = document.select("div[class=list-group]").get(1);
                            for (org.jsoup.nodes.Element row : div.select("a[class=list-group-item]")) {
                                String date = row.children().select("b[class=date]").text();
                                String title = row.attr("title");
                                String url = "https://resulthour.com"+row.attr("href");
                                QuestionPaper data = new QuestionPaper(date,title,url,getIntent().getStringExtra("university"));
                                dataList.add(data);
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

    class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<Object> dataList;
        private FragmentManager fragmentManager;
        private static final int VIEW_TYPE_AD = 0;
        private static final int VIEW_TYPE_CONTENT = 1;

        public MyAdapter(List<Object> dataList, FragmentManager fragmentManager) {
            this.dataList = dataList;
            this.fragmentManager = fragmentManager;
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0)
                return VIEW_TYPE_CONTENT;
            else
                if (position % 8 == 0)
                    return VIEW_TYPE_AD;
                return VIEW_TYPE_CONTENT;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_AD) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_banner_frame,null);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.qp_list,null);
            }
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MyHolder myHolder = (MyHolder)holder;
            if(getItemViewType(position) == VIEW_TYPE_CONTENT && dataList.get(position) instanceof QuestionPaper) {
                QuestionPaper data = (QuestionPaper) dataList.get(position);
                myHolder.date.setText(data.getDate());
                myHolder.title.setText(data.getTitle());
                myHolder.university.setText(data.getUniversity());

                Typeface tfRegular = ResourcesCompat.getFont(myHolder.itemView.getContext(),R.font.metropolis_regular);
                myHolder.title.setTypeface(tfRegular,Typeface.BOLD);
                myHolder.university.setTypeface(tfRegular);

                myHolder.title.setCompoundDrawablePadding(15);
                myHolder.university.setCompoundDrawablePadding(15);
                myHolder.university.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_account_balance_24,0,0,0);
                myHolder.title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_error_outline_12,0,0,0);

                myHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DownloadFragment downloadPFragment = new DownloadFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("url",data.getUrl());
                        bundle.putString("content","qp");
                        downloadPFragment.setArguments(bundle);
                        downloadPFragment.show(fragmentManager,downloadPFragment.getTag());
                    }
                });
                PopupMenu.OnMenuItemClickListener menuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.recycler_share:
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_SUBJECT,"Result Hour Question Paper");
                                intent.putExtra(Intent.EXTRA_TEXT,"Download Question Paper for "+data.getTitle()+"\nOn Result Hour App\n"+
                                        "https://play.google.com/store/apps/details?id="+myHolder.itemView.getContext().getPackageName());
                                myHolder.itemView.getContext().startActivity(Intent.createChooser(intent,"Share with"));
                        }
                        return true;
                    }
                };
                myHolder.menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(myHolder.itemView.getContext(), v);
                        popup.setOnMenuItemClickListener(menuItemClickListener);
                        popup.inflate(R.menu.menu_recycler);
                        popup.show();
                    }
                });
            } else if (dataList.get(position) instanceof AdView) {
                // Populate banner ads here.
                myHolder.refreshAd(myHolder.itemView);
            }
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        class MyHolder extends RecyclerView.ViewHolder {
            private final TextView date,title,university;
            private final CardView cardView;
            private final ImageView menu;
            private NativeAd mNativeAd;

            public MyHolder(@NonNull View itemView) {
                super(itemView);
                date = itemView.findViewById(R.id.qp_list_release_date);
                title = itemView.findViewById(R.id.qp_list_title);
                university = itemView.findViewById(R.id.qp_list_university);
                cardView = itemView.findViewById(R.id.qp_list_card);
                menu = itemView.findViewById(R.id.qp_list_context_menu);
            }
            private void refreshAd(View view) {
                AdLoader.Builder builder = new AdLoader.Builder(view.getContext(), view.getContext().getString(R.string.native_ad_unit));
                builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        if (mNativeAd != null) {
                            mNativeAd.destroy();
                        }
                        mNativeAd = nativeAd;
                        FrameLayout frameLayout = view.findViewById(R.id.recycler_banner_frame);
                        NativeAdView adView = (NativeAdView) LayoutInflater.from(view.getContext()).inflate(R.layout.native_ad, null);
                        populateNativeAdView(nativeAd, adView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                    }
                });
                NativeAdOptions adOptions = new NativeAdOptions.Builder().build();
                builder.withNativeAdOptions(adOptions);
                AdLoader adLoader = builder.withAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        CardView cardView = view.findViewById(R.id.recycler_banner_card);
                        cardView.setVisibility(View.VISIBLE);
                    }
                }).build();
                adLoader.loadAd(new AdRequest.Builder().build());
            }
            private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
                adView.setMediaView(adView.findViewById(R.id.ad_media));
                adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
                adView.setBodyView(adView.findViewById(R.id.ad_body));
                adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
                adView.setIconView(adView.findViewById(R.id.ad_app_icon));
                adView.setPriceView(adView.findViewById(R.id.ad_price));
                adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
                adView.setStoreView(adView.findViewById(R.id.ad_store));
                adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
                ((TextView)adView.getHeadlineView()).setText(nativeAd.getHeadline());
                adView.getMediaView().setMediaContent(nativeAd.getMediaContent());
                if (nativeAd.getBody() == null) {
                    adView.getBodyView().setVisibility(View.INVISIBLE);
                } else {
                    adView.getBodyView().setVisibility(View.VISIBLE);
                    ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
                }
                if (nativeAd.getCallToAction() == null) {
                    adView.getCallToActionView().setVisibility(View.INVISIBLE);
                } else {
                    adView.getCallToActionView().setVisibility(View.VISIBLE);
                    ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
                }
                if (nativeAd.getIcon() == null) {
                    adView.getIconView().setVisibility(View.GONE);
                } else {
                    ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
                    adView.getIconView().setVisibility(View.VISIBLE);
                }
                if (nativeAd.getPrice() == null) {
                    adView.getPriceView().setVisibility(View.INVISIBLE);
                } else {
                    adView.getPriceView().setVisibility(View.VISIBLE);
                    ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
                }
                if (nativeAd.getStore() == null) {
                    adView.getStoreView().setVisibility(View.INVISIBLE);
                } else {
                    adView.getStoreView().setVisibility(View.VISIBLE);
                    ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
                }
                if (nativeAd.getStarRating() == null) {
                    adView.getStarRatingView().setVisibility(View.INVISIBLE);
                } else {

                    ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
                    adView.getStarRatingView().setVisibility(View.VISIBLE);
                }
                if (nativeAd.getAdvertiser() == null) {
                    adView.getAdvertiserView().setVisibility(View.INVISIBLE);
                } else {
                    ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
                    adView.getAdvertiserView().setVisibility(View.VISIBLE);
                }
                adView.setNativeAd(nativeAd);
            }
        }
    }
    private void loadInterstitialAds() {
        MobileAds.initialize(QPActivity.this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(QPActivity.this,getResources().getString(R.string.interstitial_ad_unit), adRequest,
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