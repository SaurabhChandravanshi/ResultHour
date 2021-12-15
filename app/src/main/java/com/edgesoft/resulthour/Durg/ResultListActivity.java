package com.edgesoft.resulthour.Durg;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edgesoft.resulthour.Interface.OnBottomReachedListener;
import com.edgesoft.resulthour.Model.BasicResultData;
import com.edgesoft.resulthour.Model.ResultData;
import com.edgesoft.resulthour.SearchResultActivity;
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
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ResultListActivity extends AppCompatActivity  {
    private RecyclerView recyclerView;
    private ResultListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private final List<Object> dataList = new ArrayList<>();
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
        adapter = new ResultListAdapter(dataList);

        GetResultDataService service = new GetResultDataService();
        pBar.setVisibility(View.VISIBLE);
        service.getResultData(intent_url).addCallback(new FutureCallback<List<Object>>() {
            @Override
            public void onSuccess(List<Object> result) {
                addBannerAds();
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

                    Document document;
                    try {
                        document = Jsoup.connect(url).get();
                        org.jsoup.nodes.Element table  =  document.select("table[class=table]").first();
                        for (Element tbody:table.select("tbody")) {
                            for (org.jsoup.nodes.Element row : tbody.select("tr")) {
                                Elements tds = row.select("td");
                                String title = tds.get(0).text();
                                String url = tds.get(1).children().select("a").attr("href");
                                String date = getFormattedDate(tds.get(2).text());
                                BasicResultData data = new BasicResultData(title,date,url,"Hemchand Yadav Vishwavidyalaya, Durg");
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

    private String getFormattedDate(String text) {
        if(text.length() == 10) {
            String day = text.substring(3,5);
            int month = Integer.parseInt(text.substring(0,2));
            String year = text.substring(6);
            String[] monthNames = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
            return day + "-"+monthNames[month-1] + "-" + year;
        }
        else {
            return text;
        }
    }

    private void addBannerAds() {
        for (int i = 4; i<dataList.size(); i+=4) {
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
    class ResultListAdapter extends RecyclerView.Adapter<ResultListAdapter.MyViewHolder> {
        private final List<Object> dataList;
        private OnBottomReachedListener onBottomReachedListener;
        private static final int VIEW_TYPE_AD = 0;
        private static final int VIEW_TYPE_CONTENT = 1;

        public  ResultListAdapter(List<Object> dataList) {
            this.dataList = dataList;
        }
        public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
            this.onBottomReachedListener = onBottomReachedListener;
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0 || dataList.get(position) instanceof ResultData)
                return VIEW_TYPE_CONTENT;
            else
            if(position % 4 == 0)
                return VIEW_TYPE_AD;
            else
                return VIEW_TYPE_CONTENT;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if(viewType == VIEW_TYPE_AD) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_banner_frame,parent,false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_list, parent, false);
            }
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final MyViewHolder myViewHolder = (MyViewHolder) holder;
            if(getItemViewType(position) == VIEW_TYPE_CONTENT && dataList.get(position) instanceof BasicResultData) {
                final BasicResultData data = (BasicResultData) dataList.get(position);
                myViewHolder.title.setText(data.getTitle());
                myViewHolder.title.setCompoundDrawablePadding(15);
                myViewHolder.university.setCompoundDrawablePadding(15);

                Typeface tfRegular = ResourcesCompat.getFont(myViewHolder.itemView.getContext(),R.font.metropolis_regular);
                myViewHolder.university.setTypeface(tfRegular);
                myViewHolder.title.setTypeface(tfRegular,Typeface.BOLD);

                myViewHolder.university.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_account_balance_24,0,0,0);
                myViewHolder.title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_error_outline_12,0,0,0);
                myViewHolder.university.setText(data.getUniversity());
                myViewHolder.releaseDate.setText(data.getDate());
                final Activity activity =((Activity)myViewHolder.itemView.getContext());

                myViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(myViewHolder.itemView.getContext(), Uri.parse(data.getTargetUrl()));
                    }
                });
                final PopupMenu.OnMenuItemClickListener onMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.recycler_share) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_SUBJECT, data.getTitle());
                            intent.putExtra(Intent.EXTRA_TEXT, "Result Declared: " + data.getTitle() + "\n" + "Release date: " + data.getDate()
                                    + "\nGet this result on Result Hour app\n" + "https://play.google.com/store/apps/details?id=com.edgesoft.resulthour");
                            myViewHolder.itemView.getContext().startActivity(intent);
                        }
                        return false;
                    }
                };
                myViewHolder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(myViewHolder.itemView.getContext(), v);
                        popup.setOnMenuItemClickListener(onMenuItemClickListener);
                        popup.inflate(R.menu.menu_recycler);
                        popup.show();
                    }
                });
                if (position == dataList.size() - 1){
                    onBottomReachedListener.onBottomReached(position);
                }
            } else if (dataList.get(position) instanceof AdView){
                myViewHolder.refreshAd(myViewHolder.itemView);
            }
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private final TextView title;
            private final TextView university;
            private final TextView releaseDate;
            private final CardView cardView;
            private final ImageView more;
            private NativeAd mNativeAd;

            public MyViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.result_list_title);
                university = itemView.findViewById(R.id.result_list_university);
                releaseDate = itemView.findViewById(R.id.result_list_release_date);
                cardView = itemView.findViewById(R.id.result_list_card);
                more = itemView.findViewById(R.id.result_list_context_menu);
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
}