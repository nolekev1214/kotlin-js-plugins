package org.example

import java.util.concurrent.ConcurrentHashMap

class Database : PluginDataSource {
    val globals: ConcurrentHashMap<String, Any> = ConcurrentHashMap()
    var plugins: MutableList<PluginEngine> = mutableListOf()

    fun addPlugin(plugin: PluginEngine) {
        plugins.add(plugin)
    }

    fun insertGlobal(value: Any) {
        val key = value.javaClass.name
        globals[key] = value

        plugins.forEach {
            if(it.shouldTrigger("onUpdate", "globals", key)) {
                it.populateInputs(this)
                it.attemptExecute()
            }
        }
    }

    override fun get(databaseGroup: String, type: String): Any? {
        return if (databaseGroup == "globals") {
            globals[type]
        } else {
            null
        }
    }
}