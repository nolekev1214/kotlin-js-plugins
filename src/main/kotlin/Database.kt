package org.example

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap

class Database(
    val triggerEvents: BlockingQueue<TriggerEvent>
) : PluginDataSource {
    val globals: ConcurrentHashMap<String, Any> = ConcurrentHashMap()

    fun insertGlobal(value: Any) {
        val key = value.javaClass.name
        globals[key] = value
        val trigger = DatabaseTriggerEvent(
            databaseGroup = "globals",
            type = key
        )
        triggerEvents.add(trigger)
    }

    override fun get(databaseGroup: String, type: String): Any? {
        return if (databaseGroup == "globals") {
            globals[type]
        } else {
            null
        }
    }
}