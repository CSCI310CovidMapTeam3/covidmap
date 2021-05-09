package com.example.myapplication.DataBase;

import android.content.Context;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class WebSpider {
    private static final String TAG = "WebSpider";

    //singleton
    private static WebSpider instance = new WebSpider();

    private static int[] results = {0, 0, 0, 0, 0};
    private static int[] totalResults = {0, 0, 0, 0, 0};


//    public static WebSpider getInstance() {
//        if (instance == null) {
//            instance = new WebSpider();
//            results = new int[]{0, 0, 0, 0, 0};
//        }
//        return instance;
//    }

    public static int[] getResults(){
        return results;
    }

    public static int[] getTotalResults(){
        return totalResults;
    }

    public static void clearData(){
        results = null;
        totalResults = null;
    }

    public static int[] getTotalCases(){
        // String baseURL = "http://publichealth.lacounty.gov/media/coronavirus/locations.htm#case-summary";
        String baseURL = "http://publichealth.lacounty.gov/media/coronavirus/locations.htm";
        //int[] results = {0,0,0,0,0};
        try {
            Connection.Response response = Jsoup.connect(baseURL).method(Connection.Method.GET).userAgent("Mozilla").execute();
            Document doc = response.parse();
            Elements elements = doc.getElementsByClass("container-xl pb-4");
            Elements eleCity = elements.get(1).children().get(0).children().get(0).children().get(1).children().get(1).children();
//            results = new int[]{0, 0, 0, 0, 0};
            for(Element element:eleCity) {
                Elements tdsElements = element.select("td");
                String nameString = tdsElements.get(0).text().trim();
                if(nameString.equals("City of Santa Monica")) {
                    String forteen = tdsElements.get(1).text();
                    totalResults[0]=Integer.parseInt(forteen);
                }
                if(nameString.equals("City of Culver City")) {
                    String forteen = tdsElements.get(1).text();
                    totalResults[1]=Integer.parseInt(forteen);
                }
                if(nameString.equals("City of Beverly Hills")) {
                    String forteen = tdsElements.get(1).text();
                    totalResults[2]=Integer.parseInt(forteen);
                }
                if(nameString.equals("City of West Hollywood")) {
                    String forteen = tdsElements.get(1).text();
                    totalResults[3]=Integer.parseInt(forteen);
                }
                if(nameString.equals("Los Angeles - Downtown")) {
                    String forteen = tdsElements.get(1).text();
                    totalResults[4]=Integer.parseInt(forteen);
                }
            }
            Log.v(TAG, totalResults.toString());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.v(TAG, "getTotalCases!"+e.toString());
            return new int[]{0, 0, 0, 0, 0};
        }
        return totalResults;

    }


    public static int[] getVaccinated(){

        // String baseURL = "http://publichealth.lacounty.gov/media/coronavirus/locations.htm#case-summary";
        String baseURL = "http://publichealth.lacounty.gov/media/coronavirus/locations.htm";
        //int[] results = {0,0,0,0,0};
        try {
            Connection.Response response = Jsoup.connect(baseURL).method(Connection.Method.GET).userAgent("Mozilla").execute();
            Document doc = response.parse();
            Elements elements = doc.getElementsByClass("container-xl pb-4");
            Elements eleCity = elements.get(1).children().get(0).children().get(0).children().get(1).children().get(1).children();
            Log.v(TAG, "getVaccinated-");
//            results = new int[]{0, 0, 0, 0, 0};
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
            Log.v(TAG, results.toString());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.v(TAG, "getVaccinated-"+e.toString());
            return new int[]{0, 0, 0, 0, 0};
        }
        return results;

    }


}
