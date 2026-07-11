package android.learn.habitapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorFilterRow(
   availableColors: List<Int>,
   selectedColor: Int?,
   onColorSelected: (Int?) -> Unit,
   modifier: Modifier = Modifier
) {
   if (availableColors.isEmpty()) return // nothing to filter by yet

   LazyRow(
      modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
   ) {
      items(availableColors) { colorInt ->
         val isSelected = selectedColor == colorInt
         Box(
            modifier = Modifier
               .size(28.dp)
               .clip(CircleShape)
               .background(Color(colorInt))
               .border(
                  width = if (isSelected) 2.dp else 0.dp,
                  color = MaterialTheme.colorScheme.onSurface,
                  shape = CircleShape
               )
               .clickable { onColorSelected(colorInt) },
            contentAlignment = Alignment.Center
         ) {
            if (isSelected) {
               Icon(
                  Icons.Default.Check,
                  contentDescription = "Filtering by this color",
                  modifier = Modifier.size(16.dp),
                  tint = Color.White
               )
            }
         }
      }
   }
}
