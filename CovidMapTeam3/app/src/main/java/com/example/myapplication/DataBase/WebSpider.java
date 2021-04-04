package com.example.myapplication.DataBase;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class WebSpider {
    private static final String TAG = "WebSpider";
    public static int[] getForteenCases(){

        String baseURL = "http://publichealth.lacounty.gov/media/coronavirus/locations.htm#case-summary";
        int[] results = {0,0,0,0,0};
        try {
            Document doc= Jsoup.connect(baseURL).get();
            Elements elements = doc.getElementsByClass("container-xl pb-4");
            Elements eleCity = elements.get(2).children().get(0).children().get(0).children().get(1).children().get(1).children();

            for(Element element:eleCity) {
                Elements tdsElements = element.select("td");
                String nameString = tdsElements.get(0).text().trim();
                if(nameString.equals("City of Santa Monica")) {
                    String forteen = tdsElements.get(1).text();
                    results[0]=Integer.parseInt(forteen);
                }
                if(nameString.equals("City of Culver City")) {
                    String forteen = tdsElements.get(1).text();
                    results[1]=Integer.parseInt(forteen);
                }
                if(nameString.equals("City of Beverly Hills")) {
                    String forteen = tdsElements.get(1).text();
                    results[2]=Integer.parseInt(forteen);
                }
                if(nameString.equals("City of West Hollywood")) {
                    String forteen = tdsElements.get(1).text();
                    results[3]=Integer.parseInt(forteen);
                }
                if(nameString.equals("Los Angeles - Downtown")) {
                    String forteen = tdsElements.get(1).text();
                    results[4]=Integer.parseInt(forteen);
                }
            }
            Log.v(TAG, String.valueOf(results));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.v(TAG, "!"+e.toString());
            return null;
        }
        return results;

    }


}
