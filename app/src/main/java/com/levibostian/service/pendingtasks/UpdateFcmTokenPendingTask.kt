package com.levibostian.service.pendingtasks

import com.levibostian.service.manager.UserManager
import com.levibostian.service.repository.UserRepository
import com.levibostian.wendy.service.PendingTask
import com.levibostian.wendy.types.PendingTaskResult
import io.reactivex.schedulers.Schedulers

class UpdateFcmTokenPendingTask(userId: String): PendingTask(dataId = userId, // userId is the dataId to simply be the unique ID for this task for Wendy to only keep 1 at a time.
        groupId = null,
        manuallyRun = false,
        tag = UpdateFcmTokenPendingTask.TAG) {

    lateinit var userManager: UserManager
    lateinit var userRepository: UserRepository

    companion object {
        const val TAG = "UpdateFcmTokenPendingTask"

        fun blank(userManager: UserManager, userRepository: UserRepository): UpdateFcmTokenPendingTask = UpdateFcmTokenPendingTask("").apply {
            this.userManager = userManager
            this.userRepository = userRepository
        }
    }

    override fun runTask(): PendingTaskResult {
        val fcmToken = userManager.pushNotificationDeviceToken

        return if (fcmToken != null && userManager.isUserLoggedIn) {
            val response = userRepository.updateFcmToken(fcmToken)
                    .subscribeOn(Schedulers.io())
                    .blockingGet()

            if (response.isFailure()) PendingTaskResult.FAILED else PendingTaskResult.SUCCESSFUL
        } else {
            PendingTaskResult.FAILED
        }
    }

}