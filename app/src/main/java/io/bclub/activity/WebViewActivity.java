package io.bclub.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.bclub.R;

public class WebViewActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String URI_EXTRA = "URI";

    @BindView(R.id.web_view)
    WebView webView;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    IntentFilter mConnectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

    boolean mFirstTime = false;

    Uri baseUri;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                webView.setNetworkAvailable(app.isNetworkConnected());
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        baseUri = getIntent().getParcelableExtra(URI_EXTRA);

        if (baseUri == null) {
            finish();
            return;
        }

        mFirstTime = savedInstanceState == null;

        setContentView(R.layout.activity_webview);

        ButterKnife.bind(this);

        configureToolbar();
        configureWebView();
        configureSwipeRefresh();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mReceiver, mConnectivityFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    void configureSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.primary), ContextCompat.getColor(this, R.color.primary_dark));
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @SuppressWarnings("ConstantConditions")
    void configureToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SetJavaScriptEnabled")
    void configureWebView() {
        WebSettings webSettings = webView.getSettings();

        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                setupToolbarAlpha(view.getTitle());

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setGeolocationDatabasePath(getFilesDir().getPath());

        webSettings.setGeolocationEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.loadUrl(baseUri.toString());
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRefresh() {
        webView.reload();
    }
}
