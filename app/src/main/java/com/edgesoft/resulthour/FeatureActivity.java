package com.edgesoft.resulthour;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.edgesoft.resulthour.Bilaspur.TTActivity;
import com.edgesoft.resulthour.Model.TimeTable;
import com.edgesoft.resulthour.Model.Uni;
import com.edgsoft.resulthour.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.sql.Time;

public class FeatureActivity extends AppCompatActivity {

    private CardView qpCard,ttCard;
    private NativeAd mNativeAd;
    private String University;
    private String TimeTableUrl;
    private String ResultUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature);
        initial();
        refreshAd();

        updateUI();

        qpCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewQPaper();
            }
        });
        ttCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewTimeTable();
            }
        });
    }

    private void updateUI() {
        if(University.equals(Uni.getPSSOUni()) || University.equals(Uni.getDurgUni())) {
            qpCard.setVisibility(View.GONE);
            qpCard.setVisibility(View.GONE);
        }
        if(University.equals(Uni.getABVV())) {
            ttCard.setVisibility(View.VISIBLE);
            setTimeTableUrl("https://www.bilaspuruniversity.ac.in/timetable.php");
        }
    }

    private void setTimeTableUrl(String url) {
        TimeTableUrl = url;
    }

    private void initial() {
        University = getIntent().getStringExtra("university");
        ResultUrl = getIntent().getStringExtra("url");
        qpCard = findViewById(R.id.feature_qp);
        ttCard = findViewById(R.id.feature_tt);
    }

    public void viewResult(View view) {
        Intent intent;
        if(University.equals(Uni.getDurgUni())) {
            intent = new Intent(FeatureActivity.this, com.edgesoft.resulthour.Durg.ResultListActivity.class);
        } else {
            intent = new Intent(FeatureActivity.this, ResultListActivity.class);
        }
        intent.putExtra("url",ResultUrl);
        intent.putExtra("university",University);
        startActivity(intent);
    }

    public void viewQPaper() {
        Intent intent = new Intent(FeatureActivity.this, QPActivity.class);
        intent.putExtra("url",getIntent().getStringExtra("url"));
        intent.putExtra("university",getIntent().getStringExtra("university"));
        startActivity(intent);
    }

    public void viewTimeTable() {
        Intent intent = new Intent(FeatureActivity.this, TTActivity.class);
        intent.putExtra("url",TimeTableUrl);
        intent.putExtra("university",getIntent().getStringExtra("university"));
        startActivity(intent);
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
            Typeface tfRegular = ResourcesCompat.getFont(this,R.font.metropolis_regular);
            ((TextView) adView.getBodyView()).setTypeface(tfRegular,Typeface.BOLD);
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
    private void refreshAd() {
        AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.native_ad_unit));
        builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                if (mNativeAd != null) {
                    mNativeAd.destroy();
                }
                mNativeAd = nativeAd;
                FrameLayout frameLayout = findViewById(R.id.feature_native_ad_frame);
                NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.native_ad, null);
                populateNativeAdView(nativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
                CardView adCard = findViewById(R.id.feature_ad_card);
                adCard.setVisibility(View.VISIBLE);
            }
        });
        NativeAdOptions adOptions = new NativeAdOptions.Builder().build();
        builder.withNativeAdOptions(adOptions);
        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }
}