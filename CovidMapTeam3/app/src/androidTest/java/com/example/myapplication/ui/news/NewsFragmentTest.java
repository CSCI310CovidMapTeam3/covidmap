package com.example.myapplication.ui.news;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NewsFragmentTest {

    NewsFragment newsFragment;

    @Before
    public void setUp() throws Exception {
        newsFragment = new NewsFragment();
    }

    // test if the web page is correct
    @Test
    public void testTwitterPage() {
        assertEquals(newsFragment.getUrl(), "https://twitter.com/dt_covid19");
    }

}