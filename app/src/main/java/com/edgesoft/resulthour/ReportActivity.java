package com.edgesoft.resulthour;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import com.edgsoft.resulthour.R;
import com.github.clemp6r.futuroid.Async;
import com.github.clemp6r.futuroid.Future;
import com.github.clemp6r.futuroid.FutureCallback;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

public class ReportActivity extends AppCompatActivity {

    private NativeAd mNativeAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {}
        });
        refreshAd();

        SpannableString ss = new SpannableString("For instant resolution of your issue please contact us using live chat support.");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/917000268870?text=[Result Hour]"));
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.darkBlue));
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan,61,78, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView textView = findViewById(R.id.report_tv_chat);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

    }

    public void submitIssue(View view) {
        EditText email = findViewById(R.id.report_email);
        EditText message = findViewById(R.id.report_message);

        if(TextUtils.isEmpty(message.getText())) {
            message.setError("Please write issue");
            return;
        }
        Button button = (Button) view;
        button.setText("Please wait...");
        button.setEnabled(false);
        SubmitIssue submitIssue = new SubmitIssue();
        try {
            String params = "email="+ URLEncoder.encode(email.getText().toString(),"UTF-8")+"&message="+URLEncoder.encode(message.getText().toString(),"UTF-8");
            submitIssue.submit("https://i3developer.com/Mailer/ResultHourReport.php",params).addCallback(new FutureCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    if(result != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if(jsonObject.getString("status").equals("success")) {
                                Toast.makeText(ReportActivity.this, jsonObject.getString("msg"), Toast.LENGTH_LONG).show();
                                finish();
                            } else
                                Toast.makeText(ReportActivity.this, jsonObject.getString("msg"), Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            Toast.makeText(ReportActivity.this, R.string.def_err_msg, Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(ReportActivity.this, R.string.def_err_msg, Toast.LENGTH_SHORT).show();
                    button.setEnabled(true);
                    button.setText("Submit your issue");
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(ReportActivity.this, R.string.def_err_msg, Toast.LENGTH_SHORT).show();
                    button.setEnabled(true);
                    button.setText("Submit your issue");
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    class SubmitIssue {
        public Future<String> submit(String url,String params) {
            return Async.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    URL urlParam = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection)urlParam.openConnection();
                    conn.setRequestMethod("POST");

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write( params );
                    wr.flush();

                    String line;
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));

                    while ((line=br.readLine())!= null) {
                        sb.append(line);
                    }
                    return sb.toString();
                }
            });
        }
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
                FrameLayout frameLayout = findViewById(R.id.report_native_ad_frame);
                NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.native_ad, null);
                populateNativeAdView(nativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
                CardView adCard = findViewById(R.id.report_ad_card);
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