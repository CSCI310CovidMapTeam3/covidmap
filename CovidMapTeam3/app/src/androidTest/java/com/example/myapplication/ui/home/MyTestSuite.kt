package com.example.myapplication.ui.home

import com.example.myapplication.ui.home.HomeFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyTestSuite {
    @Test fun testEventFragment() {
        // The "fragmentArgs" arguments are optional.
        val fragmentArgs = bundleOf("numElements" to 0)
        val scenario = launchFragment<HomeFragment>(fragmentArgs)
    }
}