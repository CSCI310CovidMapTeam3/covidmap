package com.example.myapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapter.OnBoardingPageAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OnBoardingActivity extends AppCompatActivity {

    private View mStartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        mStartBtn = findViewById(R.id.start_btn);
        mStartBtn.setOnClickListener(v -> {
           getSharedPreferences("app", Context.MODE_PRIVATE)
                   .edit()
                   .putBoolean("isFirst",false)
                   .apply();
            Intent i = new Intent(OnBoardingActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        });

        ViewPager2 viewPager2 = findViewById(R.id.view_page);
        viewPager2.setAdapter(new OnBoardingPageAdapter());

        BottomNavigationView bottomNavigationView = findViewById(R.id.page_indicator);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setItemIconSize(33);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.indicator_one:
                            viewPager2.setCurrentItem(0);
                            break;
                        case R.id.indicator_two:
                            viewPager2.setCurrentItem(1);
                            break;
                        case R.id.indicator_three:
                            viewPager2.setCurrentItem(2);
                            break;
                    }
                    return true;
                });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                if (position == 2 && mStartBtn.getVisibility() == View.GONE) {
                    mStartBtn.setVisibility(View.VISIBLE);
                } else if (mStartBtn.getVisibility() == View.VISIBLE) {
                    mStartBtn.setVisibility(View.GONE);
                }
            }
        });
    }
}