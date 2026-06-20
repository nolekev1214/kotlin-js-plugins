package org.example

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

class Timer(
    val triggerEvents: ArrayBlockingQueue<TriggerEvent>,
) {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    fun schedule(durationString: String) = schedule(Duration.parse(durationString))

    fun schedule(duration: Duration) {
        val trigger = DurationTriggerEvent(duration)
        scheduler.scheduleAtFixedRate(
            { triggerEvents.add(trigger) },
            duration.inWholeMilliseconds,
            duration.inWholeMilliseconds,
            TimeUnit.MILLISECONDS,
        )
    }

    fun scheduleFromPlugins(plugins: List<PluginEngine>) {
        plugins
            .map { it.trigger }
            .filterIsInstance<DurationTriggerEvent>()
            .distinctBy { it.duration }
            .forEach { schedule(it.duration) }
    }
}
