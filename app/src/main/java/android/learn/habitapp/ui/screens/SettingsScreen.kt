package android.learn.habitapp.ui.screens

import android.learn.habitapp.ui.components.SubScreenTopBar
import android.learn.habitapp.ui.theme.HabitColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun SettingsScreen(
   onBack: () -> Unit,
   canScheduleExactAlarms: () -> Boolean,
   onRequestExactAlarmPermission: () -> Unit,
) {
   // Exact-alarm permission is granted from system settings, outside the app,
   // so re-check when the screen resumes rather than only once at first composition.
   val lifecycleOwner = LocalLifecycleOwner.current
   var isAlarmPermissionGranted by remember { mutableStateOf(canScheduleExactAlarms()) }

   DisposableEffect(lifecycleOwner) {
      val observer = LifecycleEventObserver { _, event ->
         if (event == Lifecycle.Event.ON_RESUME) {
            isAlarmPermissionGranted = canScheduleExactAlarms()
         }
      }
      lifecycleOwner.lifecycle.addObserver(observer)
      onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
   }

   val context = LocalContext.current
   val appVersion = remember {
      try {
         context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—"
      } catch (e: Exception) {
         "—"
      }
   }

   Column(
      modifier = Modifier
         .fillMaxSize()
         .padding(horizontal = 16.dp)
   ) {
      SubScreenTopBar(title = "Settings", onBack = onBack)

      Spacer(Modifier.height(8.dp))
      SettingsSectionLabel("Notifications")
      SettingsCard {
         SettingsRow(
            icon = Icons.Outlined.Alarm,
            title = "Exact Alarm Permission",
            subtitle = if (isAlarmPermissionGranted)
               "Granted — reminders will fire on time"
            else
               "Not granted — tap to allow in system settings",
            trailing = { StatusDot(granted = isAlarmPermissionGranted) },
            onClick = if (isAlarmPermissionGranted) null else onRequestExactAlarmPermission
         )
      }

      Spacer(Modifier.height(20.dp))
      SettingsSectionLabel("About")
      SettingsCard {
         SettingsRow(
            icon = Icons.Outlined.Info,
            title = "Version",
            subtitle = appVersion,
            onClick = null
         )
      }
   }
}

@Composable
private fun SettingsSectionLabel(text: String) {
   Text(
      text = text.uppercase(),
      style = MaterialTheme.typography.bodySmall.copy(
         fontWeight = FontWeight(600),
         color = HabitColors.TextSecondary,
         letterSpacing = 0.6.sp,
      ),
      modifier = Modifier.padding(bottom = 8.dp)
   )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
   Column(
      modifier = Modifier
         .fillMaxWidth()
         .background(color = HabitColors.Surface, shape = RoundedCornerShape(16.dp))
   ) {
      content()
   }
}

@Composable
private fun SettingsRow(
   icon: ImageVector,
   title: String,
   subtitle: String,
   onClick: (() -> Unit)?,
   trailing: @Composable () -> Unit = {},
) {
   Row(
      modifier = Modifier
         .fillMaxWidth()
         .let { if (onClick != null) it.clickable(onClick = onClick) else it }
         .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp)
   ) {
      Icon(icon, contentDescription = null, tint = HabitColors.PrimaryDark)
      Column(modifier = Modifier.weight(1f)) {
         Text(
            title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
            color = HabitColors.TextPrimary
         )
         Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = HabitColors.TextSecondary
         )
      }
      trailing()
   }
}

@Composable
private fun StatusDot(granted: Boolean) {
   Box(
      modifier = Modifier
         .size(10.dp)
         .background(
            color = if (granted) HabitColors.Primary else HabitColors.Danger,
            shape = CircleShape
         )
   )
}
