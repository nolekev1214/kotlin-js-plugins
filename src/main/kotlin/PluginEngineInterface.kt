package org.example

sealed interface PluginEngineInterface {
    fun attemptExecute()
    fun shouldTrigger(triggerType: String, databaseGroup: String, type: String): Boolean
    fun populateInputs(database: PluginDataSource)
    fun collectOutputs(): List<Any>
}