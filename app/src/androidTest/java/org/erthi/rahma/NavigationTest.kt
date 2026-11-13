package org.erthi.rahma

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun guidanceSurvivesRecreationAndSystemBackReturnsHome() {
        compose.onNodeWithText("أحكام الجنائز").performScrollTo().performClick()
        compose.onNodeWithText("الجنائز على هدي السنة").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("الجنائز على هدي السنة").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("إرثي | رحمة").assertIsDisplayed()
    }

    @Test fun audioScreenOpensWithoutStartingPlaybackAndReturnsHome() {
        compose.onNodeWithText("المصحف الصوتي").performScrollTo().performClick()
        compose.onNodeWithText("الاستماع إلى القرآن الكريم").assertIsDisplayed()
        compose.onNodeWithText("الرجوع إلى النعي").performClick()
        compose.onNodeWithText("إرثي | رحمة").assertIsDisplayed()
    }
}
