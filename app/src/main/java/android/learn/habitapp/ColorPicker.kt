package android.learn.habitapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val HabitColorPalette = listOf(
   0xFFE57373.toInt(), // red
   0xFFFFB74D.toInt(), // orange
   0xFFFFF176.toInt(), // yellow
   0xFF81C784.toInt(), // green
   0xFF4FC3F7.toInt(), // light blue
   0xFF7986CB.toInt(), // indigo
   0xFFBA68C8.toInt(), // purple
   0xFFF06292.toInt(), // pink
)

@Composable
fun ColorPicker(
   selectedColor: Int?,
   onColorSelected: (Int?) -> Unit,
   modifier: Modifier = Modifier
) {
   Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
      Text(
         "Color",
         style = MaterialTheme.typography.labelLarge,
         color = MaterialTheme.colorScheme.onSurfaceVariant,
         modifier = Modifier.padding(bottom = 8.dp)
      )

      LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
         item {
            // "None" option — resets to default theme color
            ColorSwatch(
               color = null,
               isSelected = selectedColor == null,
               onClick = { onColorSelected(null) }
            )
         }
         items(HabitColorPalette) { colorInt ->
            ColorSwatch(
               color = Color(colorInt),
               isSelected = selectedColor == colorInt,
               onClick = { onColorSelected(colorInt) }
            )
         }
      }
   }
}

@Composable
private fun ColorSwatch(
   color: Color?,
   isSelected: Boolean,
   onClick: () -> Unit
) {
   Box(
      modifier = Modifier
         .size(40.dp)
         .clip(CircleShape)
         .background(color ?: MaterialTheme.colorScheme.surfaceContainerHigh)
         .border(
            width = if (isSelected) 3.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            shape = CircleShape
         )
         .clickable { onClick() },
      contentAlignment = Alignment.Center
   ) {
      if (color == null) {
         Icon(
            Icons.Outlined.Block,
            contentDescription = "No color",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
         )
      } else if (isSelected) {
         Icon(
            Icons.Default.Check,
            contentDescription = "Selected",
            modifier = Modifier.size(20.dp),
            tint = Color.White
         )
      }
   }
}