package android.learn.habitapp.data.repository

import android.learn.habitapp.data.emoji.HabitEmoji
import android.learn.habitapp.data.emoji.HabitEmojiData
import android.learn.habitapp.data.emoji.HabitEmojiData.categories
import kotlin.collections.filter

object HabitEmojiRepository {

   fun getByCategory(category: String): List<HabitEmoji> {
      return if (category == "Recent") {
         emptyList() // hook later
      } else {
         HabitEmojiData.categories.firstOrNull {
            it.name == category
         }?.emojis ?: emptyList()
      }
   }
   fun search(
      query: String,
      items: List<HabitEmoji>
   ): List<HabitEmoji> {

      if (query.isBlank()) return items
      val items = HabitEmojiData.flattenedList
      val q = query.lowercase()

      return items.filter { emoji ->
         emoji.emoji.contains(query) ||
                 emoji.keywords.any { it.contains(q) }
      }
   }
}

