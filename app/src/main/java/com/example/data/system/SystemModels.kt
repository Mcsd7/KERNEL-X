package com.example.data.system

data class DeviceHardwareInfo(
  val manufacturer: String,
  val model: String,
  val device: String,
  val product: String,
  val hardware: String,
  val board: String,
  val androidVersion: String,
  val apiLevel: Int,
  val securityPatch: String,
  val supportedAbis: List<String>,
  val kernelVersion: String,
  val uptimeMillis: Long,
  val buildFingerprint: String
)

data class RamInfo(
  val totalBytes: Long,
  val usedBytes: Long,
  val availableBytes: Long,
  val usedPercent: Float,
  val isLowMemory: Boolean,
  val thresholdBytes: Long
)

data class StorageInfo(
  val totalBytes: Long,
  val usedBytes: Long,
  val freeBytes: Long,
  val usedPercent: Float
)

data class CpuCoreInfo(
  val coreIndex: Int,
  val frequencyKHz: Long? = null
)

data class CpuInfo(
  val architecture: String,
  val coresCount: Int,
  val processorName: String,
  val abiList: String,
  val cores: List<CpuCoreInfo> = emptyList()
)

data class BatteryInfo(
  val level: Int,
  val isCharging: Boolean,
  val status: String,
  val health: String,
  val temperatureCelsius: Float,
  val voltageMilliVolts: Int,
  val powerSource: String
)

data class SystemSnapshot(
  val hardware: DeviceHardwareInfo,
  val ram: RamInfo,
  val storage: StorageInfo,
  val cpu: CpuInfo,
  val battery: BatteryInfo,
  val totalInstalledApps: Int,
  val userAppsCount: Int,
  val systemAppsCount: Int,
  val timestamp: Long = System.currentTimeMillis()
)
