package com.example.data.system

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.Locale

class SystemMonitorRepository(private val context: Context) {

  private val activityManager =
    context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

  fun getHardwareInfo(): DeviceHardwareInfo {
    return DeviceHardwareInfo(
      manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase(Locale.ROOT) },
      model = Build.MODEL,
      device = Build.DEVICE,
      product = Build.PRODUCT,
      hardware = Build.HARDWARE,
      board = Build.BOARD,
      androidVersion = Build.VERSION.RELEASE,
      apiLevel = Build.VERSION.SDK_INT,
      securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Build.VERSION.SECURITY_PATCH
      } else "N/A",
      supportedAbis = Build.SUPPORTED_ABIS.toList(),
      kernelVersion = readKernelVersion(),
      uptimeMillis = SystemClock.elapsedRealtime(),
      buildFingerprint = Build.FINGERPRINT
    )
  }

  fun getRamInfo(): RamInfo {
    val memInfo = ActivityManager.MemoryInfo()
    activityManager?.getMemoryInfo(memInfo)
    val total = memInfo.totalMem
    val available = memInfo.availMem
    val used = (total - available).coerceAtLeast(0L)
    val usedPercent = if (total > 0L) (used.toDouble() / total.toDouble()).toFloat() else 0f

    return RamInfo(
      totalBytes = total,
      usedBytes = used,
      availableBytes = available,
      usedPercent = usedPercent,
      isLowMemory = memInfo.lowMemory,
      thresholdBytes = memInfo.threshold
    )
  }

  fun getStorageInfo(): StorageInfo {
    return try {
      val path = Environment.getDataDirectory()
      val stat = StatFs(path.absolutePath)
      val blockSize = stat.blockSizeLong
      val totalBlocks = stat.blockCountLong
      val availableBlocks = stat.availableBlocksLong

      val total = totalBlocks * blockSize
      val free = availableBlocks * blockSize
      val used = (total - free).coerceAtLeast(0L)
      val usedPercent = if (total > 0L) (used.toDouble() / total.toDouble()).toFloat() else 0f

      StorageInfo(
        totalBytes = total,
        usedBytes = used,
        freeBytes = free,
        usedPercent = usedPercent
      )
    } catch (e: Exception) {
      StorageInfo(
        totalBytes = 0L,
        usedBytes = 0L,
        freeBytes = 0L,
        usedPercent = 0f
      )
    }
  }

  fun getCpuInfo(): CpuInfo {
    val coresCount = Runtime.getRuntime().availableProcessors()
    val arch = System.getProperty("os.arch") ?: Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
    val processorName = readCpuModel() ?: Build.HARDWARE

    val coreList = mutableListOf<CpuCoreInfo>()
    for (i in 0 until coresCount) {
      val freq = readCoreFrequency(i)
      coreList.add(CpuCoreInfo(coreIndex = i, frequencyKHz = freq))
    }

    return CpuInfo(
      architecture = arch,
      coresCount = coresCount,
      processorName = processorName,
      abiList = Build.SUPPORTED_ABIS.joinToString(", "),
      cores = coreList
    )
  }

  fun getBatteryInfo(): BatteryInfo {
    return try {
      val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
      if (intent != null) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
          status == BatteryManager.BATTERY_STATUS_FULL

        val statusStr = when (status) {
          BatteryManager.BATTERY_STATUS_CHARGING -> "Carregando"
          BatteryManager.BATTERY_STATUS_DISCHARGING -> "Descarregando"
          BatteryManager.BATTERY_STATUS_FULL -> "Completa"
          BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Conectado, não carregando"
          else -> "Desconhecido"
        }

        val health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
          BatteryManager.BATTERY_HEALTH_GOOD -> "Boa"
          BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Superaquecimento"
          BatteryManager.BATTERY_HEALTH_DEAD -> "Esgotada"
          BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Sobretensão"
          else -> "Normal"
        }

        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val powerSource = when (plugged) {
          BatteryManager.BATTERY_PLUGGED_AC -> "Carregador AC"
          BatteryManager.BATTERY_PLUGGED_USB -> "Porta USB"
          BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Sem Fio"
          else -> "Bateria"
        }

        BatteryInfo(
          level = batteryPct,
          isCharging = isCharging,
          status = statusStr,
          health = health,
          temperatureCelsius = temp,
          voltageMilliVolts = voltage,
          powerSource = powerSource
        )
      } else {
        BatteryInfo(
          level = 100,
          isCharging = false,
          status = "N/A",
          health = "N/A",
          temperatureCelsius = 25f,
          voltageMilliVolts = 3800,
          powerSource = "Bateria"
        )
      }
    } catch (e: Exception) {
      BatteryInfo(
        level = 100,
        isCharging = false,
        status = "N/A",
        health = "N/A",
        temperatureCelsius = 25f,
        voltageMilliVolts = 3800,
        powerSource = "Bateria"
      )
    }
  }

  fun getAppCounts(): Triple<Int, Int, Int> {
    return try {
      val pm = context.packageManager
      val packages = pm.getInstalledPackages(0)
      var user = 0
      var system = 0
      for (pkg in packages) {
        val appInfo = pkg.applicationInfo
        if (appInfo != null) {
          val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
            (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
          if (isSystem) system++ else user++
        }
      }
      Triple(packages.size, user, system)
    } catch (e: Exception) {
      Triple(0, 0, 0)
    }
  }

  suspend fun getSnapshot(): SystemSnapshot = withContext(Dispatchers.IO) {
    val counts = getAppCounts()
    SystemSnapshot(
      hardware = getHardwareInfo(),
      ram = getRamInfo(),
      storage = getStorageInfo(),
      cpu = getCpuInfo(),
      battery = getBatteryInfo(),
      totalInstalledApps = counts.first,
      userAppsCount = counts.second,
      systemAppsCount = counts.third
    )
  }

  fun monitorFlow(intervalMillis: Long = 2000L): Flow<SystemSnapshot> = flow {
    while (true) {
      emit(getSnapshot())
      delay(intervalMillis)
    }
  }.flowOn(Dispatchers.IO)

  private fun readKernelVersion(): String {
    return try {
      val file = File("/proc/version")
      if (file.exists() && file.canRead()) {
        BufferedReader(FileReader(file)).use { it.readLine() ?: System.getProperty("os.version") ?: "Linux" }
      } else {
        System.getProperty("os.version") ?: "Linux"
      }
    } catch (e: Exception) {
      System.getProperty("os.version") ?: "Linux"
    }
  }

  private fun readCpuModel(): String? {
    return try {
      val file = File("/proc/cpuinfo")
      if (file.exists() && file.canRead()) {
        BufferedReader(FileReader(file)).useLines { lines ->
          for (line in lines) {
            if (line.startsWith("Hardware", ignoreCase = true) ||
              line.startsWith("model name", ignoreCase = true) ||
              line.startsWith("Processor", ignoreCase = true)
            ) {
              val parts = line.split(":")
              if (parts.size > 1) {
                return parts[1].trim()
              }
            }
          }
        }
      }
      null
    } catch (e: Exception) {
      null
    }
  }

  private fun readCoreFrequency(core: Int): Long? {
    val path = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq"
    return try {
      val file = File(path)
      if (file.exists() && file.canRead()) {
        file.readText().trim().toLongOrNull()
      } else null
    } catch (e: Exception) {
      null
    }
  }
}
