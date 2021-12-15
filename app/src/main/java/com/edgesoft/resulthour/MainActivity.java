package com.edgesoft.resulthour;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import com.edgesoft.resulthour.Fragment.ContentMainFragment;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import android.view.LayoutInflater;
import android.view.View;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.edgsoft.resulthour.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private NativeAd mNativeAd;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshAd();


        loadHomePage();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Download Result Hour App For All Chhattisgarh Latest Results");
                intent.putExtra(Intent.EXTRA_TEXT,"Download Result Hour App to Get Latest Result Of Bilaspur University | " +
                        "Bastar University | Sarguja University | Pt Sunder Lal Sharma Open University\nInstall Result Hour app\n" +
                        "https://play.google.com/store/apps/details?id=com.edgesoft.resulthour");
                startActivity(Intent.createChooser(intent,"Share Via"));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmation");
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_exit_dialog,null);
            if (mNativeAd != null) {
                builder.setView(view);
                showNativeAdOnExitDialog(view);
            }
            builder.setMessage("Are you sure you wants to Exit ?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.super.onBackPressed();
                }
            })
                    .setNegativeButton("NO",null)
                    .setNeutralButton("RATE US", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.edgesoft.resulthour"));
                            startActivity(Intent.createChooser(intent,"Open With"));
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_rate_us:
                launchUrl("https://play.google.com/store/apps/details?id=com.edgesoft.resulthour");
                break;
            case R.id.action_share_app:
                shareApp();
                break;
            case R.id.action_privacy:
                launchUrl("https://i3developer.com/Policies/ResultHour.htm");
                break;
            case R.id.action_terms:
                launchUrl("https://i3developer.com/Terms/ResultHour.htm");

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_jobs_info_cg)
            launchUrl("https://play.google.com/store/apps/details?id=chhattisgarh.rojgar.samachar");
        else if (id == R.id.nav_whatslink)
            launchUrl("https://play.google.com/store/apps/details?id=com.i3d.whatslink");
        else if (id == R.id.nav_shayari_app)
            launchUrl("https://play.google.com/store/apps/details?id=com.i3developer.shayari");
        else if (id == R.id.nav_dev_page)
            launchUrl("https://play.google.com/store/apps/dev?id=9018600825061407450");
        else if (id == R.id.nav_rate_app)
            launchUrl("https://play.google.com/store/apps/details?id=com.edgesoft.resulthour");
        else if (id == R.id.nav_share)
            shareApp();
        else if (id == R.id.nav_live_chat)
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/917000268870?text=[Result Hour]")));
        else if (id == R.id.nav_report_issue)
            startActivity(new Intent(MainActivity.this,ReportActivity.class));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void launchUrl(String s) {
        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(s));
        startActivity(intent);
    }

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,"Download Result Hour App For All Chhattisgarh Latest Results");
        intent.putExtra(Intent.EXTRA_TEXT,"Download Result Hour App to Get Latest Result & Question Papers for Bilaspur University | " +
                "Bastar University | Sarguja University | Pt Sunder Lal Sharma Open University.\nInstall Result Hour app\n" +
                "https://play.google.com/store/apps/details?id=com.edgesoft.resulthour");
        startActivity(Intent.createChooser(intent,"Share Via"));
    }

    public void loadHomePage() {
        Fragment fragment = new ContentMainFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    public void resultList(View view) {
        String[] tags = view.getTag().toString().split(",");
        Intent intent = new Intent(MainActivity.this, FeatureActivity.class);
        intent.putExtra("url",tags[1]);
        intent.putExtra("university",tags[0]);
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

    private void showNativeAdOnExitDialog(View view) {
        FrameLayout frameLayout = view.findViewById(R.id.custom_exit_native_ad_frame);
        NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.native_ad, null);
        populateNativeAdView(mNativeAd, adView);
        frameLayout.removeAllViews();
        frameLayout.addView(adView);
        CardView adCard = view.findViewById(R.id.custom_exit_ad_card);
        adCard.setVisibility(View.VISIBLE);
    }
}
