package org.example

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import java.io.File

fun loadPlugins() : List<PluginEngineInterface> {
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

            when (pluginInfo.getMember("pluginEngine").asInt()) {
                1 -> PluginEngine1(context)
                else -> null
            }
        }
        ?: emptyList()
}