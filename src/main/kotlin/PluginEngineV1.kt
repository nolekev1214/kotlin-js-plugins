package org.example

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value

class PluginEngineV1(val context: Context) : PluginEngine {
    var inputsSatisfied = false
    val main: Value = context
        .getBindings("js")
        .getMember("main")
    val pluginInfo: Value = context
        .getBindings("js")
        .getMember("pluginInfo")
    val trigger: TriggerEvent

    init {
        println("Plugin Loaded using Engine v1: ${pluginInfo.getMember("description").asString()}")
        trigger = DatabaseTriggerEvent(
            databaseGroup = pluginInfo.getMember("trigger").getMember("databaseGroup").asString(),
            type = pluginInfo.getMember("trigger").getMember("type").asString()
        )
    }

    override fun attemptExecute() {
        if (inputsSatisfied) {
            main.execute()
        }
    }

    override fun shouldTrigger(
        trigger: TriggerEvent
    ): Boolean {
        return trigger == this.trigger
    }

    override fun populateInputs(database: PluginDataSource) {
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

    override fun collectOutputs(): List<Any> {
        TODO("Not yet implemented")
    }
}