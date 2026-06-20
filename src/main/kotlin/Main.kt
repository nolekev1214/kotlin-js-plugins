package org.example

import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue

fun main() {
    val triggerEventsQueue = LinkedBlockingQueue<TriggerEvent>()
    val database = Database(triggerEventsQueue)
    val pluginExecutor = PluginExecutor(database, triggerEventsQueue)
    pluginExecutor.start()

    database.insertGlobal("Bill")
    Thread.sleep(1000) //BUGBUG: if the global channels too fast the plugin executor will miss the event
    database.insertGlobal("Bob")
    Thread.sleep(1000)
    database.insertGlobal("Jane")
    Thread.sleep(1000)
    database.insertGlobal(1234)

    val latch = CountDownLatch(1)
    latch.await()
}