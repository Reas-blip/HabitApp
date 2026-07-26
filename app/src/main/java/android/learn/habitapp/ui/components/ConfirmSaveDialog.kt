package android.learn.habitapp.ui.components

import android.learn.habitapp.ui.theme.HabitColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Composable
fun ConfirmSaveDialog(
   onDiscardRequest: () -> Unit,
   onCancel: () -> Unit,
   onSave: () -> Unit,
   dialogTitle: String,
   dialogText: String,
   icon: ImageVector,
) {
   Dialog(onDismissRequest = { onCancel() }) {
      Card(
         colors = CardDefaults.cardColors(containerColor = HabitColors.SurfaceTint),
         border = BorderStroke(width = 1.dp, HabitColors.SoftBorder),
         modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
         shape = RoundedCornerShape(16.dp),
      ) {
         Box {
            IconButton(onClick = { onCancel() }, modifier = Modifier.align(Alignment.TopEnd)) {
               Icon(
                  Icons.Default.Close,
                  contentDescription = "Cancel Save",
                  tint = HabitColors.Danger
               )
            }
            Column(
               modifier = Modifier
                  .align(Alignment.Center)
                  .fillMaxWidth()
                  .padding(24.dp),
               horizontalAlignment = Alignment.CenterHorizontally,
            ) {
               Icon(imageVector = icon, contentDescription = "Save Dialog Icon")

               Spacer(modifier = Modifier.height(16.dp))

               Text(
                  text = dialogTitle,
                  style = MaterialTheme.typography.titleLarge,
                  maxLines = 1,
                  color = HabitColors.TextPrimary
               )

               Spacer(modifier = Modifier.height(8.dp))

               Text(
                  text = dialogText,
                  style = MaterialTheme.typography.bodyMedium,
                  textAlign = TextAlign.Center,
                  color = HabitColors.TextPrimary
               )

               Spacer(modifier = Modifier.height(24.dp))



               Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
               ) {

                  Button(
                     onClick = { onSave() }, shape = RoundedCornerShape(
                        topStartPercent = 50,
                        topEndPercent = 15,
                        bottomEndPercent = 15,
                        bottomStartPercent = 50,
                     ), modifier = Modifier.weight(.45f)
                  ) {
                     Text("Save")
                  }
                  Spacer(Modifier.width(8.dp))
                  Button(
                     onClick = { onDiscardRequest() }, shape = RoundedCornerShape(
                        topStartPercent = 15,
                        topEndPercent = 50,
                        bottomEndPercent = 50,
                        bottomStartPercent = 15,
                     ), modifier = Modifier.weight(.45f)
                  ) {
                     Text("Discard")
                  }

               }

            }
         }
      }
   }
}
