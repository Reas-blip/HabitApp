package android.learn.habitapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
         modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
         shape = RoundedCornerShape(16.dp),
      ) {
         Column(
            modifier = Modifier
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
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
               text = dialogText,
               style = MaterialTheme.typography.bodyMedium,
               textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary action full-width, like a system dialog's main button
            Button(
               onClick = { onSave() },
               modifier = Modifier.fillMaxWidth(),
            ) {
               Text("Save")
            }

            Row(
               modifier = Modifier.fillMaxWidth(),
               horizontalArrangement = Arrangement.SpaceBetween,
            ) {

               Button(
                  onClick = { onCancel() }, shape = RoundedCornerShape(
                     topStartPercent = 50,
                     topEndPercent = 15,
                     bottomEndPercent = 15,
                     bottomStartPercent = 50,
                  ), modifier = Modifier.weight(.45f)
               ) {
                  Text("Cancel")
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
