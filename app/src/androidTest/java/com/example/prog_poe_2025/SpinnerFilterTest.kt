package com.example.prog_poe_2025

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpinnerFilterTest {

    @Test
    fun testSpinnerFilter_LastMinute() {
        // Select "Last Minute" from the spinner
        onView(withId(R.id.spinDateFilter)).perform(click())
        onData(allOf(`is`("Last Minute"))).perform(click())

        // ✅ Ensures transactions older than 1 minute disappear
        onView(withText("Coffee - R50")).check(doesNotExist())
    }
}
