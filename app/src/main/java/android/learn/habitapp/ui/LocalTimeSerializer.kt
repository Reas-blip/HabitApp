package android.learn.habitapp.ui

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalTime

object LocalTimeSerializer : KSerializer<LocalTime> {
   override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

   override fun serialize(encoder: Encoder, value: LocalTime) {
      encoder.encodeString(value.toString()) // e.g. "08:30" or "08:30:15"
   }

   override fun deserialize(decoder: Decoder): LocalTime {
      return LocalTime.parse(decoder.decodeString())
   }
}
