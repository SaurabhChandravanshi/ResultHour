package com.edgesoft.resulthour.Adapter;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.edgesoft.resulthour.Model.ResultData;
import com.edgesoft.resulthour.ResultWebViewActivity;
import com.edgsoft.resulthour.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.List;

public class ViewResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_AD = 0;
    private static final int VIEW_TYPE_CONTENT = 1;
    private List<Object> dataList;
    private NativeAd mNativeAd;

    public  ViewResultAdapter(List<Object> dataList) {
        this.dataList = dataList;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == VIEW_TYPE_AD) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_banner_frame, parent, false);;
        }
        else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_list, parent, false);
        }
        return new MyViewHolder(view);
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MyViewHolder myViewHolder = (MyViewHolder)holder;

        if(getItemViewType(position) == VIEW_TYPE_CONTENT && dataList.get(position) instanceof ResultData) {
            final ResultData data = (ResultData) dataList.get(position);

            Typeface typeface = ResourcesCompat.getFont(myViewHolder.itemView.getContext(),R.font.sans_regular);
            myViewHolder.title.setTypeface(typeface,Typeface.BOLD);
            myViewHolder.university.setTypeface(typeface);
            myViewHolder.title.setText(data.getTitle());
            myViewHolder.releaseDate.setText(data.getDate());
            myViewHolder.university.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_account_balance_24,0,0,0);
            myViewHolder.title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_error_outline_12,0,0,0);
            myViewHolder.university.setText(data.getUniversityName());
            myViewHolder.university.setCompoundDrawablePadding(15);
            myViewHolder.title.setCompoundDrawablePadding(15);

            myViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(myViewHolder.itemView.getContext(), ResultWebViewActivity.class);
                    intent.putExtra("url",data.getExamId());
                    myViewHolder.itemView.getContext().startActivity(intent);
                }
            });
        }
        else if (dataList.get(position) instanceof AdView){
            refreshAd(myViewHolder.itemView);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title,university,releaseDate;
        private CardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.result_list_title);
            university = itemView.findViewById(R.id.result_list_university);
            releaseDate =itemView.findViewById(R.id.result_list_release_date);
            cardView = itemView.findViewById(R.id.result_list_card);
        }
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
