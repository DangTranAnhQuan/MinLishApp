package com.project.minlishapp.presentation.dashboard

data class DashboardUiState(
    val totalWordsLearned: Int = 0,
    val currentStreak: Int = 0,
    val accuracy: Float = 0f,
    val dailyActivityData: List<Float> = emptyList(),
    val retentionData: List<Float?> = emptyList()
) {
    val level: Level
        get() = when {
            totalWordsLearned < 150 -> Level.Beginner
            totalWordsLearned <= 600 -> Level.Intermediate
            else -> Level.Advanced
        }
}

enum class Level(val title: String, val colorHex: Long) {
    Beginner("Beginner", 0xFF4CAF50),
    Intermediate("Intermediate", 0xFF2196F3),
    Advanced("Advanced", 0xFF9C27B0)
}
