package org.example

import kotlin.time.Duration

data class DurationTriggerEvent(
    val duration: Duration,
) : TriggerEvent
