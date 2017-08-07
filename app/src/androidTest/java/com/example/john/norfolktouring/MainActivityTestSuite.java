package com.example.john.norfolktouring;

import android.app.Activity;
import android.content.res.Resources;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

/**
 * Created by John on 8/6/2017.
 */

/**
 * The test suite - used to run all of the following tests..
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({MainActivityTestSuite.MainActivityTest.class})
public class MainActivityTestSuite {

    @RunWith(AndroidJUnit4.class)
    public static class MainActivityTest {

        @Rule
        public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

        /**
         * Test that the IntroductoryFragment displays immediately.
         */
        @Test
        public void introductoryFragmentDisplaysAutomatically() {
            // Check if the YouTube player is displayed when MainActivity is created.
            onView(withId(R.id.youtube_fragment)).check(matches(isDisplayed()));
        }

        /**
         * Test that the Navigation Drawer allows the user to open the TourLocationListFragments.
         */
        @Test
        public void categoryFragmentsDrawerItemTest() {
            MainActivity activity = mActivityTestRule.getActivity();
            List<String> categoryNames = activity.getNavigationDrawerCategories();
            for (int categoryIndx = 0; categoryIndx < categoryNames.size(); categoryIndx++) {
                // Open the Navigation Drawer.
                onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
                // Click this item in the Navigation Drawer.
                onData(anything()).inAdapterView(withId(R.id.left_drawer))
                        .atPosition(categoryIndx).perform(click());
                // Check if the corresponding Fragment is displayed by checking the Action Bar text.
                // This assumes that the subclasses of `TourLocationListFragment` change the
                // Action Bar title to their respective category names.
                Resources resources = getInstrumentation().getTargetContext().getResources();
                String categoryName = categoryNames.get(categoryIndx);
                String actionBarTitle = NorfolkTouring.getActionBarTitle(activity);
                assert actionBarTitle.equals(categoryName);
            }
        }

        /**
         * Auto-generated method from Android.
         */
        private static Matcher<View> childAtPosition(
                final Matcher<View> parentMatcher, final int position) {

            return new TypeSafeMatcher<View>() {
                @Override
                public void describeTo(Description description) {
                    description.appendText("Child at position " + position + " in parent ");
                    parentMatcher.describeTo(description);
                }

                @Override
                public boolean matchesSafely(View view) {
                    ViewParent parent = view.getParent();
                    return parent instanceof ViewGroup && parentMatcher.matches(parent)
                            && view.equals(((ViewGroup) parent).getChildAt(position));
                }
            };
        }
    }

}


