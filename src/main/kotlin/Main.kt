package org.example

fun main() {
    val database = Database()
    for (plugin in loadPlugins()) {
        database.addPlugin(plugin)
    }

    database.insertGlobal("Bill")
    database.insertGlobal("Bob")
    database.insertGlobal("Jane")
    database.insertGlobal(1234)
}