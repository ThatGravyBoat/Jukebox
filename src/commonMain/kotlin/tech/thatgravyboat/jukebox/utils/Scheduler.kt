package tech.thatgravyboat.jukebox.utils

import kotlinx.coroutines.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object Scheduler {

    private val scope = MainScope() + CoroutineName("Jukebox Scheduler")

    fun schedule(initialDelay: Long, period: Long, unit: DurationUnit, runnable: suspend () -> Unit): Job {
        return scope.launch(Dispatchers.Default) {
            if (isActive) delay(initialDelay.toDuration(unit))
            while (isActive) {
                runnable.invoke()
                delay(period.toDuration(unit))
            }
        }
    }

    fun schedule(delay: Long, unit: DurationUnit, runnable: suspend () -> Unit) {
        scope.launch(Dispatchers.Default) {
            if (isActive) {
                delay(delay.toDuration(unit))
                runnable.invoke()
            }
        }
    }

    fun async(runnable: suspend () -> Unit) {
        scope.launch(Dispatchers.Default) {
            runnable.invoke()
        }
    }

    fun Job.invokeCancel(): Boolean {
        this.cancel()
        return this.isCompleted
    }
}