package android.learn.habitapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun IOSInsetListRow(
   title: String,
   icon: ImageVector,
   badgeCount: Int = 0,
   selected: Boolean,
   onClick: () -> Unit,
   modifier: Modifier = Modifier
) {
   Row(
      modifier = modifier
         .height(50.dp)
         .fillMaxWidth()
         .clip(RoundedCornerShape(10.dp))
         .background(if (selected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer)
         .clickable(onClick = onClick)
         .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
   ) {
      Icon(
         imageVector = icon,
         contentDescription = null,
         tint = Color(0xFF007AFF),
         modifier = Modifier.size(22.dp)
      )
      Spacer(Modifier.width(12.dp))
      Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp)
      if (badgeCount > 0) {
         Box(
            modifier = Modifier
               .defaultMinSize(20.dp, 20.dp)
               .clip(RoundedCornerShape(10.dp))
               .background(Color(0xFFFF3B30))
               .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
         ) {
            Text(
               text = if (badgeCount > 99) "99+" else badgeCount.toString(),
               color = Color.White,
               fontSize = 12.sp,
               fontWeight = FontWeight.Bold
            )
         }
      }
   }
}
