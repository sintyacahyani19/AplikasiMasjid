package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.MasjidDatabase
import com.example.data.TransactionRepository
import com.example.ui.DashboardScreen
import com.example.ui.TransactionViewModel
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var database: MasjidDatabase
    private lateinit var viewModel: TransactionViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Build in-memory database for clean, isolated visual/logic testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            MasjidDatabase::class.java
        ).allowMainThreadQueries().build()

        val repository = TransactionRepository(database.transactionDao())
        viewModel = TransactionViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun dashboard_visual_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }

        // Wait for system interactions and idle state to ensure visual completeness
        composeTestRule.waitForIdle()

        // Capture visual snapshot of our newly designed Mosque dashboard
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
