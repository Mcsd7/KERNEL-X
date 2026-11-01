package com.example.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppManagerRepository(private val context: Context) {

  private val packageManager: PackageManager = context.packageManager

  suspend fun loadInstalledApps(): List<AppItem> = withContext(Dispatchers.IO) {
    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
    } else {
      @Suppress("DEPRECATION")
      PackageManager.GET_PERMISSIONS
    }

    val installedPackages: List<PackageInfo> = try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getInstalledPackages(flags as PackageManager.PackageInfoFlags)
      } else {
        @Suppress("DEPRECATION")
        packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
      }
    } catch (e: Exception) {
      emptyList()
    }

    val appList = mutableListOf<AppItem>()

    for (pkg in installedPackages) {
      val appInfo = pkg.applicationInfo ?: continue
      val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

      val appName = try {
        appInfo.loadLabel(packageManager).toString()
      } catch (e: Exception) {
        pkg.packageName
      }

      val icon = try {
        appInfo.loadIcon(packageManager)
      } catch (e: Exception) {
        null
      }

      val size = try {
        val file = File(appInfo.publicSourceDir)
        if (file.exists()) file.length() else 0L
      } catch (e: Exception) {
        0L
      }

      val requested = pkg.requestedPermissions?.toList() ?: emptyList()
      val granted = mutableListOf<String>()

      if (pkg.requestedPermissions != null && pkg.requestedPermissionsFlags != null) {
        for (i in pkg.requestedPermissions!!.indices) {
          val perm = pkg.requestedPermissions!![i]
          val grantedFlag = (pkg.requestedPermissionsFlags!![i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
          if (grantedFlag) {
            granted.add(perm)
          }
        }
      }

      val hasLaunch = packageManager.getLaunchIntentForPackage(pkg.packageName) != null

      val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        pkg.longVersionCode
      } else {
        @Suppress("DEPRECATION")
        pkg.versionCode.toLong()
      }

      appList.add(
        AppItem(
          packageName = pkg.packageName,
          appName = appName,
          versionName = pkg.versionName ?: "1.0",
          versionCode = versionCode,
          isSystemApp = isSystem,
          minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) appInfo.minSdkVersion else 21,
          targetSdk = appInfo.targetSdkVersion,
          firstInstallTime = pkg.firstInstallTime,
          lastUpdateTime = pkg.lastUpdateTime,
          sourceDir = appInfo.sourceDir ?: "",
          apkSizeBytes = size,
          hasLaunchIntent = hasLaunch,
          requestedPermissions = requested,
          grantedPermissions = granted,
          icon = icon
        )
      )
    }

    appList.sortedBy { it.appName.lowercase() }
  }

  suspend fun loadPermissionSummaries(apps: List<AppItem>): List<PermissionGroupSummary> = withContext(Dispatchers.Default) {
    SensitivePermissionGroup.values().map { group ->
      val matchingApps = mutableListOf<PermissionAppDetail>()
      var grantedCount = 0

      for (app in apps) {
        val declaredInGroup = app.requestedPermissions.filter { it in group.permissions }
        if (declaredInGroup.isNotEmpty()) {
          val grantedInGroup = app.grantedPermissions.filter { it in group.permissions }
          val deniedInGroup = declaredInGroup.filter { it !in grantedInGroup }

          if (grantedInGroup.isNotEmpty()) {
            grantedCount++
          }

          matchingApps.add(
            PermissionAppDetail(
              packageName = app.packageName,
              appName = app.appName,
              isSystemApp = app.isSystemApp,
              declaredPermissions = declaredInGroup,
              grantedPermissions = grantedInGroup,
              deniedPermissions = deniedInGroup,
              icon = app.icon
            )
          )
        }
      }

      matchingApps.sortWith(
        compareByDescending<PermissionAppDetail> { it.grantedPermissions.isNotEmpty() }
          .thenBy { it.appName.lowercase() }
      )

      PermissionGroupSummary(
        group = group,
        grantedAppsCount = grantedCount,
        declaredAppsCount = matchingApps.size,
        apps = matchingApps
      )
    }
  }

  fun launchApp(packageName: String): Boolean {
    return try {
      val intent = packageManager.getLaunchIntentForPackage(packageName)
      if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
      } else false
    } catch (e: Exception) {
      false
    }
  }

  fun openAppDetailsSettings(packageName: String) {
    try {
      val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      // Fallback to general apps settings
      openGeneralAppsSettings()
    }
  }

  fun openGeneralAppsSettings() {
    try {
      val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      // ignore
    }
  }

  fun openBatterySettings() {
    try {
      val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      try {
        val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      } catch (e2: Exception) {
        openGeneralAppsSettings()
      }
    }
  }

  fun openBatteryUsageSettings() {
    try {
      val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      openBatterySettings()
    }
  }

  fun createUninstallIntent(packageName: String): Intent {
    return Intent(Intent.ACTION_DELETE).apply {
      data = Uri.fromParts("package", packageName, null)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
  }
}
