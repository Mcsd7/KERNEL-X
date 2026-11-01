package com.example.data.apps

import android.graphics.drawable.Drawable

data class AppItem(
  val packageName: String,
  val appName: String,
  val versionName: String,
  val versionCode: Long,
  val isSystemApp: Boolean,
  val minSdk: Int,
  val targetSdk: Int,
  val firstInstallTime: Long,
  val lastUpdateTime: Long,
  val sourceDir: String,
  val apkSizeBytes: Long,
  val hasLaunchIntent: Boolean,
  val requestedPermissions: List<String>,
  val grantedPermissions: List<String>,
  val icon: Drawable? = null
)

enum class SensitivePermissionGroup(
  val id: String,
  val title: String,
  val description: String,
  val permissions: List<String>
) {
  CAMERA(
    id = "camera",
    title = "Câmera",
    description = "Captura de fotos e gravação de vídeos",
    permissions = listOf("android.permission.CAMERA")
  ),
  MICROPHONE(
    id = "microphone",
    title = "Microfone",
    description = "Gravação de áudio e reconhecimento de voz",
    permissions = listOf("android.permission.RECORD_AUDIO")
  ),
  LOCATION(
    id = "location",
    title = "Localização",
    description = "Acesso a GPS e localização precisa/aproximada",
    permissions = listOf(
      "android.permission.ACCESS_FINE_LOCATION",
      "android.permission.ACCESS_COARSE_LOCATION",
      "android.permission.ACCESS_BACKGROUND_LOCATION"
    )
  ),
  CONTACTS(
    id = "contacts",
    title = "Contatos",
    description = "Leitura e modificação da agenda de contatos",
    permissions = listOf(
      "android.permission.READ_CONTACTS",
      "android.permission.WRITE_CONTACTS",
      "android.permission.GET_ACCOUNTS"
    )
  ),
  PHONE(
    id = "phone",
    title = "Telefone & Chamadas",
    description = "Discagem, registro de chamadas e estado celular",
    permissions = listOf(
      "android.permission.READ_PHONE_STATE",
      "android.permission.CALL_PHONE",
      "android.permission.READ_CALL_LOG",
      "android.permission.WRITE_CALL_LOG",
      "android.permission.ANSWER_PHONE_CALLS"
    )
  ),
  SMS(
    id = "sms",
    title = "SMS & Mensagens",
    description = "Envio, recebimento e leitura de mensagens SMS",
    permissions = listOf(
      "android.permission.SEND_SMS",
      "android.permission.RECEIVE_SMS",
      "android.permission.READ_SMS"
    )
  ),
  STORAGE(
    id = "storage",
    title = "Arquivos & Mídia",
    description = "Acesso a fotos, áudios, vídeos e armazenamento",
    permissions = listOf(
      "android.permission.READ_EXTERNAL_STORAGE",
      "android.permission.WRITE_EXTERNAL_STORAGE",
      "android.permission.READ_MEDIA_IMAGES",
      "android.permission.READ_MEDIA_VIDEO",
      "android.permission.READ_MEDIA_AUDIO",
      "android.permission.MANAGE_EXTERNAL_STORAGE"
    )
  ),
  NOTIFICATIONS(
    id = "notifications",
    title = "Notificações",
    description = "Exibição de alertas e avisos em segundo plano",
    permissions = listOf("android.permission.POST_NOTIFICATIONS")
  )
}

data class PermissionAppDetail(
  val packageName: String,
  val appName: String,
  val isSystemApp: Boolean,
  val declaredPermissions: List<String>,
  val grantedPermissions: List<String>,
  val deniedPermissions: List<String>,
  val icon: Drawable? = null
)

data class PermissionGroupSummary(
  val group: SensitivePermissionGroup,
  val grantedAppsCount: Int,
  val declaredAppsCount: Int,
  val apps: List<PermissionAppDetail>
)
