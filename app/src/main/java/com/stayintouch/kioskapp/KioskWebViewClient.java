package com.stayintouch.kioskapp;

import android.net.http.SslError;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.stayintouch.kioskapp.config.Configuration;
import com.stayintouch.kioskapp.lib.SaveAndLoad;
import com.stayintouch.kioskapp.lib.URLRequest;

import java.io.File;

class KioskWebViewClient extends WebViewClient {

    private boolean enableCaching = false;
    private Handler reloadHandler = null;
    private Handler loadLocalVideoHandler = null;
    private String originalUrl = null;
    private long retryDelay = 90_000L;

    void loadConfiguration(Configuration configuration) {
        originalUrl = configuration.getUrl();
        retryDelay = configuration.getRetryDelay();
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        System.out.println("Test: " + url);

        if (enableCaching) {
            if (url.contains(".mp4") || url.contains(".wav")) {
                String[] url_parts = url.split("/");
                String file_name = url_parts[url_parts.length - 1];

                if (SaveAndLoad.readFromFile(file_name, view.getContext()).equals("")) {
                    URLRequest.startDownload(url, file_name);
                }
                return new WebResourceResponse(SaveAndLoad.getMimeType(url), "UTF-8", SaveAndLoad.readFromFileAndReturnInputStream(file_name, view.getContext()));

            }
        }
        return super.shouldInterceptRequest(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onReceivedError(final WebView view, int errorCode, String description, final String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if (!failingUrl.contains(".ico")) {
            loadFallbackLocalVideo(view);
            postWebViewReloadDelayed(view, retryDelay);
        }
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        if (!request.getUrl().toString().contains(".ico")) {
            loadFallbackLocalVideo(view);
            postWebViewReloadDelayed(view, retryDelay);
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed(); //Ignore SSL certificate error
    }

    private void loadFallbackLocalVideo(final WebView webView) {
        File fallbackFile = new File(new File(webView.getContext().getFilesDir(), "Video"), "fallback.mp4");
        if (fallbackFile == null || !fallbackFile.exists()) return;
        String localPath = "file://" + fallbackFile.getAbsolutePath();
        final String html = "<body style=\"background:#000;\" id=\"video_body\"><video poster=\"file:///android_asset/fon.png\" loop=\"\" id=\"video\" style=\"position:fixed;left:0;top:0;width:100vw;height: 100vh;\" preload=\"auto\" autoplay=\"\" muted=\"\" allow=\"autoplay\"><source src=\""+ localPath + "\" type=\"video/mp4\"> </video></body>";
        webView.post(
                new Runnable() {
                    @Override
                    public void run() {
                        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                    }
                }
        );
    }

    private void postWebViewReloadDelayed(final WebView webView, Long delay) {
        if (reloadHandler != null) return;
        reloadHandler = new Handler(Looper.getMainLooper());
        reloadHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(originalUrl);
                        if (reloadHandler != null) {
                            reloadHandler.removeCallbacksAndMessages(null);
                            reloadHandler = null;
                        }
                    }
                },
                delay
        );
    }

    void dispose() {
        if (reloadHandler != null) {
            reloadHandler.removeCallbacksAndMessages(null);
            reloadHandler = null;
        }
        if (loadLocalVideoHandler != null) {
            loadLocalVideoHandler.removeCallbacksAndMessages(null);
            loadLocalVideoHandler = null;
        }
    }
}
