package com.project.minlishapp.data.repository

import com.project.minlishapp.domain.model.DailyStat
import com.project.minlishapp.domain.repository.DailyStatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class DailyStatRepositoryImpl @Inject constructor() : DailyStatRepository {
    override fun getWeeklyStats(userId: String): Flow<List<DailyStat>> {
        // Mock implementation to prevent build error
        return flowOf(emptyList())
    }
}

