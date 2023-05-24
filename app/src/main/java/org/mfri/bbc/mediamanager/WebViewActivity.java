package org.mfri.bbc.mediamanager;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);
        WebView webView = findViewById(R.id.viewWebView);
        //webView.getSettings().setJavaScriptEnabled(true);
        Bundle bundle = this.getIntent().getExtras().getBundle("bundle_page");
        webView.loadUrl("file:///android_asset/"+bundle.getString("page")+".html");
    }
}