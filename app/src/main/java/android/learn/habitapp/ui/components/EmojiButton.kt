package android.learn.habitapp.ui.components

import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun EmojiButton(
   selectedEmoji: String,
   modifier: Modifier = Modifier,
   onClickIcon: () -> Unit = {},
) {
   IconButton(
      onClick = onClickIcon,
      colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.surface),
      modifier = Modifier.then(modifier)
   ) {
      Text(selectedEmoji, style = MaterialTheme.typography.headlineSmall)
   }
}
