package org.example

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value
import kotlin.time.Duration

const val PLUGIN_ENGINE_VERSION = 1

class PluginEngine(val context: Context) {
    var inputsSatisfied = false
    val main: Value = context
        .getBindings("js")
        .getMember("main")
    val pluginInfo: Value = context
        .getBindings("js")
        .getMember("pluginInfo")
    val trigger: TriggerEvent

    init {
        println("Plugin Loaded: ${pluginInfo.getMember("description").asString()}")
        val triggerInfo = pluginInfo.getMember("trigger")
        trigger = if (triggerInfo.hasMember("periodic")) {
            DurationTriggerEvent(Duration.parse(triggerInfo.getMember("periodic").asString()))
        } else {
            DatabaseTriggerEvent(
                databaseGroup = triggerInfo.getMember("databaseGroup").asString(),
                type = triggerInfo.getMember("type").asString()
            )
        }
    }

    fun attemptExecute() {
        if (inputsSatisfied) {
            main.execute()
        }
    }

    fun shouldTrigger(
        trigger: TriggerEvent
    ): Boolean {
        return trigger == this.trigger
    }

    fun populateInputs(database: PluginDataSource) {
        val inputs = pluginInfo.getMember("inputs")
        for(i in 0..<inputs.arraySize){
            val input = inputs.getArrayElement(i)
            val type = input.getMember("type").asString()
            val databaseGroup = input.getMember("databaseGroup").asString()
            val name = input.getMember("name").asString()

            val value = database.get(databaseGroup, type)
            if (value == null) {
                inputsSatisfied = false
                return
            }
            context.getBindings("js").putMember(name, value)
        }
        inputsSatisfied = true
    }

    @Suppress("unused")
    fun collectOutputs(): List<Any> {
        TODO("Not yet implemented")
    }
}