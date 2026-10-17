package com.coder7475.focusguard.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class FocusPreferencesTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var context: Context
    private lateinit var focusPreferences: FocusPreferences

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val testDataStoreFile = File(context.filesDir, "test_focus_prefs.preferences_pb")
        if (testDataStoreFile.exists()) {
            testDataStoreFile.delete()
        }

        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { context.preferencesDataStoreFile("test_focus_prefs") }
        )
        focusPreferences = FocusPreferences(testDataStore)
    }

    @Test
    fun defaultOnboardingAndBatteryFlagsAreFalse() = runTest(testDispatcher) {
        val onboarding = focusPreferences.isOnboardingCompleted.first()
        val batteryGuide = focusPreferences.isBatteryGuideDismissed.first()

        assertFalse(onboarding)
        assertFalse(batteryGuide)
    }

    @Test
    fun setOnboardingCompletedUpdatesState() = runTest(testDispatcher) {
        focusPreferences.setOnboardingCompleted(true)
        val onboarding = focusPreferences.isOnboardingCompleted.first()
        assertTrue(onboarding)
    }

    @Test
    fun setBatteryGuideDismissedUpdatesState() = runTest(testDispatcher) {
        focusPreferences.setBatteryGuideDismissed(true)
        val batteryGuide = focusPreferences.isBatteryGuideDismissed.first()
        assertTrue(batteryGuide)
    }
}
