import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

object KeyboardMarkup {

    val mainMenuKeyboardMarkup = InlineKeyboardMarkup.create(
        listOf(
            InlineKeyboardButton.CallbackData(text = "\uD83D\uDCDD Goals (/goals)", callbackData = "goalsButton"),
            InlineKeyboardButton.CallbackData(text = "\uD83D\uDCC6 Habit Tracker", callbackData = "habitTrackerButton")),
        listOf(
            InlineKeyboardButton.CallbackData(text = "\uD83D\uDCCA Progress", callbackData = "progressButton"),
            InlineKeyboardButton.CallbackData(text = "⚙\uFE0F Settings", callbackData = "settingsButton")),

        )
    val goalsAddShowKeyboardMarkup = InlineKeyboardMarkup.create(
        listOf(
            InlineKeyboardButton.CallbackData(text = "➕ Add Goal", callbackData = "addGoalButton"),
            InlineKeyboardButton.CallbackData(text = "📋 Show Goals", callbackData = "showGoalsButton")
        ),
        listOf(
            InlineKeyboardButton.CallbackData(text = "🏠 Main Menu", callbackData = "mainMenuButton")
        )
    )
    val goalsCategoryMarkup = InlineKeyboardMarkup.create(
        listOf(
            InlineKeyboardButton.CallbackData(text = "🏋️‍♂️ Sport", callbackData = "sportButton"),
            InlineKeyboardButton.CallbackData(text = "📚 Reading", callbackData = "readingButton"),
            InlineKeyboardButton.CallbackData(text = "💼 Work", callbackData = "workButton"),
            InlineKeyboardButton.CallbackData(text = "🎨 Personal", callbackData = "personalButton")),
        listOf(InlineKeyboardButton.CallbackData(text = "🏠 Main Menu", callbackData = "mainMenuButton")
        )
    )

    val backToMainMenuSingleMarkup = InlineKeyboardMarkup.create(
        listOf(listOf(InlineKeyboardButton.CallbackData(text= "Main Menu", callbackData = "mainMenuButton")))
    )

    val addNewOrShowHabitMarkup = InlineKeyboardMarkup.create(
        listOf(
            InlineKeyboardButton.CallbackData(text = "➕ Add New Habit", callbackData = "addNewHabitButton"),
            InlineKeyboardButton.CallbackData(text = "📋 Show Habits", callbackData = "showHabitsButton")
        ),
        listOf(
            InlineKeyboardButton.CallbackData(text = "🏠 Main Menu", callbackData = "mainMenuButton")
        )
    )

    val settingsMarkup = InlineKeyboardMarkup.create(
        listOf(
            InlineKeyboardButton.CallbackData(text = "❓ Help" , callbackData = "helpButton"),
            InlineKeyboardButton.CallbackData(text = "\uD83D\uDDD1\uFE0F\n Delete" , callbackData = "deleteButton")
        ),
        listOf(
            InlineKeyboardButton.CallbackData(text = "🏠 Main Menu", callbackData = "mainMenuButton")
        )
    )
}