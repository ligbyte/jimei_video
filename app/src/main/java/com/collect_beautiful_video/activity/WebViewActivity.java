package com.collect_beautiful_video.activity;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.collect_beautiful_video.R;

import java.lang.reflect.Method;

public class WebViewActivity extends FragmentActivity {
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String title = getIntent().getStringExtra("title");
        String url = getIntent().getStringExtra("url");
        ((TextView) findViewById(R.id.tv_title)).setText(title);
        progressBar = findViewById(R.id.progress);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        webView = findViewById(R.id.web_view);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // 启用javascript
        settings.setDomStorageEnabled(true); // 支持HTML5中的一些控件标签
        settings.setBuiltInZoomControls(false); // 自选，非必要
        //处理http和https混合的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        } else {
            settings.setMixedContentMode(WebSettings.LOAD_NORMAL);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 允许javascript出错
            try {
                Method method = Class.forName("android.webkit.WebView").
                        getMethod("setWebContentsDebuggingEnabled", Boolean.TYPE);
                if (method != null) {
                    method.setAccessible(true);
                    method.invoke(null, true);
                }
            } catch (Exception e) {
                // do nothing
            }
        }

        webView.setFocusable(true); // 自选，非必要
        webView.setDrawingCacheEnabled(true); // 自选，非必要
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); // 自选，非必要
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) { // 显示加载进度，自选
                progressBar.setProgress(progress);
                if (progress <= 0) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                if (progress >= 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false); // 页面有请求位置的时候需要
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://") || url.startsWith("https://")) { // 4.0以上必须要加
                    view.loadUrl(url);
                    return true;
                }
                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //super.onReceivedSslError(view, handler, error)
                switch (error.getPrimaryError()) {
                    case SslError.SSL_INVALID: // 校验过程遇到了bug
                    case SslError.SSL_UNTRUSTED: // 证书有问题
                        handler.proceed();
                    default:
                        handler.cancel();
                }
            }
        });
        webView.loadUrl(url);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }
}
