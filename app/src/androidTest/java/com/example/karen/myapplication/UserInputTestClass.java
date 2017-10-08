package com.example.karen.myapplication;

/**
 * Created by Karen on 10/12/2016.
 */

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.myapplication.ui.MainActivity;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserInputTestClass {

    private String mStringToBetyped;
    private String mStringToDisplayed;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void initValidString() {
        // Specify a valid string.
        mStringToBetyped = "John";
        mStringToDisplayed = "Hello John";
    }

    @Test
    public void changeText_sameActivity() {
        /*
        // Type text and then press the button.
        onView(withId(R.id.first_name))
                .perform(clearText());
        //onView(withId(R.id.textToBeDisplayed)).perform(clearText());

        onView(withId(R.id.first_name))
                .perform(typeText(mStringToBetyped));
        closeSoftKeyboard();
        onView(withId(R.id.submit)).perform(click());

        // Check that the text was changed.
        onView(withId(R.id.textToBeDisplayed))
                .check(matches(withText(mStringToDisplayed)));
                */
    }
}
