package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DefaultPresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Galaxy Watch Studio", appName)
  }

  @Test
  fun `verify default watch face templates exist`() {
    val presets = DefaultPresets.presets
    assertTrue("Should have default presets", presets.isNotEmpty())
    val firstPreset = presets.first()
    assertEquals("Galaxy Ultra Táctico S25", firstPreset.title)
    assertNotNull(firstPreset.complicationTop)
    assertNotNull(firstPreset.complicationBottom)
  }

  @Test
  fun `verify wearable connection path and action requirements`() {
    val preset = DefaultPresets.presets.first()
    val path = com.example.service.SamsungGalaxyWearableService.PATH_WATCHFACE_SYNC_DATA
    assertEquals("/galaxy_watch/watchface/active", path)
    assertNotNull(preset.id)
  }
}
