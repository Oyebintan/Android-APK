package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SpamDatabase
import com.example.data.repository.SpamRepository
import com.example.ui.SpamShieldApp
import com.example.ui.SpamViewModel
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule 
    val composeTestRule = createComposeRule()

    private lateinit var db: SpamDatabase
    private lateinit var repository: SpamRepository
    private lateinit var viewModel: SpamViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Setup in-memory SQLite isolated Room Database for perfect isolated test cases
        db = Room.inMemoryDatabaseBuilder(context, SpamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            
        repository = SpamRepository(db.spamDao())
        
        viewModel = SpamViewModel(
            application = context as Application,
            repository = repository
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun greeting_screenshot() {
        composeTestRule.setContent { 
            MyApplicationTheme { 
                SpamShieldApp(viewModel = viewModel) 
            } 
        }

        // Capture a glorious screenshot of our homepage
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
