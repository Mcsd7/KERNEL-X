package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.apps.SensitivePermissionGroup
import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kernel X", appName)
  }

  @Test
  fun `format bytes correctly`() {
    val bytes = 1024L * 1024L * 1024L * 4L // 4 GB
    val formatted = FormatUtils.formatBytes(bytes)
    assertTrue(formatted.contains("4.0") || formatted.contains("4"))
    assertTrue(formatted.contains("GB"))
  }

  @Test
  fun `sensitive permission groups defined`() {
    assertTrue(SensitivePermissionGroup.values().isNotEmpty())
    val camera = SensitivePermissionGroup.CAMERA
    assertTrue(camera.permissions.contains("android.permission.CAMERA"))
  }
}
