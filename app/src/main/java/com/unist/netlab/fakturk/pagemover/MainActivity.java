package com.unist.netlab.fakturk.pagemover;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity
{
    WebView netlab;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        netlab = (WebView) findViewById(R.id.webView);
        netlab.setWebViewClient(new MyWebViewClient());
        netlab.getSettings().setJavaScriptEnabled(true);
        netlab.loadUrl("http://netlab.unist.ac.kr/people/changhee-joo/");


        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(netlab.getLayoutParams());
        marginParams.setMargins(250,250,250,250);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        netlab.setLayoutParams(layoutParams);

    }

    public class MyWebViewClient extends WebViewClient
    {

        public MyWebViewClient() {
            super();
            //start anything you need to
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //Do something to the urls, views, etc.
        }
    }
}
