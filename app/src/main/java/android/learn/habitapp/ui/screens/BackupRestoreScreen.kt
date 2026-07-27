package android.learn.habitapp.ui.screens

import android.learn.habitapp.HabitViewModel
import android.learn.habitapp.ui.components.ConfirmDialog
import android.learn.habitapp.ui.components.SubScreenTopBar
import android.learn.habitapp.ui.theme.HabitColors
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun BackupRestoreScreen(
   habitViewModel: HabitViewModel,
   onBack: () -> Unit,
) {
   val context = LocalContext.current
   val scope = rememberCoroutineScope()

   var isBusy by remember { mutableStateOf(false) }
   var statusMessage by remember { mutableStateOf<String?>(null) }
   var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

   val exportLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.CreateDocument("application/json")
   ) { uri ->
      if (uri == null) return@rememberLauncherForActivityResult
      isBusy = true
      scope.launch {
         statusMessage = try {
            withContext(Dispatchers.IO) {
               val json = habitViewModel.exportBackupJson()
               context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                  ?: throw IllegalStateException("Couldn't open the selected file for writing.")
            }
            "Backup saved."
         } catch (e: Exception) {
            "Couldn't save backup: ${e.message}"
         } finally {
            isBusy = false
         }
      }
   }

   val importLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.OpenDocument()
   ) { uri -> if (uri != null) pendingImportUri = uri }

   Column(
      modifier = Modifier
         .fillMaxSize()
         .padding(horizontal = 16.dp)
   ) {
      SubScreenTopBar(title = "Backup & Restore", onBack = onBack)
      Spacer(Modifier.height(8.dp))

      BackupActionCard(
         icon = Icons.Outlined.FileDownload,
         title = "Export Backup",
         subtitle = "Save all your habits and history to a file you choose.",
         buttonLabel = "Export",
         enabled = !isBusy,
         onClick = { exportLauncher.launch("habitapp_backup_${LocalDate.now()}.json") }
      )

      Spacer(Modifier.height(14.dp))

      BackupActionCard(
         icon = Icons.Outlined.FileUpload,
         title = "Import Backup",
         subtitle = "Add habits from a backup file. This only adds — it never deletes or overwrites what's already here.",
         buttonLabel = "Import",
         enabled = !isBusy,
         onClick = { importLauncher.launch(arrayOf("application/json")) }
      )

      if (isBusy) {
         Spacer(Modifier.height(16.dp))
         Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularProgressIndicator(modifier = Modifier.height(16.dp), color = HabitColors.Primary, strokeWidth = 2.dp)
            Text("Working…", style = MaterialTheme.typography.bodySmall, color = HabitColors.TextSecondary)
         }
      }

      statusMessage?.let {
         Spacer(Modifier.height(16.dp))
         Text(it, style = MaterialTheme.typography.bodySmall, color = HabitColors.TextSecondary)
      }
   }

   if (pendingImportUri != null) {
      val uri = pendingImportUri!!
      ConfirmDialog(
         title = "Import this backup?",
         text = "This adds every habit from the file to your current habits. It won't remove or overwrite anything already here.",
         confirmLabel = "Import",
         icon = Icons.Outlined.FileUpload,
         onConfirm = {
            pendingImportUri = null
            isBusy = true
            scope.launch {
               statusMessage = try {
                  val count = withContext(Dispatchers.IO) {
                     val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: throw IllegalStateException("Couldn't read the selected file.")
                     habitViewModel.importBackupJson(text)
                  }
                  "Imported $count habit${if (count == 1) "" else "s"}."
               } catch (e: Exception) {
                  "Couldn't import backup: ${e.message}"
               } finally {
                  isBusy = false
               }
            }
         },
         onDismiss = { pendingImportUri = null }
      )
   }
}

@Composable
private fun BackupActionCard(
   icon: ImageVector,
   title: String,
   subtitle: String,
   buttonLabel: String,
   enabled: Boolean,
   onClick: () -> Unit,
) {
   Column(
      modifier = Modifier
         .fillMaxWidth()
         .background(color = HabitColors.Surface, shape = RoundedCornerShape(16.dp))
         .padding(16.dp)
   ) {
      Row(
         verticalAlignment = Alignment.CenterVertically,
         horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
         Icon(icon, contentDescription = null, tint = HabitColors.PrimaryDark)
         Text(
            title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight(700)),
            color = HabitColors.TextPrimary
         )
      }
      Spacer(Modifier.height(6.dp))
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = HabitColors.TextSecondary)
      Spacer(Modifier.height(12.dp))
      Button(
         onClick = onClick,
         enabled = enabled,
         shape = RoundedCornerShape(50),
         colors = ButtonDefaults.buttonColors(
            containerColor = HabitColors.Primary,
            contentColor = HabitColors.Surface
         )
      ) {
         Text(buttonLabel)
      }
   }
}
