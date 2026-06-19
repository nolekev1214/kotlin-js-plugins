package org.example

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value

class PluginEngine1(val context: Context) : PluginEngineInterface {
    var inputsSatisfied = false
    val main: Value = context
        .getBindings("js")
        .getMember("main")
    val pluginInfo: Value = context
        .getBindings("js")
        .getMember("pluginInfo")

    override fun attemptExecute() {
        if (inputsSatisfied) {
            main.execute()
        }
    }

    override fun shouldTrigger(
        triggerType: String,
        databaseGroup: String,
        type: String
    ): Boolean {
        val trigger = pluginInfo.getMember("trigger")
        return trigger.getMember("triggerType").asString() == triggerType
            && trigger.getMember("databaseGroup").asString() == databaseGroup
            && trigger.getMember("type").asString() == type
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
                return;
            }
            context.getBindings("js").putMember(name, value)
        }
        inputsSatisfied = true
    }

    override fun collectOutputs(): List<Any> {
        TODO("Not yet implemented")
    }
}