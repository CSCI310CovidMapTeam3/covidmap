package com.example.myapplication.ui.news;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.MainActivity;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class NewsFragment extends Fragment {

    private NewsViewModel newsViewModel;
    private WebView mWebView;

    private String url = "https://twitter.com/dt_covid19";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        newsViewModel = new ViewModelProvider(this).get(NewsViewModel.class);
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
        // init weather
        OkGo.<String>get("https://www.metaweather.com/api/location/2442047")
                .execute(new AbsCallback<String>() {

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return response.body().string();
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.e("onSuccess", "onSuccess: " + response.body());
                        try {
                            JSONObject respJSON = new JSONObject(response.body());
                            JSONArray consolidated_weather = respJSON.getJSONArray("consolidated_weather");
                            JSONObject weatherJSONO = consolidated_weather.getJSONObject(0);
                            String weather_state_name = weatherJSONO.getString("weather_state_name");
                            String weather_state_abbr = weatherJSONO.getString("weather_state_abbr");
                            String the_temp = weatherJSONO.getString("the_temp");
                            String max_temp = weatherJSONO.getString("max_temp");
                            String min_temp = weatherJSONO.getString("min_temp");
                            HashMap<String, String> weatherMap = new HashMap<>();
                            weatherMap.put("weather_state_name", weather_state_name);
                            weatherMap.put("weather_state_abbr", weather_state_abbr);
                            weatherMap.put("the_temp", the_temp);
                            weatherMap.put("max_temp", max_temp);
                            weatherMap.put("min_temp", min_temp);
                            newsViewModel.getWeatherMap().setValue(weatherMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onError(null);
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        getView().findViewById(R.id.weather_layout).setVisibility(View.GONE);
                    }
                });
        newsViewModel.getWeatherMap().observe(getViewLifecycleOwner(),
                weatherMap -> {
                    ImageView weatherImg = getView().findViewById(R.id.weather_img);
                    TextView weatherText = getView().findViewById(R.id.weather_text);
                    TextView tempNow = getView().findViewById(R.id.temp_now);
                    TextView temperature = getView().findViewById(R.id.temp);
                    // set data
                    Glide.with(weatherImg).load("https://www.metaweather.com/static/img/weather/png/64/"
                            + weatherMap.get("weather_state_abbr") + ".png").into(weatherImg);
                    weatherText.setText(weatherMap.get("weather_state_name"));
                    String max_temp = weatherMap.get("max_temp").split("\\.")[0];
                    String min_temp = weatherMap.get("min_temp").split("\\.")[0];
                    String the_temp = weatherMap.get("the_temp").split("\\.")[0];
                    temperature.setText(max_temp + " ~ " + min_temp + "°");
                    tempNow.setText(the_temp + "°");
                    getView().findViewById(R.id.weather_layout).setVisibility(View.VISIBLE);
                });
        return root;
    }

<<<<<<< HEAD
    public String getUrl() {
=======
    public String getUrl(){
>>>>>>> bdfdbfdfd7cd6ee7022957793162c5dd0298dd24
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