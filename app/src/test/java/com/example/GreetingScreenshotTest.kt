package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.DefaultPresets
import com.example.ui.components.GalaxyWatchCanvas
import com.example.ui.components.WatchViewMode
import com.example.ui.theme.GalaxyWatchStudioTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
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

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun watch_studio_screenshot() {
    val samplePreset = DefaultPresets.presets.first()
    composeTestRule.setContent {
      GalaxyWatchStudioTheme {
        Box(modifier = Modifier.size(300.dp)) {
          GalaxyWatchCanvas(
            watchFace = samplePreset,
            viewMode = WatchViewMode.ACTIVE,
            modifier = Modifier.size(300.dp)
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
