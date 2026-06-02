package com.project.minlishapp.domain.repository

import com.project.minlishapp.domain.model.DailyStat
import kotlinx.coroutines.flow.Flow

interface DailyStatRepository {
    fun getWeeklyStats(userId: String): Flow<List<DailyStat>>
}
