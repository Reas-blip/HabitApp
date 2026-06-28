package android.learn.habitapp.data.emoji

data class EmojiCategory(
   val name: String,
   val icon: String,
   val emojis: List<HabitEmoji>
)

data class HabitEmoji(
   val emoji: String,
   val keywords: List<String>,
)
object HabitEmojiData {


   val categories = listOf(
      EmojiCategory(
         name = "Fitness",
         icon = "💪",
         emojis = listOf(
            // ===================== FITNESS =====================
            HabitEmoji("🏃", listOf("run", "running", "jog", "cardio", "exercise")),
            HabitEmoji("🚶", listOf("walk", "walking", "steps", "stroll")),
            HabitEmoji("🚴", listOf("bike", "cycling", "ride", "bicycle")),
            HabitEmoji("🏋️", listOf("gym", "weights", "lift", "strength")),
            HabitEmoji("🤸", listOf("stretch", "flexibility", "gymnastics")),
            HabitEmoji("🏊", listOf("swim", "pool", "water sport")),
            HabitEmoji("🧘", listOf("meditation", "yoga", "calm", "breathing")),
            HabitEmoji("💪", listOf("strong", "muscle", "power", "strength")),
            HabitEmoji("🏃‍♂️", listOf("run", "male run")),
            HabitEmoji("🏃‍♀️", listOf("run", "female run")),
            HabitEmoji("🥊", listOf("boxing", "fight", "training")),
            HabitEmoji("🏓", listOf("table tennis", "ping pong")),
            HabitEmoji("🏸", listOf("badminton")),
            HabitEmoji("⚽", listOf("football", "soccer")),
            HabitEmoji("🏀", listOf("basketball")),
            HabitEmoji("🎾", listOf("tennis")),
            HabitEmoji("🏈", listOf("american football")),
            HabitEmoji("🏐", listOf("volleyball")),
            HabitEmoji("⛹️", listOf("basketball play")),
         )
      ),
      EmojiCategory(
         name = "Health",
         icon = "🩺",
         emojis = listOf(
// ===================== HEALTH =====================
            HabitEmoji("💧", listOf("water", "hydrate", "drink")),
            HabitEmoji("🍎", listOf("apple", "fruit", "healthy")),
            HabitEmoji("🥗", listOf("salad", "diet", "healthy food")),
            HabitEmoji("🥦", listOf("broccoli", "vegetable")),
            HabitEmoji("🥕", listOf("carrot", "veggie")),
            HabitEmoji("🍵", listOf("tea", "green tea")),
            HabitEmoji("☕", listOf("coffee", "caffeine")),
            HabitEmoji("😴", listOf("sleep", "rest", "nap")),
            HabitEmoji("🛌", listOf("sleep", "bed")),
            HabitEmoji("🧠", listOf("brain", "focus", "mind")),
            HabitEmoji("❤️", listOf("heart", "health", "love")),
            HabitEmoji("💊", listOf("medicine", "pill")),
            HabitEmoji("🩺", listOf("doctor", "checkup")),
            HabitEmoji("🧘‍♂️", listOf("meditation male")),
            HabitEmoji("🧘‍♀️", listOf("meditation female")),
         )
      ),
      EmojiCategory(
         name = "Learning",
         icon = "📖",
         emojis = listOf(
// ===================== LEARNING =====================
            HabitEmoji("📚", listOf("books", "study", "reading")),
            HabitEmoji("📖", listOf("read", "book reading")),
            HabitEmoji("✏️", listOf("write", "writing")),
            HabitEmoji("📝", listOf("notes", "study notes")),
            HabitEmoji("🎓", listOf("school", "graduation", "study")),
            HabitEmoji("💻", listOf("coding", "computer", "programming")),
            HabitEmoji("🧠", listOf("thinking", "brain", "learning")),
            HabitEmoji("🔬", listOf("science", "experiment")),
            HabitEmoji("🧪", listOf("chemistry", "lab")),
            HabitEmoji("📐", listOf("math", "geometry")),
            HabitEmoji("🧮", listOf("calculation", "math")),
            HabitEmoji("🎨", listOf("art", "drawing")),
         )
      ),
      EmojiCategory(
         name = "Work",
         icon = "💼",
         emojis = listOf(
            // ===================== WORK =====================
            HabitEmoji("💼", listOf("work", "job", "office")),
            HabitEmoji("📊", listOf("charts", "analysis")),
            HabitEmoji("📈", listOf("growth", "increase")),
            HabitEmoji("📉", listOf("decline", "stats")),
            HabitEmoji("📅", listOf("calendar", "schedule")),
            HabitEmoji("⏰", listOf("alarm", "time", "deadline")),
            HabitEmoji("📌", listOf("pin", "important")),
            HabitEmoji("📋", listOf("tasks", "clipboard")),
            HabitEmoji("🗂️", listOf("files", "organization")),
            HabitEmoji("🚀", listOf("launch", "project", "start")),
            HabitEmoji("☎️", listOf("call", "phone")),
         )
      ),
      EmojiCategory(
         name = "Home",
         icon = "🏠",
         emojis = listOf(
            // ===================== HOME =====================
            HabitEmoji("🏠", listOf("home", "house")),
            HabitEmoji("🧹", listOf("clean", "cleaning")),
            HabitEmoji("🧺", listOf("laundry")),
            HabitEmoji("🛒", listOf("shopping", "groceries")),
            HabitEmoji("🍳", listOf("cooking", "food prep")),
            HabitEmoji("🧼", listOf("wash", "cleaning")),
            HabitEmoji("🛏️", listOf("bed", "make bed")),
            HabitEmoji("🚪", listOf("door", "home entry")),
            HabitEmoji("🔧", listOf("repair", "fix")),
            HabitEmoji("🪴", listOf("plants", "gardening")),
         )
      ),
      EmojiCategory(
         name = "Food",
         icon = "🍎",
         emojis = listOf(
            // ===================== FOOD =====================
            HabitEmoji("🍎", listOf("apple", "fruit")),
            HabitEmoji("🍌", listOf("banana", "fruit")),
            HabitEmoji("🍞", listOf("bread")),
            HabitEmoji("🥛", listOf("milk")),
            HabitEmoji("🍗", listOf("chicken")),
            HabitEmoji("🍔", listOf("burger")),
            HabitEmoji("🍕", listOf("pizza")),
            HabitEmoji("🍜", listOf("noodles", "ramen")),
            HabitEmoji("🍲", listOf("stew", "meal")),
            HabitEmoji("🍇", listOf("grapes")),
            HabitEmoji("🥑", listOf("avocado")),
            HabitEmoji("🍓", listOf("strawberry")),
            HabitEmoji("🥜", listOf("nuts")),
         )
      ),
      EmojiCategory(
         name = "Nature",
         icon = "🌱",
         emojis = listOf(
            // ===================== NATURE =====================
            HabitEmoji("🌿", listOf("plant", "nature", "green")),
            HabitEmoji("🌱", listOf("growth", "sprout")),
            HabitEmoji("🌳", listOf("tree")),
            HabitEmoji("🌻", listOf("sunflower")),
            HabitEmoji("🌸", listOf("flower", "blossom")),
            HabitEmoji("🌞", listOf("sun", "sunny")),
            HabitEmoji("🌙", listOf("moon", "night")),
            HabitEmoji("🌧️", listOf("rain")),
            HabitEmoji("🌈", listOf("rainbow")),
            HabitEmoji("🌊", listOf("water", "ocean")),
            HabitEmoji("🐾", listOf("paw", "animal tracks")),
         )
      ),
      EmojiCategory(
         name = "Motivation / Symbols",
         icon = "🔥",
         emojis = listOf(
            // ===================== MOTIVATION / SYMBOLS =====================
            HabitEmoji("🔥", listOf("fire", "hot", "energy")),
            HabitEmoji("⭐", listOf("star", "best")),
            HabitEmoji("🎯", listOf("goal", "target")),
            HabitEmoji("✔️", listOf("done", "complete")),
            HabitEmoji("❌", listOf("fail", "cancel")),
            HabitEmoji("💯", listOf("perfect", "hundred")),
            HabitEmoji("🚀", listOf("launch", "start", "speed")),
            HabitEmoji("⚡", listOf("energy", "fast")),
            HabitEmoji("⏱️", listOf("timer", "focus")),
            HabitEmoji("🏆", listOf("win", "trophy", "success"))
         )
      ),
   )

   val flattenedList: List<HabitEmoji> = buildList{
     categories.forEach { addAll( it.emojis ) }
  }

}


//LazyColumn(
//verticalArrangement = Arrangement.spacedBy(16.dp),
//modifier = Modifier.weight(1f, fill = false)
//) {
//   habitEmojiCategories.forEach { (category, emojis) ->
//      item {
//         Column {
//            Text(
//               text = category,
//               style = MaterialTheme.typography.labelMedium,
//               color = MaterialTheme.colorScheme.onSurfaceVariant,
//               modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            // A simple flow-like grid layout for the emojis
//            Row(
//               modifier = Modifier.fillMaxWidth(),
//               horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//               emojis.forEach { emoji ->
//                  Box(
//                     modifier = Modifier
//                        .size(48.dp)
//                        .clip(CircleShape)
//                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
//                        .clickable {
//                           onEmojiSelected(emoji)
//                           onCloseSheet()
//                        },
//                     contentAlignment = Alignment.Center
//                  ) {
//                     Text(text = emoji, fontSize = 24.sp)
//                  }
//               }
//            }
//         }
//      }
//   }
//}
//
//HorizontalDivider(
//modifier = Modifier.padding(vertical = 16.dp),
//color = MaterialTheme.colorScheme.outlineVariant
//)
//val softwareKeyboardController = LocalSoftwareKeyboardController.current
//// Fallback text field so they can use their native device keyboard for custom emojis
//var customInput by remember { mutableStateOf("") }
//OutlinedTextField(
//value = customInput,
//keyboardOptions = KeyboardOptions(
//keyboardType = KeyboardType.Text,
//platformImeOptions = PlatformImeOptions("com.google.android.inputmethod.latin.emoji")
//),
//onValueChange = { input ->
//   customInput = input
//
//   if (input.isNotBlank()) {
//      onEmojiSelected(input)
//      onDismissRequest()
//   }
//},
//placeholder = { Text("Type or paste a custom emoji...") },
//modifier = Modifier.fillMaxWidth(),
//singleLine = true,
//shape = RoundedCornerShape(12.dp)
//)