package android.learn.habitapp.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.dropShadow(
   shape: RoundedCornerShape,
   color: Color = Color.Black.copy(alpha = 0.25f),
   blur: Dp = 6.dp,
   offsetX: Dp = 0.dp,
   offsetY: Dp = 2.dp,
   spread: Dp = (-3).dp
) = drawBehind {
   val shadowSize = Size(size.width + spread.toPx() * 2, size.height + spread.toPx() * 2)
   val outline = shape.createOutline(shadowSize, layoutDirection, this)

   val path = when (outline) {
      is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
      is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
      is Outline.Generic -> outline.path
   }

   val frameworkPaint = Paint().apply {
      this.color = color.toArgb()
      if (blur.toPx() > 0f) {
         maskFilter = BlurMaskFilter(blur.toPx(), BlurMaskFilter.Blur.NORMAL)
      }
   }

   drawIntoCanvas { canvas ->
      canvas.nativeCanvas.save()
      canvas.nativeCanvas.translate(
         offsetX.toPx() - spread.toPx(),
         offsetY.toPx() - spread.toPx()
      )
      canvas.nativeCanvas.drawPath(path.asAndroidPath(), frameworkPaint)
      canvas.nativeCanvas.restore()
   }
}