package org.example

data class DatabaseTriggerEvent(
    val databaseGroup: String,
    val type: String,
) : TriggerEvent
