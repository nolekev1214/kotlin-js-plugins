package org.example

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import java.io.File
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

class PluginExecutor(
    val database: PluginDataSource,
    var triggerEventsQueue: ArrayBlockingQueue<TriggerEvent>,
    var plugins: List<PluginEngine> = loadPlugins(),
) {
    fun start(){
        thread(start = true, isDaemon = true){
            while(true){
                val triggerEvent = triggerEventsQueue.take()
                plugins.forEach {
                    if(it.shouldTrigger(triggerEvent)) {
                        it.populateInputs(database)
                        it.attemptExecute()
                    }
                }
            }
        }
    }
}

fun loadPlugins() : List<PluginEngine> {
    return File("./plugins")
        .listFiles()
        ?.filter { it.extension == "js" }
        ?.mapNotNull {
            val source = Source.newBuilder("js", it).build()

            val basicContext = Context.newBuilder("js").build()
            basicContext.eval(source)
            val pluginInfo = basicContext
                .getBindings("js")
                .getMember("pluginInfo")

            val builder = Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .allowHostClassLookup { true }
            if (pluginInfo.getMember("debugger").asBoolean()) {
                builder.option("inspect", "true")
            }
            val context = builder.build()
            context.eval(source)

            if (pluginInfo.getMember("pluginEngine").asInt() != PLUGIN_ENGINE_VERSION){
                throw IllegalArgumentException("Plugin engine version mismatch")
            }

            PluginEngine(context)
        }
        ?: emptyList()
}