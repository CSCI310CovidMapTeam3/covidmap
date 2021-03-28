package com.example.myapplication.ui.news;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.activity.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.home.HomeViewModel;

public class NewsFragment extends Fragment {

    private NewsViewModel newsViewModel;
    private WebView mWebView;

    private String url = "https://twitter.com/dt_covid19";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        newsViewModel =
                new ViewModelProvider(this).get(NewsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_news, container, false);
        mWebView = root.findViewById(R.id.news_web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // delete div
                String js = "var list=document.getElementById(\"layers\");\n" +
                        "list.removeChild(list.childNodes[1]);";
                view.loadUrl("javascript:" + js);
            }
        });

        return root;
    }

    public String getUrl(){
        return url;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setOnBackListener(() -> {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                    return true;
                } else {
                    return false;
                }
            });
        }
    }
}