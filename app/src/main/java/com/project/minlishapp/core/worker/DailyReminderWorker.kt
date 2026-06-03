package com.project.minlishapp.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.project.minlishapp.core.notification.NotificationHelper
import com.project.minlishapp.domain.repository.AuthRepository
import com.project.minlishapp.domain.use_case.GetDueCardsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getDueCardsUseCase: GetDueCardsUseCase,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val currentUser = authRepository.currentUser.first()
        if (currentUser == null) return Result.success()

        val dueCards = getDueCardsUseCase(currentUser.uid).first()
        val count = dueCards.size

        if (count > 0) {
            NotificationHelper.showNotification(
                context = applicationContext,
                title = "Đã đến giờ ôn tập! 🚀",
                message = "Bạn có $count từ vựng đang chờ được ôn tập hôm nay..."
            )
        }

        return Result.success()
    }
}
