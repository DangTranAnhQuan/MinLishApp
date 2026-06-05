package com.project.minlishapp.core.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.project.minlishapp.core.notification.NotificationHelper
import com.project.minlishapp.domain.repository.AuthRepository
import com.project.minlishapp.domain.usecase.srs.GetDueCardsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val getDueCardsUseCase: GetDueCardsUseCase,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("DailyReminderWorker", "Worker started")
        return try {
            val currentUser = authRepository.currentUser.first()
            if (currentUser == null) {
                return Result.success()
            }

            val dueCards = getDueCardsUseCase(currentUser.uid, System.currentTimeMillis()).first()
            val count = dueCards.size

            if (count > 0) {
                NotificationHelper.showNotification(
                    context = applicationContext,
                    title = "MinLish - Đã đến giờ ôn tập! 🚀",
                    message = "Bạn có $count từ vựng đang chờ được ôn tập hôm nay..."
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
